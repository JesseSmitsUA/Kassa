package kassa.gui.dialog;

import java.sql.SQLException;
import java.util.TreeMap;

import kassa.core.Tables;
import kassa.core.items.Item;
import kassa.core.items.Items;
import kassa.core.orders.Order;
import kassa.core.orders.TableOrders;
import kassa.core.storage.Storage;

import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QStandardItemModel;
import com.trolltech.qt.gui.QTableView;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

/**
 * Admin Dialog
 * 
 * @author Stephen Pauwels
 */
@SuppressWarnings("unused")
public class GuiAdmin extends QDialog {
	
	private enum view_mode { CURRENT_TABLES_MODE, HISTORY_MODE }

	/**
	 * signal emmitted when data is changed
	 */
	public final Signal0 DataChanged = new Signal0();

	private view_mode m_mode;
	private int m_selectedTable;

	private Storage m_database;
	private Tables m_tables;
	private Tables m_historyTables;
	private Items m_items;

	private QStandardItemModel m_orderModel;
	private QStandardItemModel m_historyModel;
	private QStandardItemModel m_itemModel;

	private QItemTableView m_topView;
	private QTableView m_bottomView;
	
	private GuiTotals m_totalsDial;
	private GuiAnalytics m_analyticsDial;

	/**
	 * Constructor taking a parent Frame
	 * 
	 * @param parent
	 *            the Frame's parent
	 */
	public GuiAdmin(QWidget parent, Storage database, Tables tables, Items items) {
		super(parent);
		GuiProgress progress = GuiProgress.getProgress();
		
		progress.start();
		m_database = database;
		m_selectedTable = -1;
		m_tables = tables;
		m_items = items;
		m_mode = view_mode.CURRENT_TABLES_MODE;
		
		
		progress.setValue(25);
		try {
			m_historyTables = m_database.readHistory(m_database.readItems());
		} catch (SQLException e) {
			QMessageBox.critical(this, "Database fout",
					"Volgende fout gebeurde in de database\n" + e.getMessage());
			m_historyTables = null;
		}
		
		progress.setValue(75);
		m_totalsDial = new GuiTotals(m_database);
		try {
			m_analyticsDial = new GuiAnalytics(m_items, m_database);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		progress.setValue(85);
		drawGui();
		
		progress.stop();
	}

	/**
	 * Constructor without parent
	 */
	public GuiAdmin(Storage database, Tables tables, Items items) {
		this(null, database, tables, items);
	}

	private void dataChanged(QModelIndex index1, QModelIndex index2) {
		try {
			if (m_mode == view_mode.HISTORY_MODE) {
				return;
			}
			Items items = m_database.readItems();
			TableOrders table = m_tables.getTable(m_selectedTable);
			table.deleteOrders();
			m_database.removeOrder(m_selectedTable);

			Order newOrder = new Order();
			for (int i = 0; i < m_itemModel.rowCount(); i++) {
				newOrder.addItem(
						items.getItemByName((String) m_itemModel.data(i, 0)),
						(Integer) m_itemModel.data(i, 1));
			}
			table.addOrder(newOrder);
			m_database.addOrder(m_selectedTable, newOrder);
			
			m_topView.setModel(getCurrentOrders());
			m_bottomView.setModel(getItems(m_tables, m_selectedTable));

			DataChanged.emit();
		} catch (SQLException e) {
			QMessageBox.critical(this, "Database fout",
					"Volgende fout gebeurde in de database\n" + e.getMessage());
		}
	}

	private void drawGui() {
		setWindowTitle("Admin");
		setMinimumWidth(400);
		
		QVBoxLayout layout = new QVBoxLayout();

		m_topView = new QItemTableView();
		m_topView.setSortingEnabled(true);
		m_topView.setModel(getCurrentOrders());
		m_topView.clicked.connect(this, "orderSelected(QModelIndex)");
		layout.addWidget(m_topView);

		m_bottomView = new QTableView();
		layout.addWidget(m_bottomView);

		QHBoxLayout upperButtonLayout = new QHBoxLayout();
		QPushButton button = new QPushButton("Items");
		button.clicked.connect(this, "toItems()");
//		upperButtonLayout.addWidget(button);

		button = new QPushButton("Huidige tafels");
		button.clicked.connect(this, "toCurrent()");
		upperButtonLayout.addWidget(button);

		button = new QPushButton("Geschiedenis");
		button.clicked.connect(this, "toHistory()");
		upperButtonLayout.addWidget(button);

		button = new QPushButton("Totalen");
		button.clicked.connect(this, "toTotals()");
		upperButtonLayout.addWidget(button);
		
		button = new QPushButton("Analytics");
		button.clicked.connect(this, "toAnalytics()");
		upperButtonLayout.addWidget(button);

		QHBoxLayout lowerButtonLayout = new QHBoxLayout();
		button = new QPushButton("Sluiten");
		button.setFixedWidth(75);
		button.clicked.connect(this, "accept()");
		lowerButtonLayout.addWidget(button);

		layout.addLayout(upperButtonLayout);
		layout.addLayout(lowerButtonLayout);
		
		setLayout(layout);
		
		QIcon icon = new QIcon();
		icon.addFile("classpath:icon_64.png");
		setWindowIcon(icon);
	}

	private QStandardItemModel getHistoryOrders() {
		m_historyModel = new QStandardItemModel(m_historyTables.getNrTables(), 2, this);

		m_historyModel.setHeaderData(0, Qt.Orientation.Horizontal, "Client");
		m_historyModel.setHeaderData(1, Qt.Orientation.Horizontal, "Totaal");

		int i = 0;
		for (TableOrders table : m_historyTables) {
			m_historyModel.setData(i, 0, table.tableNr());
			m_historyModel.setData(i++, 1, "\u20AC " + table.getTotalPrice());
		}
		return m_historyModel;
	}

	private QStandardItemModel getItems(Tables tables, int tableNr) {
		TableOrders table = tables.getTable(tableNr);
		if (table.nrOfItems() == 0)
			return null;

		m_itemModel = new QStandardItemModel(table.nrOfItems(), 2, this);

		m_itemModel.setHeaderData(0, Qt.Orientation.Horizontal, "Item");
		m_itemModel.setHeaderData(1, Qt.Orientation.Horizontal, "Aantal");

		TreeMap<Item, Integer> items = table.getItems();

		Item item = items.firstKey();
		int index = 0;
		while (item != null) {
			m_itemModel.setData(index, 0, item.getName());
			m_itemModel.setData(index, 1, items.get(item));

			item = items.higherKey(item);
			index++;
		}
		m_itemModel.dataChanged.connect(this, "dataChanged(QModelIndex, QModelIndex)");

		return m_itemModel;
	}

	private QStandardItemModel getCurrentOrders() {
		m_orderModel = new QStandardItemModel(m_tables.getNrTables(), 3, this);

		m_orderModel.setHeaderData(0, Qt.Orientation.Horizontal, "Tafel");
		m_orderModel.setHeaderData(1, Qt.Orientation.Horizontal, "Aantal items");
		m_orderModel.setHeaderData(2, Qt.Orientation.Horizontal, "Totaal");
		int i = 0;
		for (TableOrders table : m_tables) {
			m_orderModel.setData(i, 0, i + 1);
			m_orderModel.setData(i, 1, table.totalNrOfItems());
			m_orderModel.setData(i++, 2, "\u20AC " + table.getTotalPrice());
		}
		return m_orderModel;
	}

	private void orderSelected(QModelIndex index) {
		if (index == null) {
			m_bottomView.setModel(null);
			m_selectedTable = -1;
		} else if (m_mode == view_mode.CURRENT_TABLES_MODE) {
			m_bottomView.setModel(getItems(m_tables, index.row() + 1));
			m_selectedTable = index.row()+1;
		} else {
			m_bottomView.setModel(getItems(m_historyTables, index.row() + 1));
			m_selectedTable = index.row()+1;
		}
	}
	
	private void toCurrent() {
		m_topView.setModel(getCurrentOrders());
		m_bottomView.setModel(null);
		m_mode = view_mode.CURRENT_TABLES_MODE;
	}
	
	private void toHistory() {
		if (m_historyTables == null) {
			return;
		}
		m_topView.setModel(getHistoryOrders());
		m_bottomView.setModel(null);
		m_mode = view_mode.HISTORY_MODE;
	}
	
	private void toItems() {
		GuiAddItems addItems = new GuiAddItems(m_items, m_database);
		addItems.exec();
		DataChanged.emit();
	}
	
	private void toTotals() {
		m_totalsDial.exec();
	}
	
	private void toAnalytics() {
		m_analyticsDial.exec();
	}
}
