package kassa.gui;

import java.sql.SQLException;

import kassa.gui.dialog.GuiStartUp;

public class GuiWorkspaceFactory {
	public GuiWorkspaceFactory() {
		
	}

	public GuiAbstractWorkspace setup() throws SQLException {
		GuiStartUp startup = new GuiStartUp();
		startup.exec();

		if (startup.getSystemMode() == 0) { // Load Kassa
			if (startup.getClientMode()) { // Load Client mode
				return new kassa.gui.client.GuiWorkspace(startup.getDatabase(), startup.getItems(), startup.getTables(), startup.getPrinter());
			}
			// Load Master mode
			return new kassa.gui.master.GuiWorkspace(startup.getDatabase(), startup.getItems(), startup.getTables(), startup.getPrinter());
		}
		else if (startup.getSystemMode() == 1) { // Load Reservations
			return null;
		}
		else if (startup.getSystemMode() == 2) { // Load Food
			return new kassa.gui.foodmanager.GuiWorkspace(startup.getDatabase());
		}
		else 
			return null;
	}
}
