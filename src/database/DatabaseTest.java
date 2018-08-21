package database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import database.SqliteDb;
public class DatabaseTest {

	public static void main(String[] args) {
		SqliteDb myDb = new SqliteDb("test.db");
		ResultSet resultSet = myDb.select("SELECT * FROM ParcelableClass");
		if (resultSet == null) {
			System.out.println("result set is null");
		} else {
			System.out.println("result set is not null");
		}
		int count = 0;
		try {
			while (resultSet.next()) {
				System.out.println(count);
				String className = resultSet.getString("CLASSNAME");
				System.out.println(className);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// so rubbish....
		// let's hand made
		System.out.println("hand made");
	
		selectAll();
	
	


	}
	private static Connection connect() {
		Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:test.db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            
            System.out.println("Connection to SQLite has been established.");
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
	}
	public static void selectAll(){
        String sql = "SELECT * FROM ParcelableClass";
        
        try (Connection conn = connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            // loop through the result set
             stmt.close();
            while (rs.next()) {
                System.out.println(
                                   rs.getString("CLASSNAME") + "\t" +
                                   rs.getString("SUPERCLASS") + "\t" 
                                   );
                break;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}
