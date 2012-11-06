package edu.cmu.lti.oaqa.bio.resource_wrapper.resource_dao;

import java.util.Collection;

import edu.cmu.lti.oaqa.bio.resource_wrapper.Entity;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Relation;
import edu.cmu.lti.oaqa.bio.species_mapper.Species;

public interface ResourceDataAccessObjectExtended extends ResourceDataAccessObject {
	
	/**
	 * More specific Resource query method, must include type comparison.
	 * @param query		search terms
	 * @param aType		String of a type
	 * @return			Collection<Entity> matching the query and type
	 */
	Collection<Entity> getEntities(String query, String aType);
	
	/**
	 * More specific Resource query method that only returns pertaining to the specified species.
	 * @param query		search terms
	 * @param species	a Species object, as is returned by SpeciesMapper
	 * @return			ArrayList<Entity> matching the query and species
	 * @see edu.cmu.lti.oaqa.bio.species_mapper.SpeciesMapper
	 */
	Collection<Entity> getEntities(String query, Species species);
	
	/**
	 * Reads an Entity object and queries the resource based on the specified relation.
	 * @param eObj		Entity object, base of Relation
	 * @param relObj	Relation object, the verb
	 * @return			A Collection<Entity> containing the related Entity's
	 */
	Collection<Entity> getRelatedEntities(Entity eObj, Relation relObj);	
}
