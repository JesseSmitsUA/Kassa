package kassa.gui.master;

import java.io.IOException;
import java.sql.SQLException;

import kassa.core.Tables;
import kassa.core.exceptions.MobileException;
import kassa.core.io.Mobile;
import kassa.core.io.IOPrinter;
import kassa.core.items.Item;
import kassa.core.items.Items;
import kassa.core.orders.Order;
import kassa.core.orders.TableOrders;
import kassa.core.storage.Storage;
import kassa.gui.GuiAbstractWorkspace;
import kassa.gui.GuiInput;
import kassa.gui.GuiSelector;
import kassa.gui.Updater;
import kassa.gui.dialog.GuiAdmin;
import kassa.gui.dialog.GuiConnectionLost;
import kassa.gui.dialog.GuiPassword;
import kassa.gui.dialog.GuiReturn;
import kassa.gui.dialog.GuiSplit;
import kassa.gui.dialog.GuiTicket;

import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QMessageBox;

/**
 * Most upper part of the Screen to control the system
 * 
 * @author Stephen Pauwels
 */
@SuppressWarnings("unused")
public class GuiWorkspace extends GuiAbstractWorkspace{
	/* System */
	private Storage m_database;
	private Items m_items;
	private Tables m_tables;
	
	private IOPrinter m_printer;
	
	private int m_tableNr;
	private Order m_current_order;

	/* GUI + qt */
	private QHBoxLayout m_layout;
	private GuiInput m_input;
	private GuiSelector m_selector;
	private GuiTable m_table;
	
	private Updater m_updater;
	private Mobile m_mobile_server;

	/**
	 * Constructor
	 * @throws SQLException 
	 */
	public GuiWorkspace(Storage database, Items items, Tables tables, IOPrinter printer) throws SQLException {		
		super(null);
		
		// Init members
		m_database = database;
		m_items = items;
		m_tables = tables;
		m_printer = printer;
		
		try {
			m_mobile_server = new Mobile(m_items, m_tables);
			m_mobile_server.NewOrder.connect(this, "receiveOrder(Order, Integer)");
		} catch (MobileException e) {
			QMessageBox.critical(this, "Mobile fout",
					"Kan server niet starten!");
		}

		// Prepare new order
		m_current_order = new Order();

		/* Init gui when all variables are initialized */
		if (m_items != null && m_tables != null) {
			drawGui();
			updateTable(0);
		}
		
		m_updater = new Updater(this);
	}
	
	/**
	 * Draw GUI
	 * @throws SQLException 
	 */
	private void drawGui() throws SQLException {
		// Set layout
		QHBoxLayout layout = new QHBoxLayout();
		setFrameStyle(1);

		m_input = new GuiInput();
		m_input.setFixedWidth(290);

		m_selector = new GuiSelector(m_items, m_tables);
		m_selector.setMinimumWidth(350);

		m_table = new GuiTable(m_tables);
		m_table.setFixedWidth(250);
		m_table.setFrameStyle(1);

		layout.addWidget(m_input);
		layout.addWidget(m_selector);
		layout.addWidget(m_table);

		m_layout = layout;
		setLayout(layout);
		
		// Connections
		m_input.Accept.connect(this, "acceptOrder()");
		m_input.Clear.connect(this, "clearOrder()");
		
		m_selector.itemAccepted.connect(this, "addItem(Item)");
		m_selector.tableChanged.connect(this, "updateTable(int)");
		
		m_table.Accept.connect(this, "payTable()");
		m_table.Print.connect(this, "printTable()");
		m_table.Reconnect.connect(this, "toNormalMode()");
		m_table.Split.connect(this, "splitTable()");
		m_table.Admin.connect(this, "showAdmin()");
		m_table.NameChanged.connect(this, "nameChanged(String)");
		m_table.RequestStatus.connect(this, "statusRequest()");
	}
	
