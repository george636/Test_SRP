package com.george.reminderpro;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

public class ListRemindersActivity extends BaseActivity {
	
	private ListView listViewScheduledReminders; 
	private List<Reminder> reminders;
    private CustomListAdapter adapter;
	//private String action;
	private DateAndTime timeNow;
	private ImageButton buttonPlusReminder;
	private ImageButton buttonHelp;
	private ImageButton buttonSettings;
	private ReminderManager rm;
	private Context applicationContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_list_reminders);

        //add back button on the top
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		//INITIALIZE OBJECTS
		applicationContext = getApplicationContext();
		rm = new ReminderManager(applicationContext);
		listViewScheduledReminders = (ListView)findViewById(R.id.listViewScheduledReminders);
		reminders = rm.getAllRemindersFromDB();
        adapter = new CustomListAdapter(this, reminders);
		//action = "";		
		timeNow = new DateAndTime(applicationContext);
		
		//bind adapter to list view
		listViewScheduledReminders.setAdapter(adapter);
		
		buttonPlusReminder 		= (ImageButton)findViewById(R.id.buttonPlusReminder);
		buttonHelp 				= (ImageButton)findViewById(R.id.buttonHelp);
		buttonSettings			= (ImageButton)findViewById(R.id.buttonSettings);
		
		OnClickListener buttonPlusReminderClickListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startNewReminderActivity();
			}
		};
		
		OnClickListener buttonHelpClickListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showHelp();
			}
		};
		
		OnClickListener buttonSettingsClickListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showSettings();
			}
		};
		
		buttonPlusReminder.setOnClickListener(buttonPlusReminderClickListener);
		buttonHelp.setOnClickListener(buttonHelpClickListener);
		buttonSettings.setOnClickListener(buttonSettingsClickListener);
		
		//setup on click listener for each item
		listViewScheduledReminders.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
				
				
				timeNow.setToNow();
				//****************************************************************
				//TO GET CURRENT REMINDER OBJECT DO THIS: reminders.get(position)
				//****************************************************************			
				chooseReminderAction(reminders.get(position));
			}
		});

		//setup on long click listener for each item	
		listViewScheduledReminders.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id) {
				Reminder r = reminders.get(position);
				
				if (r.getRecurrencePosition() !=0) {
					showDeleteDialog(r);
				}
				else {
					chooseReminderAction(r);
					Log.v("DEBUG", "Reminder info: ID " + r.getId() + " alarmID " + r.getAlarmId() + " stamp " + r.getTimestamp() + " status " + r.getStatusString() + " recurring position " + r.getRecurrencePosition());
				}
				return true;
			}
		});
	}
	
	
	/*
	 * I will use onStart to capture Intent data sent from ReminderAlarmReceiver (reminder_id is expected)
	 */
	@Override
	protected void onStart() {		
		super.onStart();

		timeNow.setToNow();
		//check if extras send or if this activity was started from New Reminder Activity
		if (getIntent().getExtras() != null) {
			Bundle alarmExtras = getIntent().getExtras();
			
			//if other intent was sent but not contains reminder id
			try {
				int receivedReminderId = alarmExtras.getInt("reminder_id");
				
				//check if ID is valid
				if (receivedReminderId > 0) {
					//action = "ACTIVE";
					chooseReminderAction(rm.getReminderFromDB(receivedReminderId));
				}
			}
			catch (Exception e) {
			}
		}	
	}
		
	@TargetApi(11)
	private void refreshRemindersList() {
		reminders.clear();
		reminders = rm.getAllRemindersFromDB();
		
		adapter.clear();
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			adapter.addAll(reminders);
		}
		else {
			for (Reminder r: reminders) {
				adapter.add(r);
			}
		}
		
		adapter.notifyDataSetChanged();
	}
	

	
	private void chooseReminderAction(final Reminder selectedReminder) {
		timeNow.setToNow();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Reminder Menu")
		.setMessage("What would you like to do?\n\nReminder: " + selectedReminder.toString())
		.setIcon(R.drawable.ic_launcher)
	    .setNeutralButton("Edit", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int id) {
                Intent editReminderIntent = new Intent(applicationContext, NewReminderActivity.class);
	    		editReminderIntent.putExtra("reminder_id", selectedReminder.getId());
                editReminderIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

	    		try {
	    			rm.deleteNotification(selectedReminder.getId());
	    		}
	    		catch (Exception e) {
	    		}
	    		
	    		startActivity(editReminderIntent);
	    	}
		});
		
		//if reminder is NOT recurring
		if(selectedReminder.getRecurrencePosition() == 0) {
			builder.setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
		    	public void onClick(DialogInterface dialog, int id) {
		    		rm.deleteReminder(selectedReminder.getId());
                    //prevent dialog with empty data from showing when application was opened again
                    getIntent().removeExtra("reminder_id");
		    		Toast.makeText(applicationContext, "Reminder deleted", Toast.LENGTH_SHORT).show();
			   }
			});
		}
		//reminder is recurring
		else {
			//if it is active - reminder went off
			if ((timeNow.getTimestamp() - selectedReminder.getTimestamp()) >= 0) {
				builder.setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
			    	public void onClick(DialogInterface dialog, int id) {
			    		rm.dismissRecurringReminder(selectedReminder.getId());
				   }
				});
			}
		}
		
		
		//reminder is for FUTURE
		if ((timeNow.getTimestamp() - selectedReminder.getTimestamp()) < 0 ) {
			builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User cancelled the dialog
			}
			});
		}
		//add SNOOZE button if reminder is expired but add cancel button if it is deactivated
		else if ((timeNow.getTimestamp() - selectedReminder.getTimestamp()) >= 0) {

            //reminder is old and deactivated
            if (selectedReminder.getStatus() == 0) {
                builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
            }
            else {
                builder.setPositiveButton(R.string.snooze, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User hit snooze button the dialog
                        //utilize ReminderManager to perform snooze
                        rm.snoozeReminder(selectedReminder.getId());

                        //remove reminder id extra from existing intent - this will prevent Reminder Menu from appearing on every launch
                        getIntent().removeExtra("reminder_id");

                        timeNow.setToNow();
                        refreshRemindersList();
                    }
                });
            }
		}
		
		builder.create().show();		// create and show the alert dialog
	}
	
	private void showDeleteDialog(final Reminder r) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("DELETE Reminder")
		.setMessage("Do you really want to permanently delete recurring reminder?\n\nReminder: " + r.toString())
		.setIcon(R.drawable.ic_action_delete)
		.setPositiveButton(android.R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User cancelled the dialog
                dialog.cancel();
			}
		})
	    .setNegativeButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int id) {
	    		rm.deleteReminder(r.getId());
	    		Toast.makeText(applicationContext, "Reminder deleted", Toast.LENGTH_SHORT).show();
	    	}
		});
		builder.create().show();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_list_reminders, menu);
		return true;
	}
	
	private void showSettings() {
		Intent settingsIntent = new Intent(applicationContext, SettingsActivity.class);
    	startActivity(settingsIntent);
	}
	
	private void startNewReminderActivity() {
		Intent createReminderIntent = new Intent(ListRemindersActivity.this, NewReminderActivity.class);
        createReminderIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	ListRemindersActivity.this.startActivity(createReminderIntent);
    	
    	//clean up
    	finish();
	}
	
	private void showHelp() {
		Intent showHelpIntent = new Intent(ListRemindersActivity.this, HelpActivity.class);
    	ListRemindersActivity.this.startActivity(showHelpIntent);
	}

    @Override protected int getLayoutResource() {
        return R.layout.activity_list_reminders;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
            case android.R.id.home:
                startNewReminderActivity();
                return true;
    		case R.id.menu_help:
    			showHelp();
    			return true;
    		case R.id.menu_create_new_reminder:
    			startNewReminderActivity();
    			return true;
    		case R.id.menu_settings:
    			showSettings();
    			return true;
    	default:
			return super.onOptionsItemSelected(item);
    	}
    }

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}


	@Override
	protected void onResume() {
		super.onResume();
		refreshRemindersList();
	}


	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus == true) {
			refreshRemindersList();
		}
	}	
}
