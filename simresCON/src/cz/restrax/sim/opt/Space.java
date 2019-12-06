package cz.restrax.sim.opt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import cz.jstools.classes.definitions.Utils;

public abstract class Space {
	protected double[] lowlim;
	protected double[] highlim;
	protected double[] tol;
	protected int dim;
	protected HashMap<Integer, ProbeResult> results;
	protected HashMap<Integer,ProbeResult> queue;
	private ProbeResult last=null;
	private ProbeResult first=null;
	protected final ArrayList<SpaceVariable> variables;
	protected final LinConstraint constraints;
	
	public Space() {
		super();
		results=new HashMap<Integer,ProbeResult>();
		queue=new HashMap<Integer,ProbeResult>();
		variables = new ArrayList<SpaceVariable>();
		constraints = new LinConstraint();
		lowlim=null;
		highlim=null;
		tol=null;
	}
	
	
	abstract public void sendParameters(double[] position);
	abstract public void sendParameter(String id, double position);
	abstract public double[] getVariableValues();
	abstract public String getSetCommand();
	abstract protected String getCommand(String id, double val);



	/**
	 * Initialize calculation of FM for given position and probe ID number.
	 * @param position
	 * @param id
	 */
	public void probe(double[] position, int id) {
		queue.put((Integer)id,null);
	}
	
	/**
	 * Obtain the result of previously called probe through this procedure
	 * @param fm
	 */
	public void receive(ProbeResult result) {
		if (result==null) return;
		if (queue.containsKey((Integer)result.id)) {
			queue.put((Integer)result.id,result);
		};
	}

	public class SpaceVariable {
		protected final String ID;
		protected final double fMin;
		protected final double fMax;
		protected final double fTol;
		protected double fValue;
		public SpaceVariable(String input) throws Exception {
			super();
			String[] items=input.split("([ ]|\t)+");
			if (items.length>3) {
				ID=items[0];
				fMin=Double.parseDouble(items[1]);
				fMax=Double.parseDouble(items[2]);
				fTol=Double.parseDouble(items[3]);			
				fValue=(fMax+fMin)/2.0;
			} else {
				throw new Exception("SpaceVariable: wrong input format ("+input+")");
			}
		}		
		public void setValue(double val) {
			fValue=Math.min(fMax, Math.max(fMin,val));
		}		
		public double getValue() {
			return fValue;
		}
		
		public String getValueString() {
			return String.format("%f",fValue);					
		}

	}
		
	public String getResultHeader() {
		return String.format("time id  FM");
	}
	
	/**
	 * Creates and initializes internal arrays for given dimension.
	 * @param dim
	 */
	public void initialize(int dim) {
		this.dim=dim;
		lowlim=new double[dim];
		highlim=new double[dim];
		tol=new double[dim];
		for (int i=0;i<variables.size();i++) {
			tol[i]=variables.get(i).fTol;
			lowlim[i]=variables.get(i).fMin;
			highlim[i]=variables.get(i).fMax;
			// adjust highlim-lowlim to a multiple of tol
			highlim[i]=getValidPosition(i,variables.get(i).fMax);
		}
	}
	
	public double getValidPosition(int i,double pos) {
		double z;
		if (tol[i]>0) {
        	z=lowlim[i]+tol[i]*Math.round((pos-lowlim[i])/tol[i]);
        } else {
        	z=pos;
        }		
		return Math.min(highlim[i],Math.max(lowlim[i],z));
	}
	
	public double[] getValidPositions(double[] pos) {
		double[] res = new double[dim];
		for (int i=0;i<dim;i++) {
			res[i]=getValidPosition(i,pos[i]);
		}
		return res;
	}
	
	/**
	 * Read definitions of parameters from a file, 1 parameter per line.
	 * The syntax is 
	 * @param fname
	 * @return error message (empty string if there are no errors)
	 */
	public String readVariableDefinitions(String fname) {
		String[] text=null;
		String errMsg="";
		try {
			String content = Utils.readFileToString(fname);
			text=content.split("\n");
			defineVariables(text);
		} catch (IOException e) {
			// e.printStackTrace();
			errMsg="Can't read file."+e.getMessage();
			//program.getMessages().errorMessage("Can't read file."+e.getMessage(),"low", this.toString());
			clearVariables();
		} catch (Exception e) {
			// e.printStackTrace();
			errMsg="Syntax error in parameter list\n"+e.getMessage();
			//program.getMessages().errorMessage("Syntax error in parameter list\n"+e.getMessage(),"low", this.toString());
			clearVariables();
		}
		return errMsg;
	}	
	/**
	 * Set variable definitions from a string array. Each string 
	 * contains at least 3 space-delimited items:
	 * ID, minimum, maximum
	 * @param lines
	 * @throws Exception
	 */
	public void defineVariables(String[] lines) throws Exception {
		variables.clear();
		constraints.clear();
		for (int i=0;i<lines.length;i++) {
			String line=lines[i];
			if (line.length()>2) {
				if (line.indexOf("#")==0 || line.trim().length()==0) {
				//if (lines[i].matches("[#].*") || lines[i].matches("^([ ]|\t)*$") ) {
					// skip comments and empty lines
				// constraint
				} else if (line.indexOf("=")>0) {
					defineNewConstraint(line);
				// variable
				} else {
					defineNewVariable(line);
				}
			}
		}
		initialize(variables.size());
	}
	
