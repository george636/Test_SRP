package com.george.reminderpro;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment
implements TimePickerDialog.OnTimeSetListener {
	
	NewReminderActivity nr;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		nr = (NewReminderActivity)getActivity();

		// Create a new instance of TimePickerDialog and return it
		return new TimePickerDialog(getActivity(), this, nr.requestedDateAndTime.getHourInt(), nr.requestedDateAndTime.getMinuteInt(),
		DateFormat.is24HourFormat(getActivity()));
	}
	
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		// Do something with the time chosen by the user	
		nr.requestedDateAndTime.setHourInt(hourOfDay);
		nr.requestedDateAndTime.setMinuteInt(minute);
		
		String timeString = nr.requestedDateAndTime.getTimeString();
		nr.updateTimeDisplay(timeString);
	}
}