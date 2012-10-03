package edu.cmu.lti.oaqa.bio.umls_wrapper;
import java.util.ArrayList;


// Class that represents a term
/**
 * @author  Tom
 */
public class Term 
{
      /**
     * @uml.property  name="term"
     */
    private String term;
      /**
     * @return
     * @uml.property  name="term"
     */
    public String getTermString()
      {
          return this.term;
      }
      
      /**
     * @param value
     * @uml.property  name="term"
     */
    public void setTerm(String value)
      {
          this.term = value;
      }

    
    private String definition;
    /**
   * @return
   * @uml.property  name="term"
   */
  public String getDefinition()
    {
        return this.definition;
    }
    
    /**
   * @param value
   * @uml.property  name="term"
   */
  public void setDefinition(String value)
    {
        this.definition = value;
    }
    
      /**
     * @uml.property  name="relationships"
     */
    private ArrayList<TermRelationship> relationships;
      /**
     * @param myRelationships
     * @uml.property  name="relationships"
     */
    public void setTermRelationships(ArrayList<TermRelationship> myRelationships)
      {
          this.relationships = myRelationships;
      }
      
      /**
     * @return
     * @uml.property  name="relationships"
     */
    public ArrayList<TermRelationship> getTermRelationships()
      {
          return this.relationships;
      }
}
