package cz.restrax.sim.xml.reader;

import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.DefaultXmlHandler;
import cz.restrax.sim.ProjectList;

public class ProjectsHandler extends DefaultXmlHandler {
	private ProjectList plist;
	private ProjectsExh contentHandler=null;
	private boolean setCurrent=false;
	@Override
	public CallBackInterface getContentHandler() {
		if (contentHandler==null) {
			contentHandler=new ProjectsExh(xml,plist, setCurrent);
		}
		return contentHandler;
	}

	public ProjectsHandler(ProjectList plist, boolean setCurrent) {
		super();
		this.plist=plist;
		this.setCurrent=setCurrent;
	}

	@Override
	public String preProcessContent(String content) {
		return content;
	}


}
