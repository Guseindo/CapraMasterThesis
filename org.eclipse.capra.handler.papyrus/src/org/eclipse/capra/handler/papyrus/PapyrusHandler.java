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
package org.eclipse.capra.handler.papyrus;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capra.core.adapters.Connection;
import org.eclipse.capra.core.handlers.AbstractArtifactHandler;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.emf.facet.custom.metamodel.v0_2_0.internal.treeproxy.EObjectTreeElement;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;

/**
 * A handler to create trace links from and to model elements created in
 * Papyrus.
 */
public class PapyrusHandler extends AbstractArtifactHandler<EObjectTreeElement> {

	@Override
	public EObject createWrapper(EObjectTreeElement artifact, EObject artifactModel) {
		// Returns the EObject corresponding to the input object if the input is
		// an EObject, or if it is Adaptable to an EObject
		return EMFHelper.getEObject(artifact);
	}

	@Override
	public EObjectTreeElement resolveWrapper(EObject wrapper) {
		return (EObjectTreeElement) wrapper; // TODO
	}

	@Override
	public String getDisplayName(EObjectTreeElement artifact) {
		EObject sel = EMFHelper.getEObject(artifact);
		return org.eclipse.capra.core.helpers.EMFHelper.getIdentifier(sel); // TODO
	}

	public List<Connection> getInternalElements(EObject element, EObject traceModel) {
		/*
		 * SelectRelationshipsHandler.setPreviousElement(element);
		 * 
		 * List<Connection> directElements = getConnectedElements(element,
		 * traceModel); List<Connection> allElements = new ArrayList<>();
		 * ArrayList<String> duplicationCheck = new ArrayList<>(); /* for
		 * (Connection conn : directElements) { for (EObject o :
		 * conn.getTargets()) { addConnectionsForRelations(o, allElements,
		 * duplicationCheck); } } addConnectionsForRelations(element,
		 * allElements, duplicationCheck);
		 * 
		 * return allElements;
		 */
		return new ArrayList<Connection>();
	}

