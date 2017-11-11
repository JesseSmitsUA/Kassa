package kassa.core.storage;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract interface Database {
	
	public abstract void close() throws SQLException;
	
	public abstract ResultSet query(String sql) throws SQLException;
	public abstract int updateQuery(String sql) throws SQLException;
}
