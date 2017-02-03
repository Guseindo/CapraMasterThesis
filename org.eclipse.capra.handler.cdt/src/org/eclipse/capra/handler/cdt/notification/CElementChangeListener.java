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
package org.eclipse.capra.handler.cdt.notification;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.capra.GenericArtifactMetaModel.ArtifactWrapper;
import org.eclipse.capra.GenericArtifactMetaModel.ArtifactWrapperContainer;
import org.eclipse.capra.core.adapters.TracePersistenceAdapter;
import org.eclipse.capra.core.helpers.ExtensionPointHelper;
import org.eclipse.capra.handler.cdt.CDTHandler;
import org.eclipse.capra.ui.notification.CapraNotificationHelper;
import org.eclipse.capra.ui.notification.CapraNotificationHelper.IssueType;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * Checks for changes of C/C++ elements to determine if they affect the trace
 * model. Creates markers on the artifact model if the changes affect artifact
 * wrappers.
 * 
 * @author Dusan Kalanj
 */
public class CElementChangeListener implements IElementChangedListener {

	@Override
	public void elementChanged(ElementChangedEvent event) {

		TracePersistenceAdapter tracePersistenceAdapter = ExtensionPointHelper.getTracePersistenceAdapter().get();
		EObject awc = tracePersistenceAdapter.getArtifactWrappers(new ResourceSetImpl());
		List<ArtifactWrapper> cArtifacts = ((ArtifactWrapperContainer) awc).getArtifacts().stream()
				.filter(p -> p.getArtifactHandler().equals(CDTHandler.class.getName())).collect(Collectors.toList());

		if (cArtifacts.size() == 0)
			return;

		IPath path = new Path(EcoreUtil.getURI(awc).toPlatformString(false));
		IFile wrapperContainer = ResourcesPlugin.getWorkspace().getRoot().getFile(path);

		new WorkspaceJob(CapraNotificationHelper.NOTIFICATION_JOB) {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				visit(event.getDelta(), cArtifacts, wrapperContainer);
				return Status.OK_STATUS;
			}
		}.schedule();

	}

	private void visit(ICElementDelta delta, List<ArtifactWrapper> cArtifacts, IFile wrapperContainer) {

		// Visit all the affected children of affected element
		for (ICElementDelta subDelta : delta.getAffectedChildren())
			visit(subDelta, cArtifacts, wrapperContainer);

		int flags = delta.getFlags();
		int changeType = delta.getKind();
		IssueType issueType = null;

		// TODO doesn't work if a source folder is renamed - it says that the
		// children have been deleted, instead of that their ancestor was
		// renamed. But renaming the src folder isn't good practice anyway.
		if (changeType == ICElementDelta.REMOVED) {

			if ((flags & ICElementDelta.F_MOVED_TO) != 0)
				if (delta.getMovedToElement().getElementName().equals(delta.getElement().getElementName()))
					issueType = IssueType.MOVED;
				else
					issueType = IssueType.RENAMED;
			else
				issueType = IssueType.DELETED;

		} else if (changeType == ICElementDelta.CHANGED)
			issueType = IssueType.CHANGED;

		if (issueType != null) {
			String affectedElementUri = delta.getElement().getHandleIdentifier();

			for (ArtifactWrapper aw : cArtifacts) {
				String artifactUri = aw.getUri();

				// If the change type is "CHANGED", meaning that the element
				// wasn't deleted, renamed, added or moved, only consider making
				// a marker if the URI of the affected element is the same as
				// the URI in the wrapper.
				if (changeType == ICElementDelta.CHANGED && !artifactUri.contentEquals(affectedElementUri))
					continue;
				// Otherwise (the change is either "delete", "move" or
				// "rename"), consider making the marker for the affected
				// element as well as its children, who's URIs have changed and
				// need updating.
				else if (artifactUri.contains(affectedElementUri)) {
					HashMap<String, String> markerInfo = generateMarkerInfo(aw, delta, issueType);

					if (markerInfo != null)
						CapraNotificationHelper.createCapraMarker(markerInfo, wrapperContainer);
				}
			}
		}
	}

	/**
	 * Generates the attributes that will later be assigned (in the createMarker
	 * method in the CapraNotificationHelper) to a Capra change marker.
	 * 
	 * @param aw
	 *            ArtifactWrapper that links to the element in the delta or to a
	 *            child of the element in the delta
	 * @param delta
	 *            represents changes in the state of a C/C++ element
	 * @param issueType
	 *            the type of change that occurred
	 * @return a key value HashMap, containing the attributes to be assigned to
	 *         a Capra change marker and their keys (IDs).
	 */
	private HashMap<String, String> generateMarkerInfo(ArtifactWrapper aw, ICElementDelta delta, IssueType issueType) {
		HashMap<String, String> markerInfo = new HashMap<String, String>();

		// Properties from the C/C++ element in the wrapper (all elements)
		String oldArtifactUri = aw.getUri();
		String oldArtifactName = aw.getName();

		// Properties from the affected C/C++ element before the change.
		ICElement oldAffectedElement = delta.getElement();
		String oldAffectedElementUri = oldAffectedElement.getHandleIdentifier();
		String oldAffectedElementName = oldAffectedElement.getElementName();

		// Properties to be assigned to the marker so that they can be later
		// used in quick fix solutions.
		String newAffectedElementUri = null;
		String newAffectedElementName = null;

		// Affected element in it's new state, if it has been renamed or moved,
		// otherwise null.
		ICElement newAffectedElement = delta.getMovedToElement();
		if (newAffectedElement != null) {
			newAffectedElementUri = newAffectedElement.getHandleIdentifier();
			newAffectedElementName = newAffectedElement.getElementName();
		}

		// Generate a message for the marker based on the change.
		// TODO Make messages more readable (replaces uris with names?).
		String message = "";
		switch (issueType) {
		case RENAMED:
			if (oldArtifactUri.equals(oldAffectedElementUri))
				// The element in the wrapper is the renamed element.
				message = oldAffectedElementUri + " has been renamed to " + newAffectedElementUri + ".";
			else
				// The element in the wrapper is a child of the renamed element.
				message = oldAffectedElementName + ", an ancestor of " + oldArtifactUri + ", has been renamed to "
						+ newAffectedElementName + ".";
			break;
		case MOVED:
			if (oldArtifactUri.equals(oldAffectedElementUri))
				// The element in the wrapper is the moved element.
				message = oldAffectedElementUri + " has been moved to " + newAffectedElementUri + ".";
			else
				// The element in the wrapper is a child of the moved element.
				message = oldAffectedElementName + ", an ancestor of " + oldArtifactUri + ", has been moved to "
						+ newAffectedElementUri;
			break;
		case DELETED:
			message = aw.getUri() + " has been deleted or has had its signature changed.";
			break;
		case CHANGED:
			message = aw.getUri() + " has been edited. Please check if associated trace links are still valid.";
			break;
		}

		if (message.isEmpty())
			return null;

		if (newAffectedElementUri != null) {
			// The affected element has been renamed or moved.
			String newArtifactUri;
			String newArtifactName;

			if (oldArtifactUri.equals(oldAffectedElementUri)) {
				// The element in the wrapper is the affected element.
				newArtifactUri = newAffectedElementUri;
				newArtifactName = newAffectedElementName;
			} else {
				// The element in the wrapper is a child of the affected
				// element. Update the relevant part of the URI.
				newArtifactUri = oldArtifactUri.replace(oldAffectedElementUri, newAffectedElementUri);
				newArtifactName = oldArtifactName;
			}

			markerInfo.put(CapraNotificationHelper.NEW_URI, newArtifactUri);
			markerInfo.put(CapraNotificationHelper.NEW_NAME, newArtifactName);
		}

		markerInfo.put(CapraNotificationHelper.ISSUE_TYPE, issueType.getValue());
		markerInfo.put(CapraNotificationHelper.OLD_URI, oldArtifactUri);
		markerInfo.put(CapraNotificationHelper.MESSAGE, message);

		return markerInfo;
	}
}
