package cz.jstools.classes;

public class ClassFieldData extends FieldData {
	private ClassData cdata;	
	public ClassFieldData(ClassFieldDef type) {
		super(type);
		cdata = new ClassData(type.getCdef(),type.id,type.name);
	}

	@Override
	public void assignData(FieldData source) {	
		try {
			cdata.setData(source.id, source.toString());
		} catch (Exception e) {
		}
	}
	
	@Override
	public ClassFieldData clone() {		
		ClassFieldData fd = new ClassFieldData((ClassFieldDef) this.type);
		fd.cdata=this.cdata.clone();
		return fd;
	}

	@Override
	public ClassData getValue() {
		return cdata;
	}

	@Override
	public void setData(String value) {
		// do nothing
	}

	@Override
	public String valueToString() {
		// TODO Auto-generated method stub
		return cdata.getId();
	}


}
