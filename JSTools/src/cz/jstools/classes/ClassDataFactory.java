package cz.jstools.classes;

import java.util.zip.DataFormatException;


public class ClassDataFactory {
	
	/**
	 * Create descendant of FieldData object of given type,id,name and value.
	 * If value==null, value remains the default one.
	 * @throws DataFormatException
	 */
	public static FieldData createField(FieldType ftype, String id, String name, String value) throws DataFormatException {
		FieldData fdata=null;
		FieldDef fd;
		fd = createFieldDef(ftype,id,name);
		if (fd instanceof FloatDef) {					
			fdata=new FloatData((FloatDef) fd);
		} else if (fd instanceof IntDef) {				
			fdata=new IntData((IntDef) fd);
		} else if (fd instanceof StringDef) {
			fdata=new StringData((StringDef) fd);
		} else if (fd instanceof SelectDef) {
			fdata=new SelectData((SelectDef) fd);
		} else if (fd instanceof RangeDef) {
			fdata=new RangeData((RangeDef) fd);
		} else {
			throw new DataFormatException("Can't create Field datafor "+ftype.toString()); 
		}
		if (value!=null) fdata.setData(value);	
		return fdata;
	}
	
	public static FieldDef createFieldDef(FieldType ftype, String id, String name) throws DataFormatException {
		FieldDef fd=null;
		String idname = FieldDef.getFieldID(id);
		int idx = FieldDef.getFieldIndex(id)+1;
		if (idx<1) idx=1;
		if (ftype==FieldType.FLOAT) {					
			fd=new FloatDef(idname,"",idx);
			fd.name=name;
		} else if (ftype==FieldType.INT) {					
			fd=new IntDef(idname,idx);
			fd.name=name;
		} else if (ftype==FieldType.STRING) {
			fd=new StringDef(idname);
			fd.name=name;
		} else if (ftype==FieldType.SELECT) {					
			fd=new SelectDef(idname);
			fd.name=name;
		} else if (ftype==FieldType.RANGE) {
			fd=new RangeDef(idname);
			fd.name=name;
		} else {
			throw new DataFormatException("Can't create Field definition for "+ftype.toString()); 
		}
		return fd;
	}
	
	public static FieldType createFieldType(String tid) {
		return FieldType.valueOfId(tid);
	}

}
