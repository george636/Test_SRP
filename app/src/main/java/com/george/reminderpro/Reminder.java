package com.george.reminderpro;

import android.content.Context;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Reminder {
	
	private int id;
	private int alarmId;
	private String description;
	private String time;
	private String date;
	private long timestamp;
	private int status;
	private int recurrencePosition;
	private long originalTimestamp;
	
	//general constructor
	public Reminder() {
		
	}
	
	//constructor
	public Reminder(int id, int alarmId, String description, String time, String date, long timestamp, int status, int recurrencePosition, long originalTimestamp) {
		this.id 				= id;
		this.alarmId			= alarmId;
		this.description 		= description;
		this.time 				= time;
		this.date 				= date;
		this.timestamp			= timestamp;
		this.status 			= status;
		this.recurrencePosition	= recurrencePosition;
		this.originalTimestamp	= originalTimestamp;
	}
	
	//constructor with no id
	public Reminder(int alarmId, String description, String time, String date, long timestamp, int status, int recurrencePosition, long originalTimestamp) {
		this.alarmId			= alarmId;
		this.description 		= description;
		this.time 				= time;
		this.date 				= date;
		this.timestamp			= timestamp;
		this.status 			= status;
		this.recurrencePosition	= recurrencePosition;
		this.originalTimestamp	= originalTimestamp;
	}
	
	//getters
	public int getId() {
		return this.id;
	}
	
	public int getAlarmId() {
		return this.alarmId;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public String getTime() {
		return this.time;
	}
	
	public String getDate() {
		return this.date;
	}
	
	public long getTimestamp() {
		return this.timestamp;
	}
	
	public int getStatus() {
		return this.status;
	}
	
	public String getStatusString() {
		String status = (this.status == 0) ? "Expired" : "Active";
		return status;
	}
	
	public int getRecurrencePosition() {
		return this.recurrencePosition;
	}
	
	public long getOriginalTimestamp() {
		return this.originalTimestamp;
	}
	
	//setters
	public void setId(int id) {
		this.id = id;
	}
	
	public void setAlarmId(int alarmId) {
		this.alarmId = alarmId;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	/*
	public void setTime(String time) {
		this.time = time;
	}
	*/
	
	public void setTime(long timestamp, Context context) {
		Date d = new Date();
		Boolean isTime24Hours = DateFormat.is24HourFormat(context);
		SimpleDateFormat timeFormatter;
		
		if (isTime24Hours) {
			timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault()); 	//23:24
		}
		else {
			timeFormatter = new SimpleDateFormat("h:mm a", Locale.getDefault()); 	//11:23 AM
		}
				
		d.setTime(timestamp);
		
		String timeString = timeFormatter.format(d);
		this.time = timeString;
	}
	
	/*
	public void setDate(String date) {
		if (this.recurrencePosition == 0) {
			this.date = date;
		}
		else {
			//daily
			if (this.recurrencePosition == 1) {
				this.date = "DAILY";
			}
			//weekly
			else if (this.recurrencePosition == 2) {
				this.date = "WEEKLY ("+date+")";
			}
			//monthly
			else if (this.recurrencePosition == 3) {
				this.date = "MOTHLY ("+date+")";
			}
			//annually
			else if (this.recurrencePosition == 4) {
				this.date = "ANNUALLY ("+date+")";
			}
		}
	}
	*/
	
	public void setDate(long timestamp, int recurrencePos, Context context) {
		Calendar c = Calendar.getInstance();
		Date d = new Date();
		SimpleDateFormat weekdayFormatter = new SimpleDateFormat("EEEE", Locale.getDefault()); 	//Monday
		SimpleDateFormat monthFormatter = new SimpleDateFormat("LLLL", Locale.getDefault());	//January
		java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
		
		c.setTimeInMillis(timestamp);
		d.setTime(timestamp);
			
		int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
				
		String dayOfTheWeek = weekdayFormatter.format(d);
		String monthName = monthFormatter.format(d);
		String dateString = dateFormat.format(d);
		
		if (recurrencePos == 0) {
			//this.date = date;
			this.date = dateString;
		}
		else {
			//daily
			if (recurrencePos == 1) {
				this.date = "Everyday";
			}
			//weekly
			else if (recurrencePos == 2) {
				this.date = "Every " + dayOfTheWeek;
			}
			//monthly
			else if (recurrencePos == 3) {
				String trailer = "th";
				if (dayOfMonth == 1 || dayOfMonth == 21 || dayOfMonth == 31) {
					trailer = "st";
				}
				else if (dayOfMonth == 2 || dayOfMonth == 22) {
					trailer = "nd";
				}
				this.date = "Every " + dayOfMonth + trailer;
			}
			//annually
			else if (recurrencePos == 4) {
				String trailer = "th";
				if (dayOfMonth == 1 || dayOfMonth == 21 || dayOfMonth == 31) {
					trailer = "st";
				}
				else if (dayOfMonth == 2 || dayOfMonth == 22) {
					trailer = "nd";
				}
				this.date = "Every " + monthName + " " + dayOfMonth + trailer;
			}
            else if (recurrencePos == 5) {
                this.date = "Weekdays";
            }
            else if (recurrencePos == 6) {
                this.date = "Weekends";
            }
		}
	}	
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public void setRecurrencePosition(int recurrencePosition) {
		this.recurrencePosition = recurrencePosition;
	}
	
	public void setOriginalTimestamp(long originalTimestamp) {
		this.originalTimestamp = originalTimestamp;
	}

	@Override
	public String toString() {
		return this.description + "\n" + this.date + " at " + this.time;
	}
	
	public String getAllData() {
		return this.id + "|" + this.alarmId + "|" +  this.description + "|" + this.date + "|" + this.time + "|" + this.timestamp + "|" + this.getStatusString();
	}
	
	
}
