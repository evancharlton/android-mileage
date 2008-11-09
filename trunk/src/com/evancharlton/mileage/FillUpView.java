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
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

public class FillUpView extends Activity {

	private static final int DATE_DIALOG_ID = 0;
	private static final int DELETE_DIALOG_ID = 1;

	public static final int MENU_DELETE = Menu.FIRST;

	private EditText m_priceEdit;
	private EditText m_amountEdit;
	private EditText m_mileageEdit;
	private EditText m_commentEdit;
	private Button m_dateButton;
	private Button m_saveButton;
	private AlertDialog m_deleteDialog;
	private Spinner m_vehicleSpinner;
	private DatePickerDialog m_dateDlg = null;

	private int m_day;
	private int m_year;
	private int m_month;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fillup);

		initMembers();
		loadData();
	}

	private void initMembers() {
		m_priceEdit = (EditText) findViewById(R.id.price_edit);
		m_amountEdit = (EditText) findViewById(R.id.amount_edit);
		m_mileageEdit = (EditText) findViewById(R.id.odometer_edit);
		m_commentEdit = (EditText) findViewById(R.id.comment_edit);
		m_dateButton = (Button) findViewById(R.id.change_date_btn);
		m_saveButton = (Button) findViewById(R.id.save_btn);
		m_vehicleSpinner = (Spinner) findViewById(R.id.vehicle_spinner);

		m_deleteDialog = new AlertDialog.Builder(this).create();
		m_deleteDialog.setMessage(getString(R.string.confirm_delete));
		m_deleteDialog.setCancelable(false);
		m_deleteDialog.setButton(getString(R.string.yes), m_deleteListener);
		m_deleteDialog.setButton2(getString(R.string.no), m_deleteListener);

		Calendar cal = Calendar.getInstance();
		m_day = cal.get(Calendar.DAY_OF_MONTH);
		m_year = cal.get(Calendar.YEAR);
		m_month = cal.get(Calendar.MONTH);

		// handlers
		m_dateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});

		m_saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// save the changes
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

				if (error) {
					AlertDialog dlg = new AlertDialog.Builder(FillUpView.this).create();
					dlg.setTitle(R.string.error);
					dlg.setMessage(getString(errorMsg));
					dlg.show();
					return;
				}

				values.put(FillUps.COMMENT, m_commentEdit.getText().toString().trim());
				values.put(FillUps.VEHICLE_ID, m_vehicleSpinner.getSelectedItemId());

				Calendar c = new GregorianCalendar(m_year, m_month, m_day);
				values.put(FillUps.DATE, c.getTimeInMillis());

				getContentResolver().update(getIntent().getData(), values, null, null);
				finish();
			}
		});

		// m_priceEdit.setKeyListener(new KeyFocuser(m_amountEdit));
		// m_amountEdit.setKeyListener(new KeyFocuser(m_mileageEdit));
		// m_mileageEdit.setKeyListener(new KeyFocuser(m_commentEdit));
		// m_commentEdit.setKeyListener(new KeyFocuser(m_saveButton));
	}

	private void delete() {
		getContentResolver().delete(getIntent().getData(), null, null);
	}

	private void updateDate() {
		GregorianCalendar gc = new GregorianCalendar(m_year, m_month, m_day);
		Date d = new Date(gc.getTimeInMillis());

		m_dateButton.setText(PreferencesProvider.getInstance(FillUpView.this).format(d));
		if (m_dateDlg != null) {
			m_dateDlg.updateDate(m_year, m_month, m_day);
		}
	}

	private void loadData() {
		Intent data = getIntent();

		// load the data
		String[] projections = new String[] {
				FillUps._ID,
				FillUps.COST,
				FillUps.AMOUNT,
				FillUps.MILEAGE,
				FillUps.DATE,
				FillUps.VEHICLE_ID,
				FillUps.COMMENT
		};

		Cursor c = managedQuery(data.getData(), projections, null, null, null);
		c.moveToFirst();

		m_priceEdit.setText(c.getString(1));
		m_amountEdit.setText(c.getString(2));
		m_mileageEdit.setText(c.getString(3));
		m_commentEdit.setText(c.getString(6));
		int vehicleId = c.getInt(5);

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(c.getLong(4));
		m_year = cal.get(Calendar.YEAR);
		m_month = cal.get(Calendar.MONTH);
		m_day = cal.get(Calendar.DAY_OF_MONTH);
		updateDate();

		c = managedQuery(Vehicles.CONTENT_URI, new String[] {
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

		int position = 0;
		for (int i = 0; i < vehicleAdapter.getCount(); i++) {
			if (vehicleId == vehicleAdapter.getItemId(i)) {
				position = i;
				break;
			}
		}
		m_vehicleSpinner.setSelection(position);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, R.string.delete).setShortcut('1', 'd');
		HelpDialog.injectHelp(menu, 'h');

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_DELETE:
				showDialog(DELETE_DIALOG_ID);
				break;
			case HelpDialog.MENU_HELP:
				HelpDialog.create(this, R.string.help_title_fillup_edit, R.string.help_fillup_edit);
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
			case DELETE_DIALOG_ID:
				return m_deleteDialog;
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