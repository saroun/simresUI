package cz.restrax.sim.proc;

import java.util.Vector;

import cz.saroun.classes.definitions.Utils;
import cz.saroun.tasks.ProcessLauncher;
import cz.saroun.tasks.ProcessRunnable;
import cz.saroun.tasks.WriteRunnable;
import cz.saroun.tasks.WriteRunnable.CommandsToSend;

public class RestraxRunnable extends ProcessRunnable {
	private static final boolean  __DEBUG__       = false;
	
	public RestraxRunnable(ProcessLauncher launcher) {
		super(launcher);
	}
	
	/**
	 * Sends a set of command lines to the process, wrapped in xml tag RSXFEED. <br/>
	 * NOTE: the process is asynchronous, this command only puts the commands in a queue.
	 * 
	 * @param commands  commands do be sent
	 * @param waitkey  if !=null, wrap the commands within the pair of "WAIT' and 'NOTIFY' commands with given key.
	 */
	@Override
	public void sendCommand(Vector<String> commands, String waitkey) {
		if (__DEBUG__) {
			System.out.println(Utils.getDebugHdr("SEND"));
			System.out.println(WriteRunableRsx.WRAP_START);
			int nmax = commands.size();
			int n = nmax<=10?nmax:10;
			for (int i=0; i<n;i++) {
				System.out.println(commands.get(i).trim());
			}
			if (n<nmax) {
				System.out.println("... another "+(nmax-n)+" ..lines");
			}
			System.out.println(WriteRunableRsx.WRAP_END);
			System.out.println();
		}
		super.sendCommand(commands, waitkey);
	}
	
	@Override
	public WriteRunnable newWriteRunnable() {
		return new WriteRunableRsx(conOut, sendQueue);
	}

}
