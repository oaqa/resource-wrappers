package edu.cmu.lti.oaqa.bio.resource_wrapper.resource_dao;

import java.util.Collection;

import edu.cmu.lti.oaqa.bio.resource_wrapper.Entity;

/**
 * Interface for the integration of any external resource.  These methods are mandatory, they must all be implemented for any implementing class.
 * @author Collin McCormack (cmccorma), Tom Vu (tamv)
 * @version 0.3
 */
public interface ResourceDataAccessObject {
	/**
	 * General use Resource query method.
	 * @param query		search terms
	 * @return			Collection<Entity> matching the query
	 */
	Collection<Entity> getEntities(String query);
	
	/**
	 * Overloaded query method with option to only return an exact match to the query.
	 * This method is sometimes preferable to getEntities(query) as it will deliver much more relevant results (with exactMatch=true), often at the cost of recall (results are much narrower).
	 * Each implementation has a different method of making the comparison for an exact match and are documented as such.
	 * 
	 * @param query			search terms
	 * @param exactMatch	boolean controlling the test for exact match to query
	 * @return				Collection<Entity> matching the query
	 */
	Collection<Entity> getEntities(String query, boolean exactMatch);
	
	/**
	 * Queries the resource using an id.
	 * @param id	the id to look for
	 * @return		An Entity matching the id
	 */
	Entity getEntityById(String id);
	
	/**
	 * Overloaded method for querying the resource using an id, with the option to only return an exact match to the queried id.
	 * Each implementation has a different method of making the comparison for an exact match and are documented as such.
	 * In general, this method is much preferred to the more generic one as ID's should be unique to an Entity and a less than exact match may return an irrelevant result.
	 * 
	 * @param id			the id to look for
	 * @param exactMatch	boolean controlling the test for exact match to id
	 * @return				An Entity matching the id
	 */
	Entity getEntityById(String id, boolean exactMatch);
}
