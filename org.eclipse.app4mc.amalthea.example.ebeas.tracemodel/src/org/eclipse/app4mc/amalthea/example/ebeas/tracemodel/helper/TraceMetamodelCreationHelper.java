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

import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.APP4MCRunnable2MUMLRegion;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.MUMLDiscretePort2UMLPort;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.MUMLMsgTypeRepository2UMLInterface;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.MUMLRegion2UMLCollaboration;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.MUMLSoftwareComponent2UMLClass;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.MUMLSoftwareComponent2UMLCollaboration;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.RelatedTo;
import org.eclipse.app4mc.amalthea.model.Runnable;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Collaboration;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Port;
import org.muml.pim.component.Component;
import org.muml.pim.component.DiscretePort;
import org.muml.pim.msgtype.MessageTypeRepository;
import org.muml.pim.realtimestatechart.Region;

public class TraceMetamodelCreationHelper {

	public void createMUMLMsgTypeRepository2UMLInterface(EObject baseTrace, List<EObject> selection) {

		MUMLMsgTypeRepository2UMLInterface trace = (MUMLMsgTypeRepository2UMLInterface) baseTrace;
		for (EObject eObject : selection) {
			if (eObject instanceof MessageTypeRepository) {
				trace.setMsgTypeRepository((MessageTypeRepository) eObject);
			} else {
				trace.setMsgInterface((Interface) eObject);
			}
		}
	}

	public void createMUMLSoftwareComponent2UMLClass(EObject baseTrace, List<EObject> selection) {

		MUMLSoftwareComponent2UMLClass trace = (MUMLSoftwareComponent2UMLClass) baseTrace;
		for (EObject eObject : selection) {
			if (eObject instanceof Component) {
				trace.setComponent((Component) eObject);
			} else {
				trace.setScenarioType((Class) eObject);
			}
		}
	}

	public void createMUMLDiscretePort2UMLPort(EObject baseTrace, List<EObject> selection) {

		MUMLDiscretePort2UMLPort trace = (MUMLDiscretePort2UMLPort) baseTrace;
		for (EObject eObject : selection) {
			if (eObject instanceof DiscretePort) {
				trace.getDiscretePort().add((DiscretePort) eObject);
			} else if (eObject instanceof Port) {
				trace.getPort().add((Port) eObject);
			}
		}
	}

	public void createMUMLSoftwareComponent2UMLCollaboration(EObject baseTrace, List<EObject> selection) {

		MUMLSoftwareComponent2UMLCollaboration trace = (MUMLSoftwareComponent2UMLCollaboration) baseTrace;
		for (EObject eObject : selection) {
			if (eObject instanceof Component) {
				trace.getComponent().add((Component) eObject);
			} else if (eObject instanceof Collaboration) {
				trace.getCollaboration().add((Collaboration) eObject);
			}
		}
	}

	public void createMUMLRegion2UMLCollaboration(EObject baseTrace, List<EObject> selection) {

		MUMLRegion2UMLCollaboration trace = (MUMLRegion2UMLCollaboration) baseTrace;
		for (EObject eObject : selection) {
			if (eObject instanceof Region) {
				trace.getRegion().add((Region) eObject);
			} else if (eObject instanceof Collaboration) {
				trace.getCollaboration().add((Collaboration) eObject);
			}
		}
	}

	public void createAPP4MCRunnable2MUMLRegion(EObject baseTrace, List<EObject> selection) {

		APP4MCRunnable2MUMLRegion trace = (APP4MCRunnable2MUMLRegion) baseTrace;
		for (EObject eObject : selection) {
			if (eObject instanceof Runnable) {
				trace.setRunnable((Runnable) eObject);
			} else {
				trace.setRegion((Region) eObject);
			}
		}
	}

	public void createRelatedTo(EObject baseTrace, List<EObject> selection) {

		RelatedTo trace = (RelatedTo) baseTrace;
		trace.getItems().addAll(selection);
	}
}
