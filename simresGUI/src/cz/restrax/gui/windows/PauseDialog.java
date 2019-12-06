package cz.restrax.gui.windows;


import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JPanel;

import cz.jstools.classes.editors.CommandExecutor;
import cz.jstools.classes.ieditors.InternalDialog;





/**
 * This class opens a nonmodal pause dialog. It contains only two buttons:
 * "Next" and "Quit" and it is used by request restrax during processing
 * its job file.
 *
 *
 * @author   Svoboda Ji��, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.5 $</dt>
 *               <dt>$Date: 2019/01/06 00:08:39 $</dt></dl>
 */
public class PauseDialog extends InternalDialog {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    CONSTANTS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	private static final long  serialVersionUID = 2329412561536737880L;

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private JPanel  pnlContentPane               = null;
	////////////////////////////////////////////////////
	private JButton  btnNext                     = null;
	private JButton  btnQuit                     = null;
	private final CommandExecutor executor;

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public PauseDialog(CommandExecutor executor, JDesktopPane desktop) {
		super(desktop);
		this.executor=executor;
		initialize();
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  GUI INITIALIZATION                                  //
	//////////////////////////////////////////////////////////////////////////////////////////
	private void initialize() {
		this.setResizable(false);
		this.setBounds(new java.awt.Rectangle(0,0,185,68));
		this.setTitle("Job control");
		this.setContentPane(getPnlContentPane());
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
			pnlContentPane.setLayout(null);
			pnlContentPane.add(getBtnNext(), null);
			pnlContentPane.add(getBtnQuit(), null);
		}
		return pnlContentPane;
	}


	/*"***************************************************************************************
	* BUTTONS                                                                                *
	*****************************************************************************************/
	private JButton getBtnNext() {
		if (btnNext == null) {
			btnNext = new JButton();
			btnNext.setBounds(new java.awt.Rectangle(5,5,80,25));
			btnNext.setText("Next");
			btnNext.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					executor.executeCommand("");
					closeDialog();
				}
			});
		}
		return btnNext;
	}

	private JButton getBtnQuit() {
		if (btnQuit == null) {
			btnQuit = new JButton();
			btnQuit.setBounds(new java.awt.Rectangle(90,5,80,25));
			btnQuit.setText("Quit");
			btnQuit.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					executor.executeCommand("Q");
					closeDialog();
				}
			});
		}
		return btnQuit;
	}
}