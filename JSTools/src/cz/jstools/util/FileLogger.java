package cz.jstools.util;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cz.jstools.classes.definitions.Utils;

/**
 * Implements a simple file logger. Supports automatic save to a log file (driven by timer). 
 * Use addText to add new record to the queue.<br/>
 * To start automatic logger, use:<br/>
 * - setLogFile(String filename)<br/>
 * - setAutoSavePeriod(long autoSavePeriod)<br/>
 * - setAutoSave(boolean autoSave)<br/>
 * 
 * Set  saveIncremental=true (default) if you want to clear contents after saving to a file
 * @author  Jan Saroun
 * @version  <dl><dt>$Revision: 1.6 $</dt>
 *               <dt>$Date: 2019/11/06 09:12:59 $</dt></dl>
 */
public class FileLogger implements LoggerInterface {
	private static final boolean  __DEBUG__       = false;
	public static final int MAX_FILE_SIZE=1000000;
	protected String logFile="";
	protected boolean enabled=false;
	protected boolean echo=false;
	protected boolean autoSave=false;
	protected long autoSavePeriod=10000;
	protected final ArrayList<String> records;
	protected boolean  hasUnsavedResults = false;
	//protected int count=0;
	protected boolean saveIncremental=true;
	private Timer timer=null;
	protected File logF=null;
	protected boolean dbg=false;
	
	public FileLogger() {
		super();	
		enabled=false;
		echo=false;
		autoSave=false;
		//count=0;
		records= new ArrayList<String>();
		hasUnsavedResults = false;
	}
	
	/**
	 * Create new logger and fill records from the logger in argument
	 * @param logger
	 */
	public FileLogger(FileLogger logger) {
		this();
		this.logFile=logger.logFile;
		this.echo=logger.echo;
		this.autoSavePeriod=logger.autoSavePeriod;
		this.logF=logger.logF;
		
		for (int i=0;i<logger.getRecords().size();i++) {
			records.add(logger.getRecords().get(i));
		}
		hasUnsavedResults = true;
		this.enabled=logger.enabled;
		try {
			setAutoSave(false,logger.autoSave);
		} catch (IOException e) {
			stopAutoSave();
		}
		
	}
	
	public String toString() {
		String txt="";
		for (int i=0;i<records.size();i++) {
			txt += records.get(i);
		}
		return txt;
	}
	
	/**
	 * This is the input method all other methods should use to add new HTML text. 
	 * @param text HTML text (a complete set of tags, not fragments !) 
	 */
	public void addText(String text) {
		if (enabled) {
			if (text!=null && (text.trim().length() != 0)) {
				if (__DEBUG__) {
					System.out.println("LOG");
					System.out.println(text);
					System.out.println();					
				}
				records.add(text);
				hasUnsavedResults=true;
				if (echo) System.out.print(text);
			}
		}
	}
	
	public void clear() {
		records.clear();
		hasUnsavedResults=false;
	}
	
	public void print(String text) {
		addText(text);
	}
	public void println(String text) {
		addText(text+" \n");
	}

	public void printlnDbg(String text) {
		if (dbg) {
			addText(text+" \n");
		}
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}	
	
	public boolean isEcho() {
		return echo;
	}

	public void setEcho(boolean echo) {
		this.echo = echo;
	}

	
	/**
	 * @return true if there are unsaved results
	 */
	public boolean hasUnsavedResults() {
		return hasUnsavedResults;
	}
	
	public ArrayList<String> getRecords() {
		return records;
	}
	
	/**
	 * @return Total length of recorded strings
	 */
	public int getLength() {
		int n=0;
		for (int i=0;i<records.size();i++) {
			n += records.get(i).length();
		}
		return n;
	}

	
	/**
	 * @return number of records
	 */
	public int getCount() {
		return records.size();
	}

	public String getContent() {
		String out="";
		for (int i=0;i<records.size();i++) {
			out +=records.get(i);
		}
		return out;
	}

	
	/**
	 * Save the whole logger content, equivalent to flushToFile(false).
	 */
	public void flushToFile() {
		flushToFile(false);
	}
	
