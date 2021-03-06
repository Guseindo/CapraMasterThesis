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
package org.eclipse.capra.ui.plantuml;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.capra.core.adapters.Connection;
import org.eclipse.capra.core.adapters.TraceMetaModelAdapter;
import org.eclipse.capra.core.adapters.TracePersistenceAdapter;
import org.eclipse.capra.core.handlers.IArtifactHandler;
import org.eclipse.capra.core.helpers.ArtifactHelper;
import org.eclipse.capra.core.helpers.EMFHelper;
import org.eclipse.capra.core.helpers.ExtensionPointHelper;
import org.eclipse.capra.ui.helpers.TraceCreationHelper;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;

import net.sourceforge.plantuml.eclipse.utils.DiagramTextProvider;

/**
 * Provides PlantUML with a string representation of elements connected by trace
 * links.
 * 
 * @author Anthony Anjorin, Salome Maro
 */
public class DiagramTextProviderHandler implements DiagramTextProvider {
	private EObject artifactModel = null;

	@Override
	public String getDiagramText(IEditorPart editor, ISelection arg1) {
		List<Object> selectedModels = TraceCreationHelper
				.extractSelectedElements(editor.getSite().getSelectionProvider().getSelection());
		return getDiagramText(selectedModels);
	}

	@SuppressWarnings("unchecked")
	public String getDiagramText(List<Object> selectedModels) {
		List<EObject> firstModelElements = null;
		List<EObject> secondModelElements = null;
		EObject selectedObject = null;
		ResourceSet resourceSet = new ResourceSetImpl();
		EObject traceModel = null;
		List<Connection> traces = new ArrayList<>();

		TracePersistenceAdapter persistenceAdapter = ExtensionPointHelper.getTracePersistenceAdapter().get();
		TraceMetaModelAdapter metamodelAdapter = ExtensionPointHelper.getTraceMetamodelAdapter().get();

		artifactModel = persistenceAdapter.getArtifactWrappers(resourceSet);

		if (selectedModels.size() > 0) {
			ArtifactHelper artifactHelper = new ArtifactHelper(artifactModel);
			// check if there is a hander for the selected and get its Wrapper
			IArtifactHandler<Object> handler;
			if (selectedModels.get(0).getClass().getPackage().toString().contains("org.eclipse.eatop")) {
				handler = (IArtifactHandler<Object>) artifactHelper.getEastAdlHandler(selectedModels.get(0))
						.orElse(null);
			} else {
				handler = (IArtifactHandler<Object>) artifactHelper.getHandler(selectedModels.get(0)).orElse(null);
			}
			if (handler != null) {
				selectedObject = handler.createWrapper(selectedModels.get(0), artifactModel);
				if (selectedObject != null) {
					resourceSet = selectedObject.eResource().getResourceSet();
					traceModel = persistenceAdapter.getTraceModel(resourceSet);
					List<String> selectedRelationshipTypes = SelectRelationshipsHandler.getSelectedRelationshipTypes();
					if (selectedModels.size() == 1) {
						if (DisplayTracesHandler.isTraceViewTransitive()) {
							int transitivityDepth = Integer.parseInt(TransitivityDepthHandler.getTransitivityDepth());
							traces = metamodelAdapter.getTransitivelyConnectedElements(selectedObject, traceModel,
									selectedRelationshipTypes, transitivityDepth);
						} else {
							traces = metamodelAdapter.getConnectedElements(selectedObject, traceModel,
									selectedRelationshipTypes);
						}
						if (DisplayInternalLinksHandler.areInternalLinksShown()
								&& DisplayTracesHandler.isTraceViewTransitive()) {
							EObject previousElement = SelectRelationshipsHandler.getPreviousElement();
							int transitivityDepth = Integer.parseInt(TransitivityDepthHandler.getTransitivityDepth());
							if (previousElement != null) {
								String previousElementName = EMFHelper.getNameAttribute(previousElement);
								String currentElementName = EMFHelper.getNameAttribute(selectedObject);
								if (!previousElementName.equals(currentElementName)) {
									SelectRelationshipsHandler.clearPossibleRelationsForSelection();
									SelectRelationshipsHandler.emptySelectedRelationshipTypes();
									SelectRelationshipsHandler.setPreviousElement(selectedObject);
								}
							} else {
								SelectRelationshipsHandler.setPreviousElement(selectedObject);
							}
							traces.addAll(metamodelAdapter.getInternalElementsTransitive(selectedObject, traceModel,
									selectedRelationshipTypes, transitivityDepth, traces));
						} else if (DisplayInternalLinksHandler.areInternalLinksShown()) {
							EObject previousElement = SelectRelationshipsHandler.getPreviousElement();
							if (previousElement != null) {
								String previousElementName = EMFHelper.getNameAttribute(previousElement);
								String currentElementName = EMFHelper.getNameAttribute(selectedObject);
								if (!previousElementName.equals(currentElementName)) {
									SelectRelationshipsHandler.clearPossibleRelationsForSelection();
									SelectRelationshipsHandler.emptySelectedRelationshipTypes();
									SelectRelationshipsHandler.setPreviousElement(selectedObject);
								}
							} else {
								SelectRelationshipsHandler.setPreviousElement(selectedObject);
							}
							traces.addAll(metamodelAdapter.getInternalElements(selectedObject, traceModel,
									selectedRelationshipTypes, false, 0, traces));
						}
						List<EObject> links = extractLinksFromTraces(traces);
						SelectRelationshipsHandler.addToPossibleRelationsForSelection(links);
						return VisualizationHelper.createNeighboursView(traces, selectedObject);
					} else if (selectedModels.size() == 2) {
						IArtifactHandler<Object> handlerSecondElement;
						if (selectedModels.get(1).getClass().getPackage().toString().contains("org.eclipse.eatop")) {
							handlerSecondElement = (IArtifactHandler<Object>) artifactHelper
									.getEastAdlHandler(selectedModels.get(1)).orElse(null);
						} else {
							handlerSecondElement = (IArtifactHandler<Object>) artifactHelper
									.getHandler(selectedModels.get(1)).orElse(null);
						}
						if (DisplayTracesHandler.isTraceViewTransitive()) {
							firstModelElements = EMFHelper
									.linearize(handler.createWrapper(selectedModels.get(0), artifactModel));
							secondModelElements = EMFHelper.linearize(
									handlerSecondElement.createWrapper(selectedModels.get(1), artifactModel));
						} else {
							List<EObject> firstObject = new ArrayList<>();
							firstObject.add(handler.createWrapper(selectedModels.get(0), artifactModel));
							List<EObject> secondObject = new ArrayList<>();
							secondObject.add(handlerSecondElement.createWrapper(selectedModels.get(1), artifactModel));
							firstModelElements = firstObject;
							secondModelElements = secondObject;
						}
					} else if (selectedModels.size() > 2) {
						if (DisplayTracesHandler.isTraceViewTransitive()) {
							firstModelElements = selectedModels.stream().flatMap(r -> {
								IArtifactHandler<Object> individualhandler;
								if (r.getClass().getPackage().toString().contains("org.eclipse.eatop")) {
									individualhandler = (IArtifactHandler<Object>) artifactHelper.getEastAdlHandler(r)
											.orElse(null);
								} else {
									individualhandler = (IArtifactHandler<Object>) artifactHelper.getHandler(r)
											.orElse(null);
								}
								return EMFHelper.linearize(individualhandler.createWrapper(r, artifactModel)).stream();
							}).collect(Collectors.toList());
							secondModelElements = firstModelElements;
						} else {
							List<EObject> Objects = new ArrayList<>();
							selectedModels.stream().forEach(o -> {
								IArtifactHandler<Object> individualhandler;
								if (o.getClass().getPackage().toString().contains("org.eclipse.eatop")) {
									individualhandler = (IArtifactHandler<Object>) artifactHelper.getEastAdlHandler(o)
											.orElse(null);
									Objects.add(individualhandler.createWrapper(o, artifactModel));
								} else {
									individualhandler = (IArtifactHandler<Object>) artifactHelper.getHandler(o)
											.orElse(null);
								}
								Objects.add(individualhandler.createWrapper(o, artifactModel));
							});
							firstModelElements = Objects;
							secondModelElements = firstModelElements;
						}
					}
				}
			}
		}
		return VisualizationHelper.createMatrix(traceModel, firstModelElements, secondModelElements);
	}

	@Override
	public boolean supportsEditor(IEditorPart editor) {
		return true;
	}

	private static List<EObject> extractLinksFromTraces(List<Connection> traces) {
		List<EObject> links = new ArrayList<>();
		for (Connection trace : traces) {
			if (!links.contains(trace.getTlink())) {
				links.add(trace.getTlink());
			}
		}
		return links;
	}

	@Override
	public boolean supportsSelection(ISelection arg0) {
		return true;
	}
}
