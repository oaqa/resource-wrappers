package edu.cmu.lti.oaqa.bio.annotate.entrezgene_wrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import edu.cmu.lti.oaqa.bio.annotate.entrezgene_dao.EntrezGeneDAO;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Entity;
import edu.cmu.lti.oaqa.bio.resource_wrapper.EntityTermConverter;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Origin;
import edu.cmu.lti.oaqa.bio.resource_wrapper.ResourceWrapper;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Term;
import edu.cmu.lti.oaqa.bio.resource_wrapper.TermRelationship;
import edu.cmu.lti.oaqa.bio.resource_wrapper.cache.DBCache;

public class EntrezGeneWrapper implements ResourceWrapper {
	EntrezGeneDAO egdao;
	private DBCache dbc;
	
	/**
	 * Constructor, initializes data access object and Cache connector.
	 */
	public EntrezGeneWrapper() {
		this.egdao = new EntrezGeneDAO();
		this.dbc = new DBCache();
	}
	
	/**
	 * A "smarter" Entrez Gene search function.  Wraps the rough edges of the 
	 * raw EntreqGeneDAO search.  It accounts for exceptions and automatically 
	 * retries in the case where the outside service fails.
	 * 
	 * @param query String of the term(s) to search for
	 * @param retry Number of retries to performs in case of bad connection
	 * @return ArrayList&ltString&gt of Entrez Gene ID's usable for fetching
	 */
	private ArrayList<String> smartSearch(String query, int retry) {
		// Default return value
		ArrayList<String> searchResults = new ArrayList<String>(0);
		
		// Loop until valid search results are obtained or retries are exhausted
		while (retry >= 0) {
			try {
				searchResults = this.egdao.search(query);
				break;
			} catch (IOException ioe) {
				System.out.println("Retrying search: IOException occurred (bad connection?)");
				retry--;
			} catch (NullPointerException npe) {
				System.out.println("Retrying search: NullPointerException occurred (XML problem)");
				retry--;
			}
		}
		
		return searchResults;
	}
	
	/**
	 * A "smarter" Entrez Gene fetch function.  Wraps the rough edges of the 
	 * raw EntrezGeneDAO fetch.  It accounts for exceptions and automatically 
	 * retries in the case where the outside service fails.  Also, automatically 
	 * adds the Term to the cache.
	 * 
	 * This will still return null if the entry corresponding to id cannot be 
	 * retrieved within the number of retries specified.
	 * 
	 * @param id String Entrez Gene identifier
	 * @param retry Number of retries to perform in case of bad connection
	 * @return Term corresponding to id, null if no entry could be retrieved
	 */
	private Term smartFetch(String id, int retry) {
		Term outputTerm = null;
		Entity fetched = null;
		
		while (fetched == null && retry >= 0) {
			try {
				fetched = this.egdao.fetch(id);
				break;
			} catch (IOException ioe) {
				System.out.println("Retrying fetch: IOException occurred (bad connection?)");
				retry--;
			} catch (NullPointerException npe) {
				System.out.println("Retrying fetch: NullPointerException occurred (XML problem)");
				retry--;
			}
		}
		// If fetched is actually an Entity object, convert and add to cache
		if (fetched != null) {
			outputTerm = EntityTermConverter.EntityToTerm(fetched);
			this.dbc.addWholeTerm(outputTerm);
		}
		
		return outputTerm;
	}
	
