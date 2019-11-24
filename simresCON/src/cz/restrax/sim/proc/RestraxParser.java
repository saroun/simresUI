package cz.restrax.sim.proc;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.StringReader;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import cz.restrax.sim.SimresCON;
import cz.restrax.sim.xml.handlers.RsxdumpExh;
import cz.saroun.classes.definitions.Utils;
import cz.saroun.tasks.ConsoleListener;
import cz.saroun.xml.DefaultXmlHandler;
import cz.saroun.xml.XmlUtils;


/**
 * Listens to the console input.
 *
 *
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.13 $</dt>
 *               <dt>$Date: 2019/06/12 17:58:11 $</dt></dl>
 */
public class RestraxParser implements ConsoleListener {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                      CONSTANTS                                       //
	//////////////////////////////////////////////////////////////////////////////////////////
	private static final boolean  __DEBUG__       = false;
	private static final boolean  __DEBUGXML__       = false;
	private static final int      BUFFER_CAPACITY = 8192;
	private static final String   START_ELEMENT   = "<RSXDUMP>";
	private static final String   END_ELEMENT     = "</RSXDUMP>";
	private static final Object sync = new Object();
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                        FIELDS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	private XMLReader              xmlReader     = null;
	private ConsoleXmlHandler      xmlHandler    = null;
	//private StringBuilder          buffer        = null;
	private final StringBuffer     buffer;
	private SimresCON              program       = null;
	private String                 waitFor       = START_ELEMENT;
	private final XmlUtils  xml;
	


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTRUCTORS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	public RestraxParser(SimresCON program) {
		this.program = program;
		xml     = new XmlUtils();
		//buffer = new StringBuilder(BUFFER_CAPACITY);
		buffer = new StringBuffer(BUFFER_CAPACITY);
		try {
			xmlReader  = XMLReaderFactory.createXMLReader();
			xmlHandler = new ConsoleXmlHandler(xml,program);			
			xmlReader.setContentHandler(xmlHandler);
			xmlReader.setErrorHandler(xmlHandler);
		} catch (SAXException ex) {
			System.err.println("Creating XML parser: " + ex.getMessage());
			throw new IllegalStateException("Creating XML parser: " + ex.getMessage());
		}
	}

	protected class ConsoleXmlHandler extends DefaultXmlHandler {
		private final SimresCON  program;
		
		public ConsoleXmlHandler(XmlUtils  xml, SimresCON program) {
			super(xml);
			this.program = program;
		}
		
		@Override
		public RsxdumpExh getContentHandler() {
			return new RsxdumpExh(xml,program);
		}

