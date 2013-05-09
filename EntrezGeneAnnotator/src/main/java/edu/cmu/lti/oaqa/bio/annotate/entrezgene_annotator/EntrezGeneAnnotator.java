package edu.cmu.lti.oaqa.bio.annotate.entrezgene_annotator;

import java.util.Collection;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.bio.annotate.entrezgene_wrapper.EntrezGeneWrapper;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Term;
import edu.cmu.lti.oaqa.bio.types.Gene;
import edu.cmu.lti.oaqa.core.nlp.Entity;

public class EntrezGeneAnnotator extends JCasAnnotator_ImplBase {
	
	private EntrezGeneWrapper egw;
	
	/**
	 * Initialize context and Entrez gene wrapper.
	 */
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		this.egw = new EntrezGeneWrapper();
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		// Get the index of Entity types, and an iterator over the index
		AnnotationIndex<Annotation> entityIdx = jcas.getAnnotationIndex(Entity.type);
		FSIterator<Annotation> entityIter = entityIdx.iterator();
		
		// Iterate over the index of Entity's, setting the ref from the resource, where possible
		while (entityIter.hasNext()) {
			Entity ent = (Entity) entityIter.next();
			/*
			 * Try to get the TOP reference from the Entity annotation.  If it hasn't been
			 * set (i.e. == null) then it will throw some sort of Missing Feature exception.
			 * 
			 * If the reference is found, then we don't have to do any work and can continue
			 * the loop.  If it's missing, then we run the code in the catch and set the
			 * reference if we can.
			 */
			try {
				ent.getRef();
			} catch (Exception ex) {
				Term match = this.egw.getExactTerm(ent.getCoveredText());
				if (match != null) {
					TOP ref = getNamedType(jcas, Gene.type, match.getTerm());
					if (ref != null)
						ent.setRef(ref);
					else {
						Gene geneRef = new Gene(jcas);
						geneRef.setName(match.getTerm());
						geneRef.setSynonyms(convertCollectionToStringArray(this.egw.getSynonyms(match.getTerm()), jcas));		
						geneRef.addToIndexes();
						ent.setRef(geneRef);
					}
				}
			}
		}
		
	}
	
	/**
	 * Convert a Collection of Strings into a UIMA StringArray.
	 * This is useful for moving lists of Strings from any other piece of
	 * Java code into some feature structure of a JCas. 
	 * 
	 * @param c a Collection of Strings to copy into a UIMA StringArray
	 * @param jcas The JCas being operated on
	 * @return StringArray containing the Strings of c
	 */
	private StringArray convertCollectionToStringArray(Collection<String> c, JCas jcas) {
		String[] s = new String[c.size()];  // new string array for copying into StringArray
		c.toArray(s);  // Collection -> String[]
		StringArray a = new StringArray(jcas, c.size());
		a.copyFromArray(s, 0, 0, c.size());  // String[] -> StringArray
		return a;
	}

	/**
	 * Search the JCas for a TOP object with the specified type and name.
	 * 
	 * @param jcas JCas being operated on
	 * @param type type to filter look for
	 * @param name value of the 'name' attribute
	 * @return TOP object matching the parameters, null if not found
	 */
	private TOP getNamedType(JCas jcas, int type, String name) {
		// Create iterator over TOP objects from the JCas FSIndex
		FSIterator<TOP> it = jcas.getJFSIndexRepository().getAllIndexedFS(type);
		// Iterate over the TOP objects, attempting to match each item
		while (it.hasNext()) {
			Gene ent = (Gene) it.next();
			if (ent.getName().equalsIgnoreCase(name))
				return ent;
		}
		// Default return value, useful for a pseudo-contains function
		return null;
	}
}
