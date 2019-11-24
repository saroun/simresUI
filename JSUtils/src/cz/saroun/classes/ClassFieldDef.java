package cz.saroun.classes;



/**
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.4 $</dt>
 *               <dt>$Date: 2014/05/05 18:41:18 $</dt></dl>
 */
public class ClassFieldDef extends FieldDef {
	private ClassDef cdef;
	public ClassFieldDef(String id, ClassDef cdef) {
		super(id,1,FieldType.CLASSOBJ);
		this.cdef=new ClassDef(cdef.cid,cdef.name,cdef.parent);
		this.cdef.assign(cdef,this);
	}
	
	public ClassDef getCdef() {
		return cdef;
	}
	
	@Override
    public String getReadonlyCondID() {
    	return cdef.cid+"."+readonlyCond[0];
    }
	@Override
    public String getHiddenCondID() {
    	return cdef.cid+"."+hiddenCond[0];
    }
    
}
