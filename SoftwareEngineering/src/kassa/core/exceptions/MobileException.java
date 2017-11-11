package kassa.core.exceptions;

/**
 * Exception Class for Mobile system
 * 
 * @author Stephen Pauwels
 */
public class MobileException extends Exception {

	private static final long serialVersionUID = 1L;

	private String m_message;

	/**
	 * Constructor
	 * 
	 * @param message	Message to display
	 */
	public MobileException(String message) {
		m_message = message;
	}

	/**
	 * Return the file that caused the exception
	 * 
	 * @return String The file
	 */
	public String getFile() {
		return m_message;
	}

}