	/**
	 * Close system
	 */
	public boolean closeSystem() {
		if (m_tables == null) {
			m_updater.stopTimer();
			return true;
		} else if (m_tables.getEmpty().size() == m_tables.getNrTables()) {
			m_updater.stopTimer();
			return true;
		} else if (m_tables.getEmpty().size() != m_tables.getNrTables()) {
			QMessageBox.StandardButton button = QMessageBox.question(
							this,
							"Programma sluiten",
							"Er zijn nog onbetaalde tafels, bent u zeker dat u wilt afsluiten?",
							new QMessageBox.StandardButtons(
									QMessageBox.StandardButton.Ok,
									QMessageBox.StandardButton.Cancel));
			if (button == QMessageBox.StandardButton.Ok) {
				m_updater.stopTimer();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Update orders from database
	 */
	public void updateOrders() {
		try {
			m_tables = m_database.readTables(m_items);
			m_selector.updateTables(m_tables);
			m_table.updateTables(m_tables);
			m_selector.refresh();
		} catch (SQLException e) {
			e.printStackTrace();
			toLocalMode();
		}
	}
	
	/**
	 * Close connection with database
	 */
	public void closeDatabase() throws SQLException {
		if (m_database != null) {
			m_database.close();
		}
	}
	
	private void addItem(Item item) {
		int quantity = m_input.getNumberAndReset();
		m_current_order.addItem(item, quantity);
		
		m_input.updateView(m_current_order);
	}
	
	/**
	 * Accept order and write to database
	 * 
	 * @param order	Order to accept
	 */
	private void acceptOrder() {
		if (m_current_order.notEmpty()) {
			// Add current_order to Tables and to Database
			m_tables.getTable(m_tableNr).addOrder(m_current_order);

			try {
				m_database.addOrder(m_tableNr, m_current_order);
			} catch (SQLException e) {
				e.printStackTrace();
				toLocalMode();
			}

			if (m_current_order.notEmpty() && m_printer != null)
				m_printer.printOrder(m_current_order, m_tableNr, m_database.getOrderNr());	

			// Reset all views and settings
			m_current_order = new Order();
			m_input.updateView(m_current_order);
		}

		m_selector.toTables();
	}

	private void clearOrder() {
		m_current_order.clear();
		m_input.updateView(m_current_order);
	}
	
	/**
	 * Receive order from mobile
	 */
	private void receiveOrder(Order order, Integer table) {
		m_tables.getTable(table).addOrder(order);
		
		try {
			m_database.addOrder(table, order);
		} catch (SQLException e) {
			e.printStackTrace();
			toLocalMode();
		}
		
		if (order.notEmpty() && m_printer != null)
			m_printer.printOrder(order, table, m_database.getOrderNr());
		
	}

	/**
	 * update total system
	 * @throws SQLException 
	 */
	private void updateSystem() {
		m_layout.removeWidget(m_selector);
		m_selector.dispose();

		m_selector = new GuiSelector(m_items, m_tables);
		m_selector.setMinimumWidth(350);
		
		m_selector.itemAccepted.connect(this, "addItem(Item)");
		m_selector.tableChanged.connect(this, "updateTable(int)");

		m_layout.insertWidget(1, m_selector);

		m_selector.toTables();
	}
	
	/**
	 * Go to payment view for table
	 */
	private void payTable() {
		TableOrders table = m_tables.getTable(m_tableNr);
		GuiTicket ticket = new GuiTicket(table);
		if (ticket.exec() == QDialog.DialogCode.Accepted.value()) {
			if (m_printer != null) {
				m_printer.printTicket(table, m_tableNr);
			}
			m_tables.closeTable(m_tableNr);
			try {
				m_database.payed(m_tableNr);
			} catch (SQLException e) {
				e.printStackTrace();
				toLocalMode();
			}
		} else {
			// Return without closing the table
		}
		
		m_selector.toTables();
	}
	
	private void printTable() {
		TableOrders table = m_tables.getTable(m_tableNr);
		GuiTicket ticket = new GuiTicket(table);
		if (ticket.exec() == QDialog.DialogCode.Accepted.value()) {
			if (m_printer != null) {
				m_printer.printTicket(table, m_tableNr);
			}
		} else {
			// Return without printing the table
		}	
	}

	/**
	 * New table is selected
	 * 
	 * @param tableNr	New table nr
	 */
	private void updateTable(int tableNr) {		
		m_tableNr = tableNr;
		if (tableNr == 0) {
			m_input.disableKeypad(true);
		} else {
			m_input.disableKeypad(false);
			updateOrders();
			m_current_order = new Order();
		}
		m_input.updateView(m_current_order);
		m_table.updateTable(tableNr);
	}
	
	private void splitTable() {
		GuiSplit split = new GuiSplit(m_tables.getTable(m_tableNr), m_items, m_tables, m_database);
		if (split.exec() == QDialog.DialogCode.Accepted.value()) {
			updateTable(0);
			m_selector.toTables();
		} 
		else {

		}
	}
	
	private void statusRequest() {
		try {
			m_table.setStatus(m_database.readFoodOrder(m_tableNr));
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void showAdmin() {
		GuiAdmin admin = new GuiAdmin(m_database, m_tables, m_items);
		admin.DataChanged.connect(this, "updateSystem()");
		admin.exec();
	}
	
	public boolean toLocalMode() {
		try {
			m_database.switchToLocalDatabase();
			m_database.migrateDatabase(m_tables, m_items, "backup");
			m_table.setModus(Storage.working_mode.LOCAL_MODE);
			QMessageBox.critical(this, "Verbinding verbroken", "De verbinding met de server is verbroken, " +
					"overgeschakeld naar lokale modus.");
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean toNormalMode() {
		try {
			m_database.restoreDatabaseConnection();
			m_database.migrateDatabase(m_tables, m_items, "password");
			m_table.setModus(Storage.working_mode.NORMAL_MODE);
			QMessageBox.information(this, "Verbinding hersteld", "De verbinding is opnieuw tot stand gebracht. " +
					"Geschiedenis niet meer beschikbaar.");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean freeze() {
		return true;
	}
	
	public boolean initMobileMode() {
		return true;
	}
	
	public void nameChanged(String name) {
		try {
			m_database.changeName(m_tableNr, name);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
