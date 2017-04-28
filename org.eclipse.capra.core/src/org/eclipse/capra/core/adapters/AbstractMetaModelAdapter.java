package org.eclipse.capra.core.adapters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capra.core.handlers.IArtifactHandler;
import org.eclipse.capra.core.helpers.ArtifactHelper;
import org.eclipse.capra.core.helpers.ExtensionPointHelper;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

public abstract class AbstractMetaModelAdapter implements TraceMetaModelAdapter {

	private List<Connection> getInternalElementsTransitive(EObject element, EObject traceModel,
			List<Object> accumulator, List<String> selectedRelationshipTypes, int currentDepth, int maximumDepth) {
		List<Connection> directElements = getInternalElements(element, traceModel, selectedRelationshipTypes);
		List<Connection> allElements = new ArrayList<>();
		int currDepth = currentDepth + 1;
		for (Connection connection : directElements) {
			if (!accumulator.contains(connection.getTlink())) {
				allElements.add(connection);
				accumulator.add(connection.getTlink());
				for (EObject e : connection.getTargets()) {
					if (maximumDepth == 0 || currDepth <= maximumDepth) {
						allElements.addAll(getInternalElementsTransitive(e, traceModel, accumulator,
								selectedRelationshipTypes, currDepth, maximumDepth));
					}
				}
			}
		}

		return allElements;
	}

	public List<Connection> getInternalElementsTransitive(EObject element, EObject traceModel,
			List<String> selectedRelationshipTypes, int maximumDepth) {
		List<Object> accumulator = new ArrayList<>();
		return getInternalElementsTransitive(element, traceModel, accumulator, selectedRelationshipTypes, 0,
				maximumDepth);
	}

	public List<Connection> getInternalElements(EObject element, EObject traceModel,
			List<String> selectedRelationshipTypes) {
		List<Connection> allElements = new ArrayList<>();
		ArrayList<String> duplicationCheck = new ArrayList<>();
		List<Connection> directElements = getConnectedElements(element, traceModel, selectedRelationshipTypes);

		ResourceSet resourceSet = new ResourceSetImpl();
		TracePersistenceAdapter persistenceAdapter = ExtensionPointHelper.getTracePersistenceAdapter().get();
		EObject artifactModel = persistenceAdapter.getArtifactWrappers(resourceSet);
		ArtifactHelper artifactHelper = new ArtifactHelper(artifactModel);

		for (Connection conn : directElements) {
			for (EObject o : conn.getTargets()) {
				@SuppressWarnings("unchecked")
				IArtifactHandler<Object> handler = (IArtifactHandler<Object>) artifactHelper.getHandler(o).orElse(null);
				handler.addInternalLinks(o, allElements, duplicationCheck, selectedRelationshipTypes);
			}
		}

		@SuppressWarnings("unchecked")
		IArtifactHandler<Object> handler = (IArtifactHandler<Object>) artifactHelper.getHandler(element).orElse(null);
		handler.addInternalLinks(element, allElements, duplicationCheck, selectedRelationshipTypes);
		return allElements;
	}

	public String isThereATraceBetween(EObject first, EObject second, EObject traceModel) {
		return "";
	}

	@Override
	public String isThereAnInternalTraceBetween(EObject first, EObject second, EObject traceModel) {
		ResourceSet resourceSet = new ResourceSetImpl();
		TracePersistenceAdapter persistenceAdapter = ExtensionPointHelper.getTracePersistenceAdapter().get();
		EObject artifactModel = persistenceAdapter.getArtifactWrappers(resourceSet);
		ArtifactHelper artifactHelper = new ArtifactHelper(artifactModel);
		@SuppressWarnings("unchecked")
		IArtifactHandler<Object> handler = (IArtifactHandler<Object>) artifactHelper.getHandler(first).orElse(null);
		@SuppressWarnings("unchecked")
		IArtifactHandler<Object> handlerSecondElement = (IArtifactHandler<Object>) artifactHelper.getHandler(second)
				.orElse(null);
		if (handler.getClass().equals(handlerSecondElement.getClass())) {
			return handler.isThereAnInternalTraceBetween(first, second, traceModel);
		} else {
			String firstTraceString = handler.isThereAnInternalTraceBetween(first, second, traceModel);
			String spacer = firstTraceString.equals("") ? "" : ", ";
			return firstTraceString + spacer
					+ handlerSecondElement.isThereAnInternalTraceBetween(first, second, traceModel);
		}
	}
}
