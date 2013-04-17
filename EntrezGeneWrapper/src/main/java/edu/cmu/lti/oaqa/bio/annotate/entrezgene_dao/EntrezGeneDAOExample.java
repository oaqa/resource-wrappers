package edu.cmu.lti.oaqa.bio.annotate.entrezgene_dao;

import java.io.IOException;
import java.util.ArrayList;

import edu.cmu.lti.oaqa.bio.annotate.entrezgene_dao.EntrezGeneDAO;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Entity;

/**
 * Generic examples for the use of EntrezGeneDAO.
 * 
 * @author Collin McCormack
 */
public class EntrezGeneDAOExample {
	
	public static void main(String[] args) {
		EntrezGeneDAO eg = new EntrezGeneDAO();
		
		ArrayList<String> searchResults = new ArrayList<String>(0);
		
		// SEARCH
		try {
			searchResults = eg.search("BRCA1");
			for (String id : searchResults)
				System.out.println(id);
		} catch (NullPointerException npe) {
			System.out.println("XML issue");
			npe.printStackTrace();
		} catch (IOException ioe) {
			System.out.println("Connection issue");
			ioe.printStackTrace();
		}

		// SUMMARY
		if (searchResults.size() != 0) {
			try {
				String name = eg.getName(searchResults.get(0));
				System.out.println(name);
			} catch (IOException ioe) {
				System.out.println("Connection issue");
				ioe.printStackTrace();
			} catch (NullPointerException npe) {
				System.out.println("XML issue");
				npe.printStackTrace();
			}
		}
		
		// FETCH
		// For "APC" in mouse
		try {
			Entity apc = eg.fetch("11789");
			apc.print();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
