package kassa.gui.master;

import java.sql.SQLException;
import java.util.ArrayList;

import kassa.core.Tables;
import kassa.core.items.Items;
import kassa.core.orders.TableOrders;
import kassa.core.storage.Storage;

import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QFont;
import com.trolltech.qt.gui.QFrame;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QInputDialog;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

/**
 * Table section of Screen
 * 
 * @author Stephen Pauwels
 */
@SuppressWarnings("unused")
public class GuiTable extends QFrame {

	/**
	 * signal emmitted when OK button has been pressed
	 */
	public final Signal0 Accept = new Signal0();
	public final Signal0 Reconnect = new Signal0();
	
	public final Signal0 RequestStatus = new Signal0();
	
	public final Signal0 Split = new Signal0();
	public final Signal0 Admin = new Signal0();
	public final Signal0 Print = new Signal0();
	
	public final Signal1<String> NameChanged = new Signal1<String>();

	/* System */
	private TableOrders m_orders;
	private Tables m_tables;
	
	private int m_tableNr;
	private String m_tableName;

	/* GUI + qt */
	private QPushButton[] m_tableActionButtons;
	private QWidget m_tableActions;
	private QLabel m_tableNrLabel;
	private QLabel m_tableNameLabel;
	private QLabel m_tableOrders;
	private QLabel m_total;
	
	private QLabel m_modus;
	private QPushButton m_reconnect;
	private QLabel m_mobileInformation;
	
	private boolean m_showReturn;

	/**
	 * Constructor taking a parent Frame
	 * 
	 * @param parent the Frame's parent
	 */
	public GuiTable(QWidget parent, Tables tables) {
		super(parent);
		
		// Init members
		m_tables = tables;
		m_orders = null;
		m_tableNr = 0;
		
		m_showReturn = true;
		
		drawGui();
	}
	
	/**
	 * Constructor without parent
	 */
	public GuiTable(Tables tables) {
		this(null, tables);
	}
	
	/**
	 * Draw GUI
	 */
	public void drawGui() {
		QVBoxLayout layout = new QVBoxLayout();

		QPushButton button = new QPushButton("Admin");
		button.clicked.connect(Admin);
		button.setFixedHeight(50);
		layout.addWidget(button);
		
		// Modus
		m_modus = new QLabel("Modus: Normaal");
		m_modus.setFixedHeight(20);
		layout.addWidget(m_modus);
		
		m_reconnect = new QPushButton("Connectie maken");
		m_reconnect.clicked.connect(Reconnect);
		m_reconnect.setVisible(false);
		layout.addWidget(m_reconnect);
		
		// Mobile display
		QLabel mobileTitle = new QLabel("Mobiel:");
		mobileTitle.setFixedHeight(20);
		m_mobileInformation = new QLabel();
		m_mobileInformation.setFixedHeight(40);
		m_mobileInformation.setAlignment(AlignmentFlag.AlignTop);
		m_mobileInformation.setText("Geconnecteerde devices: ---");
		layout.addWidget(mobileTitle);
		layout.addWidget(m_mobileInformation);

		// Table display
		m_tableNrLabel = new QLabel();
		m_tableNrLabel.setFont(new QFont(null, 12));
		m_tableNrLabel.setAlignment(AlignmentFlag.AlignTop);
		m_tableNrLabel.setFixedHeight(50);
		m_tableNameLabel = new QLabel();
		m_tableNameLabel.setFont(new QFont(null, 12));
		m_tableNameLabel.setAlignment(AlignmentFlag.AlignTop);
		m_tableNameLabel.setFixedHeight(50);
		m_tableOrders = new QLabel();
		m_tableOrders.setFont(new QFont(null, 12));
		m_tableOrders.setAlignment(AlignmentFlag.AlignTop);
		m_total = new QLabel();
		m_total.setAlignment(AlignmentFlag.AlignTop);
		m_total.setFixedHeight(50);
		drawTable();

		// Action buttons
		m_tableActionButtons = new QPushButton[4];
		button = new QPushButton("Afrekenen");
		button.clicked.connect(Accept);
		button.setDisabled(true);
		m_tableActionButtons[0] = button;
		button = new QPushButton("Split");
		button.clicked.connect(Split);
		button.setDisabled(true);
		m_tableActionButtons[1] = button;
		button = new QPushButton("Print");
		button.clicked.connect(Print);
		button.setDisabled(true);
		m_tableActionButtons[3] = button;

		m_tableActions = new QWidget();
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addWidget(m_tableActionButtons[0]);
		buttonLayout.addWidget(m_tableActionButtons[1]);
		m_tableActions.setLayout(buttonLayout);
		m_tableActions.setFixedHeight(75);

		// Add Layouts
		layout.addWidget(m_tableNrLabel);
		layout.addWidget(m_tableOrders);
		layout.addWidget(m_total);
		layout.addWidget(m_tableActions);
		
		button = new QPushButton("Geef naam in");
		button.clicked.connect(this, "enterName()");
		button.setFixedHeight(30);
		button.setDisabled(true);
		m_tableActionButtons[2] = button;
		layout.addWidget(button);

		setLayout(layout);
		
	}
	
