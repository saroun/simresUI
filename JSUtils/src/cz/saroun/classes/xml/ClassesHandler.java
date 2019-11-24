package cz.saroun.classes.xml;

import cz.saroun.classes.ClassesCollection;
import cz.saroun.classes.EnumCollection;
import cz.saroun.xml.CallBackInterface;
import cz.saroun.xml.DefaultXmlHandler;

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
