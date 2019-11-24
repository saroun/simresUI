package cz.restrax.sim.opt;

public abstract class AbstractOptimizer {
	//private final SimresGUI program;
	protected Space space;	
	protected boolean running;
	
	public AbstractOptimizer() {
		super();
		//this.program=program;
		space = null;
		running=false;
	}
	
	abstract public void start();		
	abstract public void stop();
	abstract public void probe(int id);
	protected abstract void receive(ProbeResult result) ;
	abstract public void saveResult();
	abstract public String getResultsReport();		
					
	public boolean isRunning() {
		return running;
	}
	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public Space getSpace() {
		return space;
	}	
	
	public void setSpace(Space space) {
		this.space=space;
	}

	public void cont() {
		// TODO Auto-generated method stub
		return;
	}

}
