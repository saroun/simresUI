package cz.jstools.classes.editors;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import cz.jstools.classes.editors.propertiesView.Browsable;
import cz.jstools.classes.editors.propertiesView.VString;



/**
 * This class shows a dialog for editing the spectrometer description.
 * @author   Svoboda Jan Saroun, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/17 17:58:45 $</dt></dl>
 */
public class DescriptionDialog extends JDialog implements Browsable {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTANTS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	private static final long  serialVersionUID = 4097611215974618393L;
	private static final int   CONTROL_BUTTON_HEIGHT = 25;
	private static final int   CONTROL_BUTTON_WIDTH  = 80;


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private JPanel       pnlContentPane = null;
	private JPanel       pnlButtons     = null;
	private JTextArea    txaDescription = null;
	private JScrollPane  scrDescription = null;
	private JButton      btnSet         = null;
	private JButton      btnCancel      = null;
	//////////////////////////////////////////////////
	private VString      value          = null;            


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public DescriptionDialog() {
		super();
		initialize();
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  GUI INITIALIZATION                                  //
	//////////////////////////////////////////////////////////////////////////////////////////
	private void initialize() {
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		this.setModal(true);
		this.setTitle("Description");
		this.setContentPane(getPnlContentPane());
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
			pnlContentPane.setLayout(new GridBagLayout());
			
			GridBagConstraints c = new GridBagConstraints();
			
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 1.0;
			c.weighty = 1.0;
			c.fill = GridBagConstraints.BOTH;
			c.anchor = GridBagConstraints.NORTHWEST;
			pnlContentPane.add(getScrDescription(), c);
		
			
			c.gridx = 0;
			c.gridy = 1;
			c.weightx = 1.0;
			c.weighty = 0.0;
			c.fill = GridBagConstraints.BOTH;
			c.anchor = GridBagConstraints.CENTER;
			pnlContentPane.add(getPnlButtons(), c);

			pnlContentPane.setPreferredSize(new java.awt.Dimension(350,120));
		}

		return pnlContentPane;
	}

	/*"***************************************************************************************
	* TEXT AREAS                                                                             *
	*****************************************************************************************/
	private JTextArea getTxaDescription() {
		if (txaDescription == null) {
			txaDescription = new JTextArea();
			txaDescription.setLineWrap(true);
			txaDescription.setName("Description");
			txaDescription.setWrapStyleWord(true);
		}
		return txaDescription;
	}

	/*"***************************************************************************************
	* SCROLL PANES                                                                           *
	*****************************************************************************************/
	private JScrollPane getScrDescription() {
		if (scrDescription == null) {
			scrDescription = new JScrollPane();
			scrDescription.setViewportView(getTxaDescription());
		}
		return scrDescription;
	}

	
	private JPanel getPnlButtons() {
		if (pnlButtons == null) {
			pnlButtons = new JPanel();
			pnlButtons.setLayout(new GridBagLayout());
			
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 1.0;
			c.weighty = 0.0;
			c.insets  = new java.awt.Insets(5, 5, 5, 5);
			c.fill    = GridBagConstraints.NONE;
			c.anchor  = GridBagConstraints.EAST;
			pnlButtons.add(getBtnSet(), c);

			c.gridx = 1;
			c.gridy = 0;
			c.weightx = 1.0;
			c.weighty = 0.0;
			c.insets  = new java.awt.Insets(5, 5, 5, 5);
			c.fill    = GridBagConstraints.NONE;
			c.anchor  = GridBagConstraints.WEST;
			pnlButtons.add(getBtnCancel(), c);
		}
		return pnlButtons;
	}

	private JButton getBtnSet() {
		if (btnSet == null) {
			btnSet = new JButton();
			btnSet.setText("Set");
			btnSet.setPreferredSize(new java.awt.Dimension(CONTROL_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT));
			btnSet.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// Description
					// Note: In text area can be end-of-line character (internaly represented
					//        as '\n'), but restrax description can contain only 1 line. So 
					//        replace end-of-line char by ordinary space ' ';
					value = new VString(txaDescription.getText().replace('\n', ' '));
					dispose();
				}
			});
		}
		return btnSet;
	}


	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setText("Cancel");
			btnCancel.setPreferredSize(new java.awt.Dimension(CONTROL_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT));
			btnCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dispose();
				}
			});
		}
		return btnCancel;
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  IMPLEMENTED INTERFACES                              //
	//////////////////////////////////////////////////////////////////////////////////////////
	public Object browse(Object content) {
		value = (VString)content;
		txaDescription.setText(value.toString());
		setVisible(true);
		return value;
	}
}