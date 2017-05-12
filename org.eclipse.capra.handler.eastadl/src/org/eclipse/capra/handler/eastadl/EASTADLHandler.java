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
package org.eclipse.capra.handler.eastadl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capra.core.adapters.Connection;
import org.eclipse.capra.core.handlers.AbstractArtifactHandler;
import org.eclipse.eatop.eastadl21.ClampConnector;
import org.eclipse.eatop.eastadl21.ClampConnector_port;
import org.eclipse.eatop.eastadl21.DeriveRequirement;
import org.eclipse.eatop.eastadl21.EAElement;
import org.eclipse.eatop.eastadl21.Extend;
import org.eclipse.eatop.eastadl21.FaultFailurePropagationLink;
import org.eclipse.eatop.eastadl21.FaultFailurePropagationLink_fromPort;
import org.eclipse.eatop.eastadl21.FaultFailurePropagationLink_toPort;
import org.eclipse.eatop.eastadl21.Feature;
import org.eclipse.eatop.eastadl21.FeatureLink;
import org.eclipse.eatop.eastadl21.FunctionConnector;
import org.eclipse.eatop.eastadl21.FunctionConnector_port;
import org.eclipse.eatop.eastadl21.HardwareConnector;
import org.eclipse.eatop.eastadl21.HardwareConnector_port;
import org.eclipse.eatop.eastadl21.HardwarePortConnector;
import org.eclipse.eatop.eastadl21.HardwarePortConnector_port;
import org.eclipse.eatop.eastadl21.Include;
import org.eclipse.eatop.eastadl21.Realization;
import org.eclipse.eatop.eastadl21.Realization_realized;
import org.eclipse.eatop.eastadl21.Realization_realizedBy;
import org.eclipse.eatop.eastadl21.Refine;
import org.eclipse.eatop.eastadl21.Refine_refinedBy;
import org.eclipse.eatop.eastadl21.Requirement;
import org.eclipse.eatop.eastadl21.RequirementsLink;
import org.eclipse.eatop.eastadl21.Satisfy;
import org.eclipse.eatop.eastadl21.Satisfy_satisfiedBy;
import org.eclipse.eatop.eastadl21.State;
import org.eclipse.eatop.eastadl21.Transition;
import org.eclipse.eatop.eastadl21.UseCase;
import org.eclipse.eatop.eastadl21.VVCase;
import org.eclipse.eatop.eastadl21.VVProcedure;
import org.eclipse.eatop.eastadl21.Verify;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * Handler to allow tracing to and from arbitrary model elements handled by EMF.
 */
public class EASTADLHandler extends AbstractArtifactHandler<EAElement> {

	@Override
	public EObject createWrapper(EAElement artifact, EObject artifactModel) {
		return artifact;
	}

	@Override
	public EAElement resolveWrapper(EObject wrapper) {
		return (EAElement) wrapper;
	}

	@Override
	public String getDisplayName(EAElement artifact) {
		return artifact.eClass().getName();
	}

