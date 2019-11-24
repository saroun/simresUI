package cz.restrax.sim.opt;

import java.util.ArrayList;

import cz.restrax.sim.opt.Space.SpaceVariable;

public class LinEquation {

	public String var;
	public String input;
	public double value;
	protected final ArrayList<LinTerm> expression;
	
	/**
	 * Multiplicative term
	 */
	public class MultTerm {		
			public final int op; // * (0) or / (1) or empty (-1, first term)
			public final String var;
			public MultTerm(int op, String var) {
				super();
				this.op=op;
				this.var=var;			
			}
			public String toString() {
				if (op==0) {
					return "*"+var;
				} else if (op==1) {
					return "/"+var;
				} else {
					return var;
				}
				
			}
	}
		
	/**
	 * Additive term
	 */
	public class LinTerm {
		public final int sgn;
		public final ArrayList<MultTerm> terms;
		public String input;
		public LinTerm(int sgn, String input) {
			super();
			this.sgn=sgn;
			this.input=input;
			terms = new ArrayList<MultTerm>();	
			set(input);
		}
		
		/**
		 * Add one term of a linear expression. The term can be composed 
		 * of variable names or constants separated by * or / signs
		 * @param space
		 * @param term
		 * @return
		 */
		public void set(String input) {
			String[] cmd=input.split("[*/]");
			this.input=input;
			terms.clear();
			if (cmd.length>0) {
				// put the first term with default + sign
				terms.add(new MultTerm(-1, cmd[0].trim()));
				// add other terms if required
				int ll=cmd[0].length();
				for (int i=1;i<cmd.length;i++) {
					String sg=input.substring(ll, ll+1);
					if (sg.equals("*")) {
						terms.add(new MultTerm(0, cmd[i].trim()));
					} else if (sg.equals("/")) {
						terms.add(new MultTerm(1, cmd[i].trim()));
					}
					ll+=cmd[i].length()+1;
				}
			} 
		}
		
		/**
		 * Evaluate the constraint equation using given variable space. 
		 * @param space
		 * @param constraints is a list of linear equations
		 * @return
		 */
		public double eval(Space space, LinConstraint constraints) {
			double res=0;
			for (int i=0;i<terms.size();i++) {
			 // read one term, convert to double and add to the result
				String val=terms.get(i).var;
				int op=terms.get(i).op;
				double x=1;
				try {
			// if the term is a constant, parse it to a number
					x=Double.parseDouble(val);
				} catch (Exception e) {
			// otherwise try to interpret it  as a variable	
					SpaceVariable svar=space.getVariable(val);
					if (svar!=null) {
				// found: add the value 
						x=svar.getValue();
					} else {
				// not found: assume it is a variable defined and evaluated before
						LinEquation eq=constraints.getEquation(val);
						if (eq!=null) {
							x=eq.getLastValue();
						}
					}
				}
				switch(op) {
				  case -1: res = x;break;
				  case 0: res *= x;break;
				  case 1: res = res/x;break;
				  default: res *= x;
				}
			}
			return res;
		}
		
		public String toString() {
			String sg;
			switch (sgn) {
			case 1: sg="+"; break;
			case -1: sg="-";break;
			default: sg="";break;
			}
			String res=sg;
			for (int i=0;i<terms.size();i++) {
				res+=terms.get(i).toString();
			}
			return res;			
		}		
	}	
	
	
	public LinEquation(String id, String input) {
		super();
		var="";
		value=0;
		expression=new ArrayList<LinTerm>();	
		set(id,input);
	}
	
	/**
	 * Read linear expression from the input and create corresponding array of LinTerm objects.
	 * Just splits the string on =,+,- characters.
	 * @param input
	 */
	protected void set(String id, String input) {			
		expression.clear();
		this.input=input;
		var=id.trim();
		String[] eqs=input.split("[+-]");
		// put the first term with default + sign
		expression.add(new LinTerm(1, eqs[0].trim()));
		// add other terms if required
		int ll=eqs[0].length();
		for (int i=1;i<eqs.length;i++) {
			String sg=input.substring(ll, ll+1);
			if (sg.equals("+")) {
				expression.add(new LinTerm(1, eqs[i].trim()));
			} else if (sg.equals("-")) {
				expression.add(new LinTerm(-1, eqs[i].trim()));
			}
				ll+=eqs[i].length()+1;
		}
	}
	

	
	/**
	 * Evaluate the constraint equation using given variable space. 
	 * @param space
	 * @param constraints is a list of linear equations
	 * @return
	 */
	public double eval(Space space, LinConstraint constraints) {
		double res=0;
		for (int i=0;i<expression.size();i++) {
		 // read one term, convert to double and add to the result
			double x=expression.get(i).eval(space, constraints);
			int sg=expression.get(i).sgn;
			switch (sg) {
			case 1: case -1: res += sg*x;break;
		    default: res += x;
			}				
		}
		value=res;
		return res;
	}
	
	public double getLastValue() {
		return value;
	}
	
	public String toString() {
		return input;
	}	
}
