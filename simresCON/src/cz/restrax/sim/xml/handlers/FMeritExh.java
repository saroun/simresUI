package cz.restrax.sim.xml.handlers;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.XmlUtils;
import cz.restrax.sim.SimresCON;
import cz.restrax.sim.opt.SimResult;

/**
 * Xml handler for element <code>FMERIT</code>.
 *
 *
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.8 $</dt>
 *               <dt>$Date: 2015/08/20 17:29:09 $</dt></dl>
 */
public class FMeritExh implements CallBackInterface {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    CONSTANTS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public static final String  ENAME = "FMERIT";

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private XmlUtils    xml      = null;
	private SimresCON  program  = null;
	private SimResult result;

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTRUCTORS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	public FMeritExh(XmlUtils xml, SimresCON program) {
		this.xml     = xml;
		this.program = program;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                IMPLEMENTED INTERFACES                                //
	//////////////////////////////////////////////////////////////////////////////////////////
	public void startElement(String name, Attributes atts) throws SAXParseException {
		if (name.equals(FMeritExh.ENAME)) {
			String[] attNames = {"type", "formula","id"};			
			xml.testAttributes(atts, 3, attNames);
			int id = Integer.parseInt(atts.getValue("id"));
			double[] data=null;
			if (id>=0) data=program.getSwarmOptimizer().getSpace().getVariableValues();
			result=new SimResult(id,0.0d,System.currentTimeMillis(),data);
			result.formula=Integer.parseInt(atts.getValue("formula"));
			result.typ=Integer.parseInt(atts.getValue("type"));			
		} else if (name.equals("FM")) {				
		} else if (name.equals("EFM")) {			
		} else if (name.equals("INT")) {			
		} else if (name.equals("EINT")) {			
		} else if (name.equals("WIDTH")) {			
		} else if (name.equals("EWIDTH")) {			
		} else {
			xml.stopParsing(XmlUtils.WRONG_ELEMENT);
		}
	}
	
	public void endElement(String name) throws SAXParseException {
		if (name.equals(FMeritExh.ENAME)) {
			if (result.INT<=0.0d) result.FM=0.0d;
			xml.removeHandler();
			if (result.id>=0) {	
				/*
				program.getConsoleLog().print(String.format(
					    "Handle result id=%d  FM=%f  data[0]=%f\n",
							result.id,result.FM,result.data[0]	
				));	*/
				program.getSwarmOptimizer().getSpace().receive(result);
			} else {
				program.getResultsLog().printFValue("Figure of merit", 
				String.format("%f",result.FM), String.format("%f",result.eFM),"");
			}
			xml.getContent();
		} else if (name.equals("FM")) {	
			result.FM=Double.parseDouble(xml.getContent());
		} else if (name.equals("EFM")) {	
			result.eFM=Double.parseDouble(xml.getContent());
		} else if (name.equals("INT")) {	
			result.INT=Double.parseDouble(xml.getContent());
		} else if (name.equals("EINT")) {	
			result.eINT=Double.parseDouble(xml.getContent());
		} else if (name.equals("WIDTH")) {	
			result.WIDTH=Double.parseDouble(xml.getContent());
		} else if (name.equals("EWIDTH")) {	
			result.eWIDTH=Double.parseDouble(xml.getContent());
		}
		else {
			xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
		}
	}
}