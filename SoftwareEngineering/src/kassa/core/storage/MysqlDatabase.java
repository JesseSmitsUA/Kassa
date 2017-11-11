package kassa.core.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MysqlDatabase implements Database {
	
	private Connection m_conn;
	private String m_database;
	private String m_user;
	private String m_password;

	public MysqlDatabase(String host, String user, String password) throws ClassNotFoundException, SQLException {
		m_database = host;
		m_user = user;
		m_password = password;
		openConnection();
	}
	
	public void openConnection() throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		
		String url = "jdbc:mysql://" + getHost(m_database) + "/mysql";
		m_conn = DriverManager.getConnection(url, m_user, m_password);
		
		updateQuery("CREATE DATABASE  IF NOT EXISTS " + getDatabase(m_database));
		updateQuery("USE " + getDatabase(m_database));
	}
	
	private String getHost(String input) {
		int i = 0;
		String output = "";
		while (input.charAt(i) == '/') {
			i++;
		}
		while (input.charAt(i) != '/') {
			output += input.charAt(i);
			i++;
		}
		return output;
	}
	
	private String getDatabase(String input) {
		int i = 0;
		String output = "";
		while (input.charAt(i) == '/') {
			i++;
		}
		while (input.charAt(i) != '/') {
			i++;
		}
		output = input.substring(i+1, input.length());
		return output;
	}

	/**
	 * Close the connection with the database
	 */
	public void close() {
		try {
			m_conn.close();
			m_conn = null;
		} catch (Exception e) {
			// DO NOTHING
		}
	}
	
	public String getDB() {
		return m_database;
	}

	/**
	 * Perform a search query on the database
	 * 
	 * @param sql
	 *            Query for searching
	 * @return ResultSet All results for the query
	 * @throws SQLExceptino
	 *             Error in database connection
	 */
	public ResultSet query(String sql) throws SQLException {
		Statement stmt = m_conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		try {
			m_conn.isValid(2);
		} catch (AbstractMethodError e) {
			
		}
		ResultSet returnSet = stmt.executeQuery(sql);
		return returnSet;
	}

	/**
	 * Performs an update query on the database
	 * 
	 * @param sql
	 *            Query for updating
	 * @throws SQLException
	 *             Error when connecting to database
	 */
	public int updateQuery(String sql) throws SQLException {
		Statement stmt = null;
		ResultSet key = null;
		int id = -1;
		try {
			try {
				m_conn.isValid(2);
			} catch (AbstractMethodError e) {
				
			}
			
			stmt = m_conn.createStatement();

			stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			
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
