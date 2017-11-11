package kassa.core.orders;

import java.util.TreeMap;

import kassa.core.exceptions.ItemsException;
import kassa.core.items.Item;

/**
 * Class for managing an order
 * 
 * @author Stephen Pauwels
 */
public class Order {

	private TreeMap<Item, Integer> m_items;
	private TreeMap<String, Integer> m_supplements;

	/**
	 * Constructor
	 */
	public Order() {
		m_items = new TreeMap<Item, Integer>();
		m_supplements = new TreeMap<String, Integer>();
	}

	/**
	 * Add item to list
	 * 
	 * @param item
	 *            Item to add
	 * @param number
	 *            Number of items to add
	 */
	public void addItem(Item item, int number) {
		if (number == 0)
			return;
		
		// Add Item
		Integer nrOfItem = m_items.get(item);
//		if (nrOfItem == null)
//			m_items.put(item, number);
//		else
//			m_items.put(item, number + nrOfItem);
		m_items.put(item, number);
		// Add Supplements
		if (item.getSupplement() != null && !item.getSupplement().equals("")) {
			Integer nrOfSupps = m_supplements.get(item.getSupplement());
			if (nrOfSupps == null)
				m_supplements.put(item.getSupplement(), number);
			else
				m_supplements.put(item.getSupplement(), number + nrOfSupps);
		}
	}

	public void clear() {
		m_items.clear();
		m_supplements.clear();
	}

	/**
	 * Remove items from the list
	 * 
	 * @param item
	 *            Item to delete
	 * @param number
	 *            Number of items to delete
	 */
	public void deleteItem(Item item, int number) throws ItemsException {
		Integer nrItems = m_items.get(item);
		if (nrItems == null) {
			throw new ItemsException(item, "Item not found in list!");
		} else if (nrItems - number < 0) {
			throw new ItemsException(item,
					"Deleted more items than there are in the list!");
		} else if (nrItems - number == 0) {
			m_items.remove(item);
		} else {
			m_items.put(item, nrItems - number);
		}
	}

	/**
	 * Return a string with the order for the ticket
	 * 
	 * @return String
	 */
	public String[] displayOrderTicket() {
		String[] output = new String[3];

		String quantityTop = "";
		String nameTop = "";
		String priceTop = "";

		String quantityDown = "";
		String nameDown = "";
		String priceDown = "";

		if (m_items.size() == 0)
			return null;

		Item item = m_items.firstKey();
		while (item != null) {
			if (item.getCategory().compareTo("Drank") == 0) {
				quantityTop += m_items.get(item) + "\n";
				nameTop += item.getName() + "\n";
				priceTop += "\u20AC " + (round(m_items.get(item) * item.getPrice())) + "\n";
			} else {
				quantityDown += m_items.get(item) + "\n";
				nameDown += item.getName() + "\n";
				priceDown += "\u20AC " + (round(m_items.get(item) * item.getPrice())) + "\n";
			}
			item = m_items.higherKey(item);
		}

		output[0] = quantityTop + "\n" + quantityDown;
		output[1] = nameTop + "\n" + nameDown;
		output[2] = priceTop + "\n" + priceDown;

		return output;
	}

	public TreeMap<Item, Integer> getItems() {
		return m_items;
	}

	public int getNrOfItems() {
		return m_items.size();
	}

	/**
	 * Return the total price of the order
	 * 
	 * @return double
	 */
	public double getTotalPrice() {
		if (m_items.size() == 0)
			return 0;
		double total = 0;
		Item item = m_items.firstKey();
		while (item != null) {
			total += item.getPrice() * m_items.get(item);
			item = m_items.higherKey(item);
		}
		return round(total);
	}

	public boolean hasDrinks() {
		Item item = m_items.firstKey();
		while (item != null) {
			if (item.getCategory().compareTo("Drank") == 0)
				return false;
			item = m_items.higherKey(item);
		}
		return true;
	}

	public boolean hasFood() {
		Item item = m_items.firstKey();
		while (item != null) {
			if (item.getCategory().compareTo("Drank") != 0)
				return true;
			item = m_items.higherKey(item);
		}
		return false;
	}

	/**
	 * Return true if order is not empty
	 * 
	 * @return Boolean
	 */
	public Boolean notEmpty() {
		return (m_items.size() != 0);
	}

	/**
	 * Return a string with the order
	 * 
	 * @return String
	 */
	public String printDrinkOrder() {
		String output = "";
		if (m_items.size() == 0)
			return "";
		Item item = m_items.firstKey();
		while (item != null) {
			if (item.getCategory().compareTo("Drank") == 0) {
				output += m_items.get(item) + "\t\t" + item.getName() + "\n";
			}
			item = m_items.higherKey(item);
		}
		return output;
	}

	public String printFoodOrder() {
		String output = "";
		if (m_items.size() == 0)
			return "";
		
		// Read items
		Item item = m_items.firstKey();
		while (item != null) {
			if (item.getCategory().compareTo("Drank") != 0) {
				output += m_items.get(item) + "\t\t" + item.getName() + "\n";
			}
			item = m_items.higherKey(item);
		}
		output += "\n";

		// Read supplements
		if (!m_supplements.isEmpty()) {
			String supp = m_supplements.firstKey();
			while (supp != null) {
				output += m_supplements.get(supp) + "\t\t" + supp + "\n";
				supp = m_supplements.higherKey(supp);
			}
		}

		return output;
	}

	public String printOrder() {
		String output = printDrinkOrder();
		if (output.length() != 0)
			output += "\n";
		output += printFoodOrder();
		return output;
	}

	/**
	 * Return a string with the order
	 * 
	 * @return String
	 */
	public String printTable() {
		if (m_items.size() == 0)
			return "";

		String upper = "";
		String lower = "";
		Item item = m_items.firstKey();
		while (item != null) {
			if (item.getCategory().compareTo("Drank") == 0) {
				upper += m_items.get(item) + "\t" + item.getName() + "\n";
			} else {
				lower += m_items.get(item) + "\t" + item.getName() + "\n";
			}
			item = m_items.higherKey(item);
		}

		return upper + "\n" + lower;
	}

	private double round(double input) {
		return Math.round(input * 100) / 100.0;
	}
	
	public boolean secondTicket() {
		Item item = m_items.firstKey();
		while (item != null) {
			if (item.getNrTickets() == 2)
				return true;
			item = m_items.higherKey(item);
		}
		return false;
	}
	
}
