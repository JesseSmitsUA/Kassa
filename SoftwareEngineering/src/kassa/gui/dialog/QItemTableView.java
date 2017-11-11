package kassa.gui.dialog;

import com.trolltech.qt.core.QRect;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QItemSelectionModel.SelectionFlag;
import com.trolltech.qt.gui.QItemSelectionModel.SelectionFlags;
import com.trolltech.qt.gui.QMouseEvent;
import com.trolltech.qt.gui.QTableView;

/**
 * Model for items
 * 
 * @author Stephen Pauwels
 */
public class QItemTableView extends QTableView {

	@Override
	public void mousePressEvent(QMouseEvent event) {
		if (event.button() == Qt.MouseButton.LeftButton
				&& (this.indexAt(event.pos()) == null)) {
			clearSelection();
			this.clicked.emit(null);
		} else {
			clearSelection();
			SelectionFlags flags = new SelectionFlags(SelectionFlag.Select);
			setSelection(new QRect(event.pos(), event.pos()), flags);
			this.clicked.emit(this.indexAt(event.pos()));
		}
	}

}
