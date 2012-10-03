package edu.cmu.lti.oaqa.bio.annotate.umls;

import java.util.ArrayList;
import java.util.HashSet;
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
import com.tinkerpop.blueprints.Vertex;

import edu.cmu.lti.oaqa.bio.annotate.graph.ClassRelationship;
import edu.cmu.lti.oaqa.bio.annotate.graph.Concept;
import edu.cmu.lti.oaqa.bio.annotate.graph.ConceptRelationship;
import edu.cmu.lti.oaqa.bio.annotate.graph.Relationship;
import edu.cmu.lti.oaqa.graph.core.tools.Traversals;

public class UMLSConcept implements Concept{
	
	Vertex vertex = null;
	private List<String> terms = null;
	private List<Relationship> conceptRelationships = null;
	private List<ClassRelationship> classRelationships = null;
	private List<String> definitions;
	private List<Relationship> typeOfs;
	private boolean synonymsAreOrdered;
	
	public UMLSConcept(Vertex vertex){
		this.vertex = vertex;
	}
	
	public UMLSConcept(Vertex vertex, boolean prefetch){
		this.vertex = vertex;
		if(prefetch==true){
			getTerms();
		}
	}
	
	public UMLSConcept(List<String> terms, List<Relationship> conceptRelationships,
			List<ClassRelationship> classRelationships, List<String> definitions,
			List<Relationship> typeOfs, boolean synonymsAreOrdered){
		
		this.terms = terms;
		this.conceptRelationships = conceptRelationships;
		this.classRelationships = classRelationships;
		this.definitions = definitions;
		this.typeOfs=typeOfs;
		this.synonymsAreOrdered=synonymsAreOrdered;
	}
		
	/**
	 * @return all terms except term
	 */
	public List<String> getTermsNoRepeats(){
		Multiset<String> multiset = HashMultiset.create();
		List<String> terms = this.getTerms();
		for(String term:terms){
			multiset.add(term);
		}
		Set<String> sortedSet = Multisets.copyHighestCountFirst(multiset).elementSet();
		return Lists.newArrayList(sortedSet);
	}
	
	public Vertex getRawVertex(){
		return vertex;
	}
	
	public List<String> getTerms(){
		if(this.terms==null){
			this.terms = new ArrayList<String>();
			for(Vertex atom:this.vertex.query().labels("atom").direction(Direction.OUT).vertices()){
				String term = (String) atom.getProperty("exactString");
				if(term!=null) this.terms.add(term.toLowerCase());
			}
		}
		return Lists.newArrayList(terms);
	}
	
	public List<String> getDefinition(){
		if(this.definitions==null){
			this.definitions = new ArrayList<String>();
			for(Vertex atom: this.vertex.query().labels("atom").direction(Direction.OUT).vertices()){
				//String[] array = (String[]) atom.getProperty("definition");
				Object defArray = atom.getProperty("definition");
				if(defArray==null) continue;
				//System.out.println(defArray.toString());
				String def = getDefFromJSON(defArray.toString());
				this.definitions.add(def);
			}
		}
		return Lists.newArrayList(this.definitions);
	}
	
	private String getDefFromJSON(String string) {
		String[] split = string.split("\"value\":");
		String def = split[split.length-1];
		def = def.substring(1, def.length()-3);
		return def;
	}

	public List<String> getSynonyms(String term){
		List<String> list = Lists.newArrayList(this.getTerms()); //does this create a whole new list or what?
		list.remove(term);
		if(!this.terms.contains(term)) throw new Error("term: \""+term+"\" is not in this concept");
		return list;
	}

	public List<Relationship> getAllRelationships() {
		List<Relationship> typeOfs = getTypeOfs();
		List<Relationship> conceptRelationships = getConceptRelationships();
		//List<Relationship> classRelationships =  getClassRelationships();
		ArrayList<Relationship> returnList = Lists.newArrayList(typeOfs);
		returnList.addAll(conceptRelationships);
		return returnList;
	}

	public List<Relationship> getConceptRelationships() {
		return getRelationshipsWithCache(conceptRelationships,"relationshipName",Direction.BOTH,"relatedConcept");
	}

	public List<Relationship> getTypeOfs() {
		return getRelationshipsWithCache(typeOfs,null,Direction.OUT,"semanticType");
	}

	public boolean synonymsAreOrdered() {
		return this.synonymsAreOrdered;
	}

	public boolean backedByDB() {
		return  this.vertex!=null;
	}
	
	
	private List<Relationship> getRelationshipsWithCache(List<Relationship> cacheField, String labelKey,
		Direction dir, String...edgeLabels){
		if(cacheField==null){
			cacheField = new ArrayList<Relationship>();
			for(Edge e:vertex.query().labels(edgeLabels).direction(dir).edges()){
				Concept toConcept = new UMLSConcept(Traversals.getAdjacentOtherVertexOnEdge(e,this.vertex));
				String relationshipLabel;
				if(labelKey == null) relationshipLabel=e.getLabel();
				else relationshipLabel=(String) e.getProperty(labelKey);
				double confidence = 0.0;
				String source = (String) e.getProperty("source");
				Relationship rel = new UMLSRelationship(this, toConcept, relationshipLabel, confidence, source);
				cacheField.add(rel);
			}
		}
	//finish the getRelationshipsWithCache
	//redo constuctors for UMLSConcept and UMLSRelationship so as to get rid of the factory class
	return cacheField;
	}
}
