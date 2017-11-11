package kassa.core.exceptions;

/**
 * Exception Class for Csv file input, when error while parsing csv
 * 
 * @author Stephen Pauwels
 */
public class CsvParserException extends CsvException {

	private static final long serialVersionUID = 1L;

	private int m_line;

	/**
	 * Constructor
	 * 
	 * @param filename
	 *            File that caused the exception
	 * @param line
	 *            Line of the error
	 */
	public CsvParserException(String filename, int line) {
		super(filename);
		m_line = line;
	}

	/**
	 * Get line of the error
	 * 
	 * @return int the line
	 */
	public int getLine() {
		return m_line;
	}

}
