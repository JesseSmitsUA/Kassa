package kassa.gui.dialog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import kassa.core.exceptions.ItemsException;
import kassa.core.io.IOcsv;
import kassa.core.items.Item;
import kassa.core.items.ItemFactory;
import kassa.core.items.Items;
import kassa.core.storage.Storage;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.trolltech.qt.core.QDir;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QDoubleValidator;
import com.trolltech.qt.gui.QDoubleValidator.Notation;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QFormLayout;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QStandardItem;
import com.trolltech.qt.gui.QStandardItemModel;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

/**
 * Add Items dialog
 * 
 * @author Stephen Pauwels
 */
@SuppressWarnings("unused")
public class NewGuiAddItems extends QDialog {

	private ItemFactory m_factory;
	private Items m_items;
	private Integer m_selected_row;
	
	private QComboBox m_category;
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
	public NewGuiAddItems(Items items) {
		this(null, items);
	}

	/**
	 * Constructor taking a parent Frame
	 * 
	 * @param parent
	 *            the Frame's parent
	 */
	public NewGuiAddItems(QWidget parent, Items items) {
		super(parent);
		m_items = items;
		m_factory = new ItemFactory();
		drawGui();
	}

	private void acceptItems() {
		try {
			QStandardItemModel model = (QStandardItemModel) m_itemView.model();
			for (int i = 0; i < model.rowCount(); i++) {
				Item item = m_factory.createItem((String) model.data(i, 2), (String) model.data(i, 0), 
						Double.parseDouble((String) model.data(i, 1)), (String) model.data(i, 3), 
						(String) model.data(i, 4), Integer.parseInt((String) model.data(i, 5)));

				m_items.addItem(item);
			}
			close(); 
		} catch (SQLException e) {
			QMessageBox.critical(this, "Database fout",
					"Volgende fout gebeurde in de database:\n" + e.getMessage());
		} catch (ItemsException e) {
			QMessageBox.critical(
					this,
					"Item fout",
					"Volgende fout gebeurde tijdens het toevoegen:\n"
							+ e.getMessage());
		} catch (NullPointerException e) {
			e.printStackTrace();
			//DO NOTHING
		}
	}

	private void cancelItems() {
		close();
	}
	
	private void newItem() {
		m_itemButton.setText("Item toevoegen");
		m_itemView.clearSelection();
		clearItem();
	}
	
	private void addItem() {
		QStandardItemModel model = (QStandardItemModel) m_itemView.model();
		ArrayList<QStandardItem> new_row = new ArrayList<QStandardItem>();
		
		new_row.add(new QStandardItem(formatName(m_name.text())));
		new_row.add(new QStandardItem(m_price.text()));
		new_row.add(new QStandardItem(m_category.currentText()));
		new_row.add(new QStandardItem(m_subCat.isEnabled()?m_subCat.currentText():""));
		new_row.add(new QStandardItem(formatName(m_supplement.text())));
		new_row.add(new QStandardItem("" + m_numTicket.value()));
		
		model.appendRow(new_row);
		m_itemView.setModel(model);
		
		newItem();
	}
	
