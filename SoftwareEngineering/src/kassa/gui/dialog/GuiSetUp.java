package kassa.gui.dialog;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import com.trolltech.qt.gui.QFormLayout.ItemRole;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QRadioButton;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

/**
 * Set Up Dialog
 * 
 * @author Stephen Pauwels
 */
@SuppressWarnings("unused")
public class GuiSetUp extends QDialog {

	private IODetectDB m_detect;
	private String m_addr; 
	
	private Storage m_database;
	private Items m_items;
	private Tables m_tables;
	private IOPrinter m_printer;
	
	private QFormLayout m_form;
	private QLineEdit m_databasePath;

	private QPushButton m_itemChoose;
	private QSpinBox m_tableNrs;
	
	private QRadioButton m_a5Printer;
	private QRadioButton m_ticketPrinter;

	private QPushButton m_acceptButton;

	private boolean m_print;
	private boolean m_databaseSet;
	private boolean m_itemsSet;
	private boolean m_printerSet;
	private boolean m_tablesSet;
	
	/**
	 * Constructor without parent
	 */
	public GuiSetUp(boolean print) {
		this(null);
		
		m_detect = new IODetectDB();
		m_detect.locationReceived.connect(this, "locationReceived(String)");
		
		m_print = print;
		m_printerSet = false;
		m_itemsSet = false;
		m_tablesSet = false;
		drawGui();
		
		m_addr = "localhost";
		((QLabel) m_form.itemAt(1, ItemRole.FieldRole).widget()).setText("//" + m_addr + "/");
	}

	/**
	 * Constructor taking a parent Frame
	 * 
	 * @param parent	the Frame's parent
	 */
	public GuiSetUp(QWidget parent) {
		super(parent);
	}
	
	private void drawGui() {
		setWindowTitle("Set-Up");
		QVBoxLayout layout = new QVBoxLayout();
		m_form = new QFormLayout();

		m_databasePath = new QLineEdit("Database");
		m_form.insertRow(0, new QLabel("Naam"), m_databasePath);
		
		m_form.insertRow(1, new QLabel("Server"), new QLabel(""));
		
		QPushButton button = new QPushButton("Zoek server...");
		button.clicked.connect(this, "searchServer()");
		m_form.insertRow(2, new QLabel("Zoek"), button);

		button = new QPushButton("Selecteer...");
		button.clicked.connect(this, "selectDatabase()");
		m_form.insertRow(3, new QLabel("Selecteer database"), button);

		m_itemChoose = new QPushButton("Kies...");
		m_itemChoose.clicked.connect(this, "selectItems()");
		m_itemChoose.setDisabled(true);
		m_form.insertRow(4, new QLabel("Selecteer Items"), m_itemChoose);

		m_tableNrs = new QSpinBox();
		m_tableNrs.setDisabled(true);
		m_tableNrs.setMinimum(0);
		m_tableNrs.valueChanged.connect(this, "selectTables(int)");
		m_form.insertRow(5, new QLabel("Aantal tafels:"), m_tableNrs);
		
		if (m_print) {
			button = new QPushButton("Kies...");
			button.clicked.connect(this, "selectPrinter()");
			m_form.insertRow(7, new QLabel("Selecteer Printer"), button);
			
			m_a5Printer = new QRadioButton();
			m_ticketPrinter = new QRadioButton();
			m_ticketPrinter.setChecked(true);
			m_form.insertRow(8, new QLabel("Printer"), m_a5Printer);
			m_form.insertRow(9, new QLabel("Ticket"), m_ticketPrinter);
		}

		// Button Box Initialization
		QDialogButtonBox buttonBox = new QDialogButtonBox(
				QDialogButtonBox.StandardButton.createQFlags(
						QDialogButtonBox.StandardButton.Ok,
						QDialogButtonBox.StandardButton.Cancel));
		m_acceptButton = buttonBox.button(QDialogButtonBox.StandardButton.Ok);
		m_acceptButton.setDisabled(true);
		buttonBox.accepted.connect(this, "acceptInput()");
		buttonBox.rejected.connect(this, "rejectInput()");
		buttonBox.setFixedHeight(100);

		layout.addLayout(m_form);
		layout.addWidget(buttonBox);
		
		setLayout(layout);
		
		QIcon icon = new QIcon();
		icon.addFile("classpath:icon_64.png");
		setWindowIcon(icon);
	}

	private void acceptInput() throws SQLException {
		m_database.setTables(m_tables.getNrTables());
		accept();
	}

	private void rejectInput() {
		reject();
	}

	private void checkInput() {
		if (m_databaseSet) {
			m_tableNrs.setDisabled(false);
			m_itemChoose.setDisabled(false);
		}
		else {
			m_tableNrs.setDisabled(true);
			m_itemChoose.setDisabled(true);
		}
		
		if (m_databaseSet && (m_printerSet || !m_print) && m_itemsSet && m_tablesSet)
			m_acceptButton.setDisabled(false);
		else
			m_acceptButton.setDisabled(true);
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

	private void selectDatabase() {
		try {
			String fileName = m_databasePath.text();
			if (fileName.compareTo("") != 0) {
				File file = new File(fileName);
				if (file.exists())
					file.delete();

				m_database = new Storage("//" + m_addr + "/" + fileName, "kassa", "kassa");
				m_database.newSystem();
				m_databaseSet = true;
			}
		} catch (ClassNotFoundException e) {
			QMessageBox.critical(this, "Driver niet gevonden",
					"De driver voor de database kon niet worden gevonden!");
		} catch (SQLException e) {
			QMessageBox.critical(this, "Database fout",
					"Volgende fout gebeurde in de database\n" + e.getMessage());
		}
		checkInput();
	}

	private void selectItems() {
		if (m_database == null) {
			QMessageBox.critical(this, "Geen database",
					"Gelieve eerst een database te selecteren!");
			return;
		}
		if (m_items == null)
			m_items = new Items(m_database);
		NewGuiAddItems addItems = new NewGuiAddItems(m_items);
		addItems.exec();
		if (m_items.getNrItems() > 0)
			m_itemsSet = true;
		else
			m_itemsSet = false;
		checkInput();
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

	private void selectTables(int value) {
		if (m_database == null) {
			QMessageBox.critical(this, "Geen database",
					"Gelieve eerst een database te selecteren!");
			m_tableNrs.setValue(0);
			return;
		}
		m_tables = new Tables(value);
		if (value != 0)
			m_tablesSet = true;
		else
			m_tablesSet = false;
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
