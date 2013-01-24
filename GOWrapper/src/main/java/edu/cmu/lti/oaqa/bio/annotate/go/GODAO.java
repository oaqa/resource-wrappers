package edu.cmu.lti.oaqa.bio.annotate.go;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;

import edu.cmu.lti.oaqa.bio.resource_wrapper.Entity;
import edu.cmu.lti.oaqa.bio.resource_wrapper.ID;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Relation;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Origin;
import edu.cmu.lti.oaqa.bio.resource_wrapper.obo.OBOGraph;
import edu.cmu.lti.oaqa.bio.resource_wrapper.obo.OBONode;

/**
 * Wraps a Gene Ontology file.
 * 
 * @author Collin McCormack (cmccorma)
 * @version 0.1.1
 * @see OBOGraph
 */
public class GODAO implements edu.cmu.lti.oaqa.bio.resource_wrapper.resource_dao.ResourceDataAccessObject {

	private OBOGraph graph;
	
	/**
	 * Default constructor.  Reads a the GO file checked into SVN and constructs an internal graph.
	 * @throws FileNotFoundException
	 */
	public GODAO() throws FileNotFoundException {
		this.graph = new OBOGraph(new FileReader(new File("resources\\gene_ontology.obo")));
	}
	
	/**
	 * Reads the supplied obo file and constructs an internal graph.
	 * @param oboFile GO .obo file
	 * @throws FileNotFoundException
	 */
	public GODAO(File oboFile) throws FileNotFoundException {
		this.graph = new OBOGraph(new FileReader(oboFile));
	}
	
	/**
	 * Converts an OBONode to a ResourceWrapper Entity.  Mostly used when returning results from the OBOGraph to another module.
	 * @param node
	 * @return
	 */
	private Entity convertNodeToEntity(OBONode node) {
		Entity retEnt = new Entity(Origin.GO);
		///// Map fields and attributes to Entity /////
		// Set name from term of node
		retEnt.setName(node.getName());
		// Set definition from GO 'def'
		retEnt.setDefinition(node.getDefinition());
		// Set ID from GO ID, alt_id's
		retEnt.addID(new ID("GO", node.getId()));
		if (node.hasAttribute("alt id")) {
			for (String altId : node.getAttribute("alt id"))
				retEnt.addID(new ID("GO", altId));
		}
		// Set Synonyms
		for (String syn : node.getAllSynonyms())
			retEnt.addSynonym(syn);
		// Set Relations
		if (node.hasAttribute("relationship")) {
			for (String rltsp : node.getAttribute("relationship")) {
				String relation = rltsp.substring(0, rltsp.indexOf(' '));
				String relationID = rltsp.substring(rltsp.indexOf(' ') + 1);
				retEnt.addRelation(new Relation(relation, relationID));
			}
		}
		if (node.hasAttribute("is a")) {
			for (String isaID : node.getAttribute("is a")) {
				retEnt.addRelation(new Relation("is a", isaID));
			}
		}
		// Set Type
		retEnt.setType(node.getType());
		// Set Source
		retEnt.setOrigin(Origin.GO);
		return retEnt;
	}
	/**
	 * Searches names, synonyms, and definitions within GO.  The highest scoring Entity's are returned (there may be multiple in the case of a tie).
	 * @param query search String (case-insensitive)
	 * @return ArrayList of matching Entities
	 */
	public Collection<Entity> getEntities(String query) {
		ArrayList<OBONode> searchResults = this.graph.search(query);
		ArrayList<Entity> entityResults= new ArrayList<Entity>(searchResults.size());
		for (OBONode node : searchResults)
			entityResults.add(convertNodeToEntity(node));
		return entityResults;
	}

	/**
	 * If exactMatch is true, search names and synonyms for an exact match.  Otherwise, do a normal getEntities(query) call.
	 * @param query search String
	 * @param exactMatch boolean flag for exact matching
	 * @return ArrayList of matching Entity results (may be empty)
	 */
	public Collection<Entity> getEntities(String query, boolean exactMatch) {
		if (exactMatch) {
			// Try exact-matching name and synonym search
			ArrayList<OBONode> nameResults = this.graph.searchNames(query);
			if (!nameResults.isEmpty()) {
				ArrayList<Entity> entityResults = new ArrayList<Entity>(nameResults.size());
				for (OBONode node : nameResults)
					entityResults.add(convertNodeToEntity(node));
				return entityResults;
			}
			else // Return empty list if no matching results
				return new ArrayList<Entity>(0);
		}
		else {
			return this.getEntities(query);
		}
	}

	/**
	 * Get an Entity by it's GO ID (includes alt_id's).  'id' String must have 'GO' prefix, e.g. 'GO:0005400'.
	 * @param id String of a GO ID
	 * @return Entity corresponding to the GO ID, null if no matches are found
	 */
	public Entity getEntityById(String id) {
		Entity ent = null;
		try {
			ent = convertNodeToEntity(this.graph.getNodeById(id));
		} catch (Exception e) {
			return null;
		}
		return ent;
	}

	/**
	 * Same as getEntityById(String id), but tests the match against the query.
	 * This is actually a bit pointless as it's re-doing the same work done when the ID match is initially found.
	 * @param id String of a GO ID
	 * @param exactMatch boolean flag for exact matching
	 * @return Entity corresponding to the GO ID, null if no matches are found  
	 */
	public Entity getEntityById(String id, boolean exactMatch) {
		Entity ent = this.getEntityById(id);
		if (exactMatch) {
			boolean exact = false;
			for (ID tempID : ent.getIDs()) {
				if (tempID.equals(id)) {
					exact = true;
					break;
				}
			}
			if (exact)
				return ent;
			else
				return null;
		}
		else {
			return ent;
		}
	}

}
