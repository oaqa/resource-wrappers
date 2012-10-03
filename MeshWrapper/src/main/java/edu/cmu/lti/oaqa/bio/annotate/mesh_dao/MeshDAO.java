package edu.cmu.lti.oaqa.bio.annotate.mesh_dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.cmu.lti.oaqa.bio.resource_wrapper.Entity;
import edu.cmu.lti.oaqa.bio.resource_wrapper.ID;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Relation;
import edu.cmu.lti.oaqa.bio.resource_wrapper.ResourceWrapper;
import edu.cmu.lti.oaqa.bio.resource_wrapper.xml.XMLNode;
import edu.cmu.lti.oaqa.bio.resource_wrapper.xml.XMLTree;

public class MeshDAO implements ResourceWrapper {
	private static final String baseURL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
	private static final String esearch = baseURL + "esearch.fcgi?db=mesh&term=";
	private static final String efetch = baseURL + "efetch.fcgi?db=mesh&retmode=text&id=";
	private static DocumentBuilderFactory xmlDBF = DocumentBuilderFactory.newInstance();
	
	private HashMap<String,ArrayList<String>> searchCache;
	
	/**
	 * Constructor.
	 */
	public MeshDAO() {
		this.searchCache = new HashMap<String,ArrayList<String>>();
	}
	
	/**
	 * Search MeSH using the native search functionality from the web service.
	 * @param queryTerms String, terms to query for
	 * @return ArrayList of id Strings
	 * @throws IOException
	 */
	public ArrayList<String> search(String queryTerms) throws IOException {
		// Construct HTTP request URL
		String urlStr = esearch;
		System.out.println("Searching \"" + queryTerms + "\"...");
		urlStr += queryTerms.replace(' ', '+');
		System.out.println(urlStr);
		
		if (this.searchCache.containsKey(urlStr)) {
			return this.searchCache.get(urlStr);
		}
		
		URL url = new URL(urlStr);

		// Open URL and get XML result
		InputStream connStream = url.openStream();
		XMLTree xml = new XMLTree(parseXmlResult(connStream));
		
		// Parse XML and return ID's
		ArrayList<XMLNode> ids = xml.findNodes("Id");
		ArrayList<String> idList = new ArrayList<String>(ids.size());
		for (XMLNode n : ids) {
			idList.add(n.getText());
		}
		return idList;
	}
	
