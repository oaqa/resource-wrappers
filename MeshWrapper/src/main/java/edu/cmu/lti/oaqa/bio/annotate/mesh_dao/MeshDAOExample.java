package edu.cmu.lti.oaqa.bio.annotate.mesh_dao;

import java.util.ArrayList;

import edu.cmu.lti.oaqa.bio.resource_wrapper.Entity;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Relation;

public class MeshDAOExample {

	public static void main(String[] args) {
		MeshDAO mw = new MeshDAO();
		
		ArrayList<Entity> results = mw.getEntities("Alzheimer's disease");
		
		if (results.size() > 0) {
			System.out.println("Synonyms for " + results.get(0).getName() + ":");
			for (String s : results.get(0).getSynonyms()) {
				System.out.println("\t" + s);
			}
			System.out.println("Hierarchies for " + results.get(0).getName() + ":");
			for (Relation r : results.get(0).getRelations()) {
				if (r.getProperty().equals("MeSH Hierarchy"))
					System.out.println("\t" + r.getValue());
			}
		}
		else
			System.out.println("No results!");
	}

}
