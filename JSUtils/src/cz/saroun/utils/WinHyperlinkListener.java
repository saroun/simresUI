package cz.saroun.utils;

import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class WinHyperlinkListener implements HyperlinkListener {
    public void hyperlinkUpdate(HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        	String[] cmd = new String[4];
        	cmd[0] = "cmd.exe";
    		cmd[1] = "/C";
    		cmd[2] = "start";
    		cmd[3] = evt.getURL().toString();
        	try {
				Runtime.getRuntime().exec( cmd );
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Can''t open web browser.", "Error", JOptionPane.ERROR_MESSAGE);
			}
        }
    }
}
