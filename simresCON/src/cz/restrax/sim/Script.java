package cz.restrax.sim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import cz.saroun.classes.definitions.Utils;


public class Script {
	private String script;
	private SimresCON program;
	private boolean batchMode = false;
	public Script(SimresCON program) {
		super();
		this.program=program;		
		script="";
	}
	
	/**
	 * Split command string to lines. Removes empty and comment lines.
	 * @param cmd Command text (multi-line)
	 * @return Command lines as a Vector.
	 */
	public static Vector<String> parseCommands(String cmd) {
		Vector<String> commands = new Vector<String>();
		String[] lines = cmd.split("\n");
		String line;
		for (int i=0;i<lines.length;i++) {
			line = lines[i].trim();
			if (! line.isEmpty() && ! line.startsWith("#")) {
				commands.add(line);
			}
		}
		return commands;
	}
	
	/**
	 * Encapsulates a block of script commands stored as Vector<String> 
	 * with associated flag ui. If (ui), then the commands should be processed 
	 * by the user interface (UI). Otherwise the commands should be sent to the SIMRES kernel. 
	 */
	public class ScriptBlock {
		private final Vector<String> text;
		private final boolean ui;
		ScriptBlock(Vector<String> text, boolean ui) {
			this.text = text;
			this.ui = ui;
		}
		
		public String toString() {
			return text.toString();
		}
		
		public Vector<String> getText() {
			return text;
		}
		public boolean isUI() {
			return ui;
		}
	}

	public boolean isEmpty() {
		return (script==null || script.equals(""));
	}
	public void readScript(String fileName) {
		String fname=program.getProjectList().getFullPath(ProjectList.PROJ_CFG, fileName);
		try {
			String content=Utils.readFileToString(fname);
			script=content;			
		} catch (IOException e) {
			program.getMessages().errorMessage("Can''t read file "+fname, "low", "Script");
		}
	}
	
	public void saveScript(String fileName) {
		try {
			if (! isEmpty()) Utils.writeStringToFile(fileName,script+"\n");
		} catch (IOException ex) {			
			String message = "Problem with file '" + fileName + "': " + ex.getMessage();
			program.getMessages().errorMessage(message,"low", this.getClass().toString());
		} 									
	}
	
	public void clearScript() {
		script="";
	}

	public String getScript() {
		return script;
	}


	/**
	 * Ensures that there is always "XML UPDATE" before "DO MC".
	 * @param scriptText
	 * @return corrected script
	 */
	public static Vector<String> validateScript(String scriptText) {
		String [] ss = scriptText.split("\n");
		Vector<String> commands = new Vector<String>();
		boolean lastUpdate=false;
		for (int i=0;i<ss.length;i++) {
			String cmd = ss[i].trim();
			String cmdU = cmd.toUpperCase();			
			if (cmdU.matches("[\t ]*DO[\t ]*MC")) {
				if (! lastUpdate) {
					commands.add("XML UPDATE");					
				}
			}
			lastUpdate=(cmdU.matches("[\t ]*XML[\t ]*UPDATE"));
			if (lastUpdate) {
				commands.add("XML UPDATE");
			} else if (! cmd.isEmpty() && ! cmd.startsWith("#")) {
				commands.add(cmd);
			}				
		}
		return commands;
	}
	
	public void setScript(String script) {
		//this.script = validateScript(script);
		this.script = script;
	}

	/**
	 * Should be true if the script is executed
	 * @return
	 */
	public boolean isBatchMode() {
		return batchMode;
	}

	public void setBatchMode(boolean batchMode) {
		this.batchMode = batchMode;
	}
	
	/**
	 * Breaks the script text into blocks at lines starting with "BLOCK". 
	 * Lines starting with BLOCK UI will be launched by TaskExecutor without waiting for signal. 
	 * Skips empty an comment lines. 
	 * Calls validateScript() on each added block; 
	 * @param scriptText
	 * @return 
	 */
	public ArrayList<ScriptBlock> getCommandBlocks(String scriptText) {
		ArrayList<ScriptBlock> blocks = new ArrayList<ScriptBlock>(); 
		String [] lines = scriptText.split("\n");
		String s = "";
		String line;
		boolean ui = false;
		for (int i=0;i<lines.length;i++) {
			line = lines[i].trim();
			if ((! line.isEmpty()) && (! line.startsWith("#"))) {
				if (line.toUpperCase().startsWith("BLOCK")) {
					Vector<String> content = validateScript(s);
					if (content.size()>0) {
						blocks.add(new ScriptBlock(validateScript(s), ui));
					}
					s = "";
					if (line.toUpperCase().matches("[\t ]*BLOCK[\t ]*UI")) {
						ui = true;
					} else {
						ui = false;
					}
				} else {
					s += line+"\n";
				}
			}
		}
		// add the rest of the content after last block command
		if (! s.isEmpty()) {
			Vector<String> content = validateScript(s);
			if (content.size()>0) {
				blocks.add(new ScriptBlock(content, ui));
			}
		}
		return blocks;
	}
	
}
