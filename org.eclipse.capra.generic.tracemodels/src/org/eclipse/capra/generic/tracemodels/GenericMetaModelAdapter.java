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
package org.eclipse.capra.generic.tracemodels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.capra.GenericTraceMetaModel.GenericTraceMetaModelFactory;
import org.eclipse.capra.GenericTraceMetaModel.GenericTraceMetaModelPackage;
import org.eclipse.capra.GenericTraceMetaModel.GenericTraceModel;
import org.eclipse.capra.GenericTraceMetaModel.RelatedTo;
import org.eclipse.capra.core.adapters.Connection;
import org.eclipse.capra.core.adapters.TraceMetaModelAdapter;
import org.eclipse.capra.core.helpers.EMFHelper;
import org.eclipse.capra.ui.plantuml.SelectRelationshipsHandler;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.Relationship;
import org.eclipse.uml2.uml.Transition;

/**
 * Provides generic functionality to deal with traceability meta models.
 */
public class GenericMetaModelAdapter implements TraceMetaModelAdapter {

	public GenericMetaModelAdapter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public EObject createModel() {
		return GenericTraceMetaModelFactory.eINSTANCE.createGenericTraceModel();
	}

	@Override
	public Collection<EClass> getAvailableTraceTypes(List<EObject> selection) {
		Collection<EClass> traceTypes = new ArrayList<>();
		if (selection.size() > 1) {
			traceTypes.add(GenericTraceMetaModelPackage.eINSTANCE.getRelatedTo());
		}
		return traceTypes;
	}

	@Override
	public EObject createTrace(EClass traceType, EObject traceModel, List<EObject> selection) {
		GenericTraceModel TM = (GenericTraceModel) traceModel;
		EObject trace = GenericTraceMetaModelFactory.eINSTANCE.create(traceType);
		RelatedTo RelatedToTrace = (RelatedTo) trace;
		RelatedToTrace.getItem().addAll(selection);

		TM.getTraces().add(RelatedToTrace);
		return TM;
	}

	@Override
	public void deleteTrace(EObject first, EObject second, EObject traceModel) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isThereATraceBetween(EObject firstElement, EObject secondElement, EObject traceModel) {
		GenericTraceModel root = (GenericTraceModel) traceModel;
		List<RelatedTo> relevantLinks = new ArrayList<RelatedTo>();
		List<RelatedTo> allTraces = root.getTraces();

		for (RelatedTo trace : allTraces) {
			if (firstElement != secondElement) {
				if (trace.getItem().contains(firstElement) && trace.getItem().contains(secondElement)) {
					relevantLinks.add(trace);
				}
			}
		}
		if (relevantLinks.size() > 0) {
			return true;
		} else
			return false;
	}

	@Override
	public List<Connection> getConnectedElements(EObject element, EObject tracemodel) {
		GenericTraceModel root = (GenericTraceModel) tracemodel;
		List<Connection> connections = new ArrayList<>();
		List<RelatedTo> traces = root.getTraces();

		if (element instanceof RelatedTo) {
			RelatedTo trace = (RelatedTo) element;
			connections.add(new Connection(element, trace.getItem(), trace));
		} else {

			for (RelatedTo trace : traces) {
				if (trace.getItem().contains(element)) {
					connections.add(new Connection(element, trace.getItem(), trace));
				}
			}
		}
		return connections;
	}

	private List<Connection> getTransitivelyConnectedElements(EObject element, EObject traceModel,
			List<Object> accumulator) {
		List<Connection> directElements = getConnectedElements(element, traceModel);
		List<Connection> allElements = new ArrayList<>();

		directElements.forEach(connection -> {
			if (!accumulator.contains(connection.getTlink())) {
				allElements.add(connection);
				accumulator.add(connection.getTlink());
				connection.getTargets().forEach(e -> {
					allElements.addAll(getTransitivelyConnectedElements(e, traceModel, accumulator));
				});
			}
		});

		return allElements;
	}

	@Override
	public List<Connection> getTransitivelyConnectedElements(EObject element, EObject traceModel) {
		List<Object> accumulator = new ArrayList<>();
		return getTransitivelyConnectedElements(element, traceModel, accumulator);
	}

