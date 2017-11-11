package kassa.core.orders;

import java.util.TreeMap;

import kassa.core.exceptions.ItemsException;
import kassa.core.items.Item;

/**
 * Class for managing all orders of one table
 * 
 * @author Stephen Pauwels
 */
public class TableOrders {

	private int m_tableNr;
	private String m_name;

	private Order m_totalOrder;

	/**
	 * Constructor
	 */
	public TableOrders(int tableNr, String name) {
		m_totalOrder = new Order();
		m_tableNr = tableNr;
		m_name = name;
	}
	
	public TableOrders(int tableNr) {
		this(tableNr, "");
	}

	/**
	 * Add order to table
	 * 
	 * @param order
	 *            Order to add
	 */
	public void addLoadedOrder(Order order) {
		TreeMap<Item, Integer> items = order.getItems();
		Item item = items.firstKey();
		while (item != null) {
			m_totalOrder.addItem(item, items.get(item));
			item = items.higherKey(item);
		}
	}

	/**
	 * Add order to table
	 * 
	 * @param order
	 *            Order to add
	 */
	public void addOrder(Order order) {
		if (order.getNrOfItems() == 0) {
			return;
		}
		TreeMap<Item, Integer> items = order.getItems();
		Item item = items.firstKey();
		while (item != null) {
			m_totalOrder.addItem(item, items.get(item));
			item = items.higherKey(item);
		}
	}

	/**
	 * Delete order from table
	 * 
	 * @param order
	 *            Order to delete
	 */
	public void deleteOrder(Order order) {
		try {
			TreeMap<Item, Integer> items = order.getItems();
			Item item = items.firstKey();
			while (item != null) {
				m_totalOrder.deleteItem(item, items.get(item));
				item = items.higherKey(item);
			}
		} catch (ItemsException e) {

		}
	}

	public void deleteOrders() {
		m_totalOrder = new Order();
	}

	public String[] displayFinalOrders() {
		return m_totalOrder.displayOrderTicket();
	}

	public TreeMap<Item, Integer> getItems() {
		return m_totalOrder.getItems();
	}

	/**
	 * Return the total price of all the orders
	 * 
	 * @return double
	 */
	public double getTotalPrice() {
		return m_totalOrder.getTotalPrice();
	}

	/**
	 * Return true if table is empty
	 * 
	 * @return boolean
	 */
	public boolean notEmpty() {
		return m_totalOrder.notEmpty();
	}

	/**
	 * Return the number of orders for this table
	 * 
	 * @return int
	 */
	public int nrOfItems() {
		return m_totalOrder.getNrOfItems();
	}

	/**
	 * Print all orders from table
	 * 
	 * @return String
	 */
//	public String printOrders() {
//		return m_totalOrder.printOrder();
//	}

	/**
	 * Print all orders from table
	 * 
	 * @return String
	 */
	public String printTable() {
		return m_totalOrder.printTable();
	}

	public int totalNrOfItems() {
		TreeMap<Item, Integer> items = m_totalOrder.getItems();
		if (items.size() == 0)
			return 0;

		Item item = items.firstKey();
		int sum = 0;
		while (item != null) {
			sum += items.get(item);
			item = items.higherKey(item);
		}
		return sum;
	}
	
	public int tableNr() {
		return m_tableNr;
	}
	
	public String tableName() {
		return m_name;
	}
	
	public void setTableName(String name) {
		m_name = name;
	}
}
