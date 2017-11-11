package kassa.gui.client;

import java.sql.SQLException;

import kassa.core.Tables;
import kassa.core.io.IOPrinter;
import kassa.core.items.Item;
import kassa.core.items.Items;
import kassa.core.orders.Order;
import kassa.core.storage.Storage;
import kassa.gui.GuiAbstractWorkspace;
import kassa.gui.GuiInput;
import kassa.gui.GuiSelector;
import kassa.gui.Updater;
import kassa.gui.dialog.GuiConnectionLost;
import kassa.gui.dialog.GuiProgress;

import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QMessageBox;

/**
 * Most upper part of the Screen to control the system
 * 
 * @author Stephen Pauwels
 */
@SuppressWarnings("unused")
public class GuiWorkspace extends GuiAbstractWorkspace {
	/* System */
	private Storage m_database;
	private Items m_items;
	private Tables m_tables;
	
	private int m_tableNr;
	private Order m_current_order;
	
	private IOPrinter m_printer;
	
	/* GUI + qt */
	private GuiInput m_input;
	private GuiSelector m_selector;
	private GuiTable m_table;
	
	private Updater m_updater;

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

		setLayout(layout);

		// Connections
		m_input.Accept.connect(this, "acceptOrder()");
		m_input.Clear.connect(this, "clearOrder()");
		
		m_selector.itemAccepted.connect(this, "addItem(Item)");
		m_selector.tableChanged.connect(this, "updateTable(int)");
		
		m_table.Accept.connect(this, "tableClosed()");
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
			QMessageBox.StandardButton button = QMessageBox
					.question(
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
	public void updateOrders() throws SQLException  {
		m_tables = m_database.readTables(m_items);
		m_selector.updateTables(m_tables);
		m_table.updateTables(m_tables);
		m_selector.refresh();
	}
	
	/**
	 * Close connection with database
	 */
	public void closeDatabase() throws SQLException {
		if (m_database != null) {
			m_database.close();
		}
	}
	
	private void addItem(Item item) throws SQLException {
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
			GuiProgress progress = GuiProgress.getProgress();

			progress.start();
			// Add current_order to Tables and to Database
			m_tables.getTable(m_tableNr).addOrder(m_current_order);

			try {
				m_database.addOrder(m_tableNr, m_current_order);
			} catch (SQLException e) {
				toLocalMode();
				return;
			}

			progress.setValue(33);

			if (m_current_order.notEmpty() && m_printer != null)
				m_printer.printOrder(m_current_order, m_tableNr, m_database.getOrderNr());	

			progress.setValue(66);

			// Reset all views and settings
			m_current_order = new Order();
			m_input.updateView(m_current_order);

			try {
				updateOrders();
			} catch (SQLException e) {
				toLocalMode();
				return;
			}

			progress.setValue(100);
			progress.stop();
		}
		
		m_selector.toTables();
	}
	
	private void clearOrder() {
		m_current_order.clear();
		m_input.updateView(m_current_order);
	}

	/**
	 * Inform a table is closed
	 */
	private void tableClosed() {
		m_selector.toTables();
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
			m_current_order = new Order();
		}
		m_input.updateView(m_current_order);
		m_table.updateTable(tableNr);
	}
	
	public boolean toLocalMode() {
		this.setDisabled(true);
		QMessageBox.critical(this, "Verbinding verbroken", "Verbinding met server is verbroken." +
				" Client applicatie kan niet meer gebruikt worden. Let op: de laatst ingegeven bestelling" +
				" is niet opgenomen in de database!");
		return true;
	}
	
	public boolean freeze() {
		return true;
	}
	
	public boolean initMobileMode() {
		return true;
	}
}
