package com.evancharlton.mileage;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

public class SettingsView extends Activity {
	public static final String DATE = "date_pref";
	public static final String NUMBER = "number_pref";
	public static final String CURRENCY = "currency_pref";
	public static final String VOLUME = "volume_pref";
	public static final String DISTANCE = "distance_pref";
	public static final String ECONOMY = "economy_pref";

	private Button m_saveBtn;
	private Spinner m_dateSpinner;
	private Spinner m_currencySpinner;
	private Spinner m_volumeSpinner;
	private Spinner m_distanceSpinner;
	private Spinner m_economySpinner;

	// private Spinner m_numberSpinner;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		initUI();
	}

	private void initUI() {
		m_saveBtn = (Button) findViewById(R.id.settings_save_changes);
		m_dateSpinner = (Spinner) findViewById(R.id.settings_date_format);
		m_currencySpinner = (Spinner) findViewById(R.id.settings_currency);
		m_volumeSpinner = (Spinner) findViewById(R.id.settings_volume);
		m_distanceSpinner = (Spinner) findViewById(R.id.settings_distance);
		m_economySpinner = (Spinner) findViewById(R.id.settings_economy);
		// m_numberSpinner = (Spinner)
		// findViewById(R.id.settings_number_format);

		m_saveBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				PreferencesProvider prefs = PreferencesProvider.getInstance(SettingsView.this);
				prefs.write(SettingsView.DATE, m_dateSpinner.getSelectedItemPosition());
				prefs.write(SettingsView.CURRENCY, m_currencySpinner.getSelectedItemPosition());
				// prefs.write(SettingsView.NUMBER,
				// m_numberSpinner.getSelectedItemPosition());
				finish();
			}
		});

		PreferencesProvider prefs = PreferencesProvider.getInstance(SettingsView.this);
		m_dateSpinner.setSelection(prefs.getInt(DATE, 0));
		m_currencySpinner.setSelection(prefs.getInt(CURRENCY, 0));
		m_volumeSpinner.setSelection(prefs.getInt(VOLUME, 0));
		m_distanceSpinner.setSelection(prefs.getInt(DISTANCE, 0));
		m_economySpinner.setSelection(prefs.getInt(ECONOMY, 0));
		// m_numberSpinner.setSelection(prefs.getInt(NUMBER, 0));
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		HelpDialog.injectHelp(menu, 'h');
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case HelpDialog.MENU_HELP:
				HelpDialog.create(this, R.string.help_title_settings, R.string.help_settings);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
