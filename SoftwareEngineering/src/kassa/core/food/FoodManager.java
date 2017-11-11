package kassa.core.food;

import java.sql.SQLException;
import java.util.ArrayList;

import kassa.core.items.Items;
import kassa.core.storage.Storage;

public class FoodManager {

	private Storage m_database;
	private Items m_items;
	
	private ArrayList<FoodOrder> m_waiting;
	private ArrayList<FoodOrder> m_busy;
	private ArrayList<FoodOrder> m_delivered;
	
	public FoodManager(Storage database) throws SQLException {
		m_database = database;
		m_items = m_database.readItems();
	}
	
	public void load() throws SQLException {
		m_waiting = m_database.readFoodOrders(m_items, 0, 1);
		m_busy = m_database.readFoodOrders(m_items, 0, 0);
		m_delivered = m_database.readFoodOrders(m_items, 1, 0);
	}
	
	public void close() throws SQLException {
		m_database.close();
	}
	
	public void refresh() throws SQLException {
		m_waiting = m_database.readFoodOrders(m_items, 0, 1);
	}
	
	public ArrayList<FoodOrder> getList(int i) {
		if (i == 0)
			return getWaiting();
		else if (i == 1)
			return getBusy();
		else if (i == 2)
			return getDelivered();
		return null;
	}
	
	public ArrayList<String> getSupplements() {
		return m_items.getSupplements();
	}
	
	public ArrayList<FoodOrder> getWaiting() {
		return m_waiting;
	}
	
	public ArrayList<FoodOrder> getBusy() {
		return m_busy;
	}
	
	public ArrayList<FoodOrder> getDelivered() {
		return m_delivered;
	}
	
	public String getDetails(int order_id) {
		FoodOrder info_order = findOrder(order_id, m_waiting);
		if (info_order != null)
			return info_order.getDetails();
		
		info_order = findOrder(order_id, m_busy);
		if (info_order != null)
			return info_order.getDetails();
		
		info_order = findOrder(order_id, m_delivered);
		if (info_order != null)
			return info_order.getDetails();
				
		return "";
	}
	
	public void changeToBusy(int order_id) throws SQLException {
		FoodOrder change_order = findOrder(order_id, m_waiting);
		m_waiting.remove(change_order);
		m_busy.add(change_order);
		
		m_database.setOrderToBusy(order_id);
	}
	
	public void changeToDelivered(int order_id) throws SQLException {
		FoodOrder change_order = findOrder(order_id, m_busy);
		m_busy.remove(change_order);
		m_delivered.add(0, change_order);
		
		m_database.setOrderToDelivered(order_id);
	}
	
	public void resetOrder(int order_id) throws SQLException {
		FoodOrder change_order = findOrder(order_id, m_busy);
		m_busy.remove(change_order);
		m_waiting.add(change_order);
		
		m_database.setOrderToReset(order_id);
	}
	
	private FoodOrder findOrder(int order_id, ArrayList<FoodOrder> list) {
		for (FoodOrder order : list) {
			if (order.getId() == order_id)
				return order;
		}
		return null;
	}
	
	public int getStatus(int order_id) {
		FoodOrder info_order = findOrder(order_id, m_waiting);
		if (info_order != null)
			return 1;
		
		info_order = findOrder(order_id, m_busy);
		if (info_order != null)
			return 2;
		
		info_order = findOrder(order_id, m_delivered);
		if (info_order != null)
			return 3;
				
		return 0;
	}
	
	public String getAvgTimings() throws SQLException {
		
		return m_database.getAverageDeliveryTime();
	}
}
