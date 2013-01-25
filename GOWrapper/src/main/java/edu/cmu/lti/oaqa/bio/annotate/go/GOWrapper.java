package edu.cmu.lti.oaqa.bio.annotate.go;

import java.util.ArrayList;
import java.util.Collection;

import edu.cmu.lti.oaqa.bio.resource_wrapper.Origin;
import edu.cmu.lti.oaqa.bio.resource_wrapper.ResourceWrapper;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Term;
import edu.cmu.lti.oaqa.bio.resource_wrapper.TermRelationship;
import edu.cmu.lti.oaqa.bio.resource_wrapper.cache.DBCache;

/**
 * A wrapper for Gene Ontology (GO).
 * 
 * @author Collin McCormack
 * @version 0.1
 */
public class GOWrapper implements ResourceWrapper {
	private DBCache dbc;
	
	/**
	 * Constructor, initiate database cache object.
	 */
	public GOWrapper() {
		this.dbc = new DBCache();
	}
	
	@Override
	/**
	 * Search for terms from GO that match 'termQuery'.  Return the best match.
	 * 
	 * @param termQuery search 'term'
	 * @return Term when one is found, null if no matches
	 */
	public Term getTerm(String termQuery) {
		ArrayList<String> matches = this.dbc.searchTerms(termQuery, Origin.GO);
		if (matches.size() == 0)
			return null;
		return this.dbc.getTerm(matches.get(0), Origin.GO);
	}

	@Override
	/**
	 * Search for terms from GO that match 'termQuery'.  Return the best match.
	 * If exact is true, query for the exact term (instead of searching with it).
	 * 
	 * @param termQuery
	 * @param exact boolean for exact matching to termQuery
	 * @return Term when one is found, null if no matches
	 */
	public Term getTerm(String termQuery, boolean exact) {
		if (exact)
			return this.dbc.getTerm(termQuery, Origin.GO);
		else {
			return this.getTerm(termQuery);
		}
	}

	@Override
	/**
	 * Same as {@link #getTerms(String,int), but only returns a maximum of 5 results.
	 * @see #getTerms(String,int)
	 */
	public Collection<Term> getTerms(String termQuery) {
		return this.getTerms(termQuery, 5);
	}

	@Override
	/**
	 * Search the database using termQuery.  Return the number of requested results
	 * or the number of matches for the search, whichever is lesser.
	 * 
	 * @param termQuery search terms
	 * @param amountRequested the maximum amount of results to return
	 * @return ArrayList of Term objects matching termQuery
	 */
	public Collection<Term> getTerms(String termQuery, int amountRequested) {
		ArrayList<String> matches = this.dbc.searchTerms(termQuery, Origin.GO);
		ArrayList<Term> results = new ArrayList<Term>();
		int limit = matches.size() > amountRequested ? matches.size() : amountRequested;
		for (int i = 0; i < limit; i++)
			results.add(this.dbc.getTerm(matches.get(i), Origin.GO));
		return results;
	}

	@Override
	/**
	 * Convenience method for returning the synonyms of the result of an exact match search for termQuery.
	 * @param termQuery exact term
	 * @return ArrayList of String synonyms
	 */
	public Collection<String> getSynonyms(String termQuery) {
		Term baseTerm = this.getTerm(termQuery, true);
		if (baseTerm == null)
			return new ArrayList<String>(0);
		else {
			ArrayList<TermRelationship> results = baseTerm.getTermRelationshipsByRelation("synonym");
			ArrayList<String> synonyms = new ArrayList<String>(results.size());
			for (TermRelationship tr : results)
				synonyms.add(tr.getToTerm());
			return synonyms;
		}
	}
	
	public Term getTermByID(String goID) {
		if (this.dbc.IDinCache(goID))
			return this.dbc.getTermByID(goID, Origin.GO);
		else
			return null;
	}
	
}
