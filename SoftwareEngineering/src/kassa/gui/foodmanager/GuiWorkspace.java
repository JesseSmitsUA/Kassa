package kassa.gui.foodmanager;

import java.sql.SQLException;
import java.util.ArrayList;

import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.QSignalMapper;
import com.trolltech.qt.core.QTimer;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.Alignment;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QFont;
import com.trolltech.qt.gui.QGroupBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QScrollArea;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

import kassa.core.food.FoodManager;
import kassa.core.food.FoodOrder;
import kassa.core.storage.Storage;
import kassa.gui.GuiAbstractWorkspace;

public class GuiWorkspace extends GuiAbstractWorkspace {

	private FoodManager m_manager;
	
	private QLabel m_order_info_label;
	
	private QVBoxLayout m_layout;
	private QGroupBox m_manager_box;
	
	private QSignalMapper m_order_mapper;
	private QSignalMapper m_button_mapper;
	
	private String m_orig_style;
	private QPushButton m_selected_button;
	
	private int m_selected;
	private QPushButton m_reset_order_button;
	private QPushButton m_to_busy_button;
	private QPushButton m_to_delivered_button;
	
	private QTimer m_clear_selection_timer;
	
	private QLabel m_avg_timing_label;
	
	public GuiWorkspace(Storage database) {
		super(null);

		try {
			m_manager = new FoodManager(database);
			m_manager.load();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		m_selected = -1;
		
		QTimer timer = new QTimer();
		timer.timeout.connect(this, "refresh()");
		timer.start(10000);
		
		m_clear_selection_timer = new QTimer();
		m_clear_selection_timer.timeout.connect(this, "clearSelection()");
		
		m_order_mapper = new QSignalMapper();
		m_order_mapper.mappedInteger.connect(this, "order_clicked(int)");
		
		m_button_mapper = new QSignalMapper();
		m_button_mapper.mappedQObject.connect(this, "button_clicked(QObject)");
		
		m_orig_style = "";
		
		drawGui();
	}
	
	private void drawGui() {
		m_layout = new QVBoxLayout();
		
		QGroupBox order_box = new QGroupBox("Order Info");
		order_box.setFixedHeight(200);
		QVBoxLayout order_info = new QVBoxLayout();
		
		m_order_info_label = new QLabel();
		m_order_info_label.setFont(new QFont(null,20));
		
		order_info.addWidget(m_order_info_label);
		order_box.setLayout(order_info);
		order_box.setFlat(true);

		QWidget button_widget = new QWidget();
		button_widget.setFixedHeight(40);
		QHBoxLayout button_layout = new QHBoxLayout();

		m_reset_order_button = new QPushButton("Reset");
		m_reset_order_button.clicked.connect(this, "resetOrder()");
		m_reset_order_button.setEnabled(false);
		m_reset_order_button.setFixedHeight(30);
		m_reset_order_button.setFont(new QFont(null, 16));
		button_layout.addWidget(m_reset_order_button);
		
		m_to_busy_button = new QPushButton("Naar bezig");
		m_to_busy_button.clicked.connect(this, "toBusy()");
		m_to_busy_button.setEnabled(false);
		m_to_busy_button.setFixedHeight(30);
		m_to_busy_button.setFont(new QFont(null, 16));
		button_layout.addWidget(m_to_busy_button);
		
		m_to_delivered_button = new QPushButton("Naar afgeleverd");
		m_to_delivered_button.clicked.connect(this, "toDelivered()");
		m_to_delivered_button.setEnabled(false);
		m_to_delivered_button.setFixedHeight(30);
		m_to_delivered_button.setFont(new QFont(null, 16));
		button_layout.addWidget(m_to_delivered_button);
		
		button_widget.setLayout(button_layout);
		
		m_manager_box = new QGroupBox("Orders");
		m_manager_box.setFixedHeight(500);
		m_manager_box.setFlat(true);
		
		QHBoxLayout all_order_layout = new QHBoxLayout();
		
		ArrayList<String> supps = m_manager.getSupplements();
				
		for (int i = 0; i < 3; i++) {
			QScrollArea manager_scroll = new QScrollArea();
			manager_scroll.setVerticalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOn);
			
			QVBoxLayout order_layout = new QVBoxLayout();
			
			for (FoodOrder order : m_manager.getList(i)) {
				QPushButton button = new QPushButton(order.getInfo());
				if (order.getSupplement() != null && !order.getSupplement().equals("")) {
					QColor color = COLORS[supps.indexOf(order.getSupplement()) % COLORS.length];
					button.setStyleSheet("* { background-color: rgb(" + color.red() + "," + color.green() + "," + color.blue() + "); text-align: left}");
				}
				button.setFixedHeight(50);
				button.setMinimumWidth(200);
				button.setFont(new QFont(null, 25));
				
				order_layout.addWidget(button);
				button.clicked.connect(m_order_mapper, "map()");
				m_order_mapper.setMapping(button, order.getId());
				button.clicked.connect(m_button_mapper, "map()");
				m_button_mapper.setMapping(button, button);
			}

			if (m_manager.getList(i).size() == 0)
				order_layout.addWidget(new QWidget());
			
			QWidget widget = new QWidget();
			widget.setLayout(order_layout);
			manager_scroll.setWidget(widget);
			
			all_order_layout.addWidget(manager_scroll);
		}
		
		m_manager_box.setLayout(all_order_layout);
		
		QPushButton button = new QPushButton("Refresh");
		button.clicked.connect(this, "refresh()");
		button.setFixedHeight(35);
		
		m_layout.addWidget(button);
		m_layout.addWidget(order_box);
		m_layout.addStretch();
		m_layout.addWidget(button_widget);
		m_layout.addWidget(m_manager_box);
		
		m_avg_timing_label = new QLabel("");
		try {
			m_avg_timing_label.setText("Gemiddelde bedientijd: " + m_manager.getAvgTimings());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		m_layout.addWidget(m_avg_timing_label);
		
		setLayout(m_layout);
	}
	
	public void refresh() {
//		String selected_info = "";
//		if (m_selected_button != null)
//			selected_info = m_selected_button.text();
		m_selected_button = null;
		m_orig_style = "";
		
		m_layout.removeWidget(m_manager_box);
		m_layout.removeWidget(m_avg_timing_label);
		m_manager_box.dispose();
		m_avg_timing_label.dispose();
		
		m_order_mapper = new QSignalMapper();
		m_order_mapper.mappedInteger.connect(this, "order_clicked(int)");
		
		m_button_mapper = new QSignalMapper();
		m_button_mapper.mappedQObject.connect(this, "button_clicked(QObject)");
		
		try {
			m_manager.refresh();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		m_manager_box = new QGroupBox("Orders");
		m_manager_box.setFixedHeight(500);
		m_manager_box.setFlat(true);
		
		QHBoxLayout manager_layout = new QHBoxLayout();
		
		ArrayList<String> supps = m_manager.getSupplements();
		
		for (int i = 0; i < 3; i++) {
			QScrollArea manager_scroll = new QScrollArea();
			manager_scroll.setVerticalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOn);
			
			QVBoxLayout order_layout = new QVBoxLayout();
			order_layout.setAlignment(new Alignment(Qt.AlignmentFlag.AlignTop));
			
			for (FoodOrder order : m_manager.getList(i)) {
				QPushButton button = new QPushButton(order.getInfo());
				if (order.getSupplement() != null && !order.getSupplement().equals("")) {
					QColor color = COLORS[supps.indexOf(order.getSupplement()) % COLORS.length];
					button.setStyleSheet("* { background-color: rgb(" + color.red() + "," + color.green() + "," + color.blue() + "); text-align: left}");
				}
				button.setFixedHeight(50);
				button.setMinimumWidth(200);
				button.setFont(new QFont(null, 25));
				order_layout.addWidget(button);
				button.clicked.connect(m_order_mapper, "map()");
				m_order_mapper.setMapping(button, order.getId());
				button.clicked.connect(m_button_mapper, "map()");
				m_button_mapper.setMapping(button, button);
//				if (order.getInfo().equals(selected_info)) {
	//				button_clicked(button);
		//		}
			}

			if (m_manager.getList(i).size() == 0)
				order_layout.addWidget(new QWidget());
			
			QWidget widget = new QWidget();
			widget.setLayout(order_layout);
			manager_scroll.setWidget(widget);
			
			manager_layout.addWidget(manager_scroll);
		}
		
		m_avg_timing_label = new QLabel("");
		try {
			m_avg_timing_label.setText("Gemiddelde bedientijd: " + m_manager.getAvgTimings());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		m_layout.addWidget(m_avg_timing_label);
		
		m_manager_box.setLayout(manager_layout);
		m_layout.addWidget(m_manager_box);
		m_layout.addWidget(m_avg_timing_label);
	}
	
	private void order_clicked(int order_nr) {
		m_clear_selection_timer.stop();
		m_order_info_label.setText(m_manager.getDetails(order_nr));
		
		int status = m_manager.getStatus(order_nr);
		
		if (status == 1) {
			m_reset_order_button.setEnabled(false);
			m_to_busy_button.setEnabled(true);
			m_to_delivered_button.setEnabled(false);
		} else if (status == 2) {
			m_reset_order_button.setEnabled(true);
			m_to_busy_button.setEnabled(false);
			m_to_delivered_button.setEnabled(true);
		} else {
			m_reset_order_button.setEnabled(false);
			m_to_busy_button.setEnabled(false);
			m_to_delivered_button.setEnabled(false);
		}
		
		m_selected = order_nr;
		if (m_selected != -1)
			m_clear_selection_timer.start(15000);
	}
	
	@SuppressWarnings("unused")
	private void button_clicked(QObject button) {
		if (button instanceof QPushButton) {
			if (m_selected_button != null) {
				m_selected_button.setDown(false);
				m_selected_button.setStyleSheet(m_orig_style);
				m_orig_style = "";
			}
			
			QPushButton q_button = (QPushButton) button;
			m_orig_style = q_button.styleSheet();
			q_button.setStyleSheet("* { background-color: rgb(255,255,100); text-align: left}");
			q_button.setDown(true);
			m_selected_button = q_button;
		}
	}
	
	@SuppressWarnings("unused")
	private void clearSelection() {
		order_clicked(-1);
		m_clear_selection_timer.stop();
		
		if (m_selected_button != null) {
			m_selected_button.setDown(false);
			m_selected_button.setStyleSheet(m_orig_style);
			m_selected_button = null;
			m_orig_style = "";
		}
	}
	
	@SuppressWarnings("unused")
	private void toBusy() {
		try {
			m_manager.changeToBusy(m_selected);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		order_clicked(-1);
		refresh();
	}
	
	@SuppressWarnings("unused")
	private void toDelivered() {
		try {
			m_manager.changeToDelivered(m_selected);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		order_clicked(-1);
		refresh();
	}
	
	@SuppressWarnings("unused")
	private void resetOrder() {
		try {
			m_manager.resetOrder(m_selected);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		order_clicked(-1);
		refresh();
	}
	
	@Override
	public boolean closeSystem() {
		return true;
	}

	@Override
	public void closeDatabase() throws SQLException {
		m_manager.close();
	}

	@Override
	public void updateOrders() throws SQLException {

	}

	@Override
	public boolean freeze() {
		return false;
	}

	@Override
	public boolean toLocalMode() {
		return false;
	}

	@Override
	public boolean initMobileMode() {
		return false;
	}
	
	private static final QColor[] COLORS =	{
		new QColor(164, 0, 0),     new QColor(32, 74, 135),   new QColor(78, 154, 6),   new QColor(206, 92, 0),
		new QColor(92, 35, 102),   new QColor(143, 89, 2),    new QColor(196, 160, 0),
		new QColor(204, 0, 0),     new QColor(52, 101, 164),  new QColor(115, 210, 22), new QColor(245, 121, 0),
		new QColor(117, 80, 123),  new QColor(193, 125, 17),  new QColor(237, 212, 0),
		new QColor(239, 41, 41),   new QColor(114, 159, 207), new QColor(138, 226, 52), new QColor(252, 175, 62),
		new QColor(173, 127, 168), new QColor(233, 185, 110), new QColor(252, 233, 79)
		};

}