		@Override
		public String preProcessContent(String content) {
			return content;
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                IMPLEMENTED INTERFACES                                //
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * <p>Tato metoda prijme string z konzole procesu restrax. Snazi se vyhledat
	 * pocatecni a koncove elementy xml vystupu. Vse  ostatni posila na neformatovanou
	 * textovou konzoli. Vse co je mezi pocatecnim a koncovym elementem (vcetne techto
	 * elementu) uchovava v bufferu a ve chvili, kdy prijde koncovy element vysle
	 * vysledky do xml parseru, ktery uz je obhospodaren. Vse co je po koncovem elementu
	 * odmazne a posle do dalsiho kola, protoze se muze i stat, ze v jednom stringu "receivu"
	 * muze byt nekolik start a end elementu. Takto dojde k obsluze i tech dalsich. </p>
	 * 
	 * <p><i>Poznamka:</i><br>
	 * substring(start, end) kopiruje vse od indexu start do indexu (end-1)!!!</p>
	 *
	 *
	 * <p>Muze se stat, ze nastane takovyto pripad:<dl>
	 * <dt>1. cteni z konzole</dt>
	 *     <dd>text text text text <RESTRAX_OU</code></dd>
	 * <dt>2. cteni z konzole</dt>
	 *     <dd>TPUT> xml xml xml xml xml xml </RESTRA</code></dd>
	 * <dt>3 cetni z konzole</dt>
	 *     <dd>X_OUTPUT> text text text</dd></dl></p>
	 * 
	 * <p>Pak by mi muj postup nefungoval, protoze by se vsechno posilalo na konzoli, protoze
	 * ani jednou by to nenaslo pocatecni nebo koncovy tag. Takovy pripad se asi mue stat, takze
	 * muzu:<ol type="a">
	 * <li>na textovou konzoli budu vse posilat az najdu vzdycky <START_ELEMENT> --- to by ale zpozdilo
	 *    zrejme nektere vypisy</li>
	 * <li>regulerne vyhledavat, najit i jen cast retezce a pak si do prite pamatovat, odkud zase zacit
	 *     testovat. Staci testovat vzdy zacatek a konec retezce v delce START ci END elementu. A zacatek
	 *     testovat pouze pokud z minuleho pripadu se nalezla na konci cast koncoveho elementu</li></ol></p>
	 */
	public void receive(String s) {
		String  temp  = null;
		String  elem  = null;
		int     start = -1;
		int     end   = -1;
		
		//synchronized(sync) {
		buffer.append(s);

		search:
			for ( ; ; ) {
				// nasledujici postup osetruje (asi zbytecne) pripad, kdy by nebyly
				// START_ELEMENT a END_ELEMENT spravne parovany, tedy resp. udela
				// chybnou XML extrakci a parser potom zakrici.
				if (waitFor == START_ELEMENT) {
					start = buffer.indexOf(START_ELEMENT);
					if (start!= -1) {
						waitFor = END_ELEMENT;
						
						if (__DEBUG__) {
							System.out.println(Utils.getDebugHdr("LOG"));
							System.out.println(buffer.substring(0, start));
							System.out.println();
						}//(__DEBUG__)				
						program.getConsoleLog().print(buffer.substring(0, start));
						
						// EventQueue.invokeLater( new RestraxParserRun(RestraxParserRun.CMD_LOG,buffer.substring(0, start)) );
						//program.getConsoleLog().print(buffer.substring(0, start));  // vse co je pred
						buffer.delete(0, start);    // zanech co je za vcetne prebytecneho elementu
					}
				}

				if (waitFor == END_ELEMENT) {
					end   = buffer.indexOf(END_ELEMENT);
					if (end != -1) {
						waitFor = START_ELEMENT;
			// !!! Parser must be executed in the event queue !!!
		    // RsxDump calls Swing components ...
						String content=buffer.substring(0, end + END_ELEMENT.length());
						EventQueue.invokeLater( new RestraxParserRun(RestraxParserRun.CMD_PARSE,content) );
						
						// parseXml(buffer.substring(0, end + END_ELEMENT.length()));  // vse co je pred vcetne koncoveho elemtu
						buffer.delete(0, end + END_ELEMENT.length()); // zanech co je za
						
						continue search;
					}
				}				
				// ze se nenasel START_ELEMENT nebo END_ELEMENT muze jeste znamenat, ze je tento
				// na konci rozdelen
				temp = buffer.toString();
				elem = (waitFor == START_ELEMENT) ? START_ELEMENT : END_ELEMENT;
				for (int i=(elem.length()-1); i>0; --i) {  // testuj od "<RESTRAX_OUTPUT" do "<"
					if (temp.endsWith(elem.substring(0, i))) {
						break search;  // konci to nedokoncenym elementem, pockej na dalsk davku
					}
				}				
				// Tak zde je jiste, ze se v bufferu nenachazi rozdeleny element. Jestlize
				// cekam na koncovy element, nedelam nic, protoze v bufferu mam uz ulozeny
				// XML obsah (protoze se nachazi mezi START_ELEMENT a END_ELEMENT)
				// Jestlize vsak cekam na START_ELEMENT, pak obsah bufferu musi obsahovat
				// jenom text mimo START_ELEMENT a END_ELEMENT (XML casti byly po parsingu
				//  vyriznuty), tudiz ho poslu na textovou konzoli a vyprazdn�m ho
				if (waitFor == START_ELEMENT) {
					if (__DEBUG__) {
						System.out.println(Utils.getDebugHdr("LOG"));
						System.out.println(buffer.toString());
						System.out.println();
					}//(__DEBUG__)				
					program.getConsoleLog().print(buffer.toString());
					// EventQueue.invokeLater( new RestraxParserRun(RestraxParserRun.CMD_LOG,buffer.toString()) );
					buffer.setLength(0);  // smaz vsechen obsah (rychlejsi nez "buffer.delete(0, buffer.length());")
				}				
				break search;
			}
		//sync.notifyAll();
		//}
	}

	private final class RestraxParserRun implements Runnable {
		private static final int CMD_PARSE=1;
		private static final int CMD_LOG=2;
		private final String content;
		private final int cmd;
		public RestraxParserRun(int cmd,String content) {
			super();
			this.content=content;
			this.cmd=cmd;
		}
		public void run() {
			//synchronized(sync) {
			switch(cmd) {
			case CMD_PARSE: 
				parseXml(this.content);
				break;
			case CMD_LOG:
				if (__DEBUG__) {
					System.out.println(Utils.getDebugHdr("LOG"));
					System.out.println(content);
					System.out.println();
				}//(__DEBUG__)				
				program.getConsoleLog().print(content);
			}
			//sync.notifyAll();
			//}
		}

		protected void parseXml(String content) {
			if ( __DEBUGXML__) {
				System.out.println(Utils.getDebugHdr());
				System.out.println(content);
				System.out.println();
			}//(__DEBUG__)
			try {				
				xmlReader.parse(new InputSource(new StringReader(content)));
			} catch (IOException ex) {
				System.err.printf("error %s.%s, RestraxParser\n","parseXml","IOException");
				System.err.println("Console XML I/O problem: " + ex.getMessage());
				System.err.print(content);
				program.Terminate();
			} catch (SAXException ex) {
				System.err.printf("error %s.%s, RestraxParser\n","parseXml","SAXException");
				System.err.print(content);
				ex.printStackTrace();
				System.err.println("Console XML parser: " + ex.getMessage());
				program.Terminate();
				if (__DEBUGXML__) {
					System.out.println(Utils.getDebugHdr());
					ex.printStackTrace();
					System.out.println();
				}//(__DEBUG__)
			} catch (Exception e) {
				System.err.printf("error %s.%s, RestraxParser\n","parseXml","Exception");
				System.err.print(content);
				e.printStackTrace();
				System.err.println("Console XML parser: " + e.getMessage());
			}


		}
	
	}
}