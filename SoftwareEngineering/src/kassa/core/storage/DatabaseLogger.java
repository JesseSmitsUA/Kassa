package kassa.core.storage;

import java.sql.SQLException;

public class DatabaseLogger {

	private Database m_database;
	
	public DatabaseLogger(Database database) throws SQLException {
		m_database = database;
		
		m_database.updateQuery("CREATE TABLE IF NOT EXISTS log (time DATETIME, table_nr INT, message VARCHAR(255))");
		
		addLog("Loaded database");
	}
	
	public void addLog(String log) throws SQLException {
		m_database.updateQuery("INSERT INTO log (time, message) VALUES (now(), \"" + log + "\")");
	}
	
	public void addTableLog(int table_nr, String log) throws SQLException {
		m_database.updateQuery("INSERT INTO log (time, table_nr, message) VALUES (now(), " + table_nr + ", \"" + log + "\")");
	}
}
