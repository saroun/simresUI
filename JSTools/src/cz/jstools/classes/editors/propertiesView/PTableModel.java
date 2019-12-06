package cz.jstools.classes.editors.propertiesView;

import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;


/**
 * Table model that handles special needs of property table.
 *
 *
 * @author   Svoboda Jiøí, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:52 $</dt></dl>
 */
public class PTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -9074340171480380017L;

	private String[]        colNames   = {"Property", "Value", "Units"};
	private Vector<PItem>   properties = null;
	private int[]           lut        = null;
	
	/** toto pole drzi delku lut tabulky, spocitanou pro aktualne viditelne
	 *  polozky. Lut ma totiz stale stejnou delku danou maximalnim poctem
	 *  viditelnych polozek, dopredu totiz pocet aktualne viditellnych polozek
	 *  neznam. */
	private int             lutLen       = -1;
	private Stack<Integer>  sectionIndex = null;
	private int             level        = 0;
	private boolean showUnits = false;
	
	
	//////////////////////////////////////////////////////////////////////////////////////
	//                                   CONSTRUCTORS                                   //
	//////////////////////////////////////////////////////////////////////////////////////
	public PTableModel(boolean showUnits) {
		super();
		this.showUnits = showUnits;
		sectionIndex       = new Stack<Integer>();
		properties = new Vector<PItem>();
	}
		
	//////////////////////////////////////////////////////////////////////////////////////
	//                                   OTHER METHODS                                  //
	//////////////////////////////////////////////////////////////////////////////////////
	public int getColumnCount() {
		if (showUnits) {
			return 3;
		} else return 2;
	}
	
	public int getRowCount() {
		return lutLen;
	}
	
	public String getColumnName(int col) {
		return colNames[col];
	}

	
	/**
	 * In the table there could be null pid, if the user do not require to get
	 * value of property (it is aaccessible via pid). But if the pid is non-null,
	 * be sure, that is unique
	 * 
	 * @param pid  id of the property being added
	 */
	private void checkPid(String pid) {
		if (pid == null) {
			return;
		}
			
		if (pid.trim().length() == 0) {
			throw new IllegalStateException("Pid in property table must not be empty.");
		}
        String s=null;
		for (int i=0; i<properties.size(); ++i) {
			s=properties.elementAt(i).getPid();
			if ((s != null) &&
			    (s.equals(pid))) {
				throw new IllegalStateException("Pid '" + pid + "'must be unique in property table.");
			}
		}
	}
	
	private int getIndexOfPid(String pid) {
		if (pid == null) {
			throw new NullPointerException("Pid must not be null.");
		}

		for (int i=0; i<properties.size(); ++i) {
			String pidAt = properties.elementAt(i).getPid();
			if (pidAt != null) {
				if (pidAt.equals(pid)) {
					return i;
				}
			}
		}

		throw new NoSuchElementException("Pid '" + pid + "' is unknown.");
	}
	
	public void addProperty(PropertyItem propertyItem) {
		/*
		 * Create PItem firstly, because PropertyItem does not contain
		 * any getter. Getters are defined as lately as PItem is created.
		 * Pid, etc. can be obtained afterwards
		 */
		PItem pItem = new PItem(level,propertyItem);		
		checkPid(pItem.getPid());
		properties.add(pItem);
		if (pItem.isSection()) {
			sectionIndex.push(properties.size()-1);
			++level;
		}
	}

	
	public void endSection() {
		int end   = properties.size()-1;
		int begin = sectionIndex.pop();
		--level;

	    // (end-begin+1) - 1 --- do not count in a header of section
		properties.elementAt(begin).setItemsInside(end-begin);
	}

	
	public Object getValueOfId(String pid) {
		int index = getIndexOfPid(pid);
		return properties.elementAt(index).getValue();
	}
	
	public PItem getPItem(String pid) {
		int index = getIndexOfPid(pid);
		return properties.elementAt(index);
	}
	
	public PropertyAttributes getPropertyAttributesOfId(String pid) {
		int index = getIndexOfPid(pid);
		return (PropertyAttributes)properties.elementAt(index);  // offer only setVisible and setEditable methods
	}

	
	public void setValueOfId(String pid, Object value) {
		int index = getIndexOfPid(pid);
		properties.elementAt(index).setValue(value);
	}

	
	public void clearValueChanged(String pid) {
		int index = getIndexOfPid(pid);
		if ((index>=0) && (index<properties.size())) {
			 properties.elementAt(index).clearValueChanged();
		}
	}

	public void clearValueChanges() {
		for (int i=0; i<properties.size(); ++i) {
			 properties.elementAt(i).clearValueChanged();
		}
	}
	
	/** @param pid identification string
	 * @return "ValueChanged" attribute for the property with given <b>pid</b>.
	 */
	public boolean isPropertyChanged(String pid) {
		int index = getIndexOfPid(pid);
		return properties.elementAt(index).isValueChanged();
	}
	
	/** @param pid identification string
	 * @return "ValueChanged" attribute for the property with given <b>pid</b>.
	 */
	public boolean isPropertySection(String pid) {
		int index = getIndexOfPid(pid);
		return properties.elementAt(index).isSection;
	}
	
	/** Return a string array with the PIDs of all properties that have changed.
	 */
	public String[] changedPropertiesPid() {
		StringBuffer pids = new StringBuffer(256);
		String pid="";
		int L=0;
		for (int i=0; i<properties.size(); ++i) {
			 if (properties.elementAt(i).isValueChanged()) {
				 pid=properties.elementAt(i).pid;
				 if (L==0) {
					 pids.append(pid);
				 } else {
					 pids.append(":"+pid);
				 }	
				 L++;
			 }
		};
		if (pids.length() > 0) {
			return pids.toString().split(":");
		} else {
			return new String[0];
		}
	}

	/** Return a string array with the PIDs of all properties.
	 */
	public String[] propertiesPid() {
		StringBuffer pids = new StringBuffer(256);
		String pid="";
		int L=0;
		for (int i=0; i<properties.size(); ++i) {
			pid=properties.elementAt(i).pid;
			if (L==0) {
				pids.append(pid);
			} else {
			 pids.append(":"+pid);
			}	
		 L++;
		};
		if (pids.length() > 0) {
			return pids.toString().split(":");
		} else {
			return new String[0];
		}
	}

	/**
	 * This method must be called when all properties are added to the table.
	 * It prepares active view of table, where some sections can be collapsed.
	 */
	public void initializeProperties() {
		lut = new int[properties.size()];
		updateLut();
	}
	
	public void updateProperties() {
		updateLut();
	}
	
	private void updateLut() {
		int len = 0;
		for (int i=0; i<properties.size(); ++i) {
			
			if (properties.elementAt(i).isVisible()) {
				lut[len++] = i;
			}
			if (properties.elementAt(i).isSection() &&
			  ((properties.elementAt(i).isVisible() == false) ||
			    properties.elementAt(i).isCollapsed())) {
				i += properties.elementAt(i).getItemsInside();
			}
		}
		
		lutLen = len;
		
		fireTableDataChanged();
	}
	
	public PItem getPropertyItemAt(int row) {
		return properties.elementAt(lut[row]);
	}

	public Object getValueAt(int row, int col) {
		if (col == 0) {
			/*
			 * when property is not section, value of isCollpased()
			 * is ignored by renderer, so it can be arbitraty.
			 */
			return properties.elementAt(lut[row]).isCollapsed();
		} else if (col == 1) {
			return properties.elementAt(lut[row]).getValue();
		} else if ((col == 2) && showUnits) {
			String unit= properties.elementAt(lut[row]).getUnit();
			if (unit == null) {
				return new String("");
			} else return properties.elementAt(lut[row]).getUnit();
		} else {
			throw new IndexOutOfBoundsException("Property table has only "+getColumnCount()+" columns (col=" + col + ").");
		}
	}
	
	public boolean isCellEditable(int row, int col) {
		return ( ((col == 1) && (properties.elementAt(lut[row]).isEditable())) ||
		         ((col == 0) && (properties.elementAt(lut[row]).isSection() == true)) );
	}
	
	public void setValueAt(Object value, int row, int col) {
		if (col == 0) {
			boolean collapsed = ((Boolean)value).booleanValue();
			properties.elementAt(lut[row]).setCollapsed(collapsed);
			updateLut();
		}
		else if (col == 1) {
			PItem pItem = properties.elementAt(lut[row]);			
			pItem.setValue(value);
			if (pItem.isValueChanged()) {
				fireTableCellUpdated(lut[row], 0);  // zmen barvu jmena hned
				properties.elementAt(lut[row]).fireValueChanged();					
			}
	//	} else if (col==2 && showUnits) {
	//		// do nothing
		} else {			
			throw new IndexOutOfBoundsException("Only first two columns of table 'DataListTable' are editable (col=" + col + ").");
		}
	}
}