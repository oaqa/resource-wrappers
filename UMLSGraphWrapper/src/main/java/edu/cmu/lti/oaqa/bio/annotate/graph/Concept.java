/**
 * 
 */
package edu.cmu.lti.oaqa.bio.annotate.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jackm321
 *
 */
public interface Concept {

	public List<String> getDefinition();
	public List<String> getTerms();
	public List<String> getSynonyms(String term);
	public List<Relationship> getAllRelationships();
	public List<Relationship> getTypeOfs();
	public boolean synonymsAreOrdered();
	public boolean backedByDB();

}
