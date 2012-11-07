package edu.cmu.lti.oaqa.bio.annotate.entrezgene_wrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.cmu.lti.oaqa.bio.annotate.entrezgene_dao.EntrezGeneDAO;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Origin;
import edu.cmu.lti.oaqa.bio.resource_wrapper.ResourceWrapper;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Term;
import edu.cmu.lti.oaqa.bio.resource_wrapper.TermRelationship;
import edu.cmu.lti.oaqa.bio.resource_wrapper.cache.DBCache;
import edu.cmu.lti.oaqa.bio.resource_wrapper.resource_dao.EntityTermConverter;

public class EntrezGeneWrapper implements ResourceWrapper {
	EntrezGeneDAO egw;
	private DBCache dbc;
	
	/**
	 * Constructor.
	 */
	public EntrezGeneWrapper() {
		this.egw = new EntrezGeneDAO();
		this.dbc = new DBCache();
	}
	
	/**
	 * Get a complete Term object from Entrez Gene. 
	 * If the database already contains the top match, then it will be the source for
	 * the Term.  In the case that multiple entities have the same name, all of their relationships
	 * will be put into the same 'composite' Term object.
	 * Caution: may not return exact match to query.  Search is based on
	 * Entrez Gene native search, try it here: http://www.ncbi.nlm.nih.gov/gene
	 * @param termQuery String to query for
	 * @return Term object that was the top result from Entrez Gene, or composite from db
	 */
	public Term getTerm(String termQuery) {
		Term outputTerm = null;
		ArrayList<String> searchResults = null;
		String id = null;
		
		// Search EG
		try {
			searchResults = this.egw.search(termQuery);
			if (searchResults == null || searchResults.size() == 0)
				return null;
			id = searchResults.get(0);
		} catch (IOException e) {
			System.out.println("getterm(Str): Entrez Gene web service issue [search]");
			e.printStackTrace();
		}
		
		// Check database for first entry; if present, retrieve it
		if (this.dbc.IDinCache("EntrezGene:"+id))
			outputTerm = this.dbc.getTermByID("EntrezGene:"+id, Origin.ENTREZGENE);
		// Else, retrieve it from EG
		else {
			try {
				outputTerm = EntityTermConverter.EntityToTerm(this.egw.fetch(id));
			} catch (IOException e) {
				System.out.println("getTerm(Str): Entrez Gene web service issue [fetch]");
				e.printStackTrace();
			}
			// Add to cache
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
	public Term getTerm (String termQuery, boolean exact) {
		Term outputTerm = null;
		// Exact matching
		if (exact) {
			// Check for exact term presence in cache
			if (this.dbc.inCache(termQuery)) {
				System.out.println("Retrieving "+termQuery+" from cache...");
				outputTerm = this.dbc.getTerm(termQuery, Origin.ENTREZGENE);
			}
			// Else, use the original resource to get it
			else {
				System.out.println("Retrieving "+termQuery+" from web service...");
				try {
					ArrayList<String> searchResults = this.egw.search(termQuery);
					// Check database for search results, see if they match the query
					for (String id : searchResults) {
						if (this.dbc.IDinCache(id)) {
							Term tempTerm = this.dbc.getTermByID(id, Origin.ENTREZGENE);
							if (tempTerm.getTerm().equalsIgnoreCase(termQuery)) {
								outputTerm = tempTerm;
								break;
							}
						}
						else { // Otherwise, use web service to get info
							String name = this.egw.getSummary(id);
							if (name.equalsIgnoreCase(termQuery)) {
								outputTerm = EntityTermConverter.EntityToTerm(this.egw.fetch(id));
								break;
							}
						}
					}
				} catch (IOException ioe) {
					System.out.println("Search or fetch to Entrez Gene web service failed due to IO.");
					ioe.printStackTrace();
				}
			}
			// 	Add it to the cache
			if (outputTerm != null)
				this.dbc.addWholeTerm(outputTerm);
		}
		// Inexact matching
		else 
			outputTerm = this.getTerm(termQuery);
		
		return outputTerm;
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
	 * Order of the list is the relevance ranking from Entrez Gene search.
	 * 
	 * Database cache is used to retrieve terms whenever possible; when the database does NOT contain
	 * the required information, the Entrez Gene web service is queried instead.
	 * 
	 * @param termQuery String to query for
	 * @param amountRequested Maximum number of results to attempt to find
	 * @return ArrayList of Terms, size is never greater than amountRequested
	 */
	public Collection<Term> getTerms(String termQuery, int amountRequested) {
		List<String> idResults = null;
		ArrayList<Term> termResults = null;
		try {
			// Search EG for termQuery
			idResults = this.egw.search(termQuery);
			// Trim results down to amountRequested, if necessary
			if (idResults.size() > amountRequested)
				idResults = idResults.subList(0, amountRequested);
			termResults = new ArrayList<Term>(idResults.size());
			// Check database for names
			for (String id : idResults) {
				// If present, retrieve from database
				id = "EntrezGene:"+id;
				if (this.dbc.IDinCache(id))
					termResults.add(this.dbc.getTermByID(id, Origin.ENTREZGENE));
				// Else, retrieve from EG and add to cache
				else {
					try {
						termResults.add(EntityTermConverter.EntityToTerm(this.egw.fetch(id)));
					} catch (IOException e) {
						System.out.println("Wasn't able to fetch ID "+id+" from Entrez Gene.  See stack trace below:");
						e.printStackTrace();
					}
				}
			}
			// Add termResults to cache AFTER retrieving all of them
			// If done before, it will test positive for being in the database if named the same
			// This short-circuits the retrieval of results for other identically named items
			// And any different results are lost.
			for (Term t : termResults)
				this.dbc.addWholeTerm(t);
		} catch (IOException ioe) {
			System.out.println("getTerms(Str): Entrez Gene web service issue [search, summary, fetch]");
			ioe.printStackTrace();
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
