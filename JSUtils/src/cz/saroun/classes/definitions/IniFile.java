package cz.saroun.classes.definitions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.saroun.classes.definitions.Constants;




/**
 * Class that handles ini file format.
 * <h2>1 COMMENTS</h2>
 *     Only line comments are allowed.
 *     <br>
 *     Line comments start with either '#' or ';' character preceded by
 *     any number of empty characters. All characters between either
 *     '#' or ';' and end of line are ignored.
 *
 * <h2>2 SECTIONS</h2>
 *     This file is divided to several sections. Section name starts with
 *     '[' character preceded by any number of empty characters and ends
 *     with ']' character followed by any number of empty characters.
 *     <br>  
 *     Section name can contain any character (spaces included) except
 *     for '[', ']', '?' and '$' characters. The character sequence "->"
 *     is restricted too. It not good idea to include '#' and ';'
 *     characters as a section name (but it is possible).
 *     <br>
 *     Only one level sectioning is available (i.e. no subsections are
 *     allowed).
 *
 * <h2>3 KEY, VALUE</h2>
 *     Syntax of such definition is key=value. All characters (warning:
 *     spaces too) between start of line and FIRST '=' character is
 *     considered as a key. All characters (all means ALL) after FIRST
 *     '=' character is cionsidered as a value so it can contain '='
 *     characters too.
 *     <br>
 *     Key can contain any character except for '$', '?' (when included,
 *     it has special meaning, see below). The character sequence "->" is
 *     restricted too.  It not good idea to include '[', ']', '#' and ';'
 *     characters as a key name (but it is possible).
 *     <br>
 *     Value can contain ANY character. But if the name includes the '$'
 *     character, it must be dubled, i.e. put "$$" in the place of '$'.
 *
 * <h2>4 AUTOMATIC INDEXING</h2>
 *     Keys has the featur named as key indexing. When key contains '?'
 *     character at any position (it is aloowed only one per key) it is
 *     internally substituted by number of previously counted same keys.
 *     Number is placed on the place of '?' character.
 *     <br><br>
 *     Example:
 *     <table border="0">
 *        <tr><td>test?key</td><td>...</td><td>test0key</td></tr>
 *        <tr><td>test?key</td><td>...</td><td>test1key</td></tr>
 *        <tr><td>test?key</td><td>...</td><td>test2key</td></tr>
 *        <tr><td>testkey?</td><td>...</td><td>testkey0</td></tr>
 *        <tr><td>?testkey</td><td>...</td><td>0testkey</td></tr>
 *        <tr><td>testkey?</td><td>...</td><td>testkey1</td></tr>
 *        <tr><td>?testkey</td><td>...</td><td>1testkey</td></tr>
 *        <tr><td>...     </td><td>...</td><td>6x putted key test?key</td></tr>
 *        <tr><td>test?key</td><td>...</td><td>test9key</td></tr>
 *        <tr><td>test?key</td><td>...</td><td>test10key</td></tr>
 *    </table>
 *    As one can see "?testkey", "test?key" and "testkey?" are three different keys.
 *
 *    Number of keys is not limited.
 *
 *    This is useful for generating menus, when this feature easily allow change
 *    the order of item. With fix numbers user should reindex item to change their
 *    order in menu. User should not worry about start index (usualy 0 or 1) too.
 *    But be careful: when specifiyng menus items where each items contain several
 *    parameters, check that all parameters are copyied even in case they are empty.
 *    <br><br>
 *    Example:
 *    <table border="0">
 *       <tr><td>Indexing mode   </td><td>Non-indexing    </td></tr>
 *       <tr><td>item?.parA=par1A</td><td>item0.parA=par1A</td></tr>
 *       <tr><td>item?.parB=par1B</td><td>item0.parB=par1B</td></tr>
 *       <tr><td>item?.parC=     </td><td>                </td></tr>
 *       <tr><td>                </td><td>                </td></tr>
 *       <tr><td>item?.parA=par2A</td><td>item0.parA=par2A</td></tr>
 *       <tr><td>item?.parB=par2B</td><td>item0.parB=par2B</td></tr>
 *       <tr><td>item?.parC=par2C</td><td>item0.parC=par2C</td></tr>
 *    </table>
 *    Note that in non indexing mode one does not have to specify item0.parC. But in indexing
 *    mode one MUST, because one must force internal counter of keys to count correctly.
 *
 * <h2>5 VALUE REFERENCING</h2>
 *     Previously defined values can be referenced in the new value using
 *     "$section name->key$" syntax. All characters between '$' and "->"
 *     are considered to be section name, so there can be white spaces.
 *     Note that even value in the same section must contain name of the
 *     section in reference. All characters between "->" and '$' are
 *     considered to be key. Key must be explicitly set, so '?' characters
 *     are not allowed. If the value should contain '$' character, it must
 *     be doubled: "$$".
 *     <br><br>
 *     Example:
 *     <pre>
 *        [example section 1]
 *            ekey=string value $$
 *            ekey1=other string value
 *
 *        [example section 2]
 *            ekey=$example section 1->ekey$
 *            ekey1=$$example section 2->ekey$$
 *            ekey2=$$example section 2->ekey$
 *            ekey3=$$$example section 2->ekey$$$ text added: $example section 1->ekey1$
 *     </pre>
 *     <br>
 *     Result:<dl>
 *        <dt>"ekey"  in "example section 1" contains "string value $"</dt>
 *        <dt>"ekey1" in "example section 1" contains "other string value"</dt>
 *        <dt>"ekey"  in "example section 2" contains "string value $"</dt>
 *        <dt>"ekey1" in "example section 2" contains "$example section 2->ekey$"</dt>
 *        <dt>"ekey2" in "example section 2" causes error because expression is ambiguous:<dl>
 *                <dt>'$'  character  at the beginning is not corectly doubled ("$$$example...")</dt>
 *                <dt>or "$$" characters at the beginning should be replaced by simple character '$'</dt>
 *                <dt>or '$'  character  at the end is not corectly doubled ("...ekey$$")</dt>
 *                <dt>or '$'  character  at the end should be omitted ("...ekey")</dt></dl></dt>
 *        <dt>"ekey3" in "example section 2" contains "$string value $$ text added: other string value"</dt></dl>
 *
 * <h2>6 OTHER</h2>
 *     There can be empty lines (empty or containing any number of spaced,
 *     tabulator characters) in the file.
 *
 *
 * @author   Svoboda Ji��, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.2 $</dt>
 *               <dt>$Date: 2019/07/10 18:42:31 $</dt></dl>
 */
