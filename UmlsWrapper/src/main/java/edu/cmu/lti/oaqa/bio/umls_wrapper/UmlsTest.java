package edu.cmu.lti.oaqa.bio.umls_wrapper;
import java.util.*;

public class UmlsTest 
{
    
    /// <summary>
    /// Test driver class for UmlsTermsDAO.
    /// </summary>
    public static void main(String[] args) 
    {
        UmlsTermsDAO umlsDao = new UmlsTermsDAO();

        // Test strings
        String term = "Presenilin";
        String synonym = "aids";
        String noun1 = "benzodiazepine";
        String noun2 = "sedative and hypnotic";
        String verb = "inverse_isa";
        String class1 = "bacterium";
        String class2 = "cell component";
        String classVerb = "affects";
        boolean strictMatch = true;

/**/        
        try
        {
            LogUtil.traceLog(1, "TERM TEST --------------------------------------------------------");

            Term myTerm = new Term();
            myTerm = umlsDao.getTerm(term, strictMatch);

            // Get the relationships belonging to the term.
            ArrayList<TermRelationship> myRelations = new ArrayList<TermRelationship>();
            myRelations = myTerm.getTermRelationships();

            int relationCount = myRelations.size();
 
            LogUtil.traceLog(1, "Number of relations:\t" + String.valueOf(relationCount));

            if (relationCount > 0)
            {
                LogUtil.traceLog(1, "Test Term: " + myTerm.getTermString());
                
                
                LogUtil.traceLog(1, "First relationship:\t" + 
                                    myRelations.get(0).getFromTerm().toString() + " | " +
                                    myRelations.get(0).getRelationship().toString() + " | " +
                                    myRelations.get(0).getToTerm().toString());
    
                LogUtil.traceLog(1, "Last relationship:\t" + 
                        myRelations.get(relationCount - 1).getFromTerm().toString() + " | " +
                        myRelations.get(relationCount - 1).getRelationship().toString() + " | " +
                        myRelations.get(relationCount - 1).getToTerm().toString());             // Remember, arrays are zero based
            }
        }
        catch (Exception ex)
        {
            LogUtil.traceLog(1, "TERM TEST FAILED!", ex);
        }
        
        
        try
        {
            LogUtil.traceLog(1, "SYNONYM TEST -----------------------------------------------------");

            ArrayList<TermRelationship> myRelations = new ArrayList<TermRelationship>();
            myRelations = umlsDao.getTermSynonyms(synonym, strictMatch);
            
            int relationCount = myRelations.size();

            LogUtil.traceLog(1, "Number of synonyms:\t" + String.valueOf(relationCount));

            if (relationCount > 0)
            {
                LogUtil.traceLog(1, "Test Synonym: " + myRelations.get(0).getFromTerm().toString());
                
                LogUtil.traceLog(1, "First synonym:\t" + 
                                    myRelations.get(0).getFromTerm().toString() + " | " +
                                    myRelations.get(0).getRelationship().toString() + " | " +
                                    myRelations.get(0).getToTerm().toString());
    
                LogUtil.traceLog(1, "Last synonym:\t" + 
                        myRelations.get(relationCount - 1).getFromTerm().toString() + " | " +
                        myRelations.get(relationCount - 1).getRelationship().toString() + " | " +
                        myRelations.get(relationCount - 1).getToTerm().toString());             // Remember, arrays are zero based

            }
        }
        catch (Exception ex)
        {
            LogUtil.traceLog(1, "SYNONYM TEST FAILED!", ex);
        }
    

        try
        {
            LogUtil.traceLog(1, "GENE, PROTEIN, DISEASE ONLY SYNONYM TEST -----------------------------------------------------");

            ArrayList<TermRelationship> myRelations = new ArrayList<TermRelationship>();
            myRelations = umlsDao.getProteinGeneDiseaseSynonyms(synonym, strictMatch);
            
            int relationCount = myRelations.size();

            LogUtil.traceLog(1, "Number of synonyms:\t" + String.valueOf(relationCount));

            if (relationCount > 0)
            {
                LogUtil.traceLog(1, "Test Synonym: " + myRelations.get(0).getFromTerm().toString());
                
                LogUtil.traceLog(1, "First synonym:\t" + 
                                    myRelations.get(0).getFromTerm().toString() + " | " +
                                    myRelations.get(0).getRelationship().toString() + " | " +
                                    myRelations.get(0).getToTerm().toString());
    
                LogUtil.traceLog(1, "Last synonym:\t" + 
                        myRelations.get(relationCount - 1).getFromTerm().toString() + " | " +
                        myRelations.get(relationCount - 1).getRelationship().toString() + " | " +
                        myRelations.get(relationCount - 1).getToTerm().toString());             // Remember, arrays are zero based

            }
        }
        catch (Exception ex)
        {
            LogUtil.traceLog(1, "GENE, PROTEIN, DISEASE ONLY SYNONYM TEST FAILED!", ex);
        }
        
        
        try
        {
            LogUtil.traceLog(1, "NOUN_NOUN TEST ---------------------------------------------------");

            ArrayList<TermRelationship> myRelations = new ArrayList<TermRelationship>();
            myRelations = umlsDao.getTermRelationships(UmlsTermsDAO.QueryType.NOUN_NOUN, noun1, noun2, strictMatch);
            
            int relationCount = myRelations.size();

            LogUtil.traceLog(1, "Number of relations:\t" + String.valueOf(relationCount));

            if (relationCount > 0)
            {
                LogUtil.traceLog(1, "Test Nouns: " + myRelations.get(0).getFromTerm().toString() + " | " + myRelations.get(0).getToTerm().toString());
                
                LogUtil.traceLog(1, "First relation:\t" + 
                                    myRelations.get(0).getFromTerm().toString() + " | " +
                                    myRelations.get(0).getRelationship().toString() + " | " +
                                    myRelations.get(0).getToTerm().toString());
    
                LogUtil.traceLog(1, "Last relation:\t" + 
                        myRelations.get(relationCount - 1).getFromTerm().toString() + " | " +
                        myRelations.get(relationCount - 1).getRelationship().toString() + " | " +
                        myRelations.get(relationCount - 1).getToTerm().toString());             // Remember, arrays are zero based
            }
        }
        catch (Exception ex)
        {
            LogUtil.traceLog(1, "NOUN_NOUN TEST FAILED!", ex);
        }

        
        try
        {
            LogUtil.traceLog(1, "NOUN_VERB TEST ---------------------------------------------------");

            ArrayList<TermRelationship> myRelations = new ArrayList<TermRelationship>();
            myRelations = umlsDao.getTermRelationships(UmlsTermsDAO.QueryType.NOUN_VERB, noun1, verb, strictMatch);
            
            int relationCount = myRelations.size();

            LogUtil.traceLog(1, "Number of relations:\t" + String.valueOf(relationCount));

            if (relationCount > 0)
            {
                LogUtil.traceLog(1, "Test Noun and Verb: " + myRelations.get(0).getFromTerm().toString() + " | " + myRelations.get(0).getRelationship().toString());
                
                LogUtil.traceLog(1, "First relation:\t" + 
                                    myRelations.get(0).getFromTerm().toString() + " | " +
                                    myRelations.get(0).getRelationship().toString() + " | " +
                                    myRelations.get(0).getToTerm().toString());
    
                LogUtil.traceLog(1, "Last relation:\t" + 
                        myRelations.get(relationCount - 1).getFromTerm().toString() + " | " +
                        myRelations.get(relationCount - 1).getRelationship().toString() + " | " +
                        myRelations.get(relationCount - 1).getToTerm().toString());             // Remember, arrays are zero based
            }
        }
        catch (Exception ex)
        {
            LogUtil.traceLog(1, "NOUN_VERB TEST FAILED!", ex);
        }
        
       
        try
        {
            LogUtil.traceLog(1, "RELATION LIST TEST -----------------------------------------------");

            ArrayList<String> myVerbList = new ArrayList<String>();
            myVerbList = umlsDao.getRelationList();
            
            int relationCount = myVerbList.size();

            LogUtil.traceLog(1, "Number of verbs:\t" + String.valueOf(relationCount));

            if (relationCount > 0)
            {
                LogUtil.traceLog(1, "First relation verb:\t" +
                        myVerbList.get(0).toString());
    
                LogUtil.traceLog(1, "Last relation verb:\t" + 
                        myVerbList.get(relationCount - 1).toString());
            }
        }
        catch (Exception ex)
        {
            LogUtil.traceLog(1, "RELATION LIST TEST FAILED!", ex);
        }


/**/        

        
        try
        {
            LogUtil.traceLog(1, "CLASS NOUN TEST --------------------------------------------------");

            ArrayList<ClassRelationship> myRelations = new ArrayList<ClassRelationship>();
            myRelations = umlsDao.getClassRelationships(UmlsTermsDAO.QueryType.NOUN, class1, "", strictMatch);
            
            int relationCount = myRelations.size();

            LogUtil.traceLog(1, "Number of relations:\t" + String.valueOf(relationCount));

            LogUtil.traceLog(1, "Test Noun: " + class1);
            
            if (relationCount > 0)
            {
                LogUtil.traceLog(1, "First relation:\t" + 
                                    myRelations.get(0).getFrom().toString() + " | " +
                                    myRelations.get(0).getRelationship().toString() + " | " +
                                    myRelations.get(0).getTo().toString());
    
                LogUtil.traceLog(1, "Last relation:\t" + 
                        myRelations.get(relationCount - 1).getFrom().toString() + " | " +
                        myRelations.get(relationCount - 1).getRelationship().toString() + " | " +
                        myRelations.get(relationCount - 1).getTo().toString());             // Remember, arrays are zero based
            }
        }
        catch (Exception ex)
        {
            LogUtil.traceLog(1, "CLASS NOUN_NOUN TEST FAILED!", ex);
        }

                
        try
        {
            LogUtil.traceLog(1, "CLASS NOUN_NOUN TEST ---------------------------------------------");

            ArrayList<ClassRelationship> myRelations = new ArrayList<ClassRelationship>();
            myRelations = umlsDao.getClassRelationships(UmlsTermsDAO.QueryType.NOUN_NOUN, class1, class2, strictMatch);
            
            int relationCount = myRelations.size();

            LogUtil.traceLog(1, "Number of relations:\t" + String.valueOf(relationCount));

            LogUtil.traceLog(1, "Test Nouns: " + class1 + " | " + class2);

            if (relationCount > 0)
            {
                LogUtil.traceLog(1, "First relation:\t" + 
                                    myRelations.get(0).getFrom().toString() + " | " +
                                    myRelations.get(0).getRelationship().toString() + " | " +
                                    myRelations.get(0).getTo().toString());
    
                LogUtil.traceLog(1, "Last relation:\t" + 
                        myRelations.get(relationCount - 1).getFrom().toString() + " | " +
                        myRelations.get(relationCount - 1).getRelationship().toString() + " | " +
                        myRelations.get(relationCount - 1).getTo().toString());             // Remember, arrays are zero based
            }
        }
        catch (Exception ex)
        {
            LogUtil.traceLog(1, "CLASS NOUN_NOUN TEST FAILED!", ex);
        }
        
        
        try
        {
            LogUtil.traceLog(1, "CLASS NOUN VERB TEST ---------------------------------------------");

            ArrayList<ClassRelationship> myRelations = new ArrayList<ClassRelationship>();
            myRelations = umlsDao.getClassRelationships(UmlsTermsDAO.QueryType.NOUN_VERB, class1, classVerb, strictMatch);
            
            int relationCount = myRelations.size();

            LogUtil.traceLog(1, "Number of relations:\t" + String.valueOf(relationCount));

            LogUtil.traceLog(1, "Test Noun and Verb: " + class1 + " | " + classVerb);
            
            if (relationCount > 0)
            {
                LogUtil.traceLog(1, "First relation:\t" + 
                                    myRelations.get(0).getFrom().toString() + " | " +
                                    myRelations.get(0).getRelationship().toString() + " | " +
                                    myRelations.get(0).getTo().toString());
    
                LogUtil.traceLog(1, "Last relation:\t" + 
                        myRelations.get(relationCount - 1).getFrom().toString() + " | " +
                        myRelations.get(relationCount - 1).getRelationship().toString() + " | " +
                        myRelations.get(relationCount - 1).getTo().toString());             // Remember, arrays are zero based
            }
        }
        catch (Exception ex)
        {
            LogUtil.traceLog(1, "CLASS NOUN_VERB TEST FAILED!", ex);
        }


        try
        {
            LogUtil.traceLog(1, "CO-OCCURRENCE TEST ---------------------------------------------");

            ArrayList<String> myCooccurringTerms = new ArrayList<String>();
            myCooccurringTerms = umlsDao.getCooccurringTerms(term, true);
            
            int relationCount = myCooccurringTerms.size();

            LogUtil.traceLog(1, "Number of co-occurring terms:\t" + String.valueOf(relationCount));

            LogUtil.traceLog(1, "Test term: " + term);
            
            if (relationCount > 0)
            {
                LogUtil.traceLog(1, "First co-occurring term:\t" + 
                                    myCooccurringTerms.get(0).toString());
    
                LogUtil.traceLog(1, "Last co-occurring term:\t" + 
                        myCooccurringTerms.get(relationCount - 1).toString());
            }
        }
        catch (Exception ex)
        {
            LogUtil.traceLog(1, "CO-OCCURRENCE TEST FAILED!", ex);
        }
        
/**/        

        
        LogUtil.traceLog(1, "TEST COMPLETE --------------------------------------------------------");
    }
}   // End Class
