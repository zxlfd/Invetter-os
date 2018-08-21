package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import main.Common;
import util.LogUtil;

public class SqliteDb {
	
	public Connection connection = null;
	private Statement stmt = null;
	private String DB_PATH = "";

	public SqliteDb(String dbPath) {
		DB_PATH=dbPath;
		
		try {
			java.lang.Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
			System.out.println(connection.getAutoCommit());
//			connection.setAutoCommit(false);

		//	connection.commit();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}
	
	public void executeUpdate(String sql){
		synchronized (DB_PATH) {
			try {
				stmt = connection.createStatement();
				stmt.executeUpdate(sql);
	//			connection.commit();
			} catch (SQLException e) {
				System.out.println("Sql Error : " + sql);
				e.printStackTrace();
			} finally {
				if (stmt != null) {
					try{
						stmt.close();
					}catch (SQLException e) {
						LogUtil.error("SqlUpdate", sql);
						e.printStackTrace();
					} 
					
				}
			}

		}
	}
	public ResultSet select(String sql){
//		synchronized (DB_PATH) {
			ResultSet resultSet = null;
			try {
				stmt = connection.createStatement();
				resultSet = stmt.executeQuery(sql);
				// connection.commit();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return resultSet;
//		}
	}
	
	
	public boolean isClosed() {
		try {
			return connection != null && connection.isClosed();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void close() {
		if (!isClosed()) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
