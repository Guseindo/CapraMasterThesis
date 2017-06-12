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
import java.util.Optional;

import org.eclipse.capra.GenericTraceMetaModel.GenericTraceMetaModelFactory;
import org.eclipse.capra.GenericTraceMetaModel.GenericTraceMetaModelPackage;
import org.eclipse.capra.GenericTraceMetaModel.GenericTraceModel;
import org.eclipse.capra.GenericTraceMetaModel.RelatedTo;
import org.eclipse.capra.core.adapters.AbstractMetaModelAdapter;
import org.eclipse.capra.core.adapters.Connection;
import org.eclipse.capra.core.adapters.TraceMetaModelAdapter;
import org.eclipse.capra.core.helpers.ExtensionPointHelper;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * Provides generic functionality to deal with traceability meta models.
 */
public class GenericMetaModelAdapter extends AbstractMetaModelAdapter implements TraceMetaModelAdapter {

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
		if (selection.size() >= 1) {
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


		// String builder to build the name of the trace link so by adding the
		// elements it connects so as to make it easy for a user to visually
		// differentiate trace links
	
		String name = "";
		
		for (Object obj : selection) {
				name = name + " " + ExtensionPointHelper.getArtifactHandlers().stream()
						.map(handler -> handler.withCastedHandler(obj, (h, e) -> h.getDisplayName(e)))
						.filter(Optional::isPresent)
						.map(Optional::get)
						.findFirst()
						.orElseGet(obj::toString);
		}
		
		RelatedToTrace.setName(name.toString());
		TM.getTraces().add(RelatedToTrace);
		return TM;
	}

	@Override
	public void deleteTrace(EObject first, EObject second, EObject traceModel) {
		// TODO Auto-generated method stub

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

	@Override
	public List<Connection> getConnectedElements(EObject element, EObject tracemodel,
			List<String> selectedRelationshipTypes) {
		GenericTraceModel root = (GenericTraceModel) tracemodel;
		List<Connection> connections = new ArrayList<>();
		List<RelatedTo> traces = root.getTraces();

		if (selectedRelationshipTypes.size() == 0 || selectedRelationshipTypes
				.contains(GenericTraceMetaModelPackage.eINSTANCE.getRelatedTo().getName())) {
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
		}
		return connections;
	}

	@Override
	public String isThereATraceBetween(EObject first, EObject second, EObject traceModel) {
		GenericTraceModel root = (GenericTraceModel) traceModel;
		List<RelatedTo> relevantLinks = new ArrayList<RelatedTo>();
		List<RelatedTo> allTraces = root.getTraces();
		String traceString = "";

		for (RelatedTo trace : allTraces) {
			if (first != second) {
				if (trace.getItem().contains(first) && trace.getItem().contains(second)) {
					relevantLinks.add(trace);
				}
			}
		}
		if (relevantLinks.size() > 0) {
			traceString = "X";
		}

		String spacer = "";
		String internalTraceString = this.isThereAnInternalTraceBetween(first, second, traceModel);
		spacer = (!traceString.equals("") && !internalTraceString.equals("")) ? ", " : "";
		return traceString + spacer + internalTraceString;
	}

	private List<Connection> getTransitivelyConnectedElements(EObject element, EObject traceModel,
			ArrayList<Object> accumulator, int currentDepth, int maximumDepth) {
		List<Connection> directElements = getConnectedElements(element, traceModel);
		List<Connection> allElements = new ArrayList<>();

		int currDepth = currentDepth + 1;
		for (Connection connection : directElements) {
			if (!accumulator.contains(connection.getTlink())) {
				allElements.add(connection);
				accumulator.add(connection.getTlink());
				for (EObject e : connection.getTargets()) {
					if (maximumDepth == 0 || currDepth <= maximumDepth) {
						allElements.addAll(
								getTransitivelyConnectedElements(e, traceModel, accumulator, currDepth, maximumDepth));
					}
				}
			}
		}

		return allElements;
	}

	private List<Connection> getTransitivelyConnectedElements(EObject element, EObject traceModel,
			List<Object> accumulator, List<String> selectedRelationshipTypes, int currentDepth, int maximumDepth) {
		List<Connection> directElements = getConnectedElements(element, traceModel, selectedRelationshipTypes);
		List<Connection> allElements = new ArrayList<>();
		int currDepth = currentDepth++;
		for (Connection connection : directElements) {
			if (!accumulator.contains(connection.getTlink())) {
				allElements.add(connection);
				accumulator.add(connection.getTlink());
				for (EObject e : connection.getTargets()) {
					if (maximumDepth == 0 || currDepth <= maximumDepth) {
						allElements.addAll(getTransitivelyConnectedElements(e, traceModel, accumulator,
								selectedRelationshipTypes, currDepth, maximumDepth));
					}
				}
			}
		}

		return allElements;
	}

	@Override
	public List<Connection> getTransitivelyConnectedElements(EObject element, EObject traceModel, int maximumDepth) {
		ArrayList<Object> accumulator = new ArrayList<>();
		return getTransitivelyConnectedElements(element, traceModel, accumulator, -2, maximumDepth);
	}

	@Override
	public List<Connection> getTransitivelyConnectedElements(EObject element, EObject traceModel,
			List<String> selectedRelationshipTypes, int maximumDepth) {
		List<Object> accumulator = new ArrayList<>();
		return getTransitivelyConnectedElements(element, traceModel, accumulator, selectedRelationshipTypes, -2,
				maximumDepth);
	}

}
