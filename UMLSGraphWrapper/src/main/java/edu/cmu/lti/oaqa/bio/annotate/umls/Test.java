package edu.cmu.lti.oaqa.bio.annotate.umls;

import java.util.List;
import java.util.Set;

import edu.cmu.lti.oaqa.bio.annotate.graph.Concept;
import edu.cmu.lti.oaqa.bio.annotate.graph.ConceptBundle;
import edu.cmu.lti.oaqa.bio.annotate.graph.Relationship;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//termsTest("mutation");
		//synonymsTest("apis mellifera");
		//definitionTest("human");
		//typeOfTest("platypus");
		//metathesaurusRelsTest("field mouse");
		SemanticConnectionsTest("","");
	}
		
		
		private static void termsTest(String string) {
			System.out.println("terms test:\n");
			ConceptBundle results = new GraphQueryEngine().search(string);
			if(results==null){
				System.out.println("No results found for \""+string+"\"");
				return;
			}
			List<String> terms = results.getTerms();
			for(String term:terms){
				System.out.println("term: " + term);
			}
			System.out.println("\n--------------------------------------------------\n");		
	}


		private static void synonymsTest(String string) {
			System.out.println("synonyms test:\n");
			ConceptBundle results = new GraphQueryEngine().search(string);
			if(results==null){
				System.out.println("No results found for \""+string+"\"");
				return;
			}
			List<String> syns = results.getSynonyms();
			for(String syn:syns){
				System.out.println("synonym: " + syn);
			}
			System.out.println("\n--------------------------------------------------\n");		
	}


		private static void definitionTest(String string) {
			System.out.println("definitions test:\n");
			ConceptBundle results = new GraphQueryEngine().search(string);
			if(results==null){
				System.out.println("No results found for \""+string+"\"");
				return;
			}
			List<String> defs = results.getDefinitions();
			for(String def:defs){
				System.out.println("definition: " + def);
			}
			System.out.println("\n--------------------------------------------------\n");		
	}


		private static void typeOfTest(String string) {
			System.out.println("typeOf test:\n");
			ConceptBundle results = new GraphQueryEngine().search(string);
			if(results==null){
				System.out.println("No results found for \""+string+"\"");
				return;
			}
			UMLSConcept concept = (UMLSConcept) results.getConcepts().get(0);
			String fromConceptTerm = concept.getTermsNoRepeats().get(0);
			for(Relationship rel:((UMLSConcept) concept).getTypeOfs()){
				String relLabel = rel.getRelationshipLabel();
				UMLSConcept toConcept = (UMLSConcept) rel.getToConcept();
				String toConceptTerm = (String) toConcept.getRawVertex().getProperty("STY");
				System.out.println("\n" + fromConceptTerm + (" --> ") + relLabel + (" --> ") + toConceptTerm);
			}
			System.out.println("\n--------------------------------------------------\n");		
	}


		private static void metathesaurusRelsTest(String string) {
			System.out.println("Metathesaurus relationships test:\n");
			ConceptBundle results = new GraphQueryEngine().search(string);
			if(results==null){
				System.out.println("No results found for \""+string+"\"");
				return;
			}
			UMLSConcept concept = (UMLSConcept) results.getConcepts().get(0);
			for(Relationship rel:concept.getConceptRelationships()){
				String fromConceptTerm = concept.getTermsNoRepeats().get(0);
				String relLabel = rel.getRelationshipLabel();
				String toConceptTerm = rel.getToConcept().getTerms().get(0);
				System.out.println("\n" + fromConceptTerm + (" --> ") + relLabel + (" --> ") + toConceptTerm);
			}
			
			System.out.println("\n--------------------------------------------------\n");
					
	}


		private static void SemanticConnectionsTest(String string1,String string2) {
			System.out.println("Semantic Network connections test:\n"); //need to reindex graph for this to work
			Concept concept1 = (UMLSConcept) new GraphQueryEngine().search("human").getConcepts().get(0);
			Concept concept2 = (UMLSConcept) new GraphQueryEngine().search("human").getConcepts().get(0);
			GraphQueryEngine.getSemanticNetworkRelationships(concept1, concept2);
			System.out.println("\n--------------------------------------------------\n");		
	}

}