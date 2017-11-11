package kassa.gui;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.trolltech.qt.core.QObject;
import com.trolltech.qt.gui.QApplication;

public class Updater extends QObject {
	
	public Updater(GuiAbstractWorkspace workspace) {
		TimerTask task = new TimerTask() {
			public void run() {
                QApplication.invokeLater(new Runnable() {
					public void run() {
						m_workspace.update();						
					}
                });
			};
		};
		
		m_workspace = workspace;			
		m_timer = new Timer();
		m_timer.schedule(task, new Date(), 2500);
	}
	
	public void stopTimer() {
		m_timer.cancel();
	}
	
	private Timer m_timer;
	private GuiAbstractWorkspace m_workspace;
}
