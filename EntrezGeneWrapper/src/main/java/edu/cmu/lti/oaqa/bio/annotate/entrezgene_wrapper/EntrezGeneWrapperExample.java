package edu.cmu.lti.oaqa.bio.annotate.entrezgene_wrapper;

import java.util.Collection;

import edu.cmu.lti.oaqa.bio.resource_wrapper.ResourceWrapper;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Term;
import edu.cmu.lti.oaqa.bio.resource_wrapper.TermRelationship;

public class EntrezGeneWrapperExample {

	public static void main(String[] args) {
		ResourceWrapper egWrap = new EntrezGeneWrapper();
		
		// getTerm
		System.out.println("getTerm(BRCA1):");
		Term test1 = egWrap.getTerm("BRCA1");
		System.out.println(test1.toString());
		for (TermRelationship tr : test1.getAllTermRelationships())
			System.out.println(tr.toString());
		System.out.println();
		
		// getTerm w/ exact matching
		System.out.println("getTerm(borca1, true):");
		Term test2 = egWrap.getTerm("borca1", true);
		try {
			System.out.println(test2.toString());
			for (TermRelationship tr : test2.getAllTermRelationships())
				System.out.println(tr.toString());
		} catch (NullPointerException e) {
			;
		}
		System.out.println();
		
		// getTerm w/ exact matching
		System.out.println("getTerm(SHH, true):");
		Term test3 = egWrap.getTerm("SHH", true);
		if (test3 != null) {
			System.out.println(test3.toString());
			for (TermRelationship tr : test3.getAllTermRelationships())
				System.out.println(tr.toString());
		}
		System.out.println();
		
		// getTerms w/ return limit
		System.out.println("getTerms(bard1, 7):");
		Collection<Term> test4 = egWrap.getTerms("bard1", 7);
		for (Term t : test4) {
			System.out.println(t.toString());
			for (TermRelationship tr : t.getAllTermRelationships())
				System.out.println(tr.toString());
		}
		System.out.println();
		
		// getSynonyms
		System.out.println("getSynonyms(apc):");
		for (String s : egWrap.getSynonyms("apc"))
			System.out.println(s);
		
		System.out.println("DONE");
	}

}
