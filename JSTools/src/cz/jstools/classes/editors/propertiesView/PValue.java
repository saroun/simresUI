package cz.jstools.classes.editors.propertiesView;

import java.text.ParseException;


/**
 * This abstract class is predecessot of all editable values
 * in property editor.
 *
 *
 * @author   Svoboda Jiøí, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:52 $</dt></dl>
 */
 abstract public class PValue{
	abstract public String   toString();
	abstract public String   toHtmlString();
	abstract public void     assignValue(String s) throws ParseException;
	abstract public PValue   duplicate();
	abstract public boolean  equals(Object value);
}
