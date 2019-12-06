package cz.restrax.sim.xml.reader;

//import cz.restrax.sim.Instrument;
import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.DefaultXmlHandler;
import cz.restrax.sim.SimresCON;

public class ComponentHandler extends DefaultXmlHandler {
	protected final SimresCON program;
	protected final String compID;
	protected ComponentExh contentHandler=null;
	public ComponentHandler(SimresCON program, String compID) {
		super();
		this.compID=compID;		
		this.program=program;
	}
	@Override
	public CallBackInterface getContentHandler() {
		if (contentHandler==null) {
			contentHandler=new ComponentExh(xml, program, compID);
		}
		return contentHandler;
	}
	@Override
	public String preProcessContent(String content) {
		return content;
	}

}
