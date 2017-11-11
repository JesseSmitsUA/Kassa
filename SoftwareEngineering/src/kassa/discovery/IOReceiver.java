package kassa.discovery;

import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.network.QHostAddress;
import com.trolltech.qt.network.QUdpSocket;
import com.trolltech.qt.network.QUdpSocket.HostInfo;

public class IOReceiver extends QObject {

	QUdpSocket m_socket;
	
	public IOReceiver() {
		m_socket = new QUdpSocket(this);
		if (!m_socket.bind(54321, QUdpSocket.BindFlag.ShareAddress)) {
			System.out.println("Error");
		}
		
		m_socket.readyRead.connect(this, "readDatagram()");
	}
	
	@SuppressWarnings("unused")
	private void readDatagram() {
		while (m_socket.hasPendingDatagrams()) {
			HostInfo sender = new HostInfo();
			byte[] bytes = new byte[(int) m_socket.pendingDatagramSize()];

			m_socket.readDatagram(bytes, sender);

			String s = new String(bytes);
			if (s.startsWith("Request Location")) {
				QByteArray data = new QByteArray("Location");
				m_socket.writeDatagram(data, new QHostAddress(QHostAddress.SpecialAddress.Broadcast), 54321);
			}
		}
	}
}
