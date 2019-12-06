package cz.restrax.sim.opt;

import java.util.ArrayList;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.FieldDef;
import cz.jstools.classes.FieldType;
import cz.jstools.classes.definitions.Utils;
import cz.restrax.sim.Instrument;
import cz.restrax.sim.SimresCON;

public class GuideSpace extends SimSpace {
	private final ArrayList<ClassData> guides;
	public GuideSpace(SimresCON program) {
		super(program);
		guides = new ArrayList<ClassData>();
	}
	
	private class GuideVariable extends SimVariable {
		private final ClassData cls;
		//private final FloatData var;
		private final int idx;
		//private final double len;
		
		public GuideVariable(String input, ClassData c) throws Exception {
			super(FieldType.FLOAT,input);
			String[] ids=ID.split("[.]");			
			if (ids.length>1) {				
				String fid=FieldDef.getFieldID(ids[1]);
				if (! fid.equals("ANGLE")) {
					throw new Exception("Only ANGLE field is allowed as GuideVariable");
				}
				cls=c;
				//len=(Double)c.getField("SIZE").getValue(2);
				idx=FieldDef.getFieldIndex(ids[1]);
				if (idx>1) {
					throw new Exception("Only ANGLE(1) or ANGLE(2) is allowed as GuideVariable");
				}				
				//var = (FloatData) c.getField(FieldDef.getFieldID(ids[1]));								
			} else {
				throw new Exception("Wrong format of variable ID, expected COMPONENT.VARIABLE ");
			}						
		}
		

		/*
		
		@Override		
		public String getCommand() {
			String s="";	
			double w1;
			try {
				w1 = (Double)cls.getField("SIZE").getValue(idx);
				int ord=guides.indexOf(cls);
				double a0=0.0d;
				if (ord>0) a0=getAngle(ord-1, idx);			
				// new angle
				double a1 = a0+fValue*0.001d; // fValue angles are given in [mrad] 
				// new exit width
				double w2 = w1 - a1*len;
				s += String.format("set %s EXIT(%d) %f\n", cls.getId(),idx+1,w2);
			// adapt the following guides to keep their relative angles
				double len;
				while (ord<guides.size()-1) {
					ord++;
					s += String.format("set %s SIZE(%d) %f\n", guides.get(ord).getId(),idx+1,w2);
					a0=getAngle(ord, idx);
					len=getAngle(ord, 2);
					w2 = w2 - a0*len;
					s += String.format("set %s EXIT(%d) %f\n", guides.get(ord).getId(),idx+1,w2);
				}				
			} catch (Exception e1) {
				e1.printStackTrace();
			}							
			return s;
		}
		*/
	}

