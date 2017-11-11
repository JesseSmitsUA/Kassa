package kassa.core.storage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import kassa.core.Tables;
import kassa.core.food.FoodOrder;
import kassa.core.items.Item;
import kassa.core.items.Items;
import kassa.core.orders.Order;
import kassa.core.orders.TableOrders;

public class Storage {

	public enum working_mode { NORMAL_MODE, MOBILE_MODE, LOCAL_MODE }

	private SystemDatabase m_database;
	private SystemMobile m_mobile;
	private working_mode m_mode;
	
	private String m_host;
	private String m_user;
	private String m_password;
	
	public Storage(String host, String user, String password) throws SQLException, ClassNotFoundException {
		m_database = new SystemDatabase(new MysqlDatabase(host, user, password));
		m_mobile = new SystemMobile();
		m_mode = working_mode.NORMAL_MODE;
		
		m_host = host;
		m_user = user;
		m_password = password;
	}
	
	public void close() throws SQLException {
		m_database.close();
	}
	
	public void newSystem() throws SQLException {
		m_database.setupTables();
	}
	
	public void loadSystem() {

	}
	
	public void payed(int tableNr) throws SQLException {
		m_database.payed(tableNr);
	}
	
	/**
	 * Add item to database + return database id number
	 */
	public int addItem(Item item) throws SQLException {
		int id = m_database.addItem(item);
		if (m_mode == working_mode.MOBILE_MODE)
			m_mobile.addItem(item);
		return id;
	}
	
	public void deleteItem(Item item) throws SQLException {
		m_database.deleteItem(item);
		if (m_mode == working_mode.MOBILE_MODE)
			m_mobile.deleteItem(item);
	}
	
	public void deleteItems() throws SQLException {
		m_database.deleteItems();
	}

	public void addOrder(int tableNr, Order order) throws SQLException {
		m_database.addOrder(tableNr, order);
		if (m_mode == working_mode.MOBILE_MODE)
			m_mobile.addOrder(tableNr, order);
	}


	public TreeMap<Item, Integer> getTotals(Items items) throws SQLException{
		if (m_mode == working_mode.NORMAL_MODE)
			return m_database.getTotals(items);
		return null;
	}

	public Tables readHistory(Items items) throws SQLException {
		if (m_mode == working_mode.NORMAL_MODE)
			return m_database.readHistory(items);
		return null;
	}
	
	public Items readItems() throws SQLException {
		return m_database.readItems(this);
	}

	public Tables readTables(Items items) throws SQLException {
		return m_database.readTables(items);
	}

	public void splitToPayed(Order split_order, TableOrders order) throws SQLException {
		m_database.splitToPayed(split_order, order);
	}
	
	public void split(int new_table, Order split_order, TableOrders order) throws SQLException {
		m_database.split(new_table, split_order, order);
	}
	
	public void removeOrder(int tableNr) throws SQLException {
		m_database.removeFullOrder(tableNr);
	}

	public void updateItem(int id, Item item) throws SQLException {
        m_database.updateItem(id, item);
    }

	public void updateOrderItem(int tableNr, int item, int number) throws SQLException {
		m_database.updateOrderItem(tableNr, item, number);
	}
	
	/**
	 * System database
	 **/
	
	/**
	 * Set number of tables
	 */
	public void setTables(int tableNrs) throws SQLException {
		m_database.setTables(tableNrs);
	}
	
	public int numberOfTables() {
		return m_database.numberOfTables();
	}
	
	public void changeName(int tableNr, String name) throws SQLException {
		m_database.addNameToTable(tableNr, name);
	}
	/**
	 * Get next orderNr
	 */
	public int getOrderNr() {
		return m_database.getOrderNr();
	}
	
	public HashMap<Integer, HashSet<Integer>> getAllOrders() throws SQLException {
		return m_database.getAllOrders();
	}
	
	/**
	 * Switch to local mode
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public void switchToLocalDatabase() throws SQLException, ClassNotFoundException {
		m_mode = working_mode.LOCAL_MODE;
		m_database = new SystemDatabase(new SqliteDatabase());
	}
	
	public void migrateDatabase(Tables tables, Items items, String password) throws SQLException {
		m_database.migrateDatabase(tables, items, password);
	}
	
	public void restoreDatabaseConnection() throws ClassNotFoundException, SQLException {
		m_database = new SystemDatabase(new MysqlDatabase(m_host, m_user, m_password));
		m_mode = working_mode.NORMAL_MODE;
	}
	
	public void updateDatabase(Tables tables, Items items, String password) throws SQLException {
		m_database.migrateDatabase(tables, items, password);
	}
	

	/**
	 * Food Manager
	 */
	public void setOrderToBusy(int order_id) throws SQLException {
		m_database.setOrderToBusy(order_id);
	}
	
	public void setOrderToDelivered(int order_id) throws SQLException {
		m_database.setOrderToDelivered(order_id);
	}
	
	public void setOrderToReset(int order_id) throws SQLException {
		m_database.setOrderToReset(order_id);
	}
	
 	public ArrayList<FoodOrder> readFoodOrders(Items items, int delivered, int waiting) throws SQLException {
		return m_database.readFoodOrders(items, delivered, waiting);
	}
	
	public ArrayList<String> readFoodOrder(int tableNr) throws SQLException, IOException {
		return m_database.readFoodOrder(tableNr);
	}
	
	public String getAverageDeliveryTime() throws SQLException {
		return m_database.getAverageDeliveryTime();
	}
}
