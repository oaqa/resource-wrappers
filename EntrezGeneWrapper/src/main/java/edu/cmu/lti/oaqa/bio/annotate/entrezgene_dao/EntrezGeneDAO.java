package edu.cmu.lti.oaqa.bio.annotate.entrezgene_dao;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.cmu.lti.oaqa.bio.resource_wrapper.Entity;
import edu.cmu.lti.oaqa.bio.resource_wrapper.ID;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Relation;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Origin;
import edu.cmu.lti.oaqa.bio.resource_wrapper.resource_dao.ResourceDataAccessObjectExtended;
import edu.cmu.lti.oaqa.bio.resource_wrapper.xml.XMLNode;
import edu.cmu.lti.oaqa.bio.resource_wrapper.xml.XMLTree;
import edu.cmu.lti.oaqa.bio.species_mapper.Species;

/**
 * A class to wrap the functionality of Entrez Gene E-Utilities, primarily search and fetch.
 * Search and summary results are cached locally for faster retrieval.
 * 
 * See EntrezGeneDAOExample for generic uses of this class.
 * 
 * @author Collin McCormack (cmccorma)
 * @version 0.4
 */
public class EntrezGeneDAO implements ResourceDataAccessObjectExtended {
	
	private static final String baseURL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
	private static final String esearch = baseURL + "esearch.fcgi?db=gene&term=";
	private static final String esummary =  baseURL + "esummary.fcgi?db=gene&id=";
	private static final String summarySuffix = "[gene] AND alive[prop]";
	private static final String efetch = baseURL + "efetch.fcgi?db=gene&retmode=xml&id=";
	private static DocumentBuilderFactory xmlDBF = DocumentBuilderFactory.newInstance();
	
	private HashMap<String,ArrayList<String>> searchCache;
	private HashMap<String,String> idLookupCache;
	
	/**
	 * Creates a new EntrezGene object (creates new caches).
	 */
	public EntrezGeneDAO() {
		this.searchCache = new HashMap<String,ArrayList<String>>();
		this.idLookupCache = new HashMap<String,String>();
	}
	
	/**
	 * Uses the Entrez Gene web service to search the database using the contents of queryTerms.  By default, returns 20 results.</br>
	 * The result can be plugged right into 'fetch' to get Entity's</br>
	 * Search results are cached by url.
	 * 
	 * @param queryTerms	a String query
	 * @return				an ArrayList<String> of EntrezGene UID's, empty ArrayList<String> on error
	 * @throws IOException
	 */
	public ArrayList<String> search(String queryTerms) throws IOException {
		/* Possible Exceptions:
		 * 		UnsupportedEncodingException - URLEncoder
		 * 		MalformedURLException - URL constructor
		 * 		IOException - URL.connect
		 */
		
		// Construct HTTP request URL
		String urlStr = esearch;
		System.out.println("Searching \"" + queryTerms + "\"...");
		urlStr += queryTerms.replace(' ', '+') + summarySuffix;
		System.out.println(urlStr);
		
		if (this.searchCache.containsKey(urlStr)) {
			return this.searchCache.get(urlStr);
		}
		
		URL url = new URL(urlStr);

		// Open URL and get XML result
		InputStream connStream = url.openStream();
		XMLTree xml = null;
		try {
			xml = new XMLTree(parseXmlResult(connStream));
		} catch (NullPointerException ne) {
			return new ArrayList<String>();
		}
		
		// Something went wrong and just return an empty list
		if (xml.getRoot() == null)
			return new ArrayList<String>();
		
		// Parse XML and return ID's
		ArrayList<XMLNode> ids = xml.findNodes("Id");
		ArrayList<String> idList = new ArrayList<String>(ids.size());
		for (XMLNode n : ids) {
			idList.add(n.getText());
		}
		return idList;
	}
	/**
	 * Query the Entrez Gene web service for additional information associated with an ID.
	 * @param id Entrez Gene ID String
	 * @return String name of the item associated with the supplied ID
	 * @throws IOException
	 */
	public String getSummary(String id) throws IOException {
		System.out.println("Retrieving summary for " + id + "...");
		
		// Item already in cache, retrieve it and return it
		if (this.idLookupCache.containsKey(id))
			return this.idLookupCache.get(id);
		
		// Otherwise, retrieve summary
		String urlStr = esummary + id;
		URL url = new URL(urlStr);
		// Read InputStream into XMLTree
		InputStream connStream = url.openStream();
		XMLTree xml = new XMLTree(parseXmlResult(connStream));
		// Find Name of Entity in summary record
		ArrayList<XMLNode> itemNodes = xml.findNodes("Item");
		String result = null;
		for (XMLNode n : itemNodes) {
			if (n.getAttribute("Name").equals("Name")) {
				// Found it, add to cache and return the String
				result = n.getText();
				this.idLookupCache.put(id, result);
				break;
			}
		}
		return result;
	}
	
