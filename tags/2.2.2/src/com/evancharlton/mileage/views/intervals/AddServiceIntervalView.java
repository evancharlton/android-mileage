package com.evancharlton.mileage.views.intervals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.evancharlton.mileage.HelpDialog;
import com.evancharlton.mileage.PreferencesProvider;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.binders.VehicleBinder;
import com.evancharlton.mileage.models.ServiceInterval;
import com.evancharlton.mileage.models.Vehicle;
import com.evancharlton.mileage.models.defaults.PresetServiceInterval;

public class AddServiceIntervalView extends Activity {
	protected Button m_saveBtn;
	protected Button m_dateBtn;
	protected Spinner m_presetSpinner;
	protected Spinner m_vehicleSpinner;
	protected Spinner m_durationUnitsSpinner;
	protected EditText m_odometerEdit;
	protected EditText m_distanceEdit;
	protected EditText m_durationEdit;
	protected EditText m_descriptionEdit;

	protected DatePickerDialog m_dateDlg = null;

	protected Calendar m_startDate;
	protected int m_year;
	protected int m_month;
	protected int m_day;

	protected static final int DATE_DIALOG_ID = 1;
	protected static final String PRESET_POS = "preset_position";
	protected static final String VEHICLE_POS = "vehicle_position";
	protected static final String ODOMETER = "odometer";
	protected static final String START_DATE = "start_date";
	protected static final String DISTANCE = "distance";
	protected static final String DURATION = "duration";
	protected static final String DURATION_UNITS = "duration_units";
	protected static final String DESCRIPTION = "description";
	private static final long MONTH;

	protected static ArrayList<PresetServiceInterval> PRESETS = new ArrayList<PresetServiceInterval>();

	static {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(0);
		cal.add(Calendar.MONTH, 1);
		MONTH = cal.getTimeInMillis();
		cal = null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (PRESETS.size() == 0) {
			PRESETS.add(new PresetServiceInterval(0, 0, getString(R.string.preset_custom)));
			PRESETS.add(new PresetServiceInterval(3000, MONTH * 3, getString(R.string.preset_oil_change_standard)));
			PRESETS.add(new PresetServiceInterval(10000, MONTH * 10, getString(R.string.preset_oil_change_synthetic)));
			PRESETS.add(new PresetServiceInterval(15000, MONTH * 15, getString(R.string.preset_air_filter)));
			PRESETS.add(new PresetServiceInterval(30000, MONTH * 30, getString(R.string.preset_power_steering)));
			PRESETS.add(new PresetServiceInterval(25000, MONTH * 25, getString(R.string.preset_transmission_fluid)));
			PRESETS.add(new PresetServiceInterval(60000, MONTH * 60, getString(R.string.preset_timing_belt)));
			PRESETS.add(new PresetServiceInterval(25000, MONTH * 25, getString(R.string.preset_fuel_filter)));
		}

		setContentView(R.layout.interval);

		initUI();

		Bundle data = (Bundle) getLastNonConfigurationInstance();
		if (data != null) {
			m_presetSpinner.setSelection(data.getInt(PRESET_POS, 0));
			m_vehicleSpinner.setSelection(data.getInt(VEHICLE_POS, 0));
			m_odometerEdit.setText(data.getString(ODOMETER));
			m_startDate.setTimeInMillis(data.getLong(START_DATE, System.currentTimeMillis()));
			m_distanceEdit.setText(data.getString(DISTANCE));
			m_durationEdit.setText(data.getString(DURATION));
			m_durationUnitsSpinner.setSelection(data.getInt(DURATION_UNITS, 0));
			m_descriptionEdit.setText(data.getString(DESCRIPTION));

			m_month = m_startDate.get(Calendar.MONTH);
			m_year = m_startDate.get(Calendar.YEAR);
			m_day = m_startDate.get(Calendar.DAY_OF_MONTH);

			updateDate();
		}
	}

