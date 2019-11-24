package cz.restrax.gui.windows;


import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import cz.restrax.gui.SimresGUI;
import cz.saroun.classes.ClassData;
import cz.saroun.classes.ClassDataCollection;
import cz.saroun.classes.editors.CommandPane;
import cz.saroun.classes.ieditors.ICommandEditor;


/**
 * Menu object for ClassDataCollection
 * Each item opens a ClassData editor for corresponding ClassData object
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.10 $</dt>
 *               <dt>$Date: 2014/08/23 20:40:50 $</dt></dl>
 */
public class DataCollectionMenu extends JMenu {
	private static final long serialVersionUID = 3481673932923852339L;
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTANTS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private ClassDataCollection classCollection = null;
	private HashMap<String, ICommandEditor> dialogs = null;
	private SimresGUI  program                        = null;

	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public DataCollectionMenu(SimresGUI program,ClassDataCollection classCollection) {
		super();
		this.program = program;
	//	classCollection = new ClassDataCollection("");
		dialogs = new HashMap<String,ICommandEditor>();
		setClassCollection(classCollection);
	//	updateContents();
	}
	
	public void updateContents() {
		if (classCollection != null) {
		this.removeAll();
// re-create the menu structure
		for (int i=0;i<classCollection.size();i++) {
			ClassData cls=classCollection.get(i);
			if (! cls.isHidden()) {
				JMenuItem  m = new JMenuItem();
				m.setText(cls.getName());
				m.addActionListener(new ClassListener(cls));
				this.add(m);
			}
		}
// close dialogs which have data not defined in classCollection
		for (String s : dialogs.keySet()) {
			ICommandEditor d = dialogs.get(s);
			if (classCollection.get(s)==null) {
				d.closeDialog();
				dialogs.remove(d);
				d=null;
			} else {
				d.getPane().updatePropertyEditors();
			}
		}
// remove all items
	//	dialogs.clear();
		// if (program.getDesktop()!=null)	program.getDesktop().repaint();
		}
	}
	

    /**
     * Opens property editor for given class
     *
     */
    public class ClassListener implements ActionListener {
    	private ClassData c = null;
    	public ClassListener(ClassData cls) {
    		super();
    		this.c=cls;
    	}
    	public void actionPerformed(ActionEvent e) {
    		if (! getDialogs().containsKey(c.getId())) {
    			Point origin = new Point(0,0);    			
    			CommandPane pn = new CommandPane(program.getGuiExecutor(),c,false);
    			pn.InitProperties();
    			ICommandEditor ced = new ICommandEditor(origin,program.getDesktop());
    			ced.InitProperties(pn);
    			getDialogs().put(c.getId(), ced);
    		}
    		getDialogs().get(c.getId()).showDialog();
    	}
    }
    
	public void setClassCollection(ClassDataCollection classCollection) {
		this.classCollection = classCollection;
		updateContents();
	}

	public HashMap<String, ICommandEditor> getDialogs() {
		return dialogs;
	}

}
