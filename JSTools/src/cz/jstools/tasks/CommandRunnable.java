package cz.jstools.tasks;


/**
 * Extends TaskRunnable to allow easy handling of commands with arguments. 
 * Child classes must implement task() to handle given command. 
 * @author Jan Saroun, saroun@ujf.cas.cz
 */

abstract public class CommandRunnable extends TaskRunnable {
	private final String command;
	private final String[] args;
	/**
	 * Creates CommandRunnable with given command string with arguments.
	 * @param key TaskRunnable key
	 * @param label TaskRunnable label
	 * @param command Single string command
	 * @param args Command arguments as an array of strings.
	 * @see TaskRunnable
	 */
	public CommandRunnable(String key, String label, String command, String[] args) {
		super(key, label);
		this.command = command;
		this.args = args;
	}
	public String getCommand() {
		return command;
	}
	public String[] getArgs() {
		return args;
	}
}
