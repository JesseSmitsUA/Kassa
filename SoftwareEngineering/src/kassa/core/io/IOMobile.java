package kassa.core.io;

import java.util.ArrayList;

import kassa.core.exceptions.MobileException;

import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QDataStream;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.QSignalMapper;
import com.trolltech.qt.network.QAbstractSocket.SocketState;
import com.trolltech.qt.network.QHostAddress;
import com.trolltech.qt.network.QTcpServer;
import com.trolltech.qt.network.QTcpSocket;

@SuppressWarnings("unused")
public class IOMobile extends QObject {
	
	public final Signal0 DataChanged = new Signal0();
	public final Signal1<QTcpSocket> NewConnection = new Signal1<QTcpSocket>();
	public final Signal2<QTcpSocket, QByteArray> MessageReceived = new Signal2<QTcpSocket, QByteArray>();
	
	QTcpServer m_server;
	ArrayList<QTcpSocket> m_nodes;
	
	int m_size; // received size
	
	QSignalMapper m_node_mapper;
	QSignalMapper m_message_mapper;
	
	public IOMobile() throws MobileException {
		m_server = new QTcpServer();
		m_nodes = new ArrayList<QTcpSocket>();
		m_size = 0;
		
		m_node_mapper = new QSignalMapper();
		m_node_mapper.mappedQObject.connect(this, "nodeDisconnected(QObject)");
		
		m_message_mapper = new QSignalMapper();
		m_message_mapper.mappedQObject.connect(this, "receive(QObject)");
		
		m_server.newConnection.connect(this, "newConnection()");
		
		if (!m_server.listen(new QHostAddress(QHostAddress.SpecialAddress.Any), 6666))
			throw new MobileException("Couldn't start server!");
	}

	public void newConnection() {
		if (m_server.hasPendingConnections()) {
			QTcpSocket socket = m_server.nextPendingConnection();
			
			socket.disconnected.connect(m_node_mapper, "map()");
			socket.error.connect(m_node_mapper, "map()");
			m_node_mapper.setMapping(socket, socket);
			socket.readyRead.connect(m_message_mapper, "map()");
			m_message_mapper.setMapping(socket, socket);
			
			m_nodes.add(socket);
									
			DataChanged.emit();
			NewConnection.emit(socket);
		}
	}
	
	public int nrConnected() {
		return m_nodes.size();
	}
	
	private void nodeDisconnected(QObject object) {
		if (object instanceof QTcpSocket) {
			QTcpSocket socket = (QTcpSocket) object;
			m_nodes.remove(socket);
			
			DataChanged.emit();
		}
	}
	
	/**
	 * Used to send a message to a node
	 */
	public void send(QTcpSocket socket, QByteArray message) {		
		if (socket.state() != SocketState.ConnectedState)
			return;
		
		QDataStream data = new QDataStream(socket);

		data.writeInt(message.size()); // Size of the message, to send individual messages over a TCP stream
		socket.write(message);
	}
	
	/**
	 * Receive a message from a node
	 */
	private void receive(QObject object) {
		if (object instanceof QTcpSocket) {
			QTcpSocket socket = (QTcpSocket) object;
					
			while (socket.bytesAvailable() >= 4) {
				while (m_size == 0) {
					if (socket.bytesAvailable() < 4)
						return;
					QDataStream data = new QDataStream(socket);
					m_size = data.readInt();
				}
				if (socket.bytesAvailable() < m_size) 	
					return;
				
				QByteArray message = socket.read(m_size);
				m_size = 0;
				MessageReceived.emit(socket, message);
			}
			
		}
	}
	
}
