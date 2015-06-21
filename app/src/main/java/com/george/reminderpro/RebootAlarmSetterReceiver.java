package com.george.reminderpro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RebootAlarmSetterReceiver extends BroadcastReceiver {

	private ReminderManager rm;
	
	@Override
	public void onReceive(Context context, Intent intent) {			
		rm = new ReminderManager(context);
		rm.resetAlarmsForAllRemindersInDB();
	}

}
