package TestClaim;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClaimInfo implements Serializable {
	private String clm_id;
	private int line_num;
	private String pvd_id;
	private String mbs_id;
	private int rel_psn_id;
	private int rel_emp_id;
	private int primacy;
	private int proc_medicare_covered;
	private String pvd_Zipcode;
	private Date line_date;
	private Date current_date;
	private Date Previous_Date;
	private String part_type;
	private String mbs_type;
	private String[] event_names;
	private Date[] event_dates;
	private List<EventInfo> eventinfo;
		private int emp_size;
	private boolean recoverable;
	private boolean primary_member;

	public ClaimInfo(String _clm_id, int _line_num, int _primacy, int _proc_medicare_covered, String _pvd_Zipcode,
			String _part_type, String[] _event_name, String _mbs_type, int _emp_size, String _line_date,
			String[] _event_dates, Date _current_date, Date _Previous_Date, boolean _primary_member) throws Exception {
		clm_id = _clm_id;
		line_num = _line_num;
		primacy = _primacy;
		proc_medicare_covered = _proc_medicare_covered;
		pvd_Zipcode = _pvd_Zipcode;

		part_type = _part_type;
		event_names = _event_name;
		mbs_type = _mbs_type;
		emp_size = _emp_size;
		line_date = new SimpleDateFormat("dd/MM/yyyy").parse(_line_date);
		event_dates = new Date[_event_dates.length];
		for (int i = 0; i < _event_dates.length; i++) {
			event_dates[i] = new SimpleDateFormat("dd/MM/yyyy").parse(_event_dates[i]);
		
		}
			 
		eventinfo = new ArrayList<EventInfo>();
	
		for (int j = 0; j < event_names.length; j++) {
				eventinfo.add(new EventInfo(event_names[j], event_dates[2 * j], event_dates[(2 * j) + 1]));
				
			}
		
		current_date = _current_date;
		Previous_Date = _Previous_Date;
		primary_member = _primary_member;

	}

	public  boolean completeEventInfo(List<EventInfo> s, String a, Date linedate){
		for(int i=0;i<s.size();i++){
			if((s.get(i).getEvent_name().equals(a)) && linedate.before(s.get(i).getEvent_end_date()) && linedate.after(s.get(i).getEvent_start_Date())){
				return true;
			}
		}
		return false;
		
	}
	
	public List<EventInfo> getEventinfo() {
		return eventinfo;
	}

	public void setEventinfo(List<EventInfo> eventinfo) {
		this.eventinfo = eventinfo;
	}
	

	public String getClm_id() {
		return clm_id;
	}

	public int getLine_num() {
		return line_num;
	}

	public String[] getEvent_names() {
		return event_names;
	}

	public void setEvent_names(String[] event_names) {
		this.event_names = event_names;
	}

	public Date[] getEvent_dates() {
		return event_dates;
	}

	public void setEvent_dates(Date[] event_dates) {
		this.event_dates = event_dates;
	}

	public Date getPrevious_Date() {
		return Previous_Date;
	}

	public String getPvd_id() {
		return pvd_id;
	}

	public String getMbs_id() {
		return mbs_id;
	}

	public int getRel_psn_id() {
		return rel_psn_id;
	}

	public int getRel_emp_id() {
		return rel_emp_id;
	}

	public String getPart_type() {
		return part_type;
	}

	public int getEmp_size() {
		return emp_size;
	}

	public int getPrimacy() {
		return primacy;
	}

	public void setClm_id(String clm_id) {
		this.clm_id = clm_id;
	}

	public void setLine_num(int line_num) {
		this.line_num = line_num;
	}

	public void setPvd_id(String pvd_id) {
		this.pvd_id = pvd_id;
	}

	public void setMbs_id(String mbs_id) {
		this.mbs_id = mbs_id;
	}

	public void setRel_psn_id(int rel_psn_id) {
		this.rel_psn_id = rel_psn_id;
	}

	public void setRel_emp_id(int rel_emp_id) {
		this.rel_emp_id = rel_emp_id;
	}

	public void setPrimacy(int primacy) {
		this.primacy = primacy;
	}

	public void setProc_medicare_covered(int proc_medicare_covered) {
		this.proc_medicare_covered = proc_medicare_covered;
	}

	public void setPvd_Zipcode(String pvd_Zipcode) {
		this.pvd_Zipcode = pvd_Zipcode;
	}

	public void setLine_date(Date line_date) {
		this.line_date = line_date;
	}

	public void setCurrent_date(Date current_date) {
		this.current_date = current_date;
	}

	public void setPrevious_Date(Date previous_Date) {
		Previous_Date = previous_Date;
	}

	public void setPart_type(String part_type) {
		this.part_type = part_type;
	}

	public void setMbs_type(String mbs_type) {
		this.mbs_type = mbs_type;
	}

	public void setEmp_size(int emp_size) {
		this.emp_size = emp_size;
	}

	public int getProc_medicare_covered() {
		return proc_medicare_covered;
	}

	public String getPvd_Zipcode() {
		return pvd_Zipcode;
	}

	public Date getLine_date() {
		return line_date;
	}

	public String getMbs_type() {
		return mbs_type;
	}

	public Date getCurrent_date() {
		return current_date;
	}

	public boolean isRecoverable() {
		return recoverable;
	}

	public void setRecoverable(boolean recoverable) {
		this.recoverable = recoverable;
	}

	public boolean isPrimary_member() {
		return primary_member;
	}

	public void setPrimary_member(boolean primary_member) {
		this.primary_member = primary_member;
	}


}
