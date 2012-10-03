package edu.cmu.lti.oaqa.bio.resource_wrapper.obo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A node within an OBOGraph that represents a single term.
 * 
 * Currently compliant with the standard at:
 * OBO Documentation found at http://www.geneontology.org/GO.format.obo-1_2.shtml
 * 
 * @author Collin McCormack (cmccorma)
 * @version 0.2
 * @see OBOGraph
 */
public class OBONode {
	private String id;
	private String name;
	private String def;
	private String type;
	private HashMap<String, ArrayList<String>> attr;
	private ArrayList<OBOEdge> inEdges;
	private ArrayList<OBOEdge> outEdges;
	
	public OBONode(ArrayList<String> chunk, HashSet<OBOEdge> edgeContainer) {
		this.id = null;
		this.name = null;
		this.def = null;
		this.type = null;
		this.attr = new HashMap<String, ArrayList<String>>();
		this.inEdges = new ArrayList<OBOEdge>();
		this.outEdges = new ArrayList<OBOEdge>();
		
		// Parse text to create node
		for (String line : chunk) {
			if (line.equals("[Term]"))
				continue;
			String lineDef = line.substring(0, line.indexOf(':'));
			if (lineDef.equals("id")) {
				this.id = line.substring(line.indexOf(':') + 2);
			}
			else if (lineDef.equals("name")) {
				this.name = line.substring(line.indexOf(':') + 2);
			}
			else if (lineDef.equals("namespace")) {
				this.type = line.substring(line.indexOf(':') + 2).replace('_', ' ');
			}
			else if (lineDef.equals("alt_id")) {
				// Add attribute, no edge
				// alt_id's as an edge would only point to self... not really useful
				this.addAttribute("alt id", line.substring(line.indexOf(':') + 2));
			}
			else if (lineDef.equals("def")) {
				this.def = line.substring(line.indexOf(" \"") + 2, line.indexOf("\" "));
			}
			else if (lineDef.equals("synonym")) {
				String beginSynonym = line.substring(line.indexOf(" \"") + 2);
				String synonym = beginSynonym.substring(0, beginSynonym.indexOf('"'));
				//System.out.println(beginSynonym);
				String synType = beginSynonym.substring(beginSynonym.indexOf('"')+2);
				//System.out.println(synType);
				synType = synType.substring(0, synType.indexOf(" ["));
				this.addAttribute("synonym-"+synType, synonym.trim());
			}
			else if (lineDef.equals("is_a")) {
				// Add attribute and edge
				String isaID = line.substring(line.indexOf(':') + 2, line.indexOf('!') - 1);
				this.addAttribute("is a", isaID);
				edgeContainer.add(new OBOEdge(this.id, isaID, "is a"));
			}
			else if (lineDef.equals("comment")) {
				this.addAttribute("comment", line.substring(line.indexOf(':') + 2));
			}
			else if (lineDef.equals("disjoint_from")) {
				this.addAttribute("disjoint from", line.substring(line.indexOf(':') + 2));
			}
			else if (lineDef.equals("intersection_of")) {
				// Add attribute and edge
				String intersectionValue = line.substring(line.indexOf(':') + 2, line.indexOf('!') - 1);
				this.addAttribute("intersection of", intersectionValue);
				// intersection_of can be followed by an id or a type (e.g. part_of) and id, add edge accordingly
				if (intersectionValue.indexOf(' ') != -1) {
					String intersectionType = intersectionValue.substring(0, intersectionValue.indexOf(' '));
					String intersectionID = intersectionValue.substring(intersectionValue.indexOf(' ') + 1);
					edgeContainer.add(new OBOEdge(this.id, intersectionID, "intersection of " + intersectionType));
				}
				else {
					OBOEdge intersection = new OBOEdge(this.id, intersectionValue, "intersection of");
					edgeContainer.add(intersection);
				}
			}
			else if (lineDef.equals("consider")) {
				// Add attribute and edge
				this.addAttribute("consider", line.substring(line.indexOf(':') + 2));
				edgeContainer.add(new OBOEdge(this.id, line.substring(line.indexOf(':') + 2), "consider"));
			}
			else if (lineDef.equals("xref")) {
				this.addAttribute("xref", line.substring(line.indexOf(':') + 2));
			}
			else if (lineDef.equals("relationship")) {
				// Add attribute and edge
				String relationWithID = line.substring(line.indexOf(':') + 2, line.indexOf('!') - 1); 
				String relation = relationWithID.substring(0, relationWithID.indexOf(' '));
				String relationID = relationWithID.substring(relationWithID.indexOf(' ') + 1);
				this.addAttribute("relationship", relationWithID);
				edgeContainer.add(new OBOEdge(this.id, relationID, relation));
			}
			else if (lineDef.equals("is_obsolete")) {
				// TODO: Do something special with is_obsolete
				// Alternatively, do we even care if something is obsolete?
				continue;
			}
			else if (lineDef.equals("replaced_by")) {
				// TODO: Do something special with replaced_by, often co-occurs with 'is_obsolete'
				continue;
			}
			else if (lineDef.equals("union_of")) {
				// Not contained in GO, don't know what to do with it
				continue;
			}
			else if (lineDef.equals("subset")) {
				// We don't track subsets (subsetdef) in the graph, so don't bother with this
				continue;
			}
			else {
				continue;
			}
		}
		// Don't add edges to edges fields because all edges get mapped to the nodes from the edgeContainer
		// Furthermore, the nodes that are pointed to may not exist yet
	}
	
	public String getId() {
		return this.id;
	}
	public String getName() {
		return this.name;
	}
	public String getDefinition() {
		return this.def;
	}
	public String getType() {
		return this.type;
	}
	public boolean hasAttribute(String identifier) {
		return this.attr.containsKey(identifier);
	}
	public ArrayList<String> getAttribute(String identifier) {
		if (!this.attr.containsKey(identifier))
			return new ArrayList<String>();
		else
			return this.attr.get(identifier);
	}
	private void addAttribute(String identifier, String value) {
		if (this.attr.containsKey(identifier))
			this.attr.get(identifier).add(value);
		else {
			this.attr.put(identifier, new ArrayList<String>());
			this.attr.get(identifier).add(value);
		}
	}
	public void addIncomingEdge(OBOEdge e) {
		this.inEdges.add(e);
	}
	public ArrayList<OBOEdge> getIncomingEdges() {
		return this.inEdges;
	}
	public void addOutgoingEdge(OBOEdge e) {
		this.outEdges.add(e);
	}
	public ArrayList<OBOEdge> getOutgoingEdges() {
		return this.outEdges;
	}
	public ArrayList<String> getAllSynonyms() {
		HashSet<String> syns = new HashSet<String>();
		if (this.hasAttribute("synonym-EXACT"))
			syns.addAll(this.getAttribute("synonym-EXACT"));
		if (this.hasAttribute("synonym-NARROW"))
			syns.addAll(this.getAttribute("synonym-NARROW"));
		if (this.hasAttribute("synonym-BROAD"))
			syns.addAll(this.getAttribute("synonym-BROAD"));
		if (this.hasAttribute("synonym-RELATED"))
			syns.addAll(this.getAttribute("synonym-RELATED"));
		return new ArrayList<String>(syns);
	}
}
 