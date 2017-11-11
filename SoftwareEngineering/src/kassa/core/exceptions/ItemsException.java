package kassa.core.exceptions;

import kassa.core.items.Item;

/**
 * Exception class for exception during item-operations
 * 
 * @author Stephen
 */
public class ItemsException extends Exception {

	private static final long serialVersionUID = 1L;

	private Item m_item;

	private String m_message;

	/**
	 * Constructor
	 * 
	 * @param item
	 *            Item that caused the exception
	 * @param message
	 *            Reason to throw the exception
	 */
	public ItemsException(Item item, String message) {
		m_item = item;
		m_message = message;
	}

	/**
	 * Return the item
	 * 
	 * @return Item
	 */
	public Item getItem() {
		return m_item;
	}

	/**
	 * Return the message
	 * 
	 * @return String
	 */
	@Override
	public String getMessage() {
		return m_message;
	}
}
