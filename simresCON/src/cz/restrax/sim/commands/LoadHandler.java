package cz.restrax.sim.commands;

import cz.jstools.classes.ClassData;
import cz.restrax.sim.ProjectList;
import cz.restrax.sim.SimresCON;

public class LoadHandler extends StdClassHandler {

	public LoadHandler(SimresCON program) {
		super(program);
	}

	public void handleClass(String action,ClassData cls) {		
		try {
			String fname=cls.getField("FILE").toString();
			String fullName = program.getProjectList().getFullPath(ProjectList.PROJ_CFG, fname);
			if (action.equalsIgnoreCase("LOAD")) {
				program.getCommands().handleCommand(SimresHandler.CMD_LOAD, fullName);	
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}