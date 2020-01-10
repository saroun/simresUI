package cz.jstools.classes.editors.propertiesView;


/**
 * This interface defines method that check whether newly assigned
 * value is correct and so can be accepted.
 *
 *
 * @author   Svoboda Jan Saroun, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:53 $</dt></dl>
 */
public interface ValueChecker {
	public boolean checkValue(Object value);
}
