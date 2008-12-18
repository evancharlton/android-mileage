package com.evancharlton.mileage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
import android.text.Editable;
import android.text.Selection;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

public class AddFillUpView extends Activity implements Persistent {
	public static final String PACKAGE = "com.evancharlton.mileage";
	public static final int DATE_DIALOG_ID = 0;
	public static final int MENU_VEHICLES = Menu.FIRST;
	public static final int MENU_SETTINGS = Menu.FIRST + 1;

	protected static final String SAVED_PRICE = "saved_price";
	protected static final String SAVED_AMOUNT = "saved_amount";
	protected static final String SAVED_ODOMETER = "saved_odometer";
	protected static final String SAVED_DATE = "saved_date";
	protected static final String SAVED_COMMENT = "saved_comment";
	protected static final String SAVED_VEHICLE = "saved_vehicle";

	protected static final int CONTEXT_CONVERT_TO_UNIT_COST = 1;
	protected static final int CONTEXT_LITRES_TO_GALLONS = 10;
	protected static final int CONTEXT_LITRES_TO_IMP_GALLONS = 11;
	protected static final int CONTEXT_GALLONS_TO_LITRES = 12;
	protected static final int CONTEXT_GALLONS_TO_IMP_GALLONS = 13;
	protected static final int CONTEXT_IMP_GALLONS_TO_LITRES = 14;
	protected static final int CONTEXT_IMP_GALLONS_TO_GALLONS = 15;
	protected static final int CONTEXT_MILES_TO_KILOMETERS = 20;
	protected static final int CONTEXT_KILOMETERS_TO_MILES = 21;

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
	private LinearLayout m_osk;
	private List<Button> m_oskButtons = new ArrayList<Button>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Calendar c = Calendar.getInstance();
		m_year = c.get(Calendar.YEAR);
		m_month = c.get(Calendar.MONTH);
		m_day = c.get(Calendar.DAY_OF_MONTH);

		setContentView(R.layout.fillup);

