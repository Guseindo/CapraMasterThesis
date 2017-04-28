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
package org.eclipse.capra.handler.muml;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capra.core.adapters.Connection;
import org.eclipse.capra.core.handlers.AbstractArtifactHandler;
import org.eclipse.capra.core.helpers.EMFHelper;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.muml.core.ExtendableElement;
import org.muml.pim.connector.Connector;
import org.muml.pim.connector.ConnectorEndpoint;
import org.muml.pim.realtimestatechart.Transition;

/**
 * Handler to allow tracing to and from arbitrary model elements handled by EMF.
 */
public class MUMLHandler extends AbstractArtifactHandler<ExtendableElement> {

	@Override
	public EObject createWrapper(ExtendableElement artifact, EObject artifactModel) {
		return artifact;
	}

	@Override
	public ExtendableElement resolveWrapper(EObject wrapper) {
		return (ExtendableElement) wrapper;
	}

	@Override
	public String getDisplayName(ExtendableElement artifact) {
		return artifact.eClass().getName();
	}

	@Override
	public void addInternalLinks(EObject investigatedElement, List<Connection> allElements,
			ArrayList<String> duplicationCheck, List<String> selectedRelationshipTypes) {
		if (Transition.class.isAssignableFrom(investigatedElement.getClass())) {
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
				}
			}
		} else if (Connector.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				Connector connector = Connector.class.cast(investigatedElement);
				EList<ConnectorEndpoint> connectedEnds = connector.getConnectorEndpoints();
				List<EObject> relatedElements = new ArrayList<>();
				connectedEnds.forEach(connectedEnd -> {
					relatedElements.add(connectedEnd);
				});
				if (!isDuplicatedEntry(investigatedElement, relatedElements, connector, duplicationCheck)) {
					Connection conn = new Connection(investigatedElement, relatedElements, connector);
					allElements.add(conn);
					addPotentialStringsForConnection(investigatedElement, relatedElements, connector, duplicationCheck);
				}
			}
		} else {
			EObject root = EcoreUtil.getRootContainer(investigatedElement);
			TreeIterator<EObject> modelContents = root.eAllContents();
			while (modelContents.hasNext()) {
				EObject content = modelContents.next();
				if (selectedRelationshipTypes.size() == 0
						|| selectedRelationshipTypes.contains(content.eClass().getName())) {
					if (Transition.class.isAssignableFrom(content.getClass())) {
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
							}
						}
					} else if (Connector.class.isAssignableFrom(content.getClass())) {
						if (selectedRelationshipTypes.size() == 0
								|| selectedRelationshipTypes.contains(content.eClass().getName())) {
							Connector connector = Connector.class.cast(content);
							EList<ConnectorEndpoint> connectedEnds = connector.getConnectorEndpoints();
							List<EObject> relatedElements = new ArrayList<>();
							boolean isConnected = false;
							for (ConnectorEndpoint connectedEnd : connectedEnds) {
								relatedElements.add(connectedEnd);
								if (EMFHelper.getNameAttribute(investigatedElement)
										.equals(EMFHelper.getNameAttribute(connectedEnd))) {
									isConnected = true;
									relatedElements.remove(connectedEnd);
								}
							}
							if (isConnected) {
								if (!isDuplicatedEntry(investigatedElement, relatedElements, connector,
										duplicationCheck)) {
									Connection conn = new Connection(investigatedElement, relatedElements, connector);
									allElements.add(conn);
									addPotentialStringsForConnection(investigatedElement, relatedElements, connector,
											duplicationCheck);
								}
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

	@Override
	public String isThereAnInternalTraceBetween(EObject first, EObject second, EObject traceModel) {
		String traceString = "";
		if (Transition.class.isAssignableFrom(first.getClass())
				|| Transition.class.isAssignableFrom(second.getClass())) {
			Transition transition;
			if (Transition.class.isAssignableFrom(first.getClass())) {
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
		} else if (Connector.class.isAssignableFrom(first.getClass())
				|| Connector.class.isAssignableFrom(second.getClass())) {
			Connector connector;
			if (Connector.class.isAssignableFrom(first.getClass())) {
				connector = Connector.class.cast(first);
			} else {
				connector = Connector.class.cast(second);
			}
			EList<ConnectorEndpoint> connectedEnds = connector.getConnectorEndpoints();
			String firstElementName = EMFHelper.getNameAttribute(first);
			String secondElementName = EMFHelper.getNameAttribute(second);
			boolean isRelated = false;
			for (ConnectorEndpoint connectedEnd : connectedEnds) {
				String relatedElementName = "";
				relatedElementName = EMFHelper.getNameAttribute(connectedEnd);
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
				if (Transition.class.isAssignableFrom(content.getClass())) {
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
				} else if (Connector.class.isAssignableFrom(content.getClass())) {
					Connector connector = Connector.class.cast(content);
					EList<ConnectorEndpoint> connectedEnds = connector.getConnectorEndpoints();
					boolean relationContainsFirstElement = false;
					boolean relationContainsSecondElement = false;
					String firstElementName = EMFHelper.getNameAttribute(first);
					String secondElementName = EMFHelper.getNameAttribute(second);
					for (ConnectorEndpoint connectedEnd : connectedEnds) {
						String relatedElementName = "";
						relatedElementName = EMFHelper.getNameAttribute(connectedEnd);
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
							traceString = connector.getClass().getName();
						} else {
							if (!traceString.toLowerCase().contains(connector.getClass().getName())) {
								traceString += ", " + connector.getClass().getName();
							}
						}
					}
				}
			}
			return traceString;
		}

	}
}
