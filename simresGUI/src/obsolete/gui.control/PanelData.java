package gui.control;

import gui.CTable;
import gui.GuiFileFilter;
import gui.SimresGUI;

import java.awt.Point;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import utils.Utils;


/**
 * Tato t��da vytvo�� panel s ��d�c�mi prvky konzolov�ho programu "Restrax".
 * Tento panel je pak zobrazen v z�lo�ce "Data" ��d�c�ho okna
 * "ControWindow".
 *
 *
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2009/01/27 12:35:36 $</dt></dl>
 */
public class PanelData extends JPanel {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTANTS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	private static final long     serialVersionUID  = -4338832869389210042L;
	private static final String   OPEN_CMD          = "OPEN";
	private static final String   ADD_CMD           = "ADD";

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private JScrollPane  scrDataTable               = null;
	///////////////////////////////////////////////////////
	private JLabel  lblDatalist                     = null;
	private JLabel  lblPAL                          = null;
	///////////////////////////////////////////////////////
	private JTextField  txfDatalistValue            = null;
	///////////////////////////////////////////////////////
	private JButton  btnDataOpen                    = null;
	private JButton  btnDataAdd                     = null;
	private JButton  btnDataDelete                  = null;
	////////////////////////////////////////////////////////////////
	private JSpinner  spnPAL                        = null;
	///////////////////////////////////////////////////////
	private CTable  tblDataTable                    = null;
	///////////////////////////////////////////////////////
	private SimresGUI      program                   = null;
	private DataNameList  dataNameList              = null;
	private String        dataPath                  = ".";  // defaultni nastaveni je na aktualni adresar


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public PanelData() {
		this(new Point(0,0), null);
	}
	
	public PanelData(Point origin, SimresGUI program) {
		super();

		this.program = program;

		initialize();

		super.setLocation(origin);
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    ACCESS METHODS                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	public DataNameList getDataNameList() {
		return dataNameList;
	}

	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}

