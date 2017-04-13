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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capra.core.adapters.Connection;
import org.eclipse.emf.ecore.EObject;

/**
 * This interface defines functionality required to map chosen Objects in the
 * Eclipse workspace to wrappers which can then be traced and persisted in EMF
 * models.
 */
public interface IArtifactHandler<T> {

	/**
	 * Does the handler support this object?
	 *
	 * @param artifact
	 *            The object to be wrapped
	 * @return <code>true</code> if object can be handled, <code>false</code>
	 *         otherwise.
	 */
	boolean canHandleArtifact(T artifact);

	/**
	 * Create a wrapper for the object
	 *
	 * @param artifact
	 *            The object to be wrapped
	 * @param artifactModel
	 * @return
	 */
	EObject createWrapper(T artifact, EObject artifactModel);

	/**
	 * Resolve the wrapper to the originally selected Object from the Eclipse
	 * workspace. This is essentially the inverse of the createWrapper
	 * operation.
	 *
	 * @param wrapper
	 *            The wrapped object
	 * @return originally selected object
	 */
	T resolveWrapper(EObject wrapper);

	/**
	 * Provide a name for the artifact to be used for display purposes.
	 * 
	 * @param artifact
	 */
	String getDisplayName(T artifact);

	/**
	 * Adds the internal links for a given element into the list of all
	 * elements. This method is delegated to the right handler (e.g. Papyrus for
	 * UML)
	 * 
	 * @param investigatedElement
	 *            Element currently under investigation for links
	 * @param allElements
	 *            List of all elements for Plant-uml view
	 * @param duplicationCheck
	 *            List of String for checking for duplication
	 */
	void addInternalLinks(EObject investigatedElement, List<Connection> allElements, ArrayList<String> duplicationCheck,
			List<String> selectedRelationshipTypes);

	/**
	 * Decide if two objects are connected according to the given trace model
	 * and returns a String with the Type of connection for the trace matrix
	 * (empty String if no connection exists)
	 * 
	 * @param first
	 *            First object
	 * @param second
	 *            Second object
	 * @param traceModel
	 *            Trace model to base decision on
	 * @return <code>true</code> if object are connected, <code>false</code>
	 *         otherwise
	 */
	String isThereAnInternalTraceBetween(EObject first, EObject second, EObject traceModel);
}
