package kassa.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import kassa.core.orders.TableOrders;

/**
 * Class for managing all the tables
 * 
 * @author Stephen Pauwels
 */
public class Tables implements Iterable<TableOrders>, Iterator<TableOrders> {

	/**
	 * Constructor
	 * 
	 * @param nrTables
	 *            Number of tables to add
	 */
	public Tables(int nrTables) {
		m_tables = new ArrayList<TableOrders>(nrTables);
		for (int i = 0; i < nrTables; i++) {
			m_tables.add(new TableOrders(i + 1));
		}
	}

	/**
	 * Close an active table
	 * 
	 * @param tableNr
	 *            Table to close
	 * @throws SQLException
	 */
	public void closeTable(int tableNr) {
		m_tables.set(tableNr - 1, new TableOrders(tableNr));
	}

	/**
	 * Return an ArrayList with al the free table numbers
	 * 
	 * @return ArrayList<Integer>
	 */
	public ArrayList<Integer> getEmpty() {
		ArrayList<Integer> empty = new ArrayList<Integer>();
		int i = 0;
		for (TableOrders orders : m_tables) {
			if (!orders.notEmpty())
				empty.add(i);
			i++;
		}
		return empty;
	}

	/**
	 * Return number of tables in the system
	 * 
	 * @return int
	 */
	public int getNrTables() {
		return m_tables.size();
	}

	/**
	 * Get tableOrder from table
	 * 
	 * @param tableNr
	 *            Table to get orders from
	 * @return TableOrder
	 */
	public TableOrders getTable(int tableNr) {
		return m_tables.get(tableNr - 1);
	}
	
	@Override
	public boolean hasNext() {
		return m_count < m_tables.size();
	}

	@Override
	public TableOrders next() {
		if (m_count == m_tables.size()) {
			return null;
		}
		return m_tables.get(m_count++);	
	}

	@Override
	public void remove() {
	
	}

	@Override
	public Iterator<TableOrders> iterator() {
		m_count = 0;
		return this;
	}
	
	private ArrayList<TableOrders> m_tables;
	
	int m_count;
}
