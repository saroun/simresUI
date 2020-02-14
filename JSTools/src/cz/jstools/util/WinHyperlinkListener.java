package cz.jstools.util;

import java.awt.Desktop;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class WinHyperlinkListener implements HyperlinkListener {
    public void hyperlinkUpdate(HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        	URL u = evt.getURL();
			try {
				Desktop.getDesktop().browse(u.toURI());
			} catch (Exception e1) {
				if (u!=null) { 
					System.out.println(u.toString());
				} else { 
					System.out.println(evt.getDescription());
				}
				JOptionPane.showMessageDialog(null, "Can''t open web browser.", "Error", JOptionPane.ERROR_MESSAGE);
			}

        	/*
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
			*/
        }
    }
}
