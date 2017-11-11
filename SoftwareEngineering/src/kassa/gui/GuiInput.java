package kassa.gui;

import kassa.core.orders.Order;

import com.trolltech.qt.core.QSignalMapper;
import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.gui.QFont;
import com.trolltech.qt.gui.QFrame;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

/**
 * Input section from Screen
 * 
 * @author Stephen
 */
@SuppressWarnings("unused")
public class GuiInput extends QFrame {

	/**
	 * signal emmitted when OK button has been pressed
	 */
	public final Signal0 Accept = new Signal0();
	
	public final Signal0 Clear = new Signal0();
	
	/* GUI + qt */
	private QPushButton[] m_buttons;
	private QFrame m_inputBox;
	private QLabel m_inputNr;
	private QFrame m_keypad;
	private QFrame m_order;
	private QLabel m_orderPrint;
	private QSignalMapper m_inputSignals;
	
	private Boolean m_def_value;

	/**
	 * Constructor without parent
	 */
	public GuiInput() {
		this(null);
	}

	/**
	 * Constructor taking a parent Frame
	 * 
	 * @param parent	the Frame's parent
	 */
	public GuiInput(QWidget parent) {
		super(parent);
		m_inputSignals = new QSignalMapper();
		m_inputSignals.mappedInteger.connect(this, "addDigit(int)");
		drawGui();
	}
	
	/**
	 * Draw Gui
	 */
	private void drawGui() {
		QVBoxLayout layout = new QVBoxLayout();
		setFrameStyle(1);

		m_inputBox = new QFrame();
		QVBoxLayout inputLayout = new QVBoxLayout();
		m_inputNr = new QLabel();
		m_inputNr.setAlignment(AlignmentFlag.AlignRight);
		m_inputNr.setFont(new QFont(null, 30));
		m_inputNr.setText("");
		inputLayout.addWidget(m_inputNr);
		m_inputBox.setLayout(inputLayout);
		m_inputBox.setMaximumHeight(70);
		m_inputBox.setFrameStyle(1);

		m_keypad = new QFrame();
		QGridLayout keypadLayout = new QGridLayout();
		m_buttons = new QPushButton[12];
		for (int i = 0; i < 9; i++) {
			QPushButton button = new QPushButton("" + (i + 1));
			button.setFont(new QFont(null, 20));
			button.clicked.connect(m_inputSignals, "map()");
			m_inputSignals.setMapping(button, i+1);
			m_buttons[i] = button;
		}
		QPushButton button = new QPushButton("0");
		button.setFont(new QFont(null, 20));
		button.clicked.connect(m_inputSignals, "map()");
		m_inputSignals.setMapping(button, 0);
		m_buttons[10] = button;

		button = new QPushButton("Clr");
		button.setFont(new QFont(null, 20));
		button.clicked.connect(this, "clearInput()");
		m_buttons[9] = button;

		button = new QPushButton("OK");
		button.setFont(new QFont(null, 20));
		button.clicked.connect(this, "accept()");
		m_buttons[11] = button;

		for (int i = 0; i < 12; i++) {
			m_buttons[i].setFixedWidth(70);
			keypadLayout.addWidget(m_buttons[i], i / 3, i % 3);
		}

		m_keypad.setMaximumHeight(300);
		m_keypad.setLayout(keypadLayout);
		m_keypad.setFrameStyle(1);

		m_order = new QFrame();
		
		QVBoxLayout orderLayout = new QVBoxLayout();
		m_orderPrint = new QLabel();
		m_orderPrint.setFont(new QFont(null, 12));
		m_orderPrint.setAlignment(AlignmentFlag.AlignTop);
		
		orderLayout.addWidget(m_orderPrint);
		
		button = new QPushButton("Alles wissen");
		button.clicked.connect(this, "clearOrder()");
		button.setFont(new QFont (null, 15));
		button.setMinimumHeight(65);
		orderLayout.addWidget(button);

		m_order.setLayout(orderLayout);
		m_order.setMinimumHeight(350);
		m_order.setFrameStyle(1);

		layout.addWidget(m_inputBox);
		layout.addWidget(m_keypad);
		layout.addWidget(m_order);
		setLayout(layout);
	}
	
	public int getNumberAndReset() {
		int return_value = Integer.parseInt(m_inputNr.text());
		clearInput();
		return return_value;
	}


	/**
	 * Disable/Enable keypad
 	 */
	public void disableKeypad(Boolean disable) {
		for (QPushButton button : m_buttons) {
			button.setDisabled(disable);
		}
		if (disable)
			m_inputNr.setText("");
		else
			clearInput();
	}
	
	/**
	 * Update the view of current order
	 */
	public void updateView(Order order) {
		m_orderPrint.setText(order.printTable());
	}

	/**
	 * Accept current order
	 */
	private void accept() {
		Accept.emit();
	}
	
	
	/**
	 * Clear current order
	 */
	private void clearOrder() {
		Clear.emit();
	}

	/**
	 * Add bit to number of items
	 */
	private void addDigit(int digit) {
		int input = Integer.parseInt(m_inputNr.text());
		
		if (m_def_value) {
			m_inputNr.setText("" + digit);
			m_def_value = false;
		}
		else if (input * 10 + digit < 10000)
			m_inputNr.setText("" + (input * 10 + digit));
	}

	/**
	 * Clear input
	 */
	private void clearInput() {
		m_inputNr.setText("" + 1);
		m_def_value = true;
	}
}
