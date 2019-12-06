package cz.restrax.sim.commands;

import java.io.File;
import java.util.HashMap;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.ClassDataCollection;
import cz.jstools.classes.ClassesCollection;
import cz.jstools.classes.editors.CommandHandler;
import cz.restrax.sim.ProjectList;
import cz.restrax.sim.SimresCON;

/**
 * Collects information about all commands and their groups.
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.8 $</dt>
 *               <dt>$Date: 2017/11/02 20:15:38 $</dt></dl>
 */
public class Commands {
	private static final String DEFAULT_GROUP="Commands";
	
	/**
	 * Defined handler ID's
	 */
	protected static final String[] DEFNAMES={"SIMRES", "SWARM", "STHND", "LOAD", "SAVECMP"};
	private ClassDataCollection commands = null;
	private HashMap<String,ClassDataCollection> groups = null;
	private final SimresCON program;
	private final HashMap<String,CommandHandler> handlers;
	
	public Commands(SimresCON program) {	
		this.program=program;
		commands = new ClassDataCollection("COMMANDS","cmd",ClassData.UPDATE_NO); 
		groups = new HashMap<String,ClassDataCollection>();
		handlers=new HashMap<String,CommandHandler>();		
	}
	
	public void setClasses(ClassesCollection classes) {	
		if (classes != null) classes.addCommands(commands);
		sortToGroups();		
	}
	
	/**
	 * Handle command given in the format cmd=ID.ACTION, where<br />
	 * <UL>
	 * <LI>ID = id of a ClassData object with command data</LI>
	 * <LI>ACTION = action name</LI>
	 * </UL>
	 * If there is no dot separator, then
	 * <UL>
	 *    <LI>if cmd is an ID of a command class, use ID=cmd, ACTION=Execute </LI>
	 *    <LI>else use ID=null, ACTION=cmd </LI>
	 * </UL>
	 * @param cmd command string
	 * @param cmd optional argument passed to non-class commands
	 */	
	public void handleCommand(String cmd,Object arg) {
		String[] s = cmd.split("[.]");
		ClassData cls=null;
		String action="";
		cls=getCommand(s[0]);
		if (s.length>1) {
			action=s[1];
		} else {
			if (cls!=null) {
				action="Execute";
			} else {
				action=s[0];
			}			
		}
		if (cls!= null) {
			CommandHandler hnd=getCmdHandler(cls);
			if (hnd!=null) {
				hnd.handle(action, cls);
			}
			
		} else {
			getCmdHandler().handle(action, arg);
		}		
	}

	/**
	 * Generate handler for given object. By default, returns 
	 * StdClassHandler if obj=ClassData, otherwise SimresHandler.
	 * Special handlers are returned for some commands like SWARM. 
	 * @param obj
	 * @return
	 */
	public CommandHandler getCmdHandler(Object obj) {
		if (obj instanceof ClassData) {
			return getHandler(((ClassData) obj).getId());
		} else {
			return getHandler("SIMRES");
		}		
	}
	/**
	 * Generate SimresHandler object.	
	 */
	public SimresHandler getCmdHandler() {
		return (SimresHandler) getHandler("SIMRES");		
	}
	
	/**
	 * Return handler for given ID (lazy creator)
	 * ID can be either command class id, or one of the pre-defined words: 
	 * SIMRES, STDHND. <br>
	 * For a class. getHandler is called to get the handler ID. Allowed IDs are:
	 * <p>
	 *   SIMRES ... SimresHandler<br>
	 *   STDHND ... StdClassHandler<br>
	 *   SWARM  ... SwarmHandler<br>
	 *   LOAD   ... LoadHandler<br>
	 *   CSAVE  ... CSaveHandler<br>   
	 *   else return null
	 * </p>
	 * Store reference to the created handlers for later use.
	 * @param id
	 * @return
	 */
	private CommandHandler getHandler(String id) {
		String hndID=id;
		ClassData cls=getCommand(id);
		// For command class, try to retrieve a user handler ID.
		// Use STDHND by default.
		if (cls!=null) {
			hndID=cls.getClassDef().getHandler();
			if (hndID==null) hndID="STDHND";		
		}
		// create the required handler if not yet done
		if (handlers.get(hndID)==null) {
			// standard SIMRES (non-class) handler
			if (hndID.equals("SIMRES")) {
				handlers.put(hndID, new SimresHandler(program));
			// standard class handler
			} else if (hndID.equals("STDHND")) {
				handlers.put(hndID, new StdClassHandler(program));
			// specialized handlers
			} else if (hndID.equals("SWARM")) {
				handlers.put(hndID, new SwarmHandler(program));
			} else if (hndID.equals("LOAD")) {
				handlers.put(hndID, new LoadHandler(program));
			} else if (hndID.equals("CSAVE")) {
				handlers.put(hndID, new CSaveHandler(program));
			} else if (hndID.equals("CLOAD")) {
				handlers.put(hndID, new CSaveHandler(program));
			}				
		}
		return handlers.get(hndID);
	}
	
	
	public void sortToGroups() {
		ClassDataCollection cc;
		ClassData cmd;
		String grp;
		groups.clear();
		cc=new ClassDataCollection(DEFAULT_GROUP,"cmd",ClassData.UPDATE_NO);
		groups.put(DEFAULT_GROUP,cc);
   // sort commands into groups
		for (int i=0;i<commands.size();i++) {			
			cmd = commands.get(i);
			grp=cmd.getClassDef().getGroup();
			if (grp.equals("")) grp=DEFAULT_GROUP;
			if (groups.containsKey(grp)) {
				groups.get(grp).addNew(cmd);
			} else {
				cc=new ClassDataCollection(grp,"cmd",ClassData.UPDATE_NO);
				groups.put(grp,cc);
				groups.get(grp).addNew(cmd);
			}	
		}
	}
	
	/**
	 * Hides all commands.
	 */
	public void clear() {
		for (int i=0;i<commands.size();i++) {			
			commands.get(i).setHidden(true);
		}
		handlers.clear();
	}

	public HashMap<String, ClassDataCollection> getGroups() {
		return groups;
	}

	public ClassDataCollection getCommands() {
		return commands;
	}
	
	public ClassData getCommand(String id) {
		return commands.get(id);
	}	
	
	public void setGroupVisible(String groupName,boolean isVisible) {
		if (groups.containsKey(groupName)) {
			groups.get(groupName).setHidden(! isVisible);
		}
	}
	
}
