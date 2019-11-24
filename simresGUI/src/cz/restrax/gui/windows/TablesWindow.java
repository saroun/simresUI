package cz.restrax.gui.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import cz.restrax.gui.SimresGUI;
import cz.restrax.gui.WinConsoleMessages;
import cz.restrax.gui.resources.Resources;
import cz.restrax.sim.tables.Tables;
import cz.saroun.classes.definitions.Constants;


public class TablesWindow extends JDialog  {
	private static final long serialVersionUID = 1L;
	private static final Dimension BTN_SIZE=new Dimension(40,20);
	private static final int MARGIN=5;
	private JPanel  pnlContentPane          = null;
	private JPanel  pnlCombo = null;
	private JComboBox<String> cmbLists = null;
	private JScrollPane  scrPane          = null;
	private JLabel  lblIcon              = null;
	protected int  returnValue           = Constants.CLOSE_BUTTON;	
	///////////////////////////////////////////////
	private JTable  table          = null;
	private ArrayList<Tables.TablesItem>  data   = null;
		
	private JButton btnOk;
	private JButton btnUpdate;
	private final Tables tables;
	private final String idKeyName;
	private final SimresGUI  program;
	
	
	public TablesWindow(SimresGUI  program, Tables tables, String idKeyName) {
		super(program.getRootWindow()); 
		this.program=program;
		this.tables=tables;
		this.idKeyName=idKeyName;
		data = tables.getValidList();	
		this.setTitle("List of "+tables.getTITLE());
		initialize();
	}
	
	private void initialize() {
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.setSize(new java.awt.Dimension(700,300));
		this.setResizable(true);
		this.setContentPane(getPnlContentPane());
		this.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
		//this.setModal(true);		
		this.getRootPane().setWindowDecorationStyle(JRootPane.INFORMATION_DIALOG);		
		this.setLocationRelativeTo(null);
	}
	
	private JPanel getPnlContentPane() {
		if (pnlContentPane == null) {
			pnlContentPane = new JPanel();
			GridBagLayout layout = new GridBagLayout();			
			GridBagConstraints constraints = new GridBagConstraints();
			pnlContentPane.setLayout(layout);
			
			JPanel rightPanel = new JPanel();
			rightPanel.setLayout(new BorderLayout());
			rightPanel.add(getScrPane(), BorderLayout.CENTER);
			JPanel p = getPnlCombo();
			if (p!=null) {
				rightPanel.add(getPnlCombo(), BorderLayout.SOUTH);
			}
			
			/*
			 * Scroll window
			 */
			constraints.insets = new java.awt.Insets(0, 0, 0, 0);
			constraints.gridx = 1;
			constraints.gridy = 0;
			constraints.gridwidth = 1;
			constraints.gridheight = 3;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;
			constraints.anchor = GridBagConstraints.CENTER;
			constraints.fill = GridBagConstraints.BOTH;
			pnlContentPane.add(rightPanel, constraints);

			/*
			 * Icon, left edge
			 */
			lblIcon = new JLabel();
			lblIcon.setIcon(Resources.getImage("messagebox_info.png"));
			constraints.insets = new java.awt.Insets(2, 2, 0, 2);
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;
			constraints.weightx = 0.0;
			constraints.weighty = 1.0;
			constraints.anchor = GridBagConstraints.NORTH;
			constraints.fill = GridBagConstraints.NONE;
			pnlContentPane.add(lblIcon, constraints);

			/*
			 * Button
			 */
			constraints.insets = new java.awt.Insets(0, 2, 2, 2);
			constraints.gridx = 0;
			constraints.gridy = 1;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;
			constraints.weightx = 0.0;
			constraints.weighty = 0.0;
			constraints.ipady = 2;  
			constraints.anchor = GridBagConstraints.CENTER;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			pnlContentPane.add(getBtnUpdate(), constraints);
			constraints.gridy = 2;
			pnlContentPane.add(getBtnOk(), constraints);
		}
		return pnlContentPane;
	}
	
	public void updateTable() {
		MirrorTableModel model = (MirrorTableModel) table.getModel();
		model.updateTable();
		table.repaint();		
	}
	
	public int showDialog() {		
		this.setVisible(true);
		return returnValue;
	}
	
	protected void closeDialog(int byWhat) {
		this.setVisible(false);
		returnValue = byWhat;		
	}
	
	/*"***************************************************************************************
	* SCROLL PANES                                                                           *
	*****************************************************************************************/
	private JScrollPane getScrPane() {
		if (scrPane == null) {
			scrPane = new JScrollPane();
			scrPane.setViewportView(getTable());
			getTable().setFillsViewportHeight(true);
		}
		return scrPane;
	}
	
