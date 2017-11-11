package kassa.gui;

import java.sql.SQLException;

import com.trolltech.qt.gui.QFrame;
import com.trolltech.qt.gui.QWidget;

public abstract class GuiAbstractWorkspace extends QFrame {
	public GuiAbstractWorkspace(QWidget parent) {
		super(parent);
	}
	
	public abstract boolean closeSystem();
	
	public abstract void closeDatabase() throws SQLException;

	public abstract void updateOrders() throws SQLException;
	
	public abstract boolean freeze();
	public abstract boolean toLocalMode();
	public abstract boolean initMobileMode();
}
