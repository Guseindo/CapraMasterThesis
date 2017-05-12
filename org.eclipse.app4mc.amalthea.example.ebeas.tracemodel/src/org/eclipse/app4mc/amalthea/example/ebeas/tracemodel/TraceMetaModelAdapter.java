/*******************************************************************************
 * Copyright (c) 2017 Fraunhofer IEM.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    David Schmelter - initial implementation
 *******************************************************************************/
package org.eclipse.app4mc.amalthea.example.ebeas.tracemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.helper.TraceMetaModelCompareHelper;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.helper.TraceMetaModelConstraintHelper;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.helper.TraceMetaModelReachabilityHelper;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.helper.TraceMetamodelCreationHelper;
import org.eclipse.capra.core.adapters.AbstractMetaModelAdapter;
import org.eclipse.capra.core.adapters.Connection;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

public class TraceMetaModelAdapter extends AbstractMetaModelAdapter
		implements org.eclipse.capra.core.adapters.TraceMetaModelAdapter {

	private TraceMetaModelConstraintHelper constraintHelper;
	private TraceMetamodelCreationHelper creationHelper;
	private TraceMetaModelCompareHelper compareHelper;
	private TraceMetaModelReachabilityHelper reachabilityHelper;

	public TraceMetaModelAdapter() {
		constraintHelper = new TraceMetaModelConstraintHelper();
		creationHelper = new TraceMetamodelCreationHelper();
		compareHelper = new TraceMetaModelCompareHelper();
		reachabilityHelper = new TraceMetaModelReachabilityHelper();
	}

	@Override
	public EObject createModel() {

		return TracemodelFactory.eINSTANCE.createEBEASTracelinkModel();
	}

	@Override
	public Collection<EClass> getAvailableTraceTypes(List<EObject> selection) {

		Collection<EClass> traceTypes = new Vector<>();
		if (selection.size() >= 2) {
			if (constraintHelper.checkMUMLMsgTypeRepository2UMLInterface(selection)) {
				traceTypes.add(TracemodelPackage.eINSTANCE.getMUMLMsgTypeRepository2UMLInterface());
			}
			if (constraintHelper.checkMUMLSoftwareComponent2UMLClass(selection)) {
				traceTypes.add(TracemodelPackage.eINSTANCE.getMUMLSoftwareComponent2UMLClass());
			}
			if (constraintHelper.checkMUMLDiscretePort2UMLPort(selection)) {
				traceTypes.add(TracemodelPackage.eINSTANCE.getMUMLDiscretePort2UMLPort());
			}
			if (constraintHelper.checkMUMLSoftwareComponent2UMLCollaboration(selection)) {
				traceTypes.add(TracemodelPackage.eINSTANCE.getMUMLSoftwareComponent2UMLCollaboration());
			}
			if (constraintHelper.checkMUMLRegion2UMLCollaboration(selection)) {
				traceTypes.add(TracemodelPackage.eINSTANCE.getMUMLRegion2UMLCollaboration());
			}
			if (constraintHelper.checkAPP4MCRunnable2MUMLRegion(selection)) {
				traceTypes.add(TracemodelPackage.eINSTANCE.getAPP4MCRunnable2MUMLRegion());
			}
			traceTypes.add(TracemodelPackage.eINSTANCE.getRelatedTo());
		}
		return traceTypes;
	}

	@Override
	public EObject createTrace(EClass traceType, EObject traceModel, List<EObject> selection) {

		EBEASTracelinkModel tm = (EBEASTracelinkModel) traceModel;
		EObject trace = TracemodelFactory.eINSTANCE.create(traceType);

		if (TracemodelPackage.eINSTANCE.getMUMLMsgTypeRepository2UMLInterface().equals(traceType)) {
			creationHelper.createMUMLMsgTypeRepository2UMLInterface(trace, selection);
		} else if (TracemodelPackage.eINSTANCE.getMUMLSoftwareComponent2UMLClass().equals(traceType)) {
			creationHelper.createMUMLSoftwareComponent2UMLClass(trace, selection);
		} else if (TracemodelPackage.eINSTANCE.getMUMLDiscretePort2UMLPort().equals(traceType)) {
			creationHelper.createMUMLDiscretePort2UMLPort(trace, selection);
		} else if (TracemodelPackage.eINSTANCE.getMUMLSoftwareComponent2UMLCollaboration().equals(traceType)) {
			creationHelper.createMUMLSoftwareComponent2UMLCollaboration(trace, selection);
		} else if (TracemodelPackage.eINSTANCE.getMUMLRegion2UMLCollaboration().equals(traceType)) {
			creationHelper.createMUMLRegion2UMLCollaboration(trace, selection);
		} else if (TracemodelPackage.eINSTANCE.getAPP4MCRunnable2MUMLRegion().equals(traceType)) {
			creationHelper.createAPP4MCRunnable2MUMLRegion(trace, selection);
		} else if (TracemodelPackage.eINSTANCE.getRelatedTo().equals(traceType)) {
			creationHelper.createRelatedTo(trace, selection);
		}

		tm.getItem().add((TraceLink) trace);

		return tm;
	}

	@Override
	public void deleteTrace(EObject arg0, EObject arg1, EObject arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public String isThereATraceBetween(EObject first, EObject second, EObject traceModel) {

		String traceString = "";
		if (first.equals(second))
			traceString = "";

		EBEASTracelinkModel tm = (EBEASTracelinkModel) traceModel;
		List<TraceLink> traces = tm.getItem();

		for (TraceLink traceLink : traces) {
			if (compareHelper.analyzeMUMLMsgTypeRepository2UMLInterface(traceLink, first, second)) {
				traceString = "X";
			} else if (compareHelper.analyzeMUMLSoftwareComponent2UMLClass(traceLink, first, second)) {
				traceString = "X";
			} else if (compareHelper.analyzeMUMLDiscretePort2UMLPort(traceLink, first, second)) {
				traceString = "X";
			} else if (compareHelper.analyzeMUMLSoftwareComponent2UMLCollaboration(traceLink, first, second)) {
				traceString = "X";
			} else if (compareHelper.analyzeMUMLRegion2UMLCollaboration(traceLink, first, second)) {
				traceString = "X";
			} else if (compareHelper.analyzeAPP4MCRunnable2MUMLRegion(traceLink, first, second)) {
				traceString = "X";
			} else if (compareHelper.analyzeRelatedTo(traceLink, first, second)) {
				traceString = "X";
			}
		}

		String spacer = "";
		String internalTraceString = this.isThereAnInternalTraceBetween(first, second, traceModel);
		spacer = (!traceString.equals("") && !internalTraceString.equals("")) ? ", " : "";
		return traceString + spacer + internalTraceString;
	}

	@Override
	public List<Connection> getConnectedElements(EObject element, EObject traceModel) {
		EBEASTracelinkModel tm = (EBEASTracelinkModel) traceModel;
		List<Connection> connections = new ArrayList<>();
		List<TraceLink> traces = tm.getItem();

		if (element instanceof TraceLink) {
			TraceLink trace = (TraceLink) element;
			connections.add(new Connection(element, reachabilityHelper.getConnectedElements(trace), trace));
		} else {
			for (TraceLink trace : traces) {
				List<EObject> connectedElements = reachabilityHelper.getConnectedElements(trace);

				if (connectedElements.contains((EObject) element)) {
					connections.add(new Connection(element, connectedElements, trace));
				}
			}
		}

		return connections;
	}

	@Override
	public List<Connection> getConnectedElements(EObject element, EObject traceModel,
			List<String> selectedRelationshipTypes) {
		EBEASTracelinkModel tm = (EBEASTracelinkModel) traceModel;
		List<Connection> connections = new ArrayList<>();
		List<TraceLink> traces = tm.getItem();

		if (element instanceof TraceLink) {
			TraceLink trace = (TraceLink) element;
			connections.add(new Connection(element, reachabilityHelper.getConnectedElements(trace), trace));
		} else {
			for (TraceLink trace : traces) {
				List<EObject> connectedElements = reachabilityHelper.getConnectedElements(trace);

				if (connectedElements.contains((EObject) element)) {
					connections.add(new Connection(element, connectedElements, trace));
				}
			}
		}

		return connections;
	}

	@Override
	public List<Connection> getTransitivelyConnectedElements(EObject element, EObject traceModel, int maximumDepth) {
		ArrayList<Object> accumulator = new ArrayList<>();
		return getTransitivelyConnectedElements(element, traceModel, accumulator, -2, maximumDepth);
	}

	private List<Connection> getTransitivelyConnectedElements(EObject element, EObject traceModel,
			ArrayList<Object> accumulator, int currentDepth, int maximumDepth) {
		List<Connection> directElements = getConnectedElements(element, traceModel);
		List<Connection> allElements = new ArrayList<>();

		int currDepth = currentDepth + 1;
		directElements.forEach(connection -> {
			if (!accumulator.contains(connection.getTlink())) {
				allElements.add(connection);
				accumulator.add(connection.getTlink());
				connection.getTargets().forEach(e -> {
					if (maximumDepth == 0 || currDepth <= maximumDepth) {
						allElements.addAll(
								getTransitivelyConnectedElements(e, traceModel, accumulator, currDepth, maximumDepth));
					}
				});
			}
		});

		return allElements;
	}

	@Override
	public List<Connection> getTransitivelyConnectedElements(EObject element, EObject traceModel,
			List<String> selectedRelationshipTypes, int maximumDepth) {
		List<Object> accumulator = new ArrayList<>();
		return getTransitivelyConnectedElements(element, traceModel, accumulator, selectedRelationshipTypes, -2,
				maximumDepth);
	}

	private List<Connection> getTransitivelyConnectedElements(EObject element, EObject traceModel,
			List<Object> accumulator, List<String> selectedRelationshipTypes, int currentDepth, int maximumDepth) {
		List<Connection> directElements = getConnectedElements(element, traceModel, selectedRelationshipTypes);
		List<Connection> allElements = new ArrayList<>();

		int currDepth = currentDepth + 1;
		directElements.forEach(connection -> {
			if (!accumulator.contains(connection.getTlink())) {
				allElements.add(connection);
				accumulator.add(connection.getTlink());
				connection.getTargets().forEach(e -> {
					if (maximumDepth == 0 || currDepth <= maximumDepth) {
						allElements.addAll(getTransitivelyConnectedElements(e, traceModel, accumulator,
								selectedRelationshipTypes, currDepth, maximumDepth));
					}

				});
			}
		});

		return allElements;
	}

}
