package edu.cmu.lti.oaqa.bio.annotate.entrezgene_dao;

import java.io.IOException;
import java.util.ArrayList;

import org.w3c.dom.Document;

import edu.cmu.lti.oaqa.bio.annotate.entrezgene_dao.EntrezGeneDAO;
import edu.cmu.lti.oaqa.bio.resource_wrapper.*;
import edu.cmu.lti.oaqa.bio.resource_wrapper.xml.*;
import edu.cmu.lti.oaqa.bio.species_mapper.Species;
/**
 * Generic examples for the use of EntrezGeneDAO.
 * 
 * @author Collin McCormack (cmccorma)
 * @version 0.1
 */
public class EntrezGeneDAOExample {
	
	public static void main(String[] args) {
		
		EntrezGeneDAO eg = new EntrezGeneDAO();
		
		// Get first 10 results for "BRCA1" and print the name of the Entities
		System.out.println("getEntities:");
		ArrayList<Entity> results = eg.getEntities("BRCA1");
		for (Entity e : results) {
			System.out.println(e.toString());
		}
		
		// Get exact match to query and print
		System.out.println("getEntities (exactMatch):");
		results = eg.getEntities("BRCA1", true);
		for (Entity e : results) {
			System.out.println(e.toString());
		}
		
		// Get Entity by it's ID and print result (dumb version)
		// This will actually return the wrong result for this id...
		System.out.println("getEntitesById:");
		Entity result = eg.getEntityById("672");
		System.out.println(result.toString());
		
		// Get Entity by it's ID and print result (smart version)
		System.out.println("getEntitiesById (exactMatch):");
		result = eg.getEntityById("672", true);
		System.out.println(result.toString());
		
		System.out.println("getEntities(query, species):");
		results = eg.getEntities("BRCA1", new Species("homo sapiens"));
		for (Entity e : results) {
			System.out.println(e.toString());
		}
		
		// Get type of Entity (from query)
		System.out.println("Get type of Entity from query:");
		results = eg.getEntities("BRCA1", true);
		if (results.size() > 0) {
			Entity z = results.get(0);
			System.out.println(z.getName());
			System.out.println("Type: " + z.getType());
		}
		
		// Get synonyms of Entity (from query)
		System.out.println("Get synonyms of Entity from query:");
		results = eg.getEntities("BRCA1", true);
		if (results.size() > 0) {
			Entity z = results.get(0);
			System.out.println(z.getName() + " Synonyms:");
			for (String syn : z.getSynonyms())
				System.out.println(syn);
		}
		
		
		
		// XML fetch for debugging
		// and convert to XMLTree for ease of use
		System.out.println("XMLTree example:");
		try {
			Document xml = eg.fetchXML("672");
			XMLTree goodXml = new XMLTree(xml);
			System.out.println(goodXml.findFirstNode("Gene-ref_locus").getText());
		} catch (IOException e1) {
			System.out.println();
			e1.printStackTrace();
		}
		
	}
	
}
