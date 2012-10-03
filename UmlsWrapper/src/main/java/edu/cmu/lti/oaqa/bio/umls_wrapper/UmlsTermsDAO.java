package edu.cmu.lti.oaqa.bio.umls_wrapper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;

public class UmlsTermsDAO 
{
    
    
    /// <summary>
    /// Initialization
    /// </summary>
    public UmlsTermsDAO() 
    {
        // Constructor
    }

    
    /// <summary>
    /// Supported relationship query types.
    /// </summary>
    public enum QueryType
    {
        NOUN,
        NOUN_NOUN,
        NOUN_VERB,
        NOUN_SYNONYM,
        PROTEIN_GENE_DISEASE_SYNONYM
    }

    
    /// <summary>
    /// Retrieves a "concept" information from the UMLS database and and constructs a Term object for that "concept".
    /// Returns a null object if there was an error retrieving information from the database.
    /// </summary>
    /// <param name="lookupTerm">A string representing the term to retrieve.</param>
    /// <param name="strictMatch">A boolean representing whether to use exact (true) or like (false) matches.</param>
    public Term getTerm(String lookupTerm, boolean strictMatch) throws Exception
    {
        Term myTerm = new Term();
        myTerm.setTerm(lookupTerm);
        
        // Get the term relationships
        ArrayList<TermRelationship> myRelationships = this.getTermRelationships(QueryType.NOUN, lookupTerm, "", strictMatch);
        
        // If the relationships are null, there was an error retrieving from the db and so we return a null term.
        if (myRelationships == null)
        {
           return null;
        }
        
        // If we got a good result set, set the term name to be equal to the database spelling and casing instead of 
        // the query parameter.
        if (myRelationships.size() > 0)
        {
            myTerm.setTerm(myRelationships.get(0).getFromTerm());
            myTerm.setDefinition(myRelationships.get(0).getFromTermDefinition());

        }
        
        // Add the relationship array to the term object.
        myTerm.setTermRelationships(myRelationships);
        
        // Return the term object with all of its relationships.
        return myTerm;        
    }

    
    /// <summary>
    /// Retrieves a collection of TermRelationship relations that are the synonyms of the input term.
    /// Returns an empty object if there are no matches.
    /// Returns a null object if there was an error retrieving information from the database.
    /// </summary>
    /// <param name="lookupTerm">The term to look up synonyms for</param>
    /// <param name="strictMatch">A boolean representing whether to use exact (true) or like (false) matches.</param>
    public ArrayList<TermRelationship> getTermSynonyms(String lookupTerm, boolean strictMatch) throws Exception
    {
        ArrayList<TermRelationship> mySynonyms = this.getTermRelationships(QueryType.NOUN_SYNONYM, lookupTerm, "", strictMatch);
        
        return mySynonyms;
    }
   

