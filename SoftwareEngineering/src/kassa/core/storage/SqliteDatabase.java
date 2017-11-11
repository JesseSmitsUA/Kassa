package kassa.core.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteDatabase implements Database {
	
	private Connection m_conn;
	
	public SqliteDatabase() throws ClassNotFoundException, SQLException {
		openConnection();
	}
	
	public void openConnection() throws SQLException, ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		
		String url = "jdbc:sqlite:back_up.db";
		m_conn = DriverManager.getConnection(url);
	}

	@Override
	public void close() throws SQLException {
		try {
			m_conn.close();
			m_conn = null;
		} catch (Exception e) {
			// DO NOTHING
		}		
	}

	@Override
	public ResultSet query(String sql) throws SQLException {
		Statement stmt = m_conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		ResultSet returnSet = stmt.executeQuery(sql);
		return returnSet;
	}

	@Override
	public int updateQuery(String sql) throws SQLException {
		sql = sql.replaceAll("AUTO_INCREMENT", "AUTOINCREMENT");
		sql = sql.replaceAll("INT ", "INTEGER ");
		Statement stmt = null;
		ResultSet key = null;
		int id = -1;
		try {
			stmt = m_conn.createStatement();
			stmt.executeUpdate(sql);
			
			key = stmt.getGeneratedKeys();
			if (key.next())
				id = key.getInt(1);
			key.close();
			
			stmt.close();
			
			return id;
		} catch (SQLException e) {
			try {
				key.close();
				stmt.close();
				throw new SQLException(e.getMessage());
			} catch (Exception f) {
				throw new SQLException(e.getMessage());
			}
		}
	}

}
