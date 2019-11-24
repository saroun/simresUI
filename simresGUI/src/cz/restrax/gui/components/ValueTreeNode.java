package cz.restrax.gui.components;

import java.util.zip.DataFormatException;

import javax.swing.tree.DefaultMutableTreeNode;

import cz.saroun.classes.ClassData;
import cz.saroun.classes.FieldData;
import cz.saroun.classes.FieldDef;
import cz.saroun.classes.ValueRange;

public class ValueTreeNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 1L;
	private ClassData cls;
	private FieldData fieldData;
	private int index;
	
	
	public ValueTreeNode(ClassData cls, String id) {
		super(id);
		this.cls=cls;
		this.index=0;
		try {
			fieldData = cls.getField(FieldDef.getFieldID(id));
			setAllowsChildren(fieldData.getType().isVector());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ValueTreeNode(ClassData cls, String id, int index) {
		super(id);
		this.cls=cls;
		this.index=index;
		try {
			fieldData = cls.getField(FieldDef.getFieldID(id));
			setAllowsChildren(fieldData.getType().isVector());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private ValueTreeNode(ClassData cls, FieldData fieldData, int index) {
		super(fieldData.getType().toIndexedName(index));
		this.cls=cls;
		this.index=index;
		this.fieldData=fieldData;
		setAllowsChildren(fieldData.getType().isVector());		
	}

	public ValueTreeNode clone() {
		return new ValueTreeNode(cls.clone(),fieldData.clone(),index);
	}	
	
	
	public Object getValue() {
		return fieldData.getValue(index);		
	}
	
	public String valueToString() {
		return fieldData.valueToString(index);		
	}
	
	public String toString() {
		String s = new String();
		if ( fieldData != null) {
			if (fieldData.getType().isVector() & isLeaf()) {
				s = fieldData.id + "("+index+")";
			} else {
				s = fieldData.id + " " + fieldData.getType().name;
			}			
		}
		return s.trim();		
	}


	public ClassData getCls() {
		return cls;
	}

	public ValueRange toValueRange() {
		ValueRange res=null;
		String s = ValueRange.getInputString(cls, fieldData, index);
		try {
			res= new ValueRange(s,fieldData.getType().tid);
		} catch (DataFormatException e) {
			e.printStackTrace();
		} 
		return res;
	}

	public FieldData getFieldData() {
		return fieldData;
	}

	public int getIndex() {
		return index;
	}
	
}
