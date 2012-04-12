package Server;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import Extractor.Event;
import Extractor.VCalendar;




public class HTTPServer extends Thread {

	  private Socket connection = null;
	  private BufferedReader incoming = null;
	  private DataOutputStream outgoing = null;

	  private String currentVersion = "2.1";

	  private static Logger logger = Logger.getLogger(HTTPServer.class.getName());





	  public HTTPServer(Socket connection) {

		    this.connection = connection;
	  }





	  public void run() {

		    try {
			      logger.log(Level.INFO, "The Client " + connection.getInetAddress() + ":" + connection.getPort() + " is connected.");

			      incoming = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			      outgoing = new DataOutputStream(connection.getOutputStream());

			      String authorization = incoming.readLine();
			      StringTokenizer tokenizer = new StringTokenizer(authorization);
			      String httpMethod = tokenizer.nextToken();
			      String httpQueryString = tokenizer.nextToken();

			      if (httpMethod.equals("POST")) {
					logger.log(Level.INFO, "Client send POST request:");

					String extractionMethod = null;
					String action = null;


					String line = incoming.readLine();
					String incomingLine = line;
					while (line.length() > 0) {
						  if (line.contains("Authorization: ")) {
							    tokenizer = new StringTokenizer(line);
							    httpMethod = tokenizer.nextToken();
							    httpQueryString = tokenizer.nextToken();
							    httpQueryString = tokenizer.nextToken();
							    if (!httpQueryString.equals(currentVersion)) {
								      logger.log(Level.INFO, incomingLine);
								      sendResponse(400, "Error: A newer version is available.\r\n");
								      throw (new Exception("Plugin has old version."));

							    }
						  }
						  if (line.contains("ExtractionMethod: ")) {
							    tokenizer = new StringTokenizer(line);
							    httpMethod = tokenizer.nextToken();
							    extractionMethod = tokenizer.nextToken();
						  }
						  if (line.contains("Action: ")) {
							    tokenizer = new StringTokenizer(line);
							    httpMethod = tokenizer.nextToken();
							    action = tokenizer.nextToken();
						  }

						  line = incoming.readLine();
						  incomingLine = incomingLine.concat("\n" + line);
					}


					if (action.contains("ANALYZE")) {


						  String message = "";
						  while (incoming.ready()) {
							    line = incoming.readLine();
							    message = message.concat(line);
							    message = message.concat("\n");
						  }


						  incomingLine = incomingLine.concat("\n\n" + message);
						  logger.log(Level.INFO, incomingLine);

						  // Analyzation Part

						  Event event = null;

						  try {

							    @SuppressWarnings("unchecked")
							    Class<Event> c2 = (Class<Event>) Class.forName(extractionMethod);

							    if (c2.getSuperclass() != Event.class) {
								      sendResponse(404, "Error: Ilegal extraction method \"" + extractionMethod + "\". Select another one.");
								      logger.log(Level.SEVERE, "Ilegal Extraction method \"" + extractionMethod + "\".");
								      throw (new Exception("Ilegal Extraction method \"" + extractionMethod + "\"."));
							    }
							    Constructor<Event> ctor = c2.getConstructor();
							    event = ctor.newInstance();
						  } catch (ClassNotFoundException e) {
							    sendResponse(404, "Error: Extraction method \"" + extractionMethod + "\" doesnt available. Select another one.");
							    logger.log(Level.SEVERE, "Extraction method \"" + extractionMethod + "\" doesnt available.");
							    throw (new Exception("Extraction method \"" + extractionMethod + "\" doesnt available."));
						  }

						  if (VCalendar.isVCalendar(message)) {
							    VCalendar vcal = new VCalendar();
							    vcal.parse(message);

							    if (vcal.getName() != null) event.addName(vcal.getName());
							    if (vcal.getPlace() != null) event.addPlace(vcal.getPlace());
							    if (vcal.getDescription() != null) event.setDescription(vcal.getDescription());
							    if (vcal.getDateFrom() != null) event.addDateFrom(vcal.getDateFrom());
							    if (vcal.getDateTo() != null) event.addDateTo(vcal.getDateTo());
						  } else {
							    event.parseMessage(message);
							    event.analyzeMessage();
						  }

						  // making JSON object
						  JSONObject JSONobj = new JSONObject();
						  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

						  JSONArray names = new JSONArray();
						  if (event.getNames().size() > 0) for (String name : event.getNames()) {
							    if (names.length() > 5) break;
							    boolean equal = false;
							    for (int i = 0; i < names.length(); i++)
								      if (names.getString(i).equalsIgnoreCase(name)) equal = true;
							    if (!equal) names.put(name);
						  }
						  else names.put("");

						  JSONArray places = new JSONArray();
						  if (event.getPlaces().size() > 0) for (String place : event.getPlaces()) {
							    if (places.length() > 5) break;
							    boolean equal = false;
							    for (int i = 0; i < places.length(); i++)
								      if (places.getString(i).equalsIgnoreCase(place)) equal = true;
							    if (!equal) places.put(place);
						  }

						  else places.put("");

						  JSONArray datesFrom = new JSONArray();
						  if (event.getDatesFrom().size() > 0) for (Calendar time : event.getDatesFrom()) {
							    if (datesFrom.length() > 5) break;
							    boolean equal = false;
							    for (int i = 0; i < datesFrom.length(); i++)
								      if (datesFrom.getString(i).equalsIgnoreCase(sdf.format(time.getTime()))) equal = true;
							    if (!equal) datesFrom.put(sdf.format(time.getTime()));
						  }
						  else datesFrom.put("");

						  JSONArray datesTo = new JSONArray();
						  if (event.getDatesTo().size() > 0) for (Calendar time : event.getDatesTo()) {
							    if (datesTo.length() > 5) break;
							    boolean equal = false;
							    for (int i = 0; i < datesTo.length(); i++)
								      if (datesTo.getString(i).equalsIgnoreCase(sdf.format(time.getTime()))) equal = true;
							    if (!equal) datesTo.put(sdf.format(time.getTime()));
						  }
						  else datesTo.put("");

						  sdf = new SimpleDateFormat("HH:mm");

						  JSONArray timesFrom = new JSONArray();
						  if (event.getDatesFrom().size() > 0) for (Calendar time : event.getDatesFrom()) {
							    if (timesFrom.length() > 5) break;
							    boolean equal = false;
							    for (int i = 0; i < timesFrom.length(); i++)
								      if (timesFrom.getString(i).equalsIgnoreCase(sdf.format(time.getTime()))) equal = true;
							    if (!equal) timesFrom.put(sdf.format(time.getTime()));
						  }

						  else timesFrom.put("");

						  JSONArray timesTo = new JSONArray();
						  if (event.getDatesTo().size() > 0) for (Calendar time : event.getDatesTo()) {
							    if (timesTo.length() > 5) break;
							    boolean equal = false;
							    for (int i = 0; i < timesTo.length(); i++)
								      if (timesTo.getString(i).equalsIgnoreCase(sdf.format(time.getTime()))) equal = true;
							    if (!equal) timesTo.put(sdf.format(time.getTime()));
						  }
						  else timesTo.put("");

						  String description;
						  if (event.getDescription() != null) description = event.getDescription();
						  else description = "";

						  try {
							    JSONobj.put("Description", description);
							    JSONobj.put("Name", names);
							    JSONobj.put("DateFrom", datesFrom);
							    JSONobj.put("DateTo", datesTo);
							    JSONobj.put("TimeFrom", timesFrom);
							    JSONobj.put("TimeTo", timesTo);
							    JSONobj.put("Place", places);

							    // send response
							    sendResponse(200, JSONobj.toString());
						  } catch (JSONException e) {
							    logger.log(Level.SEVERE, e.getMessage() + " :Problem with JSON creation.");
						  }

					}
					if (action.contains("SAVE")) {

						  logger.log(Level.INFO, incomingLine);
						  saveToDatabase();
					}
					if (action.contains("GET_METHODS")) {
						  logger.log(Level.INFO, incomingLine);
						  getImplementatios();
					}

			      }

			      if (httpMethod.equals("GET")) {
					logger.log(Level.INFO, "Client send GET request.");
					while (incoming.ready())
						  logger.log(Level.INFO, incoming.readLine());
					incoming.close();

					String responseString = "Error: GET request doesnt available.\r\n";
					sendResponse(404, responseString);

			      }


		    } catch (IOException e) {
			      logger.log(Level.SEVERE, e.getMessage() + " :Reading from plugin error.");
			      sendResponse(404, "problem with reading");
		    } catch (Exception e) {}

	  }





	  
	  @SuppressWarnings({ "rawtypes", "unchecked" })
          private void getImplementatios() {

		    final FileFilter filter = new FileFilter() {

			      public boolean accept(File pathname) {

					return pathname.getName().endsWith(".jar");
			      }
		    };
		    
		    File file = new File("extraction_methods");
		    List<File> jars = new ArrayList<File>();

		    for (File f : file.listFiles(filter)) 
			      jars.add(f);		    
		    
		    List<String> foundClasses = new ArrayList<>();
		    for (File f : jars) {
			      JarFile jar;
			      try {
					jar = new JarFile(f);
					for (Enumeration em1 = jar.entries(); em1.hasMoreElements();) {
						  String s = em1.nextElement().toString();
						  if (s.contains(".class")) {
							    s = s.replace("/", ".").replace(".class", "");
							    Class<Event> c2;
							    try {
								      c2 = (Class<Event>) Class.forName(s);
								      if (c2.getSuperclass() == Event.class) foundClasses.add(s);
							    } catch (ClassNotFoundException e) {
								      logger.log(Level.SEVERE, e.getMessage());
							    }
						  }
					}
			      } catch (IOException e) {
					logger.log(Level.SEVERE, e.getMessage());
			      }
		    }


		    JSONObject JSONobj = new JSONObject();

		    JSONArray implementatios = new JSONArray();

		    for (String s : foundClasses)
			      implementatios.put(s);

		    try {
			      JSONobj.put("Implementations", implementatios);
		    } catch (JSONException e) {
			      logger.log(Level.SEVERE, e.getMessage() + " :Problem with JSON creation.");
		    }
		    sendResponse(200, JSONobj.toString());


	  }





