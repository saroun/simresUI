package cz.saroun.classes.editors;


public interface CommandExecutor {
  public void executeCommand(String cmd, boolean log, boolean record);
  public void executeCommand(String cmd);
  public void fireAction(String action);
  /**
   * Return CommandHandler for an object.
   * @param obj	data to be passed to the action listener
   */
  public CommandHandler getCommandHandler(Object obj);  
}

