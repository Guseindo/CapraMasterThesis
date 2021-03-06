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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Toggles between showing (DSL) internal links or not
 * 
 * @author Dominik Einkemmer
 */
public class DisplayInternalLinksHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (areInternalLinksShown())
			showInternalLinks(false);
		else
			showInternalLinks(true);

		return null;
	}

	/**
	 * Checks whether the trace view is set to show transitive traces.
	 * 
	 * @return {@code true} if transitive traces are enabled, {@code false}
	 *         otherwise
	 */
	public static boolean areInternalLinksShown() {
		Preferences internalLinks = getPreference();

		return internalLinks.get("option", "turnedOff").equals("shown");
	}

	private static Preferences getPreference() {
		Preferences preferences = InstanceScope.INSTANCE.getNode("org.eclipse.capra.ui.plantuml.toggleInternalLinks");
		Preferences transitivity = preferences.node("internalLinks");
		return transitivity;
	}

	/**
	 * Sets whether the trace view is set to show transitive traces.
	 * 
	 * @param value
	 *            indicates whether transitive traces should be shown
	 */
	public static void showInternalLinks(boolean value) {
		Preferences internalLinks = getPreference();

		internalLinks.put("option", value ? "shown" : "turnedOff");

		try {
			// forces the application to save the preferences
			internalLinks.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
}