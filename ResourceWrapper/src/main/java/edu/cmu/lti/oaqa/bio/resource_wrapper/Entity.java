package edu.cmu.lti.oaqa.bio.resource_wrapper;

import java.util.ArrayList;

import edu.cmu.lti.oaqa.bio.resource_wrapper.resource_dao.ResourceDataAccessObject;

/**
 * A class to represent the general output from any resource or service implementing ResourceWrapper.
 * Represents a name, definition, type, list of synonyms, list of ID's, a list of Relation's, and the source of the information.
 * 
 * @author Collin McCormack (cmccorma), Tom Vu (tamv)
 * @see Relation
 * @see ID
 * @see ResourceDataAccessObject
 */
public class Entity {
	private String name;
	private ArrayList<Relation> relations;
	private String definition;
	private ArrayList<String> synonyms;
	private ArrayList<ID> ids;
	private String type;
	private Origin origin;
	
	public Entity() {
		this.relations = new ArrayList<Relation>();
		this.synonyms = new ArrayList<String>();
		this.ids = new ArrayList<ID>();
	}
	
	public Entity(Origin origin) {
		this();
		this.origin = origin;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getType() {
		return this.type;
	}
	
	public ArrayList<String> getSynonyms() {
		return this.synonyms;
	}
	
	public ArrayList<Relation> getRelations() {
		return this.relations;
	}
	
	public ArrayList<ID> getIDs() {
		return this.ids;
	}
	
	public Origin getOrigin() {
		return this.origin;
	}
	
	public String toString() {
		return this.name;
	}
	/**
	 * Print entire contents of the object.
	 */
	public void print() {
		System.out.println("Name: " + this.name);
		System.out.println("Type: " + this.type);
		System.out.println("Definition: " + this.definition);
		System.out.println("ID:");
		for (ID i : this.ids)
			System.out.println(i);
		System.out.println("Synonyms:");
		for (String s : this.synonyms)
			System.out.println(s);
		System.out.println("Relations:");
		for (Relation r : this.relations)
			System.out.println(r);
	}
	
	public void setName(String n) {
		this.name = n;
	}
	
	public void addRelation(Relation r) {
		this.relations.add(r);
	}
	
	public void setDefinition(String d) {
		this.definition = d;
	}
	
	public String getDefinition() {
		return this.definition;
	}
	
	public void addSynonym(String s) {
		this.synonyms.add(s);
	}
	
	public void addID(ID id) {
		this.ids.add(id);
	}
	
	public void setType(String t) {
		this.type = t;
	}
	
	public void setOrigin(Origin o) {
		this.origin = o;
	}
}
