package cz.restrax.sim.xml.writer;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.ClassDataCollection;
import cz.jstools.classes.xml.ClassCollectionsXmlExport;
import cz.restrax.sim.Instrument;
import cz.restrax.sim.SimresCON;




/**
 * Exports Instrument config. data, commands and repository components to XML files
 * @author   Jiri Svoboda, Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.9 $</dt>
 *               <dt>$Date: 2019/06/27 20:33:29 $</dt></dl>
 */
public class TasConfigXmlExport extends ClassCollectionsXmlExport {
	private final Instrument instrument;
	private final SimresCON program;
	
	public TasConfigXmlExport(SimresCON program) {
		super("SIMRES");
		this.program=program;
		this.instrument=this.program.getSpectrometer();
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  OTHER METHODS                                       //
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns XML text with instrument setup. 
	 * @param inclAll set true if the export should include additional information such as HTML or SCRIPT sections.
	 * @return
	 */
	public String exportToXml(boolean inclAll) {
		String    output="";
		String tt="";
		output = getProlog(program.getVersion().getRestraxVersion());
	//	tt += "\t";
		boolean isDemo = program.getProjectList().getCurrentProject().isSystem();
	// HTML
		if (inclAll && ! instrument.getHtml().isEmpty() && isDemo) {
			output += tt+ "<HTML>\n";
			output += tt+ " <![CDATA[\n";
			output += tt+ instrument.getHtmlText()+ "\n";
			output += tt+ "]]>\n";
			output += tt+ "</HTML>\n";
		}
	// COMMANDS	
		output += prepareCollection(tt,program.getCommands().getCommands());
	// OPTIONS
		output += prepareCollection(tt,program.getOptions());
	// INSTRUMENT
		output += tt+ "<INSTRUMENT>\n";
		output += tt+ "\t<CFGTITLE>"+instrument.getCfgTitle()+"</CFGTITLE>\n";
		if (instrument.getMonochromators().size()>0) {
			output += tt+"\t"+prepareStrVector("MONOCHROMATORS",
					instrument.getMonochromators());
		}
		if (instrument.getAnalyzers().size()>0) {
			output += tt+"\t"+prepareStrVector("ANALYZERS",
					instrument.getAnalyzers());
		}
		output += prepareCollection(tt+"\t",instrument.getInterface());
		if (instrument.getSpecimen().size()>0) {
			output += prepareCollectionEx(tt+"\t",instrument.getSpecimen(),"SPECIMEN");
		}
		output += prepareCollection(tt+"\t",instrument.getPrimarySpec());
		if (instrument.getSecondarySpec().size()>0) {
			output += prepareCollection(tt+"\t",instrument.getSecondarySpec());
		}
		output += tt+ "</INSTRUMENT>\n";
// script
		if (inclAll &&! program.getScript().isEmpty()) {
			output += tt+ "<SCRIPT type=\"command\">\n";
			output += program.getScript().getScript();
			output += tt+ "</SCRIPT>\n";
			
		}
		output += getEpilog();
		return output;
	}
	
	
	public String exportCommandToXml(String id) throws Exception {
		String    output="";
		ClassData cls = program.getCommands().getCommand(id);
		if (cls != null) {
			String tt="";
			output = getProlog(program.getVersion().getRestraxVersion());
			tt += "\t";
			output += tt+ "<COMMANDS>\n";
			output += tt+ prepareClassXml(cls,"\t\t",false);
			output += tt+ "</COMMANDS>\n";
			output += getEpilog();
		}		
		return output;
	}
	
	private String exportComponentToXml_old(String id) throws Exception {
		String    output="";
		ClassData cls = instrument.getComponent(id,Instrument.TYPE_COMPONENT);
		String colID = instrument.getComponentCollection(id);
		if (cls != null && colID !=null) {
			String tt="";
			output = getProlog(program.getVersion().getRestraxVersion());
			tt += "\t";
			output += tt+ "<INSTRUMENT redefine=\"no\">\n";			
			output += tt+tt+ String.format("<%s>\n",colID);			
			output += tt+tt+ prepareClassXml(cls,"\t\t",false);
			output += tt+tt+ String.format("</%s>\n",colID);
			output += tt+ "</INSTRUMENT>\n";
			output += getEpilog();
		}		
		return output;
	}
	
	public String exportComponentToXml(String id) throws Exception {
		String    output="";
		ClassData cls = instrument.getComponent(id,Instrument.TYPE_COMPONENT);
		if (cls != null) {
			//String tt="";
			output = getProlog(program.getVersion().getRestraxVersion());
			//tt += "\t";		
			output += prepareClassXml(cls,"\t",false);
			output += getEpilog();
		}		
		return output;
	}
	
	public String exportRepositoryToXml()  {
		String output="";
		String tt="\t";
		output = getProlog(program.getVersion().getRestraxVersion());
		output += tt+ "<REPOSITORY>\n";
		ClassDataCollection[] collections = program.getRepository().getData();
		for (int ic=0;ic<collections.length;ic++) {
			output += prepareCollectionRep(tt+"\t",collections[ic]);		
		}
		output += tt+ "</REPOSITORY>\n";
		output += getEpilog();
		return output;
	}
	

}
