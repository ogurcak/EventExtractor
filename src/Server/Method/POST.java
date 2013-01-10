
package Server.Method;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.lang.reflect.Constructor;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Extractor.Event;
import Extractor.ICalendar;
import Server.JSON;
import Server.Response;
import Server.Method.POSTActions.GetMethods;
import Server.Method.POSTActions.Save;






public class POST
{

	private static Logger logger = Logger.getLogger(POST.class.getName());

	private String currentVersion = "2.3";






	@SuppressWarnings("unused")
	public POST(DataOutputStream outgoing, BufferedReader incoming)
	{

		try {

			logger.info("Client send POST request:");

			String extractionMethod = null;
			String action = null;

			String line = incoming.readLine();
			String incomingLine = line;
			while (line.length() > 0) {
				if (line.contains("Authorization: ")) {
					StringTokenizer tokenizer = new StringTokenizer(line);
					String httpMethod = tokenizer.nextToken();
					String httpQueryString = tokenizer.nextToken();
					httpQueryString = tokenizer.nextToken();
					if (!httpQueryString.equals(currentVersion)) {
						logger.info(incomingLine);
						new Response(outgoing).sendResponse(400, "Error: New version available - http://events.email.ui.sav.sk \r\n");
						throw (new Exception("Plugin has old version."));
					}
				}
				
				if (line.contains("ExtractionMethod: ")) {
					StringTokenizer tokenizer = new StringTokenizer(line);
					String httpMethod = tokenizer.nextToken();
					extractionMethod = tokenizer.nextToken();
				}
				
				if (line.contains("Action: ")) {
					StringTokenizer tokenizer = new StringTokenizer(line);
					String httpMethod = tokenizer.nextToken();
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
				logger.info(incomingLine);

				// Analyzation Part

				Event event = null;

				if (extractionMethod == null)
					extractionMethod = "Extractor.EventRegex";

				try {

					@SuppressWarnings("unchecked")
					Class<Event> c2 = (Class<Event>) Class.forName(extractionMethod);

					if (c2.getSuperclass() != Event.class) {
						new Response(outgoing).sendResponse(404, "Error: Ilegal extraction method \"" + extractionMethod + "\". Select another one.");
						logger.error("Ilegal Extraction method \"" + extractionMethod + "\".");
						throw (new Exception("Ilegal Extraction method \"" + extractionMethod + "\"."));
					}
					Constructor<Event> ctor = c2.getConstructor();
					event = ctor.newInstance();
				} catch (ClassNotFoundException e) {
					new Response(outgoing).sendResponse(404, "Error: Extraction method \"" + extractionMethod + "\" doesnt available. Select another one.");
					logger.error("Extraction method \"" + extractionMethod + "\" doesnt available.");
					throw (new Exception("Extraction method \"" + extractionMethod + "\" doesnt available."));
				}

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

				// making JSON object
				JSONObject JSONobj = new JSONObject();


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


				try {
					JSONobj.put("Description", description);
					JSONobj.put("Name", names);
					JSONobj.put("DateFrom", datesFrom);
					JSONobj.put("DateTo", datesTo);
					JSONobj.put("TimeFrom", timesFrom);
					JSONobj.put("TimeTo", timesTo);
					JSONobj.put("Place", places);

					// send response
					new Response(outgoing).sendResponse(200, JSONobj.toString());
				} catch (JSONException e) {
					logger.fatal(e.getMessage() + " :Problem with JSON creation.");
				}

			}
			if (action.contains("SAVE")) {
				logger.info(incomingLine);
				logger.info("Saving to database started.");
				new Save(outgoing, incoming).saveToDatabase();
			}

			if (action.contains("GET_METHODS")) {
				logger.info(incomingLine);
				new GetMethods(outgoing, incoming).getMethods();
			}

		} catch (Exception e) {
			
		}

	}
}