	/*"***************************************************************************************
	* Combo                                                                                *
	*****************************************************************************************/	
	private JPanel getPnlCombo() {
		if (pnlCombo == null) {
			JComboBox<String> cb = getCmbProject();
			if (cb != null) {
				pnlCombo = new JPanel();
				pnlCombo.setLayout(new BoxLayout(pnlCombo,BoxLayout.X_AXIS));	
				pnlCombo.setPreferredSize(new Dimension(WIDTH-3*MARGIN,BTN_SIZE.height+2*MARGIN));
				pnlCombo.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));
				pnlCombo.add(new JLabel("Select tables"));	
				pnlCombo.add(Box.createHorizontalStrut(10));
				pnlCombo.add(cb);				
			}
		}
		return pnlCombo;
	}
	
	private JComboBox<String> getCmbProject() {
		if (cmbLists == null) {
			String[] items = tables.getTabListsLabels();
			if (items.length>0) {
				cmbLists = new JComboBox<String>(items);
				cmbLists.setPreferredSize(new Dimension(250,BTN_SIZE.height));
				cmbLists.setSelectedIndex(tables.getSelectedList());
				cmbLists.addActionListener (new ActionListener () {
				    public void actionPerformed(ActionEvent e) {
				    	int isel = cmbLists.getSelectedIndex();
				    	if (isel != tables.getSelectedList()) {
					    	int ans = WinConsoleMessages.showYesNoDialog(
									"<html>You are going to change the tables.<br/>" +
									"It may affect simulation results.<br/>" +	
									"Do you want to continue?</html>", 
									1
							);
							if (ans==0) {
								program.getExecutor().tablesChange(tables, isel);
								// tables.changeDefaultList(isel);
							} else {
								cmbLists.setSelectedIndex(tables.getSelectedList());
							}				    		
				    	}
				    }
				});
			}
		}
		return cmbLists;
	}

	/*"***************************************************************************************
	* BUTTONS                                                                                *
	*****************************************************************************************/
	private JButton getBtnOk() {
		if (btnOk == null) {
			btnOk = new JButton();
			btnOk.setText("Close");
			btnOk.setPreferredSize(BTN_SIZE);			
			btnOk.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
						closeDialog(Constants.OK_BUTTON);
				}
			});
		}
		return btnOk;
	}

	private JButton getBtnUpdate() {
		if (btnUpdate == null) {
			btnUpdate = new JButton();
			btnUpdate.setPreferredSize(BTN_SIZE);
			btnUpdate.setText("Reload");
			btnUpdate.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					program.getExecutor().tablesReload(tables);
					/*
					tables.reloadTables();
					tables.flushAllTables();
					*/
				}
			});
		}
		return btnUpdate;
	}
	
	/*"***************************************************************************************
	* EDITOR PANES                                                                           *
	*****************************************************************************************/
	private JTable getTable() {
		if (table == null) {
			table = new JTable(new MirrorTableModel());
			//table.getColumnModel().getColumn(0).setPreferredWidth(20);
			//table.getColumnModel().getColumn(1).setPreferredWidth(40);
			//table.getColumnModel().getColumn(2).setPreferredWidth(300);
			table.getColumnModel().getColumn(0).setMaxWidth(60);
			table.getColumnModel().getColumn(1).setMaxWidth(100);			
			DefaultTableCellRenderer ctrRenderer = new DefaultTableCellRenderer();
			ctrRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
			table.getColumn(idKeyName).setCellRenderer( ctrRenderer );
			table.getColumn("type").setCellRenderer( ctrRenderer );						
			table.setEnabled(true);
		}		
		return table;
	}
	
	class MirrorTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private String[] columnNames = {idKeyName,"type", "file"};	
		private String[] types = {"global","project", "unknown"};	
	    
		public void updateTable() {			
			fireTableDataChanged();
			//fireTableStructureChanged();
		}
		
	    public int getColumnCount() {
	        return columnNames.length;
	    }

	    public int getRowCount() {
	        return data.size();
	    }

	    public String getColumnName(int col) {
	        return columnNames[col];
	    }

	    public Object getValueAt(int row, int col) {
	    	Object res=null;
	    	if (col==0) {
	    		res=data.get(row).key;
	    	} else if (col==1) {
	    		if (data.get(row).typ<3 && data.get(row).typ>=0) {
	    			res=types[data.get(row).typ];
	    		} else {
	    			res=types[2];
	    		}
	    	} else if (col==2) {
	    		res=data.get(row).file;
	    	} else {
	    		res=" ";
	    	}
	        return res;
	    }

	    @SuppressWarnings("unchecked")
	    public Class<String> getColumnClass(int c) {
	        return (Class<String>) getValueAt(0, c).getClass();
	    }

	    /*
	     * Don't need to implement this method unless your table's
	     * editable.	     
	    public boolean isCellEditable(int row, int col) {
	        //Note that the data/cell address is constant,
	        //no matter where the cell appears onscreen.
	        if (col < 2) {
	            return false;
	        } else {
	            return true;
	        }
	    }
	    */

	    /*
	     * Don't need to implement this method unless your table's
	     * data can change.
	   
	    public void setValueAt(String value, int row, int col) {
	    	
	        data.get(row)[col] = value;
	        fireTableCellUpdated(row, col);
	    }
	      */
	    
	    
		

	}

}
