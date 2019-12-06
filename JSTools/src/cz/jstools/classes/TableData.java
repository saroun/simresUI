package cz.jstools.classes;

import cz.jstools.classes.definitions.Constants;
import cz.jstools.classes.definitions.Utils;


public class TableData extends FieldData {
	protected double[][] tableData;
	protected int nrows=0;
	protected int ncols=0;
	
	public TableData(TableDef type, int rows) {
		super(type);		
		ncols=type.cols;
		nrows=Math.min(type.rows,rows);
		tableData=new double[nrows][ncols];		
		for (int i=0;i<nrows;i++) {
			for (int j=0;j<ncols;j++) {
				tableData[i][j] = 0.0;
			}
		}
	}
	
	/**
	 * Create empty data with only 1 row
	 * @param type
	 */
	public TableData(TableDef type) {
		super(type);		
		ncols=type.cols;
		nrows=1;
		tableData=new double[nrows][ncols];		
		for (int i=0;i<nrows;i++) {
			for (int j=0;j<ncols;j++) {
				tableData[i][j] = 0.0;
			}
		}
	}
	public boolean isTable() {
    	return true;
    }
	
    public FieldData clone() {
    	TableData fd = new TableData((TableDef) this.type,this.nrows);
    	fd.assign(this);
    	return fd;
    }
        
    public double[] getRow(int irow) {
    	int nr=((TableDef)type).cols;
    	double[] row=new double[nr];
    	for (int i=0;i<nr;i++) {
    		row[i]=tableData[irow][i];
    	}
    	return row;
    }
    
	/**
	 * Copy data from source to this component.
	 * this.assign(source) is equivalent to this=source.clone()
	 */
	public void assignData(FieldData source) {
		if (source instanceof TableData) {
			assignDataT((TableData) source);
		}
    }
	
	/**
	 * Assign data from FloatData to this FloatData object.
	 * Does not change ID, name etc, but can change array dimension
	 */
	protected void assignDataT(TableData source) {			
		tableData=new double[source.nrows][source.ncols];
		this.nrows=source.nrows;
		this.ncols=source.ncols;
		for (int i=0;i<nrows;i++) {
			for (int j=0;j<ncols;j++) {
				tableData[i][j] = source.tableData[i][j];
			}
		}
    }	
	
	/**
     * Set table values, rows as space delimited set of numbers
     */
    public void setData(String value) {
    	String[] rows=value.split("[\n]+");
    	if (rows.length>0) {
    		this.nrows=rows.length;
    		this.ncols=((TableDef)type).cols;
    		double[][] tableData=new double[nrows][ncols];
    		for (int i=0;i<nrows;i++) {
    			String[] ss=rows[i].split("[ ,]+");    			
        		if (ss.length<ncols) {
        			throw new NumberFormatException("Not enough elements for table row "+type.id+"("+nrows+")");
        		}
        		for (int j=0;j<ss.length;j++) {
    	        	tableData[i][j] = Double.parseDouble(ss[j]);
            	}
    		}
    	}    	
    }
    
    public void setData(String value, int row, int col) {
    	if (row<0 || row>=nrows) { 
    		throw new NumberFormatException("Row index out of bounds for "+type.id+"("+row+")");
    	}
    	if (col<0 || row>=ncols) { 
    		throw new NumberFormatException("Column index out of bounds for "+type.id+"("+col+")");
    	}
    	tableData[row][col] = Double.parseDouble(value);    	
    }
    
    /**
     * Set single table row
     * @param Value in string representation
     * @param index vector element
     */
    public void setData(String value, int index) {
    	if (index<0 || index>=nrows) { 
    		throw new NumberFormatException("Row index out of bounds for "+type.id+"("+index+")");
    	}
    	String[] ss=value.split("[ ,]+");    			
		if (ss.length<ncols) {
			throw new NumberFormatException(
					"Not enough elements for table row "+type.id+"("+nrows+")"+
					"value=("+value+")");
		}
		for (int j=0;j<ss.length;j++) {
        	tableData[index][j] = Double.parseDouble(ss[j]);
    	}
    }
    
    
    /**
     * Returns numerical representation of the field (e.g. as a Double object).
     * Values of enumerated types are represented as integers.
     * @param index
     * @return
     */
    public Object getValue() {
    	Object a=null;
    	a = new Double[nrows][ncols];
    	for (int i=0;i<nrows;i++) {
    		for (int j=0;j<ncols;j++) ((Double[][])a)[i][j]=tableData[i][j];    			
		}    	    	
    	return a;
    }
    
    protected double[][] getValueArray() {
    	return tableData;
    }
    
    /**
     * For arrays: returns numerical representation of the array item.
     * If index out of range or type != (int|double), return null.
     * If the variable is not an array, call getValue();
     * @param index
     * @return
     */
    public Double[] getValue(int index) {
    	Double[] a=null;
    	if (index>=0 && index < nrows) {
    		a = new Double[ncols];
    		for (int j=0;j<ncols;j++) {
    			a[j]=tableData[index][j];    			
    		}
    	}    	
    	return a;
    }    
    
    /** 
     * @return String representation of the field value.
     * Vectors are represented as space delimited row of numbers.
     */
    public String valueToString() {
    	return valueToString(" ");    	
    }
    
    public String valueToString(String del) {
    	String s="";
    	for (int i=0;i<nrows;i++) {
    		s="";
    		for (int j=0;j<ncols;j++) {
    			s=s+Utils.d2s(tableData[i][j]);
    			if (j<ncols-1) s=s+del;
    		}
    		if (i<nrows-1) s=s+"\n";
		}
    	return s;
    }
    
    
    public String valueToString(int index) {
    	String s="";
    	if (index>=0 && index < nrows) {
    		for (int j=0;j<ncols;j++) {
    			s=s+Utils.d2s(tableData[index][j]);
    			if (j<ncols-1) s=s+" ";
    		}
    		return s;
    	}
    	else return "out of range";
    } 
    
    /** 
	 * Return XML string representing the value.
	 * To be directly used in XML export
	 * 
	 */
	public String getXmlValue(String tabs) {
		String output;
		String selFormatStart="%s<%s name=\"%s\" rows=\"%d\">\n";
		String selFormatItem="%s<ITEM>%s</ITEM>\n";
		String selFormatEnd="%s</%s>\n";
		output=String.format(Constants.FIX_LOCALE,selFormatStart,tabs,id,type.name,nrows);
		for (int i=0;i<nrows;i++) {
			output+=String.format(Constants.FIX_LOCALE,selFormatItem,tabs+"\t",valueToString(i));
		}
		output+=String.format(Constants.FIX_LOCALE,selFormatEnd,tabs,id);
		return output;
	}

	public int getNrows() {
		return nrows;
	}
    
}
