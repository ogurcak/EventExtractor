package Server;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;




/**
 * The main purpose of this project is the analysis of available solutions and
 * creating of application for email client Thunderbird, which at least
 * partially automate adding events to Google calendar. The main part of
 * automation is analyzing message of email and then extracting name, time and
 * place of the event. For the design of application were selected development
 * tools Java, JavaScript and XUL. The application is divided into two parts: a
 * plugin for Thunderbird, which provides only connection with Google calendar
 * and application. Second part is application, which provides all the
 * computation functionality. Project also describes the analysis of existing
 * solutions used to extract information from text - GATE and Ontea, and their
 * later using in develop application.
 * 
 * <p>
 * This program make the server part of application, and provides the extraction functionality and connection with Thunderbird plugin. 
 * 
 * @author Filip Ogurcak
 * @version 1.0
 */
public class Main {

	  private static Logger logger = Logger.getLogger(Main.class.getName());





	  /**
	   * Default constructor
	   */
	  public Main() {

	  }





	  /**
	   * Main method, which starts the program. Firstly initialize from file the
	   * logger. Then connect database and when everything done, it's
	   * waiting on specific port to client connection.
	   * 
	   * @param args
	   *                  Program parameters
	   */
	  public static void main(String args[]) {
		    
		    

		    LogManager lm = LogManager.getLogManager();

		    try {
			      InputStream is = new FileInputStream("LogManager.txt");
			      lm.readConfiguration(is);
		    } catch (Exception e) {
			      System.err.println("Error: Logging failed!!" + e.getMessage());
		    }

		    Database.connect();


		    ServerSocket Server;
		    

		    try {
			      Server = new ServerSocket(Integer.parseInt(args[0]), 10, null);
			      logger.log(Level.INFO, "Server is ready and waiting for client request.");

			      while (true) {
					Socket connection = Server.accept();
					logger.log(Level.INFO, "Client is trying to connect server.");
					(new HTTPServer(connection)).start();
			      }
		    } catch (UnknownHostException e) {
			      logger.log(Level.SEVERE, e.getMessage() + " :Server socket problem");
		    } catch (IOException e) {
			      logger.log(Level.SEVERE, e.getMessage() + " :Server socket problem");
		    }


	  }

}
