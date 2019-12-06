package cz.restrax.gui.editors;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.xml.sax.SAXParseException;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.ClassDataCollection;
import cz.jstools.classes.editors.ClassPane;
import cz.jstools.classes.ieditors.IClassEditor;
import cz.restrax.gui.SimresGUI;
import cz.restrax.gui.components.SetupTree;
import cz.restrax.gui.components.SetupTreeHandler;
import cz.restrax.gui.components.SetupTreeNode;
import cz.restrax.sim.InstrumentVerifier;

public class SetupTreeDialogModal extends JDialog implements PropertyChangeListener {
// public class SetupTreeDialog extends InternalDialog {
	private static final long serialVersionUID = 1L;
	private static final int WIDTH_TOTAL=650;
	private static final int HEIGHT_TREE_PANEL=400;
	private static final int HEIGHT_CFG_PANEL=HEIGHT_TREE_PANEL+50;
	//private static final int HEIGHT_EDIT_PANEL=50;
	private static final int WIDTH_LEFT_PANEL=300;
	private static final int WIDTH_RIGHT_PANEL=300;
	private SimresGUI program;
	private JPanel editPanel = null;
	private JTextField idEdit = null;
	private JTextField nameEdit = null;
	private JCheckBox isMonoBox = null;
	private SetupTreeNode selClass = null;
	private SetupTree cfgTree=null;
	private SetupTree repTree=null;
	
	ClassDataCollection primary=null;
	ClassDataCollection secondary=null;
	ClassDataCollection specimen=null;
	Vector<String> monochromators=null;
	Vector<String> analyzers=null;
	private static final String OKBtn="OK";
	private static final String CancelBtn="Cancel";
	
	JOptionPane optionPane;
	JPanel mainPanel;
	
	public SetupTreeDialogModal(SimresGUI program) {
		super(program.getRootWindow(), true);	
		this.program=program;
		setTitle("Layout editor");
		String[] options = {OKBtn,CancelBtn};
		// create main panel with dialog components
		mainPanel=createMainPanel();
		// create the JOptionPane.
        optionPane = new JOptionPane(mainPanel,
                                    JOptionPane.PLAIN_MESSAGE,
                                    JOptionPane.YES_NO_OPTION,
                                    null,
                                    options,
                                    options[0]);        
        setContentPane(optionPane);
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);	
		
		//Register an event handler that reacts to option pane state changes.
        optionPane.addPropertyChangeListener(this);
        
        /* Change property instead of closing window.
         * Validation and closing is then handled by propertyChange */            
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
            optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
            }
        });

		
		//setResizable(false); // resize does not work with modal window ...
		//setupVerifier=new SetupVerifier();
		//optionPane.setInputVerifier(new SetupVerifier());
	}
	
	public void showDialog() {	
		setLocationRelativeTo(program.getRootWindow());
		setVisible(true);
	}
    
	/** This method clears the dialog and hides it. */
    public void closeDialog() {
        setVisible(false);
    }
    
//	public void closeDialog(int byWhat) {
	//	program.getRootWindow().setEnabled(true);
