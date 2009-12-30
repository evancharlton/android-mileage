package com.evancharlton.mileage.views.intervals;

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.evancharlton.mileage.HelpDialog;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.models.ServiceInterval;

public class EditServiceIntervalView extends AddServiceIntervalView {
	private AlertDialog m_deleteDialog;

	private static final int MENU_DELETE = Menu.FIRST + 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.edit_service_interval);
	}

	protected void loadData() {
		long id = Long.parseLong(getIntent().getData().getLastPathSegment());
		ServiceInterval interval = new ServiceInterval(id);

		setDuration(interval.getDuration());

		m_odometerEdit.setText(String.valueOf(interval.getCreateOdometer()));
		m_distanceEdit.setText(String.valueOf(interval.getDistance()));
		m_descriptionEdit.setText(interval.getDescription());

		// date
		m_startDate = interval.getCreateDate();
		m_year = m_startDate.get(Calendar.YEAR);
		m_month = m_startDate.get(Calendar.MONTH);
		m_day = m_startDate.get(Calendar.DAY_OF_MONTH);
		updateDate();
	}

	protected void initUI() {
		super.initUI();

		m_saveBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				long id = Long.parseLong(getIntent().getData().getLastPathSegment());
				ServiceInterval interval = setData();
				if (interval == null) {
					showMessage(false);
					return;
				}
				interval.setId(id);
				interval.save();
				interval.scheduleAlarm(EditServiceIntervalView.this);
				finish();

				showMessage(true);
			}
		});

		// deactivate and hide the preset spinner since it's not relevant
		m_presetSpinner.setEnabled(false);
		m_presetSpinner.setOnItemSelectedListener(null);
		m_presetSpinner.setVisibility(View.GONE);
		((TextView) findViewById(R.id.presets_header)).setVisibility(View.GONE);

		m_deleteDialog = new AlertDialog.Builder(this).create();
		m_deleteDialog.setMessage(getString(R.string.confirm_delete));
		m_deleteDialog.setCancelable(false);
		m_deleteDialog.setButton(getString(android.R.string.yes), m_deleteListener);
		m_deleteDialog.setButton2(getString(android.R.string.no), m_deleteListener);

		loadData();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, R.string.delete).setShortcut('1', 'd').setIcon(R.drawable.ic_menu_delete);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_DELETE:
				m_deleteDialog.show();
				return true;
			case HelpDialog.MENU_HELP:
				HelpDialog.create(this, R.string.help_title_edit_service_interval, R.string.help_edit_service_interval);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void delete() {
		long id = Long.parseLong(getIntent().getData().getLastPathSegment());
		ServiceInterval interval = new ServiceInterval(id);
		interval.cancelAlarm(this);
		getContentResolver().delete(getIntent().getData(), null, null);
	}

	private DialogInterface.OnClickListener m_deleteListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			if (which == Dialog.BUTTON1) {
				delete();
				finish();
			}
		}
	};
}
