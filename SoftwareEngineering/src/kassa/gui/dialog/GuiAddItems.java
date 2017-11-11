package kassa.gui.dialog;

import java.io.File;
import java.sql.SQLException;

import kassa.core.exceptions.ItemsException;
import kassa.core.io.IOcsv;
import kassa.core.items.Item;
import kassa.core.items.ItemFactory;
import kassa.core.items.Items;
import kassa.core.storage.Storage;

import com.trolltech.qt.core.QDir;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QDoubleValidator;
import com.trolltech.qt.gui.QDoubleValidator.Notation;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QStandardItemModel;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

/**
 * Add Items dialog
 * 
 * @author Stephen Pauwels
 */
@SuppressWarnings("unused")
public class GuiAddItems extends QDialog {

	private Storage m_database;
	private ItemFactory m_factory;
	private Items m_items;
	private Item m_selectedItem;

	private boolean m_accept;

	private QComboBox m_category;
	private QModelIndex m_currentIndex; // TODO: check if needed
	private QPushButton m_deleteItem;
	private QStandardItemModel m_itemModel;
	private QItemTableView m_itemView;
	private QLineEdit m_name;
	private QLineEdit m_price;
	private QComboBox m_subCat;
	private QPushButton m_itemButton;
	private QLineEdit m_supplement;
	private QSpinBox m_numTicket;

	/**
	 * Constructor without parent
	 */
	public GuiAddItems(Items items, Storage database) {
		this(null, items, database);
	}

	/**
	 * Constructor taking a parent Frame
	 * 
	 * @param parent
	 *            the Frame's parent
	 */
	public GuiAddItems(QWidget parent, Items items, Storage database) {
		super(parent);
		m_items = items;
		m_database = database;
		m_factory = new ItemFactory();
		drawGui();
	}

	private void acceptItems() {
		m_accept = true;
		close();
		m_items.sort();
	}

	private void addItem() {
		try {
			if (m_name.text().equalsIgnoreCase("")) {
				QMessageBox.critical(this, "Input fout",
						"Gelieve een naam in te vullen voor het item!");
				return;
			}
			Item item = m_factory.createItem(m_category.currentText(),
					formatName(m_name.text()), Double.parseDouble(m_price.text().replace(',', '.')),
					m_subCat.currentText(), m_supplement.text(), m_numTicket.value());

			m_items.addItem(item);
			clearItem();
			updateItems();
		} catch (NumberFormatException e) {
			QMessageBox.critical(this, "Input fout",
					"Gelieve een correcte prijs op te geven!");
		} catch (ItemsException e) {
			QMessageBox.critical(
					this,
					"Item fout",
					"Volgende fout gebeurde tijdens het toevoegen:\n"
							+ e.getMessage());
		} catch (SQLException e) {
			QMessageBox.critical(this, "Database fout",
					"Volgende fout gebeurde in de database:\n" + e.getMessage());
		}
	}
	
	private String formatName(String name) {
		for (int i = name.length()-1; i >= 0; i--) {
			if (name.charAt(i) == ' ') {
				name = name.substring(0, i);
			} else {
				return name;
			}
		}
		return "";
	}

	private void cancelItems() {
		m_accept = false;
		close();
	}

	private void categoryChanged() {
		if (m_category.currentIndex() == 3)
			m_subCat.setDisabled(false);
		else
			m_subCat.setDisabled(true);
	}

	private void clearAllItems() {
		try {
			if (m_items.getNrItems() == 0) {
				return;
			}
			QMessageBox.StandardButton button = QMessageBox
					.question(
							this,
							"Verwijder Items",
							"U staat op het punt de volledige lijst te wissen.\nWilt u verdergaan?",
							new QMessageBox.StandardButtons(
									QMessageBox.StandardButton.Ok,
									QMessageBox.StandardButton.Cancel));
			if (button == QMessageBox.StandardButton.Ok) {
				m_items.clearAll();
				updateItems();
			} else if (button != QMessageBox.StandardButton.Cancel) {

			}
		} catch (SQLException e) {
			QMessageBox.critical(this, "Database fout",
					"Volgende fout gebeurde in de database\n" + e.getMessage());
		} catch (Exception e) {
			QMessageBox.critical(this, "Error", e.getMessage());
		}
	}

	private void clearItem() {
		m_name.setText("");
		m_price.setText("");
	}

