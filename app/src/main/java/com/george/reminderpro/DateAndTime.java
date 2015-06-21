package com.george.reminderpro;

import android.content.Context;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateAndTime {
	//variables for current time
	private int hourInt;
	private int minuteInt;
	private int monthInt;
	private int dayInt;
	private int yearInt;
	private long timestamp;
	private boolean isTime24Hours;
	Date date;
	java.text.DateFormat dateFormat;
	
	//calendar - Constructs a new instance of the Calendar subclass appropriate for the default Locale. 
	private Calendar calendar = Calendar.getInstance();
	
	//default constructor - sets time to current time - requires user to pass application context
	public DateAndTime(Context applicationContext) {
		this.hourInt = this.minuteInt = this.monthInt = this.dayInt = this.yearInt = -1;
		this.timestamp = -1;	
		this.isTime24Hours = DateFormat.is24HourFormat(applicationContext);
		this.dateFormat = android.text.format.DateFormat.getDateFormat(applicationContext);
	}
	
	//getters
	public long getTimestamp() {
		return timestamp;
	}

	public int getHourInt() {
		return hourInt;
	}

	public int getMinuteInt() {
		return minuteInt;
	}

	public int getMonthInt() {
		return monthInt;
	}
	public void setHourInt(int hourInt) {
		this.hourInt = hourInt;
	}

	public int getDayInt() {
		return dayInt;
	}
	
	public int getYearInt() {
		return yearInt;
	}
	
	public boolean isTime24Hours() {
		if (this.isTime24Hours) {
			return true;
		}
		else {
			return false;
		}
	}
	
	
	//setters
	public void setMinuteInt(int minuteInt) {
		this.minuteInt = minuteInt;
	}

	public void setMonthInt(int monthInt) {
		this.monthInt = monthInt;
	}

	public void setDayInt(int dayInt) {
		this.dayInt = dayInt;
	}

	public void setYearInt(int yearInt) {
		this.yearInt = yearInt;
	}

	public void setTimestamp() {
		this.timestamp = this.convertDateTimeToMillis(this.yearInt, this.monthInt, this.dayInt, this.hourInt, this.minuteInt);
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setTimestamp(DateAndTime dateAndTime) {
		this.timestamp = this.convertDateTimeToMillis(dateAndTime.getYearInt(), dateAndTime.getMonthInt(), dateAndTime.getDayInt(), dateAndTime.getHourInt(), dateAndTime.getMinuteInt());
	}
	
	public void setTimestamp(int year, int month, int day) {
		this.timestamp = this.convertDateToMillis(year, month, day);
	}	
	
	public void setTimestamp(int year, int month, int day, int hour, int minute) {
		this.timestamp = this.convertDateTimeToMillis(year, month, day, hour, minute);
	}
	
	public void setToNow() {
		calendar = Calendar.getInstance(); //get newest time and date
		this.hourInt = calendar.get(Calendar.HOUR_OF_DAY);
		this.minuteInt = calendar.get(Calendar.MINUTE);
		this.monthInt = calendar.get(Calendar.MONTH);
		this.dayInt = calendar.get(Calendar.DAY_OF_MONTH);
		this.yearInt = calendar.get(Calendar.YEAR);
		this.timestamp = calendar.getTimeInMillis();	
	}

	public long convertDateTimeToMillis(int year, int month, int day, int hour, int minute) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, day, hour, minute, 0);
		return c.getTimeInMillis();
	}
	
	public long convertDateToMillis(int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, day);
		return c.getTimeInMillis();
	}
	
	public void resetDateAndTime() {
		this.hourInt = this.minuteInt = this.monthInt = this.dayInt = this.yearInt = -1;
		this.timestamp = -1;	
	}
	
	//THIS FUNCTIONS has been upgraded to better represent localized time
	public String getTimeString() {
		Date d = new Date();
		SimpleDateFormat timeFormatter;
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timestamp);
		
		if (this.isTime24Hours) {
			timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault()); 	//23:24
		}
		else {
			timeFormatter = new SimpleDateFormat("h:mm a", Locale.getDefault()); 	//11:23 AM
		}
		
		c.set(Calendar.HOUR_OF_DAY, this.hourInt);
		c.set(Calendar.MINUTE, this.minuteInt);
		
		long newTimestamp = c.getTimeInMillis();
		
		d.setTime(newTimestamp);
		
		String timeString = timeFormatter.format(d);
		return timeString;
	}
	
	public String getDateString() {	
		this.setTimestamp(this.yearInt, this.monthInt, this.dayInt);
		this.date = new Date(this.timestamp);
		
		String dateString = dateFormat.format(this.date);
	
		return dateString;
	}
	
	public boolean isDateSet() {
		if (this.yearInt >= 0 && this.monthInt >= 0 && this.dayInt >= 0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean isTimeSet() {
		if (this.hourInt >= 0 && this.minuteInt >= 0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean isDateAndTimeSet() {
		if (this.isDateSet() && this.isTimeSet()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public String timestampToDay(long timestamp) {
		String day;
		Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        day = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
		        
		return day;
	}
	
}
