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
import org.eclipse.capra.core.helpers.EMFHelper;
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
			ArrayList<String> duplicationCheck, List<String> selectedRelationshipTypes) {
		if (Connector.class.isAssignableFrom(investigatedElement.getClass())) {
			if (selectedRelationshipTypes.size() == 0
					|| selectedRelationshipTypes.contains(investigatedElement.eClass().getName())) {
				Connector connector = Connector.class.cast(investigatedElement);
				QualifiedPort sourcePort = connector.getSourcePort();
				QualifiedPort targetPort = connector.getTargetPort();
				List<EObject> relatedElements = new ArrayList<>();
				relatedElements.add(sourcePort);
				relatedElements.add(targetPort);
				if (!isDuplicatedEntry(investigatedElement, relatedElements, connector, duplicationCheck)) {
					Connection conn = new Connection(investigatedElement, relatedElements, connector);
					allElements.add(conn);
					addPotentialStringsForConnection(investigatedElement, relatedElements, connector, duplicationCheck);
				}
			}
		} else if (AccessPath.class.isAssignableFrom(investigatedElement.getClass())) {
			AccessPath path = AccessPath.class.cast(investigatedElement);
			ComplexNode source = path.getSource();
			ComplexNode target = path.getTarget();
			List<EObject> relatedElements = new ArrayList<>();
			relatedElements.add(source);
			relatedElements.add(target);
			if (!isDuplicatedEntry(investigatedElement, relatedElements, path, duplicationCheck)) {
				Connection conn = new Connection(investigatedElement, relatedElements, path);
				allElements.add(conn);
				addPotentialStringsForConnection(investigatedElement, relatedElements, path, duplicationCheck);
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
							if (EMFHelper.getNameAttribute(investigatedElement)
									.equals(EMFHelper.getNameAttribute(sourcePort))) {
								isConnected = true;
								relatedElements.add(targetPort);
							} else if (EMFHelper.getNameAttribute(investigatedElement)
									.equals(EMFHelper.getNameAttribute(targetPort))) {
								isConnected = true;
								relatedElements.add(sourcePort);
							}
							if (isConnected) {
								if (!isDuplicatedEntry(investigatedElement, relatedElements, connector,
										duplicationCheck)) {
									Connection conn = new Connection(investigatedElement, relatedElements, connector);
									allElements.add(conn);
									addPotentialStringsForConnection(investigatedElement, relatedElements, connector,
											duplicationCheck);
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
						if (EMFHelper.getNameAttribute(investigatedElement)
								.equals(EMFHelper.getNameAttribute(source))) {
							isConnected = true;
							relatedElements.add(target);
						} else if (EMFHelper.getNameAttribute(investigatedElement)
								.equals(EMFHelper.getNameAttribute(target))) {
							isConnected = true;
							relatedElements.add(source);
						}
						if (isConnected) {
							if (!isDuplicatedEntry(investigatedElement, relatedElements, path, duplicationCheck)) {
								Connection conn = new Connection(investigatedElement, relatedElements, path);
								allElements.add(conn);
								addPotentialStringsForConnection(investigatedElement, relatedElements, path,
										duplicationCheck);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Adds all possible relations-ship strings as a concatenated string for a
	 * relation between two elements
	 * 
	 * @param source
	 *            The source element of the relation
	 * @param targets
	 *            The target element of the relation
	 * @param relation
	 *            The relation between the elements
	 * @param duplicationCheck
	 *            The list of strings the potential relation-ship strings are
	 *            added to
	 */
	private static void addPotentialStringsForConnection(EObject source, List<EObject> targets, EObject relation,
			List<String> duplicationCheck) {
		String potentialString = EMFHelper.getNameAttribute(source);
		for (EObject target : targets) {
			potentialString += EMFHelper.getNameAttribute(target);
		}
		potentialString += EMFHelper.getNameAttribute(relation);
		duplicationCheck.add(potentialString);

		potentialString = "";
		for (EObject target : targets) {
			potentialString += EMFHelper.getNameAttribute(target);
		}
		potentialString += EMFHelper.getNameAttribute(source);
		potentialString += EMFHelper.getNameAttribute(relation);
		duplicationCheck.add(potentialString);
	}

	/**
	 * Checks if a connection for a relation between two elements already exists
	 * 
	 * @param source
	 *            The source element of the relation
	 * @param targets
	 *            The target element of the relation
	 * @param relation
	 *            The relation between the elements
	 * @param duplicationCheck
	 *            List of concatenated strings containing all possible
	 *            combinations for each relationship added to the tracemodel
	 * @return
	 */
	private static boolean isDuplicatedEntry(EObject source, List<EObject> targets, EObject relation,
			List<String> duplicationCheck) {
		String connectionString = EMFHelper.getNameAttribute(source);
		for (EObject target : targets) {
			connectionString += EMFHelper.getNameAttribute(target);
		}
		connectionString += EMFHelper.getNameAttribute(relation);
		return duplicationCheck.contains(connectionString);
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
			String firstElementName = EMFHelper.getNameAttribute(first);
			String secondElementName = EMFHelper.getNameAttribute(second);
			boolean isRelatedToFirst = false;
			boolean isRelatedToSecond = false;
			String sourcePortName = EMFHelper.getNameAttribute(sourcePort);
			String targetPortName = EMFHelper.getNameAttribute(targetPort);
			if (sourcePortName.equals(firstElementName) || targetPortName.equals(firstElementName)) {
				isRelatedToFirst = true;
			}
			if (sourcePortName.equals(secondElementName) || targetPortName.equals(secondElementName)) {
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
			String firstElementName = EMFHelper.getNameAttribute(first);
			String secondElementName = EMFHelper.getNameAttribute(second);
			boolean isRelatedToFirst = false;
			boolean isRelatedToSecond = false;
			String sourcePortName = EMFHelper.getNameAttribute(source);
			String targetPortName = EMFHelper.getNameAttribute(target);
			if (sourcePortName.equals(firstElementName) || targetPortName.equals(firstElementName)) {
				isRelatedToFirst = true;
			}
			if (sourcePortName.equals(secondElementName) || targetPortName.equals(secondElementName)) {
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
					String firstElementName = EMFHelper.getNameAttribute(first);
					String secondElementName = EMFHelper.getNameAttribute(second);
					String sourcePortName = EMFHelper.getNameAttribute(sourcePort);
					String targetPortName = EMFHelper.getNameAttribute(targetPort);
					boolean relationContainsFirstElement = false;
					boolean relationContainsSecondElement = false;
					if (sourcePortName.equals(firstElementName) || targetPortName.equals(firstElementName)) {
						relationContainsFirstElement = true;
					} else if (sourcePortName.equals(secondElementName) || targetPortName.equals(secondElementName)) {
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
					String firstElementName = EMFHelper.getNameAttribute(first);
					String secondElementName = EMFHelper.getNameAttribute(second);
					String sourcePortName = EMFHelper.getNameAttribute(source);
					String targetPortName = EMFHelper.getNameAttribute(target);
					boolean relationContainsFirstElement = false;
					boolean relationContainsSecondElement = false;
					if (sourcePortName.equals(firstElementName) || targetPortName.equals(firstElementName)) {
						relationContainsFirstElement = true;
					} else if (sourcePortName.equals(secondElementName) || targetPortName.equals(secondElementName)) {
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
