package kassa.core.io;

import java.util.TreeMap;

import kassa.core.items.Item;
import kassa.core.orders.Order;
import kassa.core.orders.TableOrders;

import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QFont;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPrintDialog;
import com.trolltech.qt.gui.QPrinter;
import com.trolltech.qt.gui.QPrinter.ColorMode;
import com.trolltech.qt.gui.QPrinter.PageSize;

public class IOA5Printer implements IOPrinter {

	private boolean m_accept;

	private QPrinter m_printer;
	
	private QImage m_background;

	/**
	 * Constructor
	 */
	public IOA5Printer() {
		m_printer = new QPrinter();
		m_accept = false;
		
		QPrintDialog dialog = new QPrintDialog(m_printer);
		if (dialog.exec() == 1)
			m_accept = true;
		
		m_printer.setPaperSize(PageSize.A5);
		m_printer.setColorMode(ColorMode.GrayScale);
		m_printer.setResolution(300);

		String background = QFileDialog.getOpenFileName(null, "Selecteer achtergrond voor tickets");

		if (background != "") {
			m_background = new QImage(background);
			m_background = m_background.scaled(m_printer.height(), m_printer.width());
		}
		
	}

	/**
	 * Print string
	 * 
	 * @param output
	 *            String to print
	 */
	public void print(String output) {
		QPainter painter = new QPainter();
		painter.begin(m_printer);
		painter.setFont(new QFont(null, 20));
		painter.drawText(0, 0, 1000, 1000, 0, output);
		painter.end();
	}

	public boolean printerAccept() {
		return m_accept;
	}

	/**
	 * Print order
	 * 
	 * @param order
	 *            Order to print
	 * @param nrTable
	 *            Table number
	 */
	public void printOrder(Order order, int nrTable, int orderNr) {
		QPainter painter = new QPainter();
		if (order.hasDrinks()) {
			painter.begin(m_printer);
			painter.setFont(new QFont(null, 15));
			String output = "DRANK Order nr: " + orderNr + "     Tafel: " + nrTable
					+ "\n- - - - - - - - - - - - - - - - - - - -\n"
					+ order.printDrinkOrder();
			painter.drawText(0, 0, 1000, 1000, 0, output);
			painter.end();
		}

		if (order.hasFood()) {
			painter = new QPainter();
			painter.begin(m_printer);
			painter.setFont(new QFont(null, 15));
			String output = "ETEN Order nr: " + orderNr + "     Tafel: " + nrTable
					+ "\n- - - - - - - - - - - - - - - - - - - -\n"
					+ order.printFoodOrder();
			painter.drawText(0, 0, 1000, 1000, 0, output);
			painter.end();
		}
		
		if (order.secondTicket()) { // If second orderTicket is required
			painter = new QPainter();
			painter.begin(m_printer);
			painter.setFont(new QFont(null, 15));
			String output = "ETEN Order nr: " + orderNr + "     Tafel: " + nrTable
					+ "\n- - - - - - - - - - - - - - - - - - - -\n"
					+ order.printFoodOrder();
			painter.drawText(0, 0, 1000, 1000, 0, output);
			painter.end();
		}
	}

	public void printTicket(TableOrders order, int tableNr) {		
		m_printer.setOrientation(QPrinter.Orientation.Landscape);
		String quantities = "\n\n";
		String names = "Tafel: " + tableNr + "\n\n";
		String prices = "\n\n";

		TreeMap<Item, Integer> items = order.getItems();
		Item item = items.firstKey();
		int lines = 0;
		while (item != null) {
			quantities += items.get(item) + "\n";
			names += item.getName() + "\n";
			prices += "\u20AC " + round(items.get(item) * item.getPrice()) + "0\n";
			item = items.higherKey(item);
			lines++;
			if (lines == 21) { // Start a new ticket
				printTicket(quantities, names, prices);
				lines = 0;
				quantities = "";
				names = "";
				prices = "";
			}
		}
		names += "\nTOTAAL:";
		prices += "- - - - -\n\u20AC " + order.getTotalPrice() + "0";

		printTicket(quantities, names, prices);
	}
	
	private void printTicket(String quantities, String names, String prices) {
		QPainter painter = new QPainter();
		painter.begin(m_printer);
		painter.setFont(new QFont(null, 11));
		if (m_background != null)
			painter.drawImage(0, 0, m_background);
		painter.drawText(75, 75, 60, 3000, 0, quantities);
		painter.drawText(180, 75, 1500, 3000, 0, names);
		painter.drawText(840, 75, 3000, 3000, 0, prices);
		
		painter.end();
	}
	
	private double round(double input) {
		return Math.round(input * 100) / 100.0;
	}
}