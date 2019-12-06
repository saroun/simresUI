package cz.jstools.classes.editors.propertiesView;


/**
 * Package private internal property item used in property list in PTableModel. Defines all
 * atribute of property such a name, value, unique identifier,
 * or specifies that item is section, that holds another properties,
 * that are stored in list after this item.
 *
 *
 * @author   Svoboda Jiøí, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:52 $</dt></dl>
 */
class PItem extends PropertyItem{
	/** holds item inside the section. Current section item
	 * is not included, but nested properties including section
	 * item are all included. This filed server for generating
	 * a look up table that hold only visible items that are
	 * uncollapsed in PTableModel. */
	private int      itemsInside       = -1;
	
	/** the level of nesting. */
	private int      level             = 0;
	
	/** tells if the user has changed the initial value. If the user enters
	 *  exactly the same value this fieled is true, though */
	private boolean  isValueChanged    = false;

	
	// ****************************************
	//   Constructor
	// ****************************************
	/**
	 * This constructor serves for full definition of a property item.
	 * The full constructor is used here and in PTableModel (low level).
	 * Specialized API method in PropertiesView calls this full constructors
	 * and yield mostly used methods for added to property table that has
	 * less arguments.
	 *  
	 * @param level         level of nesting
	 * @param propertyItem  property item as defined by user
	 */
	
	public PItem(int level, PropertyItem propertyItem) {
		//copy all atributes
		super(propertyItem);

		this.level = level;
	}


	// ****************************************
	//     access methods
	// ****************************************
	public int getLevel() {
		return level;
	}
	
	public int getItemsInside() {
		return itemsInside;
	}

	public void setItemsInside(int itemsInside) {
		this.itemsInside = itemsInside;
	}
	
	public Object getValue() {
		return value;
	}

	/**
	 * Overrides the setValue method form PropertyItem in order to
	 * set mark that value has been changed
	 */
	public void setValue(Object value) {
		if ((isValueChanged == false) && (this.value != null)) {  // jakmile se jednou zmenila uz ji tak nech, hodnota muze byt na zacateku nedefinovana, to ignoruj
			isValueChanged = !(this.value).equals(value);
		}
		this.value = value;
	}
	
	public void clearValueChanged() {
		isValueChanged = false;
	}

	public boolean isValueChanged() {
		return isValueChanged;
	}
	
	public boolean isSection() {
		return isSection;
	}

	public String getName() {
		return name;
	}

	public boolean hasHint() {
		return (hintText != null);
	}

	public String getHint() {
		return hintText;
	}

	public boolean isCollapsed() {
		return isCollapsed;
	}

	public boolean isList() {
		return (list != null);
	}
	
	public Object[] getList() {
		return list;
	}
	
	public String getPid() {
		return pid;
	}
	
	public boolean isBrowsable() {
		return (objectToBrowse != null);
	}
	
	public Browsable getObjectToBrowse() {
		return objectToBrowse;
	}

	public boolean isEditable() {
		return isEditable;
	}
	
	public boolean hasListeners() {
		return  ((listeners != null) && (listeners.length != 0));
	}
	
	public ValueChangeListener[] getListeners() {
		return listeners;
	}
	
	public void fireValueChanged() {
		if (hasListeners()) {
			for (int i=0; i<listeners.length; ++i) {
				listeners[i].valueChanged(pid);
			}
		}
	}
	
	public boolean isDirectInput() {
		return isDirectInput;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public boolean hasValueChecker() {
		return (valueChecker != null);
	}

	public ValueChecker getValueChecker() {
		return valueChecker;
	}
}