package com.george.reminderpro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
//import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PopupDialogActivity extends Activity {

    public static List<AlertDialog> popupDialogs = new ArrayList<>();

    private Context applicationContext;
    private ReminderManager rm;
    private AlertDialog popupDialog;
    private int receivedReminderId;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		applicationContext = getApplicationContext();

        //check if extras send or if this activity was started from New Reminder Activity
        if (getIntent().getExtras() != null) {
            Bundle alarmExtras = getIntent().getExtras();

            try {
                receivedReminderId = alarmExtras.getInt("reminder_id");
                //check if ID is valid
                if (receivedReminderId > 0) {

                    //Log.v("DEBUG", "reminder id is " + receivedReminderId);

                    //if list is empty we need to fill it with nulls
                    if (popupDialogs.isEmpty()) {
                        for (int i = 0; i < receivedReminderId +1; i++){
                            popupDialogs.add(null);
                        }

                        //Log.v("DEBUG", "after filling list size is " + popupDialogs.size());
                    }
                    //otherwise we need to make sure that it is not too small
                    //this should only happen in case when 2 reminders go off at the same time
                    else if (popupDialogs.size() < receivedReminderId + 1) {
                        //Log.v("DEBUG", "list is too small " + popupDialogs.size());
                        for (int i = popupDialogs.size(); i < receivedReminderId +1; i++){
                            popupDialogs.add(null);
                        }
                        //Log.v("DEBUG", "list is adjusted " + popupDialogs.size());
                    }

                    //prevent multiple dialogs for the same reminder from popping up
                    if (popupDialogs.get(receivedReminderId) == null) {

                        //setContentView(R.layout.activity_popup_dialog);

                        //Log.v("DEBUG", "OBJECT IS NULL");

                        //setContentView(R.layout.activity_popup_dialog);

                        rm = new ReminderManager(applicationContext);
                        Reminder r = rm.getReminderFromDB(receivedReminderId);

                        chooseReminderAction(r);
                    }
                    else {
                        //Log.v("DEBUG", "OBJECT NOT NULL = dialog already in the list");
                        //make sure to finish so that blank+empty+transparent activities don't stay on the screen
                        finish();
                    }
                }
            }
            catch (Exception e) {
                //Log.v("DEBUG", "EXCEPTION");
            }
        }
	}

    private void chooseReminderAction(final Reminder selectedReminder) {

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme));

        builder.setTitle("Don't forget:")
		.setMessage(selectedReminder.toString())
		.setIcon(R.drawable.ic_launcher)
	    .setNeutralButton("Edit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent editReminderIntent = new Intent(applicationContext, NewReminderActivity.class);
                editReminderIntent.putExtra("reminder_id", selectedReminder.getId());

                try {
                    rm.deleteNotification(selectedReminder.getId());
                } catch (Exception e) {

                }

                startActivity(editReminderIntent);
                finish();
            }
        });

		//if reminder is NOT recurring
		if(selectedReminder.getRecurrencePosition() == 0) {
			builder.setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
		    	public void onClick(DialogInterface dialog, int id) {
		    		rm.deleteReminder(selectedReminder.getId());
		    		Toast.makeText(applicationContext, "Reminder deleted", Toast.LENGTH_SHORT).show();
		    		finish();
		    	}
			});
		}
		//reminder is recurring
		else {
			//if it is active - reminder went off
			builder.setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
		    	public void onClick(DialogInterface dialog, int id) {
		    		rm.dismissRecurringReminder(selectedReminder.getId());
		    		finish();
			   }
			});
		}

		//add SNOOZE button if reminder is expired 
		builder.setPositiveButton(R.string.snooze, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User hit snooze button the dialog
				//utilize ReminderManager to perform snooze
				rm.snoozeReminder(selectedReminder.getId());

				//remove reminder id extra from existing intent - this will prevent Reminder Menu from appearing on every launch
				getIntent().removeExtra("reminder_id");
				finish();
			}
		});
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                //Log.v("DEBUG", "CANCELLING and FINISHING");
                popupDialogs.set(receivedReminderId, null);
                //Log.v("DEBUG", "CANCELLED");
                finish();
                //Log.v("DEBUG", "FINISHED");
            }
        });
		
		//if set to false users can't click around the dialog to get rid of it = very intrusive
		builder.setCancelable(true);
		
		builder.create();
        popupDialog = builder.show();

        //TODO - insert at first available position, alternatively check if possible to insert reminder into db at first available row
        popupDialogs.set(receivedReminderId, popupDialog);
	}

}
