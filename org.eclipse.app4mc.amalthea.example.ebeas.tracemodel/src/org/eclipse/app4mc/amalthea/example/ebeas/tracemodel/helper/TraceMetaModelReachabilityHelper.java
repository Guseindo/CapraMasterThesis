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
import java.util.Vector;

import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.APP4MCRunnable2MUMLRegion;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.MUMLDiscretePort2UMLPort;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.MUMLMsgTypeRepository2UMLInterface;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.MUMLRegion2UMLCollaboration;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.MUMLSoftwareComponent2UMLClass;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.MUMLSoftwareComponent2UMLCollaboration;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.RelatedTo;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.TraceLink;
import org.eclipse.emf.ecore.EObject;

public class TraceMetaModelReachabilityHelper {

	public List<EObject> getConnectedElements(TraceLink basetraceLink) {
		List<EObject> connectedElements = new Vector<EObject>();

		if (basetraceLink instanceof MUMLMsgTypeRepository2UMLInterface) {
			MUMLMsgTypeRepository2UMLInterface traceLink = (MUMLMsgTypeRepository2UMLInterface) basetraceLink;
			connectedElements.add(traceLink.getMsgInterface());
			connectedElements.add(traceLink.getMsgTypeRepository());
		} else if (basetraceLink instanceof MUMLSoftwareComponent2UMLClass) {
			MUMLSoftwareComponent2UMLClass traceLink = (MUMLSoftwareComponent2UMLClass) basetraceLink;
			connectedElements.add(traceLink.getComponent());
			connectedElements.add(traceLink.getScenarioType());
		} else if (basetraceLink instanceof MUMLDiscretePort2UMLPort) {
			MUMLDiscretePort2UMLPort traceLink = (MUMLDiscretePort2UMLPort) basetraceLink;
			connectedElements.addAll(traceLink.getDiscretePort());
			connectedElements.addAll(traceLink.getPort());
		} else if (basetraceLink instanceof MUMLSoftwareComponent2UMLCollaboration) {
			MUMLSoftwareComponent2UMLCollaboration traceLink = (MUMLSoftwareComponent2UMLCollaboration) basetraceLink;
			connectedElements.addAll(traceLink.getCollaboration());
			connectedElements.addAll(traceLink.getComponent());
		} else if (basetraceLink instanceof MUMLRegion2UMLCollaboration) {
			MUMLRegion2UMLCollaboration traceLink = (MUMLRegion2UMLCollaboration) basetraceLink;
			connectedElements.addAll(traceLink.getCollaboration());
			connectedElements.addAll(traceLink.getRegion());
		} else if (basetraceLink instanceof APP4MCRunnable2MUMLRegion) {
			APP4MCRunnable2MUMLRegion traceLink = (APP4MCRunnable2MUMLRegion) basetraceLink;
			connectedElements.add(traceLink.getRegion());
			connectedElements.add(traceLink.getRunnable());
		} else if (basetraceLink instanceof RelatedTo) {
			RelatedTo traceLink = (RelatedTo) basetraceLink;
			connectedElements.addAll(traceLink.getItems());
		}

		return connectedElements;
	}
}
