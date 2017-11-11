package kassa.gui.dialog;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QProgressDialog;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

/**
 * ProgressBar Dialog - Singleton
 * 
 * @author Stephen Pauwels
 */
public class GuiProgress extends QDialog {

	private static GuiProgress m_singleton;

	public static GuiProgress getProgress() {
		if (m_singleton == null) {
			m_singleton = new GuiProgress();
		}
		return m_singleton;
	}

	private QProgressDialog m_Progress;

	/**
	 * Constructor without parent
	 */
	private GuiProgress() {
		this(null);
	}

	/**
	 * Constructor taking a parent Frame
	 * 
	 * @param parent
	 *            the Frame's parent
	 */
	private GuiProgress(QWidget parent) {
		super(parent);
		drawGui();
	}

	private void drawGui() {
		QVBoxLayout layout = new QVBoxLayout();

		m_Progress = new QProgressDialog("Data verzamelen...", "Cancel", 0, 100);
		m_Progress.setWindowModality(Qt.WindowModality.WindowModal);

		layout.addWidget(m_Progress);
		
		setLayout(layout);
		
		QIcon icon = new QIcon();
		icon.addFile("classpath:icon_64.png");
		setWindowIcon(icon);
	}

	public void setValue(int value) {
		m_Progress.setValue(value);
	}

	public void start() {
		m_Progress.setValue(0);
		show();
	}

	public void stop() {
		m_Progress.setValue(100);
		close();
		m_Progress.setValue(0);
	}
}