	/**
	 * Uses the Entrez Gene web service to fetch an entry based on a unique Entrez Gene UID.  
	 * The XML response is parsed and the relevant information extracted into a Entity object, which is returned.
	 * 
	 * @param id the Entrez Gene UID to fetch (String)
	 * @return an Entity object containing the information from the response, null if XMl is absent, malformed, or does not contain a name
	 * @throws IOException
	 */
	public Entity fetch(String id) throws IOException {
		/* Possible Exceptions:
		 * 		UnsupportedEncodingException - URLEncoder
		 * 		MalformedURLException - URL constructor
		 * 		IOException - URL.connect
		 */
		
		System.out.println("Fetching " + id + "...");
		
		XMLTree xml = null;
		
		////////// Open InputStream from Entrez server and read it live //////////
		// Construct URL
		String urlStr = efetch + id;
		URL url = new URL(urlStr);
		
		// Read InputStream
		InputStream connStream = url.openStream();
		
		// Parse XML from stream
		try {
			xml = new XMLTree(parseXmlResult(connStream));
		} catch (NullPointerException e1) {
			System.out.println("Something wrong with the InputStream from Entrez");
			e1.printStackTrace();
			return null;
		}
		
		// Something went wrong, return null
		if (xml.getRoot() == null)
			return null;
		
		// Construct Entity
		Entity ent = new Entity(Origin.ENTREZGENE);
		
		////////// Get name //////////
		String name = "";
		try {
			// Names are most commonly at this tag
			name = xml.findFirstNode("Gene-ref_locus").getText();
		} catch (NullPointerException e) {
			try {
				// Though occasionally they're here, usually for incomplete records.
				name = xml.findFirstNode("Gene-ref_locus-tag").getText();
			} catch (NullPointerException ne) {
				// There's no name at all, return null
				return null;
			}
		}
		ent.setName(name);
		
		// Get Description
		String description = "";
		try {	
			description = xml.findFirstNode("Gene-ref_desc").getText();
			ent.addRelation(new Relation("description", description));
		} catch (NullPointerException e) {
			;
		}
		
		////////// Get definition //////////
		try {
			String definition = xml.findFirstNode("Entrezgene_summary").getText();
			ent.setDefinition(definition);
		} catch (NullPointerException n) {
			// No definition tags are present, so just use the description
			ent.setDefinition(description);
		}
		
		////////// Get Synonyms //////////
		try {
			ArrayList<XMLNode> synNodes =  xml.findNodes("Gene-ref_syn_E");
			for (XMLNode n : synNodes) {
				ent.addSynonym(n.getText());
			}
		} catch (NullPointerException e) {
			System.out.println("No synonyms for " + ent.getName() + "/" + id);
		}
		
		////////// Get ID's //////////
		ent.addID(new ID("EntrezGene", id));
		try {
			HashSet<String> addedIDs = new HashSet<String>();	// to prevent duplicate ID's being added
			XMLNode idRoot = xml.findFirstNode("Gene-ref_db");
			ArrayList<XMLNode> dbTags = idRoot.getChildren(false);
			idRoot = xml.findFirstNode("Entrezgene_unique-keys");
			dbTags.addAll(idRoot.getChildren(false));
			for (XMLNode n : dbTags) {
				String auth = n.findOne("Dbtag_db", false).getText();
				XMLNode authIdNode = n.findOne("Object-id_id", true);
				String authId = null;
				if (authIdNode != null)
					authId = authIdNode.getText();
				else
					authId = n.findOne("Object-id_str", true).getText();
				if (addedIDs.contains(auth+":"+authId))
					continue;
				else {	
					ent.addID(new ID(auth, authId));
					addedIDs.add(auth+":"+authId);
				}
			}
		} catch (NullPointerException e) {
			System.out.println("No ID's for " + ent.getName());
		}
		
		////////// Get relations //////////
		// Get Phenotypes
		ArrayList<XMLNode> geneComments = xml.findNodes("Gene-commentary");
		XMLNode phenotypeRoot = null;
		for (XMLNode n : geneComments) {
			try {
				if (n.findOne("Gene-commentary_heading", false).getText().equals("Phenotypes")) {
					phenotypeRoot = n;
					break;
				}
			} catch (NullPointerException e) {
				continue;
			}
		}
		// if no phenotypes are present, don't try to add any!
		if (phenotypeRoot != null) {
			phenotypeRoot = phenotypeRoot.findOne("Gene-commentary_comment", false);
			ArrayList<XMLNode> phenotypes = phenotypeRoot.getChildren(false);
			for (XMLNode n : phenotypes) {
				try {
					String p = n.findOne("Gene-commentary_heading", false).getText();
					ent.addRelation(new Relation("phenotype", p));
				} catch (NullPointerException e) {
					continue;
				}
			}
		}
		
		// Get Interactions
		ArrayList<XMLNode> intxnNodes = new ArrayList<XMLNode>();
		for (XMLNode n : geneComments) {
			try {
				if (n.findOne("Dbtag_db", true).getText().equals("BIND"))
					intxnNodes.add(n);
			} catch (NullPointerException e) {
				continue;
			}
		}
		for (XMLNode n : intxnNodes) {
			try {
				ent.addRelation(new Relation("interaction", n.findOne("Gene-commentary_text", false).getText()));
			} catch (NullPointerException e) {
				continue;
			}
		}
		
		// Get "codes for" proteins
		try {
			XMLNode protNameRoot = xml.findFirstNode("Prot-ref_name");
			ArrayList<XMLNode> protList = protNameRoot.getChildren(false);
			for (XMLNode n : protList) {
				ent.addRelation(new Relation("codes for protein", n.getText()));
			}
		} catch (NullPointerException e) {
			System.out.println(ent.getName() + " has no proteins.");
		}
		
		////////// Get type //////////
		XMLNode typeNode = xml.findFirstNode("Entrezgene_type");
		ent.setType(typeNode.getAttribute("value") + " gene");
		
		return ent;
	}
	
