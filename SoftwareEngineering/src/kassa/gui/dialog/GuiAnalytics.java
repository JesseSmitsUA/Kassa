package kassa.gui.dialog;

import java.sql.SQLException;
import java.util.TreeMap;

import kassa.core.analytics.Eclat;
import kassa.core.analytics.TransactionDB;
import kassa.core.items.Item;
import kassa.core.items.Items;
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
public class GuiAnalytics extends QDialog {

	private Storage m_database;
	private TransactionDB m_transdb;

	private Eclat m_eclat;
	/**
	 * Constructor taking a parent Frame
	 * 
	 * @param parent
	 *            the Frame's parent
	 * @throws Exception 
	 */
	public GuiAnalytics(QWidget parent, Items items, Storage database) throws Exception {
		super(parent);
		m_database = database;
		m_transdb = new TransactionDB(items, database);
		
		m_eclat = new Eclat(m_transdb, items);
		m_eclat.calculate(2);
		drawGui();
	}

	/**
	 * Constructor without parent
	 * @throws Exception 
	 */
	public GuiAnalytics(Items items, Storage database) throws Exception {
		this(null, items, database);
	}

	private void drawGui() {
		setWindowTitle("Admin");
		setMinimumWidth(350);

		QVBoxLayout layout = new QVBoxLayout();

		QPushButton button = new QPushButton("Test");
		button.clicked.connect(this, "calculate()");

		QHBoxLayout buttonLayout = new QHBoxLayout();
		button = new QPushButton("Sluiten");
		button.setFixedWidth(75);
		button.clicked.connect(this, "accept()");
		buttonLayout.addWidget(button);

		layout.addLayout(buttonLayout);
		setLayout(layout);
		
		QIcon icon = new QIcon();
		icon.addFile("classpath:icon_64.png");
		setWindowIcon(icon);
	}
	
	private void calculate() {
		System.out.println("Done");
	}

}
