package edu.cmu.lti.oaqa.bio.resource_wrapper;

/**
 * Represents an arbitrary relationship between the owning Entity and something.  A sort of catch-all for things that don't otherwise fit conveniently into other Entity fields.
 * Ex "part of", "DNA repair"
 * Ex "phenotype", "polydactyly"
 * Ex "interaction", "benzodiazepine"
 * 
 * @author Collin McCormack (cmccorma), Tom Vu (tamv)
 * @see Entity
 */
public class Relation {
	private String property;
	private String value;
	
	public Relation(String property, String value) {
		this.property = property;
		this.value = value;
	}
	
	public String getProperty() {
		return this.property;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public String toString() {
		return this.property + ": " + this.value;
	}
}
