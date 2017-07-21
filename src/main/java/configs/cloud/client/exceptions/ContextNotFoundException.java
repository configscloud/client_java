package configs.cloud.client.exceptions;

public class ContextNotFoundException extends Exception {
	  public ContextNotFoundException() { super(); }
	  public ContextNotFoundException(String message) { super(message); }
	  public ContextNotFoundException(String message, Throwable cause) { super(message, cause); }
	  public ContextNotFoundException(Throwable cause) { super(cause); }
}
