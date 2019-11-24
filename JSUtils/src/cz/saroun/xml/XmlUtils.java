package cz.saroun.xml;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;


/**
 * Definuje rùzné pomocné metody, jako napø. pro testování atributù XML elementù.
 *
 *
 * @author   Svoboda Jiøí, PhD.
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
	 * Tato metoda ovìøuje, zda poèet daných atributù je alespon roven požadovanému poètu atributù.
	 * V záporném pøípadì hází "SAXParseException".
	 *
	 * @param   atts               dané atributy
	 * @param   expected           požadovaný poèet atributù
	 * @throws  SAXParseException  v pøípadì, že nejsou splnìny podmínky testu
	 */
	public void testAttributes(Attributes atts, int expected) throws SAXParseException {
		if (atts.getLength() < expected) {
			stopParsing("Wrong number of attributes (act " + atts.getLength() + "/exp " + expected + ")");
		}
	}

	/**
	 * Tato metoda ovìøuje, zda poèet daných atributù je alespon roven požadovanému poètu atributù, zda
	 * existuje specifikovaný atribut a zdali má požadovanou hodnotu.
	 *
	 * @param   atts               dané atributy
	 * @param   expected           požadovaný poèet atributù
	 * @param   attName            jméno konkrétního atributu
	 * @param   attValue           požadovaná hodnota tohoto konkrétního atributu
	 * @throws  SAXParseException  v pøípadì, že nejsou splnìny podmínky testu
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
	 * Tato metoda ovìøuje, zda poèet daných atributù je alespon roven požadovanému poètu atributù a zda
	 * existuje specifikovaný atribut.
	 *
	 * @param   atts               dané atributy
	 * @param   expected           požadovaný poèet atributù
	 * @param   attName            jméno konkrétního atributu
	 * @throws  SAXParseException  v pøípadì, že nejsou splnìny podmínky testu
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
	 * Tato metoda ovìøuje, zda poèet daných atributù je alespon roven požadovanému poètu atributù a zda
	 * existují specifikované atributy.
	 *
	 * @param   atts               dané atributy
	 * @param   expected           požadovaný poèet atributù
	 * @param   attNames           seznam ("array") jmen konkrétních atributù
	 * @throws  SAXParseException  v pøípadì, že nejsou splnìny podmínky testu
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
	 * Tato metoda ovìøuje, zda poèet daných atributù je alespon roven požadovanému poètu atributù, zda
	 * existuje specifikovaný atribut a zdali má jednu z požadovaných hodnot.
	 *  
	 * @param   atts               dané atributy
	 * @param   expected           požadovaný poèet atributù
	 * @param   attName            jméno konkrétního atributu
	 * @param   attValues          seznam ("array") požadovaných hodnot tohoto konkrétního atributu
	 * @throws  SAXParseException  v pøípadì, že nejsou splnìny podmínky testu
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
	 * Tato metoda ovìøuje zda
	 * existuje specifikovaný atribut a zdali má jednu z požadovaných hodnot.
	 *  
	 * @param   atts               dané atributy
	 * @param   attName            jméno konkrétního atributu
	 * @param   attValues          seznam ("array") požadovaných hodnot tohoto konkrétního atributu
	 * @throws  SAXParseException  v pøípadì, že nejsou splnìny podmínky testu
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
	 * Tato metoda ukonèuje parsing XML a generuje patøiènou hlášku.
	 * 
	 * @param  whichCase  specifikuje okolnost, která vedla k ukonèení parsingu:<ul>
	 *                    <li>WRONG_ELEMENT  XML element je neznámý nebo neoèekávaný.
	 *                                   V hlášce je uvedeno plné jméno s XML cestou
	 *                                   k tomuto chybnému elementu.</li>
	 *                    <li>UNHANDLED_ELEMENT  XML element nemá definovanou obsluhu
	 *                                       vinou programátora. V hlášce je uvedeno plné
	 *                                       jméno s XML cestou k tomuto neošetøenému
	 *                                       elementu.</li></ul>
	 * @throws  SAXParseException  tuto vyjímku hází metoda vždy, nebo? slouží
	 *                             k ukonèení parsování
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
		 * Po vygenerování chybové hlášky konèí parsing XML, je tøeba vyèistit zásobník, jinak by
		 * v dalším parsování stále obsahoval tyto souèasné elementy. Nemusím se obávat, že pøijde
		 * požadavek "pop()", protože parsing je hozením vyjímky ukonèen a proto již nepøijdou žádné
		 * koncové elementy
		 */
		clearAll();
		
		throw new SAXParseException(msg, null);
	}
	
	
	/**
	 * Tato metoda ukonèuje parsing XML a generuje patøiènou hlášku. Jde o klon užívaný pøi
	 * parsování tagu Frame, jehož atribut id specifikuje, o jakou datovou strukturu uloženou
	 * v potagu SPECIFIC jde. Pøi výpisu je tedy potøeba dodat informace o jaký typ Framu jde
	 * 
	 * @param  whichCase  specifikuje okolnost, která vedla k ukonèení parsingu
	 *                    WRONG_ELEMENT  XML element je neznámý nebo neoèekávaný.
	 *                                   V hlášce je uvedeno plné jméno s XML cestou
	 *                                   k tomuto chybnému elementu.
	 *                    UNHANDLED_ELEMENT  XML element nemá definovanou obsluhu
	 *                                       vinou programátora. V hlášce je uvedeno plné
	 *                                       jméno s XML cestou k tomuto neošetøenému elementu.
	 * @param  cid  specifikuje id framu
	 * @param  type  specifikuje typ framu (SOURCE, DETECTOR a pod.)
	 * @throws  SAXParseException  tuto vyjímku hází metoda vždy, nebo? slouží
	 *                             k ukonèení parsování
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
		 * po vygenerování chybové hlášky konèí parsing XML, je tøeba vyèistit zásobník, jinak by
		 * v dalším parsování stále obsahoval tyto souèasné elementy. Nemusím se obávat, že pøijde
		 * požadavek "pop()", protože parsing je hozením vyjímky ukonèen a proto již nepøijdou žádné
		 * koncové elementy
		 */
		clearAll();
		
		throw new SAXParseException(msg, null);
	}


	/**
	 * Tato metoda ukonèuje parsování XML hozením vyjímky SAXParseException. Vypíše se
	 * cesta aktuálního elementu plus požadovaná hláška
	 *
	 * @param   msg                uživatelská hláška
	 * @throws  SAXParseException  tuto vyjímku hází metoda vždy, nebo? slouží k ukonèení parsování
	 */
	public void stopParsing(String msg) throws SAXParseException {
		String msg2 = "Element '" + elementPath.getPath() + "': " + msg;

		/*
		 * po vygenerování chybové hlášky konèí parsing XML, je tøeba vyèistit zásobník, jinak by
		 * v dalším parsování stále obsahoval tyto souèasné elementy. Nemusím se obávat, že pøijde
		 * požadavek "pop()", protože parsing je hozením vyjímky ukonèen a proto již nepøijdou žádné
		 * koncové elementy
		 */
		clearAll();
		
		System.err.println(msg2);
		throw new SAXParseException(msg2, null);
	}
}