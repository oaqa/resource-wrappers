package edu.cmu.lti.oaqa.bio.resource_wrapper.resource_dao;

import edu.cmu.lti.oaqa.bio.resource_wrapper.Entity;
import edu.cmu.lti.oaqa.bio.resource_wrapper.ID;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Relation;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Term;
import edu.cmu.lti.oaqa.bio.resource_wrapper.TermRelationship;

public class EntityTermConverter {
	
	public static Term EntityToTerm(Entity ent) {
		Term term = new Term(ent.getName());
		// Add type
		term.addTermRelationship(new TermRelationship(term.getTerm(), "type", ent.getType(), 1.0, ent.getOrigin()));
		// Add definition
		term.addTermRelationship(new TermRelationship(term.getTerm(), "definition", ent.getDefinition(), 1.0, ent.getOrigin()));
		// Add ID's
		for (ID id : ent.getIDs())
			term.addTermRelationship(new TermRelationship(term.getTerm(), "ID", id.toString(), 1.0, ent.getOrigin()));
		// Add synonyms
		for (String syn : ent.getSynonyms())
			term.addTermRelationship(new TermRelationship(term.getTerm(), "synonym", syn, 1.0, ent.getOrigin()));
		// Add other relationships
		for (Relation rel : ent.getRelations())
			term.addTermRelationship(new TermRelationship(term.getTerm(), rel.getProperty(), rel.getValue(), 1.0, ent.getOrigin()));
		
		return term;
	}
}
