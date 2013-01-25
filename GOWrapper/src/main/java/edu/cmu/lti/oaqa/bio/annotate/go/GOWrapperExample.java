package edu.cmu.lti.oaqa.bio.annotate.go;

import java.util.Collection;

import edu.cmu.lti.oaqa.bio.resource_wrapper.Term;
import edu.cmu.lti.oaqa.bio.resource_wrapper.TermRelationship;

public class GOWrapperExample {

	public static void main(String[] args) {
		GOWrapper gow = new GOWrapper();
		
		System.out.println("getTerm(MAPK)");
		Term test1 = gow.getTerm("MAPK");
		System.out.println(test1.toString());
		for (TermRelationship tr : test1.getAllTermRelationships())
			System.out.println(tr.toString());
		System.out.println();
		
		System.out.println("getTerm(MAPK, true)");
		try {
			Term test2 = gow.getTerm("MAPK", true);
			System.out.println(test2.toString());
			for (TermRelationship tr : test2.getAllTermRelationships())
				System.out.println(tr.toString());
		} catch (NullPointerException e) {
			System.out.println("No exact match found.");
		}
		System.out.println();
		
		System.out.println("getTerm(flocculation, true)");
		Term test3 = gow.getTerm("flocculation", true);
		System.out.println(test3.toString());
		for (TermRelationship tr : test3.getAllTermRelationships())
			System.out.println(tr.toString());
		System.out.println();
		
		System.out.println("getTerms(acyl, 3)");
		Collection<Term> test4 = gow.getTerms("acyl", 3);
		for (Term t : test4) {
			System.out.println(t.toString());
			for (TermRelationship tr : t.getAllTermRelationships())
				System.out.println(tr.toString());
		}
		System.out.println();
		
		// getSynonyms
		System.out.println("getSynonyms(histidine biosynthetic process)");
		for (String s : gow.getSynonyms("histidine biosynthetic process"))
			System.out.println(s);
		System.out.println();
		
		// getTermByID
		System.out.println("getTermByID(GO:0000133)");
		Term test5 = gow.getTermByID("GO:0000133");
		System.out.println(test5.toString());
		for (TermRelationship tr : test5.getAllTermRelationships())
			System.out.println(tr.toString());
		
		System.out.println("DONE");
	}

}
