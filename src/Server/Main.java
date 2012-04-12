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




public class Main {

	  private static Logger logger = Logger.getLogger(Main.class.getName());





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
			      Server = new ServerSocket(5000, 10, InetAddress.getByName("127.0.0.1"));

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
