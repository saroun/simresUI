package cz.saroun.xml;

import java.util.Stack;


/**
 * Tato tøída je odvozena od tøídy "Stack" a slouží k uchování cesty k aktuálnímu
 * XML elementu. Definuje se v ní nová metoda "getPath()", která vrací celý obsah
 * zásobníku (cestu) s hodnotami oddìlenými znakem '.'.
 *
 *
 * @author   Svoboda Jiøí, PhD.
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