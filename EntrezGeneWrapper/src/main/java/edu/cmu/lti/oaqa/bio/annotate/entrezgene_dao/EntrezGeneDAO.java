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
import edu.cmu.lti.oaqa.bio.resource_wrapper.xml.XMLNode;
import edu.cmu.lti.oaqa.bio.resource_wrapper.xml.XMLTree;

/**
 * A class to wrap the functionality of Entrez Gene E-Utilities,
 * primarily search and fetch.  Search and name results are cached
 * locally for faster retrieval.
 * 
 * See EntrezGeneDAOExample for generic uses of this class.
 * 
 * @author Collin McCormack
 */
public class EntrezGeneDAO {
	
	private static final String baseURL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
	private static final String esearch = baseURL + "esearch.fcgi?db=gene&term=";
	private static final String esummary =  baseURL + "esummary.fcgi?db=gene&id=";
	private static final String summarySuffix = "[gene]+AND+alive[prop]";
	private static final String efetch = baseURL + "efetch.fcgi?db=gene&retmode=xml&id=";
	private static DocumentBuilderFactory xmlDBF = DocumentBuilderFactory.newInstance();
	
	private HashMap<String,ArrayList<String>> searchCache;
	private HashMap<String,String> idLookupCache;
	
	/**
	 * Create a new Entrez Gene Data Access Object.  Initialize a new in-memory
	 * search cache.
	 */
	public EntrezGeneDAO() {
		this.searchCache = new HashMap<String,ArrayList<String>>();
		this.idLookupCache = new HashMap<String,String>();
	}
	
	/**
	 * Uses the Entrez Gene web service to search the Entrez database for genes
	 * using the queryTerms parameter as input.  This will return an ArrayList of
	 * Strings representing Entrez Gene internal ID's.  A maximum of 20 ID strings 
	 * are returned, in decreasing relevance (i.e. index 0 is most relevant,
	 * index 19 is least relevant).
	 * 
	 * The ID strings can be input directly into {@link #fetch(String)} to obtain 
	 * an object from the resource.
	 * 
	 * Search results are cached in memory, so repeat searches (identical parameters) 
	 * will return from the cache.
	 * 
	 * Can throw an IOException if the remote resource is unavailable or network 
	 * issues are hampering a connection.  Throws a NullPointerException in cases
	 * of a bad download or XML parsing problems.
	 * 
	 * @param queryTerms	a String of terms to query with
	 * @return				an ArrayList of Strings representing Entrez Gene ID's
	 * @throws IOException
	 * @throws NullPointerException
	 */
	public ArrayList<String> search(String queryTerms) throws IOException, NullPointerException {		
		// Construct HTTP request string
		String urlStr = esearch;
		System.out.println("[EG] Searching \"" + queryTerms + "\"");
		urlStr += queryTerms.replace(' ', '+') + summarySuffix;
		
		// Check cache before sending out request
		if (this.searchCache.containsKey(urlStr))
			return this.searchCache.get(urlStr);

		// Construct and open URL, get XML result
		URL url = new URL(urlStr);
		InputStream connStream = url.openStream();
		XMLTree xml = null;
		// !! May throw NullPointerException if the stream is interrupted !!
		// Exception is caught in wrapper
		xml = new XMLTree(parseXmlResult(connStream));		
		// Something (else) went wrong, throw a the same exception as above
		if (xml.getRoot() == null)
			throw new NullPointerException();
		
		// Parse XML and return ID's
		ArrayList<XMLNode> ids = xml.findNodes("Id");
		ArrayList<String> idList = new ArrayList<String>(ids.size());
		for (XMLNode n : ids) {
			idList.add(n.getText());
		}
		return idList;
	}
	
	/**
	 * Query the Entrez Gene web service for the name associated with
	 * the specified ID.
	 * 
	 * Can throw an IOException if the remote resource is unavailable or network 
	 * issues are hampering a connection.  Throws a NullPointerException in cases
	 * of a bad download or XML parsing problems.
	 * 
	 * @param id Entrez Gene ID (String)
	 * @return String name of the item associated with the supplied ID
	 * @throws IOException
	 */
	public String getName(String id) throws IOException, NullPointerException {
		System.out.println("[EG] Retrieving name: " + id);
		
		// Item already in cache, retrieve it and return it
		if (this.idLookupCache.containsKey(id))
			return this.idLookupCache.get(id);
		
		// Construct URL and send request for summary
		String urlStr = esummary + id;
		URL url = new URL(urlStr);
		InputStream connStream = url.openStream();
		
		// Parse XML document and find Name of the entity
		// Possibility for NullPointerException if the XML document
		// is incomplete (or otherwise bad)
		XMLTree xml = new XMLTree(parseXmlResult(connStream));
		ArrayList<XMLNode> itemNodes = xml.findNodes("Item");
		String name = null;
		for (XMLNode n : itemNodes) {
			if (n.getAttribute("Name").equals("Name")) {
				// Found it, add to cache and return the String
				name = n.getText();
				this.idLookupCache.put(id, name);
				break;
			}
		}
		return name;
	}
	
	/**
	 * Uses the Entrez Gene web service to fetch the entry associated with the
	 * parameter 'id' (Entrez Gene UID). 
	 * 
	 * The XML response is parsed and the relevant information extracted into 
	 * an Entity object, which is returned.  Will return null if the record 
	 * does not have a name.
	 * 
	 * Can throw an IOException if the remote resource is unavailable or network 
	 * issues are hampering a connection.  Throws a NullPointerException in cases
	 * of a bad download or XML parsing problems.
	 * 
	 * @param id the Entrez Gene UID to fetch (String)
	 * @return an Entity object containing the information from the response, 
	 * null if the record has no discernible name
	 * @throws IOException
	 * @throws NullPointerException
	 */
	public Entity fetch(String id) throws IOException, NullPointerException {
		System.out.println("[EG] Fetching record: " + id);
		
		XMLTree xml = null;
		
		// Construct URL
		String urlStr = efetch + id;
		URL url = new URL(urlStr);
		// Connect to the web service
		InputStream connStream = url.openStream();
		// Parse XML from stream
		// Possibility to throw a NullPointerException here (rare)
		xml = new XMLTree(parseXmlResult(connStream));
		// Something went wrong, issue is usually similar to above
		if (xml.getRoot() == null)
			throw new NullPointerException();
		
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
	 * Debug method for viewing Entrez Gene responses.  
	 * Returns an XML DOM Document.
	 * 
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
	 * Method for parsing XML responses to DOM Document's (encapsulated 
	 * here because the Java way of doing it is so obtuse).
	 * 
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
			System.out.println("Could not read XML (bad connection?)");
			e.printStackTrace();
		}
		return null;
	}
}
