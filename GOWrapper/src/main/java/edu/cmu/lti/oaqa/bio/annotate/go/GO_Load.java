package edu.cmu.lti.oaqa.bio.annotate.go;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

import edu.cmu.lti.oaqa.bio.resource_wrapper.DBInfo;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Origin;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Term;
import edu.cmu.lti.oaqa.bio.resource_wrapper.TermRelationship;
import edu.cmu.lti.oaqa.bio.resource_wrapper.cache.DBCache;

/**
 * Loads a Gene Ontology file into the database cache.
 * 
 * @author Collin McCormack (cmccorma)
 * @version 0.1
 */
public class GO_Load {

	public static void main(String[] args) {
		Scanner goScan = null;
		try {
			if (args.length > 0)
				goScan = new Scanner(new FileReader(new File(args[0])));
			else
				goScan = new Scanner(new FileReader(new File("resources\\gene_ontology_ext.obo")));
		} catch (FileNotFoundException FNFex) {
			FNFex.printStackTrace();
			System.out.println("Could not find the requested file.");
		}
			
		// Parse input stream into chunks of text
		System.out.println("Reading file...");
		ArrayList<ArrayList<String>> chunks = new ArrayList<ArrayList<String>>();
		ArrayList<String> currentTextChunk = null;
		while (goScan.hasNextLine()) {
			String line = goScan.nextLine();
			// The tests against null below ensure that the first chunk of declarations will NOT be captured
			if (line.equals("") && currentTextChunk != null) {
				chunks.add(currentTextChunk);
				currentTextChunk = null;
			}
			else if (line.equals("[Term]"))
				currentTextChunk = new ArrayList<String>();
			else if (currentTextChunk != null)
				currentTextChunk.add(line);
		}
		
		// Convert chunks of text to Term's
		System.out.println("Converting .obo into Term objects...");
		ArrayList<Term> terms = new ArrayList<Term>();
		for (ArrayList<String> c : chunks)
			terms.add(textToTerm(c));
		
		// Add to the database
		System.out.println("Adding objects to the database...");
		DBCache dbc = new DBCache(new DBInfo("jdbc:mysql://localhost:3306/bioqa", "root", "bioqa"));
		for (Term t : terms)
			dbc.addWholeTerm(t);
		
		System.out.println("Done");
	}
	
	/**
	 * 
	 * 
	 * See http://www.geneontology.org/GO.format.obo-1_2.shtml for the OBO specification.
	 * @param chunk ArrayList of Strings containing the lines of text from the OBO file
	 * @return a populated Term object
	 */
	private static Term textToTerm(ArrayList<String> chunk) {
		// Find name and create term
		Term term = null;
		for (String line : chunk) {
			// All lines are formatted like "attributeName: attribute data"
			// Parse the attribute name
			String attrName = line.substring(0, line.indexOf(':'));
			// Parse the name of the term
			if (attrName.equals("name")) {
				term = new Term(line.substring(line.indexOf(':') + 2));
				break;
			}
		}
		
		// Iterate again and fill in the TermRelationship's
		for (String line : chunk) {
			String attrName = line.substring(0, line.indexOf(':'));
			
			// Parse the GO ID of the term
			if (attrName.equals("id")) {
				String id = line.substring(line.indexOf(':') + 2);
				TermRelationship tr = new TermRelationship(term.getTerm(), "id", id, 1.0, Origin.GO);
				term.addTermRelationship(tr);
			}
			// Parse the namespace (type) of the term
			else if (attrName.equals("namespace")) {
				String type =line.substring(line.indexOf(':') + 2).replace('_', ' ');
				TermRelationship tr = new TermRelationship(term.getTerm(), "type", type, 1.0, Origin.GO);
				term.addTermRelationship(tr);
			}
			// Parse any alternate ID's for the term
			else if (attrName.equals("alt_id")) {
				String id = line.substring(line.indexOf(':') + 2);
				TermRelationship tr = new TermRelationship(term.getTerm(), "id", id, 1.0, Origin.GO);
				term.addTermRelationship(tr);
			}
			// Parse the definition of the term
			else if (attrName.equals("def")) {
				String def = line.substring(line.indexOf(" \"") + 2, line.indexOf("\" "));
				TermRelationship tr = new TermRelationship(term.getTerm(), "definition", def, 1.0, Origin.GO);
				term.addTermRelationship(tr);
			}
			// Parse any synonyms for the term
			else if (attrName.equals("synonym")) { 
				String beginSynonym = line.substring(line.indexOf(" \"") + 2);
				String synonym = beginSynonym.substring(0, beginSynonym.indexOf('"'));
				// Synonym type parsing, not currently used
				//String synType = beginSynonym.substring(beginSynonym.indexOf('"')+2);
				//synType = synType.substring(0, synType.indexOf(" ["));
				
				TermRelationship tr = new TermRelationship(term.getTerm(), "synonym", synonym, 1.0, Origin.GO);
				term.addTermRelationship(tr);
			}
			// Parse the "is a" (identity) relationships
			else if (attrName.equals("is_a")) {
				String isaID = line.substring(line.indexOf(':') + 2, line.indexOf('!') - 1);
				TermRelationship tr = new TermRelationship(term.getTerm(), "is a", isaID, 1.0, Origin.GO);
				term.addTermRelationship(tr);
			}
			/* Parse the "intersection_of" tag for a term
			 * This tag indicates that this term is equivalent to the intersection of several other terms.
			 * The value is either a term id, or a relationship type id, a space, and a term id.
			 */ 
			else if (attrName.equals("intersection_of")) {
				String intsxn = line.substring(line.indexOf(':') + 2, line.indexOf('!') - 1);
				TermRelationship tr = new TermRelationship(term.getTerm(), "intersection of", intsxn, 1.0, Origin.GO);
				term.addTermRelationship(tr);
			}
			// Parse the relationships of a term
			else if (attrName.equals("relationship")) {
				String relationWithID = line.substring(line.indexOf(':') + 2, line.indexOf('!') - 1); 
				String relation = relationWithID.substring(0, relationWithID.indexOf(' '));
				String relationID = relationWithID.substring(relationWithID.indexOf(' ') + 1);
				
				TermRelationship tr = new TermRelationship(term.getTerm(), relation, relationID, 1.0, Origin.GO);
				term.addTermRelationship(tr);
			}
			else if (attrName.equals("is_obsolete")) {
				TermRelationship tr = new TermRelationship(term.getTerm(), "is obsolete", "true", 1.0, Origin.GO);
				term.addTermRelationship(tr);
			}
			else if (attrName.equals("replaced_by")) {
				String replacedID = line.substring(line.indexOf(':') + 2);
				TermRelationship tr = new TermRelationship(term.getTerm(), "replaced by", replacedID, 1.0, Origin.GO);
				term.addTermRelationship(tr);
			}
			// Parse cross-references for a term
			else if (attrName.equals("xref")) {
				continue;
				//this.addAttribute("xref", line.substring(line.indexOf(':') + 2));
			}
			else {
				continue;
			}
			/* Doesn't (currently) handle:
			 * 		xref (cross-reference to another system, should implement in the future)
			 *		comment (human comment, don't care)
			 * 		disjoint_from (don't care)
			 * 		consider (similar to replaced_by, but requires human analysis)
			 * 		union_of (not contained in original test file)
			 * 		subset (not tracking them via subsetdef)
			 */
		}
		
		return term;
	}

}
