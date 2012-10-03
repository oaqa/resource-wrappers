package edu.cmu.lti.oaqa.bio.umls_wrapper;

/**
 * @author  Tom
 */
public class TermRelationship 
{
    // From Term
    private String fromTerm;
    public String getFromTerm()
    {
        return this.fromTerm;
    }
    public void setFromTerm(String term)
    {
        this.fromTerm = term;
    }

    // Relationship
    private String relationship;
    public String getRelationship()
    {
        return this.relationship;
    }
    public void setRelationship(String relationship)
    {
        this.relationship = relationship;
    }
    
    // To Term
    private String toTerm;
    public String getToTerm()
    {
        return this.toTerm;
    }
    public void setToTerm(String term)
    {
        this.toTerm = term;
    }

    // From Definition
    private String fromTermDefinition;
    public String getFromTermDefinition()
    {
        return this.fromTermDefinition;
    }
    public void setFromTermDefinition(String value)
    {
        this.fromTermDefinition = value;
    }

    // To Definition
    private String toTermDefinition;
    public String getToTermDefinition()
    {
        return this.toTermDefinition;
    }
    public void setToTermDefinition(String value)
    {
        this.toTermDefinition = value;
    }
    
    // Source (provenance)
    private String source;
    public String getSource()
    {
        return this.source;
    }
    public void setSource(String source)
    {
        this.source = source;
    }

    // Relationship confidence
    private Double confidence;
    public Double getConfidence()
    {
        if (this.confidence == null)
        {
            return 0.0;
        }
        else
        {
            return this.confidence;
        }
    }
    public void setConfidence(Double confidence)
    {
        this.confidence = confidence;
    }
    
    // Last Updated
    private String lastUpdated;
    public String getLastUpdated()
    {
        return this.lastUpdated;
    }
    public void setLastUpdated(String lastUpdated)
    {
        this.lastUpdated = lastUpdated;
    }
}
