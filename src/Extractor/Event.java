package Extractor;


import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;




abstract public class Event {


	  protected List<String> names = new ArrayList<String>();
	  protected List<String> places = new ArrayList<String>();
	  protected String description = null;
	  protected List<Calendar> datesFrom = new ArrayList<Calendar>();
	  protected List<Calendar> datesTo = new ArrayList<Calendar>();

	  protected String message = null;





	  public Event() {

	  }





	  public void setMessage(String message) {

		    this.message = message;
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
