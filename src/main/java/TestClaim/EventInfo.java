package TestClaim;

import java.io.Serializable;
import java.util.Date;

public class EventInfo implements Serializable{
	private String event_name;
	private Date event_start_Date;
	private Date event_end_date;
	public EventInfo(String _event_name, Date _event_start_date, Date _event_end_date){
		this.event_name = _event_name;
		this.event_start_Date = _event_start_date;
		this.event_end_date = _event_end_date;
		
		
	}
	public String getEvent_name() {
		return event_name;
	}
	public void setEvent_name(String event_name) {
		this.event_name = event_name;
	}
	public Date getEvent_start_Date() {
		return event_start_Date;
	}
	public void setEvent_start_Date(Date event_start_Date) {
		this.event_start_Date = event_start_Date;
	}
	public Date getEvent_end_date() {
		return event_end_date;
	}
	public void setEvent_end_date(Date event_end_date) {
		this.event_end_date = event_end_date;
	}
	
	
	}


