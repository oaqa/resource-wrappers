package edu.cmu.lti.oaqa.bio.annotate.graph;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;

import edu.cmu.lti.oaqa.bio.annotate.umls.ScoredResult;

public class ConceptBundle {
	
	private List<Concept> conceptList;
	private String searchString; //the search string that was originally used to query the graph
	
	public ConceptBundle(List<Concept> conceptList){
		this.conceptList=conceptList;
	}
	
	public ConceptBundle(List<Concept> conceptList, String searchString){
		this.conceptList=conceptList;
		this.searchString = searchString;
	}
	
	public List<Concept> getConcepts(){
		return Lists.newArrayList(this.conceptList);
	}
	
	public List<String> getTerms(){
		Multiset<String> multiset = HashMultiset.create();
		for (Concept concept:this.conceptList){
			for(String term:concept.getTerms()){
				multiset.add(term);
			}
		}
		List<String> terms = new ArrayList<String>();
		for(String term : Multisets.copyHighestCountFirst(multiset).elementSet()){
			terms.add(term);
		}
		return terms;
	}
	
	public List<String> getSynonyms(){
		List<String> terms = getTerms();
		if(this.searchString==null){
			throw new Error("no searchTerm was provided at construction of the ConceptBundle");
		}
		terms.remove(this.searchString);
		return terms;
	}
	
	public List<String> getDefinitions(){
		List<String> defs = new ArrayList<String>();
		for(Concept concept:this.conceptList){
			for(String def:concept.getDefinition()){
				defs.add(def);
			}
		}
		return defs;
	}
	
	public boolean isEmpty(){
		return this.conceptList.isEmpty();
	}
}
