package cz.restrax.sim.xml.reader;

import java.util.HashMap;

import cz.jstools.classes.ClassDataCollection;
import cz.jstools.classes.ClassesCollection;
import cz.jstools.xml.DefaultXmlHandler;
import cz.restrax.sim.SimresCON;

public class RepositoryHandler extends DefaultXmlHandler {
	public RepositoryHandler(SimresCON program) {
		super();
		this.program=program;
		this.classes=this.program.getClasses();
	}
	private SimresCON program;
	private RepositoryExh contentHandler=null;
	private final ClassesCollection classes;
	private HashMap<String,ClassDataCollection> repository=null;
	@Override
	public RepositoryExh getContentHandler() {
		if (contentHandler==null) {
			contentHandler=new RepositoryExh(xml,getRepository(),classes);
			contentHandler.setInitialData(program.getDefaultComponents());
		}
		return contentHandler;
	}
	
	private HashMap<String,ClassDataCollection> getRepository() {
		if (repository==null) {
			repository=new HashMap<String,ClassDataCollection>();
		}
		return repository;
	}
	
	private boolean isValid() {
		return getContentHandler().isValid();
	}
	
	/**
	 * Return all found class collections (groups) as an array of ClassDataCollection objects.	 * 
	 */
	public ClassDataCollection[] getData() {
		ClassDataCollection[] collection=new ClassDataCollection[getRepository().size()];
		int i=0;
		for (String s : getRepository().keySet()) {
			collection[i]=getRepository().get(s);
			i++;
		}
		return collection;
	}
	
	/**
	 * Return ClassDataCollection object of given name. If not found, return null.  
	 */
	public ClassDataCollection getCollection(String name) {
		return getRepository().get(name);
	}
	
	
	/**
	 * Replace existing repository with object in collections
	 * @param collections new repository objects
	 */
	public void importClasses(ClassDataCollection[] collections) {
		repository.clear();
		for (int i=0;i<collections.length;i++) {
			repository.put(collections[i].getName(), collections[i]);
		}
	}
	
	public int getSize() {
		int n=0;
		ClassDataCollection col;
		if (repository != null) {
			int i=0;
			for (String s : getRepository().keySet()) {
				col=getRepository().get(s);
				if (col != null) n += col.size();
				i++;
			}
		}
		return n;
	}

	@Override
	public String preProcessContent(String content) {
		return content;
	}
	

}
