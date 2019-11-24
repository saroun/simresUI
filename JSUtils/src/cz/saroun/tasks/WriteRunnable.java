package cz.saroun.tasks;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Writes text to the console of a BufferedWriter. Used to send commands to a child
 * process launched by ProcessLauncher/ProcessRunnable.  
 * @author Jan Saroun, saroun@ujf.cas.cz
 * @see ProcessLauncher
 * @see ProcessRunnable
 */
public class WriteRunnable implements Runnable {
	private static final boolean __DEBUG__ = false;
	private final AtomicBoolean active;
	private final LinkedBlockingQueue<CommandsToSend> sendQueue;
	protected BufferedWriter out;
	
	public WriteRunnable(BufferedWriter out, LinkedBlockingQueue<CommandsToSend> sendQueue) {
		active = new AtomicBoolean(false);
		this.out = out;
		this.sendQueue = sendQueue;
	}
	
	
	public static class CommandsToSend {
		public final Vector<String> commands;
		public final boolean waitfor;
		public final String key;
		
		public CommandsToSend(Vector<String> commands, String waitkey) {
			this.commands=commands;
			this.waitfor=(waitkey != null);
			this.key = waitkey;
		}
	}

	public void run() {
		active.set(true);
		while (active.get()) {
			try {
				if (sendQueue != null) {
					CommandsToSend q = sendQueue.take();
					if (q!=null) send(q);
				}
			} catch (InterruptedException e) {
				String msg=String.format("Thread '%s' InterruptedException in WriteRunnable, %s\n", 
					Thread.currentThread().getName(), e.getMessage());
				System.err.println(msg);
			}
		}
		try {
			out.close();
		} catch (IOException e) {}
		// this allows GC to destroy the stream even if this WriteRunnable persists.
		out = null;
		if (__DEBUG__) System.out.format("Thread '%s' finished.",Thread.currentThread().getName());
	}
	
	protected void send(CommandsToSend q)  {
		Vector<String> commands = q.commands;
		String command="";
		try {
			for (int i=0; i<commands.size();i++) {
				command = commands.get(i).trim();
				out.write(command);
				out.newLine();
			}
			out.flush();
		} catch (IOException ex) {}
	}
	
	protected void stop() {
		active.set(false);
	}
}	