	/**
	 * Useful method for debugging Entrez Gene responses.  Returns an XML DOM Document.
	 * @param id			the Entrez Gene UID to fetch (String)
	 * @return				XML Document
	 * @throws IOException
	 */
	public Document fetchXML(String id) throws IOException {
		String urlStr = efetch + id;
		URL url = new URL(urlStr);
		
		// Open URL and parse XML result
		InputStream connStream = url.openStream();
		Document xml = parseXmlResult(connStream);
		return xml;
	}
	
	/**
	 * Method for parsing XML responses to DOM Document's (encapsulated here because the Java way of doing it is so obtuse).
	 * @param stream	InputStream of XML information
	 * @return			XML DOM Document
	 */
	private static Document parseXmlResult(InputStream stream) {
		DocumentBuilder xmlReader;
		try {
			xmlReader = xmlDBF.newDocumentBuilder();
			return xmlReader.parse(stream);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Could not read XML sent from Entrez.  Possibly a bad connection.");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * General query for Entrez Gene.
	 * Maximum of 10 Entity's are returned in an ArrayList&ltEntity&gt.  If the query returns no results, an empty ArrayList is returned.
	 * Additionally, it will return null if an IOException occurs.
	 * 
	 * @param query		search terms
	 * @return			An ArrayList&ltEntity&gt containing matching Entity's (max 10 items)
	 */
	public ArrayList<Entity> getEntities(String query) {
		ArrayList<Entity> allEntities = new ArrayList<Entity>();
		ArrayList<String> results;
		// Search Entrez Gene, fail gracefully if it doesn't work.
		try { results = search(query);
		} catch (IOException ioSearch) {
			System.out.println("getEntities("+query+"), couldn't search Entrez gene for '"+query+"'");
			ioSearch.printStackTrace();
			return allEntities; // empty array
		}
		// How many results do we want?  Set size here.
		int size = results.size() < 10 ? results.size() : 10;
		for (int i = 0; i < size; i++) {
			// Try to get each result from the Entrez web servce
			// If we can't, fail gracefully for that entry
			try {
				Entity temp = fetch(results.get(i));
				if (temp != null)
					allEntities.add(temp);
			} catch (IOException e) {
				System.out.println("getEntities("+query+"): Could not retrieve '"+results.get(i)+"' from Entrez.");
				System.out.println("IOException, most likely couldn't contact the server");
				e.printStackTrace();
			}
		}
		return allEntities;
	}
	
	/**
	 * Overloaded version of getEntities that tests for an exact match (case-insensitive).
	 * Exact matching is done for name and synonyms.  Definition, relations, and ID are left out to prevent spurious matches.
	 * If an ID match is desired, use getEntitiesById.
	 * 
	 * @param	query		search terms
	 * @param	exactMatch	boolean flag for an exact match test on each result
	 * @return				ArrayList&ltEntity&gt of matching entities, empty if no results from search, null on error
	 */
	public ArrayList<Entity> getEntities(String query, boolean exactMatch) {
		if (exactMatch) {
				query = query.trim();
				ArrayList<String> results;
				ArrayList<Entity> matchingEntities = new ArrayList<Entity>();
				// Search Entrez Gene, fail gracefully if it doesn't work.
				try { results = search(query);
				} catch (IOException ioSearch) {
					System.out.println("getEntities("+query+","+exactMatch+"), couldn't search Entrez gene for '"+query+"'");
					ioSearch.printStackTrace();
					return matchingEntities; // empty array
				}
				// Look through results for query match
				outer: for (int i = 0; i < results.size(); i++) {
					try {
						// fetch record from Entrez Gene
						Entity tempEntity = fetch(results.get(i));
						if (tempEntity == null)
							continue outer;
						// search name
						if (tempEntity.getName().equalsIgnoreCase(query)) {
							matchingEntities.add(tempEntity);
							continue outer;
						} 
						// search synonyms
						for (String syn : tempEntity.getSynonyms()) {
							if (syn.equalsIgnoreCase(query)) {
								matchingEntities.add(tempEntity);
								continue outer;
							}
						}
					} catch (IOException e) {
						System.out.println("getEntities("+query+","+exactMatch+"): Could not retrieve '"+results.get(i)+"' from Entrez.");
						System.out.println("IOException, most likely couldn't contact the server");
						e.printStackTrace();
					}
				}
				return matchingEntities;
		}
		else
			return getEntities(query);
	}
	
	/**
	 * The same behavior as getEntities(query), but does an additional comparison against the type.
	 * Currently it's a 'dumb' comparison (exact match, not case-sensitive).
	 * 
	 * @param query		search terms
	 * @param aType		type as String
	 * @return			An ArrayList&ltEntity&gt of matching Entity objects
	 * @see Entity
	 */
	public ArrayList<Entity> getEntities(String query, String aType) {
		// TODO type system integration (?)
		ArrayList<Entity> initResults = (ArrayList<Entity>) getEntities(query);
		ArrayList<Entity> results = new ArrayList<Entity>();
		for (Entity e : initResults) {
			if (e.getType().toLowerCase().equals(aType.toLowerCase()))
				results.add(e);
		}
		return results;
	}

	/**
	 * NOTE: This is method does not work very well for Entrez Gene default search, using getEntityById(id, exactMatch=true) is highly advised.
	 * 
	 * Retrieves an Entity via the Entrez Gene web service.
	 * The first result of a search is returned.  If there are no results, then null is returned.
	 * 
	 * @param	id	String representing the id to search for, either a number or in the style of "AUTHORTIY:0001"
	 * @return		An Entity, if no results then null
	 */
	public Entity getEntityById(String id) {
		//TODO if the search is for a Entrez gene id (i.e. [uid]), do something smarter
		// What does the Entrez gene id look like? e.g. UID:###, GENE:### ?
		ArrayList<String> results;
		// Attempt search, fail gracefully if it doesn't work
		try {
			results = search(id);
		} catch (IOException ioSearch) {
			System.out.println("getEntityById("+id+"), couldn't search Entrez gene for '"+id+"'");
			ioSearch.printStackTrace();
			return null;
		}
		// Attempt record fetch, fail gracefully if it doesn't work
		try{
			return fetch(results.get(0));
		} catch (IOException ioFetch) {
			System.out.println("getEntityById("+id+"), couldn't fetch '"+results.get(0)+"' from Entrez Gene");
			ioFetch.printStackTrace();
			return null;
		} catch (IndexOutOfBoundsException idxEx) {
			// This may never be reached, but is here for sanity's sake/legacy
			// The only possible case is if the search returns empty (and not by failing)
			System.out.println("getEntityById("+id+"), no search results for '"+results.get(0)+"'");
			idxEx.printStackTrace();
			return null;
		}
	}

	/**
	 * Retrieves an Entity via the Entrez Gene web service.
	 * If exactMatch is true, then "id" is compared against retrieved ID's until a match is found.  If no match is found, return null.
	 * If exactMatch is false, then the simpler getEntityById(String id) is called.
	 * 
	 * @param	id			String representing the id to search for, either a number or in the style of "AUTHORTIY:0001"
	 * @param	exactMatch	boolean flag for an exact match test on each result
	 * @return				An Entity matching the queried id, if no match then null
	 */
	public Entity getEntityById(String id, boolean exactMatch) {
		if (exactMatch) {
			ArrayList<String> results;
			// Attempt search in EG, fail gracefully if it doesn't work
			try { results = search(id);
			} catch (IOException ioSearch) {
				System.out.println("getEntityById("+id+","+exactMatch+"), couldn't search Entrez gene for '"+id+"'");
				ioSearch.printStackTrace();
				return null;
			}
			Entity correctEntity = null;
			// Look through results for id match
			for (int i = 0; i < 20; i++) {
				// Attempt to fetch record from EG, fail gracefully if it doesn't work
				try {
					correctEntity = fetch(results.get(i));
					if (correctEntity == null)
						continue;
					// Test each ID for match to id, return if it's a hit
					for (ID idObj : correctEntity.getIDs()) {
						int colonLoc = idObj.toString().indexOf(':') + 1;
						if (id.equals(idObj.toString().substring(colonLoc)) || id.equals(idObj.toString())) // Found it!
							return correctEntity;
					}
				} catch (IOException ioFetch) {
					System.out.println("getEntityById("+id+","+exactMatch+"), couldn't fetch '"+results.get(i)+"' from Entrez Gene");
					ioFetch.printStackTrace();
				} catch (IndexOutOfBoundsException idxEx) {
					System.out.println("Exhausted search results (less than 20 results), ID does not correspond to an object in this resource");
					idxEx.printStackTrace();
					return null;
				}
			}
			// All search results up to #20 used and no match found
			System.out.println("All search results up to #20 used and no match found, ID does not correspond to an object in this resource");
			return null;
		}
		else
			return getEntityById(id);
	}

	/**
	 * Not implemented for Entrez Gene.  Entrez Gene is not a graph (or anything like one) and as such does not have easily search-able relationships.
	 * @throws UnsupposrtedOperationException
	 */
	public ArrayList<Entity> getRelatedEntities(Entity eObj, Relation relObj) {
		// TODO getRelatedEntities(Entity, Relation)
		// Not sure how to implement this yet as Entrez Gene is NOT a graph...
		// Maybe use 'advanced' Entrez Gene search for known relation types?
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * General query for Entrez Gene, species parameter is used to narrow results to only the species specified.
	 * Otherwise, behaves like getEntities(query)
	 * 
	 * @param query		A String of query terms
	 * @param species	A Species object, as is returned by SpeciesMapper
	 */
	public ArrayList<Entity> getEntities(String query, Species species) {
		ArrayList<Entity> allEntities = new ArrayList<Entity>();
		query = species.getName() + "[Organism] " + query;
		ArrayList<String> results;
		// Attempt search in EG, fail gracefully if it doesn't work
		try{ results = search(query);
		} catch (IOException ioSearch) {
			System.out.println("getEntities("+query+","+species+"), couldn't search Entrez gene for '"+query+"'");
			ioSearch.printStackTrace();
			return allEntities;
		}
		// Max of 10 results, fewer if the search returned fewer
		int size = results.size() < 10 ? results.size() : 10;
		for (int i = 0; i < size; i++) {
			// Attempt to fetch record from EG, fail gracefully if it doesn't work
			try {
				Entity temp = fetch(results.get(i));
				if (temp != null)
					allEntities.add(temp);
			} catch (IOException ioFetch) {
				System.out.println("getEntities("+query+","+species+"), couldn't fetch '"+results.get(i)+"' from Entrez Gene");
				ioFetch.printStackTrace();
			}
		}
		return allEntities;
	}

}