	public List<Connection> getInternalElementsTransitive(EObject element, EObject traceModel,
			List<Object> accumulator) {
		List<Connection> directElements = getInternalElements(element, traceModel);
		List<Connection> allElements = new ArrayList<>();

		directElements.forEach(connection -> {
			if (!accumulator.contains(connection.getTlink())) {
				allElements.add(connection);
				accumulator.add(connection.getTlink());
				connection.getTargets().forEach(e -> {
					allElements.addAll(getInternalElementsTransitive(e, traceModel, accumulator));
				});
			}
		});

		return allElements;
	}

	@Override
	public List<Connection> getInternalElementsTransitive(EObject element, EObject traceModel) {
		List<Object> accumulator = new ArrayList<>();
		return getInternalElementsTransitive(element, traceModel, accumulator);
	}

	@Override
	public List<Connection> getInternalElements(EObject element, EObject traceModel) {
		List<Connection> directElements = getConnectedElements(element, traceModel);
		List<Connection> allElements = new ArrayList<>();
		ArrayList<String> duplicationCheck = new ArrayList<>();
		for (Connection conn : directElements) {
			for (EObject o : conn.getTargets()) {
				addConnectionsForRelations(o, allElements, duplicationCheck);
			}
		}
		addConnectionsForRelations(element, allElements, duplicationCheck);
		return allElements;
	}

