package cz.jstools.classes.editors;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.definitions.FileAccess;
 
/**
 * Like CommandExecutor, but implements some methods needed for commands GUI interface 
 *
 */
public interface CommandGuiExecutor extends CommandExecutor{

/**
* Create file chooser with given attributes.
*/
public ActionListener getActionListener(String action, ClassData cls);

	
/**
 * Create file chooser with given attributes.
 */
public FileSelectDialog getFileSelectDialog(Component parent, FileAccess access, String id, String filter, String hint);

/**
 * Response to a "SHOW" command, eg. open graph or 3D view of the object
 * @param obj
 */
public void showView(Object obj);


/**
 * Return ImageIcon for given object from resources 
 * @param obj should be ClassPane or descendant
 * @return
 */
public ImageIcon getClassIcon(Object obj);


}

