package cz.jstools.util;

import java.io.File;
import java.io.IOException;

import cz.jstools.classes.definitions.Utils;

/**
 * Extends FileLogger to allow for HTML output. Provides primitives for formatting various 
 * data types like values with physical units, enumerated types, tables of values etc.
 * @author  Jan Saroun
 * @version  <dl><dt>$Revision</dt>
 *               <dt>$Date: 2015/08/20 17:38:42 $</dt></dl>
 */
public class HTMLLogger extends FileLogger {
	public static final String   HTML_META_CONTENT = "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />";
	public static final String   HTML_PROLOGUE = "<!DOCTYPE html>\n<html>\n<head>\n";
	public static final String   HEAD_EPILOGUE = "</head>\n<body>\n";
	public static final String   HTML_EPILOGUE = "</body>\n</html>\n";
	public static final String   STYLE_PROLOGUE =  "<style type='text/css'>\n"+"<!--\n";
	public static final String   STYLE_EPILOGUE =  "-->\n"+"</style>\n";	
	public static final int       WARNING_MSG             = 1;
	public static final int       ERROR_MSG               = 2;
	public static final int       INFO_MSG                = 4;
	public static final int       TITLE_MSG               = 8;
	public static final int    MSG_MASK = WARNING_MSG | ERROR_MSG | INFO_MSG | TITLE_MSG;
	
	private String stylesheet="";
	
	public HTMLLogger(FileLogger logger) {
		super(logger);
		// no incremental save for HTML 
		saveIncremental=false;
	}	
	public HTMLLogger() {
		super();
		saveIncremental=false;
	}

	@Override
	public void println(String text) {
		addText(text+"</br>\n");
	}
	@Override
	public void flushToFile(boolean append) {
		flushHtmlToFile();
	}

	/**
	 * Save the logger content.
	 */
	public boolean SaveToFile(File f) {
		boolean res=false;
		if (f==null) return false;
		if (records.size()>0) {
			try {
				Utils.writeUTF8ToFile(f, getHtmlContent());
				res=true;
			} catch (IOException e) {
				System.err.printf("%s: Problem when writing to %s\n",this.getClass().getName(),f.toString());
			}
		}
		return res;
	}
	
