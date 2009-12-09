package com.evancharlton.mileage;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.evancharlton.mileage.models.FillUp;

public class SettingsView extends PreferenceActivity {
	private static final int MENU_WIPE_LOCATION_DATA = 10;
	private static final int DIALOG_DELETE = 10;
	private static final int DIALOG_WIPED = 11;

	protected WipeLocationData m_task;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.layout.settings);

		m_task = (WipeLocationData) getLastNonConfigurationInstance();
		if (m_task == null) {
			m_task = new WipeLocationData();
		}
		m_task.activity = this;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_WIPE_LOCATION_DATA, Menu.NONE, R.string.settings_erase_location_data);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_WIPE_LOCATION_DATA:
				showDialog(DIALOG_DELETE);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(final int id) {
		switch (id) {
			case DIALOG_DELETE:
				return new AlertDialog.Builder(this).setMessage(R.string.confirm_erase_location).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						removeDialog(id);
						m_task.execute();
					}
				}).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						removeDialog(id);
					}
				}).create();
			case DIALOG_WIPED:
				return new AlertDialog.Builder(this).setTitle(R.string.deleted).setMessage(R.string.location_data_erased).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						removeDialog(id);
					}
				}).create();
		}
		return super.onCreateDialog(id);
	}

	private static class WipeLocationData extends AsyncTask<String, Integer, Boolean> {
		public SettingsView activity;

		@Override
		protected Boolean doInBackground(String... params) {
			ContentValues values = new ContentValues();
			values.put(FillUp.LATITUDE, 0);
			values.put(FillUp.LONGITUDE, 0);
			int i = activity.getContentResolver().update(FillUp.CONTENT_URI, values, "", new String[] {});
			return i > 0;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				activity.showDialog(DIALOG_WIPED);
			}
		}
	}
}