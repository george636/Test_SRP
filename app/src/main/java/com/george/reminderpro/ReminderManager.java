package com.george.reminderpro;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

public class ReminderManager {
	private Context context;
	private DatabaseHandler dbh;
	private AlarmManager alarmManager;
	

	/*
	 * [][][][][][][][][]
	 * CONSTRUCTOR
	 * [][][][][][][][][]
	 * 
	 */
	public ReminderManager(Context context) {
		this.context = context;
		this.dbh = DatabaseHandler.getInstance(context);
		this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}
	

	/*
	 * [][][][][][][][][]
	 * PRIVATE FUNCTIONS
	 * [][][][][][][][][]
	 * 
	 */
	private void dismissPopupDialog(int reminderId) {
        try {
            //check if user is even using popups
            SharedPreferences userSettings 			= PreferenceManager.getDefaultSharedPreferences(context);
            boolean popupDialogCheckboxPreference 	= userSettings.getBoolean("use_popup_dialog_preference_checkbox", false);

            if (popupDialogCheckboxPreference) {
                //TODO - loop through this List and compare reminder id, when found cancel it and reset position in the list to be a placeholder. Alternatively check if possible to insert reminder into db at first available row
                PopupDialogActivity.popupDialogs.get(reminderId).cancel();
            }
        }
        catch (Exception e) {
        }
	}
	
	
	/*
	 * [][][][][][][][][]
	 * PUBLIC FUNCTIONS
	 * [][][][][][][][][]
	 * 
	 */
	
	public void insertReminderIntoDbAndSetAlarm(Reminder r) {
		//this function expects reminder object WITHOUT actual ID
		//it will create the ID and use it to set alarm
		// (id of a row is the same as KEY_ID field)
		int reminderId = this.insertReminderIntoDB(r);
		this.setReminderAlarm(reminderId);
	}
	
	public Reminder getReminderFromDB(int id) {
		Reminder r = dbh.getReminder(id);
		return r;
	}
	
	public List<Reminder> getAllRemindersFromDB() {
		List<Reminder> reminders;
		reminders = dbh.getAllReminders();
		return reminders;
	}
	
	public void setReminderAlarm(int reminderId) {
		Reminder r = dbh.getReminder(reminderId);
		
	    Intent reminderAlarmReceiverIntent = new Intent(context, ReminderAlarmReceiver.class);
	    reminderAlarmReceiverIntent.putExtra("reminder_id", r.getId());
	    reminderAlarmReceiverIntent.putExtra("reminder_description", r.getDescription());
	    reminderAlarmReceiverIntent.putExtra("reminder_recurrence_position", r.getRecurrencePosition());
	    
		PendingIntent newAlarm = PendingIntent.getBroadcast(context, r.getAlarmId(), reminderAlarmReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		SharedPreferences userSettings = PreferenceManager.getDefaultSharedPreferences(context);
		    
		boolean repetitionCheckboxPreference	= userSettings.getBoolean("should_repeat_preference_checkbox", true);
	    
		
		//THIS LOGIC NEEDED TO BE CHANGED BECAUSE OF API 19 CHANGE
		//IN API 19 setRepeating is actually setInexactRepeating and is not accurate
		//it is combined with other wake locks for better battery life
		
	    //check if user wants repeatable alarm
	    if (repetitionCheckboxPreference) {
	    	//get default value of interval
	    	int repetitionInterval =  Integer.parseInt(context.getResources().getString(R.string.settings_repetition_interval_default));
	    	try {
	    		//retrieve alarm interval length from preferences
				repetitionInterval = Integer.parseInt(userSettings.getString("repetition_interval_preference", context.getResources().getString(R.string.settings_repetition_interval_default)));
			}
			catch (NumberFormatException nfe) {
		
			}
	    	
	    	long repetitionIntervalLong = repetitionInterval * 1000 * 60; 
	    	
	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, r.getTimestamp(), repetitionIntervalLong, newAlarm);
	    }
	    	//they do not want persistent alarm
	    else {
	    	alarmManager.set(AlarmManager.RTC_WAKEUP, r.getTimestamp(), newAlarm);
	    }
	}
	
