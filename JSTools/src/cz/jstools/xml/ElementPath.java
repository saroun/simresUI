package cz.jstools.xml;

import java.util.Stack;


/**
 * Derived from the class Stack. It serves to store the path to the actual XML element.
 * Defines "getPath()", which returns the whole stack content (path) with values delimited by ".".
 * @author   Svoboda Jan Saroun, PhD.
 */
public class ElementPath extends Stack<String> {
	private static final long  serialVersionUID = 6231724023109952526L;


	public ElementPath() {
		super();
	}
	
	public String getPath() {
		String path = null;
		for (int i=0; i<size(); ++i) {
			if (i==0) {
				path = elementAt(i);
			} else {
				path += "." + elementAt(i);
			}
		}
		return path;
	}
}