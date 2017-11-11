package kassa.core.io;

import kassa.core.Tables;
import kassa.core.exceptions.MobileException;
import kassa.core.items.Item;
import kassa.core.items.Items;
import kassa.core.orders.Order;

import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.network.QTcpSocket;
import com.trolltech.qt.core.QXmlStreamAttributes;
import com.trolltech.qt.core.QXmlStreamReader;
import com.trolltech.qt.core.QXmlStreamWriter;

@SuppressWarnings("unused")
public class Mobile extends QObject{
	public final Signal0 DataChanged = new Signal0();
	
	public final Signal2<Order, Integer> NewOrder = new Signal2<Order, Integer>();

	private IOMobile m_io;
	
	private Items m_items;
	private Tables m_tables;
	private Order m_current_order;
	
	public Mobile(Items items, Tables tables) throws MobileException {
		m_io = new IOMobile();
		m_items = items;
		m_tables = tables;
		m_current_order = new Order();
		
		m_io.DataChanged.connect(DataChanged);
		m_io.MessageReceived.connect(this, "received(QTcpSocket, QByteArray)");
	}
	
	private void sendTableNrs(QTcpSocket socket) {
		QByteArray bytes = new QByteArray();
		QXmlStreamWriter writer = new QXmlStreamWriter(bytes);
		
		// Write XML
		writer.writeStartDocument();
		writer.writeStartElement("Tables");
		writer.writeAttribute("Nr", "" + m_tables.getNrTables());
		writer.writeEndElement();
		writer.writeEndDocument();
		
		m_io.send(socket, bytes);
	}
	
	private void sendItems(QTcpSocket socket) {		
		QByteArray bytes = new QByteArray();
		QXmlStreamWriter writer = new QXmlStreamWriter(bytes);
		
		writer.writeStartDocument();
		writer.writeStartElement("Items");
		for (Item item : m_items) {
			writer.writeStartElement("Item");
			writer.writeAttribute("Name", item.getName());
			writer.writeAttribute("Type", item.getCategory());
			writer.writeAttribute("SubType", item.getSubCat());
			writer.writeEndElement();
		}
		writer.writeEndElement();
		writer.writeEndDocument();
		
		m_io.send(socket, bytes);
	}
	
	private void sendConfirm(QTcpSocket socket) {
		QByteArray bytes = new QByteArray();
		QXmlStreamWriter writer = new QXmlStreamWriter(bytes);
		
		writer.writeStartDocument();
		writer.writeStartElement("Done");
		writer.writeEndDocument();
		
		m_io.send(socket, bytes);
	}
	
	private void received(QTcpSocket sender, QByteArray bytes) {
		if (bytes.size() == 0)
			return;
		
		int tableNr = -1;
		
		QXmlStreamReader reader = new QXmlStreamReader(bytes);
		while (!reader.atEnd() && !reader.hasError()) {
			QXmlStreamReader.TokenType token = reader.readNext();
			
			if (token == QXmlStreamReader.TokenType.StartDocument)
				continue;
			
			if (token == QXmlStreamReader.TokenType.StartElement) {
				if (reader.name().compareTo("Request") == 0 ) {
					QXmlStreamAttributes attributes = reader.attributes();
					if (attributes.hasAttribute("Type")) {
						// REQUEST TABLES
						if (attributes.value("Type").toString().compareTo("Tables") == 0) {
							sendTableNrs(sender);
						} 
						// REQUEST ITEMS
						else if (attributes.value("Type").toString().compareTo("Items") == 0) {
							sendItems(sender);
							sendConfirm(sender);
						}
					}
				} else if (reader.name().compareTo("Order") == 0) {
					tableNr = Integer.parseInt(reader.attributes().value("Table").toString());
				} else if (reader.name().compareTo("Item") == 0) {
					Item item = m_items.getItemByName(reader.attributes().value("Name").toString());
					m_current_order.addItem(item, Integer.parseInt(reader.attributes().value("Quantity").toString()));
				}
			}
		}
		
		if (m_current_order.notEmpty() && tableNr != -1) {
			NewOrder.emit(m_current_order, tableNr);
			m_current_order = new Order();
		}
		
	}
	
	public int nrConnected() {
		return m_io.nrConnected();
	}

}
