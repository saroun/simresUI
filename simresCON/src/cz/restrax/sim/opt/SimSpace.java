package cz.restrax.sim.opt;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.FieldData;
import cz.jstools.classes.FieldDef;
import cz.jstools.classes.FieldType;
import cz.restrax.sim.Instrument;
import cz.restrax.sim.SimresCON;

public class SimSpace extends Space {
	protected SimresCON program;	
	protected LinEquation costFunction;

	public SimSpace(SimresCON program) {
		super();
		this.program=program;
		costFunction=new LinEquation("cost","0.0");
	}
	
	public class SimVariable extends SpaceVariable {
		protected final FieldType typ;
	//	protected final double fCost;
		public SimVariable(FieldType typ, String input) throws Exception {
			super(input);
			this.typ=typ;
			/*
			String[] items=input.split("([ ]|\t)+");
			if (items.length>4) {
				fCost=Double.parseDouble(items[4]);
			} else {
				fCost=0;
			}
			*/
		}
		
		@Override
		public void setValue(double val) {
			if (typ==FieldType.FLOAT) {
				fValue=Math.min(fMax, Math.max(fMin,val));
			} else {
				fValue=Math.round(Math.min(fMax, Math.max(fMin,val)));
			}
		}
		
		@Override
		public double getValue() {
			if (typ==FieldType.FLOAT) {
				return fValue;
			} else {
				return Math.round(fValue);
			}
		}
		
		@Override
		public String getValueString() {
			String res="";			
			if (typ==FieldType.FLOAT) {
				res=String.format("%f",fValue);
			} else if (typ==FieldType.FLOAT) {
				res=String.format("%d",Math.round(fValue));
			}
			return res;
		}
		
		/*
		private double getCost() {
			return fCost;
		}
		*/
		
		private String getCommand() {
			String s="";
			String[] ids=ID.split("([|]|[:])");
			for (int i=0;i<ids.length;i++) {
				String[] fullid=ids[i].split("[.]");
				if (fullid.length>1) {
					if (typ==FieldType.FLOAT) {
						s += String.format("set %s %s  %f \n", fullid[0],fullid[1],fValue);
					} else {
						s += String.format("set %s %s  %d \n", fullid[0],fullid[1],Math.round(fValue));
					}
				}
			}
			return s;
		}
		

	}
	
	@Override
	protected String getCommand(String id, double val) {
		String s="";
		String[] fullid=id.split("[.]");
		if (fullid.length>1) {
			s += String.format("set %s %s  %f \n", fullid[0],fullid[1],val);
		}
		return s;
	}
	
	public String getResultHeader() {
		return String.format("time id  FM  FM-COST COST  INT  WIDTH  data[0]");
	}
	

	/**
	 * Add a variable from a text line.
	 * Assumed format is "<b>c[component.field|component.field|...] min max tol</b>", where
	 * <dl>
	 * <dt>component</dt><dd>ID of the component</dd>
	 * <dt>field</dt><dd>ID of the field (use () for array components, e.g. SAMPLE.AX(1)).
	 * You can use multiple variable names separated by | (no spaces). In such case, these variables will be bound to the same value. 
	 * This is usefull e.g. when we want to keep zero gap between two guide segments, while varying their lengths.</dd>
	 * <dt>min, max, tol</dt><dd>minimum, maximum and tolerance values.</dd>
	 * </dl>
	 * Only float and integer types are accepted.
	 * @param inst  instrument object with all components
	 * @param text
	 * @throws Exception on wrong input format or undefined class/field.
	 */
	protected void addInput(Instrument inst, String text) throws Exception {
		String[] items=text.split("([ ]|\t)+");
		FieldType tid=FieldType.UNDEFINED;
		ClassData cls;
		if (items.length>0) {
			String[] ids=items[0].split("([|]|[:])");
			for (int i=0;i<ids.length;i++) {
				String[] id=ids[i].split("[.]");
				if (id.length>1) {
					cls=inst.getClassData(id[0]);
					if (cls!=null) {
						FieldData f = cls.getField(FieldDef.getFieldID(id[1]));
						if (f!=null) {
							FieldDef fd=f.getType();
							if (fd.tid == FieldType.TABLE) {
								tid=FieldType.FLOAT;
							}				
							else if (tid==FieldType.UNDEFINED) {
								tid=fd.tid;	
							} else if (tid != fd.tid) {
								throw new Exception(String.format("SimSpace error: Wrong variable type for %s (expected %s)\n",id[1],tid.toString()));							
							}
						}
											
					} else {
						throw new Exception(String.format("SimSpace error: Component [%s] is not defined for this instrument\n",id[0]));						
					}
				
				} else {
					tid=FieldType.FLOAT;
					//throw new Exception(String.format("SimSpace error: Invalid field ID (%s)\n",ids[i]));
				}
			}
			if (tid==FieldType.FLOAT || tid==FieldType.INT) {
				SimVariable sv=new SimVariable(tid,text);
				variables.add(sv);
			} else {
				throw new Exception(String.format("SimSpace error: Wrong variable type for %s (only FLOAT or INT are accepted)\n",items[1]));											
			}
		}		
	}
		

	
	/**
	 * Read values of variables from the Instrument.
	 * @param inst
	 */
	@Override
	public double[] getVariableValues() {
		readVariables();
		double[] data=new double[variables.size()];
		for (int i=0;i<variables.size();i++) {
			data[i]=variables.get(i).getValue();			
		}
		return data;
	}
	
