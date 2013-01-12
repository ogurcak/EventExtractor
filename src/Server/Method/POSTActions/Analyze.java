
package Server.Method.POSTActions;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Extractor.Event;
import Extractor.ICalendar;
import Server.JSON;
import Server.Response;






public class Analyze
{

	private DataOutputStream outgoing;

	private BufferedReader incoming;

	private static Logger logger = Logger.getLogger(Analyze.class.getName());

	private Event event = null;

	private JSONObject JSONobj = null;

	private long id = 0;






	public Analyze(DataOutputStream outgoing, BufferedReader incoming)
	{

		this.outgoing = outgoing;
		this.incoming = incoming;
	}






	@SuppressWarnings("unchecked")
	public void prepareExtractionMethod(String extractionMethod) throws Exception {

		try {
			if (extractionMethod == null)
				extractionMethod = "ogurcak.fiit.SK_extractor";

			Class<Event> c2 = (Class<Event>) Class.forName(extractionMethod);

			if (c2.getSuperclass() != Event.class) {
				new Response(outgoing).sendResponse(404, "Error: Ilegal extraction method \"" + extractionMethod + "\". Select another one.");
				logger.error("Ilegal Extraction method \"" + extractionMethod + "\".");
				c2 = (Class<Event>) Class.forName("ogurcak.fiit.SK_extractor");
			}

			Constructor<Event> ctor = c2.getConstructor();
			event = ctor.newInstance();

		} catch (ClassNotFoundException e) {
			new Response(outgoing).sendResponse(404, "Error: Extraction method \"" + extractionMethod + "\" doesnt available. Select another one.");
			logger.error("Extraction method \"" + extractionMethod + "\" doesnt available.");
			throw (new Exception("Extraction method \"" + extractionMethod + "\" doesnt available."));
		}
	}






	public void analyzeMessage() throws Exception {

		if (event == null)
			throw (new Exception("Call prepareExtractionMethod() first !!!"));

		try {
			// read text of message
			String message = "";
			String line = "";
			while (incoming.ready()) {
				line = incoming.readLine();
				message = message.concat(line);
				message = message.concat("\n");
			}


			// check if message has iCalendar standard
			// and if not
			// analyze message with selected extraction method
			if (ICalendar.isICalendar(message)) {
				ICalendar ical = new ICalendar();
				ical.parse(message);

				if (ical.getName() != null)
					event.addName(ical.getName());
				if (ical.getPlace() != null)
					event.addPlace(ical.getPlace());
				if (ical.getDescription() != null)
					event.setDescription(ical.getDescription());
				if (ical.getDateFrom() != null)
					event.addDateFrom(ical.getDateFrom());
				if (ical.getDateTo() != null)
					event.addDateTo(ical.getDateTo());
			} else {
				event.parseMessage(message);
				event.analyzeMessage();
			}

		} catch (IOException e) {
			logger.fatal("Error while reading from stream: " + e);
		} catch (MessagingException e) {
			logger.fatal("Error while parssing message: " + e);
		}

	}






	public void makeJsonObject() throws Exception {

		if (event == null)
			throw (new Exception("Call prepareExtractionMethod() first and then analyzeMessage() !!!"));
		
		if (id == 0)
			throw (new Exception("Call saveDataToDatabase() first !!!"));

		try {
			JSONobj = new JSONObject();

			JSON json = new JSON();
			JSONArray names = json.getNamesInJSON(event.getNames());
			JSONArray places = json.getPlacesInJSON(event.getPlaces());
			JSONArray datesFrom = json.getDatesFromInJSON(event.getDatesFrom());
			JSONArray datesTo = json.getDatesToInJSON(event.getDatesTo());
			JSONArray timesFrom = json.getTimesFromInJSON(event.getDatesFrom());
			JSONArray timesTo = json.getTimesToInJSON(event.getDatesTo());


			String description;
			if (event.getDescription() != null)
				description = event.getDescription();
			else
				description = "";



			JSONobj.put("MessageID", id);
			JSONobj.put("Description", description);
			JSONobj.put("Name", names);
			JSONobj.put("DateFrom", datesFrom);
			JSONobj.put("DateTo", datesTo);
			JSONobj.put("TimeFrom", timesFrom);
			JSONobj.put("TimeTo", timesTo);
			JSONobj.put("Place", places);

		} catch (JSONException e) {
			logger.fatal(e.getMessage() + " :Problem with JSON creation.");
		}
	}






	public void sendJsonObject() throws Exception {

		if (JSONobj == null)
			throw (new Exception("Call makeJsonObject() first !!!"));

		new Response(outgoing).sendResponse(200, JSONobj.toString());
	}






	public void saveDataToDatabase() throws Exception {

		if (event == null)
			throw (new Exception("Call prepareExtractionMethod() first and then analyzeMessage() !!!"));
		
		id = 0;
		// TODO save data from event
		// TODO save message id to variable "id"	
	}
}
