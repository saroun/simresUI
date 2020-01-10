package cz.jstools.classes.definitions;

import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JTextField;


/**
 * Methods for number-string conversions, getting numbers from 
 * "JTextFiels", working with file names, debug report formatting etc.  
 *
 *
 * @author   Svoboda Jan Saroun, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.8 $</dt>
 *               <dt>$Date: 2014/06/18 20:07:35 $</dt></dl>
 */
public class Utils {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTANTS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	private static final long  MAX_FILE_SIZE = 524288;    // 512kB
	private static final int   BUFFER_SIZE   = 4096;     // 4kB
	
	// defines float precision for output
	public static final double   EXP_FORM_MIN  = 0.1;
	public static final double   EXP_FORM_MAX  = 10000.0;


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CLASS METHODS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	
	public static String wrapOnLimit(String str,int limit) {
		String out="";
		int n=str.length()/limit;
		if (n>0) {
		String cont="";
		for (int i=0;i<n;i++) {
			cont += str.substring(i*limit, (i+1)*limit);
			cont += "\n";
		}
		if (n*limit<str.length()) cont += str.substring(n*limit, str.length());
		out=cont;
		} else {
			out=str;
		}
		return out;
	}
	
	
	/**
	 * Convert String[] to Vector<String>
	 * Items are trimmed.
	 * @param list
	 * @return
	 */
	public static Vector<String> toVector(String[] list) {
		Vector<String> V=new Vector<String>();
		if (list != null) {
			for (int i=0;i<list.length;i++) {
				V.add(list[i].trim());
			}
		}
		return V;
	}
	
	/**
	 * Flip the status of Switch
	 * @param val  integer value to be formatted
	 * @return html format of the integer value
	 */
	public static Switch flip(Switch val) {
		switch (val) {
		case OFF: return Switch.ON;
		default: return Switch.OFF;		
		}	
	}
	
	
	/**
	 * Replace some math symbols to html.
	 * e.g. x^n, ..
	 * @return
	 */
	public static String math2Html(String source) {
		String s = new String(source);
		if (s.equals("A")){
			s="&Aring;";
		} else if (s.matches("A[.^]")) {
			s=s.replaceAll("A([.^])","&Aring;$1");
		} else {			
			s=s.replaceAll("\\^(\\d+)","<sup>$1</sup>");
			s=s.replaceAll("\\^-(\\d+)","<sup>-$1</sup>");
			s=s.replaceAll("([.\\[])(A)([.^\\]])","$1&Aring;$3");			
		}
		return "<html>"+s+"</html>";
	}
	
	/**
	 * Double to string intelligent conversion. It uses DecimalFormat.
	 */
	public static String d2s(double val) {
		NumberFormat  exporter;

		exporter = NumberFormat.getInstance(Constants.FIX_LOCALE);
		if (!(exporter instanceof  DecimalFormat)) {  // United States FIX_LOCALE (Locale.US) should always return DecimalFormat
			throw new IllegalStateException("Unable to get rules for number formating...");
		}
		
		if (((Math.abs(val) > EXP_FORM_MIN) && (Math.abs(val) < EXP_FORM_MAX)) ||
		    (val == 0.0)){
			((DecimalFormat) exporter).applyPattern("0.0####");
		} else {
			((DecimalFormat) exporter).applyPattern("0.0####E0");
		}
		
		return ((DecimalFormat) exporter).format(val).toString();
	}
	
	/**
	 * Return float numbers in fortran double format:, e.g. 1.0D2 
	 * @param val
	 * @return
	 */
	public static String d2sf(double val) {		
		String sn = d2s(val).replaceAll("[Ee]","D");			
		if (!sn.contains("D")) sn = sn+"D0";					
		return sn;
	}

	
	public static DecimalFormat getNumberFormat() {
		DecimalFormatSymbols  symbols  = null;
		DecimalFormat         exporter = null;

		symbols = new DecimalFormatSymbols(Constants.FIX_LOCALE);
		exporter = new DecimalFormat("0.#####", symbols);

		return exporter;
	}
	/**
	 * Generates html form of the double value. Minus sign and eventual exponencial mark
	 * are properly formated.
	 * <b>Warning:</b> this function do not encapsulate the result in <html> and </html>
	 * tags so result can be added into another piece of html code.
	 * 
	 * @param val  double value to be formatted
	 * @return html format of the double value
	 */
	public static String d2html(double val) {
		DecimalFormatSymbols  symbols  = null;
		DecimalFormat         exporter = null;
		String                output   = null;

		symbols = new DecimalFormatSymbols(Constants.FIX_LOCALE);
		
		if (((Math.abs(val) > EXP_FORM_MIN) && (Math.abs(val) < EXP_FORM_MAX)) ||
		    (val == 0.0)){
			exporter = new DecimalFormat("0.0####", symbols);
			String s = exporter.format(val).toString();
			output = (s.startsWith("-") ? ("&#x2212;" + s.substring(1)) : s); 
		} else {
			exporter = new DecimalFormat("0.0####E0", symbols);
			String s = exporter.format(val).toString();
			String[] parts = s.split("E");
			output = (parts[0].startsWith("-") ? ("&#x2212;" + parts[0].substring(1)) : parts[0])
			       + "<small>E</small>"
			       + (parts[1].startsWith("-") ? ("&#x2212;" + parts[1].substring(1)) : parts[1]);
		}
		
		return output;
	}


