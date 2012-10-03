package edu.cmu.lti.oaqa.bio.annotate.umls;

import edu.cmu.lti.oaqa.bio.annotate.graph.Concept;
import edu.cmu.lti.oaqa.bio.annotate.graph.Relationship;

public class UMLSRelationship implements Relationship {
	private Concept fromConcept;
	private Concept toConcept;
	private String relationshipLabel;
	private double confidence;
	private String source;
	
	public UMLSRelationship(Concept fromConcept,Concept toConcept,String relationshipLabel,
			double confidence,String source){
		this.fromConcept=fromConcept;
		this.toConcept=toConcept;
		this.relationshipLabel=relationshipLabel;
		this.confidence=confidence;
		this.source=source;
	}
	
	public Concept getFromConcept() {
		return this.fromConcept;
	}

	public Concept getToConcept() {
		return this.toConcept;
	}

	public String getRelationshipLabel() {
		return this.relationshipLabel;
	}

	public double getConfidence() {
		return this.confidence;
	}

	public String getSource() {
		return this.source;
	}

}
