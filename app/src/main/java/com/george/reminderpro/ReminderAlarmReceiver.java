package com.george.reminderpro;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;

public class ReminderAlarmReceiver extends WakefulBroadcastReceiver {

	Context thisContext;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		thisContext = context;
		
		//TURN ON THE SCREEN
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		final PowerManager.WakeLock cpuWakeLock 	= pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Reminder Wakes CPU");
		@SuppressWarnings("deprecation")
		final PowerManager.WakeLock screenWakeLock 	= pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "Reminder Wakes Screen");

		//ALWAYS wake up device for few seconds, screen on option removed
		cpuWakeLock.acquire(5000);
		
		//check if reminder id was sent over, if not it means something is wrong; there is no need to waste resources to create anything else - ABORT gracefully
		Bundle extras = intent.getExtras();
		if (extras == null) {
			return;
		}
		
		int reminderId = extras.getInt("reminder_id");
		String reminderDescription = extras.getString("reminder_description");
		
		//make sure that reminder id is correct
		if (reminderId > 0) {
			
			int reminderRecurrenceFrequencyPosition = extras.getInt("reminder_recurrence_position");
			
			//RETRIEVE USER PREFERENCES
			SharedPreferences userSettings 			= PreferenceManager.getDefaultSharedPreferences(context);
//			boolean vibrationCheckboxPreference		= userSettings.getBoolean("should_vibrate_preference_checkbox", true);
			boolean vibrationCheckboxPreference		= userSettings.getBoolean("should_vibrate_preference_checkbox", false);
			boolean ringtoneCheckboxPreference		= userSettings.getBoolean("use_ringtone_preference_checkbox", true);
//			boolean screenCheckboxPreference		= userSettings.getBoolean("wake_screen_preference_checkbox", true);
			boolean screenCheckboxPreference		= userSettings.getBoolean("wake_screen_preference_checkbox", false);
			boolean ledCheckboxPreference			= userSettings.getBoolean("led_preference_checkbox", true);
//			boolean swipeAwayCheckboxPreference		= userSettings.getBoolean("swipe_away_preference_checkbox", true);
			boolean swipeAwayCheckboxPreference		= userSettings.getBoolean("swipe_away_preference_checkbox", false);
			boolean popupDialogCheckboxPreference 	= userSettings.getBoolean("use_popup_dialog_preference_checkbox", false);

			//INTENTS REQUIRED FOR CLICK ACTIONS ON NOTIFICATION
			/*
			 * set the flag that will make all the other activities be closed (this will prevent old data being shown and also will fix the issue that 
			 * clicking notification does not launch activity)
			 * 
			 * from official documentation: 
			 * If set, and the activity being launched is already running in the current task, then instead of launching a new instance of that activity, 
			 * all of the other activities on top of it will be closed and this Intent will be delivered to the (now on top) old activity as a new Intent.
			 * 
			 */
			
			//MAIN NOTIFICATION
			Intent resultIntent = new Intent(context, ListRemindersActivity.class);
			resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			resultIntent.putExtra("reminder_id", reminderId);
			
			//POPUP DIALOG
			Intent popupDialogIntent = new Intent(context, PopupDialogActivity.class);
			/*
			 * FLAG_ACTIVITY_CLEAR_TASK closes any running activity
			 * this will prevent dialog opening in the list of reminders activity
			 * or in the new reminder activity BUT it also kills all other
			 * dialogs. 
			 */
			popupDialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			popupDialogIntent.putExtra("reminder_id", reminderId);
			/*
			 * this will set a category for each dialog so that if more than
			 * one dialog is active at once all can be shown instead of the
			 * latest one
			 */
			popupDialogIntent.addCategory("DIALOGS"+reminderId);

			//SNOOZE
			Intent snoozeIntent = new Intent();
			snoozeIntent.setAction("com.gadgetjudge.simplestreminder.SNOOZE");
			snoozeIntent.putExtra("reminder_id", reminderId);
			
			//DELETE
			Intent deleteIntent = new Intent();
			deleteIntent.setAction("com.gadgetjudge.simplestreminder.DELETE");
			deleteIntent.putExtra("reminder_id", reminderId);
			
			//DEACTIVATE
			Intent deactivateIntent = new Intent();
			deactivateIntent.setAction("com.gadgetjudge.simplestreminder.DEACTIVATE");
			deactivateIntent.putExtra("reminder_id", reminderId);
			
			//DISMISS
			Intent dismissIntent = new Intent();
			dismissIntent.setAction("com.gadgetjudge.simplestreminder.DISMISS");
			dismissIntent.putExtra("reminder_id", reminderId);
			
			//EDIT
			Intent editIntent = new Intent(context, NewReminderActivity.class);
			editIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			editIntent.putExtra("reminder_id", reminderId);
			
			//MAKE SURE TO USE REMINDER ID AS A UNIQUE REQUEST CODE (2nd parameter) so it can handle 2 or more alarms that go off at the same time
			PendingIntent pendingMainClickIntent	= PendingIntent.getActivity(context, reminderId, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			PendingIntent pendingSnoozeIntent 		= PendingIntent.getBroadcast(context, reminderId, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			PendingIntent pendingDeleteIntent 		= PendingIntent.getBroadcast(context, reminderId, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT); 
			PendingIntent pendingDeactivateIntent 	= PendingIntent.getBroadcast(context, reminderId, deactivateIntent, PendingIntent.FLAG_UPDATE_CURRENT); 
			PendingIntent pendingDismissIntent 		= PendingIntent.getBroadcast(context, reminderId, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT); 
			PendingIntent pendingEditIntent 		= PendingIntent.getActivity(context, reminderId, editIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);

            NotificationCompat.Builder mBuilder =
					new NotificationCompat.Builder(context)
					.setSmallIcon(R.drawable.ic_alarm_on_white)
                    .setLargeIcon(bm)
					.setContentTitle("Do not forget:")   //"Don't forget:"
					.setContentText(reminderDescription)
					.setContentIntent(pendingMainClickIntent)
					.addAction(R.drawable.ic_action_snooze, context.getResources().getString(R.string.snooze), pendingSnoozeIntent)
					.addAction(R.drawable.ic_action_edit, context.getResources().getString(R.string.edit), pendingEditIntent)
					.setAutoCancel(false);
			
			//check if reminder has recurrence 0 = NO recurrence, other number has recurrence
			if (reminderRecurrenceFrequencyPosition == 0) {
				mBuilder.addAction(R.drawable.ic_action_delete, context.getResources().getString(R.string.delete), pendingDeleteIntent);
				
				//check if user wants the swipe to delete reminder, if not notification will be removed but reminder will stay in DB
				if (swipeAwayCheckboxPreference) {
					//on SWIPE to dismiss or clear all user action DELETE reminder
					mBuilder.setDeleteIntent(pendingDeleteIntent);
				}
				//just set status to inactive, but do not delete
				//this will prevent inactive reminders to ring again when user reboots their phone or changes settings
				else {
					mBuilder.setDeleteIntent(pendingDeactivateIntent);
				}
			}
			
			//if this is recurring reminders
			else {
				mBuilder.addAction(R.drawable.ic_action_dismiss, context.getResources().getString(R.string.dismiss), pendingDismissIntent);
				//on SWIPE to dismiss or clear all user action DISMISS reminder
				mBuilder.setDeleteIntent(pendingDismissIntent);
			}
			
			
			//if user wants to use sound
			if (ringtoneCheckboxPreference) {
				
				String pickedRingtone = userSettings.getString("ringtone_style_preference", "");
				
				//if ring tone not picked
				if (pickedRingtone.isEmpty()) {
					//set default sound
					mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
				}
				else
				{
					Uri ringtoneUri = Uri.parse(pickedRingtone);
					mBuilder.setSound(ringtoneUri);
				}
			}
			
			
			//check if user wants to use vibration
			if (vibrationCheckboxPreference) {
				String vibrationPatternPreference = userSettings.getString("vibrate_pattern_preference", context.getResources().getString(R.string.settings_vibration_pattern_default));				
				
				String delimiter = "[\\D]+";//whatever is not a digit / string of non-digits
				long[] defaultVibratePattern = convertStringToArrayTypeLong(context.getResources().getString(R.string.settings_vibration_pattern_default), delimiter);
				long[] vibrationPattern = null;
				
				//check if pattern not empty
				if (!vibrationPatternPreference.isEmpty()) {
					//build vibration pattern
					vibrationPattern = convertStringToArrayTypeLong(vibrationPatternPreference, delimiter);
					
					//in case user left blank space or invalid characters in entry box, use default pattern
					if (vibrationPattern.length == 0) {
						vibrationPattern = defaultVibratePattern;
					}

				}
				else {
					vibrationPattern = defaultVibratePattern;
				}
				mBuilder.setVibrate(vibrationPattern);
			}
            //force no vibration
            else {
                long[] vibrationPattern = new long[1];
                vibrationPattern[0] = 0;
                mBuilder.setVibrate(vibrationPattern);
            }
			
			//check if user wants to flash LED lights
			if(ledCheckboxPreference) {
				mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
			}
			
			//check if user wants popup dialog
			if (popupDialogCheckboxPreference) {
				context.startActivity(popupDialogIntent);
			}

			//if user wants to wake screen
			if (screenCheckboxPreference) {
				//wake CPU and screen
				screenWakeLock.acquire(5000);
			}

			//	wl.acquire(7500);
				
				
				//changed logic to acquire for few seconds - above
				//wl.acquire();
				
				//RELEASE WAKELOCK after 5 seconds
				//final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
				
				//Runnable task = new Runnable() {
				//	public void run() {
				//		wl.release();
				//    }
				//};
				//worker.schedule(task, 5, TimeUnit.SECONDS);
			//}

			//CREATE NOTIFICATION MANAGER
			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			
			//CALL MNOTIFICATION
			mNotificationManager.notify(reminderId, mBuilder.build());
			
		}
		completeWakefulIntent(intent);
	}
	
	private long[] convertStringToArrayTypeLong(String input, String delimiter) {
		long[] output = null;
		//clean up string to remove all spaces
		input = input.replaceAll(" ", "");
		String[] tempArrayOfStrings = input.split(delimiter);
		int count = tempArrayOfStrings.length;

		//if there is at least one value specified by user, otherwise 
		if (count > 0) {
			output = new long[count];
			
			for (int i = 0; i < count; i++) {							
				output[i] = Long.parseLong(tempArrayOfStrings[i]);						
			}
		}
		else {
			output = new long[]{};
		}

		return output;
	}
}
