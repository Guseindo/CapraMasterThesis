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

import org.eclipse.capra.GenericTraceMetaModel.GenericTraceModel;
import org.eclipse.capra.GenericTraceMetaModel.RelatedTo;
import org.eclipse.capra.core.adapters.Connection;
import org.eclipse.emf.ecore.EObject;

public abstract class AbstractArtifactHandler<T> implements IArtifactHandler<T> {

	@Override
	public boolean canHandleArtifact(T artifact) {
		try {
			Class<?> genericType = ((Class<?>) ((ParameterizedType) this.getClass().getGenericSuperclass())
					.getActualTypeArguments()[0]);
			return genericType.isAssignableFrom(artifact.getClass());
		} catch (NoClassDefFoundError e) {
			return false;
		}
	}

	@Override
	public void addInternalLinks(EObject investigatedElement, List<Connection> allElements,
			ArrayList<String> duplicationCheck, List<String> selectedRelationshipTypes) {
		// TODO Auto-generated method stub
	}

	@Override
	public String isThereATraceBetween(EObject first, EObject second, EObject traceModel) {
		GenericTraceModel root = (GenericTraceModel) traceModel;
		List<RelatedTo> relevantLinks = new ArrayList<RelatedTo>();
		List<RelatedTo> allTraces = root.getTraces();

		for (RelatedTo trace : allTraces) {
			if (first != second) {
				if (trace.getItem().contains(first) && trace.getItem().contains(second)) {
					relevantLinks.add(trace);
				}
			}
		}
		if (relevantLinks.size() > 0) {
			return "X";
		} else
			return "";
	}
}