    /// <summary>
    /// Retrieves a collection of TermRelationship relations that are the synonyms of the input term.
    /// Synonyms are only returned for terms of type gene, protein or disease.
    /// Returns an empty object if there are no matches.
    /// Returns a null object if there was an error retrieving information from the database.
    /// </summary>
    /// <param name="lookupTerm">The term to look up synonyms for</param>
    /// <param name="strictMatch">A boolean representing whether to use exact (true) or like (false) matches.</param>
    public ArrayList<TermRelationship> getProteinGeneDiseaseSynonyms(String lookupTerm, boolean strictMatch) throws Exception
    {
        ArrayList<TermRelationship> myProteinGeneDiseaseSynonyms = this.getTermRelationships(QueryType.PROTEIN_GENE_DISEASE_SYNONYM, lookupTerm, "", strictMatch);
        
        return myProteinGeneDiseaseSynonyms;
    }
    
    
    /// <summary>
    /// Retrieves a collection of distinct relation verbs from the database.  This query takes some time.
    /// </summary>
    public ArrayList<String> getRelationList() throws Exception
    {
        Connection myConn = null;
        ResultSet myResults = null;

        ArrayList<String> relationVerbs = new ArrayList<String>();

        try
        {
            // First, we get a connection to the database and a create a statement object to execute a query against
            // the database.
            myConn = this.getDbConnection();                    
            Statement myStatement = myConn.createStatement();   

            // We want to time and log the time it took to issue the query
            final long start = System.nanoTime();
            final long end;
            
            String queryString = "SELECT DISTINCT REL.rela AS Relation FROM mrrel AS REL WHERE REL.rela IS NOT NULL;";
            
            // Once we have the connection and statement, we query the database.
            myResults = myStatement.executeQuery(queryString);

            end = System.nanoTime();
            
            // Now that we have the result set from the database, we will fill an array to contain the list of relations
            int rowCount = 0;

            while (myResults.next())
            {
                relationVerbs.add(myResults.getString("Relation"));
                rowCount++;
            }
            
            LogUtil.traceLog(3, "UmlsDAO: QueryString: " + queryString);
            LogUtil.traceLog(2, "UmlsDAO: " + String.valueOf(rowCount) + " rows returned.  Query took " + String.valueOf((double)(end - start) / 1000000000) + " seconds.");
            
        }
        catch (Exception ex)
        {
            // Forces myRelationships to be null in the case of an exception.  This differentiates exceptions
            // from queries with no results.
            relationVerbs = null;
            
            LogUtil.traceLog(1, "Failed querying the UMLS database.", ex);
        }
        finally
        {
            myConn.close(); // Makes sure to close the database connection
        }
        
        return relationVerbs;
    }
    
    
    /// <summary>
    /// Retrieves a collection of TermRelationship relations that match the specified input parameters.
    /// Supports querying relationships for 2 nouns or a noun and verb.  Arguments must be specified in that order.
    /// Returns an empty object if there are no matches.
    /// Returns a null object if there was an error retrieving information from the database.
    /// </summary>
    /// <param name="qType">QueryType enumeration</param>
    /// <param name="arg1">The 1st noun to query for.</param>
    /// <param name="arg2">The 2nd noun or the verb to query for (depends on the qType).</param>
    /// <param name="strictMatch">A boolean representing whether to use exact (true) or like (false) matches.</param>
    public ArrayList<TermRelationship> getTermRelationships(QueryType qType, String arg1, String arg2, boolean strictMatch) throws Exception
    {
        String matchOperator = "=";

        if (!strictMatch)
        {
            matchOperator = "LIKE";
        }
        
        // This is the base query.  There are 4 tables containing all the info for a concept.
        // We join another set to get all of the info for 2 concepts (relations).
        String queryString =    
            "SELECT DISTINCT " +
                    "Term1RelTbl.term1, " +
                    "Term1RelTbl.relation, " +
                    "Con2.str AS term2, " +
                    "Term1RelTbl.term1Aid, " +
                    "Term1RelTbl.relId, " +
                    "Term1RelTbl.term2Aid, " +
                    "Term1RelTbl.relationSrc, " +
                    "Term1RelTbl.relDir, " +
                    "Term1RelTbl.term1Type, " +
                    "Type2.sty AS term2Type, " +
                    "Term1RelTbl.term1Cid, " +
                    "Term1RelTbl.term1Def, " +
                    "Term1RelTbl.term1DefSrc, " +
                    "Term1RelTbl.term2Cid, " +
                    "Def2.def AS term2Def, " +
                    "Def2.sab AS term2DefSrc, " +
                    "Term1RelTbl.term1TypeTreeNum, " +
                    "Type2.stn AS term2TypeTreeNum " +
            "FROM ( " +
                    "SELECT  Con1.aui    AS term1Aid, " + 
                        "Con1.cui    AS term1Cid, " +
                        "Con1.str    AS term1, " +
                        "Def1.def    AS term1Def, " +
                        "Def1.sab    AS term1DefSrc, " +
                        "Type1.sty   AS term1Type, " +
                        "Type1.stn   AS term1TypeTreeNum, " +
                        "Rel.rela    AS relation, " +
                        "Rel.rui     AS relId, " +
                        "Rel.sab     AS relationSrc, " +
                        "Rel.dir     AS relDir, " +
                        "Rel.cui2    AS term2Cid, " +
                        "Rel.aui2    AS term2Aid " +
                    "FROM mrconso AS Con1 " +
                        "INNER JOIN mrdef AS Def1 ON Con1.cui = Def1.cui " +
                        "INNER JOIN mrsty AS Type1 ON Con1.cui = Type1.cui " +
                        "INNER JOIN mrrel AS Rel ON Con1.cui = Rel.cui1 " +
                    "WHERE Con1.ispref = 'y' " +
                        "AND Con1.lat = 'eng' " +
                        "AND Con1.suppress <> 'y' " +
                        "AND Rel.rela IS NOT NULL " +
                        "AND Rel.suppress <> 'y' ";

        switch (qType)
        {
            // Here we're looking for all of the verb-noun relationships that match a noun term.
            case NOUN:
       
                queryString +=  "AND Con1.str " + matchOperator + " '" + arg1 + "'  " +
                                ") AS Term1RelTbl " +
                                "INNER JOIN mrconso AS Con2  ON term2AId = Con2.aui " +
                                "INNER JOIN mrdef   AS Def2  ON Con2.cui = Def2.cui " +
                                "INNER JOIN mrsty   AS Type2 ON Con2.cui = Type2.cui ";
                break;

            // Here we're looking for all of the verbs that relate to 2 noun terms.
            case NOUN_NOUN: 
                queryString +=  "AND Con1.str " + matchOperator + " '" + arg1 + "'  " +
                                ") AS Term1RelTbl " +
                                "INNER JOIN mrconso AS Con2  ON term2AId = Con2.aui " +
                                "INNER JOIN mrdef   AS Def2  ON Con2.cui = Def2.cui " +
                                "INNER JOIN mrsty   AS Type2 ON Con2.cui = Type2.cui " +
                                "WHERE Con2.str " + matchOperator + " '" + arg2 + "' ";
                break;

            // Here we're looking for all of the nouns that are related to a noun-verb relationship.
            case NOUN_VERB:
                queryString +=  "AND Con1.str " + matchOperator + " '" + arg1 + "' " +
                                "AND Rel.rela " + matchOperator + " '" + arg2 + "' " +
                                ") AS Term1RelTbl " +
                                "INNER JOIN mrconso AS Con2  ON term2AId = Con2.aui " +
                                "INNER JOIN mrdef   AS Def2  ON Con2.cui = Def2.cui " +
                                "INNER JOIN mrsty   AS Type2 ON Con2.cui = Type2.cui ";
                break;

            // Synonyms are a special case of noun_verb as there are several verbs that can describe a synonym in UMLS.
            case NOUN_SYNONYM:
                queryString +=  "AND Con1.str " + matchOperator + " '" + arg1 + "'  " +
                                "AND (Rel.rela = 'has_alias' OR Rel.rela = 'mapped_to') " +     // This clause is required for aliases
                                ") AS Term1RelTbl " +
                                "INNER JOIN mrconso AS Con2  ON term2AId = Con2.aui " +
                                "INNER JOIN mrdef   AS Def2  ON Con2.cui = Def2.cui " +
                                "INNER JOIN mrsty   AS Type2 ON Con2.cui = Type2.cui ";
                break;

            // Synonyms are a special case of noun_verb as there are several verbs that can describe a synonym in UMLS.
            case PROTEIN_GENE_DISEASE_SYNONYM:
                    queryString +=  "AND Con1.str " + matchOperator + " '" + arg1 + "'  " +
                                    "AND (Rel.rela = 'has_alias' OR Rel.rela = 'mapped_to') " +     // This clause is required for aliases
                                    "AND (Type1.sty = 'Gene or Genome' OR Type1.sty = 'Disease or Syndrome' OR Type1.sty = 'Amino Acid, Peptide, or Protein') " + // This clause restricts the synonym types returned
                                    ") AS Term1RelTbl " +
                                    "INNER JOIN mrconso AS Con2  ON term2AId = Con2.aui " +
                                    "INNER JOIN mrdef   AS Def2  ON Con2.cui = Def2.cui " +
                                    "INNER JOIN mrsty   AS Type2 ON Con2.cui = Type2.cui ";
                    break;

                
            default:
                LogUtil.traceLog(1, "Unexpected QueryType of:" + qType);
                
                throw new Exception("Unexpected QueryType of:" + qType);
        }
        
        // Sort order
        queryString += "ORDER BY term1, relation, term2 DESC;";
        
        // Get the term relationships
        ArrayList<TermRelationship> myRelationships = this.queryTermRelationships(queryString);
        
        return myRelationships;
    }
   
    
    /// <summary>
    /// Executes a SQL query to return a list of term relationships.
    /// Returns a null object if there was an error retrieving information from the database.
    /// </summary>
    /// <param name="queryString">A query string to execute.</param>
    private ArrayList<TermRelationship> queryTermRelationships(String queryString) throws Exception
    {
        // WARNING!  To prevent SQL injection attacks, make sure this method should only be called from trusted code!

        Connection myConn = null;
        ResultSet myResults = null;

        ArrayList<TermRelationship> myRelationships = new ArrayList<TermRelationship>();
        
        try
        {
            // First, we get a connection to the database and a create a statement object to execute a query against
            // the database.
            myConn = this.getDbConnection();                    
            Statement myStatement = myConn.createStatement();   

            // We want to time and log the time it took to issue the query
            final long start = System.nanoTime();
            final long end;
            
            // Once we have the connection and statement, we query the database.
            myResults = myStatement.executeQuery(queryString);

            end = System.nanoTime();
            
            // Now that we have the result set from the database, we will fill an array to contain all of the 
            // relationships to the query term.
            int rowCount = 0;

            while (myResults.next())
            {
                TermRelationship myRelationship = new TermRelationship();
 
                myRelationship.setFromTerm(myResults.getString("term1"));               // First noun (should be same as lookup value)
                myRelationship.setRelationship(myResults.getString("relation"));        // The relation
                myRelationship.setToTerm(myResults.getString("term2"));                 // Second noun (term that is related to the query term)
                myRelationship.setSource("UMLS/" + myResults.getString("relationSrc")); // The source where the relationship was defined (e.g. NIH, GO, Entrez, etc.)
                myRelationship.setFromTermDefinition(myResults.getString("term1Def"));  // The definition of the 1st noun         
                myRelationship.setToTermDefinition(myResults.getString("term2Def"));    // The definition of the 2nd noun     
                
                // Add the row to the array list
                myRelationships.add(myRelationship);
                
                rowCount++;
            }
            
            LogUtil.traceLog(3, "UmlsDAO: QueryString: " + queryString);
            LogUtil.traceLog(2, "UmlsDAO: " + String.valueOf(rowCount) + " rows returned.  Query took " + String.valueOf((double)(end - start) / 1000000000) + " seconds.");
            
        }
        catch (Exception ex)
        {
            // Forces myRelationships to be null in the case of an exception.  This differentiates exceptions
            // from queries with no results.
            myRelationships = null;
            
            LogUtil.traceLog(1, "Failed querying the UMLS database.", ex);
        }
        finally
        {
            myConn.close(); // Makes sure to close the database connection
        }
        
        return myRelationships;
    }

    
    /// <summary>
    /// Retrieves a collection of ClassRelationship relations that match the specified input parameters.
    /// Supports querying relationships for 2 nouns or a noun and verb.  Arguments must be specified in that order.
    /// Returns an empty object if there are no matches.
    /// Returns a null object if there was an error retrieving information from the database.
    /// </summary>
    /// <param name="qType">QueryType enumeration</param>
    /// <param name="arg1">The 1st noun to query for.</param>
    /// <param name="arg2">The 2nd noun or the verb to query for (depends on the qType).</param>
    /// <param name="strictMatch">A boolean representing whether to use exact (true) or like (false) matches.</param>
    public ArrayList<ClassRelationship> getClassRelationships(QueryType qType, String arg1, String arg2, boolean strictMatch) throws Exception
    {
        String matchOperator = "=";
        
        if (!strictMatch)
        {
            matchOperator = "LIKE";
        }
        
        // The query is complicated because we need to join the definitions table 3 times to get
        // all 3 relationship strings (from, relationship, to).
        String queryString =    "SELECT " +      // This is the outer table (relationship)
                                "stringTbl.fromString, " +
                                "relationTbl.sty_rl AS relation, " +
                                "stringTbl.toString, " +
                                "stringTbl.fromId, " +
                                "stringTbl.relationId, " +
                                "stringTbl.toId, " +
                                "stringTbl.fromDefinition, " +
                                "stringTbl.toDefinition " +
                                "FROM " +       // This is the middle table (to)
                                    "( " +
                                    "SELECT  fromTbl.fromId, " +
                                    "fromTbl.fromString, " +
                                    "fromTbl.fromDefinition, " +
                                    "fromTbl.relationId, " +
                                    "fromTbl.toId, " +
                                    "toTbl.sty_rl AS toString, " +
                                    "toTbl.def AS toDefinition " +
                                    "FROM " +   
                                        "( " +  // This is the inner table (from)
                                        "SELECT rel.ui_sty1 AS fromId,  " +
                                        "def1.sty_rl AS fromString,  " +
                                        "def1.def AS fromDefinition, " +
                                        "rel.ui_rl AS relationId, " +
                                        "rel.ui_sty2 AS toId " +
                                        "FROM srstre1 AS rel " +
                                        "INNER JOIN srdef AS def1 ON rel.ui_sty1 = def1.ui " +
                                        ") " +
                                        "AS fromTbl " +
                                    "INNER JOIN srdef AS toTbl ON fromTbl.toId = toTbl.ui " +
                                    ") " +
                                    "AS stringTbl " +
                                "INNER JOIN srdef AS relationTbl ON stringTbl.relationId = relationTbl.ui ";

        switch (qType)
        {
            // Here we're looking for all of the verb-noun relationships that match a noun term.
            case NOUN:
       
                queryString +=  "WHERE fromString " + matchOperator + " '" + arg1 + "' " +
                                "OR toString " + matchOperator + " '" + arg1 + "' ";
                break;

            // Here we're looking for all of the verbs that relate to 2 noun terms.
            case NOUN_NOUN: 
                queryString +=  "WHERE (fromString " + matchOperator + " '" + arg1 + "' AND toString " + matchOperator + " '" + arg2 + "') " +
                		        "OR (toString " + matchOperator + " '" + arg1 + "' AND fromString " + matchOperator + " '" + arg2 + "')";

                break;

            // Here we're looking for all of the nouns that are related to a noun-verb relationship.
            case NOUN_VERB:
                queryString +=  "WHERE sty_rl " + matchOperator + " '" + arg2 + "' " + 
                                "AND (fromString " + matchOperator + " '" + arg1 + "' OR toString " + matchOperator + " '" + arg1 + "') ";

                break;

            // The synonym case is not related to class queries 
            case NOUN_SYNONYM:
                LogUtil.traceLog(1, "Synonyms are not supported for class queries:" + qType);
                
                throw new Exception("Synonyms are not supported for class queries:" + qType);
                
                
            default:
                LogUtil.traceLog(1, "Unexpected QueryType of:" + qType);
                
                throw new Exception("Unexpected QueryType of:" + qType);
        }

        // Order the results
        queryString += "ORDER BY stringTbl.fromString, relationTbl.sty_rl, stringTbl.toString";
        
        // Get the term relationships
        ArrayList<ClassRelationship> myRelationships = this.queryClassRelationships(queryString);
        
        return myRelationships;
    }
    
    
    /// <summary>
    /// Executes a SQL query to return a list of class relationships.
    /// Returns a null object if there was an error retrieving information from the database.
    /// </summary>
    /// <param name="queryString">A query string to execute.</param>
    private ArrayList<ClassRelationship> queryClassRelationships(String queryString) throws Exception
    {
        // WARNING!  To prevent SQL injection attacks, make sure this method should only be called from trusted code!

        Connection myConn = null;
        ResultSet myResults = null;

        ArrayList<ClassRelationship> myRelationships = new ArrayList<ClassRelationship>();
        
        try
        {
            myConn = this.getDbConnection();                    
            Statement myStatement = myConn.createStatement();   

            final long start = System.nanoTime();
            final long end;
            
            myResults = myStatement.executeQuery(queryString);

            end = System.nanoTime();
            
            int rowCount = 0;

            while (myResults.next())
            {
                ClassRelationship myRelationship = new ClassRelationship();
 
                myRelationship.setFromClass(myResults.getString("fromString"));     // First noun (should be same as lookup value)
                myRelationship.setRelationship(myResults.getString("relation"));    // The relation
                myRelationship.setToClass(myResults.getString("toString"));         // Second noun (term that is related to the query term)
                myRelationship.setSource("UMLS");                                   // The source where the relationship was defined.  Unlike terms, the classes we use are only defined by UMLS
                 
                myRelationships.add(myRelationship);
                
                rowCount++;
            }
            
            LogUtil.traceLog(3, "UmlsDAO: Database: " + LogUtil.jdbcURL.toString());
            LogUtil.traceLog(3, "UmlsDAO: QueryString: " + queryString);
            LogUtil.traceLog(2, "UmlsDAO: " + String.valueOf(rowCount) + " rows returned.  Query took " + String.valueOf((double)(end - start) / 1000000000) + " seconds.");
            
        }
        catch (Exception ex)
        {
            myRelationships = null;
            
            LogUtil.traceLog(1, "Failed querying the UMLS database.", ex);
        }
        finally
        {
            myConn.close(); // Makes sure to close the database connection
        }
        
        return myRelationships;
    }


