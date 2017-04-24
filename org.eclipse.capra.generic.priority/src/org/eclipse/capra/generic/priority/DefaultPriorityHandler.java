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
package org.eclipse.capra.generic.priority;

import java.util.Collection;
import java.util.Comparator;

import org.eclipse.capra.core.handlers.IArtifactHandler;
import org.eclipse.capra.core.handlers.PriorityHandler;
<<<<<<< HEAD
import org.eclipse.capra.handler.emf.EMFHandler;
import org.eclipse.capra.handler.hudson.BuildElementHandler;
import org.eclipse.capra.handler.hudson.TestElementHandler;
import org.eclipse.capra.handler.muml.MUMLHandler;
import org.eclipse.capra.handler.uml.UMLHandler;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.mylyn.builds.internal.core.BuildElement;
import org.eclipse.mylyn.builds.internal.core.TestElement;
import org.muml.core.ExtendableElement;
=======
>>>>>>> 69f6db53a8fae491968cf438522a731ab5c8fd46

/**
 * Provides a simple default policy for selecting an {@link IArtifactHandler} by
 * selecting the one which handles the more specific type. This is determined by
 * comparing the assignability of the classes handled by the
 * <code>ArtifactHandlers</code>.
 */
public class DefaultPriorityHandler implements PriorityHandler {

	@Override
<<<<<<< HEAD
	public IArtifactHandler<Object> getSelectedHandler(Collection<IArtifactHandler<Object>> handlers,
			Object selectedElement) {
		// TODO: is this needed if HudsonHandler is split into
		// Build/TestElementHandler?
		if (selectedElement instanceof TestElement) {
			return handlers.stream().filter(h -> h.getClass().isAssignableFrom(TestElementHandler.class)).findAny()
					.get();
		} else if (selectedElement instanceof BuildElement) {
			return handlers.stream().filter(h -> h.getClass().isAssignableFrom(BuildElementHandler.class)).findAny()
					.get();
		} else if (EModelElement.class.isAssignableFrom(selectedElement.getClass())) {
			return handlers.stream().filter(h -> h.getClass().isAssignableFrom(UMLHandler.class)).findAny().get();
		} else if (ExtendableElement.class.isAssignableFrom(selectedElement.getClass())) {
			return handlers.stream().filter(h -> h.getClass().isAssignableFrom(MUMLHandler.class)).findAny().get();
		}
		return handlers.stream().filter(h -> h.getClass().isAssignableFrom(EMFHandler.class)).findAny().get();
=======
	public <T> IArtifactHandler<? extends T> getSelectedHandler(
			Collection<? extends IArtifactHandler<? extends T>> handlers, Object artifact) {
		return handlers.stream().max(new ArtifactHandlerPriorityComparator()).get();
	}

	/**
	 * A comparator that compares two classes by whether the handled classes are
	 * assignable to each other. If instance A is of a type that is a superclass
	 * or superinterface of instance B, it will return A>B. A=B if the
	 * assignment works in both directions or if the assignment does not work at
	 * all.
	 */
	private class ArtifactHandlerPriorityComparator implements Comparator<IArtifactHandler<?>> {

		@Override
		public int compare(IArtifactHandler<?> o1, IArtifactHandler<?> o2) {
			if (!o1.getHandledClass().isAssignableFrom(o2.getHandledClass())
					&& o2.getHandledClass().isAssignableFrom(o1.getHandledClass())) {
				return 1;
			} else if (o1.getHandledClass().isAssignableFrom(o2.getHandledClass())
					&& !o2.getHandledClass().isAssignableFrom(o1.getHandledClass())) {
				return -1;
			} else {
				return 0;
			}
		}
>>>>>>> 69f6db53a8fae491968cf438522a731ab5c8fd46
	}

}