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
package org.eclipse.capra.generic.tracemodels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.management.relation.Relation;

import org.eclipse.capra.GenericTraceMetaModel.GenericTraceMetaModelFactory;
import org.eclipse.capra.GenericTraceMetaModel.GenericTraceMetaModelPackage;
import org.eclipse.capra.GenericTraceMetaModel.GenericTraceModel;
import org.eclipse.capra.GenericTraceMetaModel.RelatedTo;
import org.eclipse.capra.core.adapters.Connection;
import org.eclipse.capra.core.adapters.TraceMetaModelAdapter;
import org.eclipse.capra.core.helpers.EMFHelper;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Relationship;
import org.eclipse.uml2.uml.Transition;

import com.google.common.base.Predicate;

/**
 * Provides generic functionality to deal with traceability meta models.
 */
public class GenericMetaModelAdapter implements TraceMetaModelAdapter {

	public GenericMetaModelAdapter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public EObject createModel() {
		return GenericTraceMetaModelFactory.eINSTANCE.createGenericTraceModel();
	}

	@Override
	public Collection<EClass> getAvailableTraceTypes(List<EObject> selection) {
		Collection<EClass> traceTypes = new ArrayList<>();
		if (selection.size() > 1) {

			traceTypes.add(GenericTraceMetaModelPackage.eINSTANCE.getRelatedTo());
		}
		return traceTypes;
	}

	@Override
	public EObject createTrace(EClass traceType, EObject traceModel, List<EObject> selection) {
		GenericTraceModel TM = (GenericTraceModel) traceModel;
		EObject trace = GenericTraceMetaModelFactory.eINSTANCE.create(traceType);
		RelatedTo RelatedToTrace = (RelatedTo) trace;
		RelatedToTrace.getItem().addAll(selection);

		TM.getTraces().add(RelatedToTrace);
		return TM;
	}

	@Override
	public void deleteTrace(EObject first, EObject second, EObject traceModel) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isThereATraceBetween(EObject firstElement, EObject secondElement, EObject traceModel) {
		GenericTraceModel root = (GenericTraceModel) traceModel;
		List<RelatedTo> relevantLinks = new ArrayList<RelatedTo>();
		List<RelatedTo> allTraces = root.getTraces();

		for (RelatedTo trace : allTraces) {
			if (firstElement != secondElement) {
				if (trace.getItem().contains(firstElement) && trace.getItem().contains(secondElement)) {
					relevantLinks.add(trace);
				}
			}
		}
		if (relevantLinks.size() > 0) {
			return true;
		} else
			return false;
	}

	@Override
	public List<Connection> getConnectedElements(EObject element, EObject tracemodel) {
		GenericTraceModel root = (GenericTraceModel) tracemodel;
		List<Connection> connections = new ArrayList<>();
		List<RelatedTo> traces = root.getTraces();

		if (element instanceof RelatedTo) {
			RelatedTo trace = (RelatedTo) element;
			connections.add(new Connection(element, trace.getItem(), trace));
		} else {

			for (RelatedTo trace : traces) {
				if (trace.getItem().contains(element)) {
					connections.add(new Connection(element, trace.getItem(), trace));
				}
			}
		}
		return connections;
	}

	private List<Connection> getTransitivelyConnectedElements(EObject element, EObject traceModel,
			List<Object> accumulator) {
		List<Connection> directElements = getConnectedElements(element, traceModel);
		List<Connection> allElements = new ArrayList<>();

		directElements.forEach(connection -> {
			if (!accumulator.contains(connection.getTlink())) {
				allElements.add(connection);
				accumulator.add(connection.getTlink());
				connection.getTargets().forEach(e -> {
					allElements.addAll(getTransitivelyConnectedElements(e, traceModel, accumulator));
				});
			}
		});

		return allElements;
	}

	@Override
	public List<Connection> getTransitivelyConnectedElements(EObject element, EObject traceModel) {
		List<Object> accumulator = new ArrayList<>();
		return getTransitivelyConnectedElements(element, traceModel, accumulator);
	}

