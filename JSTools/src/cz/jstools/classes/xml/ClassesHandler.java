package cz.jstools.classes.xml;

import cz.jstools.classes.ClassesCollection;
import cz.jstools.classes.EnumCollection;
import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.DefaultXmlHandler;

public class ClassesHandler  extends DefaultXmlHandler {
	private final EnumCollection enumTypes;
	private final ClassesCollection classes;
	
	/**
	 * Use this class only through the DefaultXmlLoader ...
	 * @param reader_tag
	 */
	public ClassesHandler(String reader_tag) {
		super();
		enumTypes=new EnumCollection();
		classes = new ClassesCollection(reader_tag);
	}
	
	public EnumCollection getEnumTypes() {
		return enumTypes;
	}

	public ClassesCollection getClasses() {
		return classes;
	}

	@Override
	public CallBackInterface getContentHandler() {		
		return new ClassesExh(xml,enumTypes,classes);
	}

	@Override
	public String preProcessContent(String content) {
		return content;
	}


}
