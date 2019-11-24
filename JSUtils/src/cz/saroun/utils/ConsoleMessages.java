package cz.saroun.utils;

import cz.saroun.utils.HTMLLogger;


public class ConsoleMessages {
    private HTMLLogger logger=null;
    private final boolean debug;
	public ConsoleMessages(HTMLLogger logger,boolean debug) {
		super();
		this.logger = logger;
		this.debug=debug;
	}	
	
	public void titleMessage(String msg) {
		if (logger != null) {
			//logger.printSeparator(msg, style);
			logger.print("<div class='title'>" + msg + "</div>\n");
		}
	}
	
	public void infoMessage(String msg,String priority) {
		if (logger != null) { 
			//logger.printSeparator("Info", HTMLLogger.INFO_MSG);
			//logger.printlns(msg, HTMLLogger.INFO_MSG);
			//logger.println();
			String s = msg.replaceAll("\n", "<br/>");
			logger.printDiv("<b>Info</b><br />"+s, HTMLLogger.INFO_MSG);
		} else {
			System.out.printf("INFO: %s\n",msg);
		}
	}
	
	public void infoMessage(String[] msg,String priority) {
		String s = "";
		for (int i=0;i<msg.length;i++) {
			s += msg[i]+"<br />\n";
		}
		infoMessage(s,priority);
	}
	
	public void errorMessage(String msg,String priority, String src) {
		if (logger != null) {
			//logger.printSeparator("Error", HTMLLogger.ERROR_MSG);
			logger.printDiv("<b>Error</b><br />"+src + ": "+msg, HTMLLogger.ERROR_MSG);
			//logger.printlns(src + ": " + msg, HTMLLogger.ERROR_MSG);
			//logger.println();
		} else {
			System.err.printf("ERROR: %s\n",msg);
		}
	}
	
	public void errorMessage(Exception e, Object src) {
		errorMessage(e.getMessage(), "low", src.getClass().getName());
	}

	public void warnMessage(String msg,String priority) {
		if (logger != null) { 
			//logger.printSeparator("Warning", HTMLLogger.WARNING_MSG);
			//logger.printlns(msg, HTMLLogger.WARNING_MSG);
			//logger.println();
			logger.printDiv("<b>Warning</b><br />"+msg, HTMLLogger.WARNING_MSG);
		} else {
			System.err.printf("WARNING: %s\n",msg);
		}
	}
	
	public void debugMessage(String msg) {
		if (debug) {
			System.err.printf("DEBUG:\n%s\n",msg);
		}		
	}
	public void debugMessage(String format,Object[] msg) {
		if (debug) {
			System.err.printf("DEBUG:\n"+format+"\n",msg);
		}		
	}
	
}
