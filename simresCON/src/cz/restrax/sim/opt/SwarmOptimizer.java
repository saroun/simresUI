package cz.restrax.sim.opt;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.definitions.Utils;
import cz.restrax.sim.ProjectList;
import cz.restrax.sim.SimresCON;
import cz.restrax.sim.commands.SimresHandler;

public class SwarmOptimizer extends AbstractOptimizer {
	public static final int TYPE_ALLPARAM=0;
	public static final int TYPE_GUIDES=1;
	private final SimresCON program;
	private Swarm swarm=null;
	private int maxLoops=0;
	private int nStop=0;
	private ClassData cls;
	private static final int MAXSPECIES=100000;
	private int counter=0;
	private int iloop=0;
	private int iwasp=0;	
	private int updateLoop=-1; // loop after which swarm updates its search space
	private long timeout;
	private Timer timer;
	private static final int TIMER_PERIOD=100;
	private long time0=0;
	private boolean changedInstrument=false;
	private int spaceType=TYPE_ALLPARAM;
	private double costWeight=0.0;
//	private ArrayList<Integer> queue;
	
	/**
	 * Optimizer for swarm optimization.
	 * Call setOptimizerParam and setSpace before using.
	 * @param program
	 */
	public SwarmOptimizer(SimresCON program) {
		super();
		this.program=program;
		space = null;
		timer = new Timer();		
	//	queue=new ArrayList<Integer>();
	}
	
