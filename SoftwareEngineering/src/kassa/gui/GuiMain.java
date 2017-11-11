package kassa.gui;

import java.sql.SQLException;

import kassa.gui.GuiAbstractWorkspace;

import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QCloseEvent;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMainWindow;
import com.trolltech.qt.gui.QMessageBox;

/**
 * Main class of program
 * 
 * @author Stephen Pauwels
 */
public class GuiMain extends QMainWindow {

	/**
	 * Main method - program entry point
	 * 
	 * @param argv
	 *            Command line arguments
	 */
	public static void main(String[] argv) {
		QApplication.initialize(argv);
		GuiMain main = new GuiMain();
		main.show();
		QApplication.execStatic();
	}

	private GuiAbstractWorkspace m_workspace;
	
	/**
	 * Constructor
	 */
	public GuiMain() {
		super((QMainWindow) null);

		setupGui();
	}

	/**
	 * Overriding closeEvent to detect when the user quits the program
	 */
	@Override
	protected void closeEvent(QCloseEvent event) {
		try {
			if (m_workspace == null) {
				event.accept();
			} else if (m_workspace.closeSystem()) {
				m_workspace.closeDatabase();
				event.accept();
				super.closeEvent(event);
			} else {
				event.ignore();
			}
		} catch(SQLException e) {

		}
	}

	private void setupGui() {
		try {
			setWindowTitle("Kassa");

			QIcon icon = new QIcon();
			icon.addFile("classpath:icon_64.png");
			setWindowIcon(icon);

			GuiWorkspaceFactory factory = new GuiWorkspaceFactory();
			m_workspace = factory.setup();

			setCentralWidget(m_workspace);

			this.setMinimumSize(1000, 700);
		}
		catch (SQLException e) {
			QMessageBox.critical(this, "Database fout",
					"Database schakelt over op lokale modus!");
			m_workspace.toLocalMode();
		}
	}
}
