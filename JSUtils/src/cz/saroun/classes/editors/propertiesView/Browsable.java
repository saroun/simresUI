package cz.saroun.classes.editors.propertiesView;



/**
 * This interface is implemented by object that is shown when
 * user clicks on browse button ('...') in property table.
 *
 *
 * @author   Svoboda Jiøí, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:52 $</dt></dl>
 */
public interface Browsable {
	/** vola se po kliknuti na tlacitko '...'. Navratova hodnota se zobrazi v policku, pokud
	 * je null,  zustava nezmenena
	 * 
	 * @return objekt ktery je vybran po kliknuti na '...'
	 */
	public Object browse(Object content);
	// public Object browse(Object content, Point origin);
}