	/*
	 * private void addConnectionsForRelations(EObject o, List<Connection>
	 * allElements, ArrayList<String> duplicationCheck) { List<String>
	 * selectedRelationshipTypes =
	 * SelectRelationshipsHandler.getSelectedRelationshipTypes(); if
	 * (Relationship.class.isAssignableFrom(o.getClass())) { if
	 * (selectedRelationshipTypes.size() == 0 ||
	 * selectedRelationshipTypes.contains(o.eClass().getName())) { Relationship
	 * rel = Relationship.class.cast(o); List<EObject> relatedElements = new
	 * ArrayList<>(); rel.getRelatedElements().forEach(element ->
	 * relatedElements.add(element)); Connection conn = new Connection(o,
	 * relatedElements, rel); allElements.add(conn);
	 * SelectRelationshipsHandler.addToPossibleRelationsForSelection(rel.eClass(
	 * ).getName()); } } else if
	 * (Transition.class.isAssignableFrom(o.getClass())) { if
	 * (selectedRelationshipTypes.size() == 0 ||
	 * selectedRelationshipTypes.contains(o.eClass().getName())) { Transition
	 * transition = Transition.class.cast(o); List<EObject> relatedElements =
	 * new ArrayList<>(); relatedElements.add(transition.getSource());
	 * relatedElements.add(transition.getTarget()); Connection conn = new
	 * Connection(o, relatedElements, transition); allElements.add(conn);
	 * SelectRelationshipsHandler.addToPossibleRelationsForSelection(transition.
	 * eClass().getName()); } } else if
	 * (Message.class.isAssignableFrom(o.getClass())) { if
	 * (selectedRelationshipTypes.size() == 0 ||
	 * selectedRelationshipTypes.contains(o.eClass().getName())) { Message msg =
	 * Message.class.cast(o); MessageOccurrenceSpecification receiver =
	 * (MessageOccurrenceSpecification) msg.getReceiveEvent();
	 * MessageOccurrenceSpecification sender = (MessageOccurrenceSpecification)
	 * msg.getSendEvent(); if (receiver != null) { List<EObject> relatedElements
	 * = new ArrayList<>(); relatedElements.add(sender.getCovered());
	 * relatedElements.add(receiver.getCovered()); Connection conn = new
	 * Connection(o, relatedElements, msg); allElements.add(conn); }
	 * SelectRelationshipsHandler.addToPossibleRelationsForSelection(msg.eClass(
	 * ).getName()); } } else { EObject root = EcoreUtil.getRootContainer(o);
	 * TreeIterator<EObject> modelContents = root.eAllContents(); while
	 * (modelContents.hasNext()) { EObject content = modelContents.next(); /*
	 * System.out.println("Name: "+EMFHelper.getNameAttribute( content));
	 * System.out.println("EClass: "+content.eClass().getName());
	 * System.out.println("EClass: "+content.getClass().toString());
	 * System.out.println("IsRelationship: "+Relationship.class.
	 * isAssignableFrom(content.getClass()));
	 * if(selectedRelationshipTypes.size()==0||selectedRelationshipTypes.
	 * contains(content.eClass().getName()))
	 * 
	 * { if (Relationship.class.isAssignableFrom(content.getClass())) {
	 * Relationship relation = Relationship.class.cast(content); boolean
	 * isRelatedToElement = false; List<EObject> relatedElements = new
	 * ArrayList<>(); for (Element relatedElement :
	 * relation.getRelatedElements()) { if
	 * (EMFHelper.getNameAttribute(relatedElement).equals(EMFHelper.
	 * getNameAttribute(o))) { isRelatedToElement = true; } else {
	 * relatedElements.add(relatedElement); } } if (isRelatedToElement) { if
	 * (!isDuplicatedEntry(o, relatedElements, relation, duplicationCheck)) {
	 * Connection conn = new Connection(o, relatedElements, relation);
	 * allElements.add(conn); addPotentialStringsForConnection(o,
	 * relatedElements, relation, duplicationCheck);
	 * SelectRelationshipsHandler.addToPossibleRelationsForSelection(relation.
	 * eClass().getName()); } } } else if
	 * (Transition.class.isAssignableFrom(content.getClass())) { Transition
	 * transition = Transition.class.cast(content); List<EObject>
	 * relatedElements = new ArrayList<>(); if
	 * (EMFHelper.getNameAttribute(transition.getSource()).equals(EMFHelper.
	 * getNameAttribute(o))) { relatedElements.add(transition.getTarget()); if
	 * (!isDuplicatedEntry(o, relatedElements, transition, duplicationCheck)) {
	 * Connection conn = new Connection(o, relatedElements, transition);
	 * allElements.add(conn); addPotentialStringsForConnection(o,
	 * relatedElements, transition, duplicationCheck);
	 * SelectRelationshipsHandler.addToPossibleRelationsForSelection(transition.
	 * eClass().getName()); } } else if
	 * (EMFHelper.getNameAttribute(transition.getTarget()).equals(EMFHelper.
	 * getNameAttribute(o))) { relatedElements.add(transition.getSource()); if
	 * (!isDuplicatedEntry(o, relatedElements, transition, duplicationCheck)) {
	 * Connection conn = new Connection(o, relatedElements, transition);
	 * allElements.add(conn); addPotentialStringsForConnection(o,
	 * relatedElements, transition, duplicationCheck);
	 * SelectRelationshipsHandler.addToPossibleRelationsForSelection(transition.
	 * eClass().getName()); } } } else if
	 * (Message.class.isAssignableFrom(content.getClass())) { Message msg =
	 * Message.class.cast(content); MessageOccurrenceSpecification receiver =
	 * (MessageOccurrenceSpecification) msg.getReceiveEvent();
	 * MessageOccurrenceSpecification sender = (MessageOccurrenceSpecification)
	 * msg.getSendEvent(); List<EObject> relatedElements = new ArrayList<>(); if
	 * (receiver != null) { if
	 * (EMFHelper.getNameAttribute(receiver.getCovered()).equals(EMFHelper.
	 * getNameAttribute(o))) { relatedElements.add(sender.getCovered()); if
	 * (!isDuplicatedEntry(o, relatedElements, msg, msg.getMessageSort(),
	 * duplicationCheck)) { Connection conn = new Connection(o, relatedElements,
	 * msg); allElements.add(conn); addPotentialStringsForConnection(o,
	 * relatedElements, msg, msg.getMessageSort(), duplicationCheck);
	 * SelectRelationshipsHandler.addToPossibleRelationsForSelection(msg.eClass(
	 * ).getName()); } } else if
	 * (EMFHelper.getNameAttribute(sender.getCovered()).equals(EMFHelper.
	 * getNameAttribute(o))) { relatedElements.add(receiver.getCovered()); if
	 * (!isDuplicatedEntry(o, relatedElements, msg, msg.getMessageSort(),
	 * duplicationCheck)) { Connection conn = new Connection(o, relatedElements,
	 * msg); allElements.add(conn); addPotentialStringsForConnection(o,
	 * relatedElements, msg, msg.getMessageSort(), duplicationCheck);
	 * SelectRelationshipsHandler.addToPossibleRelationsForSelection(msg.eClass(
	 * ).getName()); } } } } } }
	 * 
	 * }}
	 * 
	 * private static void addPotentialStringsForConnection(EObject source,
	 * List<EObject> targets, EObject relation, List<String> duplicationCheck) {
	 * String potentialString = EMFHelper.getNameAttribute(source); for (EObject
	 * target : targets) { potentialString +=
	 * EMFHelper.getNameAttribute(target); } potentialString +=
	 * EMFHelper.getNameAttribute(relation);
	 * 
	 * duplicationCheck.add(potentialString);
	 * 
	 * potentialString = ""; for (EObject target : targets) { potentialString +=
	 * EMFHelper.getNameAttribute(target); } potentialString +=
	 * EMFHelper.getNameAttribute(source); potentialString +=
	 * EMFHelper.getNameAttribute(relation);
	 * 
	 * duplicationCheck.add(potentialString); }
	 * 
	 * private static boolean isDuplicatedEntry(EObject source, List<EObject>
	 * targets, EObject relation, List<String> duplicationCheck) { String
	 * connectionString = EMFHelper.getNameAttribute(source); for (EObject
	 * target : targets) { connectionString +=
	 * EMFHelper.getNameAttribute(target); } connectionString +=
	 * EMFHelper.getNameAttribute(relation); return
	 * duplicationCheck.contains(connectionString); }
	 * 
	 * private static void addPotentialStringsForConnection(EObject source,
	 * List<EObject> targets, EObject relation, MessageSort msgSort,
	 * List<String> duplicationCheck) { String potentialString =
	 * EMFHelper.getNameAttribute(source); for (EObject target : targets) {
	 * potentialString += EMFHelper.getNameAttribute(target); } potentialString
	 * += EMFHelper.getNameAttribute(relation); potentialString +=
	 * msgSort.getName();
	 * 
	 * duplicationCheck.add(potentialString);
	 * 
	 * potentialString = ""; for (EObject target : targets) { potentialString +=
	 * EMFHelper.getNameAttribute(target); } potentialString +=
	 * EMFHelper.getNameAttribute(source); potentialString +=
	 * EMFHelper.getNameAttribute(relation); potentialString +=
	 * msgSort.getName();
	 * 
	 * duplicationCheck.add(potentialString); }
	 * 
	 * private static boolean isDuplicatedEntry(EObject source, List<EObject>
	 * targets, EObject relation, MessageSort msgSort, List<String>
	 * duplicationCheck) { String connectionString =
	 * EMFHelper.getNameAttribute(source); for (EObject target : targets) {
	 * connectionString += EMFHelper.getNameAttribute(target); }
	 * connectionString += EMFHelper.getNameAttribute(relation);
	 * connectionString += msgSort.getName(); return
	 * duplicationCheck.contains(connectionString); }
	 */

}
