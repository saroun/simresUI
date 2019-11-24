package cz.saroun.classes;


/** 
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.4 $</dt>
 *               <dt>$Date: 2014/05/05 18:41:18 $</dt></dl>
 */

public class RangeDef extends FieldDef {
	
    public RangeDef(String id, int size) {
      super(id,size,FieldType.RANGE);
    }
    public RangeDef(String id) {
    	super(id,1,FieldType.RANGE);
      }  
    @Override
    public RangeDef clone()  {
    	RangeDef fd = new RangeDef(id,size);
    	fd.assign(this);
    	return fd;
    }
}