	private void addConnectionsForRelations(EObject o, List<Connection> allElements,
			ArrayList<String> duplicationCheck) {
		List<String> selectedRelationshipTypes = SelectRelationshipsHandler.getSelectedRelationshipTypes();
		if (Relationship.class.isAssignableFrom(o.getClass())) {
			if (selectedRelationshipTypes.size() == 0 || selectedRelationshipTypes.contains(o.eClass().getName())) {
				Relationship rel = Relationship.class.cast(o);
				List<EObject> relatedElements = new ArrayList<>();
				rel.getRelatedElements().forEach(element -> relatedElements.add(element));
				Connection conn = new Connection(o, relatedElements, rel);
				allElements.add(conn);
				SelectRelationshipsHandler.addToPossibleRelationsForSelection(rel.eClass().getName());
			}
		} else if (Transition.class.isAssignableFrom(o.getClass())) {
			if (selectedRelationshipTypes.size() == 0 || selectedRelationshipTypes.contains(o.eClass().getName())) {
				Transition transition = Transition.class.cast(o);
				List<EObject> relatedElements = new ArrayList<>();
				relatedElements.add(transition.getSource());
				relatedElements.add(transition.getTarget());
				Connection conn = new Connection(o, relatedElements, transition);
				allElements.add(conn);
				SelectRelationshipsHandler.addToPossibleRelationsForSelection(transition.eClass().getName());
			}
		} else if (Message.class.isAssignableFrom(o.getClass())) {
			if (selectedRelationshipTypes.size() == 0 || selectedRelationshipTypes.contains(o.eClass().getName())) {
				Message msg = Message.class.cast(o);
				MessageOccurrenceSpecification receiver = (MessageOccurrenceSpecification) msg.getReceiveEvent();
				MessageOccurrenceSpecification sender = (MessageOccurrenceSpecification) msg.getSendEvent();
				if (receiver != null) {
					List<EObject> relatedElements = new ArrayList<>();
					relatedElements.add(sender.getCovered());
					relatedElements.add(receiver.getCovered());
					Connection conn = new Connection(o, relatedElements, msg);
					allElements.add(conn);
				}
				SelectRelationshipsHandler.addToPossibleRelationsForSelection(msg.eClass().getName());
			}
		} else {
			EObject root = EcoreUtil.getRootContainer(o);
			TreeIterator<EObject> modelContents = root.eAllContents();
			while (modelContents.hasNext()) {
				EObject content = modelContents.next();
				/*
				 * System.out.println("Name: "+EMFHelper.getNameAttribute(
				 * content));
				 * System.out.println("EClass: "+content.eClass().getName());
				 * System.out.println("EClass: "+content.getClass().toString());
				 * System.out.println("IsRelationship: "+Relationship.class.
				 * isAssignableFrom(content.getClass()));
				 */
				if (selectedRelationshipTypes.size() == 0
						|| selectedRelationshipTypes.contains(content.eClass().getName())) {
					if (Relationship.class.isAssignableFrom(content.getClass())) {
						Relationship relation = Relationship.class.cast(content);
						boolean isRelatedToElement = false;
						List<EObject> relatedElements = new ArrayList<>();
						for (Element relatedElement : relation.getRelatedElements()) {
							if (EMFHelper.getNameAttribute(relatedElement).equals(EMFHelper.getNameAttribute(o))) {
								isRelatedToElement = true;
							} else {
								relatedElements.add(relatedElement);
							}
						}
						if (isRelatedToElement) {
							if (!isDuplicatedEntry(o, relatedElements, relation, duplicationCheck)) {
								Connection conn = new Connection(o, relatedElements, relation);
								allElements.add(conn);
								addPotentialStringsForConnection(o, relatedElements, relation, duplicationCheck);
								SelectRelationshipsHandler
										.addToPossibleRelationsForSelection(relation.eClass().getName());
							}
						}
					} else if (Transition.class.isAssignableFrom(content.getClass())) {
						Transition transition = Transition.class.cast(content);
						List<EObject> relatedElements = new ArrayList<>();
						if (EMFHelper.getNameAttribute(transition.getSource()).equals(EMFHelper.getNameAttribute(o))) {
							relatedElements.add(transition.getTarget());
							if (!isDuplicatedEntry(o, relatedElements, transition, duplicationCheck)) {
								Connection conn = new Connection(o, relatedElements, transition);
								allElements.add(conn);
								addPotentialStringsForConnection(o, relatedElements, transition, duplicationCheck);
								SelectRelationshipsHandler
										.addToPossibleRelationsForSelection(transition.eClass().getName());
							}
						} else if (EMFHelper.getNameAttribute(transition.getTarget())
								.equals(EMFHelper.getNameAttribute(o))) {
							relatedElements.add(transition.getSource());
							if (!isDuplicatedEntry(o, relatedElements, transition, duplicationCheck)) {
								Connection conn = new Connection(o, relatedElements, transition);
								allElements.add(conn);
								addPotentialStringsForConnection(o, relatedElements, transition, duplicationCheck);
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
									.equals(EMFHelper.getNameAttribute(o))) {
								relatedElements.add(sender.getCovered());
								if (!isDuplicatedEntry(o, relatedElements, msg, msg.getMessageSort(),
										duplicationCheck)) {
									Connection conn = new Connection(o, relatedElements, msg);
									allElements.add(conn);
									addPotentialStringsForConnection(o, relatedElements, msg, msg.getMessageSort(),
											duplicationCheck);
									SelectRelationshipsHandler
											.addToPossibleRelationsForSelection(msg.eClass().getName());
								}
							} else if (EMFHelper.getNameAttribute(sender.getCovered())
									.equals(EMFHelper.getNameAttribute(o))) {
								relatedElements.add(receiver.getCovered());
								if (!isDuplicatedEntry(o, relatedElements, msg, msg.getMessageSort(),
										duplicationCheck)) {
									Connection conn = new Connection(o, relatedElements, msg);
									allElements.add(conn);
									addPotentialStringsForConnection(o, relatedElements, msg, msg.getMessageSort(),
											duplicationCheck);
									SelectRelationshipsHandler
											.addToPossibleRelationsForSelection(msg.eClass().getName());
								}
							}
						}
					}
				}
			}
		}
	}

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

	private static boolean isDuplicatedEntry(EObject source, List<EObject> targets, EObject relation,
			List<String> duplicationCheck) {
		String connectionString = EMFHelper.getNameAttribute(source);
		for (EObject target : targets) {
			connectionString += EMFHelper.getNameAttribute(target);
		}
		connectionString += EMFHelper.getNameAttribute(relation);
		return duplicationCheck.contains(connectionString);
	}

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
}
