package edu.cmu.lti.oaqa.bio.resource_wrapper;

import java.util.Collection;


public interface ResourceWrapper {

	/**
	 * Get the top Term result from the resource.
	 * The top result is determined by the search function for that resource.
	 * @param termQuery String to query the resource for
	 * @return Term object populated with TermRelationships from the resource
	 */
	public Term getTerm(String termQuery);
	
	/**
	 * Get the Term that exactly matches the query parameter.
	 * @param termQuery String to query the resource for
	 * @return Term object populated with TermRelationships from the resource
	 */
	public Term getExactTerm(String termQuery);
	
	/**
	 * Get the top 5 Term results from the resource.
	 * @param termQuery String to query the resource for
	 * @return Collection of Term objects populated with TermRelationships from the resource
	 */
	public Collection<Term> getTerms(String termQuery);
	
	/**
	 * Get the top Term results, amount specified by amountRequested.
	 * @param termQuery String to query the resource for
	 * @param amountRequested the number (int) of Terms to return
	 * @return Collection of Term objects populated with TermReationships from the resource
	 */
	public Collection<Term> getTerms(String termQuery, int amountRequested);
	
	/**
	 * Get all the synonyms for the requested term from the resource.
	 * Must strictly enforce exact matching to prevent wayward synonyms.
	 * @param termQuery String to query the resource for
	 * @return Collection of synonym Strings from the resource
	 */
	public Collection<String> getSynonyms(String termQuery);
	
}
