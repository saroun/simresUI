package cz.restrax.sim.xml.reader;

//import cz.restrax.sim.Instrument;
import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.DefaultXmlHandler;
import cz.restrax.sim.SimresCON;

public class InstrumentHandler extends DefaultXmlHandler {
	protected final SimresCON program;
	protected final boolean autoUpdate;
	protected InstrumentExh contentHandler=null;
	protected boolean canRedefine=true;
	public InstrumentHandler(boolean autoUpdate, SimresCON program, boolean canRedefine) {
		super();
		this.autoUpdate=autoUpdate;		
		this.program=program;
		this.canRedefine=canRedefine;
	}
	@Override
	public CallBackInterface getContentHandler() {
		if (contentHandler==null) {
			contentHandler=(InstrumentExh) program.createExh(xml, "SIMRES");
			contentHandler.setOptions(autoUpdate, canRedefine);
			//contentHandler=new InstrumentExh(xml,autoUpdate,this.program);
		}
		return contentHandler;
	}
	@Override
	public String preProcessContent(String content) {
		String out=content.replaceAll("<([\\/]*)TRACING", "<$1OPTION");
		out=out.replaceAll("<([\\/]*)REPORTS", "<$1OPTION");
		return out;
	}
	
	/*
	public void updateInstrument(Instrument instrument) {
		if (contentHandler!=null) contentHandler.updateInstrument(instrument);	
	}
	
	public void updateAll(Instrument instrument) {
		if (contentHandler!=null) contentHandler.updateAll();		
	}
	*/
	
	public boolean isRedefined() {
		if (contentHandler==null) {
			return false;
		} else
		return ( contentHandler.isValid() && contentHandler.isRedefine());
	}
	
	public void updateCommands(boolean b) {
		if (contentHandler!=null) contentHandler.updateCommands(b);
		
	}
	public void updateOptions() {
		if (contentHandler!=null) contentHandler.updateOptions();
		
	}
	
	/*
	private boolean isValid() {
		if (contentHandler!=null) {
			return contentHandler.isValid();
		} else return false;
	}
   */
}