	private void drawGui() {
		setWindowTitle("Add Items");
		setMinimumWidth(700);

		QVBoxLayout layout = new QVBoxLayout();

		QHBoxLayout itemOverview = new QHBoxLayout();
		m_itemView = new QItemTableView();
		m_itemView.setModel(makeModel(m_items));
		m_itemView.setColumnWidth(0, 200);
		m_itemView.setSortingEnabled(true);
		itemOverview.addWidget(m_itemView);

		QVBoxLayout removeItem = new QVBoxLayout();
		QPushButton button = new QPushButton("Nieuw item");
		button.clicked.connect(this, "newItem()");
		removeItem.addWidget(button);
		m_deleteItem = new QPushButton("Verwijder item");
		m_deleteItem.clicked.connect(this, "removeItem()");
		m_deleteItem.setDisabled(true);
		removeItem.addWidget(m_deleteItem);
		button = new QPushButton("Verwijder alle");
		button.clicked.connect(this, "clearAllItems()");
		removeItem.addWidget(button);
		itemOverview.addLayout(removeItem);

		QHBoxLayout addItem = new QHBoxLayout();
		QVBoxLayout label = new QVBoxLayout();
		label.addWidget(new QLabel("Naam:"));
		label.addWidget(new QLabel("Prijs:"));
		label.addWidget(new QLabel("Categorie:"));
		label.addWidget(new QLabel("SubCategorie:"));

		QVBoxLayout lineEdit = new QVBoxLayout();
		m_name = new QLineEdit();
		m_price = new QLineEdit();
		QDoubleValidator validator = new QDoubleValidator(this);
		validator.setDecimals(2);
		validator.setBottom(0);
		validator.setNotation(Notation.StandardNotation);
		m_price.setValidator(validator);
		m_category = new QComboBox();
		for (String type : m_factory.getTypes()) {
			m_category.addItem(type);
		}
		m_category.currentIndexChanged.connect(this, "categoryChanged()");

		m_subCat = new QComboBox();
		for (String type : m_factory.getSubTypes()) {
			m_subCat.addItem(type);
		}
		m_subCat.setDisabled(true);

		lineEdit.addWidget(m_name);
		lineEdit.addWidget(m_price);
		lineEdit.addWidget(m_category);
		lineEdit.addWidget(m_subCat);
		
		QHBoxLayout suppChoose = new QHBoxLayout();
		m_supplement = new QLineEdit();
		suppChoose.addWidget(new QLabel("Supplement: "));
		suppChoose.addWidget(m_supplement);

		
		QHBoxLayout numTickets = new QHBoxLayout();
		m_numTicket = new QSpinBox();
		m_numTicket.setValue(1);
		m_numTicket.setMinimum(0);
		m_numTicket.setMaximum(2);
		numTickets.addWidget(new QLabel("Print op aantal tickets: "));
		numTickets.addWidget(m_numTicket);

		QHBoxLayout addButton = new QHBoxLayout();
		m_itemButton = new QPushButton("Item toevoegen");
		m_itemButton.clicked.connect(this, "addItem()");
		addButton.addWidget(m_itemButton);

		button = new QPushButton("Input leegmaken");
		button.clicked.connect(this, "clearItem()");
		addButton.addWidget(button);

		button = new QPushButton("Open csv...");
		button.clicked.connect(this, "loadCsv()");
		addButton.addWidget(button);

		button = new QPushButton("Save csv...");
		button.clicked.connect(this, "saveCsv()");
		addButton.addWidget(button);

		addItem.addLayout(label);
		addItem.addLayout(lineEdit);

		QHBoxLayout buttonBox = new QHBoxLayout();
		button = new QPushButton("Sluiten");
		button.clicked.connect(this, "acceptItems()");
		button.setFixedWidth(150);
		buttonBox.addWidget(button);

		layout.addLayout(itemOverview);
		layout.addLayout(addItem);
		layout.addLayout(suppChoose);
		layout.addLayout(numTickets);
		layout.addLayout(addButton);
		layout.addLayout(buttonBox);
		
		setLayout(layout);
		
		QIcon icon = new QIcon();
		icon.addFile("classpath:icon_64.png");
		setWindowIcon(icon);
	}

	public Items getItems() {
		return m_items;
	}

	public boolean itemsAccepted() {
		return m_accept;
	}

	public QStandardItemModel makeModel(Items items) {
		m_itemModel = new QStandardItemModel(items.getNrItems(), 4, this);

		m_itemModel.setHeaderData(0, Qt.Orientation.Horizontal, "Naam");
		m_itemModel.setHeaderData(1, Qt.Orientation.Horizontal, "Prijs");
		m_itemModel.setHeaderData(2, Qt.Orientation.Horizontal, "Categorie");
		m_itemModel.setHeaderData(3, Qt.Orientation.Horizontal, "SubCategorie");

		int i = 0;
		for (Item item : items) {
			m_itemModel.setData(i, 0, item.getName());
			m_itemModel.setData(i, 1, "\u20AC " + item.getPrice());
			m_itemModel.setData(i, 2, item.getCategory());
			m_itemModel.setData(i, 3, item.getSubCat());
			i++;
		}

		return m_itemModel;
	}

	private void newItem() {
		m_itemButton.setText("Item toevoegen");
		m_itemView.clearSelection();
		clearItem();
	}

	private void removeItem() {
		try {
			m_items.deleteItem(m_selectedItem);
			m_currentIndex = null;
			m_deleteItem.setDisabled(true);
			m_selectedItem = null;
		} catch (SQLException e) {
			QMessageBox.information(this, "Database fout",
					"Volgende fout gebeurde in de database\n" + e.getMessage());
		}
		updateItems();
		newItem();
	}
	
	private void updateItems() {
		m_itemView.setModel(makeModel(m_items));
	}

	private void saveCsv() {
		try {
			String fileName = QFileDialog.getSaveFileName(this, "Save csv",
					QDir.homePath(), new QFileDialog.Filter(
							"Csv-files (*.csv);;All files (*.*)"));
			if (fileName.equalsIgnoreCase("")) {
				return;
			}
			IOcsv csv = new IOcsv();
			csv.saveFile(new File(fileName), m_items);
			csv.closeCsv();
		} catch (Exception e) {
			QMessageBox.critical(this, "Item fout",
					"Volgende fout gebeurde tijdens het saven van de file:\n"
							+ e.getMessage());
		}
	}
	
	private void loadCsv() {
		try {
			String fileName = QFileDialog.getOpenFileName(this, "Load csv",
					QDir.homePath(), new QFileDialog.Filter(
							"Csv-files (*.csv);;All files (*.*)"));
			if (fileName.equalsIgnoreCase("")) {
				return;
			}
			IOcsv csv = new IOcsv(fileName, m_database);
			m_items.addItems(csv.getItems());
			csv.closeCsv();
		} catch (Exception e) {
			QMessageBox.critical(this, "Item fout",
					"Volgende fout gebeurde tijdens het openen van de file:\n"
							+ e.getMessage());
		}
		updateItems();
	}

}
