package cz.restrax.gui;

import java.awt.EventQueue;

import javax.swing.JTextArea;

import cz.jstools.util.FileLogger;

/**
 * Extends FileLogger. It shows log text in JTextArea. The output is not formated, so 
 * it is intended for use as a standard console output logger.
 * @see FileLogger
 * @author   Jan Saroun
 * @version  <dl><dt>$Revision: 1.5 $</dt>
 *               <dt>$Date: 2019/05/21 16:25:01 $</dt></dl>
 */
public class WinLoggerConsole extends FileLogger {
	private static final int   MAXIMUM_CAPACITY = 100000; // characters
	private final JTextArea pane;
	public WinLoggerConsole(JTextArea pane) {
		super();
		this.pane=pane;
	}
	
	public WinLoggerConsole(JTextArea pane, FileLogger logger) {
		super(logger);
		this.pane=pane;
		addToScreen(logger.toString());
	}
	
	public void print(String text) {
		super.print(text);
		addToScreen(text);
	}
	public void println(String text) {
		super.println(text);
		addToScreen(text+"\n");
	}
	/** Print text to the GUI console.<BR>
	 * Check that contents does not exceed MAXIMUM_CAPACITY. 
	 * I so, delete initial part of the window contents. 
	 */
	protected void addToScreen(String text) {		
		if (text.trim().length() != 0) {
			EventQueue.invokeLater(new myRunnable(text));
		}
	}
	
	protected final class myRunnable implements Runnable {
		String text;
		public myRunnable(String s) {
			this.text = s;
		}
		public void run() {
		    int storedTextLength = pane.getText().length();
			int addedTextLength  = text.length();
 // if max. capacity would be exceeded, remove initial text length = capacity/2
 // but not more than the current text length 
			if ((storedTextLength + addedTextLength) > MAXIMUM_CAPACITY) {
				pane.setCaretPosition(0);
				int toDelete = Math.min(storedTextLength,MAXIMUM_CAPACITY/2);
				pane.moveCaretPosition(toDelete);
				pane.replaceSelection("");
				storedTextLength = pane.getText().length();
			}
	// only part of the new text not exceeding capacity can be written
			int toStartAt=Math.max(0,storedTextLength+addedTextLength-MAXIMUM_CAPACITY);
			pane.setCaretPosition(pane.getDocument().getLength());
			pane.replaceSelection(text.substring(toStartAt));
			pane.setCaretPosition(pane.getDocument().getLength());		
		}
	}

}
