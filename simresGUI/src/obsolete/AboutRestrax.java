package cz.restrax.gui.windows;


import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import cz.jstools.util.WinHyperlinkListener;
import cz.restrax.gui.SimresGUI;





/**
 * Zobrazi informace o programu.
 *
 *
 * @author   Svoboda Jan Saroun, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.6 $</dt>
 *               <dt>$Date: 2014/06/18 20:07:54 $</dt></dl>
 */
public class AboutRestrax extends JDialog {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                      CONSTANTS                                       //
	//////////////////////////////////////////////////////////////////////////////////////////
	private static final long  serialVersionUID = -2963030103376026112L;

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private JPanel  pnlContentPane = null;  //  @jve:decl-index=0:visual-constraint="15,41"
	//////////////////////////////////////
//	private JLabel  lblDescription = null;
	private JEditorPane txtDescription = null;
	//////////////////////////////////////
	private JButton  btnOk         = null;
	//////////////////////////////////////
	private SimresGUI  program      = null;

	private JLabel lblTitle = null;

	private JLabel lblSubtitle = null;

	private JLabel lblVersionl = null;

	private JLabel lblLicencel = null;


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////

	public AboutRestrax(SimresGUI program) {
		super(program.getRootWindow());		
		this.program = program;
		initialize();
		if (program.getRootWindow() != null) {
			this.setLocation(program.getRootWindow().getLocation());
		}
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  GUI INITIALIZATION                                  //
	//////////////////////////////////////////////////////////////////////////////////////////
	private void initialize() {
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setPreferredSize(new java.awt.Dimension(400,230));
		this.setResizable(false);
		this.setTitle("About");
		this.setContentPane(getPnlContentPane());
		this.setLocation(new java.awt.Point(0,0));
		this.pack();
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                       GUI BEANS                                      //
	//////////////////////////////////////////////////////////////////////////////////////////
	/*"***************************************************************************************
	* PANELS                                                                                 *
	*****************************************************************************************/
	private JPanel getPnlContentPane() {
		if (pnlContentPane == null) {
			lblLicencel = new JLabel();
			lblLicencel.setBounds(new java.awt.Rectangle(4,177,389,17));
			lblLicencel.setFont(new java.awt.Font("Dialog", java.awt.Font.ITALIC, 10));
			lblLicencel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
			lblLicencel.setText("<html>"
			+ "This is free software under the terms of the GNU General Public License."
			+ "</html>"		
			);
			lblVersionl = new JLabel();
			lblVersionl.setBounds(new java.awt.Rectangle(184,3,200,35));
			lblVersionl.setForeground(new java.awt.Color(0,51,153));
			lblVersionl.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			lblVersionl.setText("<html>"
					+ "<i>version:</i> <b>"+program.getVersion().getRestraxVersion()+"</b><br>"
					+ "<i>build:</i> <b>"+program.getVersion().getRestraxBuild()+"</b>"
					+ "</html>");
			lblSubtitle = new JLabel();
			lblSubtitle.setBounds(new java.awt.Rectangle(6,44,379,25));
			lblSubtitle.setForeground(new java.awt.Color(0,51,153));
			lblSubtitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
			lblSubtitle.setText("Program for neutron ray-tracing simulations");
			lblTitle = new JLabel();
			lblTitle.setBounds(new java.awt.Rectangle(3,1,193,34));
			lblTitle.setFont(new java.awt.Font("Verdana", java.awt.Font.BOLD, 24));
			lblTitle.setForeground(new Color(0x0,0x33,0x99));
			lblTitle.setText("S I M R E S");
			String content = "<html><head>\n" +
			"<style type='text/css'>\n"
		        + "body {font-family: arial, sans-serif, helvetica;"
		          +" font-size:12pt; background-color: #FFFFFF; border-width:0"
		          +" margin:0px ;padding: 0px;"
		        + "}\n"
		+   "</style>\n"+
			"</head><body>"
				+ "<i>Copyright &#x00A9;&nbsp;1995-2012&nbsp;All rights reserved</i><br/>"
				+ "Nuclear Physics Institute, CAS, Rez, Czech Republic<br/>"
				+ "Institut Laue Langevin, Grenoble, France<br/><br>"
				+ "<b>written by</b>: Jan Saroun</a><br/>" 
				+ "<b>project homepage:</b> <a href='http://neutron.ujf.cas.cz/restrax/'>neutron.ujf.cas.cz/restrax</a>"
				+ "</body></html>";
			txtDescription = new JEditorPane("text/html; charset=UTF-8",content);			
			txtDescription.setEditable(false);
			txtDescription.setBackground(Color.lightGray);
			txtDescription.setPreferredSize(new java.awt.Dimension(280,130));
//			txtDescription.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			txtDescription.setBounds(new java.awt.Rectangle(5,73,378,102));
			txtDescription.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
//			txtDescription.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
			txtDescription.setBackground(Color.white);
//			txtDescription.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
			txtDescription.addHyperlinkListener(new WinHyperlinkListener());
			
			pnlContentPane = new JPanel();
			pnlContentPane.setLayout(null);
			pnlContentPane.setForeground(new java.awt.Color(0,51,153));
			pnlContentPane.add(txtDescription, null);
			pnlContentPane.add(getBtnOk(), null);
			pnlContentPane.setPreferredSize(new java.awt.Dimension(360,230));
			pnlContentPane.add(lblTitle, null);
			pnlContentPane.add(lblSubtitle, null);
			pnlContentPane.add(lblVersionl, null);
			pnlContentPane.add(lblLicencel, null);
		}
		return pnlContentPane;
	}

	/*"***************************************************************************************
	* BUTTONS                                                                                *
	*****************************************************************************************/
	private JButton getBtnOk() {
		if (btnOk == null) {
			btnOk = new JButton();
			btnOk.setPreferredSize(new java.awt.Dimension(80,25));
			btnOk.setBounds(new java.awt.Rectangle(289,144,80,25));
			btnOk.setText("Ok");
			btnOk.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dispose();
				}
			});
		}
		return btnOk;
	}
}  //  @jve:decl-index=0:visual-constraint="17,6"