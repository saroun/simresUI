package cz.saroun.xml;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;


/**
 * Definuje r�zn� pomocn� metody, jako nap�. pro testov�n� atribut� XML element�.
 *
 *
 * @author   Svoboda Ji��, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.2 $</dt>
 *               <dt>$Date: 2013/05/02 23:01:15 $</dt></dl>
 */
public class XmlUtils {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTANTS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	public static final int WRONG_ELEMENT = 0;
	public static final int UNHANDLED_ELEMENT = 1;
	public static final int STOPPED = 1;

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                        FIELDS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	private ElementPath               elementPath = null;
	private Stack<CallBackInterface>  handlers    = null;
	private Stack<String>             contents    = null;


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTRUCTORS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	public XmlUtils() {
		elementPath = new ElementPath();
		handlers    = new Stack<CallBackInterface>();
		contents    = new Stack<String>();
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    OTHER METHODS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	public void clearAll() {
		elementPath.clear();
		handlers.clear();
		contents.clear();
	}
	
	public void addElement(String s) {
		elementPath.push(s);
	}

	public void removeElement() {
		elementPath.pop();
	}

	public void addHandler(CallBackInterface handler) {
		handlers.push(handler);
	}

	public void forwardToHandler(CallBackInterface handler,
	                             String name,
	                             Attributes atts) throws SAXParseException {
		addHandler(handler);
		CallBackInterface h=getHandler();
		h.startElement(name, atts);
	}

	public void removeHandler() {
		handlers.pop();
	}

	public CallBackInterface getHandler() {
		return handlers.peek();
	}

	public void createEmptyContent() {
		contents.push("");
	}

	public void addToContent(String s) {
		String cont="";
		if (contents.size()>0) {
			cont=contents.pop().concat(s);
		} else {
			cont=s;
		}
		contents.push(cont);
	}

	public String getContent() {
		return contents.peek();
	}
	public String popContent() {
		if (contents.size()>0) {
			return contents.pop();
		} else return "";		
	}

	public void removeContent() {
		if (contents.size()>0) {
			contents.pop();
		}
	}
	
	
	public boolean getBooleanValue(Attributes atts,String attName)  {
		boolean b=false;
		if (atts.getIndex(attName) > -1) {
			String s = atts.getValue(attName);
			b=( (s!=null) && (s.equals("yes")));
		}
		return b;
	}

	
	public boolean hasAttribute(Attributes atts,String attName)  {
		return (atts.getIndex(attName) > -1);		
	}

	/**
	 * Tato metoda ov��uje, zda po�et dan�ch atribut� je alespon roven po�adovan�mu po�tu atribut�.
	 * V z�porn�m p��pad� h�z� "SAXParseException".
	 *
	 * @param   atts               dan� atributy
	 * @param   expected           po�adovan� po�et atribut�
	 * @throws  SAXParseException  v p��pad�, �e nejsou spln�ny podm�nky testu
	 */
	public void testAttributes(Attributes atts, int expected) throws SAXParseException {
		if (atts.getLength() < expected) {
			stopParsing("Wrong number of attributes (act " + atts.getLength() + "/exp " + expected + ")");
		}
	}

	/**
	 * Tato metoda ov��uje, zda po�et dan�ch atribut� je alespon roven po�adovan�mu po�tu atribut�, zda
	 * existuje specifikovan� atribut a zdali m� po�adovanou hodnotu.
	 *
	 * @param   atts               dan� atributy
	 * @param   expected           po�adovan� po�et atribut�
	 * @param   attName            jm�no konkr�tn�ho atributu
	 * @param   attValue           po�adovan� hodnota tohoto konkr�tn�ho atributu
	 * @throws  SAXParseException  v p��pad�, �e nejsou spln�ny podm�nky testu
	 */
	public void testAttributes(Attributes atts, int expected, String attName, String attValue) throws SAXParseException {
		if (atts.getLength() < expected) {
			stopParsing("Wrong number of attributes (act " + atts.getLength() + "/exp " + expected + ")");
		}

		String str = atts.getValue(attName);
		if (str == null) {
			stopParsing("Cannot detect required attribut '" + attName + "'");
		}
		if ( !str.equalsIgnoreCase(attValue)) {
			stopParsing("Requested attribut '" + attName + "' does not have the value '" + attValue + "'");
		}
	}

	/**
	 * Tato metoda ov��uje, zda po�et dan�ch atribut� je alespon roven po�adovan�mu po�tu atribut� a zda
	 * existuje specifikovan� atribut.
	 *
	 * @param   atts               dan� atributy
	 * @param   expected           po�adovan� po�et atribut�
	 * @param   attName            jm�no konkr�tn�ho atributu
	 * @throws  SAXParseException  v p��pad�, �e nejsou spln�ny podm�nky testu
	 */
	public void testAttributes(Attributes atts, int expected, String attName) throws SAXParseException {
		if (atts.getLength() < expected) {
			stopParsing("Wrong number of attributes (act " + atts.getLength() + "/exp " + expected + ")");
		}

		if (atts.getIndex(attName) == -1) {
			stopParsing("Cannot detect required attribut '" + attName + "'");
		}
	}

	/**
	 * Tato metoda ov��uje, zda po�et dan�ch atribut� je alespon roven po�adovan�mu po�tu atribut� a zda
	 * existuj� specifikovan� atributy.
	 *
	 * @param   atts               dan� atributy
	 * @param   expected           po�adovan� po�et atribut�
	 * @param   attNames           seznam ("array") jmen konkr�tn�ch atribut�
	 * @throws  SAXParseException  v p��pad�, �e nejsou spln�ny podm�nky testu
	 */
	public void testAttributes(Attributes atts, int expected, String[] attNames) throws SAXParseException {
		if (atts.getLength() < expected) {
			stopParsing("Wrong number of attributes (act " + atts.getLength() + "/exp " + expected + ")");
		}

		for (String attName : attNames) {
			if (atts.getIndex(attName) == -1) {
				stopParsing("Cannot detect required attribut '" + attName + "'");
			}
		}
	}
	
	/**
	 * Tato metoda ov��uje, zda po�et dan�ch atribut� je alespon roven po�adovan�mu po�tu atribut�, zda
	 * existuje specifikovan� atribut a zdali m� jednu z po�adovan�ch hodnot.
	 *  
	 * @param   atts               dan� atributy
	 * @param   expected           po�adovan� po�et atribut�
	 * @param   attName            jm�no konkr�tn�ho atributu
	 * @param   attValues          seznam ("array") po�adovan�ch hodnot tohoto konkr�tn�ho atributu
	 * @throws  SAXParseException  v p��pad�, �e nejsou spln�ny podm�nky testu
	 */
	public void testAttributes(Attributes atts, int expected, String attName, String[] attValues) throws SAXParseException {
		int i;
		
		if (atts.getLength() < expected) {
			stopParsing("Wrong number of attributes (act " + atts.getLength() + "/exp " + expected + ")");
		}

		String str = atts.getValue(attName);
		if (str == null) {
			stopParsing("Cannot detect required attribut '" + attName + "'");
		}
		for (i=0; i<attValues.length; ++i) {
			if (str.equalsIgnoreCase(attValues[i])) {
				break;
			}
		}
		if (i == attValues.length) {
			stopParsing("Requested attribut '" + attName + "' have inappropriate value '" + str + "'");
		}
	}
	
	
	/**
	 * Tato metoda ov��uje zda
	 * existuje specifikovan� atribut a zdali m� jednu z po�adovan�ch hodnot.
	 *  
	 * @param   atts               dan� atributy
	 * @param   attName            jm�no konkr�tn�ho atributu
	 * @param   attValues          seznam ("array") po�adovan�ch hodnot tohoto konkr�tn�ho atributu
	 * @throws  SAXParseException  v p��pad�, �e nejsou spln�ny podm�nky testu
	 */
	public void testAttributes(Attributes atts, String attName, String[] attValues) throws SAXParseException {
		int i;
		
		String str = atts.getValue(attName);
		if (str == null) {
			stopParsing("Cannot detect required attribut '" + attName + "'");
		}
		for (i=0; i<attValues.length; ++i) {
			if (str.equalsIgnoreCase(attValues[i])) {
				break;
			}
		}
		if (i == attValues.length) {
			stopParsing("Requested attribut '" + attName + "' have inappropriate value '" + str + "'");
		}
	}


	/**
	 * Tato metoda ukon�uje parsing XML a generuje pat�i�nou hl�ku.
	 * 
	 * @param  whichCase  specifikuje okolnost, kter� vedla k ukon�en� parsingu:<ul>
	 *                    <li>WRONG_ELEMENT  XML element je nezn�m� nebo neo�ek�van�.
	 *                                   V hl�ce je uvedeno pln� jm�no s XML cestou
	 *                                   k tomuto chybn�mu elementu.</li>
	 *                    <li>UNHANDLED_ELEMENT  XML element nem� definovanou obsluhu
	 *                                       vinou program�tora. V hl�ce je uvedeno pln�
	 *                                       jm�no s XML cestou k tomuto neo�et�en�mu
	 *                                       elementu.</li></ul>
	 * @throws  SAXParseException  tuto vyj�mku h�z� metoda v�dy, nebo? slou��
	 *                             k ukon�en� parsov�n�
	 */
	public void stopParsing(int whichCase) throws SAXParseException{
		String  msg;
		
		if (whichCase == WRONG_ELEMENT) {
			msg = "Unknown or unexpected element '" + elementPath.getPath() + "'";
		} else if (whichCase == UNHANDLED_ELEMENT) {
			msg = "Unhandled element '" + elementPath.getPath() + "'";
		} else if (whichCase == STOPPED) {
			msg = "Parsing stopped";
		} else {
			throw new IllegalArgumentException("Undefined stop condition: "+whichCase);
		}

		/* 
		 * Po vygenerov�n� chybov� hl�ky kon�� parsing XML, je t�eba vy�istit z�sobn�k, jinak by
		 * v dal��m parsov�n� st�le obsahoval tyto sou�asn� elementy. Nemus�m se ob�vat, �e p�ijde
		 * po�adavek "pop()", proto�e parsing je hozen�m vyj�mky ukon�en a proto ji� nep�ijdou ��dn�
		 * koncov� elementy
		 */
		clearAll();
		
		throw new SAXParseException(msg, null);
	}
	
	
	/**
	 * Tato metoda ukon�uje parsing XML a generuje pat�i�nou hl�ku. Jde o klon u��van� p�i
	 * parsov�n� tagu Frame, jeho� atribut id specifikuje, o jakou datovou strukturu ulo�enou
	 * v potagu SPECIFIC jde. P�i v�pisu je tedy pot�eba dodat informace o jak� typ Framu jde
	 * 
	 * @param  whichCase  specifikuje okolnost, kter� vedla k ukon�en� parsingu
	 *                    WRONG_ELEMENT  XML element je nezn�m� nebo neo�ek�van�.
	 *                                   V hl�ce je uvedeno pln� jm�no s XML cestou
	 *                                   k tomuto chybn�mu elementu.
	 *                    UNHANDLED_ELEMENT  XML element nem� definovanou obsluhu
	 *                                       vinou program�tora. V hl�ce je uvedeno pln�
	 *                                       jm�no s XML cestou k tomuto neo�et�en�mu elementu.
	 * @param  cid  specifikuje id framu
	 * @param  type  specifikuje typ framu (SOURCE, DETECTOR a pod.)
	 * @throws  SAXParseException  tuto vyj�mku h�z� metoda v�dy, nebo? slou��
	 *                             k ukon�en� parsov�n�
	 */
	public void stopParsing(int whichCase, String cid, String type) throws SAXParseException{
		String  msg;
		
		if (whichCase == WRONG_ELEMENT) {
			msg = "Unknown or unexpected element '" + elementPath.getPath() + "' (frame: id='" + cid + "' type='" + type + "').";
		} else if (whichCase == UNHANDLED_ELEMENT) {
			msg = "Unhandled element '" + elementPath.getPath() + "' (frame: id='" + cid + "' type='" + type + "').";
		} else {
			throw new IllegalArgumentException("Only WRONG_ELEMENT and UNHANDLED_ELEMENT arguments are allowed.");
		}

		/*
		 * po vygenerov�n� chybov� hl�ky kon�� parsing XML, je t�eba vy�istit z�sobn�k, jinak by
		 * v dal��m parsov�n� st�le obsahoval tyto sou�asn� elementy. Nemus�m se ob�vat, �e p�ijde
		 * po�adavek "pop()", proto�e parsing je hozen�m vyj�mky ukon�en a proto ji� nep�ijdou ��dn�
		 * koncov� elementy
		 */
		clearAll();
		
		throw new SAXParseException(msg, null);
	}


	/**
	 * Tato metoda ukon�uje parsov�n� XML hozen�m vyj�mky SAXParseException. Vyp�e se
	 * cesta aktu�ln�ho elementu plus po�adovan� hl�ka
	 *
	 * @param   msg                u�ivatelsk� hl�ka
	 * @throws  SAXParseException  tuto vyj�mku h�z� metoda v�dy, nebo? slou�� k ukon�en� parsov�n�
	 */
	public void stopParsing(String msg) throws SAXParseException {
		String msg2 = "Element '" + elementPath.getPath() + "': " + msg;

		/*
		 * po vygenerov�n� chybov� hl�ky kon�� parsing XML, je t�eba vy�istit z�sobn�k, jinak by
		 * v dal��m parsov�n� st�le obsahoval tyto sou�asn� elementy. Nemus�m se ob�vat, �e p�ijde
		 * po�adavek "pop()", proto�e parsing je hozen�m vyj�mky ukon�en a proto ji� nep�ijdou ��dn�
		 * koncov� elementy
		 */
		clearAll();
		
		System.err.println(msg2);
		throw new SAXParseException(msg2, null);
	}
}