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

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.capra.core.helpers.EMFHelper;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Toggles between showing (DSL) internal links or not
 * 
 * @author Dominik Einkemmer
 */
public class SelectRelationshipsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		String[] elements = new String[2];
		elements[0] = "Test 1";
		elements[1] = "Test 2";
		ListSelectionDialog dialog = new ListSelectionDialog(window.getShell(), Arrays.asList(elements) , new ArrayContentProvider(), new LabelProvider() {
			@Override
			public String getText(Object element) {
				return (String) element;
			}
		}, "Selection: Test");
		dialog.setTitle("Select Relationships you want to include");

		if (dialog.open() == Window.OK) {
		}

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
		Preferences preferences = InstanceScope.INSTANCE.getNode("org.eclipse.capra.ui.plantuml.relationshipTypes");
		Preferences transitivity = preferences.node("relationshipTypes");
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