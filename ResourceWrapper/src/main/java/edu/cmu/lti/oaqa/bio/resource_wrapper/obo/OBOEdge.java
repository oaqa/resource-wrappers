package edu.cmu.lti.oaqa.bio.resource_wrapper.obo;

/**
 * 
 * A directed edge used in building a OBOGraph.  Connects two OBONode's with a descriptive label.
 * 
 * @author Collin McCormack (cmccorma)
 * @version 0.1
 * @see OBONode
 * @see OBOGraph
 */
public class OBOEdge {
	private String start;
	private String end;
	private String label;
	
	public OBOEdge(String startID, String endID, String label) {
		this.start = startID;
		this.end = endID;
		this.label = label;
	}
	
	public String getStart() {
		return this.start;
	}
	public String getEnd() {
		return this.end;
	}
	public String getLabel() {
		return this.label;
	}
	
	public String toString() {
		return this.start + "," + this.label + "," + this.end;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OBOEdge other = (OBOEdge) obj;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		return true;
	}
}
