package kassa.core.io;

import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.network.QHostAddress;
import com.trolltech.qt.network.QUdpSocket;
import com.trolltech.qt.network.QUdpSocket.HostInfo;

public class IODetectDB extends QObject {
	
	QUdpSocket m_socket;
	
	public final Signal1<String> locationReceived = new Signal1<String>();

	public IODetectDB() {
		m_socket = new QUdpSocket(this);
		if (!m_socket.bind(54321, QUdpSocket.BindFlag.ShareAddress)) {
			System.out.println("Error");
		}
		
		m_socket.readyRead.connect(this, "locationReceived()");
	}
	
	public void requestLocation() {
		QByteArray data = new QByteArray("Request Location");
		m_socket.writeDatagram(data, new QHostAddress(QHostAddress.SpecialAddress.Broadcast), 54321);
	}
	
	@SuppressWarnings("unused")
	private void locationReceived() {
		HostInfo sender = new HostInfo();
		byte[] bytes = new byte[(int) m_socket.pendingDatagramSize()];
		
		m_socket.readDatagram(bytes, sender);
		
		String s = new String(bytes);
		if (s.startsWith("Location")) {
			locationReceived.emit(sender.address.toString());
			m_socket.close();
		}
	}
}
