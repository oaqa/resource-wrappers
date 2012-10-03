package edu.cmu.lti.oaqa.bio.resource_wrapper.xml;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A tree node (of XMLTree) that represents a Node within a Document (XML).</br>
 * Designed to make the output from Entrez Gene easier to work with.
 * 
 * @author	Collin McCormack (cmccorma)
 * @version	1.0.1
 * @see		Node
 * @see		org.w3c.dom.Document
 * @see 	XMLTree
 */
public class XMLNode {
	private String name;
	private String text;
	private XMLNode parent;
	private ArrayList<XMLNode> children;
	private HashMap<String,String> attrib;
	private Node xmlRef;
	
	/**
	 * Constructs an XMLNode with the properties from the Node input and recursively creates all child nodes.
	 * @param	nodeParent	the parent node in the XMLTree
	 * @param	ref			the Node object that represents this node in the XML Document
	 * @see		Node
	 */
	public XMLNode(XMLNode nodeParent, Node ref) {
		this.parent = nodeParent;
		this.xmlRef = ref;
		
		this.name = ref.getNodeName();
		this.text = null;
		this.attrib = new HashMap<String,String>();
		
		// Get text of node
		Node tempNode = ref.getFirstChild();
		while (tempNode != null) {
			if (tempNode.getNodeType() == Node.TEXT_NODE) {
				this.text = tempNode.getNodeValue().trim();
			}
			tempNode = tempNode.getNextSibling();
		}
		
		// Get all the attributes of the node
		if (ref.hasAttributes()) {
			NamedNodeMap nodeAttribs = ref.getAttributes();
			for (int i = 0; i < nodeAttribs.getLength(); i++) {
				Node tempAttrNode = nodeAttribs.item(i); 
				this.attrib.put(tempAttrNode.getNodeName(), tempAttrNode.getNodeValue());
			}
		}
		
		// Get all the children and recursively create more XMLNode's
		this.children = new ArrayList<XMLNode>();
		NodeList tempChildren = ref.getChildNodes();
		for (int x = 0; x < tempChildren.getLength(); x++) {
			Node childNode = tempChildren.item(x);
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				this.addChild(new XMLNode(this, childNode));
			}
		}
	}
	
	/**
	 * Searches the children of this node and finds all occurrences of a node with the input name (case-sensitive).  
	 * Can search all children of this node or just the immediate children.
	 * @param 	name		the node name to find
	 * @param 	recursive	if true, search ALL children, otherwise only immediate children
	 * @return				An ArrayList&ltXMLNode&gt of the child nodes with a matching name
	 */
	public ArrayList<XMLNode> findAll(String name, boolean recursive) {
		// Get the children to look through
		ArrayList<XMLNode> targetChildren = new ArrayList<XMLNode>();
		if (recursive)
			targetChildren.addAll(this.getChildren(true));
		else
			targetChildren.addAll(this.children);
		// Find the desired XMLNode's
		ArrayList<XMLNode> foundChildren = new ArrayList<XMLNode>();
		for (XMLNode x : targetChildren) {
			if (x.getName().equals(name))
				foundChildren.add(x);
		}
		return foundChildren;
	}
	/**
	 * Searches for a node with a name 'name' within the children of this node.  If recursive is enabled, all the children are searched, otherwise only the immediate children are searched.
	 * @param name		The name to look for
	 * @param recursive	If true, search all children, otherwise search immediate children only
	 * @return			the first occurrence of a node with name 'name' if it exists , null if it's not found
	 */
	public XMLNode findOne(String name, boolean recursive) {
		// Get the children to look through
		ArrayList<XMLNode> targetChildren = new ArrayList<XMLNode>();
		if (recursive)
			targetChildren.addAll(this.getChildren(true));
		else
			targetChildren.addAll(this.children);
		// Find the desired XMLNode's
		XMLNode found = null;
		for (XMLNode n : targetChildren) {
			if (n.getName().equals(name)) {
				found = n;
				break;
			}
		}
		return found;
	}
	
	/**
	 * Gets the children of this node.  Can get all children or just the immediate children.
	 * @param 	recursive	if true, return ALL children, otherwise only immediate children
	 * @return				An ArrayList&ltXMLNode&gt 
	 */
	public ArrayList<XMLNode> getChildren(boolean recursive) {
		if (!recursive) 
			return this.children;
		else {
			ArrayList<XMLNode> rcsvChildren = new ArrayList<XMLNode>();
			for (XMLNode x : this.children) {
				rcsvChildren.add(x);
				rcsvChildren.addAll(x.getChildren(true));
			}
			return rcsvChildren;
		}
	}
	
	/**
	 * Gets the parent of this node.
	 * @return	the XMLNode parent
	 */
	public XMLNode getParent() {
		return this.parent;
	}
	/**
	 * Gets the node's name (from Node.getNodeName()).  Generally, &lttheStuffInHere&gt.
	 * @return	String of the node's name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Gets the node's text content.  Generally, &ltNode&gtThe stuff in here&lt/Node&gt.
	 * @return	String of the node's text content
	 */
	public String getText() {
		return this.text;
	}
	
	/**
	 * Get the attribute specified by 'key'.
	 * Example: &ltsomeNode attributeKey="information"&gtnormal text&lt/someNode&gt
	 * 			getAttribute(attributeKey) returns "information";
	 * @param key
	 * @return String (empty String if attribute is not present)
	 */
	public String getAttribute(String key) {
		try {
			return this.attrib.get(key);
		} catch (Exception e) {
			return "";
		}
	}
	
	/**
	 * Gets the nodes who share the same parent as this node (result excludes this node).
	 * @return	an ArrayList&ltXMLNode&gt of nodes who share the same parent
	 */
	public ArrayList<XMLNode> getSiblings() {
		ArrayList<XMLNode> siblings = this.parent.getChildren(false);
		siblings.remove(this);
		return siblings;
	}
	/**
	 * Gets the Node reference (part of the original Document) for this node
	 * @return	Node of the original XML Document
	 */
	public Node getRef() {
		return this.xmlRef;
	}
	
	/**
	 * Adds a child element to this node.  Appended to the ArrayList of nodes, order of addition is preserved.
	 * @param child	The XMLNode element to be added
	 */
	public void addChild(XMLNode child) {
		this.children.add(child);
	}
	
	/**
	 * Add an attribute. </br>
	 * Example Text: &ltNode attributeKey="attributeValue"&gt&lt/Node&gt
	 * @param key
	 * @param value
	 */
	public void addAttribute(String key, String value) {
		this.attrib.put(key, value);
	}
	
	/**
	 * @return	name and value of this node
	 */
	@Override
	public String toString() {
		return this.name + ": " + this.text;
	}
}
