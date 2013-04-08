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
import edu.cmu.lti.oaqa.bio.resource_wrapper.Origin;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Relation;
import edu.cmu.lti.oaqa.bio.resource_wrapper.xml.XMLNode;
import edu.cmu.lti.oaqa.bio.resource_wrapper.xml.XMLTree;

public class MeshDAO {
	private static final String baseURL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
	private static final String esearch = baseURL + "esearch.fcgi?db=mesh&term=";
	private static final String efetch = baseURL + "efetch.fcgi?db=mesh&retmode=text&id=";
	private static DocumentBuilderFactory xmlDBF = DocumentBuilderFactory.newInstance();
	
	private HashMap<String,ArrayList<String>> searchCache;
	
	/**
	 * Constructor.
	 * Initiates the local search cache.
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
		
		// Check the HashMap cache for the search results
		if (this.searchCache.containsKey(urlStr)) {
			System.out.println("Key in local cache!");
			return this.searchCache.get(urlStr);
		}
		
		// Connect to the web service and get the search results
		System.out.println("Searching with the web service");
		URL url = new URL(urlStr);
		InputStream connStream = url.openStream();

		// Get XML result
		XMLTree xml;
		// parseXmlResult will return null if an IOException occurs
		// In this case, the XMLTree constructor will fail due to a NullPointerException
		try {
			xml = new XMLTree(parseXmlResult(connStream));
		} catch (NullPointerException npExXmlTree) {
			System.out.println("MeshDAO.search: XMLTree of search results was malformed, incomplete, or non-existant, rendering it null. Empty results.");
			npExXmlTree.printStackTrace();
			return new ArrayList<String>(0);
		}
		
		// Find ID's and return
		ArrayList<XMLNode> ids = xml.findNodes("Id");
		ArrayList<String> idList = new ArrayList<String>(ids.size());
		for (XMLNode n : ids)
			idList.add(n.getText());
		return idList;
	}
	
	/**
	 * Fetch a MeSH record from the web service.
	 * Retrieves a text document record by it's id (the parameter) from 
	 * the MeSH web service.  This is parsed into the most complete 
	 * Entity possible (not all MeSH records use all of the fields) and 
	 * returned.
	 *  
	 * @param id String, internal MeSH ID
	 * @return Entity object
	 * @throws IOException
	 */
	public Entity fetch(String id) throws IOException {
		////////// Fetch document //////////
		System.out.println("Fetching " + id + "...");
		ArrayList<String> doc = null;
			
		String urlStr = efetch + id; // Construct URL
		
		// Connect to the web service and get the specified entity
		URL url = new URL(urlStr);
		InputStream connStream = url.openStream();
		// Read and parse document from connection
		doc = parseTextResult(connStream);
		
		// Add empty string at end to prevent IndexOutOfBoundsExceptions later
		doc.add("");
		
		// Create Entity
		Entity ent = new Entity(Origin.MESH);
		
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
	 * Method for parsing XML responses to DOM Document's 
	 * (encapsulated here because the Java way of doing it is so obtuse).
	 * @param stream	InputStream of XML information
	 * @return			XML DOM Document, null on IOException
	 */
	private static Document parseXmlResult(InputStream stream) {
		DocumentBuilder xmlReader;
		try {
			xmlReader = xmlDBF.newDocumentBuilder();
			return xmlReader.parse(stream);
		} catch (ParserConfigurationException pcEx) {
			pcEx.printStackTrace();
		} catch (SAXException saxEx) {
			saxEx.printStackTrace();
		} catch (IOException ioEx) {
			System.out.println("MeshDAO: could not parse the XML doc due to IOExcetpion (bad connection?).");
			ioEx.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Method for parsing text responses (in the form of InputStream's) 
	 * to ArrayList<String>, split by '\n'
	 * @param stream	InputStream of text information
	 * @return			ArrayList<String> split on '\n'; empty on error
	 */
	private static ArrayList<String> parseTextResult(InputStream stream) {
		ArrayList<String> result = new ArrayList<String>();
		try {
			// Read stream of text data from MeSH web service
			InputStreamReader isr = new InputStreamReader(stream);
			String bigStr = "";
			while (isr.ready()) {
				byte temp = (byte) isr.read();
				bigStr += (char) temp;
			}
			// Split on new lines and add to returned ArrayList
			for (String s : bigStr.split("\n"))
				result.add(s);
		} catch (IOException e) {
			System.out.println("MeshDAO.parseTextResult: IOException, something went wrong (stream not ready?), return null");
			e.printStackTrace();
		}
		return result;
	}

}
