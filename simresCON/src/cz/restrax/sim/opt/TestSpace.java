package cz.restrax.sim.opt;


public class TestSpace extends Space {
	@Override
	public void probe(double[] position, int id) {	
		super.probe(position, id);
		ProbeResult fm = new ProbeResult(id,calculateFM(position),System.currentTimeMillis(),position);
		receive(fm);
	}
	
	@Override
	public String readVariableDefinitions(String fname) {		
		return "";
	}	
	
	public double calculateFM(double[] position) {	
		double a=0.0;
		for (int i=1;i<position.length;i++) {
			a += Math.pow(10*position[i],2);
		}
		if (a>0) {
			return 10.0/(1.0+a);
		} else {
			return 0.0;
		}
	}
	
	public void defineTestSpace(int nvar) {
		variables.clear();
		for (int i=0;i<nvar;i++) {
			try {
				defineNewVariable(String.format("A%d %f %f  %f",i,-0.5,0.5,0.001));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		initialize(variables.size());
	}

	@Override
	public void sendParameters(double[] position) {
		// do nothing
	}

	/**
	 * Read actual values of variables after probe execution.
	 * Just returns the actual values of variables as double array.
	 * NOTE: Some other descendants of Space may change the values during adjustment for constraints etc. 
	 * @param inst
	 */
	@Override
	public double[] getVariableValues() {
		double[] data=new double[variables.size()];
		for (int i=0;i<variables.size();i++) {
			data[i]=variables.get(i).getValue();			
		}
		return data;
	}

	@Override
	public String getSetCommand() {
		return "";
	}

	@Override
	public void sendParameter(String id, double position) {
		//
	}

	@Override
	protected String getCommand(String id, double val) {
		return "";
	}
		


	

}