    /// <summary>
    /// Executes a SQL query to return a list of terms that cooccur with a given term.
    /// </summary>
    /// <param name="lookupTerm">A term to get cooccurring terms for.</param>
    /// <param name="strictMatch">A boolean representing whether to use exact (true) or like (false) matches.</param>
    public ArrayList<String> getCooccurringTerms(String lookupTerm, boolean strictMatch) throws Exception
    {
        String matchOperator = "=";
        
        if (!strictMatch)
        {
            matchOperator = "LIKE";
        }
        
        Connection myConn = null;
        ResultSet myResults = null;

        ArrayList<String> myCooccurringTerms = new ArrayList<String>();
        
        String queryString =    "SELECT DISTINCT " + 
                                    "Con2.str AS term2 " + 
                                "FROM " + 
                                "( " + 
                                    "SELECT  Con1.aui AS term1Aid, " + 
                                            "Con1.cui AS term1Cid, " + 
                                            "Con1.str AS term1, " +
                                            "Coc.cui2 AS term2Cid, " +
                                            "Coc.aui2 AS term2Aid " +
                                    "FROM mrconso AS Con1 " +
                                    "INNER JOIN mrcoc AS Coc ON Con1.cui = Coc.cui1 " +
                                    "WHERE Con1.ispref = 'y' " +
                                    "AND Con1.lat = 'eng' " +
                                    "AND Con1.suppress <> 'y' " +
                                    "AND Con1.str " + matchOperator + "'" + lookupTerm + "' " +
                                ") " +   
                                "AS Term1CocTbl " +
                                "INNER JOIN mrconso AS Con2 ON term2AId = Con2.aui " +
                                "ORDER BY term1, term2 DESC; ";

        try
        {
            myConn = this.getDbConnection();                    
            Statement myStatement = myConn.createStatement();   

            final long start = System.nanoTime();
            final long end;
            
            myResults = myStatement.executeQuery(queryString);

            end = System.nanoTime();
            
            int rowCount = 0;

            while (myResults.next())
            {
                myCooccurringTerms.add(myResults.getString("term2"));
                
                rowCount++;
            }
            
            LogUtil.traceLog(3, "UmlsDAO: Database: " + LogUtil.jdbcURL.toString());
            LogUtil.traceLog(3, "UmlsDAO: QueryString: " + queryString);
            LogUtil.traceLog(2, "UmlsDAO: " + String.valueOf(rowCount) + " rows returned.  Query took " + String.valueOf((double)(end - start) / 1000000000) + " seconds.");
            
        }
        catch (Exception ex)
        {
            LogUtil.traceLog(1, "Failed querying the UMLS database.", ex);
        }
        finally
        {
            myConn.close(); // Makes sure to close the database connection
        }
        
        return myCooccurringTerms;
    }
    
    
    /// <summary>
    /// Gets a connection to a database using the settings defined in the Utilities class.
    /// </summary>
    private Connection getDbConnection() throws Exception
    {
        Connection myConn = null;

        try
        {
            // Use the specified JDBC driver from the settings
            Class.forName (LogUtil.jdbcDriver.toString()).newInstance();  

            // Get the database connection
            myConn = DriverManager.getConnection(   LogUtil.jdbcURL.toString(), 
                                                    LogUtil.userName.toString(), 
                                                    LogUtil.password.toString());
        }
        catch (Exception ex)
        {
            LogUtil.traceLog(1, "Failed getting database connection.", ex);
            
            throw new Exception(ex);
        }
        
        return myConn;
    } 
    
}   // End Class
