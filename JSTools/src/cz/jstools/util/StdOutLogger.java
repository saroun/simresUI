package cz.jstools.util;
/**
 * Implements a simple console logger. <br/>
 * Prints text to STDOUT when enabled.
 * @author  Jan Saroun
 * @version  <dl><dt>$Revision</dt>
 *               <dt>$Date: 2012/01/20 17:42:34 $</dt></dl>
 */
public class StdOutLogger implements LoggerInterface {
	private boolean enabled;

	public void clear() {
	// do nothing, STDOUT can't clear	
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled=enabled;
	}
	public void print(String text) {
	  if (enabled) System.out.print(text);		
	}
	public void println(String text) {
	  if (enabled) System.out.println(text);	
	}


}
