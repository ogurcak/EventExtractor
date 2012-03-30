package Server;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;




final class Database {

	  private static String connect_string = "jdbc:sqlserver://shadowstreet.no-ip.org;database=EEDB";
	  private static String userid = "fogurcak";
	  private static String password = "heslodb123";

	  private static Connection conn = null;
	  private static Statement stmt = null;





	  static public void connect() {

		    try {
			      conn = DriverManager.getConnection(connect_string, userid, password);
			      stmt = conn.createStatement();

			      System.out.println("Database is ready.");
		    } catch (SQLException e) {
			      System.err.println(e.getMessage() + " :Database connection problem.");
		    }
	  }





	  static public void insert(String query) {

		    if (stmt != null) try {
			      stmt.executeUpdate(query);
		    } catch (SQLException e) {
			      System.err.println(e.getMessage() + " :Database insertion problem.");
		    }
		    else System.err.println("Database connection problem.");
	  }

}
