package kassa.gui.table;

import kassa.core.Tables;

import com.trolltech.qt.core.QSize;
import com.trolltech.qt.gui.QFont;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QWidget;

public class GuiTableSelector extends QWidget {
	private enum view_mode { VIEW_MODE, EDIT_MODE }
	
	private view_mode m_mode;
	private Tables m_tables;
	
	private TableView m_view;
	private QLabel m_edit_label;
	private QPushButton m_change_mode;
	
	public final Signal1<Tables> tablesUpdated = new Signal1<Tables>();
	public final Signal1<Integer> tableSelected = new Signal1<Integer>();
	
	public GuiTableSelector(Tables tables) {
		m_mode = view_mode.VIEW_MODE;
		m_tables = tables;

		QVBoxLayout layout = new QVBoxLayout();
		
		m_edit_label = new QLabel("Bewerken");
		m_edit_label.setFont(new QFont(null, 18));
		layout.addWidget(m_edit_label);
		m_edit_label.setHidden(true);
		
		m_view = new TableView(m_tables);
		m_view.tableSelected.connect(tableSelected);
		layout.addWidget(m_view);
		
		m_change_mode = new QPushButton("Edit");
		m_change_mode.clicked.connect(this, "changeMode()");
		m_change_mode.setFixedSize(new QSize(100,30));
		layout.addWidget(m_change_mode);
		
		setLayout(layout);
	}
	
	public void changeMode() {
		if (m_mode == view_mode.EDIT_MODE) {
			m_mode = view_mode.VIEW_MODE;
			m_edit_label.setHidden(true);
			m_change_mode.setText("Edit");
			m_view.changeToView();
			
			tablesUpdated.emit(m_tables);
		}
		else {
			m_mode = view_mode.EDIT_MODE;
			m_edit_label.setHidden(false);
			m_change_mode.setText("Klaar");
			m_view.changeToEdit();
		}	
	}
	
	public void updateTables(Tables tables) {
		m_tables = tables;
		m_view.updateView(m_tables);
	}
}
