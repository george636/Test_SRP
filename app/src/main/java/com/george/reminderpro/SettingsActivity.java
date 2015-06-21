package com.george.reminderpro;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	public static final String KEY_PREF_REPETITION_CHECKBOX = "should_repeat_preference_checkbox";
	public static final String KEY_PREF_REPETITION_INTERVAL = "repetition_interval_preference";
	
	@SuppressWarnings("deprecation")
	@Override 
	@TargetApi(11)
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getFragmentManager().beginTransaction().replace(android.R.id.content,
				new SettingsFragment()).commit();
		}
		//for devices running API 9 and older
		else {
            //add main preferences
			addPreferencesFromResource(R.xml.preferences_main);

            //add spinners choice
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                addPreferencesFromResource(R.xml.preferences_pickers);
            }
        }
		
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // handle the preference change here
		if (key.equals(KEY_PREF_REPETITION_CHECKBOX) || key.equals(KEY_PREF_REPETITION_INTERVAL)) {
			//EXAMPLES
			//Preference connectionPref = findPreference(key);
            //connectionPref.setSummary(sharedPreferences.getString(key, ""));
			ReminderManager rm;
			rm = new ReminderManager(getApplicationContext());
			rm.resetAlarmsForAllRemindersInDB();
			
        }	
    }
}