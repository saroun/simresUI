package cz.restrax.sim.xml.reader;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.restrax.sim.ProjectList;
import cz.restrax.sim.RsxProject;
import cz.saroun.xml.CallBackInterface;
import cz.saroun.xml.XmlUtils;


/**
 * Read the project list from XML
 * @author   Jan Saroun
 * @version  <dl><dt>$Revision: 1.8 $</dt>
 *               <dt>$Date: 2018/11/20 17:54:55 $</dt></dl>
 */

public class ProjectsExh implements CallBackInterface {

	public static final String  ENAME = "SIMRES";

	private XmlUtils xml;
	private final ProjectList plist;
	private ProjectList mylist;
	private boolean setCurrent;

	public ProjectsExh(XmlUtils xml, ProjectList plist, boolean setCurrent) {
		this.xml     = xml;
		this.plist = plist;
		this.setCurrent = setCurrent;
	}

	public void startElement(String name, Attributes atts) throws SAXParseException {
		if (name.equals(ProjectsExh.ENAME)) {
			mylist=new ProjectList(plist.getProjectListFile());
		}
		else if (name.equals("PROJECT")) {
			xml.forwardToHandler(new ProjectExh(), name, atts);
		}
		else {
			xml.stopParsing(XmlUtils.WRONG_ELEMENT);
		}
	}
	
	public void endElement(String name) throws SAXParseException {
		try {
			if (name.equals(ProjectsExh.ENAME)) {	        	
				plist.assign(mylist);
				xml.removeHandler();
			} 
		   	else {
				xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
			}
		} catch (Exception ex) {
			xml.stopParsing(ex.getMessage());
		}
	}

	/*"***************************************************************************************
	*                                    PROJECT HANDLER                                      *
	*****************************************************************************************/
	private class ProjectExh implements CallBackInterface {

		private RsxProject prj; // local copy
		private boolean current=false;
	//	private Settings  settings = null;
		
		public ProjectExh() {				
		}
		public void startElement(String name, Attributes atts) throws SAXParseException {		
			if (name.equals("PROJECT")) {
				String[]  attNames = {"system","current"};
				xml.testAttributes(atts, 1, attNames);
				String[]  validVals = {"yes","no"};
				xml.testAttributes(atts, "system", validVals);
				xml.testAttributes(atts, "current", validVals);
				String sys=atts.getValue("system");
				String cur=atts.getValue("current");
				current=cur.equals("yes") && setCurrent;
				prj = new RsxProject();
				prj.setSystem(sys.equals("yes"));
			}
			else if (name.equals("CFGPATH") ||
					name.equals("DATPATH") ||
					name.equals("OUTPATH") ||
					name.equals("CFGFILE") ||
					name.equals("DESCR") ) {
			} 
			else {
				xml.stopParsing(XmlUtils.WRONG_ELEMENT);
			}
		}
		
		public void endElement(String name) throws SAXParseException {
			try {
				if (name.equals("PROJECT")) {
					// projects loaded from file are not system ones.
					//prj.setSystem(false);	
					prj.check();
					mylist.add(prj);
					if (current) {
						mylist.setAsCurrent(prj);
					}					
					xml.removeHandler();
				}				
			   	else if (name.equals("CFGPATH")) {
					prj.setPathProject(substitute(xml.getContent()));
				}				
				else if (name.equals("DATPATH")) {
					prj.setPathData(substitute(xml.getContent()));
				}				
				else if (name.equals("OUTPATH")) {
					prj.setPathOutput(substitute(xml.getContent()));
				}
				else if (name.equals("CFGFILE")) {
					prj.setFileConfig(substitute(xml.getContent()));
				}
				else if (name.equals("DESCR")) {
					prj.setDescription(substitute(xml.getContent()));
				}
			   	else {
					xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
				}
			} catch (Exception ex) {
				xml.stopParsing(ex.getMessage());
			}
		}
		
		/**
		 * Attempts to convert URI to a file path string
		 * @param uriStr
		 * @return
		 */
		protected String uriToPath(String uriStr) {
			File f;			
			try {
			  URI uri = new URI(uriStr);
			  f = new File(uri);
			} catch(URISyntaxException e) {
			  f = new File(uriStr);
			}
			if (f!=null) {
				return f.getPath();
			} else {
				return uriStr;
			}
		}
		
		protected String substitute(String inp) {
			String out=inp;
			if (plist.getMap()==null) return inp;
			for (String key : plist.getMap().keySet() ) {
				String s = plist.getMap().get(key);
			try {
				String rep=s.replaceAll("\\\\", "&");
				String path = out.replaceAll("\\\\","&");
				out = path.replaceAll(key, rep);
				String validPath=out.replaceAll("&","/");
				out=(new File(validPath)).getPath();
			} catch (Exception e){
				System.out.println(e.getMessage());
			}								
			}
			return out;
		}
	}

}