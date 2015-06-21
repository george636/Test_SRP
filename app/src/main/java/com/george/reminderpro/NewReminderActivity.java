package com.george.reminderpro;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.util.Calendar;

public class NewReminderActivity extends BaseActivity implements OnDateSetListener, TimePickerDialog.OnTimeSetListener {

	
	//DEFINE/INSTANTIATE ALL VARIABLES AND OBJECTS
	
	//public
	public DateAndTime currentDateAndTime;
	public DateAndTime requestedDateAndTime;
	public Context applicationContext;

    public static final String DATEPICKER_TAG = "datepicker";
    public static final String TIMEPICKER_TAG = "timepicker";

	//private
	private EditText editTextTime;
	private EditText editTextDate;
	private EditText editTextDescription;
	private Spinner recurringFrequencySpinner;
	private ImageButton buttonCreateReminder;
	private ImageButton buttonListReminders;
	private ImageButton buttonHelp;
	private ImageButton buttonSettings;
	private String action;
	private int recurringFrequencySelectedPosition;
	private Reminder editedReminder;
	private ReminderManager rm;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private SharedPreferences userSettings;
    private boolean pickersCheckboxPreference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_new_reminder);
        setActionBarTitle(R.string.new_reminder);

        //INITIALIZE OBJECTS
        //cache contexts
        applicationContext 	= getApplicationContext();
        rm 					= new ReminderManager(applicationContext);
        
        //time + date fields
        editTextTime = (EditText)findViewById(R.id.editTextTime);
        editTextDate = (EditText)findViewById(R.id.editTextDate);
        
        //repeating frequency spinner
        recurringFrequencySpinner = (Spinner)findViewById(R.id.spinnerRepeatingFrequency);
        
        //prevent these text fields to have focus - this way on click will bring time/date picker right away
        //editTextTime.setFocusableInTouchMode(false);
        //editTextDate.setFocusableInTouchMode(false);
        
        editTextDescription 	= (EditText)findViewById(R.id.editTextNewReminderTitle);
        buttonCreateReminder 	= (ImageButton)findViewById(R.id.buttonCreateReminder);
        buttonListReminders		= (ImageButton)findViewById(R.id.buttonListReminders);
        buttonHelp				= (ImageButton)findViewById(R.id.buttonHelp2);
        buttonSettings			= (ImageButton)findViewById(R.id.buttonSettings);
        
        //time
        currentDateAndTime = new DateAndTime(applicationContext);
        currentDateAndTime.setToNow();
        
        requestedDateAndTime = new DateAndTime(applicationContext);
        requestedDateAndTime.setToNow(); //set date and time to now just in case user doesn't select any time or date he want the reminder is the same as today
               
        //initialize action which will be used to determine whether reminder is new or edited
        action = "";

        userSettings = PreferenceManager.getDefaultSharedPreferences(applicationContext);

        //CREATE LISTENERS
        //Pick Time Text Field        
        OnClickListener editTextTimeClickListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showTimePickerDialog(v);
			}
		};
		
		//Pick Date Text Field
		OnClickListener editTextDateClickListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDatePickerDialog(v);
			}
		};

        //pick date and time for new pickers
        if (savedInstanceState != null) {
            DatePickerDialog dpd = (DatePickerDialog) getSupportFragmentManager().findFragmentByTag(DATEPICKER_TAG);
            if (dpd != null) {
                dpd.setOnDateSetListener(this);
            }

            TimePickerDialog tpd = (TimePickerDialog) getSupportFragmentManager().findFragmentByTag(TIMEPICKER_TAG);
            if (tpd != null) {
                tpd.setOnTimeSetListener(this);
            }
        }
		
		//Create Reminder Button
		OnClickListener buttonCreateReminderClickListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
							    
				saveReminder();
			}
		};
		
		OnClickListener buttonListRemindersClickListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showReminders();
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

        AdapterView.OnItemSelectedListener recurringFrequencySpinnerItemSelectedListener = new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView tvDateDescription = (TextView)findViewById(R.id.textDateDescription);
                if (position > 0) {
                    tvDateDescription.setText(R.string.starting_date);
                }
                else {
                    tvDateDescription.setText(R.string.date);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };

		
		//ASSIGN LISTENERS
		editTextTime.setOnClickListener(editTextTimeClickListener);
		editTextDate.setOnClickListener(editTextDateClickListener);
		buttonCreateReminder.setOnClickListener(buttonCreateReminderClickListener);
		buttonListReminders.setOnClickListener(buttonListRemindersClickListener);
		buttonHelp.setOnClickListener(buttonHelpClickListener);
		buttonSettings.setOnClickListener(buttonSettingsClickListener);
        recurringFrequencySpinner.setOnItemSelectedListener(recurringFrequencySpinnerItemSelectedListener);
		

        //CALL FUNCTIONS
        updateTimeAndDateDisplay(currentDateAndTime.getTimeString(), currentDateAndTime.getDateString());

    }

	//CREATE PUBLIC METHODS
    public void updateTimeDisplay(String timeString) {
    	editTextTime.setText(timeString);
    }
    
    public void updateDateDisplay(String dateString) {
    	editTextDate.setText(dateString);
    }
        
	public void updateTimeAndDateDisplay(String timeString, String dateString) {
		updateTimeDisplay(timeString);
		updateDateDisplay(dateString);
	}

	public void resetAll() {
		//objects
		currentDateAndTime.setToNow();
		requestedDateAndTime.setToNow();
		editedReminder = null;
				
		//text fields
		editTextDescription.setText("");
		editTextDate.setText(currentDateAndTime.getDateString());
		editTextTime.setText(currentDateAndTime.getTimeString());
		
		//strings
		action = "";
		
		//spinner
		recurringFrequencySelectedPosition = 0;
	}
    
	public void showTimePickerDialog(View v) {
        //check if user wants new pickers and API < 21. It is currently impossible to show spinners in dialog on API 21+
        if (pickersCheckboxPreference && (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
            timePickerDialog = TimePickerDialog.newInstance(this, requestedDateAndTime.getHourInt(), requestedDateAndTime.getMinuteInt(), requestedDateAndTime.isTime24Hours(), true);
            timePickerDialog.setVibrate(true);
            timePickerDialog.setCloseOnSingleTapMinute(false);
            timePickerDialog.show(getSupportFragmentManager(), TIMEPICKER_TAG);
        }
        else {
            DialogFragment newFragment = new TimePickerFragment();
            newFragment.show(getSupportFragmentManager(), "timePicker");
        }
	}
	
	public void showDatePickerDialog(View v) {
        //check if user wants new pickers and API < 21. It is currently impossible to show spinners in dialog on API 21+
        if (pickersCheckboxPreference && (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
            datePickerDialog = DatePickerDialog.newInstance(this, requestedDateAndTime.getYearInt(), requestedDateAndTime.getMonthInt(), requestedDateAndTime.getDayInt(), true);
            datePickerDialog.setVibrate(true);
            datePickerDialog.setYearRange(currentDateAndTime.getYearInt(), currentDateAndTime.getYearInt()+10);
            datePickerDialog.setCloseOnSingleTapDay(false);
            datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
        }

        //run default dialogs - will need to check again if user want spinners on analog
        else {
            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(getSupportFragmentManager(), "datePicker");
        }
	}
	
	//OVERRIDE METHODS
    @Override protected int getLayoutResource() {
        return R.layout.activity_new_reminder;
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        requestedDateAndTime.setYearInt(year);
        requestedDateAndTime.setMonthInt(month);
        requestedDateAndTime.setDayInt(day);

        String dateString = requestedDateAndTime.getDateString();
        updateDateDisplay(dateString);
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        requestedDateAndTime.setHourInt(hourOfDay);
        requestedDateAndTime.setMinuteInt(minute);

        String timeString = requestedDateAndTime.getTimeString();
        updateTimeDisplay(timeString);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_new_reminder, menu);
        return true;
    }
    
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
            case android.R.id.home:
                showReminders();
                return true;
    		case R.id.menu_save_reminder:
    			saveReminder();
			return true;
    		case R.id.menu_show_reminders:
    			showReminders();
    			return true;
    		case R.id.menu_help:
    			showHelp();
    			return true;
    		case R.id.menu_settings:
    			showSettings();
    			return true;
    		
	    	default:
				return super.onOptionsItemSelected(item);
    	}
    }
    
    
    
    
    @Override
	protected void onStart() {
		super.onStart();
		
		/*
	     * **************************************
	     * UPDATE DISPLAYED TIME
	     * **************************************
	     */
		
		//time and date set by user is less than current time. no sense in keeping those values. UPDATE to current time
		currentDateAndTime.setToNow();
		
		if ((currentDateAndTime.getTimestamp() > requestedDateAndTime.getTimestamp()) && action != "EDIT") {
			requestedDateAndTime.setToNow();
			updateTimeAndDateDisplay(requestedDateAndTime.getTimeString(), requestedDateAndTime.getDateString());
		}
		
		
		/*
	     * **************************************
	     * EDIT REMINDER
	     * **************************************
	     */
		if (getIntent().getExtras() != null) {
		
			Bundle reminderExtras = getIntent().getExtras();

			//if other intent was sent but not contains reminder id
			try {
				int receivedReminderId = reminderExtras.getInt("reminder_id");
				
				try {
					//check if ID is valid
					if (receivedReminderId > 0) {

						//TRY REMOVING NOTIFICATION - THIS IS FOR EDIT BUTTON CLICKED FROM NOTIFICATION ACTION
						try {
							NotificationManager mNotificationManager = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
							mNotificationManager.cancel(receivedReminderId);
						}
						catch (Exception e) {
							
						}

						editedReminder = rm.getReminderFromDB(receivedReminderId);
						action = "EDIT";

                        //change title to Edit Reminder
                        setActionBarTitle(R.string.edit_reminder);
						
						//remove received id - this should fix a bug that caused edited time or description to be reset when coming back to activity
						getIntent().removeExtra("reminder_id");
						
						editTextDescription.setText(editedReminder.getDescription());					
						currentDateAndTime.setToNow();	
						
						//get recurring frequency choice
						recurringFrequencySelectedPosition = editedReminder.getRecurrencePosition();
						recurringFrequencySpinner.setSelection(recurringFrequencySelectedPosition);

                        //also need to update current time and current date variables, so that the pickers show correct time and date
                        final Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(editedReminder.getTimestamp());

                        //keep time same as it was in reminder
                        requestedDateAndTime.setHourInt(cal.get(Calendar.HOUR_OF_DAY));
                        requestedDateAndTime.setMinuteInt(cal.get(Calendar.MINUTE));

                        //reminder is for future time keep the date
                        if (editedReminder.getTimestamp() > currentDateAndTime.getTimestamp()) {
                            requestedDateAndTime.setYearInt(cal.get(Calendar.YEAR));
                            requestedDateAndTime.setMonthInt(cal.get(Calendar.MONTH));
                            requestedDateAndTime.setDayInt(cal.get(Calendar.DAY_OF_MONTH));

                            updateTimeAndDateDisplay(requestedDateAndTime.getTimeString(), requestedDateAndTime.getDateString());
                        }
                        //reminder is old, change the date to today
                        else {
                            currentDateAndTime.setHourInt(requestedDateAndTime.getHourInt());
                            currentDateAndTime.setMinuteInt(requestedDateAndTime.getMinuteInt());

                            //update time and date in visible fields
                            updateTimeAndDateDisplay(currentDateAndTime.getTimeString(), currentDateAndTime.getDateString());
                        }
					}
				}
				catch (Exception e) {
				}
			}
			catch (Exception e) {
			}
		}
	}

    @Override
    protected void onResume() {
        super.onResume();
        //make sure that pickers are as user's chose them
        pickersCheckboxPreference = userSettings.getBoolean("use_pickers_preference_checkbox", true);
        //Open soft keyboard when coming back from other activities
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    //PRIVATE METHODS

    //SAVE REMINDER part 1
    private void saveReminder() {
    	/*
	     * [][][][][][][][][][][][][][][][][][][][][][][][]
	     * OLD REMINDERS
	     * [][][][][][][][][][][][][][][][][][][][][][][][]
	     */
	    if (action == "EDIT") {
	    	rm.deleteReminder(editedReminder.getId());
	    	Toast.makeText(applicationContext, "Reminder updated", Toast.LENGTH_SHORT).show();
	    }
		
		
	    /*
	     * [][][][][][][][][][][][][][][][][][][][][][][][]
	     * NEW REMINDERS
	     * [][][][][][][][][][][][][][][][][][][][][][][][]
	     */
		
		//set time stamp for requested time - at this point we have all info required to do this (date and time has been set)
		requestedDateAndTime.setTimestamp();
		
		/*
		 * get recurring frequency. IDs: 
		 * 0 = none
		 * 1 = daily
		 * 2 = weekly
		 * 3 = monthly
		 * 4 = annually
		 * 5 = weekdays
		 * 6 = weekends
		 */
		 
		recurringFrequencySelectedPosition = recurringFrequencySpinner.getSelectedItemPosition();

        //check if weekdays or weekend reminder
        if (recurringFrequencySelectedPosition == 5 || recurringFrequencySelectedPosition == 6) {
            //check if starting date is either Sat or Sun
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(requestedDateAndTime.getTimestamp());

            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

            //check if need to build and show dialog
            boolean isWeekdayConflict = false;
            boolean isWeekendConflict = false;

            if ((recurringFrequencySelectedPosition == 5) && (dayOfWeek == 7 || dayOfWeek == 1)) {
                isWeekdayConflict = true;
            }
            else if ((recurringFrequencySelectedPosition == 6) && (dayOfWeek == 2 || dayOfWeek == 3 || dayOfWeek == 4 || dayOfWeek == 5 || dayOfWeek == 6)) {
                isWeekendConflict = true;
            }


            if (isWeekdayConflict || isWeekendConflict) {
                //show dialog asking if this is OK or if they want to change
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme));
                builder.setIcon(R.drawable.nice_menu_info)
                        .setTitle(R.string.recurrence_conflict_title)
                        .setCancelable(false)
                        .setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                finalizeSaving();
                            }
                        })
                        .setNegativeButton(R.string.change_date, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                showDatePickerDialog(null);
                            }
                        });

                if (isWeekdayConflict) {
                    builder.setMessage(R.string.recurrence_conflict_weekdays);
                }
                if (isWeekendConflict) {
                    builder.setMessage(R.string.recurrence_conflict_weekends);
                }

                builder.create().show();
            }
            else {
                finalizeSaving();
            }
        }

        else {
            finalizeSaving();
        }
    }

    /**
     * this is second portion of saving reminder method, I had to split it up to be able to either
     * finish or cancel saving in case of recurrence conflict
     */
    //SAVE REMINDER part 2
    private void finalizeSaving() {
        //prepare blank object
        Reminder reminder = new Reminder();

        //generate integer of current time in milliseconds that will become alarmId
        int alarmId = (int)Calendar.getInstance().getTimeInMillis();

        //set properties of the reminder
        reminder.setAlarmId(alarmId);

        String description = "";
        //build description
        if (editTextDescription.getText().toString().isEmpty()) {
            description = "[No Description]";
        }
        else {
            description = editTextDescription.getText().toString();
        }

        reminder.setDescription(description);
        reminder.setTime(requestedDateAndTime.getTimestamp(), applicationContext);//this is time string to be used just for display, not calculations
        reminder.setDate(requestedDateAndTime.getTimestamp(), recurringFrequencySelectedPosition, applicationContext);//this is date string to be used just for display, not calculations
        reminder.setTimestamp(requestedDateAndTime.getTimestamp());
        reminder.setStatus(1);//status 0 = expired, status 1 = active
        reminder.setRecurrencePosition(recurringFrequencySelectedPosition);
        reminder.setOriginalTimestamp(requestedDateAndTime.getTimestamp());

        //utilize RM to add reminder to database and set its alarm
        rm.insertReminderIntoDbAndSetAlarm(reminder);

        //clear text fields
        resetAll();

        //show list of existing reminders
        showReminders();
    }
    
    private void showReminders() {
    	Intent listRemindersIntent = new Intent(NewReminderActivity.this, ListRemindersActivity.class);
        listRemindersIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	NewReminderActivity.this.startActivity(listRemindersIntent);
    }
    
    private void showSettings() {
    	Intent settingsIntent = new Intent(applicationContext, SettingsActivity.class);
    	startActivity(settingsIntent);
    }
    
    private void showHelp() {
		Intent showHelpIntent = new Intent(NewReminderActivity.this, HelpActivity.class);
    	NewReminderActivity.this.startActivity(showHelpIntent);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		//clean up
		finish();
	}

}


