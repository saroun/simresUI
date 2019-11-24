package cz.restrax.sim.xml.writer;

import cz.restrax.sim.ProjectList;
import cz.restrax.sim.RsxProject;
import cz.restrax.sim.Version;


/**
 * Exporting project information to XML format.
 * @author   Jan Saroun

 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.5 $</dt>
 *               <dt>$Date: 2013/02/07 15:30:10 $</dt></dl>
 */
public class ProjectXmlExport {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  OTHER METHODS                                       //
	//////////////////////////////////////////////////////////////////////////////////////////
	public static String exportToXml(RsxProject proj, boolean current) {
		String output   = null;
		output  = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		output += "<SIMRES version=\""+Version.VERSION+"\">\n";
		output += getProjectXml(proj,current);
		output += "</SIMRES>\n";	
		return output;
	}

	/**
	 * Generate XML content for the project list. Puts the current project first. 
	 * Excludes system projects if required.  
	 * @param proj
	 * @return
	 */
	public static String exportListToXml(ProjectList proj, boolean excludeSystem) {
		String output   = null;
		output  = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		output += "<SIMRES version=\""+Version.VERSION+"\">\n";
		output += getProjectXml(proj.getCurrentProject(),true);
		for (int i=0;i<proj.size();i++) {
			if (! proj.get(i).isSystem() || ! excludeSystem) {
				if (proj.get(i)!=null && proj.get(i)!=proj.getCurrentProject()) {
					output += getProjectXml(proj.get(i),false);
				}
			}
		}
		output += "</SIMRES>\n";
		return output;
	}
	
	/**
	 * Generate XML output for the project data
	 * @param proj
	 * @return
	 */
	protected static String  getProjectXml(RsxProject proj,boolean current) {
		String output="";
		String fmt="\t\t<%s>%s</%s>\n";
		if (proj==null) return output;
		String cur = current ? "yes" : "no";
		String sys = proj.isSystem() ? "yes" : "no";
		output=String.format("\t<PROJECT system=\"%s\" current=\"%s\">\n",sys,cur);
		output += String.format(fmt,"CFGPATH",proj.getPathProject(),"CFGPATH");
		output += String.format(fmt,"DATPATH",proj.getPathData(),"DATPATH");
		output += String.format(fmt,"OUTPATH",proj.getPathOutput(),"OUTPATH");
		output += String.format(fmt,"CFGFILE",proj.getFileConfig(),"CFGFILE");
		output += String.format(fmt,"DESCR",proj.getDescription(),"DESCR");
		output += String.format("\t</PROJECT>\n");
		return output;
	}
}
