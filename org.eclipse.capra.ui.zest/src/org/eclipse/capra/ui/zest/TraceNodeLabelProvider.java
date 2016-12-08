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
package org.eclipse.capra.ui.zest;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.capra.core.handlers.ArtifactHandler;
import org.eclipse.capra.core.helpers.ExtensionPointHelper;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * 
 * This class provides labels for the nodes and edges for the graph to be
 * displayed in the zest view.
 *
 */
public class TraceNodeLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {

		Collection<ArtifactHandler> artifactHandlers = ExtensionPointHelper.getArtifactHandlers();
		List<ArtifactHandler> availableHandlers = artifactHandlers.stream()
				.filter(handler -> handler.canHandleSelection(element)).collect(Collectors.toList());
		if (availableHandlers.size() >= 1) {
			return availableHandlers.get(0).getDisplayName(element);
		} else
			return element.toString();

	}

	// TODO Add labels for the edges

}