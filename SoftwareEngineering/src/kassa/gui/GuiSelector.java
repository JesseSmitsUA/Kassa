package kassa.gui;

import java.sql.SQLException;
import java.util.ArrayList;

import kassa.core.Tables;
import kassa.core.items.Item;
import kassa.core.items.Items;
import kassa.core.orders.Order;
import kassa.core.orders.TableOrders;
import kassa.gui.table.GuiTableSelector;

import com.trolltech.qt.core.QSignalMapper;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QFont;
import com.trolltech.qt.gui.QFrame;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSizePolicy.Policy;
import com.trolltech.qt.gui.QStackedWidget;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

/**
 * Selector section of Screen
 * 
 * @author Stephen Pauwels
 */
@SuppressWarnings("unused")
public class GuiSelector extends QWidget {

	/*
	 * Signal to emmit when item has accepted on order
	 */
	public final Signal1<Item> itemAccepted = new Signal1<Item>();

	/*
	 * Signal to emmit when new table is selected
	 */
	public final Signal1<Integer> tableChanged = new Signal1<Integer>();
	
	public final Signal1<Tables> tablesUpdated = new Signal1<Tables>();

	/* System */
	private Items m_items;
	private Tables m_tables;

	/* GUI + qt */
	private QFrame m_top;

	private QLabel m_selection;
	private QPushButton m_drinkButton;
	private QPushButton m_foodButton;
	
	private QStackedWidget m_selector;

	private QPushButton[] m_tableButtons;
	
	private QSignalMapper m_inputSignals;
	private QSignalMapper m_tableSignals;
	
	/**
	 * Constructor without parent
	 * @throws SQLException 
	 */
	public GuiSelector(Items items, Tables tables) {
		this((QWidget) null, items, tables);
	}

	/**
	 * Constructor taking a parent Frame
	 * 
	 * @param parent	the Frame's parent
	 * @throws SQLException 
	 */
	public GuiSelector(QWidget parent, Items items, Tables tables) {
		super(parent);
		
		// Init members
		m_items = items;
		m_tables = tables;
		m_inputSignals = new QSignalMapper();
		m_inputSignals.mappedString.connect(this, "addItem(String)");
		m_tableSignals = new QSignalMapper();
		m_tableSignals.mappedInteger.connect(this, "selectTable(int)");
		
		drawGui();
	}
	
	/**
	 * Draw GUI
	 */
	private void drawGui() {
		QVBoxLayout layout = new QVBoxLayout();

		m_top = new QFrame();
		m_top.setFrameStyle(1);
		m_top.setFixedHeight(80);
		QHBoxLayout topLayout = new QHBoxLayout();

		QHBoxLayout systemBox = new QHBoxLayout();

		m_selection = new QLabel("Tafels");
		m_selection.setAlignment(Qt.AlignmentFlag.AlignCenter);
		m_selection.setFont(new QFont(null, 25));
		systemBox.addWidget(m_selection);

		QHBoxLayout buttonLayout = new QHBoxLayout();
		QPushButton button = new QPushButton("Tafels");
		button.clicked.connect(this, "toTables()");
		button.setMinimumHeight(60);
		buttonLayout.addWidget(button);

		m_drinkButton = new QPushButton("Drank");
		m_drinkButton.clicked.connect(this, "drinkItems()");
		m_drinkButton.setMinimumHeight(60);
		m_drinkButton.setDisabled(true);
		buttonLayout.addWidget(m_drinkButton);

		m_foodButton = new QPushButton("Eten");
		m_foodButton.clicked.connect(this, "foodItems()");
		m_foodButton.setMinimumHeight(60);
		m_foodButton.setDisabled(true);
		buttonLayout.addWidget(m_foodButton);

		topLayout.addLayout(systemBox);
		topLayout.addLayout(buttonLayout);
		m_top.setLayout(topLayout);

		m_selector = new QStackedWidget();
		m_selector.addWidget(createTableWidget());
		m_selector.addWidget(createItemWidget());
		m_selector.addWidget(createDrinksWidget());

		layout.addWidget(m_top);
		layout.addWidget(m_selector);
		setLayout(layout);
		toTables();
	}
	
	/** 
	 * Update tables
	 */
	public void updateTables(Tables tables) {
		m_tables = tables;
	}
	
	/**
	 * Refresh and go back to table overview
	 */
	public void refresh() {
		if (m_selector.currentIndex() == 0) {
			toTables();
		}
	}

	/**
	 * Change view to table overview
	 */
	public void toTables() {
		updateTables();
		m_selector.setCurrentIndex(0);
		m_drinkButton.setDisabled(true);
		m_foodButton.setDisabled(true);
		tableChanged.emit(0);

		m_selection.setText("Tafels");
	}
	
	/**
	 * Add item to current order
	 */
	private void addItem(String item) {
		itemAccepted.emit(m_items.getItemByName(item));
	}

