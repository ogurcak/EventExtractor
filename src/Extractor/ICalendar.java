package Extractor;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class ICalendar {

	  protected String name = null;
	  protected String place = null;
	  protected String description = null;
	  protected Calendar dateFrom = null;
	  protected Calendar dateTo = null;

	  private static Logger logger = Logger.getLogger(ICalendar.class.getName());





	  public static boolean isICalendar(String message) {

		    if (message.contains("BEGIN:VCALENDAR")) if (message.contains("BEGIN:VEVENT")) if (message.contains("END:VCALENDAR")) if (message.contains("END:VEVENT")) return true;
		    return false;
	  }





	  public void parse(String message) {

		    Pattern namePatern = Pattern.compile("^SUMMARY:.*?$", Pattern.MULTILINE);
		    Pattern placePatern = Pattern.compile("^LOCATION:.*?$", Pattern.MULTILINE);
		    Pattern descriptionPatern = Pattern.compile("^DESCRIPTION:.+?[A-Z]+:", Pattern.MULTILINE | Pattern.DOTALL);
		    Pattern dateFromPatern = Pattern.compile("^DTSTART:.*?$", Pattern.MULTILINE);
		    Pattern dateToPatern = Pattern.compile("^DTEND:.*?$", Pattern.MULTILINE);

		    Matcher matcher = namePatern.matcher(message.replace("\\n", " "));
		    if (matcher.find()) name = matcher.group().replace("SUMMARY:", "").replace("\\", "");

		    matcher = placePatern.matcher(message.replace("\\n", " "));
		    if (matcher.find()) place = matcher.group().replace("LOCATION:", "").replace("\\", "");

		    matcher = descriptionPatern.matcher(message.replace("\\n", " "));
		    if (matcher.find()) {
			      description = matcher.group().replace("DESCRIPTION:", "").replace("\\", "");
			      description = description.replace(description.substring(description.lastIndexOf("\n")), "").replace("\n", "");
		    }

		    matcher = dateFromPatern.matcher(message.replace("\\n", " "));
		    if (matcher.find()) {
			      DateFormat formatter;
			      Date date = null;
			      formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			      try {
					date = (Date) formatter.parse(matcher.group().replace("DTSTART:", "").replace("T", "").replace("Z", ""));
			      } catch (ParseException e) {
					logger.log(Level.SEVERE, e.getMessage());
			      }
			      dateFrom = GregorianCalendar.getInstance();
			      dateFrom.setTime(date);
			      dateFrom.add(Calendar.HOUR, 1);
		    }


		    matcher = dateToPatern.matcher(message.replace("\\n", " "));
		    if (matcher.find()) {
			      DateFormat formatter;
			      Date date = null;
			      formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			      try {
					date = (Date) formatter.parse(matcher.group().replace("DTEND:", "").replace("T", "").replace("Z", ""));
			      } catch (ParseException e) {
					logger.log(Level.SEVERE, e.getMessage());
			      }
			      dateTo = GregorianCalendar.getInstance();
			      dateTo.setTime(date);
			      dateTo.add(Calendar.HOUR, 1);
		    }

	  }





	  public String getName() {

		    return this.name;
	  }





	  public Calendar getDateFrom() {

		    return this.dateFrom;
	  }





	  public Calendar getDateTo() {

		    return this.dateTo;
	  }





	  public String getPlace() {

		    return this.place;
	  }





	  public String getDescription() {

		    return this.description;
	  }

}
