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

	public List<Connection> getInternalElementsTransitive(EObject element, EObject traceModel) {
		List<Object> accumulator = new ArrayList<>();
		return getInternalElementsTransitive(element, traceModel, accumulator);
	}

	public List<Connection> getInternalElements(EObject element, EObject traceModel) {
		List<Connection> allElements = new ArrayList<>();
		ArrayList<String> duplicationCheck = new ArrayList<>();
		List<Connection> directElements = getConnectedElements(element, traceModel);

		ResourceSet resourceSet = new ResourceSetImpl();
		TracePersistenceAdapter persistenceAdapter = ExtensionPointHelper.getTracePersistenceAdapter().get();
		EObject artifactModel = persistenceAdapter.getArtifactWrappers(resourceSet);
		ArtifactHelper artifactHelper = new ArtifactHelper(artifactModel);

		for (Connection conn : directElements) {
			for (EObject o : conn.getTargets()) {
				IArtifactHandler<Object> handler = artifactHelper.getHandler(o);
				handler.addInternalLinks(o, allElements, duplicationCheck);
			}
		}

		IArtifactHandler<Object> handler = artifactHelper.getHandler(element);
		handler.addInternalLinks(element, allElements, duplicationCheck);
		return allElements;
	}

	@Override
	public boolean isThereAnInternalTraceBetween(EObject first, EObject second) {
		ResourceSet resourceSet = new ResourceSetImpl();
		TracePersistenceAdapter persistenceAdapter = ExtensionPointHelper.getTracePersistenceAdapter().get();
		EObject artifactModel = persistenceAdapter.getArtifactWrappers(resourceSet);
		ArtifactHelper artifactHelper = new ArtifactHelper(artifactModel);
		IArtifactHandler<Object> handler = artifactHelper.getHandler(first);
		return handler.isThereAnInternalTraceBetween(first, second);
	}

	@Override
	public String getRelationStringForMatrix(EObject first) {
		ResourceSet resourceSet = new ResourceSetImpl();
		TracePersistenceAdapter persistenceAdapter = ExtensionPointHelper.getTracePersistenceAdapter().get();
		EObject artifactModel = persistenceAdapter.getArtifactWrappers(resourceSet);
		ArtifactHelper artifactHelper = new ArtifactHelper(artifactModel);
		IArtifactHandler<Object> handler = artifactHelper.getHandler(first);
		return handler.getRelationStringForMatrix();
	}

	@Override
	public void emptyRelationshipStrings(EObject first) {
		ResourceSet resourceSet = new ResourceSetImpl();
		TracePersistenceAdapter persistenceAdapter = ExtensionPointHelper.getTracePersistenceAdapter().get();
		EObject artifactModel = persistenceAdapter.getArtifactWrappers(resourceSet);
		ArtifactHelper artifactHelper = new ArtifactHelper(artifactModel);
		IArtifactHandler<Object> handler = artifactHelper.getHandler(first);
		handler.emptyRelationshipStrings();
	}
}
