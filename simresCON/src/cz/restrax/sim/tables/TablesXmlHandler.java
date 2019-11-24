package cz.restrax.sim.tables;

import cz.saroun.xml.CallBackInterface;
import cz.saroun.xml.DefaultXmlHandler;

public class TablesXmlHandler extends DefaultXmlHandler {
	private TablesListExh contentHandler=null;
	Tables data;
	boolean append;
	public TablesXmlHandler(Tables data, boolean isUserTable) {
		super();
		this.data=data;
		this.append = isUserTable;
	}
	@Override
	public CallBackInterface getContentHandler() {
		if (contentHandler==null) {
			contentHandler=new TablesListExh(xml,data,append);			
		}
		return contentHandler;
	}

	@Override
	public String preProcessContent(String content) {
		return content;
	}

}