	public void deleteReminder(int id) {
		//retrieve reminder from db
		Reminder reminder;
		
		//if someone tried to delete reminder that is already gone
		//such situation may occur if person clicked on reminder and then removed it from
		//pull down menu and then tried to remove it from 
		try {
			reminder = dbh.getReminder(id);
			
			//remove its pending alarm
			Intent oldReminderAlarmReceiverIntent = new Intent(context, ReminderAlarmReceiver.class);				
		    PendingIntent oldAlarm = PendingIntent.getBroadcast(context, reminder.getAlarmId(), oldReminderAlarmReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		    
		    alarmManager.cancel(oldAlarm);	
	    
		    if (oldAlarm != null) {
	    		oldAlarm.cancel();
	    	}

			try {	
				deleteNotification(id);//DELETE NOTIFICATION
			}
			catch (Exception exc) {
				
			}
			//remove reminder from DB
			dbh.deleteReminder(id);
		}
		catch (Exception e) {
			
		} 
	}
	
	public void deactivateReminder(int id) {
		//retrieve reminder from db
		Reminder reminder = dbh.getReminder(id); 
		
		//remove its pending alarm
		Intent oldReminderAlarmReceiverIntent = new Intent(context, ReminderAlarmReceiver.class);				
	    PendingIntent oldAlarm = PendingIntent.getBroadcast(context, reminder.getAlarmId(), oldReminderAlarmReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	    
	    alarmManager.cancel(oldAlarm);	
    
	    if (oldAlarm != null) {
    		oldAlarm.cancel();
    	}

		try {	
			deleteNotification(id);//DELETE NOTIFICATION
		}
		catch (Exception e) {
			
		}

		//do NOT remove reminder from DB, instead update its status to 0 = inactive
		reminder.setStatus(0);
		
		//and update it in the DB
		dbh.updateReminder(reminder);
	}
	
	public void snoozeReminder(int reminderId) {	
		try {
			//retrieve reminder from db
			Reminder reminder = dbh.getReminder(reminderId);
			
			//logic for determining if this is recurring reminder if yes it will create future reminder first 
			//and then update recurrence property of the reminder being snoozed
			if (reminder.getRecurrencePosition() != 0) {
				this.setFutureRecurringReminder(reminderId);
				//change its recurring property to NO recurring so when it comes back in 15 minutes or so it won't create new future reminder
				reminder.setRecurrencePosition(0);
				//update description
				String newDescription = reminder.getDescription().concat(" ").concat(context.getResources().getString(R.string.single_occurrence_copy));
				reminder.setDescription(newDescription);
			}		
			
			//create intent identical to what was used to create alarm and then cancel this old alarm
			Intent oldReminderAlarmReceiverIntent = new Intent(context, ReminderAlarmReceiver.class);				
			PendingIntent oldAlarm = PendingIntent.getBroadcast(context, reminder.getAlarmId(), oldReminderAlarmReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager.cancel(oldAlarm);
			
			//set new time
			long now = Calendar.getInstance().getTimeInMillis();
			int alarmId = (int)now;
			int snooze =  Integer.parseInt(context.getResources().getString(R.string.settings_snooze_length_default)); //get default value for snooze
			
			//retrieve snooze length from preferences
			SharedPreferences userSettings = PreferenceManager.getDefaultSharedPreferences(context);		
			try {
				snooze = Integer.parseInt(userSettings.getString("snooze_length_preference", context.getResources().getString(R.string.settings_snooze_length_default)));
			}
			catch (NumberFormatException nfe) {
				
			}
			
	        Calendar cal = Calendar.getInstance();
	        cal.setTimeInMillis(now);
	        cal.add(Calendar.MINUTE, snooze);
	        
			long delayedTime = cal.getTimeInMillis();
			
			//set properties of the reminder
			reminder.setAlarmId(alarmId);
			reminder.setTimestamp(delayedTime);
			//UPDATE time to show new value after snoozing
			reminder.setTime(delayedTime, context);
            //update status to active
            reminder.setStatus(1);
			//UPDATE date to show that this reminder is just single occurrence reminder
			reminder.setDate(delayedTime, reminder.getRecurrencePosition(), context);
			
			dbh.updateReminder(reminder);
			
			this.setReminderAlarm(reminder.getId());
	    	
	    	deleteNotification(reminderId);
			Toast.makeText(context, "You will be reminded in " + snooze + " minutes", Toast.LENGTH_LONG).show();
		}
		catch(Exception ex) {
			
		}
	}
	
	public void setFutureRecurringReminder(int oldReminderId) {
		Reminder oldReminder = this.getReminderFromDB(oldReminderId);
		Reminder newReminder = new Reminder();
		
		//generate integer of current time in milliseconds that will become alarmId 
		int alarmId = (int)Calendar.getInstance().getTimeInMillis();
		int recurrenceFrequencyPosition = oldReminder.getRecurrencePosition();
		
		//set properties of the reminder
		newReminder.setAlarmId(alarmId);
		newReminder.setDescription(oldReminder.getDescription());
		newReminder.setTime(oldReminder.getTimestamp(), context);//this is time string to be used just for display, not calculations
		newReminder.setDate(oldReminder.getTimestamp(), recurrenceFrequencyPosition, context);//this is date string to be used just for display, not calculations
		newReminder.setRecurrencePosition(recurrenceFrequencyPosition);
		
		//CREATE FUTURE DATE AND TIME
		Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(oldReminder.getTimestamp());
        
        
        
        //no recurrence
        if (recurrenceFrequencyPosition == 0) {
        	//NOTHING - this should never be even called
        }
        //recurring daily
        else if (recurrenceFrequencyPosition == 1) {
        	cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        //recurring weekly
        else if (recurrenceFrequencyPosition == 2) {
        	cal.add(Calendar.WEEK_OF_YEAR, 1);
        }
        //recurring monthly
        else if (recurrenceFrequencyPosition == 3) {
        	/*
        	 * do all kinds of crazy calculations to figure out:
        	 * - recurring reminder set to 31 
        	 * - what happens in February or 
        	 * - when month is only 30 days long etc
        	 * 
        	 * PSEUDO LOGIC:
        	 * if (original date >28) {

				- add 1 month as normal
				
					//keep adding days until next scheduled day = original day of month but not bigger than number of days in that month
					while next scheduled day < original day && next scheduled day < maxymalna ilosc dnigetMaximum(int field))
					{
						next scheduled day += 1;
					
					}
				}
        	 * 
        	 * 
        	 * 
        	 */
        	
        	//set variables
        	Calendar originalDate = Calendar.getInstance();
        	originalDate.setTimeInMillis(oldReminder.getOriginalTimestamp());
        	
        	int originalDay = originalDate.get(Calendar.DAY_OF_MONTH);
        	
        	//add 1 month to calendar - if original day was bigger than last day of new month then last day will be set. Nothing needs to be done.
        	//problem is the following month because now we add 1 month to already lowered number, so instead of repeating on 31st
        	//it repeats on 30th. this is even worse in February because reminder is rescheduled erroneously to  28 March
        	cal.add(Calendar.MONTH, 1);
        	
        	//check if we even have issue
        	if (originalDay > 28) {
        		int nextScheduledDay = cal.get(Calendar.DAY_OF_MONTH);

        		while (nextScheduledDay < originalDay && nextScheduledDay < cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
        			cal.add(Calendar.DAY_OF_MONTH, 1);
        			nextScheduledDay++;
        		}
        	}
        }
        //recurring annually
        else if (recurrenceFrequencyPosition == 4) {
        	cal.add(Calendar.YEAR, 1);
        }
        //recurring weekdays
        else if (recurrenceFrequencyPosition == 5) {
            int day = cal.get(Calendar.DAY_OF_WEEK);

            //on monday through thursday set reminder for next day
            if (day == 2 || day == 3 || day == 4 || day == 5) {
                //adds a day
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
            //on friday set alarm in 3 days (next monday)
            else if (day == 6) {
                cal.add(Calendar.DAY_OF_YEAR, 3);
            }
            //on saturday (in case first alarm was on that day)
            else if (day == 7) {
                cal.add(Calendar.DAY_OF_YEAR, 2);
            }
            //on sunday (in case first alarm was on that day)
            //this could be combined with first if but it clearer this way
            else if (day == 1) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
        }

        //recurring weekends
        else if (recurrenceFrequencyPosition == 6) {
            int day = cal.get(Calendar.DAY_OF_WEEK);

            //sunday
            if (day == 1) {
                //adds 6 days
                cal.add(Calendar.DAY_OF_YEAR, 6);
            }
            //mon
            else if (day == 2) {
                //adds 5 days
                cal.add(Calendar.DAY_OF_YEAR, 5);
            }
            //tue
            else if (day == 3) {
                //adds 4 days
                cal.add(Calendar.DAY_OF_YEAR, 4);
            }
            //wed
            else if (day == 4) {
                //adds 3 days
                cal.add(Calendar.DAY_OF_YEAR, 3);
            }
            //thur
            else if (day == 5) {
                //adds 2 days
                cal.add(Calendar.DAY_OF_YEAR, 2);
            }
            //fri
            else if (day == 6) {
                //adds 1 days
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
            //sat
            else if (day == 7) {
                //adds 1 days
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
        }

        
		long delayedTime = cal.getTimeInMillis();
		
		//set properties of the reminder
		newReminder.setTimestamp(delayedTime);		
		newReminder.setStatus(1);//status 0 = expired, status 1 = active
		newReminder.setRecurrencePosition(oldReminder.getRecurrencePosition());		
		
		this.insertReminderIntoDbAndSetAlarm(newReminder);
	}
	
	public void deleteNotification(int reminderId) {
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(reminderId);

        //remove dialog if necessary
        dismissPopupDialog(reminderId);
	}
	
	public void showNumberOfReminders() {
		int numRem = dbh.getRemindersCount();
		
		String msg;
		
		if (numRem == 1) {
			msg = "There is " + numRem + " reminder in the database";
		}
		else {
			msg = "There are " + numRem + " reminders in the database";
		}
		
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
	
	public void resetAlarmsForAllRemindersInDB() {
		List<Reminder> reminders = this.getAllRemindersFromDB();
		
		//2. Loop through reminders
		try {
			for (Reminder r : reminders) {
				
				//reset alarm only for active reminders
				if (r.getStatus() == 1) {
					this.setReminderAlarm(r.getId());
				}
			}
		}
		catch (Exception e) {
			//There are no reminders or something else is not right
		}
	}
	
	public void updateReminder(Reminder r) {
		dbh.updateReminder(r);
	}
	
	public void dismissRecurringReminder(int reminderId) {
		this.setFutureRecurringReminder(reminderId);
		this.deleteReminder(reminderId);
		Toast.makeText(context, "Reminder dismissed", Toast.LENGTH_SHORT).show();
	}
	
	/*
	 * [][][][][][][][][]
	 * PRIVATE FUNCTIONS
	 * [][][][][][][][][]
	 * 
	 */
	
	//this function is not available publicly, it is only used internally to retrieve reminder's id
	private int insertReminderIntoDB(Reminder r){
		int reminderId = dbh.addReminder(r);
		return reminderId;
	}
	
}
