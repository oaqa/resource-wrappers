package edu.cmu.lti.oaqa.bio.annotate.mesh_wrapper;

import java.util.Collection;

import edu.cmu.lti.oaqa.bio.resource_wrapper.Term;
import edu.cmu.lti.oaqa.bio.resource_wrapper.TermRelationship;

public class MeshWrapperExample {

	public static void main(String[] args) {
		MeshWrapper mdao = new MeshWrapper();
		
		System.out.println("getTerm(Alzheimer)");
		Term test1 = mdao.getTerm("Alzheimer");
		System.out.println(test1.toString());
		for (TermRelationship tr : test1.getAllTermRelationships())
			System.out.println(tr.toString());
		System.out.println();
		
		System.out.println("getExactTerm(Alzheimer)");
		try {
			Term test2 = mdao.getExactTerm("Alzheimer");
			System.out.println(test2.toString());
			for (TermRelationship tr : test2.getAllTermRelationships())
				System.out.println(tr.toString());
		} catch (NullPointerException e) {
			System.out.println("No exact match found.");
		}
		System.out.println();
		
		System.out.println("getExactTerm(Prions)");
		Term test3 = mdao.getExactTerm("Prions");
		System.out.println(test3.toString());
		for (TermRelationship tr : test3.getAllTermRelationships())
			System.out.println(tr.toString());
		System.out.println();
		
		System.out.println("getTerms(Polydactyly, 3)");
		Collection<Term> test4 = mdao.getTerms("Polydactyly", 3);
		for (Term t : test4) {
			System.out.println(t.toString());
			for (TermRelationship tr : t.getAllTermRelationships())
				System.out.println(tr.toString());
		}
		System.out.println();
		
		// getSynonyms
		System.out.println("getSynonyms('Genes, APC')");
		for (String s : mdao.getSynonyms("Genes, APC"))
			System.out.println(s);
		
		System.out.println("DONE");
	}
}
