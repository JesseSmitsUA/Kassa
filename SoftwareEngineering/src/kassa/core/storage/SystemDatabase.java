package kassa.core.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import kassa.core.Tables;
import kassa.core.exceptions.ItemsException;
import kassa.core.food.FoodOrder;
import kassa.core.items.Item;
import kassa.core.items.ItemFactory;
import kassa.core.items.Items;
import kassa.core.orders.Order;
import kassa.core.orders.TableOrders;

public class SystemDatabase {
	
	private Database m_database;
	private ItemFactory m_item_factory;
	
	private DatabaseLogger m_logger;

	public SystemDatabase(Database database) {
		m_database = database;
		m_item_factory = new ItemFactory();
		
		try {
			m_logger = new DatabaseLogger(m_database);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void close() throws SQLException {
		m_database.close();
	}
	
	public void setupTables() throws SQLException {
		// Drop old tables
		m_database.updateQuery("DROP TABLE IF EXISTS system;");
		m_database.updateQuery("DROP TABLE IF EXISTS items;");
		m_database.updateQuery("DROP TABLE IF EXISTS clients;");
		m_database.updateQuery("DROP TABLE IF EXISTS orders;");
		m_database.updateQuery("DROP TABLE IF EXISTS food_manager");
		
		// Create new tables
		m_database.updateQuery("CREATE TABLE system (Key1 VARCHAR(50) PRIMARY KEY, value VARCHAR(50))");
		m_database.updateQuery("CREATE TABLE items (ID INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(50), price DOUBLE, cat VARCHAR(50), subcat VARCHAR(50), supplement VARCHAR(50), nrTickets INT)");
		m_database.updateQuery("CREATE TABLE clients (ID INT PRIMARY KEY AUTO_INCREMENT, payed BOOL, tableNr INT, name VARCHAR(50))");
		m_database.updateQuery("CREATE INDEX client_tables ON clients (tableNr)");
		m_database.updateQuery("CREATE TABLE orders (ID INT PRIMARY KEY AUTO_INCREMENT, item INT REFERENCES item (ID), client INT REFERENCES clients (ID), quantity INT)");
		m_database.updateQuery("CREATE TABLE food_manager (Order_id INT, delivered BOOL, waiting BOOL, waiting_time DATETIME, busy_time DATETIME, delivered_time DATETIME)");		
		
		// start value for orders
		m_database.updateQuery("INSERT INTO system VALUES (\"orders\", \"1\")");
	}
	
	private int getClientId(int table) throws SQLException{
		int client = -1;
		ResultSet result = m_database.query("SELECT ID FROM clients WHERE tableNr = " + table + " and payed = 0");
		if (result.next())
			client = result.getInt("ID");
		result.close();
		return client;
	}
	
	public void payed(int tableNr) throws SQLException {
		int client = getClientId(tableNr);
		m_database.updateQuery("UPDATE clients SET payed = 1 WHERE ID = " + client);
		m_database.updateQuery("INSERT INTO clients VALUES (NULL, 0, " + tableNr + ",\" \")");
		
		m_logger.addTableLog(tableNr, "Payed");
	}
	
	public void addNameToTable(int tableNr, String name) throws SQLException {
		m_database.updateQuery("UPDATE clients SET name = \"" + name + "\" WHERE tableNr = " + tableNr + " AND payed = 0" );
	}
	
	public void addOrder(int tableNr, Order order) throws SQLException {
		if (!order.notEmpty())
			return;
		
		int client = getClientId(tableNr);
		TreeMap<Item, Integer> items = order.getItems();
		Item item = items.firstKey();
		while (item != null) {
			int order_id = m_database.updateQuery("INSERT INTO orders VALUES (NULL, " + item.getDatabaseId() + ", " + client + ", " + items.get(item) + ")");
			if (item.getCategory() != "Drank" && item.getCategory() != "Desert")
				m_database.updateQuery("INSERT INTO food_manager VALUES (" + order_id +", 0, 1, NOW(), NULL, NULL)");
			
			m_logger.addTableLog(tableNr, "AddOrder: " + items.get(item) + " " + item.getName() + "(" + item.getDatabaseId() + ")");

			item = items.higherKey(item);			
		}
	}
	
	public void updateOrderItem(int tableNr, int item, int number) throws SQLException {
		int client = getClientId(tableNr);
		m_database.updateQuery("UPDATE orders SET quantity = " + number
                 + " WHERE (client = " + client + " AND name = \""
                 + item + "\"");
	}
	
	public void removeFullOrder(int tableNr) throws SQLException {
		int client = getClientId(tableNr);
		m_database.updateQuery("DELETE FROM orders WHERE (client = " + client + ")");
		
		m_logger.addTableLog(tableNr, "RemoveOrder");
	}
	
	/**
	 * Add item to database + return database id number
	 */
	public int addItem(Item item) throws SQLException {
		return m_database.updateQuery("INSERT INTO items VALUES (NULL, \"" + item.getName() + "\", "
				+ item.getPrice() + ", \"" + item.getCategory() + "\",\""
				+ item.getSubCat() + "\", \"" + item.getSupplement() + "\"," + item.getNrTickets() + ")");
	}
	
	public void updateItem(int id, Item item) throws SQLException {
		m_database.updateQuery("UPDATE items SET name " +
                "= \"" + item.getName()
                + "\", price = " + item.getPrice() + ", cat = \""
                + item.getCategory() + "\", subCat = \"" + item.getSubCat()
                + "\" , supplement = \"" + item.getSupplement() + "\" WHERE ID = " + id + "");	
    }
	
	public void deleteItem(Item item) throws SQLException {
		m_database.updateQuery("DELETE FROM items WHERE (name = \"" + item.getName() + "\" and " +
				"supplement = \"" + item.getSupplement() + "\")");
	}
	

	public void deleteItems() throws SQLException {
		m_database.updateQuery("DROP TABLE items");
		m_database.updateQuery("CREATE TABLE item (ID INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(50), price FLOAT, cat VARCHAR(50), subcat VARCHAR(50), supplement VARCHAR(50), nrTickets INT)");
	}
	
	public TreeMap<Item, Integer> getTotals(Items items) throws SQLException{
		TreeMap<Item, Integer> totals = new TreeMap<Item, Integer>();
		for (Item item : items) {
			ResultSet result = m_database.query("SELECT SUM(quantity) FROM orders WHERE item = " + item.getDatabaseId());
			result.next();
			totals.put(item, result.getInt(1));
			result.close();
		}
		return totals;
	}
	
	public Tables readHistory(Items items) throws SQLException {
		ResultSet result = m_database.query("SELECT COUNT(*) FROM clients WHERE payed = 1");
		result.next();
		int nr_clients = result.getInt(1);
		result.close();
		
		result = m_database.query("SELECT ID FROM clients WHERE payed = 1");
		Tables history = new Tables(nr_clients);
		int i = 1;
		while (result.next()) {
			Order order = new Order();
			ResultSet result_orders = m_database.query("SELECT item, quantity FROM orders WHERE client = " + result.getInt("ID"));
			while (result_orders.next()) {
				order.addItem(items.getItemByID(result_orders.getInt("item")), result_orders.getInt("quantity"));
			}
			if (order.getNrOfItems() != 0)
				history.getTable(i).addLoadedOrder(order);
			
			result_orders.close();
			i++;
		}
		result.close();
		return history;
	}
	
	public Items readItems(Storage storage) throws SQLException {
		Items items = new Items(storage);
		ResultSet result = m_database.query("SELECT ID, name, price, cat, subCat, supplement, nrTickets FROM items");
		Item item = null;
		while (result.next()) {
			try {
				item = m_item_factory.createItem(result.getInt("ID"), result.getString("cat"),
						result.getString("name"), result.getFloat("price"),
						result.getString("subCat"),
						result.getString("supplement"),
						result.getInt("nrTickets"));
				items.addLoadedItem(item);
			} catch (ItemsException e) {
			}
		}
		result.close();
		return items;
	}
	
	public Tables readTables(Items items) throws SQLException {
		Tables tables = null;
		ResultSet result = m_database.query("SELECT value FROM system WHERE Key1 = \"tables\"");
		result.next();
		int tableNrs = result.getInt("value");
		if (tableNrs == 0) {
			return null;
		}
		result.close();
		tables = new Tables(tableNrs);
				
		for (int i = 1; i <= tableNrs; i++) {
			TableOrders table = tables.getTable(i);
			
			result = m_database.query("SELECT name FROM clients WHERE tableNr = " + i + " AND payed = 0");
			result.next();
			table.setTableName(result.getString("name"));
			result.close();
			
			Order order = new Order();
			
			int client_id = getClientId(i);
			result = m_database.query("SELECT item, quantity FROM orders WHERE client = " + client_id);
			while (result.next()) {
				order.addItem(items.getItemByID(result.getInt("item")), result.getInt("quantity"));
			}
			
			if (order.getNrOfItems() != 0)
				table.addLoadedOrder(order);
		}
		return tables;
	}
	
	public void splitToPayed(Order split_order, TableOrders order) throws SQLException {
		int old_client_id = getClientId(order.tableNr());
		int new_client_id = m_database.updateQuery("INSERT INTO clients VALUES (NULL, 1,0,\"\")");
		
		splitOrder(old_client_id, order, new_client_id, split_order);
		
		m_logger.addTableLog(order.tableNr(), "SplitToPayed");
	}
	
	public void split(int new_table, Order split_order, TableOrders order) throws SQLException {
		int old_client_id = getClientId(order.tableNr());
		int new_client_id = getClientId(new_table);
		
		splitOrder(old_client_id, order, new_client_id, split_order);
		
		m_logger.addTableLog(order.tableNr(), "SplitToTable " + new_table);
	}
	
	private void splitOrder(int old_client_id, TableOrders order, int new_client_id, Order split_order) throws SQLException {
		TreeMap<Item, Integer> items = split_order.getItems();
		Item item = items.firstKey();
		
		TreeMap<Item, Integer> remaining = order.getItems();
		
		while (item != null) {
			m_database.updateQuery("INSERT INTO orders VALUES (NULL, " + item.getDatabaseId() + "," + new_client_id + "," + items.get(item) +")");
			m_database.updateQuery("DELETE FROM orders WHERE client = " + old_client_id + " and item = " + item.getDatabaseId());
			if (remaining.get(item) != null)
				m_database.updateQuery("INSERT INTO orders VALUES (NULL, " + item.getDatabaseId() + ", " + old_client_id + ", " + 
						remaining.get(item) + ")");
			item = items.higherKey(item);
		}
	}
	
	public HashMap<Integer, HashSet<Integer>> getAllOrders() throws SQLException {
		ResultSet result = m_database.query("SELECT client, item FROM orders");
		HashMap<Integer, HashSet<Integer>> output = new HashMap<Integer, HashSet<Integer>>();
		while(result.next()) {
			int id = result.getInt("client");
			int item = result.getInt("item");
			if(!output.containsKey(id))
				output.put(id, new HashSet<Integer>());
			output.get(id).add(item);
		}
		return output;
	}
	
	
	/**
	 * System database
	 **/
	
	/**
	 * Set number of tables
	 */
	public void setTables(int tableNrs) throws SQLException {
		m_database.updateQuery("INSERT INTO system VALUES (\"tables\", + \"" + tableNrs
				+ "\")");
		for (int i = 1; i <= tableNrs; i++) {
			m_database.updateQuery("INSERT INTO clients VALUES (NULL, 0, " + i + ", \"\")");
		}
	}
	
	public int numberOfTables() {
		ResultSet result = null;
		try {
			result = m_database.query("SELECT value FROM system WHERE Key1 = \"tables\"");
			result.next();
			return result.getInt("value");
		} catch (Exception e) {
			try {
				result.close();
			} catch (SQLException e1) {
				
			}
			return 0;
		}
	}
	
	/**
	 * Get next orderNr
	 */
	public int getOrderNr() {
		try {
			ResultSet result = m_database.query("SELECT value FROM system WHERE Key1 = \"orders\"");
			result.next();
			int order = result.getInt("value");
			result.close();
			m_database.updateQuery("UPDATE system SET value = " + (order+1) + " WHERE Key1 = \"orders\"");
			return order;
		}
		catch (SQLException e) {
			return 0;
		}
	}
	
	public void migrateDatabase(Tables tables, Items items, String password) throws SQLException {
		setupTables();
		for (Item item : items) {
			addItem(item);
		}
		
		setTables(tables.getNrTables());
		
		for (int i = 1; i <= tables.getNrTables(); i++) {
			int client = getClientId(i);
			if (tables.getTable(i).notEmpty()) {
				TreeMap<Item, Integer> order_items = tables.getTable(i).getItems();
				Item item = order_items.firstKey();
				while (item != null) {
					m_database.updateQuery("INSERT INTO orders VALUES (NULL, " + item.getDatabaseId() + ", " + client + ", " + order_items.get(item) + ")");
					item = order_items.higherKey(item);
				}
			}
		}
	}
	
	public void setOrderToBusy(int order_id) throws SQLException {
		m_database.updateQuery("UPDATE food_manager SET waiting = 0, busy_time = NOW() WHERE Order_id = " + order_id);
	}
	
	public void setOrderToDelivered(int order_id) throws SQLException {
		m_database.updateQuery("UPDATE food_manager SET delivered = 1, delivered_time = NOW() WHERE Order_id = " + order_id);
	}
	
	public void setOrderToReset(int order_id) throws SQLException {
		m_database.updateQuery("UPDATE food_manager SET waiting = 1, delivered = 0, delivered_time = NULL, busy_time = NULL WHERE Order_id = " + order_id);
	}
	
	public ArrayList<FoodOrder> readFoodOrders(Items items, int delivered, int waiting) throws SQLException {
		ArrayList<FoodOrder> orders = new ArrayList<FoodOrder>();
		ResultSet result = m_database.query("SELECT order_id from food_manager WHERE (delivered = " + delivered + " AND waiting = " + waiting + ")");
		while (result.next()) {
			ResultSet result_order = m_database.query("SELECT item, quantity, client FROM orders WHERE ID = " + result.getInt("order_id"));
			result_order.next();
			ResultSet result_client = m_database.query("SELECT tableNr FROM clients WHERE ID = " + result_order.getInt("client"));
			result_client.next();
			
			orders.add(new FoodOrder(result.getInt("order_id"), result_client.getInt("tableNr"), items.getItemByID(result_order.getInt("item")), result_order.getInt("quantity") ));
		}
		result.close();
		return orders;
	}
	
	public ArrayList<String> readFoodOrder(int tableNr) throws SQLException {
		ResultSet result = m_database.query("SELECT name, orders.ID, orders.quantity, waiting, delivered " +
				"FROM items JOIN orders on (items.ID = orders.item) JOIN food_manager on (orders.ID = food_manager.Order_Id) " +
				"WHERE orders.ID IN " +
				"(SELECT ID from orders where client IN " +
				"(SELECT ID from clients where payed = 0 and tableNr =" + tableNr +"))");
				
		ArrayList<String> output = new ArrayList<String>();

		while (result.next()) {
			String line = result.getInt("orders.quantity") + " " + result.getString("name") + "\t";
			if (result.getInt("delivered") == 1) {
				line += "Afgeleverd";
			} else if (result.getInt("waiting") == 1) {
				line += "Wachtend";
			} else {
				line += "Bezig";
			}
			output.add(line);

		}
		return output;
	}
	
	public String getAverageDeliveryTime() throws SQLException {
		ResultSet result = m_database.query("SELECT AVG(TIMEDIFF(delivered_time, waiting_time)) FROM food_manager");
		result.next();
		double time = result.getDouble(1);
		result.close();
		
		int secs = (int) time % 60;
		int mins = ((int) time - secs) / 60;
				
		return mins + ":" + secs;
	}
}
