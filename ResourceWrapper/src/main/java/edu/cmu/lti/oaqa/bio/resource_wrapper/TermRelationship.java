package edu.cmu.lti.oaqa.bio.resource_wrapper;

public class TermRelationship {
	
	private String fromTerm;
	private String relationship;
	private String toTerm;
	private double confidence;
	private Origin origin;
	private String parentTerm;
	
	/**
	 * Detailed constructor, includes parent fromTerm.
	 * @param fromTerm Subject fromTerm
	 * @param relationship Verb relationship
	 * @param toTerm Object fromTerm
	 * @param confidence Confidence toTerm
	 * @param origin Origin of the information
	 * @param parentTerm parent fromTerm, owning concept
	 */
	public TermRelationship(String fromTerm, String relationship, String toTerm, double confidence, Origin origin, String parentTerm) {
		this.fromTerm = fromTerm;
		this.relationship = relationship;
		this.toTerm = toTerm;
		this.confidence = confidence;
		this.origin = origin;
		this.parentTerm = parentTerm;
	}
	
	/**
	 * Less detailed constructor, excludes parent fromTerm.
	 * @param fromTerm Subject fromTerm
	 * @param relationship Verb relationship
	 * @param toTerm Object fromTerm
	 * @param confidence Confidence toTerm
	 * @param origin Origin of the information
	 */
	public TermRelationship(String fromTerm, String relationship, String toTerm, double confidence, Origin origin) {
		this(fromTerm, relationship, toTerm, confidence, origin, null);
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
	
	public Origin getOrigin() {
		return this.origin;
	}
	public void setOrigin(Origin origin) {
		this.origin = origin;
	}
	
	public String getParentTerm() {
		return this.parentTerm;
	}
	public void setParentTerm(String parentTerm) {
		this.parentTerm = parentTerm;
	}
	
	public String toString() {
		return this.fromTerm + " | " + this.relationship + " | " + this.toTerm + "; " + this.confidence + "; " + this.origin + "; " + this.parentTerm;
	}
}
