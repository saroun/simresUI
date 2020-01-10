package cz.jstools.classes.editors.propertiesView;


/**
 * This interface is used to ensure, that programmer will change only
 * visibility and editability of property item while property list
 * is initialized.
 * 
 *
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:51 $</dt></dl>
 */
public interface PropertyAttributes {
	public void setEditable(boolean isEditable);
	public void setVisible(boolean isVisible);
}