package kassa.gui.table;

import java.sql.SQLException;

import kassa.core.Tables;

import com.trolltech.qt.core.QPointF;
import com.trolltech.qt.core.QRectF;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QGraphicsScene;
import com.trolltech.qt.gui.QGraphicsView;
import com.trolltech.qt.gui.QPainter;

public class TableView extends QGraphicsView {
	private enum view_mode { VIEW_MODE, EDIT_MODE }

	private view_mode m_mode;
	private Tables m_tables;
	
	private QGraphicsScene m_scene;
	
	public final Signal0 toEditMode = new Signal0();
	public final Signal0 toViewMode = new Signal0();
	public final Signal1<Tables> tablesUpdated = new Signal1<Tables>();
	public final Signal1<Integer> tableSelected = new Signal1<Integer>();
	
	public TableView(Tables tables) {
		m_mode = view_mode.VIEW_MODE;
		m_tables = tables;
		
		setTransformationAnchor(ViewportAnchor.AnchorViewCenter);
        setResizeAnchor(ViewportAnchor.AnchorViewCenter);
		setRenderHints(QPainter.RenderHint.Antialiasing, QPainter.RenderHint.TextAntialiasing);
		m_scene = new QGraphicsScene(this);
		m_scene.setSceneRect(0, 0, width(), height());
		
		setScene(m_scene);
	}
	
	public void updateView(Tables tables) {
		m_tables = tables;
		tablesUpdated.emit(m_tables);
		repaint();
		m_scene.update();
	}
	
	public void changeToEdit() {
		m_mode = view_mode.EDIT_MODE;
		toEditMode.emit();
		setHorizontalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOn);
		setVerticalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOn);
	}
	
	public void changeToView() {
		m_mode = view_mode.VIEW_MODE;
		toViewMode.emit();
		setHorizontalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);
		setVerticalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);
	}
	
	public void tableMoved(Integer tableNr, QPointF pos) throws SQLException {
//		m_tables.setPosition(tableNr, pos);
	}
	
	public void drawBackground(QPainter painter, QRectF rect) {
		if (m_mode != view_mode.EDIT_MODE)
			return;
		
		// Vertical lines
		for (int x = 0; x < width(); x+=25)
			painter.drawLine(x, -1000, x, height());
		for (int x = -25; x > -width(); x-= 25)
			painter.drawLine(x, -1000, x, height());
		
		// Horizontal lines
		for (int y = 0; y < height(); y+=15)
			painter.drawLine(-1000,y,width(), y);
		for (int y = -15; y > -height(); y-=15)
			painter.drawLine(-1000,y,width(), y);
	}

}
