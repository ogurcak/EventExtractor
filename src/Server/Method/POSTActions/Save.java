
package Server.Method.POSTActions;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import Server.Database;
import Server.Response;






public class Save
{

	private static Logger logger = Logger.getLogger(Save.class.getName());

	private DataOutputStream outgoing;

	private BufferedReader incoming;






	public Save(DataOutputStream outgoing, BufferedReader incoming)
	{

		this.outgoing = outgoing;
		this.incoming = incoming;
	}






	public void saveToDatabase() {
		

		try {
			
			String message = incoming.readLine();
			
			logger.info(message); // message

			JSONObject obj = (JSONObject) new JSONTokener(message).nextValue();			

			Integer received_id = obj.getInt("id");
			String sended_name = obj.getString("Name");
			String sended_place = obj.getString("Place");
			String sended_dateFrom = obj.getString("DateFrom");
			String sended_timeFrom = obj.getString("TimeFrom");
			String sended_dateTo = obj.getString("DateTo");
			String sended_timeTo = obj.getString("TimeTo");
			Boolean sended_allDay = obj.getBoolean("AllDay");
			String sended_description = obj.getString("Description");

			

			PreparedStatement preparedStmt = Database
					.getConnection()
					.prepareStatement(
							"INSERT INTO userEdited (id, name, place, dateFrom, timeFrom, dateTo, timeTo, allDay, description) VALUES (?,?,?,?,?,?,?,?,?)");
			preparedStmt.setInt(1, received_id);
			preparedStmt.setString(2, sended_name);
			preparedStmt.setString(3, sended_place);
			preparedStmt.setString(4, sended_dateFrom);
			preparedStmt.setString(5, sended_timeFrom);
			preparedStmt.setString(6, sended_dateTo);
			preparedStmt.setString(7, sended_timeTo);
			preparedStmt.setBoolean(8, sended_allDay);
			preparedStmt.setString(9, sended_description);
			

			Database.insert(preparedStmt);

			new Response(outgoing).sendResponse(200, "Data saved into database.");
		} catch (SQLException e) {
			logger.error(e.getMessage() + " :Database insertion problem.");
			new Response(outgoing).sendResponse(404, "Database insertion problem.");
		} catch (JSONException e) {
			logger.fatal(e.getMessage() + " :Parsing JSON error.");
			new Response(outgoing).sendResponse(404, "Problem with parsing");
		} catch (IOException e) {
			logger.fatal(e.getMessage() + " :Reading JSON error.");
			new Response(outgoing).sendResponse(404, "Problem with reading");
		}

	}
}
