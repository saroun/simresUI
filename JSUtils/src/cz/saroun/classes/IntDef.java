package cz.saroun.classes;




/** 
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.4 $</dt>
 *               <dt>$Date: 2014/05/05 18:41:17 $</dt></dl>
 */

public class IntDef extends FieldDef {	
    public IntDef(String id, int size) {
      super(id,size,FieldType.INT);
    }
    public IntDef(String id) {
    	super(id,1,FieldType.INT);
    }    
    
    @Override
    public IntDef clone() {
    	IntDef fd = new IntDef(id,size);
    	fd.assign(this);
    	return fd;
    }
    
}