	/**
	 * Create Drinks widget
	 */
	private QWidget createDrinksWidget() {
		if (m_items == null)
			return null;
		QWidget items = new QWidget();
		QGridLayout selector = new QGridLayout();
		items.setLayout(selector);

		ArrayList<Item> drinks = m_items.getDrinks();

		int i = 0;
		for (Item drink : drinks) {
			String name = drink.getName();
			QPushButton button = new QPushButton(name);
			button.setStyleSheet("* { background-color: rgb(" + drink.getColor() + ")}");

			button.setSizePolicy(Policy.Minimum, Policy.Minimum);
			
			if (name.length() < 12)
				button.setFont(new QFont(null, 16));
			else if (name.length() < 20)
				button.setFont(new QFont(null, 14));
			
			button.clicked.connect(m_inputSignals, "map()");
			m_inputSignals.setMapping(button, name);
			
			button.setFixedHeight(75);
			selector.addWidget(button, i / 6, i % 6);
			i++;
		}
		
		for (int k = drinks.size(); k < 36; k++) { // Fill grid with empty widgets
			QWidget widget = new QWidget();
			widget.setFixedHeight(75);
			selector.addWidget(widget, k / 6, k % 6);
		}

		return items;
	}

	/**
	 * Create Item widget
	 */
	private QWidget createItemWidget() {
		if (m_items == null)
			return null;
		QWidget items = new QWidget();
		QGridLayout selector = new QGridLayout();
		items.setLayout(selector);

		ArrayList<Item> nonDrinks = m_items.getNonDrinks();

		int i = 0;
		for (Item item : nonDrinks) {
			String name = item.getName();
			
			if (item.getSupplement() != null && !item.getSupplement().equals(""))
				name += "\n (" + item.getSupplement() + ")";
			
			QPushButton button = new QPushButton(name);
			button.setStyleSheet("* { background-color: rgb(" + item.getColor() + ")}");

			button.setSizePolicy(Policy.Minimum, Policy.Minimum);
			
			if (name.length() < 12)
				button.setFont(new QFont(null, 16));
			else if (name.length() < 20)
				button.setFont(new QFont(null, 14));
			
			button.clicked.connect(m_inputSignals, "map()");
			m_inputSignals.setMapping(button, item.getName());
			
			
			button.setFixedHeight(75);
			selector.addWidget(button, i / 6, i % 6);
			i++;
		}
		
		for (int k = i; k < 36; k++) { // Fill grid with empty widgets
			QWidget widget = new QWidget();
			widget.setFixedHeight(75);
			selector.addWidget(widget, k / 6, k % 6);
		}

		return items;
	}
	
	/**
	 * Create Item widget
	 */
	private QWidget createTableWidget() {
		if (m_tables == null)
			return null;
		QWidget tables = new QWidget();
		QGridLayout selector = new QGridLayout();
		tables.setLayout(selector);
		
		if (m_tableButtons == null)
			m_tableButtons = new QPushButton[m_tables.getNrTables()];

		int i = 0;
		for (TableOrders table : m_tables) {
			String name = "Tafel " + table.tableNr();
			
			QPushButton button = new QPushButton(name);
			if (table.notEmpty())
				button.setStyleSheet("* { background-color: rgb(100,255,100)}");
			else
				button.setStyleSheet("* { background-color: rgb(255,100,100)}");

			button.setSizePolicy(Policy.Minimum, Policy.Minimum);
			button.setFont(new QFont(null, 16));
			
			button.clicked.connect(m_tableSignals, "map()");
			m_tableSignals.setMapping(button, table.tableNr());
			
			button.setFixedHeight(75);
			m_tableButtons[i] = button;
			selector.addWidget(button, i / 6, i % 6);
			i++;
		}
		
		for (int k = i; k < 36; k++) { // Fill grid with empty widgets
			QWidget widget = new QWidget();
			widget.setFixedHeight(75);
			selector.addWidget(widget, k / 6, k % 6);
		}

		return tables;
	}
	
	private void updateTables() {
		int i = 1;
		for (QPushButton button : m_tableButtons) {
			if (m_tables.getTable(i).notEmpty())
				button.setStyleSheet("* { background-color: rgb(255,100,100)}");
			else
				button.setStyleSheet("* { background-color: rgb(100,255,100)}");
			i++;
		}
	}


	/**
	 * Go to drink overview
	 */
	private void drinkItems() {
		m_selector.setCurrentIndex(2);
		m_selection.setText("Drank");
	}

	/**
	 * Go to food overview
	 */
	private void foodItems() {
		m_selector.setCurrentIndex(1);
		m_selection.setText("Eten");
	}

	/**
	 * Select table after table is pressed in overview
	 */
	private void selectTable(int tableNr) {
		if (tableNr == 0) {
			updateTables();
			m_selector.setCurrentIndex(0);
			m_drinkButton.setDisabled(true);
			m_foodButton.setDisabled(true);
			m_selection.setText("Tafels");
		} else {
			m_selector.setCurrentIndex(2);
			m_drinkButton.setDisabled(false);
			m_foodButton.setDisabled(false);
		}

		tableChanged.emit(tableNr);
		m_selection.setText("Drank");
	}
	
	/**
	 * Update selector views
	 */
	private void updateSelector() {

	}
}
