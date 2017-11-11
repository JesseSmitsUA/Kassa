package kassa.core.io;

import java.util.TreeMap;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.standard.PrinterName;

import kassa.core.items.Item;
import kassa.core.orders.Order;
import kassa.core.orders.TableOrders;

import com.trolltech.qt.core.QDataStream;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QDialogButtonBox;
import com.trolltech.qt.gui.QFont;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPrintDialog;
import com.trolltech.qt.gui.QPrinter;
import com.trolltech.qt.gui.QPrinter.ColorMode;
import com.trolltech.qt.gui.QPrinterInfo;

public class IOTicketPrinter implements IOPrinter {

	private boolean m_accept;

	private QPrinter m_printer;
	private PrintService m_service;
	

	/**
	 * Constructor
	 */
	public IOTicketPrinter() {
		m_printer = new QPrinter();
		m_accept = false;
		
		QPrintDialog dialog = new QPrintDialog(m_printer);
		if (dialog.exec() == 1)
			m_accept = true;
		
		m_printer.setColorMode(ColorMode.GrayScale);
		m_printer.setResolution(200);		
		
		System.out.println(m_printer.printerName());
		AttributeSet aset = new HashAttributeSet();
		aset.add(new PrinterName(m_printer.printerName(), null));
		m_service = PrintServiceLookup.lookupPrintServices(null, aset)[0];
		
		cut();
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
			painter.setFont(new QFont(null, 10));
			String output = "DRANK Order nr: " + orderNr + "\nTafel: " + nrTable
					+ "\n- - - - - - - - - - - - - - - - - - - -\n"
					+ order.printDrinkOrder();
			painter.drawText(0, 0, 1000, 1000, 0, output);
			painter.end();
			cut();
		}

		
		if (order.hasFood()) {
			painter = new QPainter();
			painter.begin(m_printer);
			painter.setFont(new QFont(null, 10));
			String output = "ETEN Order nr: " + orderNr + "\nTafel: " + nrTable
					+ "\n- - - - - - - - - - - - - - - - - - - -\n"
					+ order.printFoodOrder();
			painter.drawText(0, 0, 1000, 1000, 0, output);
			painter.end();
			cut();
		}
		
		if (order.secondTicket()) { // If second orderTicket is required
			painter = new QPainter();
			painter.begin(m_printer);
			painter.setFont(new QFont(null, 10));
			String output = "ETEN Order nr: " + orderNr + "\nTafel: " + nrTable
					+ "\n- - - - - - - - - - - - - - - - - - - -\n"
					+ order.printFoodOrder();
			painter.drawText(0, 0, 1000, 1000, 0, output);
			painter.end();
			cut();
		}
	}

	public void printTicket(TableOrders order, int tableNr) {		
		String quantities = "\n\n\n\n";
		String names = "Kassa\n\nTafel: " + tableNr + "\n\n";
		String prices = "\n\n\n\n";

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
		names += "\nTOTAAL:\n\nBedankt voor\nuw steun";
		prices += "- - - - -\n\u20AC " + order.getTotalPrice() + "0";

		printTicket(quantities, names, prices);
	}
	
	private void printTicket(String quantities, String names, String prices) {
		QPainter painter = new QPainter();
		painter.begin(m_printer);
		int width_fraction = painter.window().width() / 7;
		painter.setFont(new QFont(null, 9));

		painter.drawText(0, 0, 1000, 1000, 0, quantities);
		painter.drawText(width_fraction, 0, 1000, 1000, 0, names);
		painter.drawText(width_fraction * 5, 0, 1000, 1000, 0, prices);
		
		painter.end();
		cut();
	}
	
	private void cut() {
		DocPrintJob job = m_service.createPrintJob();
		// TODO find codes for cutting paper
		byte[] bytes = {27, 100, 3};
		DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
		Doc doc = new SimpleDoc(bytes, flavor, null);
		try {
			job.print(doc, null);
			System.out.println("Sended Cut");
		} catch (PrintException e) {
			e.printStackTrace();
		}
	}
	
	private double round(double input) {
		return Math.round(input * 100) / 100.0;
	}
}