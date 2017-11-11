package kassa.core.exceptions;

/**
 * Exception Class for Csv file input, when file could not be read as .csv file
 * 
 * @author Stephen Pauwels
 */
public class CsvFormatException extends CsvException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param filename
	 *            File that caused the exception
	 */
	public CsvFormatException(String filename) {
		super(filename);
	}

}
