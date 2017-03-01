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
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.eclipse.capra.GenericTraceMetaModel.GenericTraceMetaModelFactory;
import org.eclipse.capra.GenericTraceMetaModel.GenericTraceMetaModelPackage;
import org.eclipse.capra.GenericTraceMetaModel.GenericTraceModel;
import org.eclipse.capra.GenericTraceMetaModel.RelatedTo;
import org.eclipse.capra.core.adapters.Connection;
import org.eclipse.capra.core.adapters.TraceMetaModelAdapter;
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
		List<EObject> tempList = new ArrayList<>();
		
		allElements.addAll(directElements);
		
		tempList.add(element);
		directElements.forEach(dE -> tempList.addAll(dE.getTargets()));
		
		for(EObject o : tempList){
			if(objectIsOfUML2Package(o)){
				EObject root = EcoreUtil.getRootContainer(o);
				TreeIterator<EObject> modelContents = root.eAllContents();				
				while(modelContents.hasNext()){
					EObject content = modelContents.next();
					/*System.out.println("Name: "+getNameAttribute(content, content.eClass().getEAllAttributes()));
					System.out.println("EClass: "+content.eClass().getName());
					System.out.println("EClass: "+content.getClass().toString());
					System.out.println("IsRelationship: "+Relationship.class.isAssignableFrom(content.getClass()));*/
					
					if(Generalization.class.isAssignableFrom(content.getClass())){
						Generalization generalization = Generalization.class.cast(content);
						generalization.getOwner();
					}
					
					if(Relationship.class.isAssignableFrom(content.getClass())){
						Relationship relation = Relationship.class.cast(content);
						boolean isRelatedToElement = false;
						List<EObject> relatedElements = new ArrayList<>();
						for(Element relatedElement : relation.getRelatedElements()){
							if(getNameAttribute(relatedElement, relatedElement.eClass().getEAllAttributes()).equals(getNameAttribute(o, o.eClass().getEAllAttributes()))){
								isRelatedToElement = true;
							} else {
								relatedElements.add(relatedElement);
							}
						}
						if(isRelatedToElement){
							allElements.add(new Connection(o, relatedElements, relation));
						}
					}
					
					if(Transition.class.isAssignableFrom(content.getClass())){
						Transition transition = Transition.class.cast(content);
						List<EObject> relatedElements = new ArrayList<>();
						if(getNameAttribute(transition.getSource(), transition.getSource().eClass()
								.getEAllAttributes()).equals(getNameAttribute(o, o.eClass().getEAllAttributes()))
						){
							relatedElements.add(transition.getTarget());
							allElements.add(new Connection(o, relatedElements, transition));
						} else if (getNameAttribute(transition.getTarget(), transition.getTarget().eClass()
										.getEAllAttributes()).equals(getNameAttribute(o, o.eClass().getEAllAttributes()))){
							relatedElements.add(transition.getSource());
							allElements.add(new Connection(o, relatedElements, transition));
						}
					}
				}
			}
		}
		
		return allElements;
	}
	
	private boolean objectIsOfUML2Package(EObject obj){
		return obj.getClass().getPackage().getName().equals("org.eclipse.uml2.uml.internal.impl");
	}
	
	private static String getNameAttribute(final EObject eObject, final List<EAttribute> attributes) {
		String name = "";
		for (EAttribute feature : attributes) {
			if (feature.getName().equals("name")) {
				Object obj = eObject.eGet(feature);
				if (obj != null) {
					name = obj.toString();
				}
			}
		}
		return name;
	}
}
