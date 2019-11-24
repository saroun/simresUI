package cz.restrax.sim.opt;

import java.util.ArrayList;


public class LinConstraint {
	protected final ArrayList<LinEquation> constraints;
	
	public LinConstraint() {
		super();
		constraints = new ArrayList<LinEquation>();
	}		
	
	public void clear() {
		constraints.clear();		
	}
	
	public LinEquation getEquation(String var) {
		LinEquation res = null;
		for (int i=0;i<constraints.size();i++) {
			if (constraints.get(i).var.equalsIgnoreCase(var)) {
				res=constraints.get(i);
				break;
			}
		}
		return res;
	}
	
	public void addItem(String id,String input) {
		LinEquation item = new LinEquation(id,input);
		if (item.var.trim().length()>0) {
			constraints.add(item);
		}
	}
	

	public String getAllCommands(Space space) {
		String cmd="";
		LinEquation formula;
		for (int i=0;i<constraints.size();i++) {
			formula=constraints.get(i);
			if (formula!=null) {
				cmd+=getConstraintCommand(space,formula);
			}		
		}
		return cmd;
	}
	
	protected String getConstraintCommand(Space space, LinEquation eq) {
		double res=0;		
		res=eq.eval(space,this);
		return space.getCommand(eq.var, res);
	}
	
	
}