	@Override
	public List<Connection> getInternalElements(EObject element, EObject traceModel) {
		List<Connection> directElements = getConnectedElements(element, traceModel);
		List<Connection> allElements = new ArrayList<>();
		HashMap<String, Connection> duplicationCheck = new HashMap<>();
		allElements.addAll(directElements);
		
		for(Connection conn : directElements){
			for(EObject o : conn.getTargets()){
				addConnectionsForRelations(o, allElements, duplicationCheck);
			}
		}
		addConnectionsForRelations(element, allElements, duplicationCheck);
		
		return allElements;
	}
	
	private static boolean objectIsOfUML2Package(EObject obj){
		return obj.getClass().getPackage().getName().equals("org.eclipse.uml2.uml.internal.impl");
	}
	
	
	
	private static void addConnectionsForRelations(EObject o, List<Connection>allElements, HashMap<String, Connection> duplicationCheck){
		if(objectIsOfUML2Package(o)){
			if(Relationship.class.isAssignableFrom(o.getClass())){
				Relationship rel = Relationship.class.cast(o);
				List<EObject> relatedElements = new ArrayList<>();
				rel.getRelatedElements().forEach(element -> relatedElements.add(element));
				Connection conn = new Connection(o, relatedElements, rel);
				allElements.add(conn);
			} else if(Transition.class.isAssignableFrom(o.getClass())){
				Transition transition = Transition.class.cast(o);
				List<EObject> relatedElements = new ArrayList<>();
				relatedElements.add(transition.getSource());
				relatedElements.add(transition.getTarget());
				Connection conn = new Connection(o, relatedElements, transition);
				allElements.add(conn);
			} else {
				EObject root = EcoreUtil.getRootContainer(o);
				TreeIterator<EObject> modelContents = root.eAllContents();				
				while(modelContents.hasNext()){
					EObject content = modelContents.next();
					/*System.out.println("Name: "+getNameAttribute(content));
					System.out.println("EClass: "+content.eClass().getName());
					System.out.println("EClass: "+content.getClass().toString());
					System.out.println("IsRelationship: "+Relationship.class.isAssignableFrom(content.getClass()));*/
					
					if(Relationship.class.isAssignableFrom(content.getClass())){
						Relationship relation = Relationship.class.cast(content);
						boolean isRelatedToElement = false;
						List<EObject> relatedElements = new ArrayList<>();
						for(Element relatedElement : relation.getRelatedElements()){
							if(EMFHelper.getNameAttribute(relatedElement).equals(EMFHelper.getNameAttribute(o))){
								isRelatedToElement = true;
							} else {
								relatedElements.add(relatedElement);
							}
						}
						if(isRelatedToElement){
							String hash = getHashForConnection(o, relatedElements, relation);
							if(!duplicationCheck.containsKey(hash)){
								Connection conn = new Connection(o, relatedElements, relation);
								allElements.add(conn);
								duplicationCheck.put(hash, conn);
							}
						}
					}
					
					if(Transition.class.isAssignableFrom(content.getClass())){
						Transition transition = Transition.class.cast(content);
						List<EObject> relatedElements = new ArrayList<>();
						if(EMFHelper.getNameAttribute(transition.getSource()).equals(EMFHelper.getNameAttribute(o))
						){
							relatedElements.add(transition.getTarget());
							String hash = getHashForConnection(o, relatedElements, transition);
							if(!duplicationCheck.containsKey(hash)){
								Connection conn = new Connection(o, relatedElements, transition);
								allElements.add(conn);
								duplicationCheck.put(hash, conn);
							}
						} else if (EMFHelper.getNameAttribute(transition.getTarget()).equals(EMFHelper.getNameAttribute(o))){
							relatedElements.add(transition.getSource());
							String hash = getHashForConnection(o, relatedElements, transition);
							if(!duplicationCheck.containsKey(hash)){
								Connection conn = new Connection(o, relatedElements, transition);
								allElements.add(conn);
								duplicationCheck.put(hash, conn);
							}
						}
					}
				}
			}
		}
	}
	
	private static String getHashForConnection(EObject source, List<EObject> targets, EObject relation){
	   String md5 = EMFHelper.getNameAttribute(source);
	   for(EObject target : targets){
		   md5 += EMFHelper.getNameAttribute(target);
	   }
	   md5 += EMFHelper.getNameAttribute(relation);
	   try {
	        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
	        byte[] array = md.digest(md5.getBytes());
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < array.length; ++i) {
	          sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
	       }
	        return sb.toString();
	    } catch (java.security.NoSuchAlgorithmException e) {
	    }
	    return null;
	}
}
