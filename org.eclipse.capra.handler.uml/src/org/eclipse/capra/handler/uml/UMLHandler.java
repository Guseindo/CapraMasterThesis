/*******************************************************************************
 * Copyright (c) 2016 Chalmers | University of Gothenburg, rt-labs and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *   Contributors:
 *      Chalmers | University of Gothenburg and rt-labs - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.capra.handler.uml;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capra.core.adapters.Connection;
import org.eclipse.capra.core.handlers.AbstractArtifactHandler;
import org.eclipse.capra.core.helpers.EMFHelper;
import org.eclipse.capra.ui.plantuml.SelectRelationshipsHandler;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.DirectedRelationship;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Relationship;
import org.eclipse.uml2.uml.Transition;

/**
 * Handler to allow tracing to and from arbitrary model elements handled by EMF.
 */
public class UMLHandler extends AbstractArtifactHandler<EModelElement> {

	@Override
	public EObject createWrapper(EModelElement artifact, EObject artifactModel) {
		return artifact;
	}

	@Override
	public EModelElement resolveWrapper(EObject wrapper) {
		return (EModelElement) wrapper;
	}

	@Override
	public String getDisplayName(EModelElement artifact) {
		return artifact.eClass().getName();
	}

	@Override
	public void addInternalLinks(EObject investigatedElement, List<Connection> allElements,
			ArrayList<String> duplicationCheck, List<String> selectedRelationshipTypes) {
		if (Relationship.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				Relationship rel = Relationship.class.cast(investigatedElement);
				List<EObject> relatedElements = new ArrayList<>();
				rel.getRelatedElements().forEach(element -> relatedElements.add(element));
				if (!isDuplicatedEntry(investigatedElement, relatedElements, rel, duplicationCheck)) {
					Connection conn = new Connection(investigatedElement, relatedElements, rel);
					allElements.add(conn);
					addPotentialStringsForConnection(investigatedElement, relatedElements, rel, duplicationCheck);
					SelectRelationshipsHandler.addToPossibleRelationsForSelection(rel.eClass().getName());
				}
			}
		} else if (Transition.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				Transition transition = Transition.class.cast(investigatedElement);
				List<EObject> relatedElements = new ArrayList<>();
				relatedElements.add(transition.getSource());
				relatedElements.add(transition.getTarget());
				if (!isDuplicatedEntry(investigatedElement, relatedElements, transition, duplicationCheck)) {
					Connection conn = new Connection(investigatedElement, relatedElements, transition);
					allElements.add(conn);
					addPotentialStringsForConnection(investigatedElement, relatedElements, transition,
							duplicationCheck);
					SelectRelationshipsHandler.addToPossibleRelationsForSelection(transition.eClass().getName());
				}
			}
		} else if (Message.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				Message msg = Message.class.cast(investigatedElement);
				MessageOccurrenceSpecification receiver = (MessageOccurrenceSpecification) msg.getReceiveEvent();
				MessageOccurrenceSpecification sender = (MessageOccurrenceSpecification) msg.getSendEvent();
				List<EObject> relatedElements = new ArrayList<>();
				relatedElements.add(sender.getCovered());
				relatedElements.add(receiver.getCovered());
				if (receiver != null) {
					if (!isDuplicatedEntry(investigatedElement, relatedElements, msg, msg.getMessageSort(),
							duplicationCheck)) {
						Connection conn = new Connection(investigatedElement, relatedElements, msg);
						allElements.add(conn);
						addPotentialStringsForConnection(investigatedElement, relatedElements, msg,
								msg.getMessageSort(), duplicationCheck);
						SelectRelationshipsHandler.addToPossibleRelationsForSelection(msg.eClass().getName());
					}
				}
			}
		} else if (Connector.class.isAssignableFrom(investigatedElement.getClass())) {
			Connector connector = Connector.class.cast(investigatedElement);
			System.out.println(connector.getType());
			EList<ConnectorEnd> connectedEnds = connector.getEnds();
			List<EObject> relatedElements = new ArrayList<>();
			connectedEnds.forEach(connectedEnd -> {
				if (connectedEnd.getPartWithPort() != null) {
					relatedElements.add(connectedEnd.getPartWithPort());
				} else {
					relatedElements.add(connectedEnd);
				}
			});
			if (!isDuplicatedEntry(investigatedElement, relatedElements, connector, duplicationCheck)) {
				Connection conn = new Connection(investigatedElement, relatedElements, connector);
				allElements.add(conn);
				addPotentialStringsForConnection(investigatedElement, relatedElements, connector, duplicationCheck);
				SelectRelationshipsHandler.addToPossibleRelationsForSelection(connector.eClass().getName());
			}
		} else {
			EObject root = EcoreUtil.getRootContainer(investigatedElement);
			TreeIterator<EObject> modelContents = root.eAllContents();
			while (modelContents.hasNext()) {
				EObject content = modelContents.next();
				if (selectedRelationshipTypes.size() == 0
						|| selectedRelationshipTypes.contains(content.eClass().getName())) {
					if (Relationship.class.isAssignableFrom(content.getClass())) {
						Relationship relation = Relationship.class.cast(content);
						boolean isRelatedToElement = false;
						List<EObject> relatedElements = new ArrayList<>();
						for (Element relatedElement : relation.getRelatedElements()) {
							if (EMFHelper.getNameAttribute(relatedElement)
									.equals(EMFHelper.getNameAttribute(investigatedElement))) {
								isRelatedToElement = true;
							} else {
								relatedElements.add(relatedElement);
							}
						}
						if (isRelatedToElement) {
							if (!isDuplicatedEntry(investigatedElement, relatedElements, relation, duplicationCheck)) {
								Connection conn = new Connection(investigatedElement, relatedElements, relation);
								allElements.add(conn);
								addPotentialStringsForConnection(investigatedElement, relatedElements, relation,
										duplicationCheck);
								SelectRelationshipsHandler
										.addToPossibleRelationsForSelection(relation.eClass().getName());
							}
						}
					} else if (Transition.class.isAssignableFrom(content.getClass())) {
						Transition transition = Transition.class.cast(content);
						List<EObject> relatedElements = new ArrayList<>();
						if (EMFHelper.getNameAttribute(transition.getSource())
								.equals(EMFHelper.getNameAttribute(investigatedElement))) {
							relatedElements.add(transition.getTarget());
							if (!isDuplicatedEntry(investigatedElement, relatedElements, transition,
									duplicationCheck)) {
								Connection conn = new Connection(investigatedElement, relatedElements, transition);
								allElements.add(conn);
								addPotentialStringsForConnection(investigatedElement, relatedElements, transition,
										duplicationCheck);
								SelectRelationshipsHandler
										.addToPossibleRelationsForSelection(transition.eClass().getName());
							}
						} else if (EMFHelper.getNameAttribute(transition.getTarget())
								.equals(EMFHelper.getNameAttribute(investigatedElement))) {
							relatedElements.add(transition.getSource());
							if (!isDuplicatedEntry(investigatedElement, relatedElements, transition,
									duplicationCheck)) {
								Connection conn = new Connection(investigatedElement, relatedElements, transition);
								allElements.add(conn);
								addPotentialStringsForConnection(investigatedElement, relatedElements, transition,
										duplicationCheck);
								SelectRelationshipsHandler
										.addToPossibleRelationsForSelection(transition.eClass().getName());
							}
						}
					} else if (Message.class.isAssignableFrom(content.getClass())) {
						Message msg = Message.class.cast(content);
						MessageOccurrenceSpecification receiver = (MessageOccurrenceSpecification) msg
								.getReceiveEvent();
						MessageOccurrenceSpecification sender = (MessageOccurrenceSpecification) msg.getSendEvent();
						List<EObject> relatedElements = new ArrayList<>();
						if (receiver != null) {
							if (EMFHelper.getNameAttribute(receiver.getCovered())
									.equals(EMFHelper.getNameAttribute(investigatedElement))) {
								relatedElements.add(sender.getCovered());
								if (!isDuplicatedEntry(investigatedElement, relatedElements, msg, msg.getMessageSort(),
										duplicationCheck)) {
									Connection conn = new Connection(investigatedElement, relatedElements, msg);
									allElements.add(conn);
									addPotentialStringsForConnection(investigatedElement, relatedElements, msg,
											msg.getMessageSort(), duplicationCheck);
									SelectRelationshipsHandler
											.addToPossibleRelationsForSelection(msg.eClass().getName());
								}
							} else if (EMFHelper.getNameAttribute(sender.getCovered())
									.equals(EMFHelper.getNameAttribute(investigatedElement))) {
								relatedElements.add(receiver.getCovered());
								if (!isDuplicatedEntry(investigatedElement, relatedElements, msg, msg.getMessageSort(),
										duplicationCheck)) {
									Connection conn = new Connection(investigatedElement, relatedElements, msg);
									allElements.add(conn);
									addPotentialStringsForConnection(investigatedElement, relatedElements, msg,
											msg.getMessageSort(), duplicationCheck);
									SelectRelationshipsHandler
											.addToPossibleRelationsForSelection(msg.eClass().getName());
								}
							}
						}
					} else if (Port.class.isAssignableFrom(content.getClass())) {
						if (selectedRelationshipTypes.size() == 0
								|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
							Port port = Port.class.cast(content);
							EList<Interface> provideds = port.getProvideds();
							boolean investigatedIsProvided = false;
							for (Interface provided : provideds) {
								if (EMFHelper.getNameAttribute(investigatedElement)
										.equals(EMFHelper.getNameAttribute(provided))) {
									investigatedIsProvided = true;
								}
							}
							EList<Interface> requireds = port.getRequireds();
							boolean investigatedIsRequired = false;
							for (Interface required : requireds) {
								if (EMFHelper.getNameAttribute(investigatedElement)
										.equals(EMFHelper.getNameAttribute(required))) {
									investigatedIsRequired = true;
								}

							}
							List<EObject> relatedElements = new ArrayList<>();

							if (investigatedIsProvided) {
								relatedElements.addAll(requireds);
							} else if (investigatedIsRequired) {
								relatedElements.addAll(provideds);
							}
							if (investigatedIsProvided || investigatedIsRequired) {
								if (!isDuplicatedEntry(investigatedElement, relatedElements, port, duplicationCheck)) {
									Connection conn = new Connection(investigatedElement, relatedElements, port);
									allElements.add(conn);
									addPotentialStringsForConnection(investigatedElement, relatedElements, port,
											duplicationCheck);
									SelectRelationshipsHandler
											.addToPossibleRelationsForSelection(port.eClass().getName());
								}
							}
						}
					} else if (Connector.class.isAssignableFrom(content.getClass())) {
						Connector connector = Connector.class.cast(content);
						EList<ConnectorEnd> connectedEnds = connector.getEnds();
						List<EObject> relatedElements = new ArrayList<>();
						boolean isConnected = false;
						for (ConnectorEnd connectedEnd : connectedEnds) {
							if (connectedEnd.getPartWithPort() != null) {
								relatedElements.add(connectedEnd.getPartWithPort());
								if (EMFHelper.getNameAttribute(investigatedElement)
										.equals(EMFHelper.getNameAttribute(connectedEnd.getPartWithPort()))) {
									isConnected = true;
									relatedElements.remove(connectedEnd.getPartWithPort());
								}
							} else {
								relatedElements.add(connectedEnd);
								if (EMFHelper.getNameAttribute(investigatedElement)
										.equals(EMFHelper.getNameAttribute(connectedEnd))) {
									isConnected = true;
									relatedElements.remove(connectedEnd);
								}
							}
						}
						;
						if (isConnected) {
							if (!isDuplicatedEntry(investigatedElement, relatedElements, connector, duplicationCheck)) {
								Connection conn = new Connection(investigatedElement, relatedElements, connector);
								allElements.add(conn);
								addPotentialStringsForConnection(investigatedElement, relatedElements, connector,
										duplicationCheck);
								SelectRelationshipsHandler
										.addToPossibleRelationsForSelection(connector.eClass().getName());
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Adds all possible relations-ship strings as a concatenated string for a
	 * relation between two elements
	 * 
	 * @param source
	 *            The source element of the relation
	 * @param targets
	 *            The target element of the relation
	 * @param relation
	 *            The relation between the elements
	 * @param duplicationCheck
	 *            The list of strings the potential relation-ship strings are
	 *            added to
	 */
	private static void addPotentialStringsForConnection(EObject source, List<EObject> targets, EObject relation,
			List<String> duplicationCheck) {
		String potentialString = EMFHelper.getNameAttribute(source);
		for (EObject target : targets) {
			potentialString += EMFHelper.getNameAttribute(target);
		}
		potentialString += EMFHelper.getNameAttribute(relation);
		duplicationCheck.add(potentialString);

		potentialString = "";
		for (EObject target : targets) {
			potentialString += EMFHelper.getNameAttribute(target);
		}
		potentialString += EMFHelper.getNameAttribute(source);
		potentialString += EMFHelper.getNameAttribute(relation);
		duplicationCheck.add(potentialString);
	}

	/**
	 * Checks if a connection for a relation between two elements already exists
	 * 
	 * @param source
	 *            The source element of the relation
	 * @param targets
	 *            The target element of the relation
	 * @param relation
	 *            The relation between the elements
	 * @param duplicationCheck
	 *            List of concatenated strings containing all possible
	 *            combinations for each relationship added to the tracemodel
	 * @return
	 */
	private static boolean isDuplicatedEntry(EObject source, List<EObject> targets, EObject relation,
			List<String> duplicationCheck) {
		String connectionString = EMFHelper.getNameAttribute(source);
		for (EObject target : targets) {
			connectionString += EMFHelper.getNameAttribute(target);
		}
		connectionString += EMFHelper.getNameAttribute(relation);
		return duplicationCheck.contains(connectionString);
	}

	/**
	 * Adds all possible relations-ship strings as a concatenated string for a
	 * relation between two elements. This method is specific for messages as
	 * the message-type plays a role in identifying duplicates.
	 * 
	 * @param source
	 *            The source element of the relation
	 * @param targets
	 *            The target element of the relation
	 * @param relation
	 *            The relation between the elements
	 * @param msgSort
	 *            The type of message
	 * @param duplicationCheck
	 *            The list of strings the potential relation-ship strings are
	 *            added to
	 */
	private static void addPotentialStringsForConnection(EObject source, List<EObject> targets, EObject relation,
			MessageSort msgSort, List<String> duplicationCheck) {
		String potentialString = EMFHelper.getNameAttribute(source);
		for (EObject target : targets) {
			potentialString += EMFHelper.getNameAttribute(target);
		}
		potentialString += EMFHelper.getNameAttribute(relation);
		potentialString += msgSort.getName();
		duplicationCheck.add(potentialString);

		potentialString = "";
		for (EObject target : targets) {
			potentialString += EMFHelper.getNameAttribute(target);
		}
		potentialString += EMFHelper.getNameAttribute(source);
		potentialString += EMFHelper.getNameAttribute(relation);
		potentialString += msgSort.getName();
		duplicationCheck.add(potentialString);
	}

	/**
	 * Checks if a connection for a relation between two elements already exists
	 * This method is specific for messages as the type of message is relevant
	 * for duplication check as well.
	 * 
	 * @param source
	 *            The source element of the relation
	 * @param targets
	 *            The target element of the relation
	 * @param relation
	 *            The relation between the elements
	 * @param msgSort
	 *            The type of message
	 * @param duplicationCheck
	 *            List of concatenated strings containing all possible
	 *            combinations for each relationship added to the tracemodel
	 * @return
	 */
	private static boolean isDuplicatedEntry(EObject source, List<EObject> targets, EObject relation,
			MessageSort msgSort, List<String> duplicationCheck) {
		String connectionString = EMFHelper.getNameAttribute(source);
		for (EObject target : targets) {
			connectionString += EMFHelper.getNameAttribute(target);
		}
		connectionString += EMFHelper.getNameAttribute(relation);
		connectionString += msgSort.getName();
		return duplicationCheck.contains(connectionString);
	}

	@Override
	public String isThereATraceBetween(EObject first, EObject second, EObject traceModel) {
		String traceString = "";
		if (Relationship.class.isAssignableFrom(first.getClass())
				|| Relationship.class.isAssignableFrom(second.getClass())) {
			Relationship rel;
			if (Relationship.class.isAssignableFrom(first.getClass())) {
				rel = Relationship.class.cast(first);
			} else {
				rel = Relationship.class.cast(second);
			}
			boolean isRelated = false;
			String firstElementName = EMFHelper.getNameAttribute(first);
			String secondElementName = EMFHelper.getNameAttribute(second);
			for (Element relatedElement : rel.getRelatedElements()) {
				String relatedElementName = EMFHelper.getNameAttribute(relatedElement);
				if (relatedElementName.equals(firstElementName) || relatedElementName.equals(secondElementName)) {
					isRelated = true;
				}
			}
			if (isRelated) {
				return "X";
			}
			return "";
		} else if (Transition.class.isAssignableFrom(first.getClass())
				|| Transition.class.isAssignableFrom(second.getClass())) {
			Transition transition;
			if (Relationship.class.isAssignableFrom(first.getClass())) {
				transition = Transition.class.cast(first);
			} else {
				transition = Transition.class.cast(second);
			}
			String sourceName = EMFHelper.getNameAttribute(transition.getSource());
			String targetName = EMFHelper.getNameAttribute(transition.getTarget());
			String firstElementName = EMFHelper.getNameAttribute(first);
			String secondElementName = EMFHelper.getNameAttribute(second);
			boolean relationContainsFirstElement = sourceName.equals(firstElementName)
					|| targetName.equals(firstElementName);
			boolean relationContainsSecondElement = sourceName.equals(secondElementName)
					|| targetName.equals(secondElementName);
			if (relationContainsFirstElement && relationContainsSecondElement) {
				return "X";
			}
			return "";
		} else if (Message.class.isAssignableFrom(first.getClass())
				|| Message.class.isAssignableFrom(second.getClass())) {
			Message msg;
			if (Relationship.class.isAssignableFrom(first.getClass())) {
				msg = Message.class.cast(first);
			} else {
				msg = Message.class.cast(second);
			}
			MessageOccurrenceSpecification receiver = (MessageOccurrenceSpecification) msg.getReceiveEvent();
			MessageOccurrenceSpecification sender = (MessageOccurrenceSpecification) msg.getSendEvent();
			if (receiver != null) {
				String sourceName = EMFHelper.getNameAttribute(sender.getCovered());
				String targetName = EMFHelper.getNameAttribute(receiver.getCovered());
				String firstElementName = EMFHelper.getNameAttribute(first);
				String secondElementName = EMFHelper.getNameAttribute(second);
				boolean relationContainsFirstElement = sourceName.equals(firstElementName)
						|| targetName.equals(firstElementName);
				boolean relationContainsSecondElement = sourceName.equals(secondElementName)
						|| targetName.equals(secondElementName);
				if (relationContainsFirstElement && relationContainsSecondElement) {
					return msg.getMessageSort().getName();
				}
				return "";
			}
			return "";
		} else if (Connector.class.isAssignableFrom(first.getClass())
				|| Connector.class.isAssignableFrom(second.getClass())) {
			Connector connector;
			if (Connector.class.isAssignableFrom(first.getClass())) {
				connector = Connector.class.cast(first);
			} else {
				connector = Connector.class.cast(second);
			}
			EList<ConnectorEnd> connectedEnds = connector.getEnds();
			String firstElementName = EMFHelper.getNameAttribute(first);
			String secondElementName = EMFHelper.getNameAttribute(second);
			boolean isRelated = false;
			for (ConnectorEnd connectedEnd : connectedEnds) {
				String relatedElementName = "";
				if (connectedEnd.getPartWithPort() != null) {
					relatedElementName = EMFHelper.getNameAttribute(connectedEnd.getPartWithPort());
				} else {
					relatedElementName = EMFHelper.getNameAttribute(connectedEnd);
				}
				if (relatedElementName.equals(firstElementName) || relatedElementName.equals(secondElementName)) {
					isRelated = true;
				}
			}
			if (isRelated) {
				return "X";
			}
			return "";
		} else {
			EObject root = EcoreUtil.getRootContainer(first);
			TreeIterator<EObject> modelContents = root.eAllContents();
			boolean isRelated = false;
			String leftArrow = Character.toString((char) 0x2190);
			String upArrow = Character.toString((char) 0x2191);
			while (modelContents.hasNext()) {
				EObject content = modelContents.next();
				if (Relationship.class.isAssignableFrom(content.getClass())) {
					Relationship relation = Relationship.class.cast(content);
					boolean relationContainsFirstElement = false;
					boolean relationContainsSecondElement = false;
					String firstElementName = EMFHelper.getNameAttribute(first);
					String secondElementName = EMFHelper.getNameAttribute(second);
					for (Element relatedElement : relation.getRelatedElements()) {
						String relatedElementName = EMFHelper.getNameAttribute(relatedElement);
						if (relatedElementName.equals(firstElementName)) {
							relationContainsFirstElement = true;
						} else if (relatedElementName.equals(secondElementName)) {
							relationContainsSecondElement = true;
						}
					}
					if (!isRelated) {
						isRelated = relationContainsFirstElement && relationContainsSecondElement;
					}
					if (relationContainsFirstElement && relationContainsSecondElement) {
						if (DirectedRelationship.class.isAssignableFrom(content.getClass())) {
							DirectedRelationship dirRel = DirectedRelationship.class.cast(content);
							List<String> sourceNames = new ArrayList<>();
							for (Element elem : dirRel.getSources()) {
								sourceNames.add(EMFHelper.getNameAttribute(elem));
							}
							if (sourceNames.contains(firstElementName)) {
								if (traceString == "") {
									traceString = relation.eClass().getName() + upArrow;
								} else {
									if (!traceString.toLowerCase().contains(relation.eClass().getName() + upArrow)) {
										traceString += ", " + relation.eClass().getName() + upArrow;
									}
								}
							} else {
								if (traceString == "") {
									traceString = leftArrow + relation.eClass().getName();
								} else {
									if (!traceString.toLowerCase().contains(leftArrow + relation.eClass().getName())) {
										traceString += ", " + leftArrow + relation.eClass().getName();
									}
								}
							}
						} else {
							if (traceString == "") {
								traceString = relation.eClass().getName();
							} else {
								if (!traceString.toLowerCase().contains(relation.eClass().getName())) {
									traceString += ", " + relation.eClass().getName();
								}
							}
						}
					}
				} else if (Transition.class.isAssignableFrom(content.getClass())) {
					Transition transition = Transition.class.cast(content);
					String sourceName = EMFHelper.getNameAttribute(transition.getSource());
					String targetName = EMFHelper.getNameAttribute(transition.getTarget());
					String firstElementName = EMFHelper.getNameAttribute(first);
					String secondElementName = EMFHelper.getNameAttribute(second);
					boolean relationContainsFirstElement = sourceName.equals(firstElementName)
							|| targetName.equals(firstElementName);
					boolean relationContainsSecondElement = sourceName.equals(secondElementName)
							|| targetName.equals(secondElementName);
					if (!isRelated) {
						isRelated = relationContainsFirstElement && relationContainsSecondElement;
					}
					if (relationContainsFirstElement && relationContainsSecondElement) {
						if (sourceName.equals(firstElementName)) {
							if (traceString == "") {
								traceString = transition.eClass().getName() + upArrow;
							} else {
								if (!traceString.toLowerCase().contains(transition.eClass().getName() + upArrow)) {
									traceString += ", " + transition.eClass().getName() + upArrow;
								}
							}
						} else {
							if (traceString == "") {
								traceString = leftArrow + transition.eClass().getName();
							} else {
								if (!traceString.toLowerCase().contains(leftArrow + transition.eClass().getName())) {
									traceString += ", " + leftArrow + transition.eClass().getName();
								}
							}
						}
					}
				} else if (Message.class.isAssignableFrom(content.getClass())) {
					Message msg = Message.class.cast(content);
					MessageOccurrenceSpecification receiver = (MessageOccurrenceSpecification) msg.getReceiveEvent();
					MessageOccurrenceSpecification sender = (MessageOccurrenceSpecification) msg.getSendEvent();
					if (receiver != null) {
						String sourceName = EMFHelper.getNameAttribute(sender.getCovered());
						String targetName = EMFHelper.getNameAttribute(receiver.getCovered());
						String firstElementName = EMFHelper.getNameAttribute(first);
						String secondElementName = EMFHelper.getNameAttribute(second);
						boolean relationContainsFirstElement = sourceName.equals(firstElementName)
								|| targetName.equals(firstElementName);
						boolean relationContainsSecondElement = sourceName.equals(secondElementName)
								|| targetName.equals(secondElementName);
						if (!isRelated) {
							isRelated = relationContainsFirstElement && relationContainsSecondElement;
						}
						if (relationContainsFirstElement && relationContainsSecondElement) {
							if (sourceName.equals(firstElementName)) {
								if (traceString == "") {
									traceString = msg.eClass().getName() + ":" + msg.getMessageSort().getName() + " "
											+ upArrow;
								} else {
									if (!traceString.toLowerCase().contains(msg.eClass().getName() + ":"
											+ msg.getMessageSort().getName() + " " + upArrow)) {
										traceString += ", " + msg.eClass().getName() + ":"
												+ msg.getMessageSort().getName() + " " + upArrow;
									}
								}
							} else {
								if (traceString == "") {
									traceString = leftArrow + msg.eClass().getName() + ":"
											+ msg.getMessageSort().getName();
								} else {
									if (!traceString.toLowerCase().contains(leftArrow + msg.eClass().getName() + ":"
											+ msg.getMessageSort().getName())) {
										traceString += ", " + leftArrow + msg.eClass().getName() + ":"
												+ msg.getMessageSort().getName();
									}
								}
							}
						}
					}
				} else if (Connector.class.isAssignableFrom(content.getClass())) {
					Connector connector = Connector.class.cast(content);
					EList<ConnectorEnd> connectedEnds = connector.getEnds();
					boolean relationContainsFirstElement = false;
					boolean relationContainsSecondElement = false;
					String firstElementName = EMFHelper.getNameAttribute(first);
					String secondElementName = EMFHelper.getNameAttribute(second);
					for (ConnectorEnd connectedEnd : connectedEnds) {
						String relatedElementName = "";
						if (connectedEnd.getPartWithPort() != null) {
							relatedElementName = EMFHelper.getNameAttribute(connectedEnd.getPartWithPort());
						} else {
							relatedElementName = EMFHelper.getNameAttribute(connectedEnd);
						}
						if (relatedElementName.equals(firstElementName)) {
							relationContainsFirstElement = true;
						} else if (relatedElementName.equals(secondElementName)) {
							relationContainsSecondElement = true;
						}
					}
					if (!isRelated) {
						isRelated = relationContainsFirstElement && relationContainsSecondElement;
					}
					if (relationContainsFirstElement && relationContainsSecondElement) {
						if (traceString == "") {
							traceString = connector.getName();
						} else {
							if (!traceString.toLowerCase().contains(connector.getName())) {
								traceString += ", " + connector.getName();
							}
						}
					}
				}
			}
			return traceString;
		}

	}
}
