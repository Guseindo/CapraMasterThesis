/*******************************************************************************
 * Copyright (c) 2017 Chalmers | University of Gothenburg, rt-labs and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Chalmers | University of Gothenburg and rt-labs - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.capra.core.handlers;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import org.eclipse.capra.core.adapters.Connection;
import org.eclipse.capra.core.helpers.EMFHelper;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

public abstract class AbstractArtifactHandler<T> implements IArtifactHandler<T> {

	@Override
	public <R> Optional<R> withCastedHandler(Object artifact, BiFunction<IArtifactHandler<T>, T, R> handleFunction) {
		if (canHandleArtifact(artifact)) {
			@SuppressWarnings("unchecked")
			T a = (T) artifact;
			return Optional.of(handleFunction.apply(this, a));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public <R> R withCastedHandlerUnchecked(Object artifact, BiFunction<IArtifactHandler<T>, T, R> handleFunction) {
		return withCastedHandler(artifact, handleFunction).orElseThrow(
				() -> new IllegalArgumentException("withCastedHanderUnchecked called with unhandleble artifact."
						+ " Artifact: " + artifact + ", handler: " + this));
	}

	protected void includeContainmentLinks(EObject investigatedElement, List<Connection> allElements,
			ArrayList<Integer> duplicationCheck, List<String> selectedRelationshipTypes) {
		List<EStructuralFeature> containments = new ArrayList<>();
		for (EStructuralFeature obj : investigatedElement.eClass().getEStructuralFeatures()) {
			if (selectedRelationshipTypes.size() == 0 || selectedRelationshipTypes.contains(obj.eClass().getName())) {
				if (EReference.class.isAssignableFrom(obj.getClass())) {
					EReference ref = EReference.class.cast(obj);
					if (ref.isContainment()) {
						if (!containments.contains(obj)) {
							List<EObject> relatedElements = new ArrayList<>();
							int structuralFeatureHashCode = 0;
							if (ref.getUpperBound() == -1) {
								if (investigatedElement.eIsSet(obj)) {
									@SuppressWarnings("unchecked")
									EList<EObject> structuralFeatures = (EList<EObject>) investigatedElement.eGet(obj,
											true);
									for (EObject structuralFeature : structuralFeatures) {
										structuralFeatureHashCode += structuralFeature.hashCode();
										relatedElements.add(structuralFeature);
									}
								}
							} else {
								if (investigatedElement.eIsSet(obj)) {
									EObject structuralFeature = (EObject) investigatedElement.eGet(obj, true);
									relatedElements.add(structuralFeature);
									structuralFeatureHashCode = structuralFeature.hashCode();
								}
							}

							int connectionHash = investigatedElement.hashCode() + ref.hashCode()
									+ structuralFeatureHashCode;
							if (!duplicationCheck.contains(connectionHash) && relatedElements.size() > 0) {
								Connection conn = new Connection(investigatedElement, relatedElements, ref);
								allElements.add(conn);
								duplicationCheck.add(connectionHash);
							}
							containments.add(obj);
						}
					}
				}
			}
		}
	}

	@Override
	public boolean canHandleArtifact(Object artifact) {
		try {
			Class<?> genericType = ((Class<?>) ((ParameterizedType) this.getClass().getGenericSuperclass())
					.getActualTypeArguments()[0]);

			return genericType.isAssignableFrom(artifact.getClass());
		} catch (NoClassDefFoundError e) {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<T> getHandledClass() {
		return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

	@Override
	public void addInternalLinks(EObject investigatedElement, List<Connection> allElements,
			ArrayList<Integer> duplicationCheck, List<String> selectedRelationshipTypes) {
		// TODO Auto-generated method stub
	}

	@Override
	public String isThereAnInternalTraceBetween(EObject first, EObject second, EObject traceModel) {
		return "";
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
	public static boolean isDuplicatedEntry(EObject source, List<EObject> targets, EObject relation,
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
	public static void addPotentialStringsForConnection(EObject source, List<EObject> targets, EObject relation,
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

}
