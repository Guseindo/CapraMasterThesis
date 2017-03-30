/*******************************************************************************
 * Copyright (c) 2017 Fraunhofer IEM.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    David Schmelter - initial implementation
 *******************************************************************************/
package org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.helper;

import java.util.List;

import org.eclipse.app4mc.amalthea.model.AmaltheaPackage;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.UMLPackage;
import org.muml.pim.component.ComponentPackage;
import org.muml.pim.msgtype.MsgtypePackage;
import org.muml.pim.realtimestatechart.RealtimestatechartPackage;

public class TraceMetaModelConstraintHelper {

	protected boolean check1To1(List<EObject> selection, EClass firstType, EClass secondType) {

		if (selection.size() != 2) {
			return false;
		}

		for (EObject eObject : selection) {
			if (eObject == null || !(firstType.isInstance(eObject) || secondType.isInstance(eObject))) {
				return false;
			}
		}

		return true;
	}

	protected boolean checkN2M(List<EObject> selection, EClass firstType, EClass secondType) {

		boolean oneFirstType = false;
		boolean oneSecondType = false;

		if (selection.size() < 2) {
			return false;
		}

		for (EObject eObject : selection) {
			if (eObject == null || !(firstType.isInstance(eObject) || secondType.isInstance(eObject))) {
				return false;
			}

			if (!oneFirstType && firstType.isInstance(eObject)) {
				oneFirstType = true;
			}
			if (!oneSecondType && secondType.isInstance(eObject)) {
				oneSecondType = true;
			}
		}

		return oneFirstType && oneSecondType;
	}

	public boolean checkMUMLMsgTypeRepository2UMLInterface(List<EObject> selection) {

		return check1To1(selection, MsgtypePackage.eINSTANCE.getMessageTypeRepository(),
				UMLPackage.eINSTANCE.getInterface());
	}

	public boolean checkMUMLSoftwareComponent2UMLClass(List<EObject> selection) {

		return check1To1(selection, ComponentPackage.eINSTANCE.getComponent(), UMLPackage.eINSTANCE.getClass_());
	}

	public boolean checkMUMLDiscretePort2UMLPort(List<EObject> selection) {

		return checkN2M(selection, ComponentPackage.eINSTANCE.getDiscretePort(), UMLPackage.eINSTANCE.getPort());
	}

	public boolean checkMUMLSoftwareComponent2UMLCollaboration(List<EObject> selection) {

		return checkN2M(selection, ComponentPackage.eINSTANCE.getComponent(), UMLPackage.eINSTANCE.getCollaboration());
	}

	public boolean checkMUMLRegion2UMLCollaboration(List<EObject> selection) {

		return checkN2M(selection, RealtimestatechartPackage.eINSTANCE.getRegion(),
				UMLPackage.eINSTANCE.getCollaboration());
	}

	public boolean checkAPP4MCRunnable2MUMLRegion(List<EObject> selection) {

		return check1To1(selection, AmaltheaPackage.eINSTANCE.getRunnable(),
				RealtimestatechartPackage.eINSTANCE.getRegion());
	}
}
