package cz.saroun.classes;



/**
 * Type definition for selection lists.
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.4 $</dt>
 *               <dt>$Date: 2014/05/05 18:41:18 $</dt></dl>
 */

public class SelectDef extends FieldDef {
    public SelectDef(String id) {
    	super(id,1,FieldType.SELECT);
      } 

    @Override
    public SelectDef clone() {
    	SelectDef fd = new SelectDef(id);
    	fd.assign(this);
    	return fd;
    }
}
