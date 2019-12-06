package cz.restrax.gui.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import cz.jstools.util.WinHyperlinkListener;
import cz.restrax.gui.SimresGUI;
import cz.restrax.gui.resources.Resources;
import cz.restrax.sim.Version;

public class CopyrightPane extends JPanel {
	private static final long serialVersionUID = -2111156044874116173L;
	private Version version;	
	public CopyrightPane(Version version) {
		this.version=version;
		createContent();	  	  
	}

	protected void createContent() {  
		JTextPane ver = new JTextPane();
		ver.setOpaque(true);
		Resources.class.getClassLoader();
		ver.setContentType("text/html; charset=UTF-8");
		ver.setEditable(false);
		ver.setBackground(new Color(231,231,236));
		ver.setPreferredSize(new Dimension(400,300));
		ver.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));	
		ver.setText(getContentHtml());
		ver.addHyperlinkListener(new WinHyperlinkListener());
		setPreferredSize(new Dimension(300,400));
		setBorder(BorderFactory.createLineBorder(new Color(0,0,0), 1));
		setLayout(new BorderLayout(0,0));				
		add(ver,BorderLayout.CENTER);
	}
	
	public String getContentBody() {
		String simver=version.getRestraxVersion();
		String simdat=version.getRestraxBuild();
		String temp=Resources.getText("copy_template.html");
		String dat=Version.BUILD.replaceAll("[$]","");		
		String html;
		html = temp.replaceAll("[$]PROGRAM_NAME[$]", SimresGUI.PROGRAM_NAME);
		html = html.replaceAll("[$]VERSION[$]", Version.VERSION);
		html = html.replaceAll("[$]VERSION_DATE[$]",dat);
		html = html.replaceAll("[$]SIMRES_VERSION[$]", simver);
		html = html.replaceAll("[$]SIMRES_DATE[$]",simdat);	
		String out = VersionPane.getContentBody()+html;
		return out;
	}
	
	public String getContentHtml() {
		String css=Resources.getText("version_style.css");
		String temp="<html>\n";
		temp += "<meta charset=\"UTF-8\">\n";
		temp += "<style type=\"text/css\">\n";		
		temp +=css;
		temp += "</style>\n";
		temp += "<body>\n";
		temp += getContentBody();
		temp += "</body>\n";
		temp += "</html>\n";
		return temp;
	}
}
