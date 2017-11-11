package kassa.gui.table;

import kassa.core.Tables;
import kassa.core.orders.TableOrders;

import com.trolltech.qt.core.QPointF;
import com.trolltech.qt.core.QRectF;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QFont;
import com.trolltech.qt.gui.QGraphicsItem;
import com.trolltech.qt.gui.QGraphicsSceneMouseEvent;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QStyleOptionGraphicsItem;
import com.trolltech.qt.gui.QWidget;

public class GraphicTable extends QGraphicsItem {
	private enum view_mode { VIEW_MODE, EDIT_MODE }

	private view_mode m_mode;
	private int m_tableNr;
	private TableOrders m_table;
	
	public final Signal1<Integer> tableSelected = new Signal1<Integer>();
	public final Signal2<Integer, QPointF> tableMoved = new Signal2<Integer, QPointF>();
		
	public GraphicTable(int tableNr, TableOrders table, QPointF pos) {
		m_mode = view_mode.VIEW_MODE;
		m_tableNr = tableNr;
		m_table = table;
		
		if (pos != null)
			setPos(pos);
		
		setFlag(QGraphicsItem.GraphicsItemFlag.ItemIsSelectable);
		setAcceptHoverEvents(true);
	}
	
	public void updateTable(Tables tables) {
		m_table = tables.getTable(m_tableNr);
	}
	
	public void changeToEdit() {
		m_mode = view_mode.EDIT_MODE;
		QGraphicsItem.GraphicsItemFlags flags = new GraphicsItemFlags();
		flags.set(QGraphicsItem.GraphicsItemFlag.ItemIsSelectable);
		flags.set(QGraphicsItem.GraphicsItemFlag.ItemIsMovable);
		this.setFlags(flags);
	}
	
	public void changeToView() {
		m_mode = view_mode.VIEW_MODE;
		this.setFlags(QGraphicsItem.GraphicsItemFlag.ItemIsSelectable);
	}

	@Override
	public QRectF boundingRect() {
		return new QRectF(((m_tableNr-1) % 7)*100, ((m_tableNr-1) / 7)*75, 75, 60);
	}

	@Override
	public void paint(QPainter painter, QStyleOptionGraphicsItem arg1, QWidget arg2) {
		int tableNr = m_tableNr - 1;
		
		if (m_table.notEmpty())
			painter.fillRect((tableNr % 7)*100, (tableNr / 7)*75,
					75, 60, new QColor("red"));
		else {
			painter.fillRect((tableNr % 7)*100, (tableNr / 7)*75,
					75, 60, new QColor("green"));
		}
		
		painter.drawRect((tableNr % 7)*100, (tableNr / 7)*75, 75, 60);
		QFont font = new QFont();
		font.setPointSize(25);
		painter.setFont(font);
		painter.drawText((tableNr % 7)*100, (tableNr / 7)*75,
				75, 60, Qt.AlignmentFlag.AlignCenter.value(), "" + m_tableNr);
	}
	
	public void mousePressEvent(QGraphicsSceneMouseEvent event) {
		if (m_mode == view_mode.VIEW_MODE)
			tableSelected.emit(m_tableNr);
	}
	
	public void mouseReleaseEvent(QGraphicsSceneMouseEvent event) {
		super.mouseReleaseEvent(event);
		if (m_mode == view_mode.EDIT_MODE) {
			this.setSelected(false);
			int x = (int) pos().x();
			int y = (int) pos().y();
			setPos(x - (x % 25), y - (y % 15));
			tableMoved.emit(m_tableNr, pos());
		}
	}

}
