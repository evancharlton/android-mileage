package com.evancharlton.mileage;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsView extends PreferenceActivity {
	public static final String DATE = "date_pref";
	public static final String NUMBER = "number_pref";
	public static final String CURRENCY = "currency_pref";
	public static final String VOLUME = "volume_pref";
	public static final String DISTANCE = "distance_pref";
	public static final String ECONOMY = "economy_pref";
	public static final String ORIENTATION = "orientation_pref";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.settings);
	}
}
