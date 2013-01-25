package edu.cmu.lti.oaqa.bio.resource_wrapper.cache;

import java.sql.SQLException;
import java.util.ArrayList;

import edu.cmu.lti.oaqa.bio.resource_wrapper.db_wrapper.ResourceDBWrapper;
import edu.cmu.lti.oaqa.bio.resource_wrapper.DBInfo;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Origin;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Term;
import edu.cmu.lti.oaqa.bio.resource_wrapper.TermRelationship;

/**
 * Database Cache Layer.  Uses ResourceDBWrapper to interface with the database.
 * Sits between the ReourceDataAccessObjects and the database, handles all interactions therein. 
 * @author Collin McCormack (cmccorma)
 * @version 0.1.1
 */
public class DBCache {
	private ResourceDBWrapper rdb;
	
	/**
	 * Constructor, automatically supplies the BioQA database login information.
	 */
	public DBCache() {
		this.rdb = new ResourceDBWrapper(new DBInfo("resources"));
	}
	
	/**
	 * Constructor that accepts a DBInfo object of database login information.
	 * @param dbLogin A DBInfo object with login information
	 */
	public DBCache(DBInfo dbLogin) {
		this.rdb = new ResourceDBWrapper(dbLogin);
	}
	
	/**
	 * Is the specified term in the database already ('terms' table)?
	 * @param term String
	 * @return true if it's already there, false if not (or on SQL error)
	 */
	public boolean inCache(String term) {
		try {
			return this.rdb.termExists(term);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * @see #inCache(String)
	 */
	public boolean inCache(Term term) {
		return this.inCache(term.getTerm());
	}
	
	/**
	 * Is the specified TermRelationship in the database already ('termrelationships' table)?
	 * @param tr TermRelationship
	 * @return true if it's already there, false if not (or on SQL error)
	 */
	public boolean inCache(TermRelationship tr) {
		try {
			return this.rdb.relationshipExists(tr);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Is the specified TermRelationship in the database already ('termrelationships' table)?
	 * @param term String subject term component
	 * @param relationship String verb relationship component
	 * @param valueTerm String object term component
	 * @return true if it's already there, false if not (or on SQL error)
	 */
	public boolean inCache(String term, String relationship, String valueTerm) {
		try {
			return this.rdb.relationshipExists(term, relationship, valueTerm);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Is the specified id in the database already?
	 * (relationship="ID" and valueTerm=id)
	 * id should be in the form "Authority:2349789"
	 * @param id the fully qualified ID to look for
	 * @return true if it's already there, false if not (or on SQL error)
	 */
	public boolean IDinCache(String id) {
		try {
			if (this.rdb.getTermByRelationshipAndValue("ID", id) != null)
				return true;
			else
				return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Add a term to the database.  Checks for existence of the term before attempting insertion.
	 * Returns true if the term is in the db (was there already or was inserted), false otherwise
	 * Only the primary term is added to the db here, all relationships are ignored.  If you want to add
	 * the whole Term object, use {@link #addWholeTerm(Term)}.
	 * @param term String to be added to the db
	 * @return boolean for existence of the term in the db
	 * @see #addWholeTerm(Term)
	 */
	private boolean addTerm(String term) {
		try {
			// check to see if term already exists
			if (this.rdb.termExists(term))
				return true;
			else { // add term
				this.rdb.insertTerm(term);
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Add a Term and it's contents to the database.  Checks for existence of the term or it's
	 * constituent TermRelationships before attempting insertion.
	 * Returns true if the term is in the db (was there already or was inserted), false otherwise
	 * @param term Term object to be added to the db
	 * @return boolean array for existence of the Term and TermRelationships
	 */
	public boolean[] addWholeTerm(Term term) {
		boolean[] results = new boolean[term.getAllTermRelationships().size() + 1];
		results[0] = this.addTerm(term.getTerm());
		int count = 0;
		for (TermRelationship tr : term.getAllTermRelationships()) {
			count += 1;
			try {
				if (!this.rdb.relationshipExists(tr))
					this.rdb.insertRelationship(tr);
				results[count] = true;
			} catch (SQLException e) {
				System.out.println(e.toString());
				e.printStackTrace();
				results[count] = false;
			} 
		} 
		return results;
	}
	
	/**
	 * Add a relationship to the database.  Checks for existence of the relationship before attempting insertion.
	 * Automatically inserts terms into Terms table if they're absent.
	 * Returns true if the relationship is in the db (was there already or was inserted), false otherwise
	 * @param tr new TermRelationship
	 * @return boolean for existence of the TermRelationship in the db
	 */
	public boolean addRelationship(TermRelationship tr) {
		try {
			// check to see if relationship already exists
			if (this.rdb.relationshipExists(tr))
				return true;
			else { // add relationship
				// Check existence of terms, add if necessary
				if (!this.rdb.termExists(tr.getFromTerm()))
					this.addTerm(tr.getFromTerm());
				if (!this.rdb.termExists(tr.getToTerm()))
					this.addTerm(tr.getToTerm());
				this.rdb.insertRelationship(tr);
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * Overloaded addRelationship method, includes parent term.
	 * @param term
	 * @param relationship
	 * @param valueTerm
	 * @param confidence
	 * @param origin
	 * @param parentTerm
	 * @return boolean for existence of the TermRelationship in the db
	 * @see #addRelationship(TermRelationship tr)
	 */
	private boolean addRelationship(String fromTerm, String relationship, String toTerm, double confidence, Origin origin, String parentTerm) {
		return this.addRelationship(new TermRelationship(fromTerm, relationship, toTerm, confidence, origin, parentTerm));
	}
	
	/**
	 * @see #addRelationship(TermRelationship tr)
	 */
	public boolean addRelationship(String fromTerm, String relationship, String toTerm, double confidence, Origin origin) {
		return this.addRelationship(new TermRelationship(fromTerm, relationship, toTerm, confidence, origin));
	}
	
	/**
	 * Get all TermRelationships pertaining to the specified term.
	 * @param term String term
	 * @return ArrayList of relevant TermRelationship's, empty on error
	 */
	private ArrayList<TermRelationship> getRelationships(String term) {
		try {
			return this.rdb.getRelationships(term);
		} catch (SQLException e) {
			e.printStackTrace();
			return new ArrayList<TermRelationship>(0);
		}
	}
	
	/**
	 * Get TermRelationships pertaining to the specified term that also originated at the specified source.
	 * @param term String term
	 * @param origin canonical Origin
	 * @return ArrayList of TermRelationships originating from the provided source
	 */
	public ArrayList<TermRelationship> getRelationshipsByOrigin(String term, Origin origin) {
		ArrayList<TermRelationship> allTR = this.getRelationships(term);
		if (origin == Origin.ALL)
			return allTR;
		else {
			ArrayList<TermRelationship> filteredTR = new ArrayList<TermRelationship>();
			for (TermRelationship tr : allTR) {
				if (tr.getOrigin() == origin)
					filteredTR.add(tr);
			}
			return filteredTR;
		}
	}
	
	/**
	 * Convenience method for {@link #getRelationshipsByOrigin(String, Origin)} for Origin.ALL.
	 * @param term String term
	 * @return ArrayList of all TermRelationships pertaining to term, regardless of source
	 */
	public ArrayList<TermRelationship> getAllRelationships(String term) {
		return this.getRelationshipsByOrigin(term, Origin.ALL);
	}
	
	/**
	 * Get a complete Term object from the database.
	 * Tests for whether the primary term exists in the database first (though, ideally, this should be done before this method is called).  If it doesn't exist, null is returned.  
	 * If the term is present then all relationships that originated from the specified origin (Origin.ALL for all)
	 * are gathered and added to the Term, which is returned.
	 * @param termQuery String, the desired term (searched exactly)
	 * @param origin Origin to match
	 * @return complete Term (null on error or no term found)
	 */
	public Term getTerm(String termQuery, Origin origin) {
		Term outputTerm = null;
		try {
			// If database does not contain the desired term then it doesn't exist and return null
			if (!this.rdb.termExists(termQuery))
				return outputTerm;
			// If we've gotten this far the desired term is in the database and we can create the "hollow" Term object
			outputTerm = new Term(termQuery);
			// Fill the Term object with relevant relationships from the database
			if (origin == Origin.ALL)
				outputTerm.addTermRelationship(this.rdb.getRelationships(outputTerm));
			else { // Do filtering by origin
				ArrayList<TermRelationship> allTR = this.rdb.getRelationships(outputTerm);
				for (TermRelationship tr : allTR) {
					if (tr.getOrigin() == origin)
						outputTerm.addTermRelationship(tr);
				}
			}
		} catch (SQLException sqle) {
			System.out.println("A SQL exception occurred.  Check parameters and/or database status.");
			sqle.printStackTrace();
		}
		return outputTerm;
	}
	
	/**
	 * Same as {@link #getTerm(String, Origin)}, but conveniently specifies Origin.ALL for origin.
	 * @param termQuery String, the desired term (searched exactly)
	 * @return complete Term (null on error or no term found)
	 */
	public Term getTerm(String termQuery) {
		return this.getTerm(termQuery, Origin.ALL);
	}
	
	/**
	 * Retrieves the complete Term object corresponding to the supplied ID
	 * @param id fully-qualified String ID e.g. "Authority:236789"
	 * @param origin Origin to retrieve from, (Origin.ALL for all)
	 * @return complete Term object
	 */
	public Term getTermByID(String id, Origin origin) {
		Term outputTerm = null;
		try {
			String term = this.rdb.getTermByRelationshipAndValue("ID", id);
			outputTerm = this.getTerm(term, origin);
		} catch (SQLException e) {
			System.out.println("A SQL exception occurred.  Check parameters and/or database status.");
			e.printStackTrace();
		}
		return outputTerm;
	}
	
	public ArrayList<String> searchTerms(String termQuery) {
		ArrayList<String> searchResults = null;
		try {
			searchResults = this.rdb.searchTerms(termQuery);
		} catch (SQLException e) {
			System.out.println("A SQL exception occurred.  Check parameters and/or database status.");
			e.printStackTrace();
		}
		return searchResults;
	}
	
	public ArrayList<String> searchTerms(String termQuery, Origin source) {
		ArrayList<String> searchResults = null;
		try {
			searchResults = this.rdb.searchTermRelationships(termQuery, source);
		} catch (SQLException e) {
			System.out.println("A SQL exception occurred.  Check parameters and/or database status.");
			e.printStackTrace();
		}
		return searchResults;
	}
	
	/**
	 * Retrieves the resources database wrapper.  This should only be used for extreme,
	 * one-off cases.  Do not use this to execute commands regularly!
	 * @return ResourceDBWrapper object pointing to the resources database
	 */
	public ResourceDBWrapper getDBWrapper() {
		return this.rdb;
	}
}
