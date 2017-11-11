package kassa.gui.dialog;

import java.sql.SQLException;
import java.util.TreeMap;

import kassa.core.items.Item;
import kassa.core.storage.Storage;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QStandardItemModel;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

/**
 * Totals Dialog
 * 
 * @author Stephen Pauwels
 */
public class GuiTotals extends QDialog {

	private Storage m_database;

	private QStandardItemModel m_itemModel;

	private QItemTableView m_items;

	private double m_total;

	private QLabel m_totalLabel;

	/**
	 * Constructor taking a parent Frame
	 * 
	 * @param parent
	 *            the Frame's parent
	 */
	public GuiTotals(QWidget parent, Storage database) {
		super(parent);
		m_database = database;
		m_total = 0;
		drawGui();
	}

	/**
	 * Constructor without parent
	 */
	public GuiTotals(Storage database) {
		this(null, database);
	}

	private void drawGui() {
		setWindowTitle("Admin");
		setMinimumWidth(350);

		QVBoxLayout layout = new QVBoxLayout();

		m_items = new QItemTableView();
		m_items.setModel(getTotals());
		layout.addWidget(m_items);

		QHBoxLayout totalLayout = new QHBoxLayout();
		QLabel label = new QLabel("Totaal:");
		totalLayout.addWidget(label);
		m_totalLabel = new QLabel("\u20AC " + m_total);
		totalLayout.addWidget(m_totalLabel);
		layout.addLayout(totalLayout);

		QHBoxLayout buttonLayout = new QHBoxLayout();
		QPushButton button = new QPushButton("Sluiten");
		button.setFixedWidth(75);
		button.clicked.connect(this, "accept()");
		buttonLayout.addWidget(button);

		layout.addLayout(buttonLayout);
		setLayout(layout);
		
		QIcon icon = new QIcon();
		icon.addFile("classpath:icon_64.png");
		setWindowIcon(icon);
	}

	private QStandardItemModel getTotals() {
		try {
			TreeMap<Item, Integer> totals = m_database.getTotals(m_database
					.readItems());
			if (totals.size() == 0) {
				return null;
			}
			m_itemModel = new QStandardItemModel(totals.size(), 3, this);

			m_itemModel.setHeaderData(0, Qt.Orientation.Horizontal, "Item");
			m_itemModel.setHeaderData(1, Qt.Orientation.Horizontal, "Aantal");
			m_itemModel.setHeaderData(2, Qt.Orientation.Horizontal,
					"Totale Prijs");

			Item item = totals.firstKey();
			int i = 0;
			while (item != null) {
				m_itemModel.setData(i, 0, item.getName());
				m_itemModel.setData(i, 1, totals.get(item));
				m_itemModel.setData(i, 2, (totals.get(item) * item.getPrice()));
				m_total += totals.get(item) * item.getPrice();
				item = totals.higherKey(item);
				i++;
			}
			m_total = round(m_total, 2);
			return m_itemModel;
		} catch (SQLException e) {
			QMessageBox.critical(this, "Database fout",
					"Volgende fout gebeurde in de database\n" + e.getMessage());
			return null;
		}
	}

	private double round(double Rval, int Rpl) {
		double p = Math.pow(10, Rpl);
		Rval = Rval * p;
		double tmp = Math.round(Rval);
		return (float) tmp / p;
	}

}
