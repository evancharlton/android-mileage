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
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
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

public class Mileage extends Activity {
	public static final String PACKAGE = "com.evancharlton.mileage";
	public static final int DATE_DIALOG_ID = 0;
	public static final int MENU_HISTORY = Menu.FIRST;
	public static final int MENU_VEHICLES = Menu.FIRST + 1;
	public static final int MENU_STATISTICS = Menu.FIRST + 2;
	public static final int MENU_SETTINGS = Menu.FIRST + 3;

	private int m_year;
	private int m_month;
	private int m_day;

	private Button m_customDateButton;
	private Button m_saveButton;
	private EditText m_priceEdit;
	private EditText m_amountEdit;
	private EditText m_mileageEdit;
	private Spinner m_vehicleSpinner;
	private DatePickerDialog m_dateDlg = null;
	private EditText m_commentEdit;
	private Button m_statisticsBtn;
	private Button m_historyBtn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Calendar c = Calendar.getInstance();
		m_year = c.get(Calendar.YEAR);
		m_month = c.get(Calendar.MONTH);
		m_day = c.get(Calendar.DAY_OF_MONTH);

		setContentView(R.layout.main);

		initData();
		initHandlers();
		updateDate();
	}

	@Override
	public void onResume() {
		super.onResume();
		updateDate();
	}

	private void initData() {
		m_saveButton = (Button) findViewById(R.id.add_fillup_btn);
		m_customDateButton = (Button) findViewById(R.id.change_date_btn);
		m_mileageEdit = (EditText) findViewById(R.id.odometer_edit);
		m_amountEdit = (EditText) findViewById(R.id.amount_edit);
		m_priceEdit = (EditText) findViewById(R.id.price_edit);
		m_commentEdit = (EditText) findViewById(R.id.comment);
		m_vehicleSpinner = (Spinner) findViewById(R.id.vehicle_spinner);
		m_historyBtn = (Button) findViewById(R.id.history_btn);
		m_statisticsBtn = (Button) findViewById(R.id.statistics_btn);

		Cursor c = managedQuery(Vehicles.CONTENT_URI, new String[] {
				Vehicles._ID,
				Vehicles.TITLE
		}, null, null, Vehicles.DEFAULT_SORT_ORDER);
		SimpleCursorAdapter vehicleAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, c, new String[] {
			Vehicles.TITLE
		}, new int[] {
			android.R.id.text1
		});
		vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		m_vehicleSpinner.setAdapter(vehicleAdapter);

	}

	private void initHandlers() {
		m_saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// save the new fill-up
				ContentValues values = new ContentValues();
				boolean error = false;
				int errorMsg = 0;

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
					double mileage = Double.parseDouble(m_mileageEdit.getText().toString());
					values.put(FillUps.MILEAGE, mileage);
				} catch (NumberFormatException nfe) {
					error = true;
					errorMsg = R.string.error_mileage;
				}

				LocationManager location = (LocationManager) getSystemService(LOCATION_SERVICE);
				boolean locationStored = false;
				if (location != null) {
					Criteria criteria = new Criteria();
					criteria.setSpeedRequired(true);
					criteria.setAccuracy(Criteria.ACCURACY_COARSE);
					String provider = location.getBestProvider(criteria, true);
					if (provider != null) {
						Location loc = location.getLastKnownLocation(provider);
						if (loc != null) {
							values.put(FillUps.LATITUDE, loc.getLatitude());
							values.put(FillUps.LONGITUDE, loc.getLongitude());
							locationStored = true;
						}
					}
				}
				if (locationStored) {
					values.put(FillUps.LATITUDE, 0D);
					values.put(FillUps.LONGITUDE, 0D);
				}

				if (error) {
					AlertDialog dlg = new AlertDialog.Builder(Mileage.this).create();
					dlg.setTitle(R.string.error);
					dlg.setMessage(getString(errorMsg));
					dlg.show();
					return;
				}

				values.put(FillUps.COMMENT, m_commentEdit.getText().toString().trim());
				values.put(FillUps.VEHICLE_ID, m_vehicleSpinner.getSelectedItemId());

				Calendar c = new GregorianCalendar(m_year, m_month, m_day);
				values.put(FillUps.DATE, c.getTimeInMillis());

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

		m_historyBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showHistory();
			}
		});

		m_statisticsBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showStatistics();
			}
		});

		PreferencesProvider prefs = PreferencesProvider.getInstance(Mileage.this);

		m_priceEdit.setHint(prefs.getString(R.array.unit_price_hints, SettingsView.CALCULATIONS));
		m_amountEdit.setHint(prefs.getString(R.array.unit_amount_hints, SettingsView.CALCULATIONS));

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

	private void updateDate() {
		GregorianCalendar gc = new GregorianCalendar(m_year, m_month, m_day);
		Date d = new Date(gc.getTimeInMillis());

		m_customDateButton.setText(PreferencesProvider.getInstance(Mileage.this).format(d));
		if (m_dateDlg != null) {
			m_dateDlg.updateDate(m_year, m_month, m_day);
		}
	}

	private void showHistory() {
		Intent i = new Intent();
		i.setClass(Mileage.this, HistoryView.class);
		startActivity(i);
	}

	private void showVehicles() {
		Intent i = new Intent();
		i.setClass(Mileage.this, VehiclesView.class);
		startActivity(i);
	}

	private void showStatistics() {
		Intent i = new Intent();
		i.setClass(Mileage.this, StatisticsView.class);
		startActivity(i);
	}

	private void showSettings() {
		Intent i = new Intent();
		i.setClass(Mileage.this, SettingsView.class);
		startActivity(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(Menu.NONE, MENU_HISTORY, 0, R.string.fillup_history).setShortcut('1', 'i');
		menu.add(Menu.NONE, MENU_VEHICLES, 0, R.string.vehicles).setShortcut('2', 'v');
		menu.add(Menu.NONE, MENU_STATISTICS, 0, R.string.statistics).setShortcut('3', 's');
		menu.add(Menu.NONE, MENU_SETTINGS, 0, R.string.settings).setShortcut('4', 'e');
		HelpDialog.injectHelp(menu, 'h');

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HISTORY:
				showHistory();
				break;
			case MENU_VEHICLES:
				showVehicles();
				break;
			case MENU_STATISTICS:
				showStatistics();
				break;
			case HelpDialog.MENU_HELP:
				HelpDialog.create(this, R.string.help_title_fillup_new, R.string.help_fillup_new);
				break;
			case MENU_SETTINGS:
				showSettings();
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

	private DialogInterface.OnClickListener m_todayListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface intf, int which) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis());
			m_year = cal.get(Calendar.YEAR);
			m_month = cal.get(Calendar.MONTH);
			m_day = cal.get(Calendar.DAY_OF_MONTH);
			updateDate();
		}
	};

	private DatePickerDialog.OnDateSetListener m_dateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int month, int day) {
			m_year = year;
			m_month = month;
			m_day = day;
			updateDate();
		}
	};
}