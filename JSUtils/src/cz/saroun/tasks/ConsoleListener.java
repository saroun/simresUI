package cz.saroun.tasks;


/**
 * This iterface should be implemented in classes that want
 * to receive console output of process that has been started
 * by "ProcessLauncher"
 *
 *
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2019/06/12 17:56:57 $</dt></dl>
 */
public interface ConsoleListener {
	public  void receive(String s);
}