	@Override
	public String getSetCommand() {
		String cmd = "";		
		for (int i=0;i<variables.size();i++) {
			GuideVariable sv=(GuideVariable) variables.get(i);
			cmd += setAngle(sv.fValue, i, sv.idx);
			try {				
				if (i<variables.size()-1) {
					double ex = (Double)guides.get(i).getField("EXIT").getValue(sv.idx);
					cmd += setWidth(ex, i+1, sv.idx);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		cmd+=constraints.getAllCommands(this);
		cmd +="XML UPDATE\n";
		return cmd;
	}
	
	@Override
	public void sendParameters(double[] position) {		
		for (int i=0;i<variables.size();i++) {
			GuideVariable sv=(GuideVariable) variables.get(i);
			sv.setValue(position[i]);
					
		}		
		String cmd =getSetCommand();
		program.executeCommand(cmd,false,false);
	}	
	
	/**
	 * Add a variable from a text line.
	 * Assumed format is "<b>component.field min max tol</b>", where
	 * <dl>
	 * <dt>component</dt><dd>ID of a guide</dd>
	 * <dt>field</dt><dd>SIZE(1) or SIZE(2).</dd>
	 * <dt>min, max, tol</dt><dd>minimum, maximum and tolerance values for angles in [mrad].</dd>
	 * </dl>
	 * The list of variables must be a sequence of guide segment angles, segments being attached to each other.
	 * @param inst
	 * @param text input line of text in the format "<b>component.field min max tol</b>"
	 * @throws Exception on wrong input format or undefined class/field.
	 */
	@Override
	protected void addInput(Instrument inst, String text) throws Exception {
		// a trick how to get rid of trailing spaces and tabs
		String[] items=text.split("([ ]|\t)+");
		ClassData cls=null;
		if (items.length>0) {
			String[] id=items[0].split("[.]");
			if (id.length>1) {
				cls=inst.getClassData(id[0]);
				if (cls!=null) {
					if (! cls.getClassDef().cid.equals("GUIDE")) {
						throw new Exception(String.format(
							"GuideSpace error: only GUIDE elements are allowed on the variable list.\n"+
							"found %s[%s]\n"
							,cls.getId(),cls.getClassDef().cid));							
					}					
				} else {
					throw new Exception(String.format("SimSpace error: Component [%s] is not defined for this instrument\n",id[0]));						
				}

			} else {
					throw new Exception(String.format("SimSpace error: Invalid field ID (%s)\n",items[0]));
			}
			guides.add(cls);
			GuideVariable sv=new GuideVariable(text,cls);
			variables.add(sv);
		}		
	}

	
	/**
	 * Get angle for given guide and side
	 * @param gid
	 * @param side
	 * @return angle in [mrad]
	 */
	protected double getAngle(int gid, int side) {
		double a=0.0d;
		if (gid<0 || gid>=guides.size()) return a;
		try {
			ClassData cls0 = guides.get(gid);
			double w01=0.0d;
			double w02=0.0d;
			double len0=1.0d;
			try {				
				w01 = (Double)cls0.getField("SIZE").getValue(side);
				w02 = (Double)cls0.getField("EXIT").getValue(side);
				len0 = (Double)cls0.getField("SIZE").getValue(2);					
			} catch (Exception e) {
				e.printStackTrace();
			}													
			a=(w01-w02)/len0*1000.0d;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return a;
	}
	
	/**
	 * @param a angle in mrad
	 * @param gid index of the guide segment
	 * @param side  side (0,1)
	 * @return
	 */
	protected String setAngle(double a, int gid, int side) {
		String cmd="";
		if (gid<0 || gid>=guides.size()) return cmd;
		try {
			ClassData cls0 = guides.get(gid);
			double w01=0.0d;
			double w02=0.0d;
			double len0=1.0d;
			try {				
				w01 = (Double)cls0.getField("SIZE").getValue(side);
				len0 = (Double)cls0.getField("SIZE").getValue(2);
				w02 = w01 - len0*a*0.001d;
				cls0.getField("EXIT").setData(Utils.d2s(w02), side);
				cmd += String.format("set %s EXIT(%d) %f\n", cls0.getId(),side+1,w02);
			} catch (Exception e) {
				e.printStackTrace();
			}													
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cmd;
	}
	
	protected String setWidth(double w, int gid, int side) {
		String cmd="";
		if (gid<0 || gid>=guides.size()) return cmd;
		try {
			ClassData cls0 = guides.get(gid);
			try {				
				cls0.getField("SIZE").setData(Utils.d2s(w), side);
				cmd += String.format("set %s SIZE(%d) %f\n", cls0.getId(),side+1,w);
			} catch (Exception e) {
				e.printStackTrace();
			}													
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cmd;
	}
	
	/**
	 * Read variable value from the Instrument.
	 * The variable is the guide segment angle relative to the previous one
	 * @param inst
	 * @param var
	 */
	@Override
	protected void readVariable(Instrument inst, SpaceVariable var)  {
		GuideVariable gv = (GuideVariable) var;		
		int ord=guides.indexOf(gv.cls);		
		double a = getAngle(ord,gv.idx);
		double a0=0.0d;
		if (ord>0) {
			a0=getAngle(ord-1,gv.idx);
		}
		var.setValue((a-a0)); // set value in mrad !
	}
 	
}