//		super.closeDialog(byWhat);
	//}
	
	private JPanel createMainPanel() {
	// config. tree DdD handler				
		SetupTreeHandler cfgHnd = new SetupTreeHandler();
		cfgHnd.setSourceActions(TransferHandler.COPY | TransferHandler.MOVE);
		cfgHnd.setAllowImport(true);
		cfgHnd.setRenameToCID(false);
		cfgHnd.ID="CFG";
    // collect instrument data and create config. tree
		ClassDataCollection[] collection=new ClassDataCollection[3];
		collection[0]=program.getSpectrometer().getPrimarySpec();
		collection[1]=program.getSpectrometer().getSpecimen();
		collection[2]=program.getSpectrometer().getSecondarySpec();	
		cfgTree = new SetupTree("Instrument",cfgHnd,SetupTreeNode.NAME_ID | SetupTreeNode.NAME_NAME);
		cfgTree.createNodes(collection,false);
		cfgTree.setDropMode(DropMode.ON_OR_INSERT);
	//	cfgTree.setPreferredSize(new Dimension(WIDTH_RIGHT_PANEL, HEIGHT_TREE_PANEL));
		cfgTree.addKeyListener(new SetupKeyListener());
	//	cfgTree.addMouseListener(new SetupDblClickListener());
		cfgTree.setVisible(true);
		
		
	// repository tree DdD handler
		SetupTreeHandler repHnd = new SetupTreeHandler();
		// repHnd.setSourceActions(TransferHandler.COPY);
		//repHnd.setCanImport(false);
		repHnd.setSourceActions(TransferHandler.COPY | TransferHandler.MOVE);
		repHnd.setAllowImport(true);
		repHnd.setRenameToCID(true);
		repHnd.ID="REP";
		
	// collect repository data and create repository. tree
		collection=program.getRepository().getData();	
		repTree = new SetupTree("Repository",repHnd,SetupTreeNode.NAME_NAME);
		repTree.createNodes(collection,true);
		
		repTree.setDropMode(DropMode.ON_OR_INSERT);
	  // move only within repository, otherwise force copy
		repTree.setMoveLocalOnly(true);
		// repTree.setPreferredSize(new Dimension(WIDTH_LEFT_PANEL, HEIGHT_TREE_PANEL));
		repTree.setVisible(true);
		
  // put all together		
		JPanel pane = new JPanel(new BorderLayout(4,4));
		
        Dimension minimumSize = new Dimension(100, 50);
		JScrollPane leftPane = new JScrollPane(repTree);
		leftPane.setMinimumSize(minimumSize);
		leftPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		leftPane.setPreferredSize(new Dimension(WIDTH_LEFT_PANEL, HEIGHT_TREE_PANEL));
		
				
		JScrollPane rightPane = new JScrollPane(cfgTree);       
        rightPane.setMinimumSize(minimumSize);
        rightPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        rightPane.setPreferredSize(new Dimension(WIDTH_RIGHT_PANEL, HEIGHT_TREE_PANEL));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,leftPane,rightPane);
        splitPane.setDividerLocation(WIDTH_TOTAL/2);
        splitPane.setPreferredSize(new Dimension(WIDTH_TOTAL, HEIGHT_CFG_PANEL+50));
		
		JPanel cfgPanel = new JPanel(new BorderLayout(4,4));
		cfgPanel.setPreferredSize(new java.awt.Dimension(WIDTH_TOTAL, HEIGHT_CFG_PANEL));
		cfgPanel.add(splitPane);
		pane.add(cfgPanel, java.awt.BorderLayout.CENTER);
		pane.add(getEditPanel(), java.awt.BorderLayout.SOUTH);

		getContentPane().add(pane, java.awt.BorderLayout.CENTER);
		// mainPanel.revalidate();
		
	//	cfgTree.addMouseListener(new NodeClickListener());
		
		cfgTree.addTreeSelectionListener(new SetupSelectionListener());
	// pack layout
		pack();
		return pane;
	}
	
	
	private JPanel getEditPanel() {
		if (editPanel==null) {
			GridBagConstraints gc = new GridBagConstraints();
			gc.fill = GridBagConstraints.HORIZONTAL;
			gc.insets = new java.awt.Insets(4, 4, 4, 4);
			gc.anchor= GridBagConstraints.WEST;
			
			editPanel=new JPanel(new GridBagLayout());
	//		editPanel.setPreferredSize(new Dimension(WIDTH_TOTAL,HEIGHT_EDIT_PANEL));
			JLabel idLabel = new JLabel("ID");
			idEdit = new JTextField(8);
			idEdit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (selClass!=null) {
						String s = cfgTree.getUniqueId(selClass,((JTextField)e.getSource()).getText(),true);
						selClass.setId(s);
						cfgTree.getModel().nodeChanged(selClass);
					}
				}
			});
			JLabel nameLabel = new JLabel("name");
			nameEdit = new JTextField(16);			
			nameEdit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (selClass!=null) {
						selClass.setName(((JTextField)e.getSource()).getText());
						cfgTree.getModel().nodeChanged(selClass);
					}
					
				}				
			});
			isMonoBox = new JCheckBox("monochromator/analyzer");
			isMonoBox.setToolTipText("Check if the component is a monochromator or analyzer");
			isMonoBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (selClass!=null) {
						selClass.getUserObject().setMonochromator(isMonoBox.isSelected());
						cfgTree.getModel().nodeChanged(selClass);
					}
				}
			});
			JButton btnNew = new JButton();
			btnNew.setText("Clear");
			btnNew.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {	
					cfgTree.setEmptyInstrument();
				}
			});
				
			
			editPanel.add(idLabel,gc);
			editPanel.add(idEdit,gc);
			editPanel.add(nameLabel,gc);
			editPanel.add(nameEdit,gc);	
			editPanel.add(isMonoBox,gc);
			editPanel.add(btnNew,gc);
		}
		return editPanel;
	}
	
	
	public void updateSpectrometer() throws SAXParseException {		
		update();
		program.getSpectrometer().setPrimarySpec(primary);
		program.getSpectrometer().setSecondarySpec(secondary);
		program.getSpectrometer().setSpecimen(specimen);
		program.getSpectrometer().updateMonochromators();		
	}
	
	

	private class SetupSelectionListener implements TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent e) {
			TreePath path =  e.getNewLeadSelectionPath();
		//	System.out.printf("SetupSelectionListener.valueChanged %s\n",path);
			if (path != null) {
				Object o = e.getNewLeadSelectionPath().getLastPathComponent();
				if (o instanceof SetupTreeNode) {		
					SetupTreeNode cn = (SetupTreeNode)o;
					idEdit.setText(cn.getId());
					nameEdit.setText(cn.getName());
					isMonoBox.setSelected(cn.getUserObject().isMonochromator());
					idEdit.setEnabled(true);
					nameEdit.setEnabled(true);
					isMonoBox.setEnabled(true);
					selClass=(SetupTreeNode)o;
					return;
				}				
			} 
			selClass=null;
			idEdit.setText("");
			nameEdit.setText("");
			idEdit.setEnabled(false);
			nameEdit.setEnabled(false);		
			isMonoBox.setEnabled(false);
		}
    	
    }
	
	private class SetupKeyListener implements KeyListener {

		public void keyPressed(KeyEvent e) {
	//		System.out.printf("pane.value=%s\n", getPane().getValue());
		}

		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_DELETE) {
				if (selClass!= null)  {
					cfgTree.getModel().removeComponent(selClass);
				}
			}
		}
		public void keyTyped(KeyEvent e) {
		}
	}
	
	private class SetupDblClickListener implements MouseListener {
		public void mouseClicked(MouseEvent e) {
			if ( (e.getClickCount()>1) && (selClass!= null)) {
				Point origin = program.getRootWindow().getLocation();
				ClassPane pn = new ClassPane(program.getGuiExecutor(), selClass.getUserObject(), true);
				pn.InitProperties();
				pn.setLinkToInstrument(false);
				IClassEditor ed = new IClassEditor(origin,program.getDesktop());
				ed.InitProperties(pn);
				// to be resolved:
				//getGlass().add(ed);			
				ed.setVisible(true);
			}
		}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
	}
	
	private void update() {
		primary = new ClassDataCollection("PRIMARY");
		secondary = new ClassDataCollection("SECONDARY");
		specimen = new ClassDataCollection("SAMPLE");
		monochromators=new Vector<String> ();
		analyzers=new Vector<String> ();		
		cfgTree.updateClassCollection();
		ClassDataCollection[] collection=cfgTree.getCollection();
		for (int i =0;i<collection.length;i++) {
			if (collection[i].getName().equals("PRIMARY")) {					
				primary=collection[i];
			} else if (collection[i].getName().equals("SECONDARY")) {
				secondary=collection[i];
			} else if (collection[i].getName().equals("SAMPLE")) {
				specimen=collection[i];
			}
		}		
		ClassData cd;
		monochromators.clear();
		for (int i=0;i<primary.size();i++) {
			cd = primary.get(i);
			if (cd.isMonochromator()) monochromators.add(cd.getId());
		}
		analyzers.clear();
		for (int i=0;i<secondary.size();i++) {
			cd = secondary.get(i);
			if (cd.isMonochromator()) analyzers.add(cd.getId());
		}
		// update repository
		repTree.updateClassCollection();
		program.getRepository().importClasses(repTree.getCollection());
	}	
	
	 /** This method reacts to state changes in the option pane. */
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();

        if (isVisible()
         && (e.getSource() == optionPane)
         && (JOptionPane.VALUE_PROPERTY.equals(prop) ||
             JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
            Object value = optionPane.getValue();
            // no action
            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                return;
            }
            //Reset the JOptionPane's value.
            //If you don't do this, then if the user
            //presses the same button next time, no
            //property change event will be fired.
            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

            if (OKBtn.equals(value)) {
            	update();
            	InstrumentVerifier ver = new InstrumentVerifier(program.getClasses(), primary, specimen,secondary);
    			boolean valid = ver.verify(this);
            	if (valid) {
            		try {
    					int answ=JOptionPane.showConfirmDialog(this, 
    							"Previous configuration data will be lost. Continue?", 
    							"Confirm", JOptionPane.YES_NO_OPTION);
    					if (answ == JOptionPane.YES_OPTION) {
    						updateSpectrometer();
    						program.sendParameters();
    						program.reset3DScene();
    					}
    				} catch (SAXParseException ex) {
    					JOptionPane.showMessageDialog(program.getRootWindow(),ex.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
    				}
    				program.getRootWindow().updateGUI();
            		closeDialog();            		
            	} else {
            		// nothing ...
            	} 
            // cancel or closed window
            } else {
            	closeDialog();
            }
        }
    }
	
}
