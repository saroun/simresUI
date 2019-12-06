package cz.restrax.sim.commands;

import cz.jstools.classes.ClassData;
import cz.restrax.sim.SimresCON;
import cz.restrax.sim.opt.GuideSpace;
import cz.restrax.sim.opt.SimSpace;
import cz.restrax.sim.opt.SwarmOptimizer;

public class SwarmHandler extends StdClassHandler {		
	public SwarmHandler(SimresCON program) {
		super(program);
	}

	public void handleClass(String action,ClassData cls) {		
		program.getMessages().debugMessage("SwarmHandler action="+action+"\n");
		if (action.equals("Run")) {
			if (! program.getSwarmOptimizer().isRunning()) {
				program.getSwarmOptimizer().setOptimizerParam(cls);
				int typ=program.getSwarmOptimizer().getSpaceType();
				SimSpace space=null;
				if (typ==SwarmOptimizer.TYPE_ALLPARAM) {
					space= new SimSpace(program);
				} else if (typ==SwarmOptimizer.TYPE_GUIDES) {
					space= new GuideSpace(program);
				} else {
					program.getMessages().errorMessage("Unknown variable space type.", "low", "SwarmOptimizer");
					return;
				}
				// TestSpace space = new TestSpace();
				// space.defineTestSpace(20);
				program.getSwarmOptimizer().setSpace(space);
				program.getSwarmOptimizer().start();
			}
		} else if (action.equals("Stop")) {
			if (program.getSwarmOptimizer().isRunning()) {
				program.getSwarmOptimizer().stop();
			}
		}  else if (action.equals("Continue")) {
			if (! program.getSwarmOptimizer().isRunning()) {
			program.getSwarmOptimizer().cont();
		}
	}
	}


}
