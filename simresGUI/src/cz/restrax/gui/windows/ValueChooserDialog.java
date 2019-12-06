package cz.restrax.gui.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyVetoException;
import java.util.zip.DataFormatException;

import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreePath;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.ClassDataCollection;
import cz.jstools.classes.FieldData;
import cz.jstools.classes.FieldType;
import cz.jstools.classes.FloatDef;
import cz.jstools.classes.RangeData;
import cz.jstools.classes.ValueRange;
import cz.jstools.classes.definitions.Utils;
import cz.jstools.classes.editors.propertiesView.Browsable;
import cz.jstools.classes.ieditors.InternalDialog;
import cz.restrax.gui.SimresGUI;
import cz.restrax.gui.components.SetupTree;
import cz.restrax.gui.components.SetupTreeNode;
import cz.restrax.gui.components.ValueRangeTable;
import cz.restrax.gui.components.ValueTreeHandler;
import cz.restrax.gui.components.ValueTreeNode;

public class ValueChooserDialog extends InternalDialog implements Browsable {
	//public class ConfigTreeDialog extends InternalDialog {
	private static final long serialVersionUID = 1L;
	private static final int WIDTH_NAVIG=250;
	private static final int WIDTH_TABLE=500;
	private static final int HEIGHT_WINDOW=400;
	private static final int WIDTH_WINDOW=WIDTH_NAVIG+WIDTH_TABLE;
	private static final int HEIGHT_BTNPANEL=40;
	protected static final int      CONTROL_BUTTON_HEIGHT = 25;
	protected static final int      CONTROL_BUTTON_WIDTH  = 80;
//	private SimresGUI program;
	private JScrollPane navigPane = null;
	private JScrollPane tablePane = null;
	private JPanel pnlButtons = null;
	private ValueRangeTable varTable = null;
	private SetupTree cfgTree=null;
	private JTextField valEdit = null;
	private JLabel unitsLabel = null;
	private JLabel description = null;
	private JButton btnApply = null;
	private JButton btnCancel = null;
	private String msg=null;
	private boolean[] validRows=null;
	
	
	ClassDataCollection primary=null;
	ClassDataCollection secondary=null;
	ClassDataCollection specimen=null;
	RangeData data = null;
	private final SimresGUI program;
	
	public ValueChooserDialog(SimresGUI program, RangeData data) {
		super(program.getDesktop());	
		this.program=program;
	    this.data=data;	
		setLocation(new Point(100,100));
		setTitle("Value range editor");
		setClosable(true);
		setIconifiable(false);
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		initComponents();	
		setResizable(true); // resize does not work with modal window ...
	//    varTable.setData(data.clone());
	//	this.setInputVerifier(new SetupVerifier());
	}

	
	public boolean validateTable() {
		boolean isOK=true;;
		RangeData ranges=null;
		validRows = null;
		msg="";		
		try {
			ranges = varTable.getData();
		} catch (DataFormatException e) {
			isOK=false;
			msg="Can't parse RangeData from table.\n"+e.getMessage();
			return isOK;
		}
		ValueRange range=null;
		FieldData fd;
		ClassData cls=null;
		int index;
		validRows = new boolean[ranges.getNp()];
		for (int i=0;i<ranges.getNp();i++) {
			validRows[i]=true;
			range=ranges.getValue(i);
			String sc = range.getClassID();
			String sf = range.getFieldID();	
			index =range.getIndex();
			try {
				cls=getClassFromId(sc);
				fd=cls.getField(sf);
				boolean indexOK = (fd.getType().isVector() & (index>0)) | ((! fd.getType().isVector()) & (index>=0));
				indexOK = indexOK & index <= fd.getType().size;
				if (! indexOK) {
					throw new Exception("Field index out of range for "+fd.id+" index="+index);
				}				
			} catch (Exception e) {
				validRows[i]=false;
				isOK=false;
				msg+=e.getMessage()+"\n";
			}
			
		}
		return isOK;
	}
	
	private void updateCfgTree() {
    // collect instrument data and create config. tree
		ClassDataCollection[] collection=new ClassDataCollection[3];
		collection[0]=program.getSpectrometer().getPrimarySpec();
		collection[1]=program.getSpectrometer().getSpecimen();
		collection[2]=program.getSpectrometer().getSecondarySpec();	
		getCfgTree().createNodes(collection,false);
	}
	
	private ClassData getClassFromId(String id) {
		ClassData cls=null;;
		cls=program.getSpectrometer().getPrimarySpec().get(id);
		if (cls==null) cls=program.getSpectrometer().getSpecimen().get(id);
		if (cls==null) cls=program.getSpectrometer().getSecondarySpec().get(id);
		return cls;
	}

