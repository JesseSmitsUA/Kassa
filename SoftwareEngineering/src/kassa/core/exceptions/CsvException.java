package kassa.core.exceptions;

/**
 * Exception Class for Csv file input
 * 
 * @author Stephen Pauwels
 */
public class CsvException extends Exception {

	private static final long serialVersionUID = 1L;

	private String m_file;

	/**
	 * Constructor
	 * 
	 * @param filename
	 *            File that caused the exception
	 */
	public CsvException(String filename) {
		m_file = filename;
	}

	/**
	 * Return the file that caused the exception
	 * 
	 * @return String The file
	 */
	public String getFile() {
		return m_file;
	}

}
