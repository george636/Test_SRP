package com.george.reminderpro;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment
implements DatePickerDialog.OnDateSetListener {

	NewReminderActivity nr;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		nr = (NewReminderActivity)getActivity();

		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(), this, nr.requestedDateAndTime.getYearInt(), nr.requestedDateAndTime.getMonthInt(), nr.requestedDateAndTime.getDayInt());
	}

	public void onDateSet(DatePicker view, int year, int month, int day) {
		nr.requestedDateAndTime.setYearInt(year);
		nr.requestedDateAndTime.setMonthInt(month);
		nr.requestedDateAndTime.setDayInt(day);
		
		String dateString = nr.requestedDateAndTime.getDateString();
		nr.updateDateDisplay(dateString);
	}
}