	/**
	 * If the cache (database) already contains the top match, then it will
	 * be the source for the Term.  In the case that multiple entities have
	 * the same name, all of their relationships will be put into the same
	 * 'composite' Term object.
	 * 
	 * If the database does not contain a match, the Entrez Gene search
	 * function is used.  The first result from the search is fetched and
	 * returned.
	 * 
	 * Caution: may not return exact match to query.  Search is based on
	 * Entrez Gene native search, try it here:
	 * http://www.ncbi.nlm.nih.gov/gene
	 * 
	 * Caution: may return a null pointer if there were no search results or
	 * the web service could not be contacted.
	 * 
	 * @param termQuery String to query for
	 * @return Term object that was the top result from Entrez Gene, or
	 * composite from the cache (or null)
	 */
	public Term getTerm(String termQuery) {
		Term outputTerm = null;
		
		if (this.dbc.inCache(termQuery)) {
			outputTerm = this.dbc.getTerm(termQuery, Origin.ENTREZGENE);
		} else {
			ArrayList<String> searchResults = this.smartSearch(termQuery, 1);
			// If no search results, and thus no valid Terms found, return null (the default)
			// Otherwise, fetch the first result from the search
			if (!searchResults.isEmpty())
				outputTerm = this.smartFetch(searchResults.get(0), 1);
		}
		return outputTerm;
	}
	
	/**
	 * Get a complete Term object from Entrez Gene, enforcing an exact match
	 * to the input query.
	 * 
	 * If the cache (database) already contains the exact match, then it will
	 * be the source for the Term.  In the case that multiple entities have
	 * the same name, all of their relationships will be put into the same
	 * 'composite' Term object.
	 * 
	 * If the database does not contain a match, the Entrez Gene search
	 * function is used.  The results are iteratively checked until an exact
	 * match is found or no results remain.
	 * 
	 * @param termQuery String to query for
	 * @return match Term object, or null if there were no valid results
	 */
	public Term getExactTerm(String termQuery) {
		Term outputTerm = null;
		// Check cache for exact termQuery
		if (this.dbc.inCache(termQuery))
			outputTerm = this.dbc.getTerm(termQuery);
		else {
			// Search, iteratively fetch and test results until an exact
			// match is found or no results remains
			for (String result : this.smartSearch(termQuery, 1)) {
				Term t = this.smartFetch(result, 1);
				if (t != null && t.getTerm().equalsIgnoreCase(termQuery)) {
					outputTerm = t;
					break;
				}	
			}
		}
		return outputTerm;
	}

	
	/**
	 * Same as {@link #getTerms(String, int)} with default of 5 requested 
	 * results.
	 * 
	 * @param termQuery String to query for
	 * @return top 5 results for termQuery
	 * @see #getTerms(String, int)
	 */
	public Collection<Term> getTerms(String termQuery) {
		return this.getTerms(termQuery, 5);
	}
	
	/**
	 * Find a list of Term objects corresponding to the termQuery, limited by
	 * amountRequested.  This function will never return a list larger than
	 * amountRequested, but it may return a smaller list.  Order of the list
	 * is the relevance ranking from the Entrez Gene search function.
	 * 
	 * The database cache is used to retrieve terms whenever possible; when
	 * the database does NOT contain the required information, the Entrez
	 * Gene web service is queried instead.
	 * 
	 * @param termQuery String to query for
	 * @param amountRequested Maximum number of results to attempt to find
	 * @return ArrayList of Terms, size is never greater than amountRequested
	 */
	public Collection<Term> getTerms(String termQuery, int amountRequested) {
		// Search and trim results (if necessary)
		ArrayList<String> searchResults = this.smartSearch(termQuery, 1);
		
		int limit = amountRequested > searchResults.size() ? searchResults.size() : amountRequested;
		
		// Iteratively fetch and add
		ArrayList<Term> termResults = new ArrayList<Term>(limit);
		for (int i = 0; i < limit; i++) {
			String id = searchResults.get(i);
			// Try to find the ID in the cache first, otherwise fetch from Entrez Gene
			if (this.dbc.IDinCache("EntrezGene:"+id))
				termResults.add(this.dbc.getTermByID("EntrezGene:"+id, Origin.ENTREZGENE));
			else {
				Term t = this.smartFetch(id, 1);
				if (t != null)
					termResults.add(t);
			}
		}
		
		return termResults;
	}		

	/**
	 * Convenience method for returning the synonyms of the result of an
	 * exact match search for termQuery.
	 * 
	 * @param termQuery String to query for
	 * @return Collection of String synonyms
	 */
	public Collection<String> getSynonyms(String termQuery) {
		Term resultTerm = this.getExactTerm(termQuery);
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
