package cz.saroun.utils;

public interface LoggerInterface {
	public void print(String text);
	public void println(String text);
	public boolean isEnabled();
	public void setEnabled(boolean enabled);
	//public void clear();
}
