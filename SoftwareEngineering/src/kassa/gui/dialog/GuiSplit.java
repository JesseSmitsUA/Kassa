package kassa.gui.dialog;

import java.sql.SQLException;
import java.util.TreeMap;

import kassa.core.Tables;
import kassa.core.items.Item;
import kassa.core.items.Items;
import kassa.core.orders.Order;
import kassa.core.orders.TableOrders;
import kassa.core.storage.Storage;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QDialogButtonBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QRadioButton;
import com.trolltech.qt.gui.QStandardItemModel;
import com.trolltech.qt.gui.QTableView;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

/**
 * Split Dialog
 * 
 * @author Stephen Pauwels
 */
@SuppressWarnings("unused")
public class GuiSplit extends QDialog {

	private QComboBox m_availableTables;

	private Storage m_database;

	QStandardItemModel m_itemModel;

	private Items m_items;

	QTableView m_itemView;

	private TableOrders m_orders;

	private Tables m_tables;
	private QRadioButton m_toTableRadio;
	private boolean m_toTicket;
	private QRadioButton m_toTicketRadio;

	/**
	 * Constructor
	 */
	public GuiSplit(QWidget parent, TableOrders orders, Items items, Tables tables, Storage database) {
		super(parent);
		m_orders = orders;
		m_items = items;
		m_toTicket = true;
		m_tables = tables;
		m_database = database;
		drawGui();
	}

	/**
	 * Constructor
	 */
	public GuiSplit(TableOrders orders, Items items, Tables tables, Storage database) {
		this(null, orders, items, tables, database);
	}

	public void drawGui() {
		QVBoxLayout layout = new QVBoxLayout();

		m_itemView = new QTableView();
		m_itemView.setModel(makeModel(m_orders));
		layout.addWidget(m_itemView);

		QVBoxLayout splitActions = new QVBoxLayout();

		QHBoxLayout toTicketLayout = new QHBoxLayout();
		m_toTicketRadio = new QRadioButton(tr("Naar afrekening"));
		m_toTicketRadio.setChecked(true);
		m_toTicketRadio.toggled.connect(this, "updateTicketRadios()");
		toTicketLayout.addWidget(m_toTicketRadio);

		splitActions.addLayout(toTicketLayout);

		QHBoxLayout toTableLayout = new QHBoxLayout();
		m_toTableRadio = new QRadioButton(tr("Naar tafel"));
		toTableLayout.addWidget(m_toTableRadio);
		m_availableTables = new QComboBox();
		m_availableTables.setDisabled(true);
		for (int table = 1; table <= m_tables.getNrTables(); table++)
			m_availableTables.addItem("Tafel " + table);
		toTableLayout.addWidget(m_availableTables);

		splitActions.addLayout(toTableLayout);

		layout.addLayout(splitActions);

		// Button Box Initialization
		QDialogButtonBox buttonBox = new QDialogButtonBox(
				QDialogButtonBox.StandardButton.createQFlags(
						QDialogButtonBox.StandardButton.Ok,
						QDialogButtonBox.StandardButton.Cancel));
		buttonBox.accepted.connect(this, "accept()");
		buttonBox.rejected.connect(this, "reject()");
		buttonBox.setFixedHeight(100);
		layout.addWidget(buttonBox);

		accepted.connect(this, "split()");

		setLayout(layout);
		setFixedSize(400, 500);
		setWindowTitle("Split");
		
		QIcon icon = new QIcon();
		icon.addFile("classpath:icon_64.png");
		setWindowIcon(icon);
	}

	public QStandardItemModel makeModel(TableOrders orders) {
		m_itemModel = new QStandardItemModel(orders.nrOfItems(), 2, this);

		m_itemModel.setHeaderData(0, Qt.Orientation.Horizontal, "Naam");
		m_itemModel.setHeaderData(1, Qt.Orientation.Horizontal, "Aantal");

		TreeMap<Item, Integer> items = orders.getItems();
		Item item = items.firstKey();
		int i = 0;
		while (item != null) {
			m_itemModel.setData(i, 0, item.getName());
			m_itemModel.setData(i, 1, 0);
			i++;
			item = items.higherKey(item);
		}

		return m_itemModel;
	}

	private void split() {
		try {
			Order newOrder = new Order();
			for (int i = 0; i < m_itemModel.rowCount(); i++) {
				if (m_orders.getItems().get(m_items.getItemByName((String) m_itemModel.data(i, 0))) >= (Integer) m_itemModel.data(i, 1)) {
					newOrder.addItem(m_items.getItemByName((String) m_itemModel.data(i, 0)), (Integer) m_itemModel.data(i, 1));
				} else {
					QMessageBox.critical(this, "Items fout",
							"Niet voldoende items aanwezig om te splitsen!");
					return;
				}
			}

			if (m_toTicket) {
				TableOrders tempOrders = new TableOrders(0);
				tempOrders.addOrder(newOrder);

				GuiTicket ticket = new GuiTicket(tempOrders);
				if (ticket.exec() == QDialog.DialogCode.Accepted.value()) {
					m_orders.deleteOrder(newOrder);
					m_database.splitToPayed(newOrder, m_orders);
				}
				tempOrders.deleteOrders();
			} else {
				TableOrders table = m_tables.getTable(m_availableTables.currentIndex()+1);
				table.addOrder(newOrder);
				m_orders.deleteOrder(newOrder);
				m_database.split(table.tableNr(), newOrder, m_orders);
			}
		} catch (SQLException e) {
			QMessageBox.critical(this, "Database fout",
					"Volgende fout gebeurde in de database\n" + e.getMessage());
		}
	}

	private void updateTicketRadios() {
		if (m_toTicketRadio.isChecked()) {
			m_toTicket = true;
			m_availableTables.setDisabled(true);
		} else {
			m_toTicket = false;
			m_availableTables.setDisabled(false);
		}
	}
}
