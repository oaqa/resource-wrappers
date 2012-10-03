/**
 * 
 */
package edu.cmu.lti.oaqa.bio.annotate.graph;

/**
 * @author jackm321
 *
 */
public interface Relationship {
	
	public Concept getFromConcept();
	public Concept getToConcept();
	public String getRelationshipLabel();
	public double getConfidence();
	public String getSource();

}
