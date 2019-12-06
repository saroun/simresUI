package cz.jstools.classes.editors.propertiesView;


/**
 * This listener is called when the value of PValue is changed.
 *
 *
 * @author   Svoboda Jiøí, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:51 $</dt></dl>
 */
public interface ValueChangeListener {
	/**
	 * This method is called after value change of PValue in Property.
	 * @param sourcePid identifier of property that has been changed.
	 */
	public void valueChanged(String sourcePid);
}
