package cz.restrax.gui;

import java.awt.EventQueue;

import javax.swing.JOptionPane;

import cz.jstools.util.ConsoleMessages;
import cz.jstools.util.HTMLLogger;

public class WinConsoleMessages extends ConsoleMessages {

	public WinConsoleMessages(HTMLLogger logger, boolean debug) {
		super(logger, debug);
	}
	
	public static int showYesNoDialog(String question,int defAnswer ) {
		String[] options = new String[] {"Yes","No"};
		int a = Math.max(0,Math.min(defAnswer, 1));
		int n = JOptionPane.showOptionDialog(null,
				question,"Confirm",JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[a]);
		return n;
	}
	
	
	public static int showOptionDialog(String[] options, String question, String title, int defAnswer ) {
		int a = Math.max(0,Math.min(defAnswer, options.length-1));
		int n = JOptionPane.showOptionDialog(null,
				question,title,JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[a]);
		return n;
	}
	
	@Override
	public void infoMessage(String msg,String priority) {		
		if (priority != null && priority.equals("high")) {
			// JOptionPane.showMessageDialog(null, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
			invokeMessageDialog(msg, "Info", JOptionPane.INFORMATION_MESSAGE);
		} else {
			super.infoMessage(msg, priority);
		}		
	}
	@Override
	public void errorMessage(String msg,String priority, String src) {
		if ((priority != null && priority.equals("high"))) {
			//JOptionPane.showMessageDialog(null, msg, src, JOptionPane.ERROR_MESSAGE);
			invokeMessageDialog(msg, src, JOptionPane.ERROR_MESSAGE);
		} else {
			super.errorMessage(msg, priority, src);
		}		
	}
	@Override
	public void warnMessage(String msg,String priority) {
		if (priority != null && priority.equals("high")) {
			invokeMessageDialog(msg, "Warning", JOptionPane.WARNING_MESSAGE);
			//JOptionPane.showMessageDialog(null, msg,"Warning" ,JOptionPane.WARNING_MESSAGE);
		} else {
			super.warnMessage(msg, priority);
		}
	}
	
	protected void invokeMessageDialog(String msg, String title, int msgType) {
		EventQueue.invokeLater(new myRunnable(msg, title, msgType));
	}
	
	private final class myRunnable implements Runnable {
		String msg;
		String title;
		int msgType;
		myRunnable(String msg, String title, int msgType) {
			this.msg=msg;
			this.title=title;
			this.msgType=msgType;
		}
		public void run() {
			JOptionPane.showMessageDialog(null, msg, title, msgType);
		}
	}
	
	
}