	public String getDataPath() {
		return dataPath;
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                   OVERRIDEN METHODS                                  //
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Overrides parent's method setEnabled(), so not only JPanel is enabled/disabled
	 * but so all component in it.
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		scrDataTable.setEnabled(enabled);
		tblDataTable.setEnabled(enabled);
		txfDatalistValue.setEnabled(enabled);
		btnDataAdd.setEnabled(enabled);
		btnDataOpen.setEnabled(enabled);
		btnDataDelete.setEnabled(enabled);
		spnPAL.setEnabled(enabled);
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                GUI INITIALIZATION                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	private void initialize() {
		this.setBounds(new java.awt.Rectangle(0,0,550,260));
		lblDatalist = new JLabel();
		lblDatalist.setText("Data name");
		lblDatalist.setBounds(new java.awt.Rectangle(10,210,65,20));
		lblDatalist.setPreferredSize(new java.awt.Dimension(20,20));
		lblPAL = new JLabel();
		lblPAL.setText("PAL");
		lblPAL.setBounds(new java.awt.Rectangle(475,210,40,20));
		lblPAL.setPreferredSize(new java.awt.Dimension(20,20));
		this.setLayout(null);
		this.add(getTxfDatalistValue(), null);
		this.add(getBtnDataAdd(), null);
		this.add(getScrDataTable(), null);
		this.add(getBtnDataDelete(), null);
		this.add(lblDatalist, null);
		this.add(lblPAL, null);
		this.add(getBtnDataOpen(), null);
		this.add(getSpnPAL(), null);
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     GUI BEANS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	/*"***************************************************************************************
	* BUTTONS                                                                                *
	*****************************************************************************************/
	private JButton getBtnDataAdd() {
		if (btnDataAdd == null) {
			btnDataAdd = new JButton();
			btnDataAdd.setText("Add");
			btnDataAdd.setBounds(new java.awt.Rectangle(340,230,60,20));
			btnDataAdd.setMargin(new java.awt.Insets(0, 0, 0, 0));
			btnDataAdd.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getDatalist(ADD_CMD);				}
			});
		}
		return btnDataAdd;
	}

	private JButton getBtnDataOpen() {
		if (btnDataOpen == null) {
			btnDataOpen = new JButton();
			btnDataOpen.setText("Open");
			btnDataOpen.setBounds(new java.awt.Rectangle(275,230,60,20));
			btnDataOpen.setMargin(new java.awt.Insets(0, 0, 0, 0));
			btnDataOpen.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getDatalist(OPEN_CMD);
				}
			});
		}
		return btnDataOpen;
	}

	private JButton getBtnDataDelete() {
		if (btnDataDelete == null) {
			btnDataDelete = new JButton();
			btnDataDelete.setText("Delete");
			btnDataDelete.setBounds(new java.awt.Rectangle(405,230,60,20));
			btnDataDelete.setMargin(new java.awt.Insets(0, 0, 0, 0));
			btnDataDelete.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String  cmd  = null;
					int[]   rows = tblDataTable.getSelectedRows();

					cmd = "DATA DEL";
					for (int i=rows.length-1; i>=0; --i) {
						cmd += " " + (rows[i]+1);  // +1 --- restrax indexuje od 1
					}

					program.executeCommand(cmd,true);
				}
			});
		}
		return btnDataDelete;
	}

	/*"***************************************************************************************
	* SPINNERS                                                                               *
	*****************************************************************************************/
	private JSpinner getSpnPAL() {
		if (spnPAL == null) {
			SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel();
			spinnerNumberModel.setMaximum(new Integer(4));
			spinnerNumberModel.setMinimum(new Integer(1));
			spinnerNumberModel.setStepSize(new Integer(1));
			spinnerNumberModel.setValue(new Integer(1));
			spnPAL = new JSpinner();
			spnPAL.setToolTipText("<html>Selected polarization analysis loop, if applicable</html>");
			spnPAL.setName("Selected polarization analysis loop");
			spnPAL.setModel(spinnerNumberModel);
			spnPAL.setBounds(new java.awt.Rectangle(475,230,40,20));
		}
		return spnPAL;
	}

	
	
	/*"***************************************************************************************
	* TEXT FIELDS                                                                            *
	*****************************************************************************************/
	private JTextField getTxfDatalistValue() {
		if (txfDatalistValue == null) {
			txfDatalistValue = new JTextField();
			txfDatalistValue.setBounds(new java.awt.Rectangle(10,230,260,20));
			txfDatalistValue.setName("EXCI library \u2192 File name");
			txfDatalistValue.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (tblDataTable.getRowCount() == 0) {
						getDatalist(OPEN_CMD);  
					} else {
						getDatalist(ADD_CMD);
					}
				}
			});
		}
		return txfDatalistValue;
	}

	/*"***************************************************************************************
	* SCROLL PANES                                                                           *
	*****************************************************************************************/
	private JScrollPane getScrDataTable() {
		if (scrDataTable == null) {
			scrDataTable = new JScrollPane();
			scrDataTable.setLocation(new java.awt.Point(10,10));
			scrDataTable.setViewportView(getTblDataTable());
			// "JScrollPane.gerPreferredSize()" se bere implicitn� z vnit�n�ho "JViewport.getPreferredSize()"
            // a ten je nastaven v tabulce "JTable tblDataTable" pomoc� metody "setPreferredScrollableViewportSize"
			// Tento krkolomn� zp�soben je zvolen proto, �e v tabulce se nastavuj� ���ky sloupc�
			// a tak i tam by logicky m�la b�t nastavena ���ka cel� tabulky
			scrDataTable.setSize(scrDataTable.getPreferredSize());  
		}

		return scrDataTable;
	}

	/*"***************************************************************************************
	* TABLES                                                                                 *
	*****************************************************************************************/
	private JTable getTblDataTable() {
		if (tblDataTable == null) {
			dataNameList = new DataNameList();
			tblDataTable = new CTable();
			tblDataTable.setPreferredScrollableViewportSize(new java.awt.Dimension(520,195));
			tblDataTable.setModel(dataNameList);
			tblDataTable.setFocusable(false);
			tblDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);  // kdy� u�, tak m�� velikost pouze u sloupce "Datalist"
			
			TableColumn tableColumn;
			// select
			tableColumn = tblDataTable.getColumnModel().getColumn(0);
			tableColumn.setPreferredWidth(30);
			tableColumn.setResizable(false);
			
			// tag
			tableColumn = tblDataTable.getColumnModel().getColumn(1);
			tableColumn.setPreferredWidth(30);
			tableColumn.setResizable(false);
			
			// MC: Monte-Carlo
			tableColumn = tblDataTable.getColumnModel().getColumn(2);
			tableColumn.setPreferredWidth(30);
			tableColumn.setResizable(false);
			
			// N: number of points
			tableColumn = tblDataTable.getColumnModel().getColumn(3);
			tableColumn.setPreferredWidth(50);
			tableColumn.setResizable(false);

			// pos: position
			tableColumn = tblDataTable.getColumnModel().getColumn(4);
			tableColumn.setPreferredWidth(200);
			tableColumn.setResizable(false);

			// data name
			tableColumn = tblDataTable.getColumnModel().getColumn(5);
			tableColumn.setPreferredWidth(180);
			tableColumn.setResizable(false);
		}
		return tblDataTable;
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  OTHER METHODS                                       //
	//////////////////////////////////////////////////////////////////////////////////////////
	private void getDatalist(String cmd) {
		String s = txfDatalistValue.getText().trim();

		if (s.length() != 0) {
			txfDatalistValue.setText("");  // po p�id�n� polo�ky "Datalist" vyma� zad�vac� pole
			program.executeCommand("DATA " + cmd + " " + s,true);
		} else {
			/*
			 * txfDataDatalistValue je pr�zdn�. Otev�i proto JFileChooser podle toho, jak� tla��tko
			 * bylo zm��knuto
			 */
			GuiFileFilter dataListFileFilter = GuiFileFilter.createDataFileFilter();
			
			JFileChooser fileChooser = new JFileChooser(dataPath);  // otev�i posledn� nav�t�ven� adres��

			// chceme sice p�idat filtr pro DATAFILE (*.dat), ale to pouze jako t�e�ni�ku na dortu
			// --- nej�ast�ji nemaj� soubory s daty ��dnou p��ponu a tak chci, aby se implicitn� nastavil
			// filtr "All files" *.*. 
			fileChooser.addChoosableFileFilter(dataListFileFilter);
			fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
			
			if (cmd == OPEN_CMD) {
				fileChooser.setApproveButtonText("Open");
				fileChooser.setDialogTitle("Open datafile");
			} else if (cmd == ADD_CMD) {
				fileChooser.setApproveButtonText("Add");
				fileChooser.setDialogTitle("Add datafile");
			} else {
				throw new IllegalArgumentException("Programmer's fault --- only OPEN_CMD or ADD_CMD are allowed");
			}
			
			
			int returnVal = fileChooser.showDialog(PanelData.this, null);  // ..., null); --- "ApproveButtonText" je uz nastaven
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String fileName = fileChooser.getSelectedFile().getPath();  // getPath() returns absolute path plus file name
				int np = ((Integer)spnPAL.getValue()).intValue(); // get default PAL		
				program.executeCommand("DATA " + cmd + " \"" + fileName + "\""+" PAL="+np,true);
				dataPath = Utils.getDirectory(fileName);  // zapamatuj si posledni navstiveny adresar, abychom se p�i
				                                          // p��t�m otev�en� dialogu zase ocitli v nem
			}
		}
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 NESTED CLASSES                                       //
	//////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * N�sleduj�c� tabulka je pouze "zrcadlem" tabulky restraxu. Tzn. �e p�i kliknut� nap�.
	 * na sloupe�ek "sel" restraxGUI pouze vy�le p��kaz do restraxu a posl�ze se na�te znovu
	 * cel� obsah tabulky jak ho vyp�e restrax. Je tedy na restraxu, jak v�e obhospoda��
	 * (sloupe�ek "sel" se toti� chov� jako radio button --- pouze jedna polo�ka m��e b�t
	 * aktivn�) 
	 */
	public class DataNameList extends AbstractTableModel {
		private static final long serialVersionUID = 5647434399155877020L;

		private String[]          columnNames = {"sel", "tag", "MC", "N", "position", "data name"};
		private Vector<Boolean>   active      = null;
		private Vector<Boolean>   tagged      = null;
		private Vector<Boolean>   mcdone      = null;
		private Vector<Integer>   npoints     = null;
		private Vector<String>    position    = null;
		private Vector<String>    dataName    = null;


		//////////////////////////////////////////////////////////////////////////////////////
		//                                   CONSTRUCTORS                                   //
		//////////////////////////////////////////////////////////////////////////////////////
		public DataNameList() {
			super();
			
			active   = new Vector<Boolean>();
			tagged   = new Vector<Boolean>();
			mcdone   = new Vector<Boolean>();
			npoints  = new Vector<Integer>();
			position = new Vector<String>();
			dataName = new Vector<String>();
		}


		//////////////////////////////////////////////////////////////////////////////////////
		//                                   OTHER METHODS                                  //
		//////////////////////////////////////////////////////////////////////////////////////
		public int getColumnCount() {
			return columnNames.length;
		}
		
		public int getRowCount() {
			return dataName.size();
		}
		
		public String getColumnName(int col) {
			return columnNames[col];
		}

		public void addRow(boolean active, boolean tagged, boolean mcdone, int npoints, String position, String dataName) {
			this.active.add(active);
			this.tagged.add(tagged);
			this.mcdone.add(mcdone);
			this.npoints.add(npoints);
			this.position.add(position);
			this.dataName.add(dataName);

			// nezapome� d�t p��kaz k obnov� tabulky:			
			int addedRowNum = this.dataName.size()-1; // p�idan� ��dka je um�st�na na konci
			fireTableRowsInserted(addedRowNum, addedRowNum);
		}

		public void clearTable() {
			int lastRow = dataName.size()-1;
			
			if (lastRow != -1) {  // table is not empty
				active.clear();
				tagged.clear();
				mcdone.clear();
				npoints.clear();
				position.clear();
				dataName.clear();
				
				fireTableRowsDeleted(0, lastRow);
			}
		}
		
		public boolean isEmpty() {
			return dataName.isEmpty();
		}
		
		public Object getValueAt(int row, int col) {
			switch (col) {
				case 0:
					return active.elementAt(row);
				case 1:
					return tagged.elementAt(row);
				case 2:
					return mcdone.elementAt(row);
				case 3:
					return npoints.elementAt(row);
				case 4:
					return position.elementAt(row);
				case 5:
					return dataName.elementAt(row);
				default:
					throw new IndexOutOfBoundsException("Table 'DataListTable' has only six columns (col=" + col + ").");
			}
		}
		
		/*
		 * "JTable" vyu��v� tuto metodu k ur�en� standardn�ho editoru, tak�e pole tabulky typu "Boolean"
		 * budou vykresleny jako "check boxes"
		 */
		public Class<?> getColumnClass(int col) {
			return getValueAt(0, col).getClass();
		}
		
		public boolean isCellEditable(int row, int col) {
			//Note that the data/cell address is constant, no matter where the cell appears onscreen.
			if (col > 1) {  // sloupce ("mcdone", "npoints", "position" a "dataName") nen� mo�n� m�nit
				return false;
				// no editting is possible for multianalyzer mode
			} else {         // av�ak sloupce "selected" a "tagged" je mo�n� m�nit/p�ep�nat
				return true;
			}
		}
		
		public void setValueAt(Object value, int row, int col) {
			String cmd;
			
			switch (col) {
				case 0:
					cmd = "DATA " + (row+1);  // restrax indexuje od 1
					break;
				case 1:
					cmd = "DATA TAG " + (row+1);
					break;
				default:
					// Pozn�mka: t�et� a� �est� sloupe�ek tabulky nen� editovateln� a tak "case 2:"--"case 5:" nebude nikdy vol�n
					throw new IndexOutOfBoundsException("Only first two columns of table 'DataListTable' are editable (col=" + col + ").");
			}

			program.executeCommand(cmd,true);
		}
		
		public void printDebugData() {
			System.out.println("Table content:");
			System.out.println("              active  tagged  mcdone  npoints  position  dataName");
			for (int i=0; i < getRowCount(); i++) {
				System.out.print("    row(" + i + "): ");
				System.out.print(active.elementAt(i) + "  ");
				System.out.print(tagged.elementAt(i) + "  ");
				System.out.print(mcdone.elementAt(i) + "  ");
				System.out.print(npoints.elementAt(i) + "  ");
				System.out.print(position.elementAt(i) + "  ");
				System.out.print(dataName.elementAt(i) + "  ");
				System.out.println();
			}
		}
	}
}