	/**
	 * Append the logger content to the log file and clear records.
	 */
	public void flushHtmlToFile() {
		if (logF==null) return;
		if (hasUnsavedResults && records.size()>0) {
				try {
					Utils.writeUTF8ToFile(logF, getHtmlContent());
					hasUnsavedResults=false;
				} catch (IOException e) {
					System.err.printf("%s: Problem when writing to %s\n",this.getClass().getName(),logFile);
					logF=null;
					try {
						setAutoSave(true,false);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
		}
	}		

	/**
	 * Set CSS style sheet as a text. Logger must be empty and disabled.
	 * @param css
	 * @throws Exception 
	 */
	public void setCSSStyle(String css) throws Exception {
		if (! enabled && records.size()==0) {
			stylesheet=css;	
		} else {
			throw new Exception("Can''t set style sheet data on enabled or non-empty logger");
		}
	}
			
	/**
	 * Get complete HTML content with headers and root tags (html, body).
	 * @return
	 */
	public String getHtmlContent() {
		String html="";
		html += HTML_PROLOGUE + HTML_META_CONTENT;
		if (! stylesheet.equals("")) {
			html += STYLE_PROLOGUE + stylesheet + STYLE_EPILOGUE;
		}
		html += HEAD_EPILOGUE;
		html += getContent();
		html += HTML_EPILOGUE;		
		return html;
	}

	public void saveLogs() {
		flushHtmlToFile();		
	}
	
	/**
	 * Prints a list of values in a table:<br />
	 * caption<br />
	 * <table cellspacing="0px">
	 * <tr>
	 * <td>name</td><td>=</td><td>value +- error</td><td>[units]</td>
	 * </tr>
	 * </table>
	 * Formatting is via css classes for TD tags (name,equal, value, unit) 
	 * @param caption
	 * @param names
	 * @param values
	 * @param errors
	 * @param units
	 */
	public void printList(String caption, String[] names, String[] values, String[] errors, String[] units) {
		StringBuffer  buffer = new StringBuffer(1024);

		if (caption != null) {
			buffer.append(caption + "<br />\n");
		}
		buffer.append("<table cellspacing='0px'>\n");
		
		for (int i=0; i<names.length; ++i) {
			buffer.append("<tr>\n"
			             +"<td class=\"name\">" + names[i] + "</td>\n"
			             +"<td class=\"equal\">=</td>\n");
			if (errors != null) {
				buffer.append("<td class=\"value\">" + values[i] 
					+ " \u00B1 " + errors[i] + "</td>\n");
			} else {
				buffer.append("<td class=\"value\">" + values[i] + "</td>\n");
			}
			if (units != null) {
				if (units[i].length() > 0) {
					buffer.append("<td class=\"unit\">[" + units[i] + "]</td>\n");
				} else {
					buffer.append("<td class=\"unit\">&nbsp;</td>\n");
				}
			}
			buffer.append("</tr>\n");
		}
		buffer.append("</table>\n");

		addText(buffer.toString());
	}

	public void println() {
		addText("<br />\n");
	}
	
	/**
	 * calls printValue(null, name, value, unit);
	 */
	public void printValue(String name, String value, String unit) {
		printValue(null, name, value, unit);
	}
	/**
	 * calls printList(caption, names, values, null,units);
	 */
	public void printValue(String caption, String name, String value, String unit) {
		String[] names =  {name};
		String[] values = {value};
		String[] units = {unit};
		
		printList(caption, names, values, null,units);
	}
	/**
	 * calls printFValue(null, name, value, error,unit);
	 */
	public void printFValue(String name, String value, String error, String unit) {
		printFValue(null, name, value, error,unit);
	}	
	
	
	/**
	 * calls printList(caption, names, values, errors,units);
	 */
	public void printFValue(String caption,String name, String value, String error, String unit) {
		String[] names =  {name};
		String[] values = {value};
		String[] errors = {error};
		String[] units = {unit};		
		printList(caption, names, values, errors,units);
	}
	
	/**
	 * calls printList(null, names, values, null,units);
	 */
	public void printList(String[] names, String[] values, String[] units) {
		printList(null, names, values, null,units);
	}
	
	/**
	 * calls printList(null, names, values, null,null);
	 */
	public void printList(String[] names, String[] values) {
		printList(null, names, values, null,null);
	}

	/**
	 * calls printList(caption, names, values, null,null);
	 */
	public void printList(String caption, String[] names, String[] values) {
		printList(caption, names, values, null,null);
	}

	/**
	 * Print text in FONT block formatted by class names according to "what" value
	 * (warning, info, error)
	 * @param text
	 * @param parameters
	 */
	public void printlns(String text, int what) {
		switch (what & MSG_MASK) {
			case WARNING_MSG:
				println("<p><font class='warning'>" + text + "</font></p>\n");
				break;
			case INFO_MSG:
				println("<p><font class='info'>" + text + "</font></p>\n");
				break;
			case ERROR_MSG:
				println("<p><font class='error'>" + text + "</font></p>\n");
				break;
			default:
				throw new IllegalArgumentException("Type of message is wrong.");
		}
	}
	/*
	public void printSeparator(String text, int parameters) {
		switch (parameters & MSG_MASK) {
			case WARNING_MSG:
				print("<div class='warning'>" + text + "</div>\n");
				break;
			case INFO_MSG:
				print("<div class='info'>" + text + "</div>\n");
				break;
			case ERROR_MSG:
				print("<div class='error'>" + text + "</div>\n");
				break;
			case TITLE_MSG:
				print("<div class='error'>" + text + "</div>\n");
				break;
			default:
				throw new IllegalArgumentException("Type of message is wrong.");
		}
	}
	*/
	
	/**
	 * Print text in DIV block formatted by class names according to "what" value
	 * (warning, info, error, title)
	 * @param text
	 * @param parameters
	 */
	public void printDiv(String text, int what) {
		switch (what & MSG_MASK) {
			case WARNING_MSG:
				print("<div class='warning'>" + text + "</div>\n");
				break;
			case INFO_MSG:
				print("<div class='info'>" + text + "</div>\n");
				break;
			case ERROR_MSG:
				print("<div class='error'>" + text + "</div>\n");
				break;
			case TITLE_MSG:
				print("<div class='title'>" + text + "</div>\n");
				break;
			default:
				throw new IllegalArgumentException("Type of message is wrong.");
		}
	}
	
	/**
	 * Print text in pre block
	 * @param text
	 */
	public void printSource(String text) {
		addText("<pre>" + text + "</pre>\n");
	}	
	
	/**
	 * Print text in DIV block, class="sep".
	 * @param text
	 */
	public void printSeparator(String text) {
		print("<div class=\"sep\">" + text + "</div>\n");
	}
	public void printSeparator(String text, int parameters) {
		switch (parameters & MSG_MASK) {
			case WARNING_MSG:
				print("<div class='warning'>" + text + "</div>\n");
				break;
			case INFO_MSG:
				print("<div class='info'>" + text + "</div>\n");
				break;
			case ERROR_MSG:
				print("<div class='error'>" + text + "</div>\n");
				break;
			default:
				throw new IllegalArgumentException("Type of message is wrong.");
		}
	}

	/**
	 * Print text in multiple lines (converts end-of-lines to BR tags)
	 * @param text
	 */
	public void printMultiLineText(String text) {
		StringBuffer  buffer = new StringBuffer(1024);
		String[] output = text.split("\n");
		buffer.append("<div class='textblock'>\n");
		for (int i=0;i<output.length;i++) {
			buffer.append(output[i]+"<br />\n");
		}
		buffer.append("</div>\n");
		addText(buffer.toString());
	}
	
	/**
	 * Shortcut for printTable(null, null, null, matrix);
	 */
	public void printTable(String[][] matrix) {
		printTable(null, null, null, matrix);
	}
	
	/**
	 * Shortcut for printTable(caption, null, null, matrix);
	 */
	public void printTable(String caption, String[][] matrix) {
		printTable(caption, null, null, matrix);
	}
	
	/**
	 * Shortcut for printTable(null, rowTitles, colTitles, matrix);
	 */
	public void printTable(String[] rowTitles, String[] colTitles, String[][] matrix) {
		printTable(null, rowTitles, colTitles, matrix);
	}
	
	/**
	 * Prints matrix as a table. Formatting is via classes:<br />
	 * colTi .. column title<br />
	 * rowTi .. row title <br />
	 * @param caption table caption
	 * @param rowTitles row titles (ignored if null)
	 * @param colTitles column titles (ignored if null)
	 * @param matrix cell data
	 */
	public void printTable(String caption, String[] rowTitles, String[] colTitles, String[][] matrix) {
		StringBuffer  buffer = new StringBuffer(1024);		
		buffer.append("<table cellspacing='0px'>\n");		
		if (caption != null) {
			buffer.append("<caption>" + caption + "</caption>\n");
		}		
		if (colTitles != null) {
			buffer.append("<tr>\n");
			
			if (rowTitles != null) {  
				buffer.append("<td class='colTi'>&nbsp;</td>\n");
			}
			
			for (int j=0; j<matrix[0].length; ++j) {  
				buffer.append("<td class='colTi'>" + colTitles[j] + "</td>\n");
			}	
			buffer.append("</tr>\n");
		}		
		for (int i=0; i<matrix.length; ++i) {
			buffer.append("<tr>\n");
			
			if (rowTitles != null) {
				buffer.append("<td class='rowTi'>" + rowTitles[i] + "</td>\n");
			}
			
			for (int j=0; j<matrix[0].length; ++j) {
				buffer.append("<td>" + matrix[i][j] + "</td>\n");
			}	
			buffer.append("</tr>\n");
		}
		buffer.append("</table>\n");
		addText(buffer.toString());
	}
			
	
	public void printTestingContent() {
		print("Test text");
		println();
		printDiv("This is error text", ERROR_MSG);
		println();
		printDiv("This is warning text", WARNING_MSG);
		println();
		printDiv("This is info text", INFO_MSG);
		println();
//		prints("This should cause an error", 0);
		println();
		printSeparator("Normal separator");
		print("Test text");
		println();
		String[][] matrix = {{"a1", "a2", "a3"}, {"b1", "b2", "b3"}};
		String[] rowTitles = {"Row a", "Row b"};
		String[] colTitles = {"Col 1", "Col 2", "Col 3"};
		println();
		printTable(matrix);
		println();
		printTable("Test table #1", matrix);
		println();
		printTable(null, colTitles, matrix);
		println();
		printTable(rowTitles, null, matrix);
		println();
		printTable(rowTitles, colTitles, matrix);
		println();
		printTable("Test table", rowTitles, colTitles, matrix);
		println();
		printTable("Very loooooooooooooooooooooooooooooooooooooooong caption of the test table", rowTitles, colTitles, matrix);
		println();
		printTable("This should be not so long caption of the test table", rowTitles, colTitles, matrix);
		println();
		String[] names  = {"a1", "a22", "a333", "a444", "a55", "a6"};
		String[] values = {"1.1", "12.321", "456", "-12.4", "889", "120345.12"};
		String[] units  = {"-", "\u00C5", "m<sup>\u22121</sup>", "m.s<sup>\u22121</sup>", "m", "s"};
		printList(names, values, units);
		println();
		printList("Test of list", names, values, null,units);
		println();
		printSource("This is a source text.\nIt keeps all new line separators\n    and spaces [          ] as you can see.");
	}


	

	public void debugMessage(String msg) {
		// TODO Auto-generated method stub
		
	}

	public void debugMessage(String format, Object[] msg) {
		// TODO Auto-generated method stub
		
	}
	
}
