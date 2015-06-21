package com.george.reminderpro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;


public class ReminderWorkerReceiver extends BroadcastReceiver {

	private final String SNOOZE 	= "com.gadgetjudge.simplestreminder.SNOOZE";
	private final String DELETE 	= "com.gadgetjudge.simplestreminder.DELETE";
	private final String DEACTIVATE = "com.gadgetjudge.simplestreminder.DEACTIVATE";
	private final String DISMISS 	= "com.gadgetjudge.simplestreminder.DISMISS";
	private ReminderManager rm;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		/* STEPS NEEDED HERE:
		 * 
		 * CHECK IF ACTION IS SNOOZE OR DELETE
		 * IF SNOOZE DO ....
		 * IF DELETE DO.....
		 * 
		 * 
		 */
		
		if (intent.getExtras() != null) {
			Bundle alarmExtras = intent.getExtras();
		
			try {
				int receivedReminderId = alarmExtras.getInt("reminder_id");
				
				//check if ID is valid
				if (receivedReminderId > 0) {	
					
					rm = new ReminderManager(context);
					
					//DELETE
					if (intent.getAction().equals(DELETE)) {											
						rm.deleteReminder(receivedReminderId);
						Toast.makeText(context, "Reminder deleted", Toast.LENGTH_SHORT).show();
					}
					//DEACTIVATE
					if (intent.getAction().equals(DEACTIVATE)) {	
						Toast.makeText(context, "Reminder deactivated", Toast.LENGTH_SHORT).show();
						rm.deactivateReminder(receivedReminderId);
						
					}
					//DISMISS
					else if (intent.getAction().equals(DISMISS)){
						//to be implemented
						rm.dismissRecurringReminder(receivedReminderId);
					}	
					//SNOOZE
					else if (intent.getAction().equals(SNOOZE)){
						rm.snoozeReminder(receivedReminderId);
					}	
				}
			}
			catch (Exception e) {
				//
			}
		
		}		
	}
}

