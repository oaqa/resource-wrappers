package edu.cmu.lti.oaqa.bio.annotate.mesh_wrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.cmu.lti.oaqa.bio.annotate.mesh_dao.MeshDAO;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Entity;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Origin;
import edu.cmu.lti.oaqa.bio.resource_wrapper.ResourceWrapper;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Term;
import edu.cmu.lti.oaqa.bio.resource_wrapper.TermRelationship;
import edu.cmu.lti.oaqa.bio.resource_wrapper.cache.DBCache;
import edu.cmu.lti.oaqa.bio.resource_wrapper.resource_dao.EntityTermConverter;

public class MeshWrapper implements ResourceWrapper {
	MeshDAO md;
	DBCache dbc;
	
	/**
	 * Constructor.
	 */
	public MeshWrapper() {
		this.md = new MeshDAO();
		this.dbc = new DBCache();
	}
	
	/**
	 * Get a complete Term object from MeSH. 
	 * If the database already contains the top match, then it will be the source for
	 * the Term.  In the case that multiple entities have the same name, all of their relationships
	 * will be put into the same 'composite' Term object.
	 * Caution: may not return exact match to query.  Search is based on
	 * MeSH native search, try it here: http://www.ncbi.nlm.nih.gov/mesh/
	 * @param termQuery String to query for
	 * @return Term object that was the top result from MeSH, or composite from db
	 */
	public Term getTerm(String termQuery) {
		Term outputTerm = null;
		ArrayList<String> searchResults = null;
		
		// Check cache for term
		if (this.dbc.inCache(termQuery)) {
			System.out.println("MeSH term in DB, fetching...");
			outputTerm = this.dbc.getTerm(termQuery, Origin.MESH);
		} else {
			System.out.println("Searching MeSH...");
			// Search from Mesh and get the top one
			try {
				searchResults = this.md.search(termQuery);
			} catch (IOException ioEx) {
				System.out.println("MeshWraper: MeshDAO search failed due to IOException, probably a bad connection.");
				ioEx.printStackTrace();
				return null;
			}
			// Fetch first result and convert to Term
			try {
				outputTerm = EntityTermConverter.EntityToTerm(this.md.fetch(searchResults.get(0)));
			} catch (IOException e) {
				System.out.println("MeshWrapper: MeshDAO fetch failed due to IOExcetion, probably a bad connection.");
				e.printStackTrace();
				return null;
			}
			// Add new term to cache
			this.dbc.addWholeTerm(outputTerm);
		}
		
		return outputTerm;
	}

	/**
	 * Similar to {@link #getTerm(String)}, but with option to enforce exact matching
	 * to the termQuery String.  Returns null when there is no exact match.
	 * @param termQuery String to query for
	 * @param exact boolean to enforce exact matching to termQuery
	 * @return match Term object, or null
	 */
	public Term getTerm(String termQuery, boolean exact) {
		if (exact) {
			Term outputTerm = null;
			// Check cache for exact termQuery
			if (this.dbc.inCache(termQuery))
				outputTerm = this.dbc.getTerm(termQuery);
			else {
				// Mesh - getEntities exact
				// Not re-implementing it locally because MeSH is fast enough that the request times are tolerable
				ArrayList<Entity> entList = this.md.getEntities(termQuery, true);
				// get first, add to cache, return
				if (entList.size() > 0) {
					outputTerm = EntityTermConverter.EntityToTerm(entList.get(0));
					this.dbc.addWholeTerm(outputTerm);
				}
			}
			return outputTerm;
		}
		else
			return this.getTerm(termQuery);
	}

	/**
	 * Same as {@link #getTerms(String, int)} with default of 5 requested results.
	 * @param termQuery String to query for
	 * @return top 5 results for termQuery
	 * @see #getTerms(String, int)
	 */
	public Collection<Term> getTerms(String termQuery) {
		return this.getTerms(termQuery, 5);
	}

	/**
	 * Find a list of Term objects corresponding to the termQuery, limited by amountRequested.
	 * Will never return a list larger than amountRequested, but may return a smaller list.
	 * Order of the list is the relevance ranking from MeSH search.
	 * 
	 * Database cache is used to retrieve terms whenever possible; when the database does NOT contain
	 * the required information, the MeSH web service is queried instead.
	 * 
	 * @param termQuery String to query for
	 * @param amountRequested Maximum number of results to attempt to find
	 * @return ArrayList of Terms, size is never greater than amountRequested
	 */
	public Collection<Term> getTerms(String termQuery, int amountRequested) {
		List<String> searchResults = null;
		ArrayList<Term> termResults = null;
		// Search MeSH
		try {
			searchResults = this.md.search(termQuery);
		} catch (IOException e) {
			System.out.println("MeshDAO search failed due to IO.");
			e.printStackTrace();
			return null;
		}
		// Trim down to amountRequested
		if (searchResults.size() > amountRequested)
			searchResults = searchResults.subList(0, amountRequested);
		termResults = new ArrayList<Term>(searchResults.size());
		// Try to find them in the cache, otherwise get them from MeSH
		for (String id : searchResults) {
			if (this.dbc.IDinCache("MeSH:"+id))
				termResults.add(this.dbc.getTermByID("MeSH:"+id, Origin.MESH));
			else {
				try {
					Term tempTerm = EntityTermConverter.EntityToTerm(this.md.fetch(id));
					this.dbc.addWholeTerm(tempTerm);
					termResults.add(tempTerm);
				} catch (IOException e) {
					System.out.println("MeshDAO fetch failed due to IO.");
					e.printStackTrace();
				}
			}
		}
		return termResults;
	}

	/**
	 * Convenience method for returning the synonyms of the result of an exact match search for termQuery.
	 * @param termQuery String to query for
	 * @return Collection of String synonyms
	 */
	public Collection<String> getSynonyms(String termQuery) {
		Term resultTerm = this.getTerm(termQuery, true);
		if (resultTerm == null)
			return new ArrayList<String>(0);
		else {
			ArrayList<TermRelationship> results = resultTerm.getTermRelationshipsByRelation("synonym");
			ArrayList<String> synonyms = new ArrayList<String>(results.size());
			for (TermRelationship tr : results)
				synonyms.add(tr.getToTerm());
			return synonyms;
		}
	}
	
	
}