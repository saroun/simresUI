package cz.restrax.gui.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import cz.jstools.util.WinHyperlinkListener;
import cz.restrax.gui.SimresGUI;
import cz.restrax.gui.resources.Resources;
import cz.restrax.sim.Version;
import cz.restrax.sim.utils.FileTools;

/**
 * Actual content of the About dialog, encapsulated in CopyrightPane.
 *
 */
public class VersionPane extends JPanel {
	private static final long serialVersionUID = 7764203301479419728L;

	public VersionPane() {
		JTextPane ver = new JTextPane();
		Resources.class.getClassLoader();
		ver.setPreferredSize(new Dimension(400,200));
		ver.setContentType("text/html; charset=UTF-8");
		ver.setEditable(false);
		ver.setBackground(new Color(231,231,236));
	//	ver.setPreferredSize(new Dimension(200,150));
		ver.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));			
		ver.setText(getContentHtml());
		ver.addHyperlinkListener(new WinHyperlinkListener());
	//	setPreferredSize(new Dimension(200,200));
		//pnlVersionPane.setForeground(new Color(231,231,236));
		setBorder(BorderFactory.createLineBorder(new Color(0,0,0), 1));
		setLayout(new BorderLayout(0,0));						
		//pnlVersionPane.add(lbl,BorderLayout.WEST);
		add(ver,BorderLayout.CENTER);
	}
	
	public static String getContentBody() {
		String temp=Resources.getText("version_template.html");
		String ico=Resources.getResource("images/simres_64.png").toString();
		String dat=Version.BUILD.replaceAll("[$]","");	
		String instdir=FileTools.getRestraxPath();
		//instdir.replace("\\", "/");
		instdir = instdir.replace(File.separator, "/");
		instdir = instdir.replace(" ", "%20");
		String html;
		html = temp.replaceAll("[$]PROGRAM_NAME[$]", SimresGUI.PROGRAM_NAME);
		html = html.replaceAll("[$]VERSION[$]", Version.VERSION);
		html = html.replaceAll("[$]ICON[$]", ico);
		html = html.replaceAll("[$]VERSION_DATE[$]",dat);	
		html = html.replaceAll("[$]INSTDIR[$]",instdir);	
		return html;
	}
	
	public String getContentHtml() {
		String css=Resources.getText("version_style.css");
		String temp="<html>";
		temp += "<style type=\"text/css\">";
		temp +=css;
		temp += "</style>";
		temp += "<body>";
		temp += getContentBody();
		temp += "</body>";
		temp += "</html>";
		return temp;
	}
	
	public String getContentHtmlOld() {
		String s=Resources.getResource("images/simres_64.png").toString();
		String dat=Version.BUILD.replaceAll("[$]","");			
		String html="<html>"
				+ "<style type=\"text/css\">"
				+ "div.label {background-color:#E7E7EC;width:200px;height:64px;vertical-align:middle;}"
				+ "img.label {float:right;padding:8px;margin-left:8px;}"
				+ "p {font-size:12pt;}"
				+ "h1 {padding-bottom:0px;margin-bottom:0px;}"
				+ "td {text-align:center;}"
				+ "</style>"
				+ "<body>"
				+ "<div class=\"label\">"
				+ "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" >"
				+ "<tr><td align=\"left\">"+"<img src=\""+s+"\">"+"</td>"
				+ "<td align=\"center\">"
				+ "<h1>"+SimresGUI.PROGRAM_NAME+"</h1>"
				+ " version:<b>" + Version.VERSION + "</b><br/>"+dat
				+ "</td>"
				+ "</tr>"
				+ "<tr><td colspan=\"2\"  align=\"left\">"
				+ "<br/>Program for neutron ray-tracing simulations<br/>"
				+ "Project homepage: "
				+ "<a href=\"http://neutron.ujf.cas.cz/restrax/\">neutron.ujf.cas.cz/restrax</a>"
				+ "<br/></td>"
				+ "</tr>"
				+ "</table>"
				+ "</div>"
				+ "</body>"
				+ "</html>"	;	
		return html;
	}
	
}
