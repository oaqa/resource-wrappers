package edu.cmu.lti.oaqa.bio.resource_wrapper;

public class TermRelationship {
	
	private String fromTerm;
	private String relationship;
	private String toTerm;
	private double confidence;
	private String source;
	private String parentTerm;
	
	/**
	 * Detailed constructor, includes parent fromTerm.
	 * @param fromTerm Subject fromTerm
	 * @param relationship Verb relationship
	 * @param toTerm Object fromTerm
	 * @param confidence Confidence toTerm
	 * @param source Source of the information
	 * @param parentTerm parent fromTerm, owning concept
	 */
	public TermRelationship(String fromTerm, String relationship, String toTerm, double confidence, String source, String parentTerm) {
		this.fromTerm = fromTerm;
		this.relationship = relationship;
		this.toTerm = toTerm;
		this.confidence = confidence;
		this.source = source;
		this.parentTerm = parentTerm;
	}
	
	/**
	 * Less detailed constructor, excludes parent fromTerm.
	 * @param fromTerm Subject fromTerm
	 * @param relationship Verb relationship
	 * @param toTerm Object fromTerm
	 * @param confidence Confidence toTerm
	 * @param source Source of the information
	 */
	public TermRelationship(String fromTerm, String relationship, String toTerm, double confidence, String source) {
		this(fromTerm, relationship, toTerm, confidence, source, null);
	}

	public String getFromTerm() {
		return this.fromTerm;
	}
	public void setFromTerm(String fromTerm) {
		this.fromTerm = fromTerm;
	}

	public String getRelationship() {
		return this.relationship;
	}
	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}
	
	public String getToTerm() {
		return this.toTerm;
	}
	public void setToTerm(String toTerm) {
		this.toTerm = toTerm;
	}
	
	public double getConfidence() {
		return this.confidence;
	}
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
	
	public String getSource() {
		return this.source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	
	public String getParentTerm() {
		return this.parentTerm;
	}
	public void setParentTerm(String parentTerm) {
		this.parentTerm = parentTerm;
	}
	
	public String toString() {
		return this.fromTerm + " | " + this.relationship + " | " + this.toTerm + "; " + this.confidence + "; " + this.source + "; " + this.parentTerm;
	}
}