	/**
	 * Update current table and draw table in the view
	 */
	public void updateTable(int tableNr) {
		if (tableNr == 0) {
			m_tableActionButtons[0].setDisabled(true);
			m_tableActionButtons[1].setDisabled(true);
			m_tableActionButtons[2].setDisabled(true);
			m_tableActionButtons[3].setDisabled(true);
			m_orders = null;
			m_tableName = "";
		} else if (m_tables.getTable(tableNr).getItems().size() == 0) {
			m_tableActionButtons[0].setDisabled(true);
			m_tableActionButtons[1].setDisabled(true);
			m_tableActionButtons[3].setDisabled(true);
			m_tableActionButtons[2].setDisabled(false);
			m_orders = m_tables.getTable(tableNr);
			m_tableName = m_orders.tableName();
		} else {
			m_orders = m_tables.getTable(tableNr);
			m_tableActionButtons[0].setDisabled(false);
			m_tableActionButtons[1].setDisabled(false);
			m_tableActionButtons[2].setDisabled(false);
			m_tableActionButtons[3].setDisabled(false);
			m_tableName = m_orders.tableName();
		}
		
		m_tableNr = tableNr;
		drawTable();
		RequestStatus.emit();
	}
	
	/**
	 * Update tables
	 */
	public void updateTables(Tables tables) {
		m_tables = tables;
	}

	/**
	 * Draw the current table in the table view
	 */
	private void drawTable() {
		if (m_tableNr != 0) {
			m_tableNrLabel.setText("Tafel: " + m_tableNr + "\nNaam: " + m_tableName);
		} else
			m_tableNrLabel.setText("");

		if (m_tableName == null || m_tableName.compareTo("") == 0)
			m_tableNameLabel.setText("");
		else
			m_tableNameLabel.setText("Naam: " + m_tableName);
		
		if (m_orders != null) {
			m_tableOrders.setText(m_orders.printTable());
			m_total.setText("TOTAAL: \t\u20AC " + m_orders.getTotalPrice());
		} else {
			m_tableOrders.setText("");
			m_total.setText("");
		}
	}
	
	public void setStatus(ArrayList<String> status) {
		String status_string = "\n\n";
		for (String line : status)
			status_string += line + "\n";
		String orig_text = m_tableOrders.text();
		m_tableOrders.setText(orig_text + status_string);
	}
	
	private void enterName() {
		String name = QInputDialog.getText(this, "Naam voor tafel", "Naam");
		if (m_orders != null)
			m_orders.setTableName(name);
		NameChanged.emit(name);
	}
	
	private void refreshMobile(int number) {
		m_mobileInformation.setText("Geconnecteerde devices: " + number);
	}
	
	public void setModus(Storage.working_mode modus) {
		if (modus == Storage.working_mode.NORMAL_MODE) {
			m_reconnect.setVisible(false);
			m_modus.setText("Modus: normaal");
		} else if (modus == Storage.working_mode.LOCAL_MODE) {
			m_reconnect.setVisible(true);
			m_modus.setText("Modus: lokaal");
		}
	}
}
