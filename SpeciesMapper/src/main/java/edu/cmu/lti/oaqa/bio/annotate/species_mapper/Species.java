package edu.cmu.lti.oaqa.bio.annotate.species_mapper;

public class Species {
	private String properName;
	
	public Species(String name) {
		this.properName = name;
	}
	
	public String getName() {
		return this.properName;
	}
}
