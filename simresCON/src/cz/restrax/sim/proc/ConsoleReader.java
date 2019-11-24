package cz.restrax.sim.proc;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.TimerTask;
import java.util.Vector;

import cz.saroun.tasks.ConsoleListener;


/**
 * This class handles periodic console output using a Timer.
 * 
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.5 $</dt>
 *               <dt>$Date: 2019/06/12 17:58:10 $</dt></dl>
 */
public class ConsoleReader extends TimerTask {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                        FIELDS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	private String                   processName = null;
	private BufferedReader           conIn       = null;
	private Vector<ConsoleListener>  receivers   = null;


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTRUCTORS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	public ConsoleReader(String processName,
	                     BufferedReader conIn,
	                     Vector<ConsoleListener> receivers) {
		this.processName = processName;
		this.conIn = conIn;
		this.receivers = receivers;
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    ACCESS METHODS                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	
	public void setConsoleInput(BufferedReader  conIn) {
		this.conIn = conIn;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    OTHER METHODS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	public void	run () {
		String  outputBuffer = "";
		int lbuff;
		
		try {
			if ((conIn != null) && conIn.ready()) {
				CharBuffer charBuf = CharBuffer.allocate(1024);
				// read all available console output by 1024 blocks
				do {  
					charBuf.clear();
					conIn.read(charBuf);
					// console output (stream) shall be reversed
					charBuf.flip();
					outputBuffer += charBuf.toString();
				} while (conIn.ready());
			}
			lbuff = outputBuffer.length();
			// check if something was read
			if (lbuff != 0) {
				// send the console output to all registered receivers 
				
				//System.out.format("ConsoleReader: read %d chars\n",lbuff);
				for (int i=0; i<receivers.size(); ++i) {
					receivers.elementAt(i).receive(outputBuffer);
				}
			}
		} catch (IOException ex) {
			System.err.println("Process '" + processName
			                 + "' output reading problem: " + ex.getMessage());
		}
	}
}