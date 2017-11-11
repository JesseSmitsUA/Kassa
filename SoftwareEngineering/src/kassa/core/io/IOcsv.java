package kassa.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;

import kassa.core.exceptions.CsvFormatException;
import kassa.core.exceptions.CsvParserException;
import kassa.core.exceptions.ItemsException;
import kassa.core.items.Item;
import kassa.core.items.ItemFactory;
import kassa.core.items.Items;
import kassa.core.storage.Storage;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

/**
 * Class for input/output with .csv files
 * 
 * @author Stephen Pauwels
 */
public class IOcsv {

	private CsvReader m_csvReader;

	private Storage m_database;

	private ItemFactory m_factory;

	private Items m_items;

	public IOcsv() {

	}

	/**
	 * Open CSV file
	 * 
	 * @param filename
	 *            File to open
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public IOcsv(String fileName, Storage database)
			throws CsvFormatException, IOException, FileNotFoundException {
		File target = new File(fileName);
		m_database = database;
		m_factory = new ItemFactory();

		// Check existance of file
		if (!target.exists()) {
			throw new FileNotFoundException(fileName);
		}

		// Check if file is empty
		FileInputStream fis = new FileInputStream(target);
		int b = fis.read();
		if (b == -1) {
			fis.close();
			throw new FileNotFoundException(fileName);
		}
		fis.close();

		// File is ok, Continue
		try {
			m_csvReader = new CsvReader(fileName);
			m_csvReader.readHeaders();
		} catch (Exception e) {
			throw new CsvFormatException(fileName);
		}
	}

	/**
	 * Close csv
	 */
	public void closeCsv() {
		try {
			m_csvReader.close();
		} catch (Exception e) {
			// DO NOTHING
		}
	}

	public Items getItems() throws SQLException {
		loadFile();
		return m_items;
	}

	/**
	 * Load .csv file to the database
	 * 
	 * @throws IOException
	 *             When file has errors
	 * @throws CsvParserException
	 *             When csv file has errors
	 * @throws CsvFormatException
	 *             When file could not be recognized as csv file
	 */
	private void loadFile() throws SQLException {
		try {
			m_items = new Items(m_database);
			Item item = null;
			while (m_csvReader.readRecord()) {
				item = m_factory.createItem(m_csvReader.get("Category"),
						m_csvReader.get("Name"),
						Double.parseDouble(m_csvReader.get("Price")),
						m_csvReader.get("SubCat"),
						m_csvReader.get("Supplement"), 
						Integer.parseInt(m_csvReader.get("Tickets")));
				m_items.addItem(item);
			}
		} catch (ItemsException e) {
			m_items = null;
		} catch (IOException e) {

		}
	}

	/**
	 * Save items to a .csv file
	 * 
	 * @param fileName
	 *            File to write to
	 * @param items
	 *            Items to write to csv
	 */
	public void saveFile(File target, Items items) throws IOException {
		if (target.exists()) {
			target.delete();
		}

		CsvWriter csv = new CsvWriter(new FileWriter(target), ',');
		csv.write("Name");
		csv.write("Price");
		csv.write("Category");
		csv.write("SubCat");
		csv.write("Supplement");
		csv.write("Tickets");
		csv.endRecord();

		for (Item item : items) {
			csv.write(item.getName());
			csv.write("" + item.getPrice());
			csv.write(item.getCategory());
			csv.write(item.getSubCat());
			csv.write(item.getSupplement());
			csv.write("" + item.getNrTickets());
			csv.endRecord();
		}

		csv.close();
	}
}
