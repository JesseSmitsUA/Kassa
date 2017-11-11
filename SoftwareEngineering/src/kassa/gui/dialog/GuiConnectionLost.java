package kassa.gui.dialog;

import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

/**
 * ProgressBar Dialog - Singleton
 * 
 * @author Stephen Pauwels
 */
public class GuiConnectionLost extends QDialog {

	private boolean m_halt;
	

	/**
	 * Constructor without parent
	 */
	public GuiConnectionLost() {
		this(null);
	}

	/**
	 * Constructor taking a parent Frame
	 * 
	 * @param parent
	 *            the Frame's parent
	 */
	public GuiConnectionLost(QWidget parent) {
		super(parent);
		m_halt = false;
		drawGui();
	}

	private void drawGui() {
		QVBoxLayout layout = new QVBoxLayout();

		layout.addWidget(new QLabel("Verbinding verbroken,\ngelieve te wachten tot de verbinding tot stand gebracht is\nVerbinden..."));
		
		setLayout(layout);
		setWindowTitle("Verbinding verbroken");
		
		QIcon icon = new QIcon();
		icon.addFile("classpath:icon_64.png");
		setWindowIcon(icon);
	}

	public void start() {
		if (!m_halt)
			show();
		m_halt = true;
	}

	public void stop() {
		if (m_halt)
			close();
		m_halt = false;
	}
}
