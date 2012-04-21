package Server;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;




final class Database {

	  private static String connect_string = "jdbc:sqlserver://shadowstreet.no-ip.org;database=EEDB";
	  private static String userid = "fogurcak";
	  private static String password = "heslodb123";

	  private static Connection conn = null;
	  private static Statement stmt = null;

	  private static Logger logger = Logger.getLogger(Database.class.getName());





	  static public void connect() {

		    try {
			      conn = DriverManager.getConnection(connect_string, userid, password);
			      stmt = conn.createStatement();

			      logger.log(Level.INFO, "Database is ready.");
		    } catch (SQLException e) {
			      logger.log(Level.WARNING, e.getMessage() + " :Database connection problem.");
		    }
	  }





	  static public void insert(String query) throws SQLException {

		    if (stmt != null) {
			      stmt.executeUpdate(query);
			      logger.log(Level.INFO, "Saved to database.");
		    } else logger.log(Level.WARNING, "Database connection problem.");
	  }





	  static public void insert(PreparedStatement statment) throws SQLException {

		    if (stmt != null) {
			      statment.execute();
			      logger.log(Level.INFO, "Saved to database.");
		    } else logger.log(Level.WARNING, "Database connection problem.");
	  }





	  static public Connection getConnection() {

		    return conn;
	  }

}
