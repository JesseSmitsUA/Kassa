package kassa.gui.dialog;

import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QDoubleValidator;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;
import com.trolltech.qt.gui.QDoubleValidator.Notation;

/**
 * Return dialog
 * 
 * @author Stephen Pauwels
 */
@SuppressWarnings("unused")
public class GuiReturn extends QDialog {

	private QLineEdit m_input;

	private QLabel m_output;

	private double m_total;
	
	private QCheckBox m_checkBox;
	
	public final Signal1<Boolean> viewChanged = new Signal1<Boolean>();

	/**
	 * Constructor without parent
	 */
	public GuiReturn(double price) {
		this(null, price);
	}

	/**
	 * Constructor taking a parent Frame
	 * 
	 * @param parent
	 *            the Frame's parent
	 */
	public GuiReturn(QWidget parent, double price) {
		super(parent);
		m_total = price;
		drawGui();
	}

	private void calculate() {
		if (m_input.text().length() == 0) {
			m_output.setText("Terug te geven: \n");
			return;
		}
		double total = round(Double.parseDouble(m_input.text().replace(',', '.')) - m_total, 2);
		if (total < 0) {
			m_output.setText("Te weinig betaald!");
			return;
		}

		String output = "Terug te geven: " + total + "0\n";

		m_output.setText(output);
	}

	private void drawGui() {
		QVBoxLayout layout = new QVBoxLayout();
		layout.addWidget(new QLabel("Bedrag te betalen: \u20AC" + m_total +"0"));

		QHBoxLayout input = new QHBoxLayout();
		input.addWidget(new QLabel("Betaald: "));
		m_input = new QLineEdit();
		QDoubleValidator validator = new QDoubleValidator(this);
		validator.setBottom(0);
		validator.setDecimals(2);
		validator.setNotation(Notation.StandardNotation);
		m_input.setValidator(validator);
		input.addWidget(m_input);
		layout.addLayout(input);

		QPushButton button = new QPushButton("Bereken!");
		button.clicked.connect(this, "calculate()");
		layout.addWidget(button);

		m_output = new QLabel("Terug te geven:\n");
		layout.addWidget(m_output);
		
		QHBoxLayout checkLayout = new QHBoxLayout();
		m_checkBox = new QCheckBox();
		m_checkBox.toggled.connect(this, "viewCheck()");
		checkLayout.addWidget(new QLabel("Laat dit niet meer zien"));
		checkLayout.addWidget(m_checkBox);
		layout.addLayout(checkLayout);

		button = new QPushButton("Sluiten");
		button.clicked.connect(this, "close()");
		layout.addWidget(button);

		setWindowTitle("Weergave");
		
		setLayout(layout);
		
		QIcon icon = new QIcon();
		icon.addFile("classpath:icon_64.png");
		setWindowIcon(icon);
	}

	private double round(double Rval, int Rpl) {
		double p = Math.pow(10, Rpl);
		Rval = Rval * p;
		double tmp = Math.round(Rval);
		return (float) tmp / p;
	}
	
	private void viewCheck() {
		viewChanged.emit(!m_checkBox.isChecked());
	}
}
