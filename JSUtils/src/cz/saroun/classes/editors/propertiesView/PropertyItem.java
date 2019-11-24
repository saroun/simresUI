package cz.saroun.classes.editors.propertiesView;


/**
 * This class is used for user definition of property item.
 * Because there are many combinations of property attribute,
 * API methods with many argument combinations are not smart enough.
 * That is why this object containing only setters method 
 * is used and only one API method 
 * addProperty(PropertyItem p) is provided.
 * 
 * <br><b>Warning:<b><br>
 * If the property is list, value must contaion reference to one of
 * them. So if String[] list = {"A", "B", "C"}, value should not be
 * for example "A" but list[0].
 *
 *
 * @author   Svoboda Jiøí, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:52 $</dt></dl>
 */
public class PropertyItem  implements PropertyAttributes {
	// ****************************************
	//      property
	// ****************************************
	protected String   name              = null;
	
	/** unique property identifier, method that handles PropertyItem
	 *  list must ensure, that pid is really unique. */
	protected String   pid               = null;
	
	/** contain a list of values which are shown in JComboBox. */
	protected Object[] list              = null;
	
	/** contain a list of listeners that are called when value has changed. */
	protected ValueChangeListener[] listeners = null;
	
	/** contain a browsable object that is call when user clicks at
	 *  '...' button. */
	protected Browsable  objectToBrowse     = null;
	
	/** tells if the value in table is editable. Some values can be
	 *  computed from other values and such value can be noneditable. */
	protected boolean  isEditable        = true;

	/** tells if the value in table is visible. Some values depends on state
	 *  of other value (e.g. Shape (Circular|Rectangular) -> fields:
	 *  diameter|width,height). So there should be chance make some values
	 *  invisible */
	protected boolean  isVisible         = true;
	
	/** value of property. When property contain a list of values,
	 *  this hold one of them. */
	protected Object   value             = null;
	
	/** object that could check if the value is correct */
	protected ValueChecker  valueChecker = null;
	
	/** a text that is shown in form ToolTipHelp when user positions
	 *  a cursor over value field. */
	protected String   hintText          = null;
	
	/** unit string to be shown in the last column */
	protected String   unit          = null;
	
	/** tells if the user can directly change the  browsable value */
	protected boolean  isDirectInput     = true;


	
	// ****************************************
	//      sections
	// ****************************************
	/** tells if the item is section. */
	protected boolean  isSection         = false;
	
	/** tells if the section is collapsed */
	protected boolean  isCollapsed       = false;
	
	
	// ****************************************
	//   Constructor
	// ****************************************
	/**
	 * Default constructor, that specifies unique property identifier.
	 * Other attributes can be set via setters.
	 */
	public PropertyItem(String pid) {
		this.pid = pid;
	}
	
	/**
	 * Default constructor. Pid can be null in properties, where
	 * user do not require to get or sets its value. Typically in sections
	 * that holds no value. See comment for setSection() method.
	 */
	public PropertyItem() {}

	
	/**
	 * Serves for intern usage in PItem which is descendant of PropertyItem and
	 * holds another fields like level and itemsInside.
	 * So there should be method to copy all parameters from PropertyItem. And this object
	 * should not offer getters, only setter, so this copying should be intern and should be
	 * done on the PropertyItem size, because if some fileds were added one would forget
	 * assign those in PItem.
	 * 
	 * @param from PropertyItem object from that fiels are copied.
	 */
	protected PropertyItem(PropertyItem from) {
		this.name           = from.name;
		this.pid            = from.pid;
		this.list           = from.list;
		this.listeners      = from.listeners;
		this.objectToBrowse = from.objectToBrowse;
		this.isEditable     = from.isEditable;
		this.isVisible      = from.isVisible;
		this.value          = from.value;
		this.valueChecker   = from.valueChecker;
		this.hintText       = from.hintText;
		this.isDirectInput  = from.isDirectInput;
		this.isSection      = from.isSection;
		this.isCollapsed    = from.isCollapsed;
		this.unit    = from.unit;
	}

	
	// ****************************************
	//   Access methods
	// ****************************************
	public void setObjectToBrowse(Browsable objectToBrowse) {
		this.objectToBrowse = objectToBrowse;
	}

	public void setHintText(String hintText) {
		this.hintText = hintText;
	}

	public void setCollapsed(boolean isCollapsed) {
		this.isCollapsed = isCollapsed;
	}

	public void setDirectInput(boolean isDirectInput) {
		this.isDirectInput = isDirectInput;
	}

	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}

	/**
	 * Specify if property is a section or not. When pid is null,
	 * it is supposed, that section value is not set too and so
	 * it is set to non editable. This behaviour could be (for
	 * uknown reasons) overwriten by subsequent call of setEditable
	 * method
	 */
	public void setSection(boolean isSection) {
		this.isSection = isSection;
		this.isEditable = false;
	/*	if (isSection && (pid == null)) {
			this.isEditable = false;
		} 
	*/
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	public void setList(Object[] list) {
		this.list = list;
	}
	
	public void addListItem(Object listItem) {
		if (list == null) {
			list = new Object[]{listItem};
		} else {
			if (list[0].getClass() != listItem.getClass()) {
				throw new ClassCastException("Attemp to add object of type '" + listItem.getClass()
				                           + "' to the list that hold another type of objects '" + list[0].getClass() + "'!");
			}
			Object[] tmpList = new Object[list.length+1];
			System.arraycopy(list, 0, tmpList, 0, list.length);
			tmpList[tmpList.length-1] = listItem;
			list = tmpList;
		}
	}

	public void setListeners(ValueChangeListener[] listeners) {
		this.listeners = listeners;
	}

	public void addListener(ValueChangeListener listener) {
		if (listeners == null) {
			listeners = new ValueChangeListener[]{listener};
		} else {
			ValueChangeListener[] tmpListeners = new ValueChangeListener[listeners.length+1];
			System.arraycopy(listeners, 0, tmpListeners, 0, listeners.length);
			tmpListeners[tmpListeners.length-1] = listener;
			this.listeners = tmpListeners;
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
	public void setValueChecker(ValueChecker valueChecker) {
		this.valueChecker = valueChecker;
	}

	public String getPid() {
		return pid;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
}