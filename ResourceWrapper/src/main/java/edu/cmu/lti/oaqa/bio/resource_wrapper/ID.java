package edu.cmu.lti.oaqa.bio.resource_wrapper;
/**
 * Represents a system ID from some authority.  Contains two strings: name of the authority, identifier given by that authority.
 * Ex HGNC:1100 (BRCA1)
 * Ex Vega:OTTHUMG00000157426 (BRCA1)
 * 
 * @author Collin McCormack (cmccorma), Tom Vu (tamv)
 * @see	Entity
 */
public class ID {
	private String authority;
	private String id;
	
	public ID(String auth, String ID) {
		this.authority = auth;
		this.id = ID;
	}
	
	public String toString() {
		return this.authority + ":" + this.id;
	}
}
