package kassa.gui.dialog;

import java.sql.SQLException;

import kassa.core.Tables;
import kassa.core.io.IOA5Printer;
import kassa.core.io.IODetectDB;
import kassa.core.io.IOPrinter;
import kassa.core.io.IOTicketPrinter;
import kassa.core.items.Items;
import kassa.core.storage.Storage;

import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QDialogButtonBox;
import com.trolltech.qt.gui.QFormLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QRadioButton;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;
import com.trolltech.qt.gui.QFormLayout.ItemRole;

/**
 * Load System Dialog
 * 
 * @author Stephen Pauwels
 */
@SuppressWarnings("unused")
public class GuiLoadSystem extends QDialog {

	private IODetectDB m_detect;
	private String m_addr; 
	
	private Storage m_database;
	private Items m_items;
	private Tables m_tables;
	private IOPrinter m_printer;
	
	private QFormLayout m_form;
	private QLineEdit m_databasePath;
	
	private QRadioButton m_a5Printer;
	private QRadioButton m_ticketPrinter;
	
	private QPushButton m_acceptButton;

	private boolean m_print;
	private boolean m_loadedSet;
	private boolean m_printerSet;

	/**
	 * Constructor without parent
	 */
	public GuiLoadSystem(boolean print) {
		this(null);
		
		m_detect = new IODetectDB();
		m_detect.locationReceived.connect(this, "locationReceived(String)");
		
		m_print = print;
		m_printerSet = false;
		m_loadedSet = false;
		drawGui();
		
		m_addr = "localhost";
		((QLabel) m_form.itemAt(1, ItemRole.FieldRole).widget()).setText("//" + m_addr + "/");
	}

	/**
	 * Constructor taking a parent Frame
	 * 
	 * @param parent
	 *            the Frame's parent
	 */
	public GuiLoadSystem(QWidget parent) {
		super(parent);
	}

	private void checkInput() {
		if ((m_printerSet || !m_print) && m_loadedSet)
			m_acceptButton.setDisabled(false);
	}

	private void drawGui() {
		setWindowTitle("Load");
		QVBoxLayout layout = new QVBoxLayout();
		m_form = new QFormLayout();

		m_databasePath = new QLineEdit("Database");
		m_form.insertRow(0, new QLabel("Naam"),  m_databasePath);
		
		m_form.insertRow(1, new QLabel("Server"), new QLabel(""));
		
		QPushButton button = new QPushButton("Zoek server...");
		button.clicked.connect(this, "searchServer()");
		m_form.insertRow(2, new QLabel("Zoek"), button);
		
		button = new QPushButton("Kies...");
		button.clicked.connect(this, "loadSystem()");
		m_form.insertRow(3, new QLabel("Open Systeem"), button);
		
		if (m_print) {
			button = new QPushButton("Kies...");
			button.clicked.connect(this, "selectPrinter()");
			m_form.insertRow(4, new QLabel("Selecteer Printer"), button);
			
			m_a5Printer = new QRadioButton();
			m_ticketPrinter = new QRadioButton();
			m_ticketPrinter.setChecked(true);
			m_form.insertRow(5, new QLabel("Printer"), m_a5Printer);
			m_form.insertRow(6, new QLabel("Ticket"), m_ticketPrinter);
		}

		// Button Box Initialization
		QDialogButtonBox buttonBox = new QDialogButtonBox(
				QDialogButtonBox.StandardButton.createQFlags(
						QDialogButtonBox.StandardButton.Ok,
						QDialogButtonBox.StandardButton.Cancel));
		buttonBox.accepted.connect(this, "accept()");
		buttonBox.rejected.connect(this, "reject()");
		m_acceptButton = buttonBox.button(QDialogButtonBox.StandardButton.Ok);
		m_acceptButton.setDisabled(true);
		buttonBox.setFixedHeight(100);

		layout.addLayout(m_form);
		layout.addWidget(buttonBox);
		
		setLayout(layout);
		
		QIcon icon = new QIcon();
		icon.addFile("classpath:icon_64.png");
		setWindowIcon(icon);
	}

	public Storage getDatabase() {
		return m_database;
	}

	public Items getItems() {
		return m_items;
	}

	public IOPrinter getPrinter() {
		return m_printer;
	}

	public Tables getTables() {
		return m_tables;
	}

	private void loadSystem() {
		try {
			String fileName = m_databasePath.text();
			if (fileName.equalsIgnoreCase(""))
				return;
			m_database = new Storage("//" + m_addr + "/" + fileName, "kassa", "kassa");

			m_items = m_database.readItems();
			m_tables = m_database.readTables(m_items);

			m_loadedSet = true;
			checkInput();
		} catch (ClassNotFoundException e) {
			QMessageBox.critical(this, "Driver niet gevonden",
					"De driver voor de database kon niet worden gevonden!");
		} catch (SQLException e) {
			QMessageBox.critical(this, "Database fout",
					"Volgende fout gebeurde in de database\n" + e.getMessage());
		}
	}

	private void selectPrinter() {
		if (m_a5Printer.isChecked()) {
			m_printer = new IOA5Printer();
		} else {
			m_printer = new IOTicketPrinter();
		}
		
		if (m_printer.printerAccept()) {
			m_printerSet = true;
		} else {
			m_printer = null;
			m_printerSet = false;
		}
		
		checkInput();
	}
	
	private void searchServer() {
		m_detect.requestLocation();
	}
	
	private void locationReceived(String addr) {
		m_addr = addr;
		((QLabel) m_form.itemAt(1, ItemRole.FieldRole).widget()).setText("//" + m_addr + "/");
	}
}
