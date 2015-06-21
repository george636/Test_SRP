package com.george.reminderpro;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;

@TargetApi(11)
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences_main);

        //add spinners choice
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            addPreferencesFromResource(R.xml.preferences_pickers);
        }
    }

}
