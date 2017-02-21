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

}
