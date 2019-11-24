package cz.restrax.sim.xml.reader;

//import cz.restrax.sim.Instrument;
import cz.restrax.sim.SimresCON;
import cz.saroun.xml.CallBackInterface;
import cz.saroun.xml.DefaultXmlHandler;

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
