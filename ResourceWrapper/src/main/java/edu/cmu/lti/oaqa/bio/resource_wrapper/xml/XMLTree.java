package edu.cmu.lti.oaqa.bio.resource_wrapper.xml;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A tree representation of an XML Document.  Allows for much easier searching and retrieval of nodes and text than the standard XML parsing library.</br>
 * Uses the standard XML parsing library to build an acyclic, n-ary tree of XMLNode's.  The tree is organized according to the structure of the original XML document, no further sorting is applied.</br>
 * Designed to make the output from Entrez Gene easier to work with.
 * 
 * @author		Collin McCormack (cmccorma)
 * @version		1.0
 * @see			org.w3c.dom.Document
 * @see			Node
 */
public class XMLTree {
	private XMLNode root;
	
	/**
	 * Recursively constructs a tree representation of an XML document using XMLNode's.</br>
	 * Root is set to null if input is null.
	 * 
	 * @param	doc		an XML Document
	 * @see		org.w3c.dom.Document
	 * @see		XMLNode
	 */
	public XMLTree(Document doc) {
		try {
			// Get first ELEMENT_NODE (DOCUMENT_TYPE_NODE has no children)
			Node temp = doc.getFirstChild();
			while (temp.getNodeType() != Node.ELEMENT_NODE) {
				temp = temp.getNextSibling();
			}
			// Recursively build tree via constructors
			this.root = new XMLNode(null, temp);
		} catch (NullPointerException ne) {
			this.root = null;
			System.out.println("XMLTree: null input");
		}
	}
	
	/**
	 * Return the root of the tree (parent is null).
	 * @return	the XMLNode root of the tree
	 */
	public XMLNode getRoot() {
		return this.root;
	}
	
	/**
	 * Searches the entire tree for nodes with the input name (case-sensitive).  The search does NOT include the root of the tree.</br>
	 * Convenience method for "XMLTreeObject.getRoot().findAll(name, true);"
	 * 
	 * @param name	the name to match
	 * @return		an ArrayList<XMLNode> with nodes matching name
	 */
	public ArrayList<XMLNode> findNodes(String name) {
		return this.root.findAll(name, true);
	}
	
	/**
	 * Similar to findNodes, but only find the first occurrence of a node.</br>
	 * Convenience method for "XMLTreeObject.getRoot().findOne(name, true);"
	 * 
	 * @param name	the name to match
	 * @return		the first XMLNode with matching name
	 */
	public XMLNode findFirstNode(String name) {
		return this.root.findOne(name, true);
	}
}
