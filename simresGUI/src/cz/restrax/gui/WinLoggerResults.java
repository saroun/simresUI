package cz.restrax.gui;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import cz.jstools.util.HTMLLogger;
import cz.restrax.gui.WinLoggerConsole.myRunnable;
import cz.restrax.sim.resources.Resources;

/**
 * Advanced implementation of HTMLLogger for ResultsWindow. 
 * Extends the SimpleHTMLLogger so that it shows HTML output in the JEditorPane.
 * Handles caching of the contents and saving excess text to put off files. (not yet implemented)
 * @see SimpleHTMLLogger
 * @author   Jan Saroun
 * @version  <dl><dt>$Revision: 1.5 $</dt>
 *               <dt>$Date: 2019/05/21 16:25:01 $</dt></dl>
 */
public class WinLoggerResults extends HTMLLogger {
	private final JEditorPane pane;
	private HTMLDocument myDoc;
	private final HTMLDocument blancDoc;
	public static int MAX_RECORDS=1000;

	public WinLoggerResults(JEditorPane pane) {
		super();
		this.pane=pane;
		this.pane.setEnabled(true);
		this.pane.setEditable(false);
		this.pane.setContentType("text/html; charset=UTF-8");
		blancDoc=(HTMLDocument) this.pane.getEditorKit().createDefaultDocument();
		clear();
	}
	
	public WinLoggerResults(JEditorPane pane, HTMLLogger logger) {
		super(logger);
		this.pane=pane;
		this.pane.setEnabled(true);
		this.pane.setEditable(false);
		this.pane.setContentType("text/html; charset=UTF-8");
		blancDoc=(HTMLDocument) this.pane.getEditorKit().createDefaultDocument();
		addToScreen(logger.toString());
	}
	
	/**
	 * Clear the results and remove temporary file. Call this method when
	 * it is sure, that window hasn't got unsaved results
	 * (hasUnsavedResults()==false)
	 */
	public void clear() {
		boolean b = isEnabled();
		setEnabled(false);
		super.clear();
		EventQueue.invokeLater(new myRunnable(b));
	}

	protected void setCSS() {
		HTMLDocument doc = (HTMLDocument)pane.getDocument();
		StyleSheet css=doc.getStyleSheet();
		String cssText=Resources.getText("results.css");
		try {
			css.loadRules(new StringReader(cssText), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			super.setCSSStyle(cssText);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected Element getBodyElement(HTMLDocument doc) {		
		Element[] roots = doc.getRootElements();
		Element body = null;
		for( int i = 0; i < roots[0].getElementCount(); i++ ) {
		    Element element = roots[0].getElement( i );
		    if( element.getAttributes().getAttribute( StyleConstants.NameAttribute ) == HTML.Tag.BODY ) {
		        body = element;
		        break;
		    }
		}
		return body;
	}
	
	@Override
	public void addText(String s) {
		super.addText(s);
		addToScreen(s);				
	}
	
	
	/**
	 * Clear first half of records, but do not update GUI
	 * @return
	 */
	protected boolean freeScreenSpace() {
		if (this.records.size()>MAX_RECORDS) {
			ArrayList<String> a = new ArrayList<String>();
			for (int i=records.size()/2;i<records.size();i++) {
				a.add(records.get(i));
			}
			super.clear();
			records.addAll(new ArrayList<String>(a));
			return true;
		}
		return false;
	}
	
	protected void addToScreen(String s) {
		if (s.trim().length() != 0) {
			EventQueue.invokeLater(new myRunnable(s));
		}
	}
	
	/**
	 * Updates results window, to be run through EventQueue.
	 *
	 */
	protected final class myRunnable implements Runnable {
		String text;
		int cmd;
		boolean enabled;
		// invoke this to add text to the window
		protected myRunnable(String s) {
			this.text = s;
			this.cmd = 1;
		}
		// invoke this to clear the window
		protected myRunnable(boolean enabled) {
			this.text = "";
			this.cmd = 0;
			this.enabled = enabled;
		}
		
		protected void clrWindow() {
			myDoc=(HTMLDocument) pane.getEditorKit().createDefaultDocument();		
			pane.setDocument(myDoc);
			setCSS();
			pane.setCaretPosition(pane.getDocument().getLength());
			setEnabled(enabled);			
		}
		
		protected void addToWindow(String s) {
			// add new text to the logger
			myDoc=(HTMLDocument) pane.getDocument();
			pane.setDocument(blancDoc);
			try {
				myDoc.insertBeforeEnd(getBodyElement(myDoc), s);
			} catch (BadLocationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			pane.setDocument(myDoc);
			pane.setCaretPosition( pane.getDocument().getLength() );			
		}
		
		public void run() {
			if (cmd==0) {
				clrWindow();
			} else if (cmd==1) {
				boolean fs = freeScreenSpace();
				if (fs) {
					// window has been purged, clear and print actual content of the logger
					clrWindow();
					WinLoggerResults.this.toString();
					System.out.printf("WinLoggerResults: shrinked to %d records\n",records.size());
				}
				addToWindow(text);			
			}
	
		}
	}

	
	

}
