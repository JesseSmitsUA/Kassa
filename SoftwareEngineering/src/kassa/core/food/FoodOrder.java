package kassa.core.food;

import kassa.core.items.Item;

public class FoodOrder {
	
	private int m_id;
	private int m_table_nr;
	private Item m_item;
	private int m_quantity;
	
	public FoodOrder(int id, int table_nr, Item item, Integer quantity_item) {
		m_id = id;
		m_table_nr = table_nr;
		m_item = item;
		m_quantity = quantity_item;
	}
	
	public int getId() {
		return m_id;
	}
	
	public String getInfo() {
		return m_id + "; T " + m_table_nr + " | " + m_quantity + " x " + m_item.getName() ;
	}
	
	public String getDetails() {
		String output = "Details voor tafel: " + m_table_nr + "\n\n";
		
		output += m_quantity + " x " + m_item.getName();
		if (m_item.getSupplement() != null && !m_item.getSupplement().equals("")) 
			output += " (" + m_item.getSupplement() + ")";
		
		return output;
	}
	
	public String getSupplement() {
		return m_item.getSupplement();
	}
}
