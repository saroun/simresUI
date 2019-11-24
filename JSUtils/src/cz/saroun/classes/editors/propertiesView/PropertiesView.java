package cz.saroun.classes.editors.propertiesView;

import javax.swing.JScrollPane;


/**
 * Zobrazuje PTable zabalenou do JScrollPane a nabizi API funkci
 * pro vytvareni properties polozek.
 *
 *
 * @author   Svoboda Jiøí, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:53 $</dt></dl>
 */
public class PropertiesView extends JScrollPane {
	private static final long    serialVersionUID = -3963989537144037444L;
	private final boolean showUnits;

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private PTable  tblDataTable             = null;
	private APIInterface apiInterface        = null;
	///////////////////////////////////////////////////////


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public PropertiesView(boolean showUnits) {
		super();
		this.showUnits=showUnits;
		initialize();
		apiInterface = new APIInterface();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                GUI INITIALIZATION                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	private void initialize() {
		this.setViewportView(getTable());
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     GUI BEANS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	/*"***************************************************************************************
	* TABLES                                                                                 *
	*****************************************************************************************/
	public PTable getTable() {
		if (tblDataTable == null) {
			tblDataTable = new PTable(showUnits);
		}
		return tblDataTable;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    OTHER METHOD                                      //
	//////////////////////////////////////////////////////////////////////////////////////////
	public APIInterface getAPIInterface() {
		return apiInterface;
	}
	
	// ****************************************
	//     API interface
	// ****************************************
	public class APIInterface {
		/**
		 * Add  property with specified attributes in PropertyItem
		 * If PropertyItem.setSection(true) then starts a new section
		 * 
		 * @param propertyItem  attributes defining a property item
		 */
		public void addProperty(PropertyItem propertyItem) {
			tblDataTable.getModel().addProperty(propertyItem);
		}		

		/**
		 * End last section
		 */
		public void endSection() {
			tblDataTable.getModel().endSection();
		}
		
		/**
		 * When all property items are added, call this method that initializes
		 * a property list
		 */
		public void initializeProperties() {
			tblDataTable.getModel().initializeProperties();
		}
		
		/**
		 * Returns a value of property with given pid.
		 * 
		 * @param pid  unique identifier of the requsted property
		 * @return value of property with given pid
		 */
		public Object getValueOfId(String pid) {
			return tblDataTable.getModel().getValueOfId(pid);
		}		
		
		public void setListForPid(String pid,Object[] list) {
			tblDataTable.getModel().getPItem(pid).setList(list);
		}
		
		/**
		 * Returns a PropertyAttributes with given pid. User can change only
		 * isVisible, or isEditable.
		 * 
		 * @param pid  unique identifier of the requsted property
		 * @return value of property with given pid
		 */
		public PropertyAttributes getPropertyAttributesOfId(String pid) {
			return tblDataTable.getModel().getPropertyAttributesOfId(pid);
		}

		
		/**
		 * Sets a value of property with given pid. After a set of
		 * setValueOfId commands updateProperties() method should
		 * be called to show changes on screen.
		 * 
		 * @param pid    unique identifier of the requsted property
		 * @param value  value for property with given pid
		 */
		public void setValueOfId(String pid, Object value) {
			tblDataTable.getModel().setValueOfId(pid, value);
		}

		/**
		 * Clears 'valueChanged' flag of given property
		 */
		public void clearValueChanged(String pid) {
			tblDataTable.getModel().clearValueChanged(pid);
		}

		/**
		 * Clears 'valueChanged' flag of all values
		 */
		public void clearValueChanges() {
			tblDataTable.getModel().clearValueChanges();
		}
		
		/**
		 * Clears 'valueChanged' flag of all values
		 */
		public boolean isPropertySection(String pid) {
			return tblDataTable.getModel().isPropertySection(pid);
		}
		
		/**
		 * @see PTableModel.getModel().changedPropertiesPid();
		 */
		public String[] changedPropertiesPid() {
			return tblDataTable.getModel().changedPropertiesPid();
		}
		
		/**
		 * @see PTableModel.getModel().propertiesPid();
		 */
		public String[] propertiesPid() {
			return tblDataTable.getModel().propertiesPid();
		}
		
		/**
		 * Must be called after a set of getPropertyItemOfId()
		 * calls or setValueOfId() calls if some attribute of property
		 * (e.q. visibility) or its value has changed.
		 */
		public void updateProperties() {
			tblDataTable.getModel().updateProperties();
		}
	}
}