	public void onResume() {
		super.onResume();
		buildVehicleSpinner();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		Bundle data = new Bundle();

		data.putInt(PRESET_POS, m_presetSpinner.getSelectedItemPosition());
		data.putInt(VEHICLE_POS, m_vehicleSpinner.getSelectedItemPosition());
		data.putString(ODOMETER, m_odometerEdit.getText().toString());
		data.putLong(START_DATE, m_startDate.getTimeInMillis());
		data.putString(DISTANCE, m_distanceEdit.getText().toString());
		data.putString(DURATION, m_durationEdit.getText().toString());
		data.putInt(DURATION_UNITS, m_durationUnitsSpinner.getSelectedItemPosition());
		data.putString(DESCRIPTION, m_descriptionEdit.getText().toString());

		return data;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		HelpDialog.injectHelp(menu, 'h');

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case HelpDialog.MENU_HELP:
				HelpDialog.create(this, R.string.help_title_add_service_interval, R.string.help_add_service_interval);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void initUI() {
		m_saveBtn = (Button) findViewById(R.id.save_btn);
		m_dateBtn = (Button) findViewById(R.id.date_btn);

		m_presetSpinner = (Spinner) findViewById(R.id.presets);
		m_vehicleSpinner = (Spinner) findViewById(R.id.vehicles);
		m_durationUnitsSpinner = (Spinner) findViewById(R.id.duration_units);

		m_odometerEdit = (EditText) findViewById(R.id.odometer);
		m_distanceEdit = (EditText) findViewById(R.id.distance);
		m_durationEdit = (EditText) findViewById(R.id.duration);
		m_descriptionEdit = (EditText) findViewById(R.id.description);

		// set up the handlers
		m_saveBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				ServiceInterval interval = setData();
				if (interval == null) {
					showMessage(false);
					return;
				}
				interval.save();
				interval.scheduleAlarm(AddServiceIntervalView.this);
				finish();

				showMessage(true);
			}
		});

		m_dateBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				showDialog(DATE_DIALOG_ID);
			}
		});

		// populate the preset spinner
		ArrayAdapter<PresetServiceInterval> presetsAdapter = new ArrayAdapter<PresetServiceInterval>(this, android.R.layout.simple_spinner_item, PRESETS);
		presetsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		m_presetSpinner.setAdapter(presetsAdapter);

		m_presetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
				PresetServiceInterval preset = PRESETS.get(position);
				m_distanceEdit.setText(String.valueOf(preset.getDistance()));
				setDuration(preset.getDuration());
				m_descriptionEdit.setText(preset.getDescription());
			}

			public void onNothingSelected(AdapterView<?> adapter) {
			}
		});

		Calendar c = Calendar.getInstance();
		m_year = c.get(Calendar.YEAR);
		m_month = c.get(Calendar.MONTH);
		m_day = c.get(Calendar.DAY_OF_MONTH);

		updateDate();
	}

	protected ServiceInterval setData() {
		ServiceInterval interval = new ServiceInterval();

		interval.setCreateDate(m_startDate);
		try {
			interval.setCreateOdometer(Double.parseDouble(m_odometerEdit.getText().toString()));
			interval.setDistance(Double.parseDouble(m_distanceEdit.getText().toString()));

			int duration = Integer.parseInt(m_durationEdit.getText().toString());
			String[] units = getResources().getStringArray(R.array.duration_units);
			int position = m_durationUnitsSpinner.getSelectedItemPosition();
			if (position >= 0 && position < units.length) {
				String unit = units[position];
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(0);
				int i = -1;
				if (unit.equalsIgnoreCase("Days")) {
					i = Calendar.DAY_OF_MONTH;
				} else if (unit.equalsIgnoreCase("Weeks")) {
					i = Calendar.WEEK_OF_MONTH;
				} else if (unit.equalsIgnoreCase("Months")) {
					i = Calendar.MONTH;
				} else if (unit.equalsIgnoreCase("Years")) {
					i = Calendar.YEAR;
				} else if (unit.equalsIgnoreCase("Seconds")) { // testing only!
					i = Calendar.SECOND;
				}
				cal.add(i, duration);
				interval.setDuration(cal.getTimeInMillis());
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}

		interval.setDescription(m_descriptionEdit.getText().toString());
		interval.setVehicleId(m_vehicleSpinner.getSelectedItemId());

		return interval;
	}

	private void buildVehicleSpinner() {
		Cursor vehicleCursor = managedQuery(Vehicle.CONTENT_URI, Vehicle.getProjection(), null, null, Vehicle.DEFAULT_SORT_ORDER);
		SimpleCursorAdapter vehicleAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, vehicleCursor, new String[] {
			Vehicle.TITLE
		}, new int[] {
			android.R.id.text1
		});
		vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		vehicleAdapter.setViewBinder(new VehicleBinder());
		m_vehicleSpinner.setAdapter(vehicleAdapter);

		if (vehicleCursor.getCount() == 1) {
			m_vehicleSpinner.setVisibility(View.GONE);
			((TextView) findViewById(R.id.vehicle_header)).setVisibility(View.GONE);
		}
	}

	protected void updateDate() {
		m_startDate = new GregorianCalendar(m_year, m_month, m_day);
		Date d = new Date(m_startDate.getTimeInMillis());

		m_dateBtn.setText(PreferencesProvider.getInstance(AddServiceIntervalView.this).format(d));
		if (m_dateDlg != null) {
			m_dateDlg.updateDate(m_year, m_month, m_day);
		}
	}

	protected void setDuration(final long duration) {
		long quotient = 0L;

		// get how long each interval is, rather than doing the calculations
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(0);
		cal.add(Calendar.YEAR, 1);
		final long DURATION_YEAR = cal.getTimeInMillis();
		cal.setTimeInMillis(0);
		cal.add(Calendar.MONTH, 1);
		final long DURATION_MONTH = cal.getTimeInMillis();
		cal.setTimeInMillis(0);
		cal.add(Calendar.WEEK_OF_MONTH, 1);
		final long DURATION_WEEK = cal.getTimeInMillis();
		cal.setTimeInMillis(0);
		cal.add(Calendar.DAY_OF_MONTH, 1);
		final long DURATION_DAY = cal.getTimeInMillis();

		int pos = 0;
		quotient = duration / DURATION_YEAR;
		if (quotient * DURATION_YEAR == duration) {
			pos = 3;
		} else {
			quotient = duration / DURATION_MONTH;
			if (quotient * DURATION_MONTH == duration) {
				pos = 2;
			} else {
				quotient = duration / DURATION_WEEK;
				if (quotient * DURATION_WEEK == duration) {
					pos = 1;
				} else {
					quotient = duration / DURATION_DAY;
					if (quotient * DURATION_DAY == duration) {
						pos = 0;
					} else {
						// TODO: unknown units
					}
				}
			}
		}
		m_durationEdit.setText(String.valueOf(quotient));
		m_durationUnitsSpinner.setSelection(pos);
	}

	protected void showMessage(boolean success) {
		if (success) {
			Toast.makeText(AddServiceIntervalView.this, R.string.service_interval_saved, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, getString(R.string.service_interval_error_saving), Toast.LENGTH_SHORT).show();
		}
	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DATE_DIALOG_ID:
				if (m_dateDlg == null) {
					m_dateDlg = new DatePickerDialog(this, m_dateSetListener, m_year, m_month, m_day);
					m_dateDlg.updateDate(m_year, m_month, m_day);
					m_dateDlg.setButton3(getString(R.string.today), m_todayListener);
				}
				return m_dateDlg;
		}
		return null;
	}

	protected DialogInterface.OnClickListener m_todayListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface intf, int which) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis());
			m_year = cal.get(Calendar.YEAR);
			m_month = cal.get(Calendar.MONTH);
			m_day = cal.get(Calendar.DAY_OF_MONTH);
			updateDate();
		}
	};

	protected DatePickerDialog.OnDateSetListener m_dateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int month, int day) {
			m_year = year;
			m_month = month;
			m_day = day;
			updateDate();
		}
	};
}
