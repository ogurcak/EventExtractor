package Extractor;


import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;





abstract public class Event {


	  protected List<String> names = new ArrayList<String>();
	  protected List<String> places = new ArrayList<String>();
	  protected String description = null;
	  protected List<Calendar> datesFrom = new ArrayList<Calendar>();
	  protected List<Calendar> datesTo = new ArrayList<Calendar>();

	  protected MimeMessage message = null;
	  
	  private static Logger logger = Logger.getLogger(Event.class.getName());





	  public Event() {

	  }





	  public Event(String message) throws AddressException, MessagingException {

		    parseMessage(message);

	  }





	  public Event(MimeMessage message) {

		    this.message = message;
	  }





	  public void setMessage(MimeMessage message) {

		    this.message = message;
	  }





	  public void parseMessage(String message) throws AddressException, MessagingException {

		    String fromAddress = null;
		    String[] toAddresses = null;
		    String subject = null;
		    String content = null;
		    Date date = null;

		    Pattern fromPattern = Pattern.compile("^From: .*?$", Pattern.MULTILINE);
		    Pattern toPattern = Pattern.compile("^To: .*?$", Pattern.MULTILINE);
		    Pattern datePattern = Pattern.compile("^Date: .*?$", Pattern.MULTILINE);
		    Pattern subjectPattern = Pattern.compile("^Subject:.*?$", Pattern.MULTILINE);
		    Pattern contentPattern = Pattern.compile("Content: (.*\\s)*", Pattern.MULTILINE);

		    Matcher matcher;
		    matcher = fromPattern.matcher(message);
		    if (matcher.find()) {
			      fromAddress = matcher.group().replace("From: ", "");
			      String[] splited = fromAddress.split(" ");
			      for(String s:splited) if(s.contains("<") && s.contains(">") && s.contains("@") ) fromAddress = s.replace("<", "").replace(">", "");
		    }

		    matcher = toPattern.matcher(message);
		    if (matcher.find()) {
			      String[] splited = matcher.group().replace("To: ", "").split(" ");
			      List<String> list = new ArrayList<String>();
			      for(String s:splited) if(s.contains("<") && s.contains(">") || s.contains("@") )
					list.add(s.replace("<", "").replace(">", ""));
			      toAddresses = new String[list.size()];
			      list.toArray(toAddresses);
			      }
		    

		    matcher = datePattern.matcher(message);
		    if (matcher.find()) date = new Date(Long.valueOf(matcher.group().replace("Date: ", "") + "000"));

		    matcher = subjectPattern.matcher(message);
		    if (matcher.find()) subject = matcher.group().replace("Subject: ", "");

		    matcher = contentPattern.matcher(message);
		    if (matcher.find()) content = matcher.group().replace("Content: ", "");


		    Session sn = Session.getInstance(System.getProperties());
		    this.message = new MimeMessage(sn);
		    this.message.setFrom(new InternetAddress(fromAddress));
		    InternetAddress[] toIntAdds = new InternetAddress[toAddresses.length];
		    for (int i = 0; i < toAddresses.length; i++) {
			      toIntAdds[i] = new InternetAddress(toAddresses[i]);
		    }

		    this.message.setRecipients(Message.RecipientType.TO, toIntAdds);
		    this.message.setSubject(subject);
		    this.message.setSentDate(date);
		    this.message.setText(content);
	  }





	  abstract public void analyzeMessage();





	  public List<String> getNames() {

		    return this.names;
	  }





	  public List<Calendar> getDatesFrom() {

		    return this.datesFrom;
	  }





	  public List<Calendar> getDatesTo() {

		    return this.datesTo;
	  }





	  public List<String> getPlaces() {

		    return this.places;
	  }





	  public String getDescription() {

		    return this.description;
	  }





	  public void addName(String name) {

		    this.names.add(name);
	  }





	  public void addPlace(String place) {

		    this.places.add(place);
	  }





	  public void setDescription(String description) {

		    this.description = description;
	  }





	  public void addDateFrom(Calendar dateFrom) {

		    this.datesFrom.add(dateFrom);
	  }





	  public void addDateTo(Calendar dateTo) {

		    this.datesTo.add(dateTo);
	  }

}
