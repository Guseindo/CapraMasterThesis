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
package org.eclipse.capra.handler.muml;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capra.core.adapters.Connection;
import org.eclipse.capra.core.handlers.AbstractArtifactHandler;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.muml.core.ExtendableElement;
import org.muml.pim.connector.Connector;
import org.muml.pim.connector.ConnectorEndpoint;
import org.muml.pim.realtimestatechart.Transition;

/**
 * Handler to allow tracing to and from arbitrary model elements handled by EMF.
 */
public class MUMLHandler extends AbstractArtifactHandler<ExtendableElement> {

	@Override
	public EObject createWrapper(ExtendableElement artifact, EObject artifactModel) {
		return artifact;
	}

	@Override
	public ExtendableElement resolveWrapper(EObject wrapper) {
		return (ExtendableElement) wrapper;
	}

	@Override
	public String getDisplayName(ExtendableElement artifact) {
		return artifact.eClass().getName();
	}

	@Override
	public void addInternalLinks(EObject investigatedElement, List<Connection> allElements,
			ArrayList<Integer> duplicationCheck, List<String> selectedRelationshipTypes) {
		if (Transition.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				Transition transition = Transition.class.cast(investigatedElement);
				List<EObject> relatedElements = new ArrayList<>();
				relatedElements.add(transition.getSource());
				relatedElements.add(transition.getTarget());
				int connectionHash = investigatedElement.hashCode() + transition.hashCode()
						+ transition.getTarget().hashCode() + transition.getSource().hashCode();
				if (!duplicationCheck.contains(connectionHash)) {
					Connection conn = new Connection(investigatedElement, relatedElements, transition);
					allElements.add(conn);
					duplicationCheck.add(connectionHash);
				}
			}
		} else if (Connector.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				Connector connector = Connector.class.cast(investigatedElement);
				EList<ConnectorEndpoint> connectedEnds = connector.getConnectorEndpoints();
				List<EObject> relatedElements = new ArrayList<>();
				connectedEnds.forEach(connectedEnd -> {
					relatedElements.add(connectedEnd);
				});
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
		} else {
			EObject root = EcoreUtil.getRootContainer(investigatedElement);
			TreeIterator<EObject> modelContents = root.eAllContents();
			while (modelContents.hasNext()) {
				EObject content = modelContents.next();
				if (selectedRelationshipTypes.size() == 0
						|| selectedRelationshipTypes.contains(content.eClass().getName())) {
					if (Transition.class.isAssignableFrom(content.getClass())) {
						Transition transition = Transition.class.cast(content);
						List<EObject> relatedElements = new ArrayList<>();
						if (transition.getSource().hashCode() == investigatedElement.hashCode()) {
							relatedElements.add(transition.getTarget());
							int connectionHash = investigatedElement.hashCode() + transition.hashCode()
									+ transition.getTarget().hashCode();
							if (!duplicationCheck.contains(connectionHash)) {
								Connection conn = new Connection(investigatedElement, relatedElements, transition);
								allElements.add(conn);
								duplicationCheck.add(connectionHash);
							}
						} else if (transition.getTarget().hashCode() == investigatedElement.hashCode()) {
							relatedElements.add(transition.getSource());
							int connectionHash = investigatedElement.hashCode() + transition.hashCode()
									+ transition.getSource().hashCode();
							if (!duplicationCheck.contains(connectionHash)) {
								Connection conn = new Connection(investigatedElement, relatedElements, transition);
								allElements.add(conn);
								duplicationCheck.add(connectionHash);
							}
							Connection conn = new Connection(investigatedElement, relatedElements, transition);
							allElements.add(conn);
						}
					} else if (Connector.class.isAssignableFrom(content.getClass())) {
						if (selectedRelationshipTypes.size() == 0
								|| selectedRelationshipTypes.contains(content.eClass().getName())) {
							Connector connector = Connector.class.cast(content);
							EList<ConnectorEndpoint> connectedEnds = connector.getConnectorEndpoints();
							List<EObject> relatedElements = new ArrayList<>();
							boolean isConnected = false;
							for (ConnectorEndpoint connectedEnd : connectedEnds) {
								relatedElements.add(connectedEnd);
								if (connectedEnd.hashCode() == investigatedElement.hashCode()) {
									isConnected = true;
									relatedElements.remove(connectedEnd);
								}
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
					}
				}
			}
		}
	}

	@Override
	public String isThereAnInternalTraceBetween(EObject first, EObject second, EObject traceModel) {
		String traceString = "";
		if (Transition.class.isAssignableFrom(first.getClass())
				|| Transition.class.isAssignableFrom(second.getClass())) {
			Transition transition;
			if (Transition.class.isAssignableFrom(first.getClass())) {
				transition = Transition.class.cast(first);
			} else {
				transition = Transition.class.cast(second);
			}
			int sourceHash = transition.getSource().hashCode();
			int targetHash = transition.getTarget().hashCode();
			boolean relationContainsFirstElement = sourceHash == first.hashCode() || targetHash == first.hashCode();
			boolean relationContainsSecondElement = sourceHash == second.hashCode() || targetHash == second.hashCode();
			if (relationContainsFirstElement && relationContainsSecondElement) {
				return "X";
			}
			return "";
		} else if (Connector.class.isAssignableFrom(first.getClass())
				|| Connector.class.isAssignableFrom(second.getClass())) {
			Connector connector;
			if (Connector.class.isAssignableFrom(first.getClass())) {
				connector = Connector.class.cast(first);
			} else {
				connector = Connector.class.cast(second);
			}
			EList<ConnectorEndpoint> connectedEnds = connector.getConnectorEndpoints();
			boolean isRelated = false;
			for (ConnectorEndpoint connectedEnd : connectedEnds) {
				if (connectedEnd.hashCode() == first.hashCode() || connectedEnd.hashCode() == second.hashCode()) {
					isRelated = true;
				}
			}
			if (isRelated) {
				return "X";
			}
			return "";
		} else {
			EObject root = EcoreUtil.getRootContainer(first);
			TreeIterator<EObject> modelContents = root.eAllContents();
			boolean isRelated = false;
			String leftArrow = Character.toString((char) 0x2190);
			String upArrow = Character.toString((char) 0x2191);
			while (modelContents.hasNext()) {
				EObject content = modelContents.next();
				if (Transition.class.isAssignableFrom(content.getClass())) {
					Transition transition = Transition.class.cast(content);
					int sourceHash = transition.getSource().hashCode();
					int targetHash = transition.getTarget().hashCode();
					boolean relationContainsFirstElement = sourceHash == first.hashCode()
							|| targetHash == first.hashCode();
					boolean relationContainsSecondElement = sourceHash == second.hashCode()
							|| targetHash == second.hashCode();
					if (!isRelated) {
						isRelated = relationContainsFirstElement && relationContainsSecondElement;
					}
					if (relationContainsFirstElement && relationContainsSecondElement) {
						if (sourceHash == first.hashCode()) {
							if (traceString == "") {
								traceString = transition.eClass().getName() + upArrow;
							} else {
								if (!traceString.toLowerCase().contains(transition.eClass().getName() + upArrow)) {
									traceString += ", " + transition.eClass().getName() + upArrow;
								}
							}
						} else {
							if (traceString == "") {
								traceString = leftArrow + transition.eClass().getName();
							} else {
								if (!traceString.toLowerCase().contains(leftArrow + transition.eClass().getName())) {
									traceString += ", " + leftArrow + transition.eClass().getName();
								}
							}
						}
					}
				} else if (Connector.class.isAssignableFrom(content.getClass())) {
					Connector connector = Connector.class.cast(content);
					EList<ConnectorEndpoint> connectedEnds = connector.getConnectorEndpoints();
					boolean relationContainsFirstElement = false;
					boolean relationContainsSecondElement = false;
					for (ConnectorEndpoint connectedEnd : connectedEnds) {
						if (connectedEnd.hashCode() == first.hashCode()) {
							relationContainsFirstElement = true;
						} else if (connectedEnd.hashCode() == second.hashCode()) {
							relationContainsSecondElement = true;
						}
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
				}
			}
			return traceString;
		}

	}
}