	protected void defineNewVariable (String input) throws Exception {
		SpaceVariable sv=new SpaceVariable(input);
		variables.add(sv);
	}
		
	/**
	 * Returns text report of result sequence. Only results increasing FM are recorded.
	 */
	public  String getResultSequence() {
		String out="";
		ProbeResult fm=first;
		long t=0;
		long dt;
		while(fm!=null) {
			if (t==0) t=fm.time;
			dt=fm.time-t;
			out+=String.format("%f  ",dt/1000.0/3600)+fm.getResultString();
			fm=fm.next;
		}
		return out;
	}
		
	public int getDim() {
		return dim;
	}	
	
	/**
	 * Returns result data with given probe ID if defined, else null.
	 * @param id
	 * @return
	 */
	/*
	private FigureOfMerit getResult(int id) {
		if (results.containsKey((Integer)id)) {
			return results.get((Integer)id);
		} else {
			return null;
		}
	}
	*/
	
	/**
	 * Returns next waiting result in the queue
	 * @param id
	 * @return
	 */
	public ProbeResult popQueue() {		
		if (! queue.isEmpty()) {
			// / return 1st item on the waitlist
			for (Integer id : queue.keySet() ) {
				return queue.remove(id);
			}			
		}
		return null;
	}
	

	
	/**
	 * @return Results record as HashMap (key=probe ID)
	 */
	public HashMap<Integer, ProbeResult> getResults() {
		return results;
	}

	/**
	 * 
	 * @param id
	 * @return True if a record with given probe ID exists.
	 */
	public boolean isResultReady(int id) {
		return results.containsKey(id);
	}

	/**
	 * Stores results of the previously called probe with given ID.
	 * Only results with increasing FM are recorded. Return true 
	 * if the result is the best one.
	 * @param result
	 */
	public boolean setResult(ProbeResult result) {
		// save only results with better FM
		boolean res=false;
		if (last==null || result.FM>last.FM) {				
			if (results.get((Integer)result.id)==null) {
				if (last==null) {
					first=result;
				} else {
					last.next=result;
				}
				result.next=null;					
				last=result;
			}				
			results.put(result.id, result);	
			res=true;
		}
		return res;
	}
	
	
	/**
	 * Clear results record.
	 */
	public void clearResults() {
		results.clear();
		last=null;
		first=null;
	}

	/**
	 * @return True if probe queue is empty.
	 */
	public boolean isAllDone() {
		return queue.isEmpty();
	}
	
	/**
	 * @return True if there is a result ready for handling in the queue.
	 */
	public boolean isReady() {
		if (! queue.isEmpty()) {
			for (Integer id : queue.keySet() ) {
				if (queue.get(id) != null) return true;
			}			
		}
		return false;		
	}

	/**
	 * Clear queue (ArayList waitlist).
	 */
	public void clearQueue() {
		queue.clear();		
	}
	
	/**
	 * Clear variables.
	 */
	public void clearVariables() {
		variables.clear();		
	}

	public String getVarName(int i) {
		String s="";
		if (i>=0 && i<variables.size()) {
			s=variables.get(i).ID;
		}
		return s;
	}

	public void setValue(int i, double val) {
		if (variables.get(i)!=null) {
			variables.get(i).setValue(val);
		}				
	}
	
	public double getValue(int i) {
		double res=0;
		if (variables.get(i)!=null) {
			res=variables.get(i).getValue();
		}
		return res;
	}

	public String getValueString(int i) {
		SpaceVariable sv = variables.get(i);
		if (sv!=null) {
			return sv.getValueString();
		} else {
			return "";
		}	
	}

	public ProbeResult getLast() {
		return last;
	}

	public HashMap<Integer, ProbeResult> getQueue() {
		return queue;
	}

	
	/**
	 * Get variable of given ID. Return null if it does not exist;
	 * If there are multiple variables of the same ID, return the first one.
	 */
	public SpaceVariable getVariable(String id) {
		SpaceVariable v=null;
		for (int i = 0;i<variables.size();i++) {
			if (variables.get(i).ID.equalsIgnoreCase(id)) {
				v=variables.get(i);
				break;
			}
		}
		return v;
	}


	public void defineNewConstraint(String input) {

	}




}
