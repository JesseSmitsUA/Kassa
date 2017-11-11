package kassa.discovery;

import com.trolltech.qt.core.QCoreApplication;

public class Main {

	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		QCoreApplication.initialize(args);
		IOReceiver io = new IOReceiver();
		
		QCoreApplication.execStatic();
	}

}
