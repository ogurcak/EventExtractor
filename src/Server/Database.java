package Server;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;




/**
 * Class provides connection with predefined database and allow inserts
 * data into it.
 * <p>
 * Class is made only for this program.
 * 
 * @author Filip Ogurcak
 * @version 1.0
 */
final public class Database {

	  private static String connect_string = "jdbc:sqlserver://localhost;database=EEDB";
	  private static String userid = "fogurcak";
	  private static String password = "heslodb123";

	  private static Connection conn = null;
	  private static Statement stmt = null;

	  private static Logger logger = Logger.getLogger(Database.class.getName());





	  /**
	   * Default constructor
	   */
	  public Database() {

	  }





	  /**
	   * Function to connect database by predefined path, user and password.
	   */
	  static public void connect() {

		    try {
			      conn = DriverManager.getConnection(connect_string, userid, password);
			      stmt = conn.createStatement();

			      logger.log(Level.INFO, "Database is ready.");
		    } catch (SQLException e) {
			      logger.log(Level.WARNING, e.getMessage() + " :Database connection problem.");
		    }
	  }





	  /**
	   * Insert data to database with query written as string.
	   * 
	   * @param query
	   *                  string with predefined SQL structure
	   * @throws SQLException
	   *                   If an exception during insert to database
	   *                   occurred
	   */
	  static public void insert(String query) throws SQLException {

		    if (stmt.getConnection().isClosed()) {
			      connect();
			      logger.log(Level.INFO, "Database reconnected.");
		    }
		    if (stmt != null) {
			      stmt.executeUpdate(query);
			      logger.log(Level.INFO, "Saved to database.");
		    } else logger.log(Level.WARNING, "Database connection problem.");
	  }





	  /**
	   * Insert data to database by prepared statement query.
	   * 
	   * @param statement
	   *                  prepared statement
	   * @throws SQLException
	   *                   If an exception during insert to database
	   *                   occurred
	   */
	  static public void insert(PreparedStatement statement) throws SQLException {

		    if (stmt != null) {
			      statement.execute();
			      logger.log(Level.INFO, "Saved to database.");
		    } else logger.log(Level.WARNING, "Database connection problem.");
	  }





	  /**
	   * Return connection to database, or null if connection was't
	   * established.
	   * 
	   * @return established connection
	   */
	  static public Connection getConnection() {

		    return conn;
	  }

}
