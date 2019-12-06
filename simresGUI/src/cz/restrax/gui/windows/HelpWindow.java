package cz.restrax.gui.windows;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import cz.jstools.util.WinHyperlinkListener;
import cz.restrax.gui.SimresGUI;
import cz.restrax.gui.resources.Resources;


public class HelpWindow extends JInternalFrame {
	private JPanel  pnlContentPane          = null;
	private JScrollPane  scrPane          = null;
	///////////////////////////////////////////////
	private JEditorPane  hlpPane          = null;
	private SimresGUI    program           = null;
	
	public HelpWindow(SimresGUI program) {
		this.program = program;		
		initialize();
	}
	
	private void initialize() {
		this.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		this.setIconifiable(true);
		this.setResizable(true);
		this.setContentPane(getPnlContentPane());
		this.setTitle("Help");
		this.setFrameIcon(Resources.getIcon(Resources.ICON16x16, "results_empty.png"));
		hlpPane.addHyperlinkListener(new WinHyperlinkListener());
		// loadResource("test");
	}
	
	private JPanel getPnlContentPane() {
		if (pnlContentPane == null) {
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.insets = new java.awt.Insets(0, 0, 0, 0);
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;
			constraints.fill = GridBagConstraints.BOTH;
			pnlContentPane = new JPanel();
			pnlContentPane.setLayout(layout);
			pnlContentPane.add(getScrPane(), constraints);
		}
		return pnlContentPane;
	}
	
	/*"***************************************************************************************
	* SCROLL PANES                                                                           *
	*****************************************************************************************/
	private JScrollPane getScrPane() {
		if (scrPane == null) {
			scrPane = new JScrollPane();
			scrPane.setViewportView(getHlpPane());
		}
		return scrPane;
	}

	/*"***************************************************************************************
	* EDITOR PANES                                                                           *
	*****************************************************************************************/
	private JEditorPane getHlpPane() {
		if (hlpPane == null) {
			hlpPane = new JEditorPane();
			hlpPane.setEnabled(true);
			hlpPane.setEditable(false);
			hlpPane.setContentType("text/html; charset=UTF-8");
		}
		return hlpPane;
	}
	
	public void showText(String htmlText) {
		hlpPane.setText(htmlText);
	}
	
	public void loadResource(String name) {
		String content = Resources.getHelpText(name+".html");
		hlpPane.setText(content);
	}	
}
