package cz.jstools.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.CharBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread safe reader of text from BufferedReader. It collects text output from BufferedReader
 * and puts this text on a queue (LinkedBlockingQueue). It is used to collect text output
 * from a child process launched by ProcessLauncher/ProcessRunnable.
 * @author Jan Saroun, saroun@ujf.cas.cz
 * @see ProcessLauncher
 * @see ProcessRunnable * 
 */
public final class ReadRunnable implements Runnable {
	private static final boolean  __DEBUG__      = false;
	private static final long     TIMER_PERIOD   = 100;   // read the console every TIMER_PERIOD ms
	private CharBuffer	charBuf = CharBuffer.allocate(8192);
	private BufferedReader inp;
	private final AtomicBoolean active;
	private final AtomicBoolean finished;
	private final Object sync; 
	private final LinkedBlockingQueue<String> receiveQueue;
	
	public ReadRunnable(BufferedReader inp, Object sync, LinkedBlockingQueue<String> queue) {
		active = new AtomicBoolean(false);
		finished = new AtomicBoolean(false);
		this.inp = inp;
		this.sync = sync; 
		this.receiveQueue = queue;
	}
	public void run() {
		active.set(true);
		while (active.get()) {			
			try {
				receive();
			} catch (InterruptedException e1) {
				String msg=String.format("Thread '%s' InterruptedException in ReadRunnable, %s\n", 
						Thread.currentThread().getName(), e1.getMessage());
				System.err.println(msg);
			} catch (IOException e1) {
				String msg=String.format("Thread '%s' IOException in ReadRunnable, %s\n", 
						Thread.currentThread().getName(), e1.getMessage());
				System.err.println(msg);
			}
		}
		if (__DEBUG__) System.out.format("Thread '%s' finalization.\n",Thread.currentThread().getName());
		try {
			inp.close();
		} catch (IOException e) {
		}
		// this allows GC to destroy the stream even if this ReadRunnable persists.
		inp = null;
		finished.set(true);
		if (__DEBUG__) System.out.format("Thread '%s' closed.\n",Thread.currentThread().getName());
	}
	
	private void receive() throws IOException, InterruptedException {
		synchronized(sync) {
			if((inp==null) || ! inp.ready()) {
				sync.wait(TIMER_PERIOD);
			}			
			if (inp != null) {
				try {
					while (inp.ready()) {  
						((Buffer)charBuf).clear();
						inp.read(charBuf);
						((Buffer)charBuf).flip();
						if (__DEBUG__) System.out.format("Put on Queue\n%s\n", charBuf.toString());
						receiveQueue.put(charBuf.toString());
					}
				} catch (IOException e) {
					String msg=String.format("Thread '%s' IOException in ReadRunnable, %s\n", 
							Thread.currentThread().getName(), e.getMessage());
					System.err.println(msg);
				} catch (InterruptedException e) {
					String msg=String.format("Thread '%s' InterruptedException in ReadRunnable, %s\n", 
							Thread.currentThread().getName(), e.getMessage());
					System.err.println(msg);
				};
				if (__DEBUG__) System.out.format("receive leaving, %d records\n",receiveQueue.size());
			};
			sync.notifyAll();
		}
	}
	
	public void stop() {
		active.set(false);
		// sync.notifyAll();
	}
	public boolean isFinished() {
		return finished.get();
	}
	
}