package edu.cmu.lti.oaqa.bio.resource_wrapper.obo;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * A directed object graph that reads in, structures, and connects the terms in an OBO file.  Builds the graph from OBONode's and OBOEdge's.
 * 
 * Currently compliant with the standard at:
 * OBO Documentation found at http://www.geneontology.org/GO.format.obo-1_2.shtml
 * 
 * @author Collin McCormack (cmccorma)
 * @version 0.2.1
 * @see OBONode
 * @see OBOEdge
 */
public class OBOGraph {
	
	private HashMap<String, OBONode> nodes;
	private HashSet<OBOEdge> edges;
	private HashMap<String, ArrayList<String>> nameSynMap;
	private HashMap<String, ArrayList<String>> termIndex;
	private HashSet<String> stopwords = OBOGraph.getStopwords();
	
	public OBOGraph(Reader oboReader) {
		this.nodes = new HashMap<String, OBONode>();
		this.edges = new HashSet<OBOEdge>();
		this.nameSynMap = new HashMap<String, ArrayList<String>>();
		this.termIndex = new HashMap<String, ArrayList<String>>();
		
		// Parse input stream into chunks of text
		Scanner textScanner = new Scanner(oboReader);
		ArrayList<ArrayList<String>> chunks = new ArrayList<ArrayList<String>>();
		ArrayList<String> current = new ArrayList<String>();
		while (textScanner.hasNextLine()) {
			String line = textScanner.nextLine();
			if (line.equals("")) {
				current = new ArrayList<String>();
				chunks.add(current);
			}
			else
				current.add(line);
		}
		
		// Parse each chunk to get Nodes and their edges
		// Add nodes to HashMap nodes, id -> OBONode
		for (ArrayList<String> list : chunks) {
			// Create and add OBONode, collect OBOEdge's
			if (!list.isEmpty() && list.get(0).equals("[Term]")) {
				OBONode n = new OBONode(list, this.edges);
				this.nodes.put(n.getId(), n);
				if (n.hasAttribute("alt id")) {
					ArrayList<String> altIds = n.getAttribute("alt id");
					for (String altId : altIds) {
						this.nodes.put(altId, n);
					}
				}
			}
			// TODO: Handle meta-data and typedef's?
		}
		
		// Iterate over all edges and add them to relevant OBONode's
		for (OBOEdge edge : this.edges) {
			this.nodes.get(edge.getStart()).addOutgoingEdge(edge);
			this.nodes.get(edge.getEnd()).addIncomingEdge(edge);
		}
		
		// Construct Names and Synonyms Map
		for (OBONode n : this.nodes.values()) {
			// OBONode name
			if (this.nameSynMap.containsKey(n.getName().toLowerCase())) {
				ArrayList<String> temp = this.nameSynMap.get(n.getName().toLowerCase());
				temp.add(n.getId());
			}
			else {
				ArrayList<String> temp = new ArrayList<String>(1);
				temp.add(n.getId());
				this.nameSynMap.put(n.getName().toLowerCase(), temp);
			}
			for (String syn : n.getAllSynonyms()) {
				if (this.nameSynMap.containsKey(syn.toLowerCase())) {
					ArrayList<String> temp = this.nameSynMap.get(syn.toLowerCase());
					temp.add(n.getId());
				}
				else {
					ArrayList<String> temp = new ArrayList<String>(1);
					temp.add(n.getId());
					this.nameSynMap.put(syn.toLowerCase(), temp);
				}
			}
		}
		
		// Build term index for searching
		// Only indexes names, synonyms, and definitions
		for (OBONode n : this.nodes.values()) {
			// Build list of words for a single node
			HashSet<String> tempWords = new HashSet<String>();
			for (String s : n.getName().toLowerCase().split(" ")) { tempWords.add(s); }
			for (String s : n.getAllSynonyms()) { 
				for (String str : s.toLowerCase().split(" ")) { tempWords.add(str.replace(',', ' ').trim()); }
			}
			for (String s : n.getDefinition().toLowerCase().split(" ")) {
				s = s.replace('.', ' ').replace(',', ' ').replace(':', ' ').replace(';', ' ');
				tempWords.add(s.trim());
			}
			tempWords.removeAll(this.stopwords); // Remove stopwords from set
			
			// Add them to the index; key = word, value = node ID that used the word
			String nodeId = n.getId();
			for (String s : tempWords) {
				if (this.termIndex.containsKey(s)) {
					ArrayList<String> tempIds = this.termIndex.get(s);
					tempIds.add(nodeId);
				}
				else {
					ArrayList<String> tempIds = new ArrayList<String>();
					tempIds.add(nodeId);
					this.termIndex.put(s, tempIds);
				}
			}
		}
		
	}
	private static HashSet<String> getStopwords() {
		HashSet<String> sw = new HashSet<String>();
		sw.add("of");
		sw.add("the");
		sw.add("a");
		sw.add("an");
		sw.add("and");
		sw.add("that");
		sw.add("to");
		sw.add("by");
		sw.add("from");
		sw.add("as");
		sw.add("in");
		sw.add("or");
		sw.add("which");
		sw.add("with");
		return sw;
	}
	/**
	 * Find an OBONode by it's ontology-specific ID.
	 * @param id String including ontology prefix e.g. 'GO:0005400'
	 * @return OBONode if found, null otherwise
	 */
	public OBONode getNodeById(String id) {
		if (this.nodes.containsKey(id))
			return this.nodes.get(id);
		else
			return null;
	}
	
