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
package org.eclipse.capra.handler.app4mc;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.app4mc.amalthea.model.AccessPath;
import org.eclipse.app4mc.amalthea.model.ComplexNode;
import org.eclipse.app4mc.amalthea.model.Connector;
import org.eclipse.app4mc.amalthea.model.IAnnotatable;
import org.eclipse.app4mc.amalthea.model.QualifiedPort;
import org.eclipse.capra.core.adapters.Connection;
import org.eclipse.capra.core.handlers.AbstractArtifactHandler;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * Handler to allow tracing to and from arbitrary model elements handled by EMF.
 */
public class APP4MCHandler extends AbstractArtifactHandler<IAnnotatable> {

	@Override
	public EObject createWrapper(IAnnotatable artifact, EObject artifactModel) {
		return artifact;
	}

	@Override
	public IAnnotatable resolveWrapper(EObject wrapper) {
		return (IAnnotatable) wrapper;
	}

	@Override
	public String getDisplayName(IAnnotatable artifact) {
		return artifact.eClass().getName();
	}

	@Override
	public void addInternalLinks(EObject investigatedElement, List<Connection> allElements,
			ArrayList<Integer> duplicationCheck, List<String> selectedRelationshipTypes) {
		this.includeContainmentLinks(investigatedElement, allElements, duplicationCheck, selectedRelationshipTypes);
		if (Connector.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				Connector connector = Connector.class.cast(investigatedElement);
				QualifiedPort sourcePort = connector.getSourcePort();
				QualifiedPort targetPort = connector.getTargetPort();
				List<EObject> relatedElements = new ArrayList<>();
				relatedElements.add(sourcePort);
				relatedElements.add(targetPort);
				int connectionHash = investigatedElement.hashCode() + connector.hashCode() + sourcePort.hashCode()
						+ targetPort.hashCode();
				if (!duplicationCheck.contains(connectionHash)) {
					Connection conn = new Connection(investigatedElement, relatedElements, connector);
					allElements.add(conn);
					duplicationCheck.add(connectionHash);
				}
			}
		} else if (AccessPath.class.isAssignableFrom(investigatedElement.getClass())) {
			AccessPath path = AccessPath.class.cast(investigatedElement);
			ComplexNode source = path.getSource();
			ComplexNode target = path.getTarget();
			List<EObject> relatedElements = new ArrayList<>();
			relatedElements.add(source);
			relatedElements.add(target);
			int connectionHash = investigatedElement.hashCode() + path.hashCode() + source.hashCode()
					+ target.hashCode();
			if (!duplicationCheck.contains(connectionHash)) {
				Connection conn = new Connection(investigatedElement, relatedElements, path);
				allElements.add(conn);
				duplicationCheck.add(connectionHash);
			}
		} else {
			EObject root = EcoreUtil.getRootContainer(investigatedElement);
			TreeIterator<EObject> modelContents = root.eAllContents();
			while (modelContents.hasNext()) {
				EObject content = modelContents.next();
				if (selectedRelationshipTypes.size() == 0
						|| selectedRelationshipTypes.contains(content.eClass().getName())) {
					if (Connector.class.isAssignableFrom(content.getClass())) {
						if (selectedRelationshipTypes.size() == 0
								|| selectedRelationshipTypes.contains(content.eClass().getName())) {
							Connector connector = Connector.class.cast(content);
							QualifiedPort sourcePort = connector.getSourcePort();
							QualifiedPort targetPort = connector.getTargetPort();
							List<EObject> relatedElements = new ArrayList<>();
							boolean isConnected = false;
							if (sourcePort.hashCode() == investigatedElement.hashCode()) {
								isConnected = true;
								relatedElements.add(targetPort);
							} else if (targetPort.hashCode() == investigatedElement.hashCode()) {
								isConnected = true;
								relatedElements.add(sourcePort);
							}
							if (isConnected) {
								int connectionHash = investigatedElement.hashCode() + connector.hashCode();
								for (EObject el : relatedElements) {
									connectionHash += el.hashCode();
								}
								if (!duplicationCheck.contains(connectionHash)) {
									Connection conn = new Connection(investigatedElement, relatedElements, connector);
									allElements.add(conn);
									duplicationCheck.add(connectionHash);
								}
							}
						}
					} else if (AccessPath.class.isAssignableFrom(investigatedElement.getClass())) {
						AccessPath path = AccessPath.class.cast(investigatedElement);
						ComplexNode source = path.getSource();
						ComplexNode target = path.getTarget();
						boolean isConnected = false;
						List<EObject> relatedElements = new ArrayList<>();
						relatedElements.add(source);
						relatedElements.add(target);
						if (source.hashCode() == investigatedElement.hashCode()) {
							isConnected = true;
							relatedElements.add(target);
						} else if (target.hashCode() == investigatedElement.hashCode()) {
							isConnected = true;
							relatedElements.add(source);
						}
						if (isConnected) {
							int connectionHash = investigatedElement.hashCode() + path.hashCode();
							for (EObject el : relatedElements) {
								connectionHash += el.hashCode();
							}
							if (!duplicationCheck.contains(connectionHash)) {
								Connection conn = new Connection(investigatedElement, relatedElements, path);
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
		String traceString = "";
		if (Connector.class.isAssignableFrom(first.getClass()) || Connector.class.isAssignableFrom(second.getClass())) {
			Connector connector;
			if (Connector.class.isAssignableFrom(first.getClass())) {
				connector = Connector.class.cast(first);
			} else {
				connector = Connector.class.cast(second);
			}
			QualifiedPort sourcePort = connector.getSourcePort();
			QualifiedPort targetPort = connector.getTargetPort();
			boolean isRelatedToFirst = false;
			boolean isRelatedToSecond = false;
			if (sourcePort.hashCode() == first.hashCode() || targetPort.hashCode() == first.hashCode()) {
				isRelatedToFirst = true;
			}
			if (sourcePort.hashCode() == second.hashCode() || targetPort.hashCode() == second.hashCode()) {
				isRelatedToSecond = true;
			}
			if (isRelatedToFirst && isRelatedToSecond) {
				return "X";
			}
			return "";
		} else if (AccessPath.class.isAssignableFrom(first.getClass())
				|| Connector.class.isAssignableFrom(second.getClass())) {
			AccessPath path;
			if (AccessPath.class.isAssignableFrom(first.getClass())) {
				path = AccessPath.class.cast(first);
			} else {
				path = AccessPath.class.cast(second);
			}
			ComplexNode source = path.getSource();
			ComplexNode target = path.getTarget();
			boolean isRelatedToFirst = false;
			boolean isRelatedToSecond = false;
			if (source.hashCode() == first.hashCode() || target.hashCode() == first.hashCode()) {
				isRelatedToFirst = true;
			}
			if (source.hashCode() == second.hashCode() || target.hashCode() == second.hashCode()) {
				isRelatedToSecond = true;
			}
			if (isRelatedToFirst && isRelatedToSecond) {
				return "X";
			}
			return "";
		} else {
			EObject root = EcoreUtil.getRootContainer(first);
			TreeIterator<EObject> modelContents = root.eAllContents();
			boolean isRelated = false;
			while (modelContents.hasNext()) {
				EObject content = modelContents.next();
				if (Connector.class.isAssignableFrom(content.getClass())) {
					Connector connector = Connector.class.cast(content);
					QualifiedPort sourcePort = connector.getSourcePort();
					QualifiedPort targetPort = connector.getTargetPort();
					boolean relationContainsFirstElement = false;
					boolean relationContainsSecondElement = false;
					if (sourcePort.hashCode() == first.hashCode() || targetPort.hashCode() == first.hashCode()) {
						relationContainsFirstElement = true;
					} else if (sourcePort.hashCode() == second.hashCode()
							|| targetPort.hashCode() == second.hashCode()) {
						relationContainsSecondElement = true;
					}
					if (!isRelated) {
						isRelated = relationContainsFirstElement && relationContainsSecondElement;
					}
					if (relationContainsFirstElement && relationContainsSecondElement) {
						if (traceString == "") {
							traceString = connector.getClass().getName();
						} else {
							if (!traceString.toLowerCase().contains(connector.getClass().getName())) {
								traceString += ", " + connector.getClass().getName();
							}
						}
					}
				} else if (AccessPath.class.isAssignableFrom(content.getClass())) {
					AccessPath path = AccessPath.class.cast(content);
					ComplexNode source = path.getSource();
					ComplexNode target = path.getTarget();
					boolean relationContainsFirstElement = false;
					boolean relationContainsSecondElement = false;
					if (source.hashCode() == first.hashCode() || target.hashCode() == first.hashCode()) {
						relationContainsFirstElement = true;
					} else if (source.hashCode() == second.hashCode() || target.hashCode() == second.hashCode()) {
						relationContainsSecondElement = true;
					}
					if (!isRelated) {
						isRelated = relationContainsFirstElement && relationContainsSecondElement;
					}
					if (relationContainsFirstElement && relationContainsSecondElement) {
						if (traceString == "") {
							traceString = path.getClass().getName();
						} else {
							if (!traceString.toLowerCase().contains(path.getClass().getName())) {
								traceString += ", " + path.getClass().getName();
							}
						}
					}
				}
			}
			return traceString;
		}

	}
}