	/**
	 * Fetch a MeSH record from the web service.
	 * @param id String, internal MeSH ID
	 * @return Entity object
	 * @throws IOException
	 */
	public Entity fetch(String id) throws IOException {
		////////// Fetch document //////////
		System.out.println("Fetching " + id + "...");
		ArrayList<String> doc;
			
		// Construct URL
		String urlStr = efetch + id;
		URL url = new URL(urlStr);
		
		// Read and parse InputStream
		InputStream connStream = url.openStream();
		doc = parseTextResult(connStream);
		
		// Add empty string at end to prevent IndexOutOfBoundsExceptions later
		doc.add("");
		
		// Create Entity
		Entity ent = new Entity("MeSH");
		
		////////// Get name //////////
		int nameIndex = -1;
		for (int x = 0; x < doc.size(); x++) {
			if (doc.get(x).startsWith("1:")) {
				nameIndex = x;
				break;
			}
		}
		ent.setName(doc.get(nameIndex).substring(3));
		
		////////// Get ID //////////
		ent.addID(new ID("MeSH", id));
		
		////////// Get definition //////////
		int defIndex = nameIndex+1;
		String def = "";
		while (!doc.get(defIndex).isEmpty()) {
			def += doc.get(defIndex) + " ";
			defIndex++;
		}
		ent.setDefinition(def.trim());
		
		////////// Get Relation's - Subheadings //////////
		int shIndex = -1;
		for (int x = 0; x < doc.size(); x++) {
			if (doc.get(x).startsWith("Subheadings")) {
				shIndex = x;
				break;
			}
		}
		if (shIndex > -1) {
			shIndex++;
			while (!doc.get(shIndex).isEmpty()) {
				ent.addRelation(new Relation("MeSH Subheading", doc.get(shIndex).trim()));
				shIndex++;
			}
		}
		
		////////// Get synonyms - Entry Terms //////////
		int etIndex = -1;
		for (int x = 0; x < doc.size(); x++) {
			if (doc.get(x).startsWith("Entry Terms")) {
				etIndex = x;
				break;
			}
		}
		if (etIndex > -1) {
			etIndex++;
			while (!doc.get(etIndex).isEmpty()) {
				ent.addSynonym(doc.get(etIndex).trim());
				etIndex++;
			}
		}
		
		//////////Get Relation's - See Also //////////
		int seIndex = -1;
		for (int x = 0; x < doc.size(); x++) {
			if (doc.get(x).startsWith("See Also")) {
				seIndex = x;
				break;
			}
		}
		if (seIndex > -1) {
			seIndex++;
			while (!doc.get(seIndex).isEmpty()) {
				ent.addRelation(new Relation("MeSH Association", doc.get(seIndex).trim()));
				seIndex++;
			}
		}
		
		////////// Get Relation's - Categories //////////
		ArrayList<Integer> hierarchyIndex = new ArrayList<Integer>();
		for (int x = 0; x < doc.size(); x++) {
			if (doc.get(x).contains("All MeSH Categories"))
				hierarchyIndex.add(x+1);
		}
		for (int i : hierarchyIndex) {
			int index = i;
			String hierarchyStr = "";
			while (!doc.get(index).isEmpty()) {
				hierarchyStr += doc.get(index).trim() + " > ";
				index++;
			}
			ent.addRelation(new Relation("MeSH Hierarchy", hierarchyStr.substring(0, hierarchyStr.length()-3)));
		}
		
		// Set type
		// There's no easy way to consistently extract an accurate type
		ent.setType(null);
		
		return ent;
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
			System.out.println("Bad connection.");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Method for parsing text responses (in the form of InputStream's) to ArrayList<String>, split by '\n'
	 * @param stream	InputStream of text information
	 * @return			ArrayList<String> split on '\n'; null on error
	 */
	private static ArrayList<String> parseTextResult(InputStream stream) {
		try {
			InputStreamReader isr = new InputStreamReader(stream);
			String bigStr = "";
			while (isr.ready()) {
				byte temp = (byte) isr.read();
				bigStr += (char) temp;
			}
			
			ArrayList<String> result = new ArrayList<String>();
			for (String s : bigStr.split("\n")) {
				result.add(s);
			}
			return result;
		} catch (IOException e) {
			System.out.println("Stream not ready?");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * General query for MeSH.  Uses the built-in search function and returns the top 5 results.
	 * @param	query	String of search terms to query with
	 * @return			ArrayList<Entity> of of items found using query, null on error
	 */
	public ArrayList<Entity> getEntities(String query) {
		try {
			ArrayList<Entity> results = new ArrayList<Entity>();
			ArrayList<String> ids = search(query);
			int size = ids.size() < 5 ? ids.size() : 5;
			for (int i = 0; i < size; i++) {
				Entity temp = fetch(ids.get(i));
				if (temp != null)
					results.add(temp);
			}
			return results;
		} catch (IOException e) {
			System.out.println("IOException: Could not contact server (probably)");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Similar to getEntities(query), the name of the items found with query must exactly match (case-insensitive) query.
	 * @param	query		String of search terms to query with
	 * @param	exactMatch	boolean flag for an exact match test on each item
	 * @return				ArrayList<Entity> of exact matches
	 */
	public ArrayList<Entity> getEntities(String query, boolean exactMatch) {
		if (exactMatch) {
			try {
				query = query.trim();
				ArrayList<String> ids = search(query);
				ArrayList<Entity> matchingEntities = new ArrayList<Entity>();
				outer: for (int i = 0; i < ids.size(); i++) {
					Entity tempEnt = fetch(ids.get(i));
					if (tempEnt == null)
						continue;
					// search whole name
					if (tempEnt.getName().equalsIgnoreCase(query)) {
						matchingEntities.add(tempEnt);
						continue;
					}
					// if commas are present, search components
					if (tempEnt.getName().contains(",")) {
						for (String s : tempEnt.getName().split(",")) {
							if (s.trim().equalsIgnoreCase(query)) {
								matchingEntities.add(tempEnt);
								continue outer;
							}
						}
					}
				}
				return matchingEntities;
			} catch (IOException e) {
				System.out.println("IOException: Could not contact server (probably)");
				e.printStackTrace();
				return null;
			}
		}
		else
			return getEntities(query);
	}
	
	/**
	 * MeSh does not contain ID's from other systems and so there is nothing to go into the Entity ID field.  It does have unique identifiers for internal use, but those aren't found elsewhere.
	 * Leaving it unimplemented, as any implementation would be more confusing than helpful.
	 */
	public Entity getEntityById(String id) {
		throw new UnsupportedOperationException("Not implemented");
	}
	
	/**
	 * See getEntityByID(String query), same thing applies
	 */
	public Entity getEntityById(String id, boolean exactMatch) {
		throw new UnsupportedOperationException("Not implemented");
	}
	
	
}
