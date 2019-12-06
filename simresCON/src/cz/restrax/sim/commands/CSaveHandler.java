package cz.restrax.sim.commands;

import java.io.File;
import java.io.IOException;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.definitions.Utils;
import cz.restrax.sim.Instrument;
import cz.restrax.sim.ProjectList;
import cz.restrax.sim.SimresCON;

public class CSaveHandler extends StdClassHandler {

	public CSaveHandler(SimresCON program) {
		super(program);
	}

	public void handleClass(String action,ClassData cls) {		
		if (action.equalsIgnoreCase("SAVE")) {
			try {
				String fname=cls.getField("FILE").toString();
				String cname = cls.getField("ID").toString();
				String fullName = program.getProjectList().getFullPath(ProjectList.PROJ_CFG_COMP, fname);
				String fileContent = program.prepareComponentXml(cname);
				if (fileContent.length()>0) {
					try {
						Utils.writeStringToFile(fullName, fileContent);
						String msg=String.format("Component %s saved in %s", cname,fullName);
						program.getMessages().infoMessage(msg, "low");
					} catch (IOException e) {
						program.getMessages().errorMessage("Can''t write in file "+fullName, "low", "SaveHandler");
					}
				} else {
					String msg=String.format("Nothing to save, component %s", cname);
					program.getMessages().warnMessage(msg, "low");
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}

		}	
		if (action.equalsIgnoreCase("LOAD")) {
			try {
				String fname=cls.getField("FILE").toString();
				String cname = cls.getField("ID").toString();
				ClassData comp = program.getSpectrometer().getComponent(cname,Instrument.TYPE_COMPONENT);
				if (comp != null) {
					String fullName = program.getProjectList().getFullPath(ProjectList.PROJ_CFG_COMP, fname);
					File f = new File(fullName);
					program.getExecutor().loadComponent(f, comp);
				} else {
					String msg=String.format("Component %s not found", cname);
					program.getMessages().warnMessage(msg, "low");
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}

		}	
	}


}