package cz.saroun.xml;

import java.util.Stack;


/**
 * Tato t��da je odvozena od t��dy "Stack" a slou�� k uchov�n� cesty k aktu�ln�mu
 * XML elementu. Definuje se v n� nov� metoda "getPath()", kter� vrac� cel� obsah
 * z�sobn�ku (cestu) s hodnotami odd�len�mi znakem '.'.
 *
 *
 * @author   Svoboda Ji��, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:51 $</dt></dl>
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