	public class SwarmTimerTask extends TimerTask {
		private long time=0;
		private static final long REPORT_PERIOD=10000;
		@Override
		public void run() {
		//	System.out.printf("run %s ",running);
			if (time0==0) time0=System.currentTimeMillis();
			if (space.isReady()) {
				ProbeResult result = space.popQueue();
			//	System.out.printf("ready=%s ",result!=null);				
				if (result!=null) {
				//	System.out.printf("id=%d  FM=%f\n",result.id,result.FM);
					receive(result);
				}
			}
			if (time<=0) time=System.currentTimeMillis();					
			long t=this.scheduledExecutionTime();
			if (t-time>REPORT_PERIOD) {
				saveResult();
				time=t;
			}		
			if (this.scheduledExecutionTime()-time0>timeout) {
				stop();
			}
			if (! running) timer.cancel();			
		}
	}

	
	@Override
	public void start() {
		if (running) return;
		try {
			counter=0;
			maxLoops=0;
			iwasp=-1;
			iloop=0;
			changedInstrument=false;
			String fname=cls.getField("INPUT").valueToString();			
			// try user's project path
			String fullname=program.getProjectList().getFullPath(ProjectList.PROJ_CFG, fname);
			File f = new File(fullname);
			if (! f.exists()) {
				String msg=String.format("Input file '%s' does not exist.\nIt must be in the project directory '%s'.\n", 
						fname,program.getProjectList().getCurrentPathProject());
				program.getMessages().errorMessage(msg,"low", "SwarmOptimizer");
				return;
			}
			
			int n=(Integer)cls.getField("NSPEC").getValue();
			n=Math.min(n,MAXSPECIES-1);
			String err=space.readVariableDefinitions(f.getPath());
			if (! err.isEmpty()) {
				program.getMessages().errorMessage(err,"low", "SwarmOptimizer");
				return;
			}
			space.clearResults();
			space.clearQueue();		
			swarm=new Swarm(space, program.seed);
			swarm.populate(n,space.getVariableValues());
			time0=0;
			cont();			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	@Override
	public void cont() {
		try {
			double acc=(Double)cls.getField("ACC").getValue();
			double spd=(Double)cls.getField("SPREAD").getValue();
			double esc=(Double)cls.getField("PEXP").getValue();	
			double tout =(Double)cls.getField("TOUT").getValue();
			boolean useAve=((Integer)cls.getField("AVE").getValue()).equals(1);
			costWeight=(Double)cls.getField("WCOST").getValue();
			timeout  = Math.round(tout*3600*1000);
			maxLoops+=(Integer)cls.getField("NLOOPS").getValue();
			nStop=(Integer)cls.getField("NSTOP").getValue();
			updateLoop=(Integer)cls.getField("UPDLOOP").getValue();
			if (updateLoop<2) updateLoop=-1;
			swarm.setAcc(acc,spd,esc);
			swarm.setUseAverage(useAve);
			running=true;
			timer = new Timer();
			timer.schedule(new SwarmTimerTask(), 10, TIMER_PERIOD);
			dispatch();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void dispatch() {
		int id = 0;
		if (! running) return;
		//if (space.getLast() != null) id=space.getLast().id;
		//int iw=getIWasp(id);
		//int iloop=getILoop(id);
		if (iloop<maxLoops) {
			if (iwasp<swarm.getCount()-1) {
				iwasp++;
			} else if (iloop<maxLoops-1) {
				iloop++;
				iwasp=0;
				if (iloop==updateLoop) {
					double fac=swarm.updateSearchSpace();
					program.getConsoleLog().print(String.format("Search volume reduction, fac=%f\n",fac));
					}
			} else if (space.isAllDone()) {
				stop();
			} else {
				return;
			}
		} else {
			stop();
		}
		if (running) {
			id=calID(iwasp, iloop);
			/* program.getConsoleLog().print(String.format("dispatch iwasp=%d  id=%d  counter=%d\n",
					iwasp,id,counter));	*/		
			probe(id);
		}
	}
	
	@Override
	public void stop() {
		running=false;
		timer.cancel();
		timer.purge();
		// queue.clear();
		saveResult();
		String[] names=new String[space.dim];
		String[] values=new String[space.dim];
		double[] target=swarm.getBest();
		for (int i=0;i<names.length;i++) {
			names[i]=space.getVarName(i);
			space.setValue(i, target[i]);
			values[i]=space.getValueString(i);
		}
		String msg=String.format("Stop, count=%d, results=%d, queue=%d\n",
				counter,space.getResults().size(),space.getQueue().size());
		program.getConsoleLog().print(msg);
		program.getResultsLog().printMultiLineText("\n"+space.getResultHeader());
		program.getResultsLog().printMultiLineText(space.getResultSequence()+"\n");
	//	program.getResultsWindow().printSource(text)
		program.getResultsLog().printList(names, values);
	// set instrument to the best configuration	
		changedInstrument=true;
		//swarm.sort();
		space.sendParameters(swarm.getBest());	
	// terminate program in console command regime
		// program.setTerminate(program.isRunOnce());
		if (program.isRunOnce()) program.Terminate();
	}
	
	public void saveResultInstrument() {
		changedInstrument=false;
		// get filename part of current config path-name
		File f= new File(program.getProjectList().getCurrentFileConfig());
		// get full path at the output directory
		String fname=program.getProjectList().getFullPath(
					ProjectList.PROJ_OUT, "swarm."+f.getName());
		program.getCommands().handleCommand(SimresHandler.CMD_SAVE, fname);
	}
		
	/** Let the wasp corresponding to the given ID to fly and probe the space.
	 *  If wasp.flyAndProbe(id) returns null, exit and wait for explicit call
	 *  of receive method. Otherwise, call the receive method with the argument value returned by
	 *  wasp.flyAndProbe(id).
	 * @see cz.restrax.sim.opt.AbstractOptimizer#probe(int)
	 */
	@Override
	public void probe(int id) {
		int iwasp=getIWasp(id);
		swarm.flyAndProbe(iwasp,id);
	}

	
	/**
	 * Applies cost correction to the previously received result.FM
	 * Store received FM as VAL
	 * Total figure of merit is calculated as VALUE / COST
	 * @param result
	 */
	protected void setCost(ProbeResult result) {
		if ((space instanceof SimSpace) ) {
			result.VAL=result.FM;
			if (costWeight>0.0 && costWeight<1.0) {
				result.COST=((SimSpace)space).getCost();
				if (result.COST>0) {
					double z = 1-costWeight;
					result.FM = Math.sqrt(z*Math.pow(result.FM,2)+costWeight*Math.pow(1.0/result.COST,2));
				}
			} else {
				result.COST=0;
			}
		}
	}
	
	@Override
	protected void receive(ProbeResult result) {
		counter++;
		result.counter=counter;
		setCost(result);
		// transmit the received result to space receiver	
		int iw=getIWasp(result.id);
		/*
		program.getConsoleLog().print(String.format(
			    "receive iw=%d  id=%d  data[0]=%f\n",
					iw,result.id,result.data[0]	
		));	
		*/	
		swarm.receive(iw, result);
		if (space.setResult(result)) {
			String msg=String.format(
				    "iw=%d  id=%d   counter=%d  FM=%f  value=%f  cost=%f  data[0]=%f  BEST\n",
						iw,result.id,result.counter,result.FM,result.VAL,result.COST,result.data[0]	
			);
			program.getConsoleLog().print(msg);
			msg=String.format(
				    "counter=%d  FM=%f  value=%f  cost=%f  data[0]=%f\n",
				    result.counter,result.FM,result.VAL,result.COST,result.data[0]);
			program.getResultsLog().printMultiLineText(msg);
			saveResultScript();
		} else {
			program.getConsoleLog().print(String.format(
					"iw=%d  id=%d   counter=%d  FM=%f  cost=%f  data[0]=%f\n",
					iw,result.id,result.counter,result.FM,result.COST,result.data[0]
			));
		}
		// check the NSTOP limit 	
		if (space.getLast()!=null) {
			if (result.counter-space.getLast().counter>nStop*swarm.getCount()) {
				stop();
				return;
			}
		}	
		// continue
		dispatch();		
	}
		

	
	@Override
	public String getResultsReport() {
		String out="";		
		try {
			out += String.format("<SwarmOptimizer>\n");
			out += String.format("WCOST=%f\n",costWeight);
			out += String.format("ACC=%s\n",cls.getField("ACC"));
			out += String.format("SPREAD=%s\n",cls.getField("SPREAD"));
			out += String.format("PEXP=%s\n",cls.getField("PEXP"));
			out += String.format("POWN=%s\n",cls.getField("POWN"));
			out += String.format("AVE=%s\n",cls.getField("AVE"));
			out += String.format("NSPEC=%s\n",cls.getField("NSPEC"));
			out += String.format("TRIALS=%s\n",counter);
			out += String.format("TIME=%f [h]\n",(System.currentTimeMillis()-time0)/3600.0/1000);
			out += "<sequence>\n";
			out += "<!-- "+space.getResultHeader()+" -->\n";
			out += space.getResultSequence();
			out += "</sequence>\n";
			out += "<result>\n";
			double[] target=swarm.getBest();
			double[] x0=null;
			double[] wv=null;
			double[] wb=null;
			CovMatrix stat = swarm.getStatistics();
			if (stat!=null) {
				x0=stat.getX0();
				wv=stat.getVANAD();	
				wb=stat.getBRAGG();	
			}
			if (wb!=null && wv!=null) {				
				out += "<!-- VAR BEST_VALUE MEAN_VALUE WIDTH_OUTER WIDTH_INNER -->\n";
				for (int i=0;i<space.getDim();i++) {
					out += String.format("%s\t%f\t%f\t%f\t%f\n",
						space.getVarName(i),
						target[i],x0[i],wv[i],wb[i]);
				}									
			} else {
				out += "<!-- VAR BEST_VALUE -->\n";
				
				for (int i=0;i<space.getDim();i++) {
					out += String.format("%s\t%f\n",
						space.getVarName(i),target[i]);
				}
			}			
			out += "</result>\n";
			out += String.format("</SwarmOptimizer>\n");
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return out;
	}
	
	/**
	 * Return best parameters as an input script for SIMRES
	 * @return
	 */
	public String getResultsScript() {
		String out="";		
		try {
			out += String.format("<SwarmOptimizer>\n");
			ProbeResult fm=space.getLast();
			if (fm != null) {
				out += "<!-- "+space.getResultHeader()+" -->\n";
				out+=fm.getResultString();
			} else {
				return null;
			}
			out += "<SCRIPT>\n";
			out += space.getSetCommand();
			/*
			double[] target=swarm.getBest();
			for (int i=0;i<space.getDim();i++) {
				String var=space.getVarName(i);
				String[] vars=var.split("([|]|[:])");
				double val=target[i];
				for (int j=0;j<vars.length;j++) {
					String[] varstr = vars[j].split("[.]");
					if (varstr.length>1) {
						out += String.format("set %s  %s  %f\n",varstr[0],varstr[1],val);
					}
				}
			}
			*/
			out += "</SCRIPT>\n";						
			out += String.format("</SwarmOptimizer>\n");
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return out;
	}
	
	@Override
	public void saveResult() {
		try {
			String fname=cls.getField("OUTPUT").valueToString();
			if (fname!=null && ! fname.equals("")) {
				Utils.writeStringToFile(
						program.getProjectList().getFullPath(ProjectList.PROJ_OUT, fname), 
						getResultsReport());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}	
	
	public void saveResultScript() {
		try {
			String fname=cls.getField("OUTPUT").valueToString();
			if (fname!=null && ! fname.equals("")) {
				String out=getResultsScript();
				if (out!=null) {
					int n=space.getResults().size();
					String cname=Utils.cutExtension(fname);
					String sname=String.format("%s_%d.scr",cname,n );
					Utils.writeStringToFile(
					program.getProjectList().getFullPath(ProjectList.PROJ_OUT, sname), 
							out);
				}				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}	

	public void setOptimizerParam(ClassData cls){
		this.cls=cls;
	}
	
	public Swarm getSwarm() {
		return swarm;
	}	
	
	private int calID(int iwasp, int iloop) {
		return iwasp+MAXSPECIES*iloop;
	}
	
	/**
	 * Calculate wasp ID from probe ID
	 * @param id
	 * @return
	 */
	private int getIWasp(int id) {
		return id % MAXSPECIES;
	}
	
	/**
	 * Calculate loop index from probe ID
	 * @param id
	 * @return
	 */
	protected int getILoop(int id) {		
		return (int) Math.floor(id/(1.0d*MAXSPECIES));
	}

	public boolean isChangedInstrument() {
		return changedInstrument;
	}

	public int getSpaceType() {
		try {
			spaceType=(Integer)cls.getField("SPACE").getValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return spaceType;
	}

	public double getCostWeight() {
		return costWeight;
	}

	public void setCostWeight(double costWeight) {
		this.costWeight = costWeight;
	}

	
}
