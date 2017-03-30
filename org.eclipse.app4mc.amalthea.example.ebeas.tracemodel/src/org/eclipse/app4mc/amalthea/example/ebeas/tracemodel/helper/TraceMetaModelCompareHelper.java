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

import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.APP4MCRunnable2MUMLRegion;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.MUMLDiscretePort2UMLPort;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.MUMLMsgTypeRepository2UMLInterface;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.MUMLRegion2UMLCollaboration;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.MUMLSoftwareComponent2UMLClass;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.MUMLSoftwareComponent2UMLCollaboration;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.RelatedTo;
import org.eclipse.app4mc.amalthea.example.ebeas.tracemodel.TraceLink;
import org.eclipse.emf.ecore.EObject;

public class TraceMetaModelCompareHelper {

	public boolean analyzeMUMLMsgTypeRepository2UMLInterface(TraceLink baseTrace, EObject first, EObject second) {
		if (baseTrace instanceof MUMLMsgTypeRepository2UMLInterface) {
		MUMLMsgTypeRepository2UMLInterface trace = (MUMLMsgTypeRepository2UMLInterface) baseTrace;
		return ((first.equals(trace.getMsgInterface()) && second.equals(trace.getMsgTypeRepository()))
				|| (second.equals(trace.getMsgInterface()) && first.equals(trace.getMsgTypeRepository())));
		} else return false;
	}

	public boolean analyzeMUMLSoftwareComponent2UMLClass(TraceLink baseTrace, EObject first, EObject second) {
		if (baseTrace instanceof MUMLSoftwareComponent2UMLClass) {
		MUMLSoftwareComponent2UMLClass trace = (MUMLSoftwareComponent2UMLClass) baseTrace;

		return ((!first.equals(second)) && first.equals(trace.getComponent()) && second.equals(trace.getScenarioType()))
				|| (second.equals(trace.getComponent()) && first.equals(trace.getScenarioType()));}
		else return false;
	}

	public boolean analyzeMUMLDiscretePort2UMLPort(TraceLink baseTrace, EObject first, EObject second) {
		if(baseTrace instanceof MUMLDiscretePort2UMLPort) {
		MUMLDiscretePort2UMLPort trace = (MUMLDiscretePort2UMLPort) baseTrace;
		return (trace.getDiscretePort().contains(first) || trace.getPort().contains(first))
				&& (trace.getDiscretePort().contains(second) || trace.getPort().contains(second));}
		else return false;
	}

	public boolean analyzeMUMLSoftwareComponent2UMLCollaboration(TraceLink baseTrace, EObject first, EObject second) {
		 if(baseTrace instanceof MUMLSoftwareComponent2UMLCollaboration) {
		MUMLSoftwareComponent2UMLCollaboration trace = (MUMLSoftwareComponent2UMLCollaboration) baseTrace;

		return (trace.getCollaboration().contains(first) || trace.getComponent().contains(first))
				&& (trace.getCollaboration().contains(second) || trace.getComponent().contains(second));}
		else return false;
	}

	public boolean analyzeMUMLRegion2UMLCollaboration(TraceLink baseTrace, EObject first, EObject second) {
		 if(baseTrace instanceof MUMLRegion2UMLCollaboration) {
		MUMLRegion2UMLCollaboration trace = (MUMLRegion2UMLCollaboration) baseTrace;

		return (trace.getCollaboration().contains(first) || trace.getRegion().contains(first))
				&& (trace.getCollaboration().contains(second) || trace.getRegion().contains(second));}
		else return false;
	}

	public boolean analyzeAPP4MCRunnable2MUMLRegion(TraceLink baseTrace, EObject first, EObject second) {
		if(baseTrace instanceof APP4MCRunnable2MUMLRegion) {
		APP4MCRunnable2MUMLRegion trace = (APP4MCRunnable2MUMLRegion) baseTrace;

		return (first.equals(trace.getRegion()) && second.equals(trace.getRunnable()))
				|| (second.equals(trace.getRegion()) && first.equals(trace.getRunnable()));}
		else return false;
	}

	public boolean analyzeRelatedTo(TraceLink baseTrace, EObject first, EObject second) {
		 if (baseTrace instanceof RelatedTo){
		RelatedTo trace = (RelatedTo) baseTrace;
		return ( trace.getItems().contains(first) && trace.getItems().contains(second));}
		else return false;
	}
}
