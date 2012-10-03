package edu.cmu.lti.oaqa.bio.resource_wrapper;

import java.util.ArrayList;
import java.util.Collection;

public class Term {
	private String term;
	private ArrayList<TermRelationship> relationships;
	
	public Term(String term) {
		this.term = term;
		this.relationships = new ArrayList<TermRelationship>();
	}
	
	public String getTerm() {
		return this.term;
	}
	
	public void addTermRelationship(TermRelationship tr) {
		this.relationships.add(tr);
	}
	
	public void addTermRelationship(Collection<TermRelationship> list) {
		this.relationships.addAll(list);
	}
	
	public ArrayList<TermRelationship> getAllTermRelationships() {
		return this.relationships;
	}
	
	public ArrayList<TermRelationship> getTermRelationshipsByRelation(String relation) {
		ArrayList<TermRelationship> matching = new ArrayList<TermRelationship>();
		for (TermRelationship tr : this.relationships) {
			if (tr.getRelationship().equals(relation))
				matching.add(tr);
		}
		return matching;
	}
	
	public String toString() {
		return this.term + " (" + this.hashCode() + ")";
	}
}