	private void initComponents() {			
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,getNavigPane(),getTablePane());
        splitPane.setDividerLocation(WIDTH_NAVIG);
        splitPane.setPreferredSize(new Dimension(WIDTH_WINDOW, HEIGHT_WINDOW)); 
		
		
		getContentPane().add(splitPane, java.awt.BorderLayout.CENTER);
		getContentPane().add(getPnlButtons(), java.awt.BorderLayout.SOUTH);
		updateCfgTree();
		pack();
	}
	
	
	protected JPanel getPnlButtons() {
		if (pnlButtons == null) {
			pnlButtons = new JPanel();
			pnlButtons.setLayout(new GridBagLayout());
			
			GridBagConstraints c = new GridBagConstraints();
			c.gridy = 0;
			c.weighty = 0.0;
			c.insets  = new java.awt.Insets(5, 5, 5, 5);
			c.fill    = GridBagConstraints.NONE;
			
			c.gridx = 0;
			c.weightx = 0.0;
			c.anchor  = GridBagConstraints.WEST;
			JLabel idLabel = new JLabel("value");
			pnlButtons.add(idLabel, c);
			
			c.gridx = 1;
			c.weightx = 0.0;
			c.anchor  = GridBagConstraints.WEST;
			unitsLabel = new JLabel(" ");
			unitsLabel.setPreferredSize(new Dimension(60,CONTROL_BUTTON_HEIGHT));
			pnlButtons.add(unitsLabel, c);
			
			c.gridx = 2;
			c.weightx = 0.0;
			c.anchor  = GridBagConstraints.WEST;
			valEdit = new JTextField();
			valEdit.setEnabled(true);
			valEdit.setEditable(false);
			valEdit.setPreferredSize(new Dimension(60,CONTROL_BUTTON_HEIGHT));
			pnlButtons.add(valEdit, c);

			c.gridx = 3;
			c.weightx = 0.0;
			c.anchor  = GridBagConstraints.WEST;
			description = new JLabel(" ");
		//	description.setPreferredSize(new Dimension(100,CONTROL_BUTTON_HEIGHT));
			pnlButtons.add(description, c);

	// APPLY		
			c.gridx = 4;
			c.weightx = 1.0;
			c.anchor  = GridBagConstraints.EAST;
			pnlButtons.add(getBtnApply(), c);
	// CANCEL	
			c.gridx = 5;
			c.weightx = 0.0;
			c.anchor  = GridBagConstraints.EAST;
			pnlButtons.add(getBtnCancel(), c);
			
			
			
		}
		return pnlButtons;
	}
	
	protected JButton getBtnApply() {
		if (btnApply == null) {
			btnApply = new JButton();
			btnApply.setPreferredSize(new java.awt.Dimension(CONTROL_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT));
			btnApply.setText("Apply");
			btnApply.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					RangeData tmpdata = null;
					try {
						tmpdata = varTable.getData();
					} catch (DataFormatException e2) {
						// this should not happen, getData is checked inside validateTable
						msg+=e2.getMessage();
					}
					if (validateTable()) {						
						if (tmpdata!=null) data.assign(tmpdata);
						closeDialog();
					} else {
						Object[] options = {"Delete invalid items", "Cancel", "Ignore"};
						int result = JOptionPane.showOptionDialog(null,
								"Errors found in table:\n"+msg,
								"Error in parameter range data",								
								JOptionPane.DEFAULT_OPTION,
								JOptionPane.ERROR_MESSAGE,
								null,options,options[0]		
						);
						if ((tmpdata!=null) & (validRows!=null) & (result==0)) {
							for (int i=0;i<tmpdata.getNp();i++) {
								if (! validRows[i]) {
									varTable.deleteRow(i);
								}
							}
						}
						if (result==2) {
							closeDialog();
						}
					}
				}
			});
		}
		return btnApply;
	}

	protected JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setText("Cancel");
			btnCancel.setPreferredSize(new java.awt.Dimension(CONTROL_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT));
			btnCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					closeDialog();
				}
			});
		}
		return btnCancel;
	}

	private JScrollPane getTablePane() {
		if (tablePane==null) {
			JPanel tablePnl = new JPanel(new BorderLayout(4,4));
			tablePnl.setPreferredSize(new Dimension(WIDTH_TABLE,HEIGHT_WINDOW-HEIGHT_BTNPANEL));
			varTable = new ValueRangeTable(data.getType().size,ValueRange.ALL,data);
			varTable.setVisible(true);	
	//		DefaultTableColumnModel cmodel = (DefaultTableColumnModel) varTable.getColumnModel();
			tablePane = new JScrollPane(varTable);   						
			tablePane.setMinimumSize(new Dimension(100, 50));      
			tablePane.setWheelScrollingEnabled(true);	
			tablePnl.add(tablePane, java.awt.BorderLayout.CENTER);	
			ValueTreeHandler valHnd = new ValueTreeHandler();
			valHnd.setSourceActions(TransferHandler.NONE);
			valHnd.setCanImport(true);
			varTable.setDropMode(DropMode.ON_OR_INSERT);
			varTable.setDragEnabled(false);			
			varTable.setTransferHandler(valHnd);
			varTable.getTableHeader().setTransferHandler(valHnd);
			varTable.setMinimumSize(new Dimension(200,100));
			varTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			varTable.addKeyListener(new ValueKeyListener());
		}
		return tablePane;
	}
	
	
	private JScrollPane getNavigPane() {
		if (navigPane==null) {
			JPanel navPnl = new JPanel(new BorderLayout(4,4));		
			navPnl.setPreferredSize(new Dimension(WIDTH_NAVIG,HEIGHT_WINDOW));
			navigPane = new JScrollPane(getCfgTree());       
			navigPane.setMinimumSize(new Dimension(100, 50));      
			navigPane.setWheelScrollingEnabled(true);	
		//	JPanel cfgPanel = new JPanel(new BorderLayout(4,4));
		//	cfgPanel.add(treePane,BorderLayout.CENTER);
		//	navigPanel.add(treePane, java.awt.BorderLayout.CENTER);
			// navigPanel.add(getEditPanel(), java.awt.BorderLayout.SOUTH);															
		}
		return navigPane;
	}
	
	private SetupTree getCfgTree() {
		if (cfgTree==null) {
			ValueTreeHandler valHnd = new ValueTreeHandler();
			valHnd.setSourceActions(TransferHandler.COPY);
			valHnd.setCanImport(false);				
			cfgTree = new SetupTree("Instrument",
					valHnd,
					SetupTreeNode.NAME_ID | SetupTreeNode.NAME_NAME);		
			cfgTree.setShowValues(true);
			cfgTree.setDragEnabled(true);
			getCfgTree().addTreeSelectionListener(new ValueSelectionListener());			
		}
		return cfgTree;
	}

	private class ValueSelectionListener implements TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent e) {
			TreePath path =  e.getNewLeadSelectionPath();
		//	System.out.printf("SetupSelectionListener.valueChanged %s\n",path);
			if (path != null) {
				Object o = e.getNewLeadSelectionPath().getLastPathComponent();
				if (o instanceof ValueTreeNode ) {
					ValueTreeNode vn = (ValueTreeNode)o;
					if (vn.isLeaf()) {
						FieldData fd = vn.getFieldData();
						valEdit.setText(vn.valueToString());
						description.setText(fd.getType().hint);
						if (fd.getType().tid == FieldType.FLOAT) {
							FloatDef fval = (FloatDef)fd.getType();
							unitsLabel.setText(
									Utils.math2Html("["+fval.units+"]")
									);
						} else unitsLabel.setText("[ ]");
						return;
					}
				}
			} 
			valEdit.setText("");
			description.setText("");
			unitsLabel.setText("");
		}
    	
    }
	
	private class ValueKeyListener implements KeyListener {

		public void keyPressed(KeyEvent e) {
	//		System.out.printf("pane.value=%s\n", getPane().getValue());
		}

		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_DELETE) {
				DefaultTableModel model = (DefaultTableModel)varTable.getModel();
				int[] rows = varTable.getSelectedRows();
				for (int i = 0; i< rows.length;i++) {
					model.removeRow(rows[i]);
				}
				varTable.repaint();				
			}
		}

		public void keyTyped(KeyEvent e) {
		}
		
	}
	
	public Object browse(Object content) {
		Point o = getContentPane().getParent().getLocation();
		o.x =+ 100;
		o.y =+ 100;
		setLocation(o);
		if (this.isOnDesktop()) {
			try {
				this.setSelected(true);
			} catch (PropertyVetoException ex) {
				this.moveToFront(); 
				System.err.println(Utils.getDebugHdr());
				System.err.println("Unable to select dialog");
				System.err.println("Reason: " + ex.getMessage());
			}
		} else {
			this.showDialog();
		}		
		return  data;
	}
		
}
