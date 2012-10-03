package edu.cmu.lti.oaqa.bio.annotate.umls;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.Sets;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.impls.rexster.RexsterGraph;

import edu.cmu.lti.oaqa.bio.annotate.graph.Concept;
import edu.cmu.lti.oaqa.bio.annotate.graph.ConceptBundle;
import edu.cmu.lti.oaqa.bio.annotate.graph.Relationship;
import edu.cmu.lti.oaqa.graph.core.tools.Search;
import edu.cmu.lti.oaqa.graph.core.tools.Tools;
import edu.cmu.lti.oaqa.graph.core.tools.Traversals;

public class GraphQueryEngine {

	RexsterGraph graph;
	
	public GraphQueryEngine(){
		//this.graph = new RexsterGraph("http://peace.isri.cs.cmu.edu:8182/graphs/UMLSGraph");
		this.graph = new RexsterGraph("http://peace.isri.cs.cmu.edu:8182/graphs/umls");
	}
	
	public ConceptBundle search(String searchString){
		Iterator<Vertex> atomItty = graph.getVertices("exactString", searchString).iterator();
		if(!atomItty.hasNext()) return null;
		UMLSConcept concept = new UMLSConcept(atomItty.next().query().labels("atom").direction(Direction.IN).vertices().iterator().next());
		List<Concept> conceptList = Lists.newArrayList((Concept)concept);
		return new ConceptBundle(conceptList,searchString.toLowerCase());
	}
	
	public ConceptBundle search(String searchString, int maxResults){
		if(maxResults==1) return search(searchString);
		Set<Concept> concepts = Sets.newHashSet();
		int count=0;
		Iterator<Vertex> searchResults = graph.getVertices("exactString", searchString).iterator();
		while(searchResults.hasNext() && count<maxResults){
			count++;
			Vertex result=searchResults.next();
			Vertex conceptVertex = result.query().labels("atom").direction(Direction.IN).vertices().iterator().next();
			concepts.add(new UMLSConcept(conceptVertex));
		}	
		return new ConceptBundle(Lists.newArrayList(concepts),searchString.toLowerCase());
	}
	
	public static List<Relationship> getSemanticNetworkRelationships(Concept c1, Concept c2){
		List<Vertex> c1TypeVertices = new ArrayList<Vertex>();
		for(Relationship rel:c1.getTypeOfs()){
			UMLSConcept concept = (UMLSConcept) rel.getToConcept();
			c1TypeVertices.add(concept.getRawVertex());
		}
		List<Vertex> c2TypeVertices = new ArrayList<Vertex>();
		for(Relationship rel:c2.getTypeOfs()){
			UMLSConcept concept = (UMLSConcept) rel.getToConcept();
			c2TypeVertices.add(concept.getRawVertex());
		}
		List<Edge> SNEdges = new ArrayList<Edge>();
		for(Vertex c1Type:c1TypeVertices){
			for(Vertex c2Type:c2TypeVertices){
				for(Edge e: getEdges(c1Type,c2Type)){
					SNEdges.add(e);	
 				}
			}
		}
		List<Relationship> relList = new ArrayList<Relationship>();
		return null;
	}
	
	public static List<Edge> getEdges(Vertex v1, Vertex v2){
		List<Edge> edges = new ArrayList<Edge>();
		for(Edge e:v1.query().direction(Direction.OUT).edges()){
			if(e.getVertex(Direction.IN).equals(v2)) edges.add(e);
		}
		System.out.println("here!!");
		for(Edge e:v2.query().direction(Direction.OUT).edges()){
			if(e.getVertex(Direction.IN).equals(v1)) edges.add(e);
		}
		return edges;
	}
}
