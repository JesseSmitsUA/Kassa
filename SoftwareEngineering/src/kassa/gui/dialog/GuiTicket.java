package kassa.gui.dialog;

import kassa.core.orders.TableOrders;

import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QDialogButtonBox;
import com.trolltech.qt.gui.QFrame;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

/**
 * Ticket Dialog
 * 
 * @author Stephen Pauwels
 */
public class GuiTicket extends QDialog {

	private TableOrders m_orders;

	/**
	 * Constructore
	 */
	public GuiTicket(QWidget parent, TableOrders orders) {
		super(parent);
		m_orders = orders;
		drawGui();
	}

	/**
	 * Constructor
	 */
	public GuiTicket(TableOrders orders) {
		this(null, orders);
	}

	public void drawGui() {
		QVBoxLayout layout = new QVBoxLayout();

		// Ticket Overview
		QFrame ticketFrame = new QFrame();
		ticketFrame.setFrameStyle(1);
		QHBoxLayout ticketLayout = new QHBoxLayout();

		String[] ticket = m_orders.displayFinalOrders();

		QLabel quantity = new QLabel(ticket[0]);
		QLabel name = new QLabel(ticket[1]);
		QLabel price = new QLabel(ticket[2]);

		quantity.setAlignment(AlignmentFlag.AlignTop);
		name.setAlignment(AlignmentFlag.AlignTop);
		price.setAlignment(AlignmentFlag.AlignTop);

		ticketLayout.addWidget(quantity);
		ticketLayout.addWidget(name);
		ticketLayout.addWidget(price);

		ticketFrame.setLayout(ticketLayout);
		layout.addWidget(ticketFrame);

		// Total
		QLabel label = new QLabel("TOTAAL: \t\u20AC "
				+ m_orders.getTotalPrice() +"0");
		label.setFixedHeight(75);
		layout.addWidget(label);

		// Button Box Initialization
		QDialogButtonBox buttonBox = new QDialogButtonBox(
				QDialogButtonBox.StandardButton.createQFlags(
						QDialogButtonBox.StandardButton.Ok,
						QDialogButtonBox.StandardButton.Cancel));
		buttonBox.accepted.connect(this, "accept()");
		buttonBox.rejected.connect(this, "reject()");
		layout.addWidget(buttonBox);

		setLayout(layout);
		setWindowTitle("Ticket");
		
		QIcon icon = new QIcon();
		icon.addFile("classpath:icon_64.png");
		setWindowIcon(icon);
	}
}
