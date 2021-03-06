package edu.cmu.lti.oaqa.bio.resource_wrapper.db_wrapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Date;

import edu.cmu.lti.oaqa.bio.resource_wrapper.DBInfo;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Term;
import edu.cmu.lti.oaqa.bio.resource_wrapper.TermRelationship;

/**
 * Object for interfacing with the resources database.
 * @author Collin McCormack (cmccorma)
 * @version 0.1
 */
public class ResourceDBWrapper {
	
	/**
	 * Constructor, currently does no actual initiation.
	 */
	public ResourceDBWrapper() {
		;
	}
	
	/**
	 * Tests for existence of a term in the database.
	 * @param term String to query for
	 * @return boolean, true if present, false if not
	 * @throws SQLException
	 */
	public boolean termExists(String term) throws SQLException {
		Connection conn = null;
		boolean result = false;
		try {
			conn = this.getDBConnection();
			String query = "SELECT COUNT(*) FROM terms WHERE term=\"" + term + "\";";
			
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			rs.next();
			
			if (rs.getInt(1) == 1)
				return true;
			else
				return false;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		return result;
	}
	
	/**
	 * @see #termExists(String)
	 */
	public boolean termExists(Term term) throws SQLException {
		return this.termExists(term.getTerm());
	}
	
	/**
	 * Insert a new term into the database.
	 * Term existence is tested before insertion, no action is taken if the term already exists.
	 * @param term Term to insert (must be unique in the db)
	 * @throws SQLException Thrown in the case of a database error or bad SQL statement
	 */
	public void insertTerm(String term) throws SQLException {
		Connection conn = null;
		try {
			conn = this.getDBConnection();
			String query = "INSERT INTO terms VALUES (\"" + term + "\");";
			
			// Test to see if the term is already in the database
			// Only insert the term if it is NOT present
			if (!this.termExists(term)) {
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(query);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
	}
	
	/**
	 * @see #insertTerm(String)
	 */
	public void insertTerm(Term term) throws SQLException {
		this.insertTerm(term.getTerm());
	}
	
	/**
	 * Delete an existing term in the database.
	 * Checks for existence of term before attempting deletion.  If the term is not in the database, no action is taken.
	 * Term deletions cascade to the 'term' and 'valueTerm' columns in the 'termRelationships' table!
	 * @param term String to delete from the database
	 * @throws SQLException Thrown in the case of a database error or bad SQL statement
	 */
	public void deleteTerm(String term) throws SQLException {
		Connection conn = null;
		// DELETE cascades to TermRelationship's 'term' and 'value'
		String query = "DELETE FROM terms WHERE term = \"" + term + "\";";
		
		try {
			conn = this.getDBConnection();
			
			if (this.termExists(term)) {
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(query);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
	}
	
	/**
	 * @see #deleteTerm(String)
	 */
	public void deleteTerm(Term term) throws SQLException {
		this.deleteTerm(term.getTerm());
	}
	
	/**
	 * This is a vague method.  It should ONLY be used to look for ID's as those are guaranteed to be unique.  
	 * It will probably fail spectacularly for anything else.
	 * It will only return the first result of the query.
	 * @param relationship String to query on relationship column
	 * @param toTerm String value to query on valueTerm column
	 * @return String term if found, else null
	 * @throws SQLException
	 */
	public String getTermByRelationshipAndValue(String relationship, String toTerm) throws SQLException {
		String query = "SELECT term FROM termrelationships WHERE relationship=\""+ relationship + "\" AND value=\"" + toTerm + "\";";
		Connection conn = null;
		try {
			conn = this.getDBConnection();
			
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next())
				return rs.getString(1);
			else
				return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			conn.close();
		}
	}
	
	/**
	 * Tests for whether a term is part of a relationship in the database.
	 * @param term String to query on
	 * @return true if the term has relationships, false if it doesn't (or on error)
	 * @throws SQLException Thrown in the case of a database error or bad SQL statement
	 */
	public boolean hasRelationships(String term) throws SQLException {
		String query = "SELECT COUNT(*) FROM termrelationships WHERE term=\"" + term + "\"";
		Connection conn = null;
		try {
			conn = this.getDBConnection();
			
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			rs.next();
			int count = rs.getInt(1);
			if (count > 0)
				return true;
			else
				return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			conn.close();
		}
	}

	/**
	 * @see #hasRelationships(String)
	 */
	public boolean hasRelationships(Term term) throws SQLException {
		return this.hasRelationships(term.getTerm());
	}
	
	/**
	 * @see #relationshipExists(String fromTerm, String relationship, String toTerm)
	 * @param tr TermRelationship to test for
	 * @return true if it exists in the database, false if not (or on error)
	 * @throws SQLException Thrown in the case of a database error or bad SQL statement
	 */
	public boolean relationshipExists(TermRelationship tr) throws SQLException {
		return this.relationshipExists(tr.getFromTerm(), tr.getRelationship(), tr.getToTerm());
	}
	
	/**
	 * Tests for the existence of a term relationship in the database.
	 * @param fromTerm
	 * @param relationship
	 * @param toTerm
	 * @return true if it exists in the database, false if not (or on error)
	 * @throws SQLException Thrown in the case of a database error or bad SQL statement
	 */
	public boolean relationshipExists(String fromTerm, String relationship, String toTerm) throws SQLException {
		Connection conn = null;
		// value in DB cannot/should not be null, so it won't be there
		if (toTerm == null)
			return false;
		if (toTerm.length() > 255)
			toTerm = toTerm.substring(0, 255);
		String query = "SELECT COUNT(*) FROM termrelationships WHERE term=\"" + fromTerm + "\" AND relationship=\"" 
							+ relationship + "\" AND value=\"" + toTerm + "\";";
		
		try {
			conn = this.getDBConnection();
			Statement stmt = conn.createStatement();
			
			ResultSet rs = stmt.executeQuery(query);
			rs.next();
			if (rs.getInt(1) == 1)
				return true;
			else
				return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			conn.close();
		}
	}
	
	/**
	 * Retrieves all term relationships from the database where the term parameter is the primary term (i.e. in the 'term' column).
	 * @param term String to query on
	 * @return ArrayList of TermRelationship's
	 * @throws SQLException Thrown in the case of a database error or bad SQL statement
	 */
	public ArrayList<TermRelationship> getRelationships(String term) throws SQLException {
		Connection conn = null;
		String query = "SELECT term, relationship, value, source, confidence, parentTerm FROM " +
							"termrelationships WHERE term=\"" + term + "\";";
		ArrayList<TermRelationship> trList = new ArrayList<TermRelationship>();
		
		try {
			conn = this.getDBConnection();
			Statement stmt = conn.createStatement();
			
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				trList.add(new TermRelationship(rs.getString("term"), rs.getString("relationship"), rs.getString("value"), 
												rs.getDouble("confidence"), rs.getString("source"), rs.getString("parentTerm")));
			}
			
			return trList;
		} catch (Exception e) {
			e.printStackTrace();
			return trList;
		} finally {
			conn.close();
		}
	}
	
	/**
	 * @see #getRelationships(String)
	 */
	public ArrayList<TermRelationship> getRelationships(Term term) throws SQLException {
		return this.getRelationships(term.getTerm());
	}
	
	/**
	 * Retrieve all TermRelationship rows that were last updated before the datetime parameter.
	 * (Should be used for updating the database.)
	 * @param datetime 
	 * @return ArrayList of TermRelationship's
	 * @throws SQLException Thrown in the case of a database error or bad SQL statement
	 */
	public ArrayList<TermRelationship> getRelationshipsOlderThan(Date datetime) throws SQLException {
		Connection conn = null;
		String query = "SELECT term, relationship, value, source, confidence, parentTerm FROM " +
							"termrelationships WHERE last_updated > " + datetime.toString() + ";";
		ArrayList<TermRelationship> trList = new ArrayList<TermRelationship>();
		
		try {
			conn = this.getDBConnection();
			Statement stmt = conn.createStatement();
			
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				trList.add(new TermRelationship(rs.getString("term"), rs.getString("relationship"), rs.getString("value"), 
												rs.getDouble("confidence"), rs.getString("source"), rs.getString("parentTerm")));
			}
			
			return trList;
		} catch (Exception e) {
			e.printStackTrace();
			return trList;
		} finally {
			conn.close();
		}
	}
	
	/**
	 * Inserts a new TermRelationship into the database.
	 * Does NOT check for the existence of the primary term!
	 * @param fromTerm
	 * @param relationship
	 * @param toTerm
	 * @param confidence
	 * @param source
	 * @param parentTerm (null allowable)
	 * @throws SQLException Thrown in the case of a database error or bad SQL statement
	 */
	public void insertRelationship(String fromTerm, String relationship, String toTerm, double confidence, String source, String parentTerm) throws SQLException {
		Connection conn = null;
		String query = null;
		if (toTerm != null) {
			if (toTerm.length() > 255)
				toTerm = toTerm.substring(0, 255);
			if (parentTerm == null) {
				query = "INSERT INTO termrelationships (term, relationship, value, confidence, source, parentTerm) VALUES (\""
							+ fromTerm + "\", \"" + relationship + "\", \"" + toTerm + "\", " + confidence + ", \"" + source + "\", NULL);";
			}
			else {
				query = "INSERT INTO termrelationships (term, relationship, value, confidence, source, parentTerm) VALUES (\""
					+ fromTerm + "\", \"" + relationship + "\", \"" + toTerm + "\", " + confidence + ", \"" + source + "\", \"" + parentTerm +
					"\");";
			}
			
			try {
				conn = this.getDBConnection();
				
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(query);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				conn.close();
			}
		}
		// if toTerm == null, skip it
	}
	
	/**
	 * Excludes parent term.
	 * @see #insertRelationship(String fromTerm, String relationship, String toTerm, double confidence, String source, String parentTerm)
	 */
	public void insertRelationship(String fromTerm, String relationship, String toTerm, double confidence, String source) throws SQLException {
		this.insertRelationship(fromTerm, relationship, toTerm, confidence, source, null);
	}
	
	/**
	 * @see #insertRelationship(String fromTerm, String relationship, String toTerm, double confidence, String source, String parentTerm)
	 * @param tr
	 * @throws SQLException Thrown in the case of a database error or bad SQL statement
	 */
	public void insertRelationship(TermRelationship tr) throws SQLException {
		this.insertRelationship(tr.getFromTerm(), tr.getRelationship(), tr.getToTerm(), tr.getConfidence(), tr.getSource(), tr.getParentTerm());
	}
	
	/**
	 * Update a relationship in the database.
	 * The old TermRelationship is entirely replaced with the new TermRelationship.  Does not test for similarity between the parameters. 
	 * @param trOld TermRelationship to query for and replace
	 * @param trNew TermRelationship that replaces trOld
	 * @throws SQLException Thrown in the case of a database error or bad SQL statement
	 */
	public void updateRelationship(TermRelationship trOld, TermRelationship trNew) throws SQLException {
		Connection conn = null;
		if (trOld.getToTerm().length() > 255)
			trOld.setToTerm(trOld.getToTerm().substring(0, 255));
		if (trNew.getToTerm().length() > 255)
			trNew.setToTerm(trNew.getToTerm().substring(0, 255));
		String query = "UPDATE termrelationships SET term=\"" + trNew.getFromTerm() + "\", relationship=\"" + trNew.getRelationship() + 
								"\", value=\"" + trNew.getToTerm() + "\", confidence=" + trNew.getConfidence() + ", source=\"" + 
								trNew.getSource() + "\", parentTerm=\"" + trNew.getParentTerm() + "\" WHERE term=\"" + trOld.getFromTerm() + 
								"\" AND relationship=\"" + trOld.getRelationship() + "\" AND value=\"" + trOld.getToTerm() + "\";";
		
		try {
			conn = this.getDBConnection();
			Statement stmt = conn.createStatement();
			
			if (this.relationshipExists(trOld))
				System.out.println("UPDATE!");
				stmt.executeUpdate(query);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
	}
	
	/**
	 * Delete a TermRelationship from the database.
	 * Does not check for existence before attempting deletion!
	 * @param fromTerm
	 * @param relationship
	 * @param toTerm
	 * @throws SQLException Thrown in the case of a database error or bad SQL statement
	 */
	public void deleteRelationship(String fromTerm, String relationship, String toTerm) throws SQLException {
		Connection conn = null;
		if (toTerm == null)
			return; // Exit early because the relationship won't be there
		// trim down to 255 to fit into database field
		if (toTerm.length() > 255)
			toTerm = toTerm.substring(0, 255);
		String query = "DELETE FROM termrelationships WHERE term=\"" + fromTerm + "\" AND relationship=\"" + relationship + "\" AND value=\"" + toTerm + "\";";
		
		try {
			conn = this.getDBConnection();
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(query);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
	}
	
	/**
	 * @see #deleteRelationship(String fromTerm, String relationship, String toTerm)
	 * @param tr
	 * @throws SQLException
	 */
	public void deleteRelationship(TermRelationship tr) throws SQLException {
		this.deleteRelationship(tr.getFromTerm(), tr.getRelationship(), tr.getToTerm());
	}
	
	/**
	 * Executes the supplied query.  Returns the ResultSet for that query.
	 * SHOULD ONLY BE USED FOR DEBUGGING PURPOSES, NEVER IN PRODUCTION CODE!
	 * @param query
	 * @return raw ResultSet
	 * @throws SQLException
	 */
	public ResultSet debugExecQuery(String query) throws SQLException {
		Connection conn = null;
		try {
			conn = this.getDBConnection();
			Statement stmt = conn.createStatement();
			return stmt.executeQuery(query);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			conn.close();
		}
	}
	
	/**
	 * Get a connection to the database.
	 * @return new Connection to the database
	 * @throws Exception
	 */
	private Connection getDBConnection() throws Exception {
		Connection conn = null;
		try {
			Class.forName(DBInfo.dbClass).newInstance();
			conn = DriverManager.getConnection(DBInfo.URL+"resources", DBInfo.userName, DBInfo.password);
		} catch(Exception e) {
			System.out.println("Database connection failed.");
			e.printStackTrace();
			throw new Exception(e);
		}
		return conn;
	}
}
