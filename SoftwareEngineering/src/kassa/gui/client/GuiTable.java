package kassa.gui.client;

import kassa.core.Tables;
import kassa.core.orders.TableOrders;

import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.gui.QFont;
import com.trolltech.qt.gui.QFrame;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QVBoxLayout;

/**
 * Table section of Screen
 * 
 * @author Stephen Pauwels
 */
public class GuiTable extends QFrame {

	/**
	 * signal emmitted when OK button has been pressed
	 */
	public final Signal0 Accept = new Signal0();

	/* System */
	private TableOrders m_orders;
	private Tables m_tables;
	
	private int m_tableNr;

	/* GUI + qt */
	private QLabel m_tableNrLabel;
	private QLabel m_tableOrders;
	private QLabel m_total;

	/**
	 * Constructor without parent
	 */
	public GuiTable(Tables tables) {
		// Init members
		m_tables = tables;
		m_orders = null;
		m_tableNr = 0;
		
		drawGui();
	}
	
	/**
	 * Draw GUI
	 */
	public void drawGui() {
		QVBoxLayout layout = new QVBoxLayout();

		m_tableNrLabel = new QLabel();
		m_tableNrLabel.setFont(new QFont(null, 12));
		m_tableNrLabel.setAlignment(AlignmentFlag.AlignTop);
		m_tableNrLabel.setFixedHeight(50);
		m_tableOrders = new QLabel();
		m_tableOrders.setFont(new QFont(null, 12));
		m_tableOrders.setAlignment(AlignmentFlag.AlignTop);
		m_total = new QLabel();
		m_total.setAlignment(AlignmentFlag.AlignTop);
		m_total.setFixedHeight(50);
		drawTable();

		layout.addWidget(m_tableNrLabel);
		layout.addWidget(m_tableOrders);
		layout.addWidget(m_total);

		setLayout(layout);
	}
	
	/**
	 * Update Tables
	 */
	public void updateTables(Tables tables) {
		m_tables = tables;
	}

	/**
	 * Update current table and draw table in the view
	 */
	public void updateTable(int tableNr) {
		if (tableNr == 0 || m_tables.getTable(tableNr).getItems().size() == 0) {
			m_orders = null;
		} else {
			m_orders = m_tables.getTable(tableNr);
		}
		
		m_tableNr = tableNr;
		drawTable();
	}

	/**
	 * Draw the current table in the table view
	 */
	private void drawTable() {
		if (m_tableNr != 0)
			m_tableNrLabel.setText("Tafel: " + m_tableNr);
		else
			m_tableNrLabel.setText("");

		if (m_orders != null) {
			m_tableOrders.setText(m_orders.printTable());
			m_total.setText("TOTAAL: \t\u20AC " + m_orders.getTotalPrice());
		} else {
			m_tableOrders.setText("");
			m_total.setText("");
		}
	}
}
