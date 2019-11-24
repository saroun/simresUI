package cz.saroun.classes;

import cz.saroun.classes.definitions.FileAccess;



/**
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.6 $</dt>
 *               <dt>$Date: 2014/09/05 13:26:10 $</dt></dl>
 */

public class StringDef extends FieldDef {
	public FileAccess  fileAccess;
	public String  filter;
    public StringDef(String id) {
    	super(id,1,FieldType.STRING);
    	fileAccess = FileAccess.NONE; // information for GUI: provide file chooser
    	filter=null;
      } 
    
    @Override
    public StringDef clone() {
    	StringDef fd = new StringDef(id);
    	fd.assign(this);
    	return fd;
    }
}