	/**
	 * Integer to string conversion.
	 * 
	 * @param val integer value
	 * @return string value of integer val
	 */
	public static String i2s(int val) {
		return Integer.toString(val);
	}
	
	/**
	 * Generates html form of the integer value. Minus sign is properly formated.
	 * <b>Warning:</b> this function do not encapsulate the result in <html> and </html>
	 * tags so result can be added into another piece of html code.
	 * 
	 * @param val  integer value to be formatted
	 * @return html format of the integer value
	 */
	public static String i2html(int val) {
		String  output   = null;
		String  s = Integer.toString(val);
		output = (s.startsWith("-") ? ("&#x2212;" + s.substring(1)) : s); 
		return output;
	}


	/**
	 * String to double conversion.
	 * 
	 * @param str String with double value
	 * @return double value from string str
	 * @throws ParseException
	 */
	public static double s2de(String str) throws ParseException {
		double val;
		
		try {
			val = Double.parseDouble(str);
		} catch (NumberFormatException ex) {
			throw new ParseException("Cannot parse \"" + str + "\"", 0);
		}
		
		return val;
	}

	public static int s2ie(String str1) throws ParseException {
		String str2 = str1.trim();
		int val;
		
		try {
			val = Integer.parseInt(str2);
		} catch (NumberFormatException ex) {
			throw new ParseException("Cannot parse \"" + str2 + "\"", 0);
		}
		
		return val;
	}
	
//	public static int s2ie(String str) throws ParseException {
//		int val;
//		
//		try {
//			val = Integer.parseInt(str);
//		} catch (NumberFormatException ex) {
//			throw new ParseException("Cannot parse \"" + str + "\"", 0);
//		}
//		
//		return val;
//	}


	public static double getDoubleFromTextField(JTextField textField) throws ParseException {
		double val;
		
		try {
			val = Double.parseDouble(textField.getText());
		} catch (NumberFormatException ex) {
			throw new ParseException("Cannot parse field <" + textField.getName() + ">: \"" + textField.getText() + "\"", 0);
		}
		
		return val;
	}
	
	/**
	 * Tato nmetoda je definovana v Utils aby se drzely blizko sebe hlasky tykajici se JTextField,
	 * nebot maji urcity format.
	 * 
	 * @param textField
	 * @throws ParseException
	 */
	public static void throwParsingEmptyInput(JTextField textField) throws ParseException {
		throw new ParseException("Cannot parse field <" + textField.getName() + "> because it is empty", 0);
	}

	
	public static int getIntFromTextField(JTextField textField) throws ParseException {
		int val;
		
		try {
			val = Integer.parseInt(textField.getText());
		} catch (NumberFormatException ex) {
			throw new ParseException("Cannot parse field <" + textField.getName() + ">: \"" + textField.getText() + "\"", 0);
		}
		
		return val;
	}

	public static void appendStringToFile(String fileName, String content) throws IOException {
		BufferedWriter  fw;			
		fw = new BufferedWriter(new FileWriter(fileName, true));
		fw.write(content);
		fw.flush();
		fw.close();	
	}
	

	public static void writeStringToFile(String fileName, String content) throws IOException {
		BufferedWriter  fw;		
		fw = new BufferedWriter(new FileWriter(fileName));
		fw.write(content);
		fw.flush();
		fw.close();	
	}
	
	public static void writeStringToFile(File fileName, String content) throws IOException {
		BufferedWriter  fw;		
		fw = new BufferedWriter(new FileWriter(fileName));
		fw.write(content);
		fw.flush();
		fw.close();	
	}
	
