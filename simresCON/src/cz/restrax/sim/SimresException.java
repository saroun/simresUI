package cz.restrax.sim;

/**
 * Wraps all SIMRES specific exceptions and provides methods for showing error messages
 * in SIMRES environment.
 * @author Jan Saroun, saroun@ujf.cas.cz
 *
 */
@SuppressWarnings("serial")
public class SimresException extends Exception {
	public static final int LOW=0;
	public static final int HIGH=1;
	private final int priority;
	private final SimresCON program;
	private final String source;
	
	public SimresException(SimresCON program, String message, Object source, int priority) {
		super(message);
		this.program = program;
		if (priority<LOW || priority>HIGH) {
			this.priority = LOW;
		} else {
			this.priority = priority;
		}
		this.source = source.getClass().getSimpleName();
	}
	
	public void showMessage() {
		String s = "low";
		if (priority==HIGH) s ="high";
		program.getMessages().errorMessage(getMessage(), s, source);
	}
	
	public void printToConsole() {
		System.err.format("%s: %s\n", source, getMessage());
	}
}
