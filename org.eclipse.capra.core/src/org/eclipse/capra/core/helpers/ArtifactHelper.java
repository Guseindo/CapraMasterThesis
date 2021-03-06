package org.eclipse.capra.core.helpers;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.capra.core.handlers.IArtifactHandler;
import org.eclipse.capra.core.handlers.PriorityHandler;
import org.eclipse.capra.handler.eastadl.EASTADLHandler;
import org.eclipse.emf.ecore.EObject;

public class ArtifactHelper {
	private EObject artifactModel;
	// Switch to Optional here to express potential absence in the type
	private static Optional<PriorityHandler> priorityHandler = ExtensionPointHelper.getPriorityHandler();
	
	// This is a tricky type... It is not really necessary here, but let's take it as a generics tutorial example! 
	// 
	// I used it during development because this type can contain collections both of type IArtifactHandler<?> 
	// AND of IArtifactHandler<Object>. The simpler type Collection<IArtifactHandler<?>> can only hold 
	// IArtifactHandler<?>. 
	private static Collection<? extends IArtifactHandler<?>> handlers = ExtensionPointHelper.getArtifactHandlers();

	/**
	 * @param artifactModel
	 */
	public ArtifactHelper(EObject artifactModel) {
		this.artifactModel = artifactModel;
	}

	/**
	 * Creates wrappers for artifacts
	 *
	 * @param artifacts
	 * @return List of wrappers
	 */
	@SuppressWarnings("unchecked")
	public List<EObject> createWrappers(List<?> artifacts) {
		return (List<EObject>) (Object)artifacts.stream()
			.map(vagueArtifact -> 
				getHandler(vagueArtifact).map(h -> h.withCastedHandlerUnchecked(vagueArtifact, 
					(handler, artifact) -> handler.createWrapper(artifact, artifactModel))))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(toList());

	}

	/**
	 * Creates wrapper for artifact
	 *
	 * @param vagueArtifact
	 * @return wrapper of null if no handler exists
	 */
	public EObject createWrapper(Object vagueArtifact) {
		Optional<EObject> wrapped = getHandler(vagueArtifact).map(
			vagueHandler -> vagueHandler.withCastedHandlerUnchecked(vagueArtifact, 
				(handler, artifact) -> handler.createWrapper(artifact, artifactModel)));
		
		return wrapped.orElse(null);
	}
	
	// Returns handler for same type as the argument
	public <T> Optional<IArtifactHandler<?>> getHandler(Object artifact) {

		List<IArtifactHandler<?>> availableHandlers = handlers.stream()
			.filter(h -> h.canHandleArtifact(artifact))
			.collect(toList());
		
		if (availableHandlers.isEmpty()) {
			return Optional.empty();
		} else if (availableHandlers.size() == 1) {
			return Optional.of(availableHandlers.get(0));
		} else {
			return priorityHandler
				.map(h -> h.getSelectedHandler(availableHandlers, artifact));
		}
	}

	public <T> Optional<IArtifactHandler<?>> getEastAdlHandler(Object artifact) {
		List<IArtifactHandler<?>> availableHandlers = handlers.stream()
				.filter(h -> h.getClass().isAssignableFrom(EASTADLHandler.class)).collect(toList());
		return Optional.of(availableHandlers.get(0));
	}
}