	  private void saveToDatabase() {

		    String message = null;
		    try {
			      message = incoming.readLine() + "\n";

			      while (incoming.ready()) {
					message = message.concat(incoming.readLine());
					message = message.concat("\n");
			      }

			      incoming.close();
			      logger.log(Level.INFO, message); // message


			      JSONObject obj = (JSONObject) new JSONTokener(message).nextValue();

			      String received_message = obj.get("messagepane").toString();
			      String received_version = obj.get("version").toString();
			      String received_method = obj.get("ExtractionMethod").toString();
			      String received_calendar = obj.get("CalendarName").toString();

			      JSONObject sended = (JSONObject) obj.get("sended");
			      String sended_name = sended.get("Name").toString();
			      String sended_place = sended.get("Place").toString();
			      String sended_dateFrom = sended.get("DateFrom").toString();
			      String sended_timeFrom = sended.get("TimeFrom").toString();
			      String sended_dateTo = sended.get("DateTo").toString();
			      String sended_timeTo = sended.get("TimeTo").toString();
			      String sended_description = sended.get("Description").toString();

			      JSONObject received = (JSONObject) obj.get("received");

			      JSONArray received_name = received.getJSONArray("Name");

			      List<String> received_names = new ArrayList<String>(5);
			      for (int i = 0; i < 5; i++)
					if (received_name.length() > i) received_names.add(received_name.getString(i));
					else received_names.add("");

			      JSONArray received_place = received.getJSONArray("Place");
			      List<String> received_places = new ArrayList<String>(5);
			      for (int i = 0; i < 5; i++)
					if (received_place.length() > i) received_places.add(received_place.getString(i));
					else received_places.add("");

			      JSONArray received_dateFrom = received.getJSONArray("DateFrom");
			      List<String> received_dateFroms = new ArrayList<String>(5);
			      for (int i = 0; i < 5; i++)
					if (received_dateFrom.length() > i) received_dateFroms.add(received_dateFrom.getString(i));
					else received_dateFroms.add("");

			      JSONArray received_timeFrom = received.getJSONArray("TimeFrom");
			      List<String> received_timeFroms = new ArrayList<String>(5);
			      for (int i = 0; i < 5; i++)
					if (received_timeFrom.length() > i) received_timeFroms.add(received_timeFrom.getString(i));
					else received_timeFroms.add("");

			      JSONArray received_dateTo = received.getJSONArray("DateTo");
			      List<String> received_dateTos = new ArrayList<String>(5);
			      for (int i = 0; i < 5; i++)
					if (received_dateTo.length() > i) received_dateTos.add(received_dateTo.getString(i));
					else received_dateTos.add("");

			      JSONArray received_timeTo = received.getJSONArray("TimeTo");
			      List<String> received_timeTos = new ArrayList<String>(5);
			      for (int i = 0; i < 5; i++)
					if (received_timeTo.length() > i) received_timeTos.add(received_timeTo.getString(i));
					else received_timeTos.add("");

			      String received_description = received.getString("Description");

			      String qry = "INSERT INTO dbo.EventExtractor (mail, version, extraction_method, calendar_name, sended_name, sended_place, sended_dateFrom, sended_timeFrom, sended_dateTo, sended_timeTo, sended_description, received_name_1, received_name_2, received_name_3, received_name_4, received_name_5, received_place_1, received_place_2, received_place_3, received_place_4, received_place_5, received_dateFrom_1, received_dateFrom_2, received_dateFrom_3, received_dateFrom_4, received_dateFrom_5, received_description, received_timeFrom_1, received_timeFrom_2, received_timeFrom_3, received_timeFrom_4, received_timeFrom_5, received_dateTo_1, received_dateTo_2, received_dateTo_3, received_dateTo_4, received_dateTo_5, received_timeTo_1, received_timeTo_2, received_timeTo_3, received_timeTo_4, received_timeTo_5) VALUES ('"
				                  + received_message
				                  + "','"
				                  + received_version
				                  + "','"
				                  + received_method
				                  + "','"
				                  + received_calendar
				                  + "','"
				                  + sended_name
				                  + "','"
				                  + sended_place
				                  + "','"
				                  + sended_dateFrom
				                  + "','"
				                  + sended_timeFrom
				                  + "','"
				                  + sended_dateTo
				                  + "','"
				                  + sended_timeTo
				                  + "','"
				                  + sended_description
				                  + "','"
				                  + received_names.get(0)
				                  + "','"
				                  + received_names.get(1)
				                  + "','"
				                  + received_names.get(2)
				                  + "','"
				                  + received_names.get(3)
				                  + "','"
				                  + received_names.get(4)
				                  + "','"
				                  + received_places.get(0)
				                  + "','"
				                  + received_places.get(1)
				                  + "','"
				                  + received_places.get(2)
				                  + "','"
				                  + received_places.get(3)
				                  + "','"
				                  + received_places.get(4)
				                  + "','"
				                  + received_dateFroms.get(0)
				                  + "','"
				                  + received_dateFroms.get(1)
				                  + "','"
				                  + received_dateFroms.get(2)
				                  + "','"
				                  + received_dateFroms.get(3)
				                  + "','"
				                  + received_dateFroms.get(4)
				                  + "','"
				                  + received_description
				                  + "','"
				                  + received_timeFroms.get(0)
				                  + "','"
				                  + received_timeFroms.get(1)
				                  + "','"
				                  + received_timeFroms.get(2)
				                  + "','"
				                  + received_timeFroms.get(3)
				                  + "','"
				                  + received_timeFroms.get(4)
				                  + "','"
				                  + received_dateTos.get(0)
				                  + "','"
				                  + received_dateTos.get(1)
				                  + "','"
				                  + received_dateTos.get(2)
				                  + "','"
				                  + received_dateTos.get(3)
				                  + "','"
				                  + received_dateTos.get(4)
				                  + "','"
				                  + received_timeTos.get(0)
				                  + "','"
				                  + received_timeTos.get(1)
				                  + "','"
				                  + received_timeTos.get(2) + "','" + received_timeTos.get(3) + "','" + received_timeTos.get(4) + "');";

			      try {
					Database.insert(qry);
					sendResponse(200, "Data saved into database.");
			      } catch (SQLException e) {
					logger.log(Level.WARNING, e.getMessage() + " :Database insertion problem.");
					sendResponse(404, "Database insertion problem.");
			      }


		    } catch (IOException e) {
			      logger.log(Level.SEVERE, e.getMessage() + " :Reading from plugin error.");
			      sendResponse(404, "problem with reading");
		    } catch (JSONException e) {
			      logger.log(Level.SEVERE, e.getMessage() + " :Parsing JSON error.");
			      sendResponse(404, "problem with parsing");
		    }

	  }





	  private void sendResponse(int status, String responseString) {

		    String statusLine = null;

		    if (status == 200) statusLine = "HTTP/1.1 200 OK" + "\r\n";
		    else
			      if (status == 400) statusLine = "HTTP/1.1 " + status + " Error" + "\r\n";
			      else statusLine = "HTTP/1.1 " + status + " Nespravny parameter" + "\r\n";

		    try {
			      outgoing.writeBytes(statusLine);


			      outgoing.writeBytes("Server: Java HTTPServer");
			      outgoing.writeBytes("Content-Type: application/json" + "\r\n");
			      outgoing.writeBytes("Content-Length: " + responseString.length() + "\r\n");
			      outgoing.writeBytes("Connection: close\r\n");

			      outgoing.writeBytes("\r\n");
			      outgoing.writeBytes(responseString);

			      outgoing.close();

			      logger.log(Level.INFO, "Server send response: Status " + status + ": \n" + responseString);
		    } catch (IOException e) {
			      logger.log(Level.SEVERE, e.getMessage() + ":Problem with sending to plugin.");
		    }


	  }
}
