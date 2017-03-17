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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capra.core.adapters.TraceMetaModelAdapter;
import org.eclipse.capra.core.helpers.ExtensionPointHelper;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Toggles between showing (DSL) internal links or not
 * 
 * @author Dominik Einkemmer
 */
public class SelectRelationshipsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		TraceMetaModelAdapter traceAdapter = ExtensionPointHelper.getTraceMetamodelAdapter().get();

		List<String> elements = traceAdapter.getPossibleRelationsForSelection();
		ListSelectionDialog dialog = new ListSelectionDialog(window.getShell(), elements, new ArrayContentProvider(),
				new LabelProvider() {
					@Override
					public String getText(Object element) {
						return (String) element;
					}
				}, "Selection: Test");
		dialog.setTitle("Select Relationships you want to include");
		dialog.setInitialElementSelections(traceAdapter.getSelectedRelationshipTypes());

		if (dialog.open() == Window.OK) {
			Object[] results = dialog.getResult();
			List<String> selectedRelations = new ArrayList<>();
			for (Object res : results) {
				selectedRelations.add((String) res);
			}
			traceAdapter.setSelectedRelationshipTypes(selectedRelations);
		}

		return null;
	}
}