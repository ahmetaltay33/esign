package tr.com.ahmetaltay.esign.exception;

public class ValidationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ValidationException() {
		
	}

	public ValidationException(String message) {
		super(message);
		
	}

	public ValidationException(String detail, String message) {
		super(message + "\n" + detail);
		
	}	

	public ValidationException(Throwable cause) {
		super(cause);
		
	}

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public ValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	
	}

}