	/**
	 * Append the logger content to the log file and clear records.
	 */
	public void flushToFile(boolean append) {
		if (logF==null) return;
		if (hasUnsavedResults && records.size()>0) {
				String out=getContent();
				try {
					if (logF.exists() && logF.length()>MAX_FILE_SIZE) purgeLogFile();
					if (append) {
						Utils.appendStringToFile(logF, out);
					} else {	
						Utils.writeStringToFile(logF, out);				
					}
					hasUnsavedResults=false;
				} catch (IOException e) {
					System.err.printf("%s: Problem when writing to %s\n",this.getClass().getName(),logFile);
					logF=null;
					try {
						setAutoSave(false,false);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}				
		}
	}
	public boolean isAutoSave() {
		return autoSave;
	}
	
	
	public void closeLogFile() {
		logF=null;
		stopAutoSave();
	}
	
	/**
	 * Starts a simple file logger, which saves all input only at the exit time.
	 * Provide absolute path name. 
	 * @result true if successfully created the log file
	 */
	public boolean startLogger(String pathname) {
		boolean res=false;		
		try {
			setLogFile(pathname);
			if (createLogFile(true,true)) {
				setSaveIncremental(false);
				setAutoSave(true, false);
				res=true;
			};			
		} catch (Exception e) {	
			closeLogFile();
			logFile=null;
		}
		return res;
	}
	
	/**
	 * Create a new log file (name=this.logFile). Adds time stamp.
	 * @param rewrite	if true, rewrite an existing log file
	 * @return true if the file is prepared for logging (exists and is writable)
	 * @throws IOException 
	 */
	public boolean createLogFile(boolean rewrite, boolean stamp) throws IOException {
		boolean res=false;
		if (logFile==null || logFile.equals("")) return false;
		File f =new File(logFile);
		if (! f.exists()) {
			try {
				f.createNewFile();
				if (stamp) Utils.writeStringToFile(logFile, getTimeStamp());	
				res=f.canWrite();
			} catch (IOException e) {
				logFile=null;
				logF=null;
				throw new IOException("Can't create log file "+logFile+"\n"+e.getMessage());
			}			
		} else {
			res = f.canWrite();
			if (res) {
				if (rewrite) {
					if (stamp) {
						Utils.writeStringToFile(logFile, getTimeStamp());
					} else {
						Utils.writeStringToFile(logFile, "");
					}
				} else {
					if (stamp) Utils.appendStringToFile(logFile, getTimeStamp());
				}				
			}
		}
		if (res) {
			logF=f;
		} else {
			logFile=null;
			logF=null;
		}
		return res;		
	}
	
	public String getTimeStamp() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String out = dateFormat.format(new Date());
		return String.format("<!-- %s -->\n",out);
	}
	
	
	
	
	public void setAutoSave(boolean rewrite, boolean autoSave) throws IOException {
		if (autoSave==this.autoSave) return;
		if (! autoSave) {
			stopAutoSave();
		} else {
			if (createLogFile(rewrite, true)) {
				startAutoSave(autoSavePeriod);
			}
		}
	}
	
	protected void startAutoSave(long period) {
		timer=new Timer();
		timer.schedule(new LoggerTimerTask(), 100, period);
		autoSave =true;		
	}
	
	protected class LoggerTimerTask extends TimerTask {
		@Override
		public void run() {
			flushToFile(saveIncremental);			
			if (saveIncremental) clear();
		}
	}
	
	public void stopAutoSave() {
		if (timer!=null) {
			timer.cancel();
			timer.purge();
			timer=null;
		}
		autoSave=false;		
	}

	public long getAutoSavePeriod() {
		return autoSavePeriod;
	}
	public void setAutoSavePeriod(long autoSavePeriod) throws Exception {
		if (! autoSave) {
			this.autoSavePeriod = autoSavePeriod;
		} else {
			throw new Exception("AbstractLogger: Can't set timer period while logging is active");
		}
		
	}

	public String getLogFile() {
		return logFile;
	}

	public void setLogFile(String logFile) throws Exception {
		if (! autoSave) {
			this.logFile = logFile;
		} else {
			throw new Exception("AbstractLogger: Can't set log file while logging is active");
		}
	}

	public boolean isSaveIncremental() {
		return saveIncremental;
	}

	public void setSaveIncremental(boolean saveIncremental) {
		this.saveIncremental = saveIncremental;
	}
	
	public void purgeLogFile() {
		if (logF!=null) {
			try {
				Utils.writeStringToFile(logF, "");
			} catch (IOException e) {				
			}
		}
	}

	public boolean isDbg() {
		return dbg;
	}

	public void setDbg(boolean dbg) {
		this.dbg = dbg;
	}

	

}