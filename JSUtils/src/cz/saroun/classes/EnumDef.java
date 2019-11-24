package cz.saroun.classes;



/**
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.4 $</dt>
 *               <dt>$Date: 2014/05/05 18:41:18 $</dt></dl>
 */

public class EnumDef extends FieldDef {	
    public EnumType enu;
	public EnumDef(String id, EnumType enu) {
    	super(id,1,FieldType.ENUM);    	
    	this.enu = enu;
    }
	
    @Override
    public EnumDef clone() {
    	EnumDef fd = new EnumDef(id,enu);
    	fd.assign(this);
    	return fd;
    }
}