	@Override
	public void addInternalLinks(EObject investigatedElement, List<Connection> allElements,
			ArrayList<Integer> duplicationCheck, List<String> selectedRelationshipTypes) {
		if (Realization.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				Realization rel = Realization.class.cast(investigatedElement);
				List<EObject> relatedElements = new ArrayList<>();
				rel.getRealized().forEach(refBy -> relatedElements.add(refBy));
				rel.getRealizedBy().forEach(req -> relatedElements.add(req));
				int connectionHash = investigatedElement.hashCode() + rel.hashCode();
				for (EObject el : relatedElements) {
					connectionHash += el.hashCode();
				}
				if (!duplicationCheck.contains(connectionHash)) {
					Connection conn = new Connection(investigatedElement, relatedElements, rel);
					allElements.add(conn);
					duplicationCheck.add(connectionHash);
				}
			}
		} else if (Extend.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				Extend rel = Extend.class.cast(investigatedElement);
				List<EObject> relatedElements = new ArrayList<>();
				relatedElements.add(rel.getExtendedCase());
				int connectionHash = investigatedElement.hashCode() + rel.hashCode() + rel.getExtendedCase().hashCode();
				if (!duplicationCheck.contains(connectionHash)) {
					Connection conn = new Connection(investigatedElement, relatedElements, rel);
					allElements.add(conn);
					duplicationCheck.add(connectionHash);
				}
			}
		} else if (Include.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				Include rel = Include.class.cast(investigatedElement);
				List<EObject> relatedElements = new ArrayList<>();
				relatedElements.add(rel.getAddition());
				int connectionHash = investigatedElement.hashCode() + rel.hashCode() + rel.getAddition().hashCode();
				if (!duplicationCheck.contains(connectionHash)) {
					Connection conn = new Connection(investigatedElement, relatedElements, rel);
					allElements.add(conn);
					duplicationCheck.add(connectionHash);
				}
			}
		} else if (FeatureLink.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				FeatureLink rel = FeatureLink.class.cast(investigatedElement);
				List<EObject> relatedElements = new ArrayList<>();
				relatedElements.add(rel.getStart());
				relatedElements.add(rel.getEnd());
				int connectionHash = investigatedElement.hashCode() + rel.hashCode() + rel.getStart().hashCode()
						+ rel.getEnd().hashCode();
				if (!duplicationCheck.contains(connectionHash)) {
					Connection conn = new Connection(investigatedElement, relatedElements, rel);
					allElements.add(conn);
					duplicationCheck.add(connectionHash);
				}
			}
		} else if (Refine.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				Refine rel = Refine.class.cast(investigatedElement);
				List<EObject> relatedElements = new ArrayList<>();
				rel.getRefinedBy().forEach(refBy -> relatedElements.add(refBy));
				rel.getRefinedRequirement().forEach(req -> relatedElements.add(req));
				int connectionHash = investigatedElement.hashCode() + rel.hashCode();
				for (EObject el : relatedElements) {
					connectionHash += el.hashCode();
				}
				if (!duplicationCheck.contains(connectionHash)) {
					Connection conn = new Connection(investigatedElement, relatedElements, rel);
					allElements.add(conn);
					duplicationCheck.add(connectionHash);
				}
			}
		} else if (Verify.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				Verify rel = Verify.class.cast(investigatedElement);
				List<EObject> relatedElements = new ArrayList<>();
				rel.getVerifiedByCase().forEach(refBy -> relatedElements.add(refBy));
				rel.getVerifiedRequirement().forEach(req -> relatedElements.add(req));
				rel.getVerifiedByProcedure().forEach(req -> relatedElements.add(req));

				int connectionHash = investigatedElement.hashCode() + rel.hashCode();
				for (EObject el : relatedElements) {
					connectionHash += el.hashCode();
				}
				if (!duplicationCheck.contains(connectionHash)) {
					Connection conn = new Connection(investigatedElement, relatedElements, rel);
					allElements.add(conn);
					duplicationCheck.add(connectionHash);
				}
			}
		} else if (Satisfy.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				Satisfy rel = Satisfy.class.cast(investigatedElement);
				List<EObject> relatedElements = new ArrayList<>();
				rel.getSatisfiedBy().forEach(refBy -> relatedElements.add(refBy));
				rel.getSatisfiedRequirement().forEach(req -> relatedElements.add(req));
				rel.getSatisfiedUseCase().forEach(req -> relatedElements.add(req));
				int connectionHash = investigatedElement.hashCode() + rel.hashCode();
				for (EObject el : relatedElements) {
					connectionHash += el.hashCode();
				}
				if (!duplicationCheck.contains(connectionHash)) {
					Connection conn = new Connection(investigatedElement, relatedElements, rel);
					allElements.add(conn);
					duplicationCheck.add(connectionHash);
				}
			}
		} else if (DeriveRequirement.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				DeriveRequirement rel = DeriveRequirement.class.cast(investigatedElement);
				List<EObject> relatedElements = new ArrayList<>();
				rel.getDerived().forEach(refBy -> relatedElements.add(refBy));
				rel.getDerivedFrom().forEach(req -> relatedElements.add(req));
				int connectionHash = investigatedElement.hashCode() + rel.hashCode();
				for (EObject el : relatedElements) {
					connectionHash += el.hashCode();
				}
				if (!duplicationCheck.contains(connectionHash)) {
					Connection conn = new Connection(investigatedElement, relatedElements, rel);
					allElements.add(conn);
					duplicationCheck.add(connectionHash);
				}
			}
		} else if (RequirementsLink.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				RequirementsLink rel = RequirementsLink.class.cast(investigatedElement);
				List<EObject> relatedElements = new ArrayList<>();
				rel.getSource().forEach(refBy -> relatedElements.add(refBy));
				rel.getTarget().forEach(req -> relatedElements.add(req));
				int connectionHash = investigatedElement.hashCode() + rel.hashCode();
				for (EObject el : relatedElements) {
					connectionHash += el.hashCode();
				}
				if (!duplicationCheck.contains(connectionHash)) {
					Connection conn = new Connection(investigatedElement, relatedElements, rel);
					allElements.add(conn);
					duplicationCheck.add(connectionHash);
				}
			}
		} else if (Transition.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				Transition transition = Transition.class.cast(investigatedElement);
				List<EObject> relatedElements = new ArrayList<>();
				relatedElements.add(transition.getFrom());
				relatedElements.add(transition.getTo());
				int connectionHash = investigatedElement.hashCode() + transition.hashCode()
						+ transition.getFrom().hashCode() + transition.getTo().hashCode();
				if (!duplicationCheck.contains(connectionHash)) {
					Connection conn = new Connection(investigatedElement, relatedElements, transition);
					allElements.add(conn);
					duplicationCheck.add(connectionHash);
				}
			}
		} else if (FaultFailurePropagationLink.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				FaultFailurePropagationLink propLink = FaultFailurePropagationLink.class.cast(investigatedElement);
				List<EObject> relatedElements = new ArrayList<>();
				relatedElements.add(propLink.getFromPort());
				relatedElements.add(propLink.getToPort());
				int connectionHash = investigatedElement.hashCode() + propLink.hashCode()
						+ propLink.getFromPort().hashCode() + propLink.getToPort().hashCode();
				;
				if (!duplicationCheck.contains(connectionHash)) {
					Connection conn = new Connection(investigatedElement, relatedElements, propLink);
					allElements.add(conn);
					duplicationCheck.add(connectionHash);
				}
			}
		} else if (FunctionConnector.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				FunctionConnector funcConnector = FunctionConnector.class.cast(investigatedElement);
				List<EObject> relatedElements = new ArrayList<>();
				funcConnector.getPort().forEach(port -> relatedElements.add(port));
				int connectionHash = investigatedElement.hashCode() + funcConnector.hashCode();
				for (EObject el : relatedElements) {
					connectionHash += el.hashCode();
				}
				if (!duplicationCheck.contains(connectionHash)) {
					Connection conn = new Connection(investigatedElement, relatedElements, funcConnector);
					allElements.add(conn);
					duplicationCheck.add(connectionHash);
				}
			}
		} else if (HardwareConnector.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				HardwareConnector hardwareConnector = HardwareConnector.class.cast(investigatedElement);
				List<EObject> relatedElements = new ArrayList<>();
				hardwareConnector.getPort().forEach(port -> relatedElements.add(port));
				int connectionHash = investigatedElement.hashCode() + hardwareConnector.hashCode();
				for (EObject el : relatedElements) {
					connectionHash += el.hashCode();
				}
				if (!duplicationCheck.contains(connectionHash)) {
					Connection conn = new Connection(investigatedElement, relatedElements, hardwareConnector);
					allElements.add(conn);
					duplicationCheck.add(connectionHash);
				}
			}
		} else if (HardwarePortConnector.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				HardwarePortConnector hardwareConnector = HardwarePortConnector.class.cast(investigatedElement);
				List<EObject> relatedElements = new ArrayList<>();
				hardwareConnector.getPort().forEach(port -> relatedElements.add(port));
				int connectionHash = investigatedElement.hashCode() + hardwareConnector.hashCode();
				for (EObject el : relatedElements) {
					connectionHash += el.hashCode();
				}
				if (!duplicationCheck.contains(connectionHash)) {
					Connection conn = new Connection(investigatedElement, relatedElements, hardwareConnector);
					allElements.add(conn);
					duplicationCheck.add(connectionHash);
				}
			}
		} else if (ClampConnector.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				ClampConnector clampConnector = ClampConnector.class.cast(investigatedElement);
				List<EObject> relatedElements = new ArrayList<>();
				clampConnector.getPort().forEach(port -> relatedElements.add(port));
				int connectionHash = investigatedElement.hashCode() + clampConnector.hashCode();
				for (EObject el : relatedElements) {
					connectionHash += el.hashCode();
				}
				if (!duplicationCheck.contains(connectionHash)) {
					Connection conn = new Connection(investigatedElement, relatedElements, clampConnector);
					allElements.add(conn);
					duplicationCheck.add(connectionHash);
				}
			}
		} else {
			EObject root = EcoreUtil.getRootContainer(investigatedElement);
			TreeIterator<EObject> modelContents = root.eAllContents();
			while (modelContents.hasNext()) {
				EObject content = modelContents.next();
				if (selectedRelationshipTypes.size() == 0
						|| selectedRelationshipTypes.contains(content.eClass().getName())) {
					if (Realization.class.isAssignableFrom(content.getClass())) {
						Realization rel = Realization.class.cast(content);
						List<EObject> relatedElements = new ArrayList<>();
						boolean isRelatedToElement = false;
						for (Realization_realized realized : rel.getRealized()) {
							if (realized.hashCode() == investigatedElement.hashCode()) {
								isRelatedToElement = true;
							} else {
								relatedElements.add(realized);
							}
						}
						for (Realization_realizedBy realizedBy : rel.getRealizedBy()) {
							if (realizedBy.hashCode() == investigatedElement.hashCode()) {
								isRelatedToElement = true;
							} else {
								relatedElements.add(realizedBy);
							}
						}
						if (isRelatedToElement) {
							int connectionHash = investigatedElement.hashCode() + rel.hashCode();
							for (EObject el : relatedElements) {
								connectionHash += el.hashCode();
							}
							if (!duplicationCheck.contains(connectionHash)) {
								Connection conn = new Connection(investigatedElement, relatedElements, rel);
								allElements.add(conn);
								duplicationCheck.add(connectionHash);
							}
						}
					} else if (Extend.class.isAssignableFrom(content.getClass())) {
						Extend rel = Extend.class.cast(content);
						List<EObject> relatedElements = new ArrayList<>();
						UseCase extended = rel.getExtendedCase();
						boolean isRelatedToElement = false;
						if (extended.hashCode() == investigatedElement.hashCode()) {
							isRelatedToElement = true;
							relatedElements.add(rel);
						}
						if (isRelatedToElement) {
							int connectionHash = investigatedElement.hashCode() + rel.hashCode();
							for (EObject el : relatedElements) {
								connectionHash += el.hashCode();
							}
							if (!duplicationCheck.contains(connectionHash)) {
								Connection conn = new Connection(investigatedElement, relatedElements, rel);
								allElements.add(conn);
								duplicationCheck.add(connectionHash);
							}
						}
					} else if (Include.class.isAssignableFrom(content.getClass())) {
						Include rel = Include.class.cast(content);
						List<EObject> relatedElements = new ArrayList<>();
						UseCase addition = rel.getAddition();
						boolean isRelatedToElement = false;
						if (addition.hashCode() == investigatedElement.hashCode()) {
							isRelatedToElement = true;
							relatedElements.add(rel);
						}
						if (isRelatedToElement) {
							int connectionHash = investigatedElement.hashCode() + rel.hashCode();
							for (EObject el : relatedElements) {
								connectionHash += el.hashCode();
							}
							if (!duplicationCheck.contains(connectionHash)) {
								Connection conn = new Connection(investigatedElement, relatedElements, rel);
								allElements.add(conn);
								duplicationCheck.add(connectionHash);
							}
						}
					} else if (FeatureLink.class.isAssignableFrom(content.getClass())) {
						FeatureLink rel = FeatureLink.class.cast(content);
						List<EObject> relatedElements = new ArrayList<>();
						Feature start = rel.getStart();
						Feature end = rel.getEnd();
						boolean isRelatedToElement = false;
						if (start.hashCode() == investigatedElement.hashCode()) {
							isRelatedToElement = true;
							relatedElements.add(end);
						} else if (end.hashCode() == investigatedElement.hashCode()) {
							isRelatedToElement = true;
							relatedElements.add(start);
						}
						if (isRelatedToElement) {
							int connectionHash = investigatedElement.hashCode() + rel.hashCode();
							for (EObject el : relatedElements) {
								connectionHash += el.hashCode();
							}
							if (!duplicationCheck.contains(connectionHash)) {
								Connection conn = new Connection(investigatedElement, relatedElements, rel);
								allElements.add(conn);
								duplicationCheck.add(connectionHash);
							}
						}
					} else if (Refine.class.isAssignableFrom(content.getClass())) {
						Refine rel = Refine.class.cast(content);
						List<EObject> relatedElements = new ArrayList<>();
						rel.getRefinedBy().forEach(refBy -> relatedElements.add(refBy));
						rel.getRefinedRequirement().forEach(req -> relatedElements.add(req));
						boolean isRelatedToElement = false;
						for (Refine_refinedBy refine : rel.getRefinedBy()) {
							if (refine.hashCode() == investigatedElement.hashCode()) {
								isRelatedToElement = true;
							} else {
								relatedElements.add(refine);
							}
						}
						for (Requirement req : rel.getRefinedRequirement()) {
							if (req.hashCode() == investigatedElement.hashCode()) {
								isRelatedToElement = true;
							} else {
								relatedElements.add(req);
							}
						}
						if (isRelatedToElement) {
							int connectionHash = investigatedElement.hashCode() + rel.hashCode();
							for (EObject el : relatedElements) {
								connectionHash += el.hashCode();
							}
							if (!duplicationCheck.contains(connectionHash)) {
								Connection conn = new Connection(investigatedElement, relatedElements, rel);
								allElements.add(conn);
								duplicationCheck.add(connectionHash);
							}
						}
					} else if (Verify.class.isAssignableFrom(content.getClass())) {
						Verify rel = Verify.class.cast(content);
						List<EObject> relatedElements = new ArrayList<>();
						rel.getVerifiedByCase().forEach(refBy -> relatedElements.add(refBy));
						rel.getVerifiedRequirement().forEach(req -> relatedElements.add(req));
						rel.getVerifiedByProcedure().forEach(req -> relatedElements.add(req));
						boolean isRelatedToElement = false;
						for (VVCase vCase : rel.getVerifiedByCase()) {
							if (vCase.hashCode() == investigatedElement.hashCode()) {
								isRelatedToElement = true;
							} else {
								relatedElements.add(vCase);
							}
						}
						for (Requirement req : rel.getVerifiedRequirement()) {
							if (req.hashCode() == investigatedElement.hashCode()) {
								isRelatedToElement = true;
							} else {
								relatedElements.add(req);
							}
						}
						for (VVProcedure vProcedure : rel.getVerifiedByProcedure()) {
							if (vProcedure.hashCode() == investigatedElement.hashCode()) {
								isRelatedToElement = true;
							} else {
								relatedElements.add(vProcedure);
							}
						}
						if (isRelatedToElement) {
							int connectionHash = investigatedElement.hashCode() + rel.hashCode();
							for (EObject el : relatedElements) {
								connectionHash += el.hashCode();
							}
							if (!duplicationCheck.contains(connectionHash)) {
								Connection conn = new Connection(investigatedElement, relatedElements, rel);
								allElements.add(conn);
								duplicationCheck.add(connectionHash);
							}
						}
					} else if (Satisfy.class.isAssignableFrom(content.getClass())) {
						Satisfy rel = Satisfy.class.cast(content);
						List<EObject> relatedElements = new ArrayList<>();
						rel.getSatisfiedBy().forEach(refBy -> relatedElements.add(refBy));
						rel.getSatisfiedRequirement().forEach(req -> relatedElements.add(req));
						rel.getSatisfiedUseCase().forEach(req -> relatedElements.add(req));
						boolean isRelatedToElement = false;
						for (Satisfy_satisfiedBy satisfy : rel.getSatisfiedBy()) {
							if (satisfy.hashCode() == investigatedElement.hashCode()) {
								isRelatedToElement = true;
							} else {
								relatedElements.add(satisfy);
							}
						}
						for (Requirement req : rel.getSatisfiedRequirement()) {
							if (req.hashCode() == investigatedElement.hashCode()) {
								isRelatedToElement = true;
							} else {
								relatedElements.add(req);
							}
						}
						for (UseCase useCase : rel.getSatisfiedUseCase()) {
							if (useCase.hashCode() == investigatedElement.hashCode()) {
								isRelatedToElement = true;
							} else {
								relatedElements.add(useCase);
							}
						}
						if (isRelatedToElement) {
							int connectionHash = investigatedElement.hashCode() + rel.hashCode();
							for (EObject el : relatedElements) {
								connectionHash += el.hashCode();
							}
							if (!duplicationCheck.contains(connectionHash)) {
								Connection conn = new Connection(investigatedElement, relatedElements, rel);
								allElements.add(conn);
								duplicationCheck.add(connectionHash);
							}
						}
					} else if (DeriveRequirement.class.isAssignableFrom(content.getClass())) {
						DeriveRequirement rel = DeriveRequirement.class.cast(content);
						List<EObject> relatedElements = new ArrayList<>();
						boolean isRelatedToElement = false;
						for (Requirement req : rel.getDerived()) {
							if (req.hashCode() == investigatedElement.hashCode()) {
								isRelatedToElement = true;
							} else {
								relatedElements.add(req);
							}
						}
						for (Requirement req : rel.getDerivedFrom()) {
							if (req.hashCode() == investigatedElement.hashCode()) {
								isRelatedToElement = true;
							} else {
								relatedElements.add(req);
							}
						}
						if (isRelatedToElement) {
							int connectionHash = investigatedElement.hashCode() + rel.hashCode();
							for (EObject el : relatedElements) {
								connectionHash += el.hashCode();
							}
							if (!duplicationCheck.contains(connectionHash)) {
								Connection conn = new Connection(investigatedElement, relatedElements, rel);
								allElements.add(conn);
								duplicationCheck.add(connectionHash);
							}
						}
					} else if (RequirementsLink.class.isAssignableFrom(content.getClass())) {
						RequirementsLink rel = RequirementsLink.class.cast(investigatedElement);
						List<EObject> relatedElements = new ArrayList<>();
						boolean isRelatedToElement = false;
						for (Requirement req : rel.getSource()) {
							if (req.hashCode() == investigatedElement.hashCode()) {
								isRelatedToElement = true;
							} else {
								relatedElements.add(req);
							}
						}
						for (Requirement req : rel.getTarget()) {
							if (req.hashCode() == investigatedElement.hashCode()) {
								isRelatedToElement = true;
							} else {
								relatedElements.add(req);
							}
						}
						if (isRelatedToElement) {
							int connectionHash = investigatedElement.hashCode() + rel.hashCode();
							for (EObject el : relatedElements) {
								connectionHash += el.hashCode();
							}
							if (!duplicationCheck.contains(connectionHash)) {
								Connection conn = new Connection(investigatedElement, relatedElements, rel);
								allElements.add(conn);
								duplicationCheck.add(connectionHash);
							}
						}
					} else if (Transition.class.isAssignableFrom(content.getClass())) {
						Transition transition = Transition.class.cast(content);
						List<EObject> relatedElements = new ArrayList<>();
						State fromState = transition.getFrom();
						State toState = transition.getTo();
						boolean isRelatedToElement = false;
						if (fromState.hashCode() == investigatedElement.hashCode()) {
							isRelatedToElement = true;
							relatedElements.add(toState);
						} else if (toState.hashCode() == investigatedElement.hashCode()) {
							isRelatedToElement = true;
							relatedElements.add(fromState);
						}
						if (isRelatedToElement) {
							int connectionHash = investigatedElement.hashCode() + transition.hashCode();
							for (EObject el : relatedElements) {
								connectionHash += el.hashCode();
							}
							if (!duplicationCheck.contains(connectionHash)) {
								Connection conn = new Connection(investigatedElement, relatedElements, transition);
								allElements.add(conn);
								duplicationCheck.add(connectionHash);
							}
						}
					} else if (FaultFailurePropagationLink.class.isAssignableFrom(content.getClass())) {
						FaultFailurePropagationLink propLink = FaultFailurePropagationLink.class.cast(content);
						List<EObject> relatedElements = new ArrayList<>();
						FaultFailurePropagationLink_fromPort fromPort = propLink.getFromPort();
						FaultFailurePropagationLink_toPort toPort = propLink.getToPort();
						boolean isRelatedToElement = false;
						if (fromPort.hashCode() == investigatedElement.hashCode()) {
							isRelatedToElement = true;
							relatedElements.add(toPort);
						} else if (toPort.hashCode() == investigatedElement.hashCode()) {
							isRelatedToElement = true;
							relatedElements.add(fromPort);
						}
						if (isRelatedToElement) {
							int connectionHash = investigatedElement.hashCode() + propLink.hashCode();
							for (EObject el : relatedElements) {
								connectionHash += el.hashCode();
							}
							if (!duplicationCheck.contains(connectionHash)) {
								Connection conn = new Connection(investigatedElement, relatedElements, propLink);
								allElements.add(conn);
								duplicationCheck.add(connectionHash);
							}
						}
					} else if (FunctionConnector.class.isAssignableFrom(content.getClass())) {
						FunctionConnector funcConnector = FunctionConnector.class.cast(content);
						List<EObject> relatedElements = new ArrayList<>();
						boolean isRelatedToElement = false;
						for (FunctionConnector_port port : funcConnector.getPort()) {
							if (port.hashCode() == investigatedElement.hashCode()) {
								isRelatedToElement = true;
							} else {
								relatedElements.add(port);
							}
						}
						if (isRelatedToElement) {
							int connectionHash = investigatedElement.hashCode() + funcConnector.hashCode();
							for (EObject el : relatedElements) {
								connectionHash += el.hashCode();
							}
							if (!duplicationCheck.contains(connectionHash)) {
								Connection conn = new Connection(investigatedElement, relatedElements, funcConnector);
								allElements.add(conn);
								duplicationCheck.add(connectionHash);
							}
						}
					} else if (HardwareConnector.class.isAssignableFrom(content.getClass())) {
						HardwareConnector hardwareConnector = HardwareConnector.class.cast(content);
						List<EObject> relatedElements = new ArrayList<>();
						boolean isRelatedToElement = false;
						for (HardwareConnector_port port : hardwareConnector.getPort()) {
							if (port.hashCode() == investigatedElement.hashCode()) {
								isRelatedToElement = true;
							} else {
								relatedElements.add(port);
							}
						}
						if (isRelatedToElement) {
							int connectionHash = investigatedElement.hashCode() + hardwareConnector.hashCode();
							for (EObject el : relatedElements) {
								connectionHash += el.hashCode();
							}
							if (!duplicationCheck.contains(connectionHash)) {
								Connection conn = new Connection(investigatedElement, relatedElements,
										hardwareConnector);
								allElements.add(conn);
								duplicationCheck.add(connectionHash);
							}
						}
					} else if (HardwarePortConnector.class.isAssignableFrom(content.getClass())) {
						HardwarePortConnector hardwareConnector = HardwarePortConnector.class.cast(content);
						List<EObject> relatedElements = new ArrayList<>();
						hardwareConnector.getPort().forEach(port -> relatedElements.add(port));
						boolean isRelatedToElement = false;
						for (HardwarePortConnector_port port : hardwareConnector.getPort()) {
							if (port.hashCode() == investigatedElement.hashCode()) {
								isRelatedToElement = true;
							} else {
								relatedElements.add(port);
							}
						}
						if (isRelatedToElement) {
							int connectionHash = investigatedElement.hashCode() + hardwareConnector.hashCode();
							for (EObject el : relatedElements) {
								connectionHash += el.hashCode();
							}
							if (!duplicationCheck.contains(connectionHash)) {
								Connection conn = new Connection(investigatedElement, relatedElements,
										hardwareConnector);
								allElements.add(conn);
								duplicationCheck.add(connectionHash);
							}
						}
					} else if (ClampConnector.class.isAssignableFrom(content.getClass())) {
						ClampConnector clampConnector = ClampConnector.class.cast(content);
						List<EObject> relatedElements = new ArrayList<>();
						boolean isRelatedToElement = false;
						for (ClampConnector_port port : clampConnector.getPort()) {
							if (port.hashCode() == investigatedElement.hashCode()) {
								isRelatedToElement = true;
							} else {
								relatedElements.add(port);
							}
						}
						if (isRelatedToElement) {
							int connectionHash = investigatedElement.hashCode() + clampConnector.hashCode();
							for (EObject el : relatedElements) {
								connectionHash += el.hashCode();
							}
							if (!duplicationCheck.contains(connectionHash)) {
								Connection conn = new Connection(investigatedElement, relatedElements, clampConnector);
								allElements.add(conn);
								duplicationCheck.add(connectionHash);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public String isThereAnInternalTraceBetween(EObject first, EObject second, EObject traceModel) {
		if (Realization.class.isAssignableFrom(first.getClass())
				|| Realization.class.isAssignableFrom(second.getClass())) {
			Realization rel;
			if (Realization.class.isAssignableFrom(first.getClass())) {
				rel = Realization.class.cast(first);
			} else {
				rel = Realization.class.cast(second);
			}
			boolean isRelated = false;
			for (Realization_realized realized : rel.getRealized()) {
				if (realized.hashCode() == first.hashCode() || realized.hashCode() == second.hashCode()) {
					isRelated = true;
				}
			}
			for (Realization_realizedBy realized : rel.getRealizedBy()) {
				if (realized.hashCode() == first.hashCode() || realized.hashCode() == second.hashCode()) {
					isRelated = true;
				}
			}
			if (isRelated) {
				return "X";
			}
			return "";
		} else if (Extend.class.isAssignableFrom(first.getClass())
				|| Extend.class.isAssignableFrom(second.getClass())) {
			Extend rel;
			if (Extend.class.isAssignableFrom(first.getClass())) {
				rel = Extend.class.cast(first);
			} else {
				rel = Extend.class.cast(second);
			}
			if (rel.getExtendedCase().hashCode() == first.hashCode()
					|| rel.getExtendedCase().hashCode() == second.hashCode()) {
				return "X";
			}
			return "";
		} else if (Include.class.isAssignableFrom(first.getClass())
				|| Include.class.isAssignableFrom(second.getClass())) {
			Include rel;
			if (Include.class.isAssignableFrom(first.getClass())) {
				rel = Include.class.cast(first);
			} else {
				rel = Include.class.cast(second);
			}
			if (rel.getAddition().hashCode() == first.hashCode() || rel.getAddition().hashCode() == second.hashCode()) {
				return "X";
			}
			return "";
		} else if (FeatureLink.class.isAssignableFrom(first.getClass())
				|| FeatureLink.class.isAssignableFrom(second.getClass())) {
			FeatureLink rel;
			if (FeatureLink.class.isAssignableFrom(first.getClass())) {
				rel = FeatureLink.class.cast(first);
			} else {
				rel = FeatureLink.class.cast(second);
			}
			if (rel.getStart().hashCode() == first.hashCode() || rel.getStart().hashCode() == second.hashCode()
					|| rel.getEnd().hashCode() == first.hashCode() || rel.getEnd().hashCode() == second.hashCode()) {
				return "X";
			}
			return "";
		} else if (Refine.class.isAssignableFrom(first.getClass())
				|| Refine.class.isAssignableFrom(second.getClass())) {
			Refine rel;
			if (Refine.class.isAssignableFrom(first.getClass())) {
				rel = Refine.class.cast(first);
			} else {
				rel = Refine.class.cast(second);
			}
			boolean isRelated = false;
			for (Refine_refinedBy refinedBy : rel.getRefinedBy()) {
				if (refinedBy.hashCode() == first.hashCode() || refinedBy.hashCode() == second.hashCode()) {
					isRelated = true;
				}
			}
			for (Requirement req : rel.getRefinedRequirement()) {
				if (req.hashCode() == first.hashCode() || req.hashCode() == second.hashCode()) {
					isRelated = true;
				}
			}
			if (isRelated) {
				return "X";
			}
			return "";
		} else if (Verify.class.isAssignableFrom(first.getClass())
				|| Verify.class.isAssignableFrom(second.getClass())) {
			Verify rel;
			if (Verify.class.isAssignableFrom(first.getClass())) {
				rel = Verify.class.cast(first);
			} else {
				rel = Verify.class.cast(second);
			}
			boolean isRelated = false;
			for (VVCase vCase : rel.getVerifiedByCase()) {
				if (vCase.hashCode() == first.hashCode() || vCase.hashCode() == second.hashCode()) {
					isRelated = true;
				}
			}
			for (Requirement req : rel.getVerifiedRequirement()) {
				if (req.hashCode() == first.hashCode() || req.hashCode() == second.hashCode()) {
					isRelated = true;
				}
			}
			for (VVProcedure vProcedure : rel.getVerifiedByProcedure()) {
				if (vProcedure.hashCode() == first.hashCode() || vProcedure.hashCode() == second.hashCode()) {
					isRelated = true;
				}
			}
			if (isRelated) {
				return "X";
			}
			return "";
		} else if (Satisfy.class.isAssignableFrom(first.getClass())
				|| Satisfy.class.isAssignableFrom(second.getClass())) {
			Satisfy rel;
			if (Satisfy.class.isAssignableFrom(first.getClass())) {
				rel = Satisfy.class.cast(first);
			} else {
				rel = Satisfy.class.cast(second);
			}
			boolean isRelated = false;
			for (Satisfy_satisfiedBy satisfiedBy : rel.getSatisfiedBy()) {
				if (satisfiedBy.hashCode() == first.hashCode() || satisfiedBy.hashCode() == second.hashCode()) {
					isRelated = true;
				}
			}
			for (Requirement req : rel.getSatisfiedRequirement()) {
				if (req.hashCode() == first.hashCode() || req.hashCode() == second.hashCode()) {
					isRelated = true;
				}
			}
			for (UseCase useCase : rel.getSatisfiedUseCase()) {
				if (useCase.hashCode() == first.hashCode() || useCase.hashCode() == second.hashCode()) {
					isRelated = true;
				}
			}
			if (isRelated) {
				return "X";
			}
			return "";
		} else if (DeriveRequirement.class.isAssignableFrom(first.getClass())
				|| DeriveRequirement.class.isAssignableFrom(second.getClass())) {
			DeriveRequirement rel;
			if (DeriveRequirement.class.isAssignableFrom(first.getClass())) {
				rel = DeriveRequirement.class.cast(first);
			} else {
				rel = DeriveRequirement.class.cast(second);
			}
			boolean isRelated = false;
			for (Requirement req : rel.getDerived()) {
				if (req.hashCode() == first.hashCode() || req.hashCode() == second.hashCode()) {
					isRelated = true;
				}
			}
			for (Requirement req : rel.getDerivedFrom()) {
				if (req.hashCode() == first.hashCode() || req.hashCode() == second.hashCode()) {
					isRelated = true;
				}
			}
			if (isRelated) {
				return "X";
			}
			return "";
		} else if (RequirementsLink.class.isAssignableFrom(first.getClass())
				|| RequirementsLink.class.isAssignableFrom(second.getClass())) {
			RequirementsLink rel;
			if (RequirementsLink.class.isAssignableFrom(first.getClass())) {
				rel = RequirementsLink.class.cast(first);
			} else {
				rel = RequirementsLink.class.cast(second);
			}
			boolean isRelated = false;
			for (Requirement req : rel.getSource()) {
				if (req.hashCode() == first.hashCode() || req.hashCode() == second.hashCode()) {
					isRelated = true;
				}
			}
			for (Requirement req : rel.getTarget()) {
				if (req.hashCode() == first.hashCode() || req.hashCode() == second.hashCode()) {
					isRelated = true;
				}
			}
			if (isRelated) {
				return "X";
			}
			return "";
		} else if (Transition.class.isAssignableFrom(first.getClass())
				|| Transition.class.isAssignableFrom(second.getClass())) {
			Transition transition;
			if (Transition.class.isAssignableFrom(first.getClass())) {
				transition = Transition.class.cast(first);
			} else {
				transition = Transition.class.cast(second);
			}
			if (transition.getFrom().hashCode() == first.hashCode()
					|| transition.getFrom().hashCode() == second.hashCode()
					|| transition.getTo().hashCode() == first.hashCode()
					|| transition.getTo().hashCode() == second.hashCode()) {
				return "X";
			}
			return "";
		} else if (FaultFailurePropagationLink.class.isAssignableFrom(first.getClass())
				|| FaultFailurePropagationLink.class.isAssignableFrom(second.getClass())) {
			FaultFailurePropagationLink propLink;
			if (FaultFailurePropagationLink.class.isAssignableFrom(first.getClass())) {
				propLink = FaultFailurePropagationLink.class.cast(first);
			} else {
				propLink = FaultFailurePropagationLink.class.cast(second);
			}
			if (propLink.getFromPort().hashCode() == first.hashCode()
					|| propLink.getFromPort().hashCode() == second.hashCode()
					|| propLink.getToPort().hashCode() == first.hashCode()
					|| propLink.getToPort().hashCode() == second.hashCode()) {
				return "X";
			}
			return "";
		} else if (FunctionConnector.class.isAssignableFrom(first.getClass())
				|| FunctionConnector.class.isAssignableFrom(second.getClass())) {
			FunctionConnector funcConnector;
			if (FunctionConnector.class.isAssignableFrom(first.getClass())) {
				funcConnector = FunctionConnector.class.cast(first);
			} else {
				funcConnector = FunctionConnector.class.cast(second);
			}
			boolean isRelated = false;
			for (FunctionConnector_port port : funcConnector.getPort()) {
				if (port.hashCode() == first.hashCode() || port.hashCode() == second.hashCode()) {
					isRelated = true;
				}
			}
			if (isRelated) {
				return "X";
			}
			return "";
		} else if (HardwareConnector.class.isAssignableFrom(first.getClass())
				|| HardwareConnector.class.isAssignableFrom(second.getClass())) {
			HardwareConnector hardwareConnector;
			if (HardwareConnector.class.isAssignableFrom(first.getClass())) {
				hardwareConnector = HardwareConnector.class.cast(first);
			} else {
				hardwareConnector = HardwareConnector.class.cast(second);
			}
			boolean isRelated = false;
			for (HardwareConnector_port port : hardwareConnector.getPort()) {
				if (port.hashCode() == first.hashCode() || port.hashCode() == second.hashCode()) {
					isRelated = true;
				}
			}
			if (isRelated) {
				return "X";
			}
			return "";
		} else if (HardwarePortConnector.class.isAssignableFrom(first.getClass())
				|| HardwarePortConnector.class.isAssignableFrom(second.getClass())) {
			HardwarePortConnector hardwareConnector;
			if (HardwarePortConnector.class.isAssignableFrom(first.getClass())) {
				hardwareConnector = HardwarePortConnector.class.cast(first);
			} else {
				hardwareConnector = HardwarePortConnector.class.cast(second);
			}
			boolean isRelated = false;
			for (HardwarePortConnector_port port : hardwareConnector.getPort()) {
				if (port.hashCode() == first.hashCode() || port.hashCode() == second.hashCode()) {
					isRelated = true;
				}
			}
			if (isRelated) {
				return "X";
			}
			return "";
		} else if (ClampConnector.class.isAssignableFrom(first.getClass())
				|| ClampConnector.class.isAssignableFrom(second.getClass())) {
			ClampConnector clampConnector;
			if (ClampConnector.class.isAssignableFrom(first.getClass())) {
				clampConnector = ClampConnector.class.cast(first);
			} else {
				clampConnector = ClampConnector.class.cast(second);
			}
			boolean isRelated = false;
			for (ClampConnector_port port : clampConnector.getPort()) {
				if (port.hashCode() == first.hashCode() || port.hashCode() == second.hashCode()) {
					isRelated = true;
				}
			}
			if (isRelated) {
				return "X";
			}
			return "";
		} else {
			String traceString = "";
			EObject root = EcoreUtil.getRootContainer(first);
			TreeIterator<EObject> modelContents = root.eAllContents();
			boolean isRelated = false;
			String leftArrow = Character.toString((char) 0x2190);
			String upArrow = Character.toString((char) 0x2191);
			while (modelContents.hasNext()) {
				EObject content = modelContents.next();
				if (Realization.class.isAssignableFrom(content.getClass())) {
					Realization rel = Realization.class.cast(content);
					boolean relationContainsFirstElement = false;
					boolean relationContainsSecondElement = false;
					for (Realization_realized realized : rel.getRealized()) {
						if (realized.hashCode() == first.hashCode()) {
							relationContainsFirstElement = true;
						} else if (realized.hashCode() == second.hashCode()) {
							relationContainsSecondElement = true;
						}
					}
					for (Realization_realizedBy realized : rel.getRealizedBy()) {
						if (realized.hashCode() == first.hashCode()) {
							relationContainsFirstElement = true;
						} else if (realized.hashCode() == second.hashCode()) {
							relationContainsSecondElement = true;
						}
					}
					if (!isRelated) {
						isRelated = relationContainsFirstElement && relationContainsSecondElement;
					}
					if (relationContainsFirstElement && relationContainsSecondElement) {
						if (traceString == "") {
							traceString = rel.eClass().getName();
						} else {
							if (!traceString.toLowerCase().contains(rel.eClass().getName())) {
								traceString += ", " + rel.eClass().getName();
							}
						}
					}
				} else if (FeatureLink.class.isAssignableFrom(content.getClass())) {
					FeatureLink rel = FeatureLink.class.cast(content);
					boolean relationContainsFirstElement = false;
					boolean relationContainsSecondElement = false;
					if (rel.getStart().hashCode() == first.hashCode() || rel.getEnd().hashCode() == first.hashCode()) {
						relationContainsFirstElement = true;
					}
					if (rel.getStart().hashCode() == second.hashCode()
							|| rel.getEnd().hashCode() == second.hashCode()) {
						relationContainsSecondElement = true;
					}
					if (!isRelated) {
						isRelated = relationContainsFirstElement && relationContainsSecondElement;
					}
					if (relationContainsFirstElement && relationContainsSecondElement) {
						if (rel.getStart().hashCode() == first.hashCode()) {
							if (traceString == "") {
								traceString = rel.eClass().getName() + " " + upArrow;
							} else {
								if (!traceString.toLowerCase().contains(rel.eClass().getName())) {
									traceString += ", " + rel.eClass().getName() + " " + upArrow;
								}
							}
						} else {
							if (traceString == "") {
								traceString = leftArrow + " " + rel.eClass().getName();
							} else {
								if (!traceString.toLowerCase().contains(rel.eClass().getName())) {
									traceString += ", " + leftArrow + " " + rel.eClass().getName();
								}
							}
						}
					}
				} else if (Refine.class.isAssignableFrom(content.getClass())) {
					Refine rel = Refine.class.cast(content);
					boolean relationContainsFirstElement = false;
					boolean relationContainsSecondElement = false;
					for (Refine_refinedBy refinedBy : rel.getRefinedBy()) {
						if (refinedBy.hashCode() == first.hashCode()) {
							relationContainsFirstElement = true;
						} else if (refinedBy.hashCode() == second.hashCode()) {
							relationContainsSecondElement = true;
						}
					}
					for (Requirement req : rel.getRefinedRequirement()) {
						if (req.hashCode() == first.hashCode()) {
							relationContainsFirstElement = true;
						} else if (req.hashCode() == second.hashCode()) {
							relationContainsSecondElement = true;
						}
					}
					if (!isRelated) {
						isRelated = relationContainsFirstElement && relationContainsSecondElement;
					}
					if (relationContainsFirstElement && relationContainsSecondElement) {
						if (traceString == "") {
							traceString = rel.eClass().getName();
						} else {
							if (!traceString.toLowerCase().contains(rel.eClass().getName())) {
								traceString += ", " + rel.eClass().getName();
							}
						}
					}
				} else if (Verify.class.isAssignableFrom(content.getClass())) {
					Verify rel = Verify.class.cast(content);
					boolean relationContainsFirstElement = false;
					boolean relationContainsSecondElement = false;
					for (VVCase vCase : rel.getVerifiedByCase()) {
						if (vCase.hashCode() == first.hashCode()) {
							relationContainsFirstElement = true;
						} else if (vCase.hashCode() == second.hashCode()) {
							relationContainsSecondElement = true;
						}
					}
					for (Requirement req : rel.getVerifiedRequirement()) {
						if (req.hashCode() == first.hashCode()) {
							relationContainsFirstElement = true;
						} else if (req.hashCode() == second.hashCode()) {
							relationContainsSecondElement = true;
						}
					}
					for (VVProcedure vProcedure : rel.getVerifiedByProcedure()) {
						if (vProcedure.hashCode() == first.hashCode()) {
							relationContainsFirstElement = true;
						} else if (vProcedure.hashCode() == second.hashCode()) {
							relationContainsSecondElement = true;
						}
					}
					if (!isRelated) {
						isRelated = relationContainsFirstElement && relationContainsSecondElement;
					}
					if (relationContainsFirstElement && relationContainsSecondElement) {
						if (traceString == "") {
							traceString = rel.eClass().getName();
						} else {
							if (!traceString.toLowerCase().contains(rel.eClass().getName())) {
								traceString += ", " + rel.eClass().getName();
							}
						}
					}
				} else if (Satisfy.class.isAssignableFrom(content.getClass())) {
					Satisfy rel = Satisfy.class.cast(content);
					boolean relationContainsFirstElement = false;
					boolean relationContainsSecondElement = false;
					for (Satisfy_satisfiedBy satisfiedBy : rel.getSatisfiedBy()) {
						if (satisfiedBy.hashCode() == first.hashCode()) {
							relationContainsFirstElement = true;
						} else if (satisfiedBy.hashCode() == second.hashCode()) {
							relationContainsSecondElement = true;
						}
					}
					for (Requirement req : rel.getSatisfiedRequirement()) {
						if (req.hashCode() == first.hashCode()) {
							relationContainsFirstElement = true;
						} else if (req.hashCode() == second.hashCode()) {
							relationContainsSecondElement = true;
						}
					}
					for (UseCase useCase : rel.getSatisfiedUseCase()) {
						if (useCase.hashCode() == first.hashCode()) {
							relationContainsFirstElement = true;
						} else if (useCase.hashCode() == second.hashCode()) {
							relationContainsSecondElement = true;
						}
					}
					if (!isRelated) {
						isRelated = relationContainsFirstElement && relationContainsSecondElement;
					}
					if (relationContainsFirstElement && relationContainsSecondElement) {
						if (traceString == "") {
							traceString = rel.eClass().getName();
						} else {
							if (!traceString.toLowerCase().contains(rel.eClass().getName())) {
								traceString += ", " + rel.eClass().getName();
							}
						}
					}
				} else if (DeriveRequirement.class.isAssignableFrom(content.getClass())) {
					DeriveRequirement rel = DeriveRequirement.class.cast(content);
					boolean relationContainsFirstElement = false;
					boolean relationContainsSecondElement = false;
					for (Requirement req : rel.getDerived()) {
						if (req.hashCode() == first.hashCode()) {
							relationContainsFirstElement = true;
						} else if (req.hashCode() == second.hashCode()) {
							relationContainsSecondElement = true;
						}
					}
					for (Requirement req : rel.getDerivedFrom()) {
						if (req.hashCode() == first.hashCode()) {
							relationContainsFirstElement = true;
						} else if (req.hashCode() == second.hashCode()) {
							relationContainsSecondElement = true;
						}
					}
					if (!isRelated) {
						isRelated = relationContainsFirstElement && relationContainsSecondElement;
					}
					if (relationContainsFirstElement && relationContainsSecondElement) {
						if (traceString == "") {
							traceString = rel.eClass().getName();
						} else {
							if (!traceString.toLowerCase().contains(rel.eClass().getName())) {
								traceString += ", " + rel.eClass().getName();
							}
						}
					}
				} else if (RequirementsLink.class.isAssignableFrom(content.getClass())) {
					RequirementsLink rel = RequirementsLink.class.cast(content);
					boolean relationContainsFirstElement = false;
					boolean relationContainsSecondElement = false;
					for (Requirement req : rel.getSource()) {
						if (req.hashCode() == first.hashCode()) {
							relationContainsFirstElement = true;
						} else if (req.hashCode() == second.hashCode()) {
							relationContainsSecondElement = true;
						}
					}
					for (Requirement req : rel.getTarget()) {
						if (req.hashCode() == first.hashCode()) {
							relationContainsFirstElement = true;
						} else if (req.hashCode() == second.hashCode()) {
							relationContainsSecondElement = true;
						}
					}
					if (!isRelated) {
						isRelated = relationContainsFirstElement && relationContainsSecondElement;
					}
					if (relationContainsFirstElement && relationContainsSecondElement) {
						if (traceString == "") {
							traceString = rel.eClass().getName();
						} else {
							if (!traceString.toLowerCase().contains(rel.eClass().getName())) {
								traceString += ", " + rel.eClass().getName();
							}
						}
					}
				} else if (Transition.class.isAssignableFrom(content.getClass())) {
					Transition transition = Transition.class.cast(content);
					boolean relationContainsFirstElement = false;
					boolean relationContainsSecondElement = false;
					if (transition.getFrom().hashCode() == first.hashCode()
							|| transition.getTo().hashCode() == first.hashCode()) {
						relationContainsFirstElement = true;
					}
					if (transition.getFrom().hashCode() == second.hashCode()
							|| transition.getTo().hashCode() == second.hashCode()) {
						relationContainsSecondElement = true;
					}
					if (!isRelated) {
						isRelated = relationContainsFirstElement && relationContainsSecondElement;
					}
					if (relationContainsFirstElement && relationContainsSecondElement) {
						if (transition.getFrom().hashCode() == first.hashCode()) {
							if (traceString == "") {
								traceString = transition.eClass().getName() + " " + upArrow;
							} else {
								if (!traceString.toLowerCase().contains(transition.eClass().getName())) {
									traceString += ", " + transition.eClass().getName() + " " + upArrow;
								}
							}
						} else {
							if (traceString == "") {
								traceString = leftArrow + " " + transition.eClass().getName();
							} else {
								if (!traceString.toLowerCase().contains(transition.eClass().getName())) {
									traceString += ", " + leftArrow + " " + transition.eClass().getName();
								}
							}
						}
					}
				} else if (FaultFailurePropagationLink.class.isAssignableFrom(content.getClass())) {
					FaultFailurePropagationLink propLink = FaultFailurePropagationLink.class.cast(content);
					boolean relationContainsFirstElement = false;
					boolean relationContainsSecondElement = false;
					if (propLink.getFromPort().hashCode() == first.hashCode()
							|| propLink.getToPort().hashCode() == first.hashCode()) {
						relationContainsFirstElement = true;
					}
					if (propLink.getFromPort().hashCode() == second.hashCode()
							|| propLink.getToPort().hashCode() == second.hashCode()) {
						relationContainsSecondElement = true;
					}
					if (!isRelated) {
						isRelated = relationContainsFirstElement && relationContainsSecondElement;
					}
					if (relationContainsFirstElement && relationContainsSecondElement) {
						if (propLink.getFromPort().hashCode() == first.hashCode()) {
							if (traceString == "") {
								traceString = propLink.eClass().getName() + " " + upArrow;
							} else {
								if (!traceString.toLowerCase().contains(propLink.eClass().getName())) {
									traceString += ", " + propLink.eClass().getName() + " " + upArrow;
								}
							}
						} else {
							if (traceString == "") {
								traceString = leftArrow + " " + propLink.eClass().getName();
							} else {
								if (!traceString.toLowerCase().contains(propLink.eClass().getName())) {
									traceString += ", " + leftArrow + " " + propLink.eClass().getName();
								}
							}
						}
					}
				} else if (FunctionConnector.class.isAssignableFrom(content.getClass())) {
					FunctionConnector funcConnector = FunctionConnector.class.cast(content);
					boolean relationContainsFirstElement = false;
					boolean relationContainsSecondElement = false;
					for (FunctionConnector_port port : funcConnector.getPort()) {
						if (port.hashCode() == first.hashCode()) {
							relationContainsFirstElement = true;
						} else if (port.hashCode() == second.hashCode()) {
							relationContainsSecondElement = true;
						}
					}
					if (!isRelated) {
						isRelated = relationContainsFirstElement && relationContainsSecondElement;
					}
					if (relationContainsFirstElement && relationContainsSecondElement) {
						if (traceString == "") {
							traceString = funcConnector.eClass().getName();
						} else {
							if (!traceString.toLowerCase().contains(funcConnector.eClass().getName())) {
								traceString += ", " + funcConnector.eClass().getName();
							}
						}
					}
				} else if (HardwareConnector.class.isAssignableFrom(content.getClass())) {
					HardwareConnector hardwareConnector = HardwareConnector.class.cast(content);
					boolean relationContainsFirstElement = false;
					boolean relationContainsSecondElement = false;
					for (HardwareConnector_port port : hardwareConnector.getPort()) {
						if (port.hashCode() == first.hashCode()) {
							relationContainsFirstElement = true;
						} else if (port.hashCode() == second.hashCode()) {
							relationContainsSecondElement = true;
						}
					}
					if (!isRelated) {
						isRelated = relationContainsFirstElement && relationContainsSecondElement;
					}
					if (relationContainsFirstElement && relationContainsSecondElement) {
						if (traceString == "") {
							traceString = hardwareConnector.eClass().getName();
						} else {
							if (!traceString.toLowerCase().contains(hardwareConnector.eClass().getName())) {
								traceString += ", " + hardwareConnector.eClass().getName();
							}
						}
					}
				} else if (HardwarePortConnector.class.isAssignableFrom(content.getClass())) {
					HardwarePortConnector hardwareConnector = HardwarePortConnector.class.cast(content);
					boolean relationContainsFirstElement = false;
					boolean relationContainsSecondElement = false;
					for (HardwarePortConnector_port port : hardwareConnector.getPort()) {
						if (port.hashCode() == first.hashCode()) {
							relationContainsFirstElement = true;
						} else if (port.hashCode() == second.hashCode()) {
							relationContainsSecondElement = true;
						}
					}
					if (!isRelated) {
						isRelated = relationContainsFirstElement && relationContainsSecondElement;
					}
					if (relationContainsFirstElement && relationContainsSecondElement) {
						if (traceString == "") {
							traceString = hardwareConnector.eClass().getName();
						} else {
							if (!traceString.toLowerCase().contains(hardwareConnector.eClass().getName())) {
								traceString += ", " + hardwareConnector.eClass().getName();
							}
						}
					}
				} else if (ClampConnector.class.isAssignableFrom(content.getClass())) {
					ClampConnector clampConnector = ClampConnector.class.cast(content);
					boolean relationContainsFirstElement = false;
					boolean relationContainsSecondElement = false;
					for (ClampConnector_port port : clampConnector.getPort()) {
						if (port.hashCode() == first.hashCode()) {
							relationContainsFirstElement = true;
						} else if (port.hashCode() == second.hashCode()) {
							relationContainsSecondElement = true;
						}
					}
					if (!isRelated) {
						isRelated = relationContainsFirstElement && relationContainsSecondElement;
					}
					if (relationContainsFirstElement && relationContainsSecondElement) {
						if (traceString == "") {
							traceString = clampConnector.eClass().getName();
						} else {
							if (!traceString.toLowerCase().contains(clampConnector.eClass().getName())) {
								traceString += ", " + clampConnector.eClass().getName();
							}
						}
					}
				}
			}
			return traceString;
		}
	}
}
