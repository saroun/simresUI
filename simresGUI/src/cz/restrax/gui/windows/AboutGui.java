package cz.restrax.gui.windows;


import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import cz.restrax.gui.SimresGUI;


/**
 * Zobrazr informace o programu.
 *
 *
 * @author   Jan Saroun Svoboda, Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.11 $</dt>
 *               <dt>$Date: 2019/04/01 13:10:27 $</dt></dl>
 */
public class AboutGui extends JDialog {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                      CONSTANTS                                       //
	//////////////////////////////////////////////////////////////////////////////////////////
	private static final long  serialVersionUID = -2963030103376026112L;

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private JPanel  pnlContentPane = null;
	private JPanel  pnlButtons = null;
	private JButton  btnOk         = null;
	private SimresGUI program;

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public AboutGui(SimresGUI program) {		
		super(program.getRootWindow());
		this.program=program;
		initialize();
		if (program.getRootWindow() != null) {
			this.setLocation(program.getRootWindow().getLocation());
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  GUI INITIALIZATION                                  //
	//////////////////////////////////////////////////////////////////////////////////////////
	private void initialize() {
		this.setResizable(true);
		this.setPreferredSize(new Dimension(450,520));
		this.setTitle("About");
		this.setContentPane(getPnlContentPane());
	//	this.setLocation(new java.awt.Point(0,0));
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setModal(true);
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
			
		
			pnlContentPane = new JPanel();
			pnlContentPane.setLayout(new BorderLayout());			
			pnlContentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			JPanel rightPanel = new JPanel();
			
			rightPanel.setLayout(new BoxLayout(rightPanel,BoxLayout.Y_AXIS));
			rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			rightPanel.setPreferredSize(new Dimension(400,400));
			//rightPanel.add(new VersionPane());
			rightPanel.add(new CopyrightPane(program.getVersion()));
			rightPanel.add(getPnlButtonsPane());
			
			
			pnlContentPane.add(rightPanel,BorderLayout.CENTER);
		}
		return pnlContentPane;
	}
	

	private JPanel getPnlButtonsPane() {
		if (pnlButtons == null) {						
			pnlButtons = new JPanel();
			pnlButtons.setLayout(new BoxLayout(pnlButtons,BoxLayout.X_AXIS));
			pnlButtons.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
			pnlButtons.add(getBtnOk());
			// pnlButtons.add(Box.createHorizontalGlue());
			// pnlButtons.add(getBtnInfo());
			// pnlButtons.add(Box.createHorizontalStrut(10));
			// pnlButtons.add(getBtnStart());
		}
		return pnlButtons;
	}
	
	/*"***************************************************************************************
	* BUTTONS                                                                                *
	*****************************************************************************************/
	private JButton getBtnOk() {
		if (btnOk == null) {
			btnOk = new JButton();
			btnOk.setPreferredSize(new java.awt.Dimension(80,25));
			//btnOk.setBounds(new java.awt.Rectangle(310,115,80,25));
			btnOk.setText("Close");
			btnOk.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dispose();
				}
			});
		}
		return btnOk;
	}
}