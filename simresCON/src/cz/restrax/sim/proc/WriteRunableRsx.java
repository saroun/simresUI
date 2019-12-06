package cz.restrax.sim.proc;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import cz.jstools.tasks.WriteRunnable;

/**
 * Extends WriteRunnable by overriding the send() method. It allows to insert WAIT/NOTIFY commands for SIMRES kernel
 * so that UI can wait until the kernel finishes the task or timeout elapses. 
 * @author Jan Saroun, saroun@ujf.cas.cz
 *
 */
public class WriteRunableRsx extends WriteRunnable {
	public static final String   WRAP_START = "<RSXFEED>";
	public static final String   WRAP_END   = "</RSXFEED>";
	public WriteRunableRsx(BufferedWriter out, LinkedBlockingQueue<CommandsToSend> sendQueue) {
		super(out, sendQueue);
	}
	
	@Override
	protected void send(CommandsToSend q) {
			Vector<String> commands = q.commands;
			boolean waitfor = q.waitfor;
			String command="";
			try {
				
				if (waitfor) {
					out.write(WRAP_START);
					out.newLine();
					if (q.key==null || q.key.isEmpty())  {
						out.write("WAIT=");
					} else {
						out.write("WAIT="+q.key.trim());
					}
					out.newLine();
					out.write(WRAP_END);
					out.newLine();
				}
				out.write(WRAP_START);
				out.newLine();
				for (int i=0; i<commands.size();i++) {
					command = commands.get(i).trim();
					out.write(command);
					out.newLine();
				}
				out.write(WRAP_END);
				out.newLine();
				if (waitfor) {
					out.write(WRAP_START);
					out.newLine();
					if (q.key==null  || q.key.isEmpty())  {
						out.write("NOTIFY=");
					} else {
						out.write("NOTIFY="+q.key.trim());
					}
					out.newLine();
					out.write(WRAP_END);
					out.newLine();
				}
				out.flush();
			} catch (IOException ex) {
				// this exception occurs normally when sending EXFF command to kernel
				if (! command.equalsIgnoreCase("EXFF")) {
					String msg=String.format("Command [%s] has writing problem: %s", command,ex.getMessage());
					System.err.println(msg);
				}
			}
	}

}
