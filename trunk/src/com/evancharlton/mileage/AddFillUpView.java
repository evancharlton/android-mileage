package com.evancharlton.mileage;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

public class AddFillUpView extends Activity {
	public static final String PACKAGE = "com.evancharlton.mileage";
	public static final int DATE_DIALOG_ID = 0;
	public static final int MENU_VEHICLES = Menu.FIRST;
	public static final int MENU_SETTINGS = Menu.FIRST + 1;

	protected int m_year;
	protected int m_month;
	protected int m_day;

	protected Button m_customDateButton;
	protected Button m_saveButton;
	protected EditText m_priceEdit;
	protected EditText m_amountEdit;
	protected EditText m_mileageEdit;
	protected Spinner m_vehicleSpinner;
	protected DatePickerDialog m_dateDlg = null;
	protected EditText m_commentEdit;
	protected SimpleCursorAdapter m_vehicleAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Calendar c = Calendar.getInstance();
		m_year = c.get(Calendar.YEAR);
		m_month = c.get(Calendar.MONTH);
		m_day = c.get(Calendar.DAY_OF_MONTH);

		setContentView(R.layout.fillup);

		loadData();
		initHandlers();
		updateDate();
		loadPrefs();
	}

	@Override
	public void onResume() {
		super.onResume();
		updateDate();
		loadPrefs();
	}

	protected void loadData() {
		m_saveButton = (Button) findViewById(R.id.save_btn);
		m_customDateButton = (Button) findViewById(R.id.change_date_btn);
		m_mileageEdit = (EditText) findViewById(R.id.odometer_edit);
		m_amountEdit = (EditText) findViewById(R.id.amount_edit);
		m_priceEdit = (EditText) findViewById(R.id.price_edit);
		m_commentEdit = (EditText) findViewById(R.id.comment_edit);
		m_vehicleSpinner = (Spinner) findViewById(R.id.vehicle_spinner);

		Cursor c = managedQuery(Vehicles.CONTENT_URI, new String[] {
				Vehicles._ID,
				Vehicles.TITLE
		}, null, null, Vehicles.DEFAULT_SORT_ORDER);
		m_vehicleAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, c, new String[] {
			Vehicles.TITLE
		}, new int[] {
			android.R.id.text1
		});
		m_vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		m_vehicleSpinner.setAdapter(m_vehicleAdapter);

		if (m_vehicleAdapter.getCount() == 1) {
			m_vehicleSpinner.setVisibility(View.GONE);
		}

	}

	protected void loadPrefs() {
		PreferencesProvider prefs = PreferencesProvider.getInstance(AddFillUpView.this);
		m_priceEdit.setHint(prefs.getString(R.array.unit_price_hints, SettingsView.VOLUME));
		m_amountEdit.setHint(prefs.getString(R.array.unit_amount_hints, SettingsView.VOLUME));
	}

	protected void initHandlers() {
		m_saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// save the new fill-up
				ContentValues values = saveData();
				if (values == null) {
					return;
				}

				getContentResolver().insert(FillUps.CONTENT_URI, values);
				resetForm(v);
			}
		});

		m_customDateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// show a date picker
				showDialog(DATE_DIALOG_ID);
			}
		});

		m_priceEdit.requestFocus();

		// m_priceEdit.setKeyListener(new KeyFocuser(m_amountEdit));
		// m_amountEdit.setKeyListener(new KeyFocuser(m_mileageEdit));
		// m_mileageEdit.setKeyListener(new KeyFocuser(m_commentEdit));
		// m_commentEdit.setKeyListener(new KeyFocuser(m_saveButton));
	}

	private void resetForm(View v) {
		m_amountEdit.setText("");
		m_priceEdit.setText("");
		m_mileageEdit.setText("");
		m_commentEdit.setText("");

		getIntent().setData(FillUps.CONTENT_URI);
	}

	protected void updateDate() {
		GregorianCalendar gc = new GregorianCalendar(m_year, m_month, m_day);
		Date d = new Date(gc.getTimeInMillis());

		m_customDateButton.setText(PreferencesProvider.getInstance(AddFillUpView.this).format(d));
		if (m_dateDlg != null) {
			m_dateDlg.updateDate(m_year, m_month, m_day);
		}
	}

	protected ContentValues saveData() {
		ContentValues values = new ContentValues();
		boolean error = false;
		int errorMsg = 0;

		long vehicleId = m_vehicleSpinner.getSelectedItemId();

		try {
			double cost = Double.parseDouble(m_priceEdit.getText().toString());
			values.put(FillUps.COST, cost);
		} catch (NumberFormatException nfe) {
			error = true;
			errorMsg = R.string.error_cost;
		}

		try {
			double amount = Double.parseDouble(m_amountEdit.getText().toString());
			values.put(FillUps.AMOUNT, amount);
		} catch (NumberFormatException nfe) {
			error = true;
			errorMsg = R.string.error_amount;
		}

		try {
			String miles = m_mileageEdit.getText().toString().trim();
			double mileage = 0.0D;
			if (miles.startsWith("+")) {
				// we have an incremental mileage
				String[] projection = new String[] {
						FillUps._ID,
						FillUps.MILEAGE
				};
				Cursor c = managedQuery(FillUps.CONTENT_URI, projection, FillUps.VEHICLE_ID + " = ?", new String[] {
					String.valueOf(vehicleId)
				}, FillUps.DEFAULT_SORT_ORDER);
				if (c.getCount() > 0) {
					c.moveToFirst();
					double previous_miles = c.getDouble(1);
					mileage = Double.parseDouble(miles.substring(1)) + previous_miles;
				}
			} else {
				mileage = Double.parseDouble(miles);
			}
			values.put(FillUps.MILEAGE, mileage);
		} catch (NumberFormatException nfe) {
			error = true;
			errorMsg = R.string.error_mileage;
		}

		LocationManager location = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean locationStored = false;
		if (location != null) {
			Location loc = location.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (loc != null) {
				values.put(FillUps.LATITUDE, loc.getLatitude());
				values.put(FillUps.LONGITUDE, loc.getLongitude());
				locationStored = true;
			}
		}
		if (!locationStored) {
			values.put(FillUps.LATITUDE, 0D);
			values.put(FillUps.LONGITUDE, 0D);
		}

		if (error) {
			AlertDialog dlg = new AlertDialog.Builder(AddFillUpView.this).create();
			dlg.setTitle(R.string.error);
			dlg.setMessage(getString(errorMsg));
			dlg.show();
			return null;
		}

		values.put(FillUps.COMMENT, m_commentEdit.getText().toString().trim());
		values.put(FillUps.VEHICLE_ID, vehicleId);

		Calendar c = new GregorianCalendar(m_year, m_month, m_day);
		values.put(FillUps.DATE, c.getTimeInMillis());
		return values;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		Mileage.createMenu(menu);
		HelpDialog.injectHelp(menu, 'h');

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = Mileage.parseMenuItem(item, this);
		if (ret) {
			return true;
		}
		switch (item.getItemId()) {
			case HelpDialog.MENU_HELP:
				HelpDialog.create(this, R.string.help_title_fillup_new, R.string.help_fillup_new);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
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