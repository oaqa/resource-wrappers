package edu.cmu.lti.oaqa.bio.umls_wrapper;

public class ClassRelationship 
{
    private String fromClass;
    public String getFrom()
    {
        return this.fromClass;
    }
    public void setFromClass(String classification)
    {
        this.fromClass = classification;
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
    private String toClass;
    public String getTo()
    {
        return this.toClass;
    }
    public void setToClass(String classification)
    {
        this.toClass = classification;
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
