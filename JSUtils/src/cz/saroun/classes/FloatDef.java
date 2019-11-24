package cz.saroun.classes;

import java.util.zip.DataFormatException;

import cz.saroun.classes.definitions.Utils;


/**
 * Defines float variable with double precision. 
 * @author   Jan Saroun
 * @version  <dl><dt>$Revision: 1.4 $</dt>
 *               <dt>$Date: 2014/05/05 18:41:18 $</dt></dl>
 */

public class FloatDef extends FieldDef {	
    public String units;
    	
    public FloatDef(String id, String units, int size) {
      super(id,size,FieldType.FLOAT);
      this.units=units;
    }    

    public FloatDef(String id, String units, int size, FieldType tid) throws DataFormatException {
        super(id,size,tid);
        if (tid!=FieldType.FLOAT && tid!=FieldType.SINGLE) {
        	throw new DataFormatException("Wrong FloatDef type");
        }
        this.units=units;
      }    
    
    public FloatDef(String id, String units) {
    	super(id,1,FieldType.FLOAT);
    	this.units=units;
    }  
    public String getUnits() {
    	if (! units.equals("")) {
			return Utils.math2Html(units);
		} else return "";
    }
    
    @Override
    public FloatDef clone() {
    	FloatDef fd = new FloatDef(id,units,size);
    	fd.assign(this);
    	return fd;
    }
}