	private void removeItem() {
		if (m_selected_row != null) {
			QStandardItemModel model = (QStandardItemModel) m_itemView.model();
			model.removeRow(m_selected_row);
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

	private void categoryChanged() {
		if (m_category.currentIndex() == 3)
			m_subCat.setDisabled(false);
		else
			m_subCat.setDisabled(true);
	}

	private void clearAllItems() {
		m_itemView.setModel(makeModel());
	}

	private void clearItem() {
		m_name.setText("");
		m_price.setText("");
		m_supplement.setText("");
	}
	
	private void itemSelected(QModelIndex p) {
		if (p != null)
			m_selected_row = p.row();
		else
			m_selected_row = null;
	}

	private void drawGui() {
		setWindowTitle("Add Items");
		setMinimumWidth(850);

		QVBoxLayout layout = new QVBoxLayout();

		QHBoxLayout itemOverview = new QHBoxLayout();
		m_itemView = new QItemTableView();
		m_itemView.setModel(makeModel());
		m_itemView.setColumnWidth(0, 200);
		m_itemView.setSortingEnabled(true);
		m_itemView.clicked.connect(this, "itemSelected(QModelIndex)");
		itemOverview.addWidget(m_itemView);

		QVBoxLayout removeItem = new QVBoxLayout();
		QPushButton button = new QPushButton("Nieuw item");
		button.clicked.connect(this, "newItem()");
		removeItem.addWidget(button);
		m_deleteItem = new QPushButton("Verwijder item");
		m_deleteItem.clicked.connect(this, "removeItem()");
		removeItem.addWidget(m_deleteItem);
		button = new QPushButton("Verwijder alle");
		button.clicked.connect(this, "clearAllItems()");
		removeItem.addWidget(button);
		itemOverview.addLayout(removeItem);

		QFormLayout form = new QFormLayout();
		
		m_name = new QLineEdit();
		m_name.setMinimumWidth(700);
		form.addRow(new QLabel("Naam:"), m_name);
		
		m_price = new QLineEdit();
		m_price.setMinimumWidth(50);
		QDoubleValidator validator = new QDoubleValidator(this);
		validator.setDecimals(2);
		validator.setBottom(0);
		validator.setNotation(Notation.StandardNotation);
		m_price.setValidator(validator);
		form.addRow(new QLabel("Prijs:"), m_price);
		
		m_category = new QComboBox();
		for (String type : m_factory.getTypes()) {
			m_category.addItem(type);
		}
		m_category.currentIndexChanged.connect(this, "categoryChanged()");
		form.addRow(new QLabel("Categorie:"), m_category);
		
		m_subCat = new QComboBox();
		for (String type : m_factory.getSubTypes()) {
			m_subCat.addItem(type);
		}
		m_subCat.setDisabled(true);
		form.addRow(new QLabel("SubCategorie:"), m_subCat);
		
		m_supplement = new QLineEdit();
		form.addRow(new QLabel("Supplement:"), m_supplement);

		m_numTicket = new QSpinBox();
		m_numTicket.setValue(1);
		m_numTicket.setMinimum(0);
		m_numTicket.setMaximum(2);
		form.addRow(new QLabel("Print op aantal tickets:"), m_numTicket);

		QHBoxLayout addButton = new QHBoxLayout();
		m_itemButton = new QPushButton("Item toevoegen");
		m_itemButton.clicked.connect(this, "addItem()");
		addButton.addWidget(m_itemButton);

		button = new QPushButton("Input leegmaken");
		button.clicked.connect(this, "clearItem()");
		addButton.addWidget(button);

		button = new QPushButton("Open items...");
		button.clicked.connect(this, "load()");
		addButton.addWidget(button);

		button = new QPushButton("Save items...");
		button.clicked.connect(this, "save()");
		addButton.addWidget(button);


		QHBoxLayout buttonBox = new QHBoxLayout();
		button = new QPushButton("Ok");
		button.clicked.connect(this, "acceptItems()");
		button.setFixedWidth(150);
		buttonBox.addWidget(button);
		button = new QPushButton("Cancel");
		button.clicked.connect(this, "cancelItems()");
		button.setFixedWidth(150);
		buttonBox.addWidget(button);

		layout.addLayout(itemOverview);
		layout.addLayout(form);
		layout.addLayout(addButton);
		layout.addLayout(buttonBox);
		
		setLayout(layout);
		
		QIcon icon = new QIcon();
		icon.addFile("classpath:icon_64.png");
		setWindowIcon(icon);
	}

	/**
	 * Make empty model for item_view
	 * 
	 * @return
	 */
	public QStandardItemModel makeModel() {
		m_itemModel = new QStandardItemModel(0, 6, this);

		m_itemModel.setHeaderData(0, Qt.Orientation.Horizontal, "Naam");
		m_itemModel.setHeaderData(1, Qt.Orientation.Horizontal, "Prijs");
		m_itemModel.setHeaderData(2, Qt.Orientation.Horizontal, "Categorie");
		m_itemModel.setHeaderData(3, Qt.Orientation.Horizontal, "SubCategorie");
		m_itemModel.setHeaderData(4, Qt.Orientation.Horizontal, "Supplement");
		m_itemModel.setHeaderData(5, Qt.Orientation.Horizontal, "Tickets");

		return m_itemModel;
	}

	private void load() {
		try {
			String fileName = QFileDialog.getOpenFileName(this, "Save items",
					QDir.homePath(), new QFileDialog.Filter(
							"Item-files (*.item);;All files (*.*)"));

			CsvReader csv = new CsvReader(fileName);
			csv.readHeaders();

			QStandardItemModel model = (QStandardItemModel) m_itemView.model();

			while (csv.readRecord()) {
				ArrayList<QStandardItem> new_row = new ArrayList<QStandardItem>();
				
				new_row.add(new QStandardItem(csv.get("Name")));
				new_row.add(new QStandardItem(csv.get("Price")));
				new_row.add(new QStandardItem(csv.get("Category")));
				new_row.add(new QStandardItem(csv.get("SubCat")));
				new_row.add(new QStandardItem(csv.get("Supplement")));
				new_row.add(new QStandardItem(csv.get("Tickets")));
				
				model.appendRow(new_row);
			}
			m_itemView.setModel(model);
		} catch (Exception e) {
			QMessageBox.critical(this, "Item fout",
					"Volgende fout gebeurde tijdens het laden van de file:\n"
							+ e.getMessage());
		}
	}
	
	private void save() {
		try {
			String fileName = QFileDialog.getSaveFileName(this, "Save items",
					QDir.homePath(), new QFileDialog.Filter(
							"Item-files (*.item);;All files (*.*)"));

			File target = new File(fileName);

			CsvWriter csv = new CsvWriter(new FileWriter(target), ',');
			csv.write("Name");
			csv.write("Price");
			csv.write("Category");
			csv.write("SubCat");
			csv.write("Supplement");
			csv.write("Tickets");
			csv.endRecord();

			QStandardItemModel model = (QStandardItemModel) m_itemView.model();
			for (int i = 0; i < model.rowCount(); i++) {
				csv.write((String) model.data(i, 0));
				csv.write((String) model.data(i, 1));
				csv.write((String) model.data(i, 2));
				csv.write((String) model.data(i, 3));
				csv.write((String) model.data(i, 4));
				csv.write((String) model.data(i, 5));
				csv.endRecord();
			}

			csv.close();
		} catch (Exception e) {
			QMessageBox.critical(this, "Item fout",
					"Volgende fout gebeurde tijdens het saven van de file:\n"
							+ e.getMessage());
		}
	}


}
