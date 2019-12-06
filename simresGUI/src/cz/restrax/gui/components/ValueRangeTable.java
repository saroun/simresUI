package cz.restrax.gui.components;

import java.util.zip.DataFormatException;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import cz.jstools.classes.FieldDef;
import cz.jstools.classes.RangeData;
import cz.jstools.classes.ValueRange;
import cz.jstools.classes.definitions.Utils;
import cz.jstools.classes.editors.propertiesView.PropertyValueEditor;
import cz.jstools.classes.editors.propertiesView.VString;
import cz.jstools.classes.editors.propertiesView.ValueChecker;

public class ValueRangeTable extends JTable {
	private static final long serialVersionUID = 1L;
	private int colTypes=0;
	private int maxRows=0;
	private RangeData data = null;
	
public ValueRangeTable (int maxRows, int colTypes, RangeData data) {
	  super(new DefaultTableModel(),
			new DefaultTableColumnModel(),
			new DefaultListSelectionModel());
	  this.maxRows=maxRows;
	  setAutoCreateColumnsFromModel(true);
	  getTableHeader().setReorderingAllowed(false);
	  getTableHeader().setEnabled(true);
	  this.colTypes=colTypes;
	  DefaultTableModel model = (DefaultTableModel)getModel();
	  model.setColumnIdentifiers(ValueRange.toStringHeader(colTypes));
	  TableColumnModel cmodel=this.getColumnModel();
	  String hdr;
	  int idx;
	  PropertyValueEditor IDEditor;
	  PropertyValueEditor valEditor;
	  for (int col=0;col<cmodel.getColumnCount();col++) {
		hdr=(String) cmodel.getColumn(col).getHeaderValue();
		idx = ValueRange.getColIndex(hdr);		
		switch(idx) {
		case ValueRange.ID:
			IDEditor= new PropertyValueEditor(new IDChecker());
			IDEditor.setClickCountToStart(1);
			cmodel.getColumn(col).setCellEditor(IDEditor);
			break;
		case ValueRange.MIN:
			valEditor= new PropertyValueEditor(null);
			valEditor.setClickCountToStart(1);
			cmodel.getColumn(col).setCellEditor(valEditor);
			break;
		case ValueRange.MAX:
			valEditor= new PropertyValueEditor(null);
			valEditor.setClickCountToStart(1);
			cmodel.getColumn(col).setCellEditor(valEditor);
			break;
		case ValueRange.VALSTEP:
			valEditor= new PropertyValueEditor(null);
			valEditor.setClickCountToStart(1);
			cmodel.getColumn(col).setCellEditor(valEditor);
			break;
		}
						
	  }	  
	  setData(data);	  
  }
 
  
  public int getColTypes() {
	return this.colTypes;
  }
  
  public void setColTypes(int colTypes) {
	this.colTypes = colTypes;
  }
  
  public String[] getItem(int row) {
	  DefaultTableModel model = (DefaultTableModel)getModel();
	  int ncol=model.getColumnCount();
	  String[] s = new String[ncol];
	  if (row>=0 & row<model.getRowCount()) {
		  for (int col=0;col<ncol;col++) {
			  s[col]=(String) model.getValueAt(row, col).toString();
		  }
	  }	  
	  return s;
  }
  
  public String[] getHeader() {
	  DefaultTableModel model = (DefaultTableModel)getModel();
	  int ncol=model.getColumnCount();
	  String[] s = new String[ncol];
	  for (int col=0;col<ncol;col++) {
			  s[col]=(String) model.getColumnName(col);
	  }	  
	  return s;
  }
  
  public void clearTable() {
	  DefaultTableModel model = (DefaultTableModel)getModel();
	  int nrow=model.getRowCount();
	  while (nrow>0) {
		  model.removeRow(nrow);
		  nrow-=1;
	  }	  
  }
  
  public ValueRange[] getValueRanges() throws DataFormatException {
		ValueRange[] result=null;
		if (data==null) return result;
		ValueRange range=null;
		String[] srow;
		srow = new String[getColCount()];
		String[] hdr = getHeader();
		int nrow=getRowCount();
		result= new ValueRange[nrow];
		for (int row=0;row<nrow;row++) {
			srow=getItem(row);
			int index=0;
			double min=0.0;
			double max=0.0;
			double valStep=0.0;
			String clsID="";
			String fieldID="";
			range=null;
		try {
			for (int col=0;col<hdr.length;col++) {
				int idx = ValueRange.getColIndex(hdr[col]);
				switch(idx) {
				case ValueRange.ID:
					String[] s = ((String)srow[col]).split("[.]");
					clsID=s[0];
					index = FieldDef.getFieldIndex(s[1])+1;
					fieldID= FieldDef.getFieldID(s[1]);
					break;
				case ValueRange.MIN:
					min=Double.parseDouble(srow[col]);	
					break;
				case ValueRange.MAX:
					max=Double.parseDouble(srow[col]);
					break;
				case ValueRange.VALSTEP:
					valStep=Double.parseDouble(srow[col]);
					break;
				}
							
			}
		} catch (Exception e) {
			throw new DataFormatException(e.getMessage());
		}
			String s="";
			if (index<=0) {
				s=clsID+"."+fieldID;
			} else {
				s=clsID+"."+fieldID+"("+index+")";
			}
			s += " "+Utils.d2s(min);
			s += " "+Utils.d2s(max);
			s += " "+Utils.d2s(valStep);
			range = new ValueRange(s,data.getType().tid);
			range.setMinValue(min);
			range.setMaxValue(max);
			range.setValStep(valStep);				
			result[row]=range;						
		}				
		return result;
	}
  
  public RangeData getData() throws DataFormatException {
	  if (data!= null) data.setData(getValueRanges());
	  return data;	  
  }  
  
  public void setData(RangeData data) {
	  clearTable();
	  this.data=data;
	  if (data!=null) {
		  DefaultTableModel model = (DefaultTableModel)getModel();
		  for (int row=0;row<data.getNp();row++) {
			  model.addRow(data.getValue(row).toObjectArray(colTypes));
		  }	
	  }
  }
  
  public void addRow(ValueRange range) {
	  if (range != null) {
		  DefaultTableModel model = (DefaultTableModel)getModel();
		  if (model.getRowCount()<maxRows) {
			  model.addRow(range.toObjectArray(colTypes));
		  }
	  }
  }
  public void deleteRow(int row) {
	  if ((row>=0) & (row < getRowCount())) {
		  DefaultTableModel model = (DefaultTableModel)getModel();
		  model.removeRow(row);
	  }
  }
  
  public void insertRow(int row,ValueRange range) {
	  if (range != null) {
		  DefaultTableModel model = (DefaultTableModel)getModel();
		  if (model.getRowCount()<maxRows) {
			  model.insertRow(row,range.toObjectArray(colTypes));
		  }
	  }
  }
  
  
  public int getRowCount() {
	  return ((DefaultTableModel)getModel()).getRowCount();
  }
  
  public int getColCount() {
	  return ((DefaultTableModel)getModel()).getColumnCount();
  }
  
  public class IDChecker implements ValueChecker {

	public boolean checkValue(Object value) {
		boolean res=false;
		if (value instanceof VString) {
			String[] s = value.toString().split("[.]");
			if (s.length == 2) {
				res=true;
			} else {
				JOptionPane.showMessageDialog(null, "Give field ID string in the format COMPONENT.FIELDNAME");
			}			
		}
		return res;
	}
	  
  }
  
  
}