	/**
	 * Searches for exact name and synonym matches.
	 * @param query String of the name of the node/term (case-insensitive)
	 * @return an array list of matching OBONode's, may be empty
	 */
	public ArrayList<OBONode> searchNames(String query) {
		ArrayList<String> ids = this.nameSynMap.get(query.toLowerCase());
		if (ids == null)
			return new ArrayList<OBONode>(0);
		else {
			ArrayList<OBONode> results = new ArrayList<OBONode>();
			for (String id : ids) {
				results.add(this.getNodeById(id));
			}
			return results;
		}
	}
	
	/**
	 * Searches the names, synonyms, and definitions using the supplied query.
	 * @param query search String
	 * @return ArrayList of OBONode's that were the best match to the query
	 */
	public ArrayList<OBONode> search(String query) {
		ArrayList<String> queryTerms = new ArrayList<String>();
		for (String s : query.toLowerCase().split(" ")) { queryTerms.add(s); System.out.println(s); }
		HashMap<String, Integer> resultIds = new HashMap<String, Integer>();
		
		for (String term : queryTerms) {
			if (this.termIndex.containsKey(term)) {
				for (String id : this.termIndex.get(term)) {
					if (resultIds.containsKey(id)) {
						Integer count = resultIds.get(id) + 1;
						resultIds.put(id, count);
					}
					else {
						resultIds.put(id, 1);
					}
				}
			}
		}
		
		// Get top count and return best result(s)
		ArrayList<String> topIds = this.top(resultIds);
		ArrayList<OBONode> results = new ArrayList<OBONode>();
		for (String s : topIds) {
			results.add(this.getNodeById(s));
		}
		return results;
	}
	
	/**
	 * Retrieves the top result from a list of index scores generated in search(...).  There may be multiple tied results.
	 * @param list HashMap id -> score
	 * @return ArrayList containing best result(s)
	 */
	private ArrayList<String> top(HashMap<String, Integer> list) {
		ArrayList<String> topResults = new ArrayList<String>();
		int cutoff = 0;
		for (Entry<String, Integer> e : list.entrySet()) {
			if (e.getValue() > cutoff)
				cutoff = e.getValue();
		}
		System.out.println("cutoff: " + cutoff);
		for (Entry<String, Integer> e : list.entrySet()) {
			if (e.getValue().intValue() == cutoff)
				topResults.add(e.getKey());
		}
		return topResults;
	}
}