		setUpOSK();
		loadData();
		initHandlers();
		updateDate();
		loadPrefs();
	}

	public void saveState(Bundle outState) {
		outState.putString(SAVED_PRICE, m_priceEdit.getText().toString());
		outState.putString(SAVED_AMOUNT, m_amountEdit.getText().toString());
		outState.putString(SAVED_COMMENT, m_commentEdit.getText().toString());
		outState.putString(SAVED_ODOMETER, m_mileageEdit.getText().toString());
		outState.putInt(SAVED_VEHICLE, m_vehicleSpinner.getSelectedItemPosition());
		Date d = new Date(m_year, m_month, m_day);
		outState.putLong(SAVED_DATE, d.getTime());
	}

	public void restoreState(Bundle inState) {
		m_priceEdit.setText(inState.getString(SAVED_PRICE));
		m_amountEdit.setText(inState.getString(SAVED_AMOUNT));
		m_commentEdit.setText(inState.getString(SAVED_COMMENT));
		m_mileageEdit.setText(inState.getString(SAVED_ODOMETER));
		m_vehicleSpinner.setSelection(inState.getInt(SAVED_VEHICLE));
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(inState.getLong(SAVED_DATE));
		m_day = c.get(Calendar.DAY_OF_MONTH);
		m_year = c.get(Calendar.YEAR);
		m_month = c.get(Calendar.MONTH);
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

		registerForContextMenu(m_amountEdit);
		registerForContextMenu(m_priceEdit);
		registerForContextMenu(m_mileageEdit);

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

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		if (view.equals(m_priceEdit)) {
			menu.clear();
			menu.add(Menu.NONE, CONTEXT_CONVERT_TO_UNIT_COST, Menu.NONE, getString(R.string.context_convert_to_unit_cost));
		} else if (view.equals(m_amountEdit)) {
			menu.clear();
			menu.add(Menu.NONE, CONTEXT_LITRES_TO_GALLONS, Menu.NONE, getString(R.string.context_litres_to_gallons));
			menu.add(Menu.NONE, CONTEXT_LITRES_TO_IMP_GALLONS, Menu.NONE, getString(R.string.context_litres_to_imp_gallons));
			menu.add(Menu.NONE, CONTEXT_GALLONS_TO_LITRES, Menu.NONE, getString(R.string.context_gallons_to_litres));
			menu.add(Menu.NONE, CONTEXT_GALLONS_TO_IMP_GALLONS, Menu.NONE, getString(R.string.context_gallons_to_imp_gallons));
			menu.add(Menu.NONE, CONTEXT_IMP_GALLONS_TO_LITRES, Menu.NONE, getString(R.string.context_imp_gallons_to_litres));
			menu.add(Menu.NONE, CONTEXT_IMP_GALLONS_TO_GALLONS, Menu.NONE, getString(R.string.context_imp_gallons_to_gallons));
		} else if (view.equals(m_mileageEdit)) {
			menu.clear();
			menu.add(Menu.NONE, CONTEXT_KILOMETERS_TO_MILES, Menu.NONE, getString(R.string.context_km_to_miles));
			menu.add(Menu.NONE, CONTEXT_MILES_TO_KILOMETERS, Menu.NONE, getString(R.string.context_miles_to_km));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case CONTEXT_LITRES_TO_GALLONS:
				convertAmount(PreferencesProvider.LITRES, PreferencesProvider.GALLONS);
				return true;
			case CONTEXT_LITRES_TO_IMP_GALLONS:
				convertAmount(PreferencesProvider.LITRES, PreferencesProvider.IMP_GALLONS);
				return true;
			case CONTEXT_GALLONS_TO_LITRES:
				convertAmount(PreferencesProvider.GALLONS, PreferencesProvider.LITRES);
				return true;
			case CONTEXT_GALLONS_TO_IMP_GALLONS:
				convertAmount(PreferencesProvider.GALLONS, PreferencesProvider.IMP_GALLONS);
				return true;
			case CONTEXT_IMP_GALLONS_TO_LITRES:
				convertAmount(PreferencesProvider.IMP_GALLONS, PreferencesProvider.LITRES);
				return true;
			case CONTEXT_IMP_GALLONS_TO_GALLONS:
				convertAmount(PreferencesProvider.IMP_GALLONS, PreferencesProvider.GALLONS);
				return true;
			case CONTEXT_CONVERT_TO_UNIT_COST:
				String amount = m_amountEdit.getText().toString().trim();
				if (amount.length() > 0) {
					try {
						Double fuel_amount = Double.parseDouble(amount);
						Double cost = Double.parseDouble(m_priceEdit.getText().toString().trim());
						cost = cost / fuel_amount;
						m_priceEdit.setText(String.valueOf(cost));
					} catch (NumberFormatException nfe) {
						// squish!
					}
				}
				return true;
			case CONTEXT_MILES_TO_KILOMETERS:
				convertDistance(PreferencesProvider.MILES, PreferencesProvider.KILOMETERS);
				return true;
			case CONTEXT_KILOMETERS_TO_MILES:
				convertDistance(PreferencesProvider.KILOMETERS, PreferencesProvider.MILES);
				return true;
		}
		return super.onContextItemSelected(item);
	}

	private void convertAmount(int from, int to) {
		try {
			Double d = Double.parseDouble(m_amountEdit.getText().toString().trim());
			d = PreferencesProvider.getInstance(this).getCalculator().convertVolume(from, to, d);
			m_amountEdit.setText(String.valueOf(d));
		} catch (NumberFormatException nfe) {
			// squish!
		}
	}

	private void convertDistance(int from, int to) {
		try {
			Double d = Double.parseDouble(m_mileageEdit.getText().toString().trim());
			d = PreferencesProvider.getInstance(this).getCalculator().convertDistance(from, to, d);
			m_mileageEdit.setText(String.valueOf(d));
		} catch (NumberFormatException nfe) {
			// squish!
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

		m_amountEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				setOskVisibility(hasFocus, false);
			}
		});

		m_priceEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				setOskVisibility(hasFocus, false);
			}
		});

		m_mileageEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				setOskVisibility(hasFocus);
			}
		});

		m_priceEdit.requestFocus();

		// m_priceEdit.setKeyListener(new KeyFocuser(m_amountEdit));
		// m_amountEdit.setKeyListener(new KeyFocuser(m_mileageEdit));
		// m_mileageEdit.setKeyListener(new KeyFocuser(m_commentEdit));
		// m_commentEdit.setKeyListener(new KeyFocuser(m_saveButton));
	}

	protected void setOskVisibility(boolean visible, boolean plus_sign) {
		Button plus = (Button) findViewById(R.id.plus_btn);
		if (plus_sign) {
			plus.setVisibility(View.VISIBLE);
		} else {
			plus.setVisibility(View.GONE);
		}
		if (m_osk != null) {
			if (visible && isPortrait()) {
				m_osk.setVisibility(View.VISIBLE);
			} else {
				m_osk.setVisibility(View.GONE);
			}
		}
	}

	protected void setOskVisibility(boolean visible) {
		setOskVisibility(visible, true);
	}

	protected boolean isPortrait() {
		WindowManager wm = getWindowManager();
		Display d = wm.getDefaultDisplay();
		return d.getWidth() < d.getHeight();
	}

	private void resetForm(View v) {
		m_amountEdit.setText("");
		m_priceEdit.setText("");
		m_mileageEdit.setText("");
		m_commentEdit.setText("");

		m_priceEdit.requestFocus();

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

	protected void setUpOSK() {
		m_osk = (LinearLayout) findViewById(R.id.number_osk);

		m_oskButtons.add((Button) findViewById(R.id.zero_btn));
		m_oskButtons.add((Button) findViewById(R.id.one_btn));
		m_oskButtons.add((Button) findViewById(R.id.two_btn));
		m_oskButtons.add((Button) findViewById(R.id.three_btn));
		m_oskButtons.add((Button) findViewById(R.id.four_btn));
		m_oskButtons.add((Button) findViewById(R.id.five_btn));
		m_oskButtons.add((Button) findViewById(R.id.six_btn));
		m_oskButtons.add((Button) findViewById(R.id.seven_btn));
		m_oskButtons.add((Button) findViewById(R.id.eight_btn));
		m_oskButtons.add((Button) findViewById(R.id.nine_btn));
		m_oskButtons.add((Button) findViewById(R.id.plus_btn));
		m_oskButtons.add((Button) findViewById(R.id.dot_btn));
		m_oskButtons.add((Button) findViewById(R.id.backspace_btn));

		for (Button btn : m_oskButtons) {
			btn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					View focus = getCurrentFocus();
					if (focus instanceof EditText) {
						EditText focusedText = (EditText) focus;
						CharSequence text = ((Button) v).getText();
						if (text.length() == 1) {
							focusedText.append(text);
						} else {
							// backspace
							Editable seq = focusedText.getText();
							int index = Selection.getSelectionStart(seq);
							if (index >= 1) {
								seq.delete(index - 1, index);
							}
						}
					}
				}
			});
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