	public static void writeUTF8ToFile(File file, String content) throws IOException {
		BufferedWriter out = 
			new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF8"));	
		out.write(content);
		out.flush();
		out.close();
	}
	
	public static void copyFile(File source, File destination) throws IOException {
		BufferedReader  fr;
		BufferedWriter  fw;
		int             n;
		char[]          buffer = new char[BUFFER_SIZE];

	    
		fr     = new BufferedReader(new FileReader(source));
		fw     = new BufferedWriter(new FileWriter(destination));
		
		while ((n = fr.read(buffer)) != -1) {
			fw.write(buffer, 0, n);
		}
		
		fr.close();
		fw.flush();
		fw.close();
	}
	
	public static void appendStringToFile(File fileName, String content) throws IOException {
		BufferedWriter  fw;
		
		fw = new BufferedWriter(new FileWriter(fileName, true));
		fw.write(content);
		fw.flush();
		fw.close();	
	}

	public static Properties readFileToProperties(String fileName) throws IOException {
		File            file;
		Properties      content = null;
		file   = new File(fileName);		
		if (file.length() > MAX_FILE_SIZE) {
			throw new IOException("Size of file: " + fileName + " exceeds limit (" + MAX_FILE_SIZE + "B).");
		}			
		content = new Properties();

/*		this works with Java 1.6
        BufferedReade fr = new BufferedReader(new FileReader(file));
		content.load(fr);
		fr.close();
*/	
// and this is compatible with Java 1.5:
		FileInputStream fs = new FileInputStream(file);
		content.load(fs);
		fs.close();
		
		return content;
	}


	public static String readFileToString(String fileName) throws IOException {
		File            file;
		BufferedReader  fr;
		int             n;
		char[]          buffer = new char[BUFFER_SIZE];
		String          content = "";
	
		
		file   = new File(fileName);
		if (! file.exists()) {
			throw new IOException("File " + fileName + " does not exist.");			
		}
		
		if (file.length() > MAX_FILE_SIZE) {
			throw new IOException("Size of file: " + fileName + " exceeds limit (" + MAX_FILE_SIZE + "B).");
		}
			
		fr     = new BufferedReader(new FileReader(file));
			
		while ((n = fr.read(buffer)) != -1) {
			content += String.valueOf(buffer, 0, n);
		}
		
		fr.close();
			
		return content;
	}
	
	public static String readFileToString(File file) throws IOException {
		BufferedReader  fr;
		int             n;
		char[]          buffer = new char[BUFFER_SIZE];
		String          content = "";
		
		if (file.length() > MAX_FILE_SIZE) {
			throw new IOException("Size of file: " + file.length() + " exceeds limit (" + MAX_FILE_SIZE + "B).");
		}
			
		fr     = new BufferedReader(new FileReader(file));
			
		while ((n = fr.read(buffer)) != -1) {
			content += String.valueOf(buffer, 0, n);
		}
		
		fr.close();
			
		return content;
	}

	public static boolean hasExtension(File file, String ext1) {
		String ext2;
		
		ext2 = Utils.getExtension(file);
		if (ext2 == null) {  // has no extension or is special file (e.g. '.settings')
			return false;
		}
	
		return ext2.equals(ext1);
	}

	public static boolean hasExtension(String fileName, String ext1) {
		String ext2;
		
		ext2 = Utils.getExtension(fileName);
		if (ext2 == null) {  // has no extension or is special file (e.g. '.settings')
			return false;
		}
	
		return ext2.equals(ext1);
	}

	public static String getExtension(File file) {
		String ext = null;
		String fileName;
		int i;
		
		fileName = file.getName();
		i = fileName.lastIndexOf('.');
		if ((i > 0) && (i < (fileName.length()-1))) {
			ext = fileName.substring(i+1).toLowerCase();
		}
		
		return ext;
	}

	public static String getExtension(String fileName) {
		String ext = null;
		int i;
		
		i = fileName.lastIndexOf('.');
		if ((i > 0) && (i < (fileName.length()-1))) {
			ext = fileName.substring(i+1).toLowerCase();
		}		
		return ext;
	}

	/**
	 * Replace extension in the file name with the given one. Append the given extension if 
	 * the original file name has no extension.
	 * @param fileName, the file name with replaced or appended extension
	 * @return
	 */
	public static String setExtension(String fileName, String ext1) {
		String ext = getExtension(fileName);
		if (ext==null) {
			return fileName+"."+ext1;
		} else {
			String[] parts=fileName.split("[.]");
			String f = parts[0]+".";
			for (int i=1;i<parts.length-1;i++) {
				f += parts[i]+".";
			}
			f += ext1;
			return f;
		}		
	}
	
