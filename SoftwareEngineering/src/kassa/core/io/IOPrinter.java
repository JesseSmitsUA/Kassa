package kassa.core.io;

import kassa.core.orders.Order;
import kassa.core.orders.TableOrders;

/**
 * Class for printing
 * 
 * @author Stephen Pauwels
 */
public interface IOPrinter {

	/**
	 * Print string
	 * 
	 * @param output
	 *            String to print
	 */
	public void print(String output);

	public boolean printerAccept();

	/**
	 * Print order
	 * 
	 * @param order
	 *            Order to print
	 * @param nrTable
	 *            Table number
	 */
	public void printOrder(Order order, int nrTable, int orderNr);

	public void printTicket(TableOrders order, int tableNr);
}