	/**
	 * Read values of variables from the Instrument.
	 */
	protected void readVariables() {
		for (int i=0;i<variables.size();i++) {
			readVariable(program.getSpectrometer(), variables.get((Integer)i));
		}
	}
	/**
	 * Read variable value from the Instrument
	 * @param inst
	 * @param var
	 */
	protected void readVariable(Instrument inst, SpaceVariable var)  {
		ClassData cls;
		String[] ids=var.ID.split("([|]|[:])");
		if (ids.length>0) {
			String[] id=ids[0].split("[.]");
			if (id.length>1) {
				cls=inst.getClassData(id[0]);
				if (cls!=null) {
					FieldData f;
					try {
						f = cls.getField(FieldDef.getFieldID(id[1]));
					if (f!=null) {
						FieldDef fd=f.getType();
						if (FieldDef.fieldHasIndex(id[1])) {
							int i=FieldDef.getFieldIndex(id[1]);
							if (fd.tid==FieldType.FLOAT) {							  
								var.setValue((Double) f.getValue(i));
							} else if (fd.tid==FieldType.INT) {
								var.setValue((Integer) f.getValue(i));
							}	  
						} else {
							if (fd.tid==FieldType.FLOAT) {							  
								var.setValue((Double) f.getValue());
							} else if (fd.tid==FieldType.INT) {
								var.setValue((Integer) f.getValue());
							}
						}
					}
					} catch (Exception e) {
						// do nothing, ignore errors						
					}					

				}
			}
		}
	}
	
	/**
	 * Calculate cost term to be applied to the figure of merit
	 * @param vars
	 * @return
	 */
	public double getCost() {
		double res=0.0;
		if (costFunction != null) {
			res=costFunction.eval(this, constraints);
		}		
		return res;
	}
	
	

	/*
	public double getCost(double[] vars) {
		double res=0.0;
		if (vars.length != dim) return res;
		for (int i=0;i<vars.length;i++) {
			SimVariable sv=(SimVariable) variables.get(i);
			//res += vars[i]*sv.getCost();		
		}
		return res;
	}
	*/
 
/* --------------------   Overriden methods -------------------------*/	


	/**
	 * Set variables to position. Send changed parameters to the program
	 * and execute FMERIT command with given ID.
	 * @return null in order to force asynchronous handling
	 * @see cz.restrax.opt.Space#probe(double[], int)
	 */
	@Override
	public void probe(double[] position, int id) {
		/*program.getConsoleLog().print(String.format(
				"SimSpace.probe, id=%d, pos[1]=%f  \n",id,position[0]));*/
		super.probe(position, id);
		sendParameters(position);
		program.executeCommand(String.format("DO FMERIT %d\n",id),false,false);

	}
	
	public String getSetCommand() {
		String cmd = "";					
		for (int i=0;i<variables.size();i++) {
			SimVariable sv=(SimVariable) variables.get(i);
			cmd+=sv.getCommand();			
		}
		cmd+=constraints.getAllCommands(this);
		cmd+="XML UPDATE\n";
		return cmd;
	}
	
	@Override
	public void sendParameters(double[] position) {		
		for (int i=0;i<variables.size();i++) {
			SimVariable sv=(SimVariable) variables.get(i);
			sv.setValue(position[i]);		
		}
		program.executeCommand(getSetCommand(),false,false);		
	}	

	@Override
	public void sendParameter(String id, double position) {
		String cmd=getCommand(id,position);
		program.executeCommand(cmd,false,false);
	}
	
	@Override
	public void defineNewVariable (String input) throws Exception {
		addInput(program.getSpectrometer(), input);
	}
	
	@Override
	public void defineNewConstraint(String input) {
		String [] cmd=input.split("[=]");
		if (cmd.length>1) {
			String id=cmd[0].trim();
			if (id.equalsIgnoreCase("cost")) {
				costFunction=new LinEquation("cost",cmd[1].trim());
			} else {
				constraints.addItem(id,cmd[1].trim());
			}			
		}		
	}
	
}
