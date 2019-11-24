package cz.saroun.classes;

import java.util.zip.DataFormatException;


/**
 * Defines float variable with double precision. 
 * @author   Jan Saroun
 * @version  <dl><dt>$Revision: 1.2 $</dt>
 *               <dt>$Date: 2014/05/05 18:41:18 $</dt></dl>
 */

public class TableDef extends FieldDef {	
	public final int rows,cols;
    
    	
    public TableDef(String id, int rows, int cols) {
      super(id,rows*cols,FieldType.TABLE);
      this.rows=rows;
      this.cols=cols;
    }    

    public TableDef(String id, int rows, int cols, FieldType tid) throws DataFormatException {
        super(id,rows*cols,tid);
        if (tid!=FieldType.TABLE) {
        	throw new DataFormatException("Wrong TableDef type");
        }
        this.rows=rows;
        this.cols=cols;
      }  
    
    @Override
    public TableDef clone()  {
    	TableDef fd = new TableDef(id,rows,cols);
    	fd.assign(this);
    	return fd;
    }
    
}
