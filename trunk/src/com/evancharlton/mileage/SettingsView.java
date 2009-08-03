package com.evancharlton.mileage;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsView extends PreferenceActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.layout.settings);
	}
}
