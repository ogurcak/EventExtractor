package Server;


import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;




public class Main {


	  public static void main(String args[]) {


		    Database.connect();


		    ServerSocket Server;

		    try {
			      Server = new ServerSocket(5000, 10, InetAddress.getByName("127.0.0.1"));

			      System.out.println("Server is ready and waiting for client request.");

			      while (true) {
					Socket connection = Server.accept();
					System.out.println("Client is trying to connect server.");
					(new HTTPServer(connection)).start();
			      }
		    } catch (UnknownHostException e) {
			      System.err.println(e.getMessage() + " :1");
		    } catch (IOException e) {
			      System.err.println(e.getMessage() + " :2");
		    }


	  }

}