public class IniFile {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private String                                  iniFile = null;
	private HashMap<String,HashMap<String,String>>  iniMap  = null;


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public IniFile(String iniFile) {
		this.iniFile = iniFile;
		processIniFile();
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    ACCESS METHODS                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	public String getFileName() {
		return iniFile;
	}
	
	
	public HashMap<String,String> getSection(String section) {
		return iniMap.get(section);
	}

	/**
	 * Find specified value from the ini file. If not found, throw exception.
	 */
	public String getNonNullValueOrDie(String section, String key) {
		//zde je treba testovat zda klic opravdu existuje i kdyz muze mit hodnotu null
		testKeyPresence(section, key);

		String value = iniMap.get(section).get(key);
		if (value == null) {
			throw new IllegalArgumentException("Variable '" + key + "' in section '" + section + "' in file '" + iniFile + "' is empty");
		}

		return value;
	}
	
	/**
	 * Find specified value from the ini file. If not found, return defaultValue.
	 */
	public String getNonNullValue(String section, String key, String defaultValue) {
		// return null if there is no such key
		// testKeyPresence(section, key);
		String val=defaultValue;
		HashMap<String,String> s = iniMap.get(section);
		if (s!=null) val=s.get(key);
		return  val;
	}
	
	/**
	 * Find specified value from the ini file. If not found, return null.
	 */
	public String getValue(String section, String key) {
		HashMap<String,String> s = iniMap.get(section);
		if (s!=null) {
			return s.get(key);
		} else return null;
	}

	public boolean isKeyPresent(String section, String key) {
		return (iniMap.containsKey(section) && iniMap.get(section).containsKey(key));
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    OTHER METHODS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	public void testKeyPresence(String section, String key) {
		if (isKeyPresent(section, key) == false) {
			throw new IllegalArgumentException("Variable '" + key + "' in section '" + section + "' is missing in file '" + iniFile + "'");
		}
	}

	public void processIniFile() {
		String   content         = null;
		Scanner  iniParser       = null;
		String   key             = null;
		String   line            = null;
		String   value           = null;
		String   currentSection  = "";
		Pattern  skipPattern     = Pattern.compile("(^\\s*$)|(^\\s*(#|;))");  // empty line or comment
		Pattern  sectionPattern  = Pattern.compile("^\\s*\\[.*?\\]\\s*$");  // section
		Pattern  keyPattern      = Pattern.compile("^.*?=");
		Pattern  indexPattern    = Pattern.compile("\\?");
		Pattern  refPattern      = Pattern.compile("\\$.*?\\$");
		Pattern  refSplitPattern = Pattern.compile("->");

		try {
			content   = Utils.readFileToString(iniFile);
		} catch (IOException ex) {
			throw new IllegalStateException("I/O problem when reading ini file '" + iniFile + "':\n" + ex.getMessage());
		}

		iniMap = new HashMap<String,HashMap<String,String>>();

		iniParser = new Scanner(content);
		iniParser.useLocale(Constants.FIX_LOCALE);

		while (iniParser.hasNextLine()) {
			line = iniParser.nextLine();
		//	System.out.printf("%s\n",line );	
			/*"******************************************************
			 * komentare
			 ********************************************************/
			if (skipPattern.matcher(line).find()) {  // prazdna radka ci komentar
				continue;
			}

			/*"******************************************************
			 * sekce
			 ********************************************************/
			Matcher sectionMatcher = sectionPattern.matcher(line);
			if (sectionMatcher.find()) {
				String section = sectionMatcher.group().trim();
				currentSection  = section.substring(1, section.length()-1).trim();  //odstram '[' a ']'
				if (iniMap.containsKey(currentSection) == false) {
					iniMap.put(currentSection, new HashMap<String,String>());
				}
				continue;
			}

			/*"******************************************************
			 * klic=hodnota
			 ********************************************************/
			Matcher keyMatcher = keyPattern.matcher(line);
			if (keyMatcher.find()) {
				key = keyMatcher.group();
				key = key.substring(0, key.length()-1);      // odstran '=' na konci
				value = line.substring(keyMatcher.end());    // matcher se maximalne zastavi na konci, takze substring vrati v tomto 
				                                             // pripade "" (nehodi exception) --- "ahoj".substring(4) -> ""
				if (value.trim().length() == 0) {  // jenom prazdne znaky
					value = null;
				}
			} else {				
				System.out.printf("Bad syntax in ini file %s: '=' is missing\nline=%s\n",iniFile,line );
				key=null;
				value=null;
			}
			
			/*"******************************************************
			 * zpracovani klic=hodnota
			 ********************************************************/
			// Vylepseni I --- moznost indexace -> nahrada znaku '?' poradovym cislem
			// 
			// OMEZENI: 1) klic nesmi v nazvu obsahovat '?', jiank to se nahradi indexem
			//          2) pokud v klici budou dva a vice znaku '?' nahradi se pouze prvni
			//          3) referencovat lze jedine jiz drive nastavene hodnoty -> pak je mozna z principu i neomezena uroven nahrady
			//             --- hodnota muze referencovat klic, ktery referencuje jiny klic atd. a nehrozi pritom zacykleni!
			if ((key != null) && key.contains("?")) {
				String[] parts = indexPattern.split(key, 2);  // na dve casti 

				for (int i=0;; ++i) {
					key = parts[0] + i + parts[1];
					
					if (iniMap.get(currentSection).containsKey(key) == false) {
						break;  // upravil klic na hodnotu ktera jeste neexistuje, skonci a jdi zpracovat hodnotu
					}
				}
			}
			
			// vylepseni II --- moznost referencovani:
			// klic = hodnota$JMENO_SEKCE->KLIC$hodnota
			// a pokud bych chtel v hodnote zadat '$' zadam escape sekvenci "$$"
			// 
			// OMEZENI: 1) klic nesmi v nazvu obsahovat '->'
			//          2) klic nesmi v nazvu obsahovat '$' (i kdybych udelal escaoe znaku '$' na "$$" i ve jmenu klice,
			//             jak by se interpretoval takovyto pripad: $alfa$$$$beta$text s$$dolarem, jako alfa$$beta, nebo jako
			//             alfa$$ a beta, nebo alfa a $beta nebo jako alfa a beta mezi nimiz je znak $? To je nejednoznacne,
			//             vsemu se vyhnu vyloucim-li znak '$' z nazvu klice)
			//
			// Pozn. Jelikoz ukladam klic=hodnota az na konci, nehrozi zacykleni pri odkazu na sebe sama, protoze
			//       v tuto chvili jeste tento klic v iniMap neexistuje
			if ((value != null) && (value.contains("$"))) {
				Matcher  matcher = refPattern.matcher(value);
				String   valueNew  = "";
				for (int startInd = 0; ; ) {
					if (matcher.find(startInd) == false) {
						if (value.substring(startInd).contains("$")) {
							// nenajde par $$, ale znak '$' se tam vyskytuje --- je spatne
							// escapovany, pocet $$ musi byt sudy
							iniParser.close();
					    	throw new IllegalArgumentException("Bad syntax in ini file '" + iniFile + "': '$' must be escaped as \"$$\"");
						} else {
							// end of replacing
							// zkopiruj zbytek puvodniho klice
							valueNew += value.substring(startInd);
							// nahrad puvodni hodnotu a skonci
							value = valueNew;
							break;
						}
					}

					String reference = matcher.group();
					// odstran '$' na pocatku a na konci, jsou pritomny, minimalne takto "$$"
					reference = reference.substring(1, reference.length()-1);
					
					if (reference.length() == 0) {  // empty reference i.e. "$$" --- but this is escape sequence for '$'
						valueNew += value.substring(startInd, matcher.start()) + "$";
					} else {
						String[] refParts = refSplitPattern.split(reference, 2);  // na dve casti
						if (refParts.length == 1) { // length == 1 --- nenasel a tudiz refParts[0] je cely puvodni string
							iniParser.close();
							throw new IllegalArgumentException("Bad syntax in ini file '" + iniFile + "': '->' is missing in reference '" + reference + "'");
						}
						String refSection = refParts[0];
						String refKey     = refParts[1];

						if ( (iniMap.containsKey(refSection) == false) ||
								(iniMap.get(refSection).containsKey(refKey) == false)) { 
							iniParser.close();
							throw new IllegalArgumentException("Bad syntax in ini file '" + iniFile +
									"': Undefined reference to section '" + refSection +
									"' and key '" + refKey + "'.");
						}
						
						String refValue = iniMap.get(refSection).get(refKey);
						valueNew += value.substring(startInd, matcher.start()) + refValue;
					}

					startInd = matcher.end();
				}
				iniMap.get(currentSection).put(key, valueNew);
			}
			
			/*"******************************************************
			 * ulozeni upraveneho klic=hodnota
			 ********************************************************/
			if (key!=null) iniMap.get(currentSection).put(key, value);
		}
		
		iniParser.close();
	}
}