	/**
	 * Return the filename without extension
	 * @return
	 */
	public static String cutExtension(String fileName) {
		String ext = getExtension(fileName);
		if (ext==null) {
			return fileName;
		} else {
			String[] parts=fileName.split("[.]");
			String f = parts[0];
			for (int i=1;i<parts.length-1;i++) {
				f += "."+parts[i];
			}
			return f;
		}		
	}
	
	/**
	 * Vrac� jm�no adres��e. Na konci neobsahuje jm�no separ�toru. 
	 */
	public static String getDirectory(File file) {
		String dir = null;
		if (file.exists()) {
			if (file.isFile()) {
				File pfile = file.getParentFile(); 
				if (pfile == null) {
					dir = null;
				} else {
					dir = pfile.getPath();
				}
			} else {
				dir = file.getPath();
			}
		} else {
			File file2 = file;
			while ((file2 != null) && (file2.exists() == false)) {
				file2 = file2.getParentFile();
			}
			if (file2 == null) {
				dir = null;
			} else {
				dir = file2.getPath();
			}
		}

		return dir;
	}
	
	/**
	 * Returns address name. No separator at the end.
	 */
	public static String getDirectory(String fileName) {
		return Utils.getDirectory(new File(fileName));
	}
	

	public static Point getLocationFromIniFile(IniFile iniFile, String iniSectionName) {
		String locationStr = iniFile.getValue(iniSectionName, "location");
		
		if (locationStr==null) return null;
		String[] locationParts = locationStr.split(",");
		
		if (locationParts.length != 2) {
			throw new IllegalArgumentException("Variable 'location' in section '" + iniSectionName + "' in file '" + 
			                                   iniFile.getFileName() + "' must have syntax 'x,y': '" + locationStr + "'.");
		}
		
		String  source   = null;
		Point   location = new Point();
		try {
			source="x-location from variable 'location'";
				location.x = Utils.s2ie(locationParts[0]);
			source="y-location from variable 'location'";
			location.y = Utils.s2ie(locationParts[1]);
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Cannot parse " + source + " in section '" + iniSectionName + "' in file '" + 
			                                   iniFile.getFileName() + "': " + ex.getMessage());
		}

		return location;
	}


	public static Dimension getSizeFromIniFile(IniFile iniFile, String iniSectionName) {
		String sizeStr = iniFile.getValue(iniSectionName, "size");
		if (sizeStr==null) return null;
		String[] sizeParts = sizeStr.split(",");
		
		if (sizeParts.length != 2) {
			throw new IllegalArgumentException("Variable 'size' in section '" + iniSectionName + "' in file '" + 
			                                   iniFile.getFileName() + "' must have syntax 'width,height': '" + sizeStr + "'.");
		}
		
		String     source = null;
		Dimension  size   = new Dimension();
		try {
			source="width from variable 'size'";
				size.width = Utils.s2ie(sizeParts[0]);
			source="height from variable 'size'";
				size.height = Utils.s2ie(sizeParts[1]);
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Cannot parse " + source + " in section '" + iniSectionName + "' in file '" + 
			                                   iniFile.getFileName() + "': " + ex.getMessage());
		}
		
		return size;
	}

	
	public static boolean getIsIconizedFromIniFile(IniFile iniFile, String iniSectionName) {
		String iconizedStr = iniFile.getValue(iniSectionName, "iconized");
		if (iconizedStr==null) return false;
		iconizedStr = iconizedStr.trim();
		
		if (iconizedStr.equals("true")) {
			return true;
		} else if (iconizedStr.equals("false")) {
			return false;
		} else {
			throw new IllegalArgumentException("Variable 'iconized' in section '" + iniSectionName + "' in file '" + 
			                                   iniFile.getFileName() + "' must be 'true' or 'false': '" + iconizedStr + "'.");
		}
	}


	public static String getDebugSep() {
		return "---------------------------------------------------------------------------";
	}

	
	public static String getDebugHdr() {
		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		StackTraceElement  callingObject = ste[3];
		
		return ("[" + callingObject.getMethodName()+ "::" + callingObject.getClassName() + "]");
	}

	public static String getDebugHdr(String msg) {
		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		StackTraceElement  callingObject = ste[3];
		
		return ("[" + callingObject.getMethodName()+ "::" + callingObject.getClassName() + ": '"+ msg + "']");
	
	}	
	


}


