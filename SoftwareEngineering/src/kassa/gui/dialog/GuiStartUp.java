package kassa.gui.dialog;

import kassa.core.Tables;
import kassa.core.io.IOPrinter;
import kassa.core.items.Items;
import kassa.core.storage.Storage;

import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

/**
 * StartUp Dialog
 * 
 * @author Stephen Pauwels
 */
@SuppressWarnings("unused")
public class GuiStartUp extends QDialog {

	private Storage m_database;
	private Tables m_tables;
	private Items m_items;
	private IOPrinter m_printer;

	private GuiLoadSystem m_loadSystem;
	private GuiSetUp m_setUp;

	private QCheckBox m_clientBox;
	private QCheckBox m_printBox;
	private QPushButton m_newSystem;
	private QPushButton m_existingSystem;
	private QComboBox m_systemBox;
	
	private boolean m_client;
	private boolean m_print;
	
	/**
	 * Constructor without parent
	 */
	public GuiStartUp() {
		this(null);
	}

	/**
	 * Constructor taking a parent Frame
	 * 
	 * @param parent
	 *            the Frame's parent
	 */
	public GuiStartUp(QWidget parent) {
		super(parent);
		drawGui();
		m_print = true;
	}

	private void drawGui() {
		setWindowTitle("Start");
		QVBoxLayout layout = new QVBoxLayout();

		QLabel name = new QLabel("KASSA");
		name.setAlignment(AlignmentFlag.AlignHCenter);
		layout.addWidget(name);
		
		m_systemBox = new QComboBox();
		m_systemBox.currentIndexChanged.connect(this, "systemChanged()");
		m_systemBox.addItem("Kassa");
		m_systemBox.addItem("Reserveringen");
		m_systemBox.addItem("Eten");
		layout.addWidget(m_systemBox);
		
		QHBoxLayout clientCheckLayout = new QHBoxLayout();
		m_clientBox = new QCheckBox();
		m_clientBox.toggled.connect(this, "clientCheck()");
		clientCheckLayout.addWidget(new QLabel("Client-mode?"));
		clientCheckLayout.addWidget(m_clientBox);
		layout.addLayout(clientCheckLayout);

		QHBoxLayout printCheckLayout = new QHBoxLayout();
		m_printBox = new QCheckBox();
		m_printBox.toggled.connect(this, "printCheck()");
		m_printBox.setChecked(true);
		printCheckLayout.addWidget(new QLabel("Print-mode?"));
		printCheckLayout.addWidget(m_printBox);
		layout.addLayout(printCheckLayout);
		
		m_newSystem = new QPushButton("Begin een nieuw systeem");
		m_newSystem.clicked.connect(this, "newSystem()");
		layout.addWidget(m_newSystem);

		m_existingSystem = new QPushButton("Open een bestaand systeem");
		m_existingSystem.clicked.connect(this, "existingSystem()");
		layout.addWidget(m_existingSystem);
		
		QPushButton item_button = new QPushButton("Item editor");
		item_button.clicked.connect(this, "itemEditor()");
		layout.addWidget(item_button);

		QPushButton button = new QPushButton("Sluiten");
		button.clicked.connect(this, "close()");
		layout.addWidget(button);

		setLayout(layout);
		
		QIcon icon = new QIcon();
		icon.addFile("classpath:icon_64.png");
		setWindowIcon(icon);
	}

	private void existingSystem() {
		m_loadSystem = new GuiLoadSystem(m_print);

		if (m_loadSystem.exec() == QDialog.DialogCode.Accepted.value()) {
			m_printer = m_loadSystem.getPrinter();
			m_items = m_loadSystem.getItems();
			m_tables = m_loadSystem.getTables();
			m_database = m_loadSystem.getDatabase();
			m_database.loadSystem();

			close();
		}
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
	
	public boolean getClientMode() {
		return m_client;
	}
	
	/**
	 * Return the system mode
	 * 0: Kassa
	 * 1: Reserveringen
	 * 2: Eten
	 * 
	 * @return	Number of system
	 */
	public int getSystemMode() {
		return m_systemBox.currentIndex();
	}

	private void newSystem() {
		m_setUp = new GuiSetUp(m_print);

		if (m_setUp.exec() == QDialog.DialogCode.Accepted.value()) {
			m_printer = m_setUp.getPrinter();
			m_items = m_setUp.getItems();
			m_tables = m_setUp.getTables();
			m_database = m_setUp.getDatabase();

			close();
		}
	}
	
	private void clientCheck() {
		m_client = m_clientBox.isChecked();
		if (m_client) {
			m_newSystem.setDisabled(true);
		}
		else {
			m_newSystem.setDisabled(false);
		}
	}
	
	private void printCheck() {
		m_print = m_printBox.isChecked();
	}
	
	private void systemChanged() {
		if (m_clientBox == null || m_printBox == null)
			return;
		
		if (m_systemBox.currentIndex() != 0) {
			m_printBox.setEnabled(false);
			m_printBox.setChecked(false);
			m_clientBox.setEnabled(false);
			m_clientBox.setChecked(false);
			
			m_newSystem.setEnabled(false);
		}
		else {
			m_printBox.setEnabled(true);
			m_clientBox.setEnabled(true);	
			m_newSystem.setEnabled(true);
		}
	}
	
	private void itemEditor() {
		NewGuiAddItems dialog = new NewGuiAddItems(null);
		dialog.exec();
	}
}
