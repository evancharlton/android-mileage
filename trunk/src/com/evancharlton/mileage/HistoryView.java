package com.evancharlton.mileage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.evancharlton.mileage.binders.VehicleBinder;
import com.evancharlton.mileage.calculators.CalculationEngine;
import com.evancharlton.mileage.models.FillUp;
import com.evancharlton.mileage.models.Vehicle;

public class HistoryView extends Activity implements View.OnCreateContextMenuListener {
	public static final int MENU_IMPORT_EXPORT = Menu.FIRST;
	public static final int MENU_EXPORT = Menu.FIRST;
	public static final int MENU_EXPORT_DB = Menu.FIRST + 1;
	public static final int MENU_EXPORT_SQL = Menu.FIRST + 2;
	public static final int MENU_EXPORT_CSV = Menu.FIRST + 3;
	public static final int MENU_IMPORT = Menu.FIRST + 4;
	public static final int MENU_IMPORT_DB = Menu.FIRST + 5;
	public static final int MENU_IMPORT_SQL = Menu.FIRST + 6;
	public static final int MENU_IMPORT_CSV = Menu.FIRST + 7;

	public static final int MENU_DELETE = Menu.FIRST;
	public static final int MENU_EDIT = Menu.FIRST + 1;
	public static final int DELETE_DIALOG_ID = 1;

	public static final String TAG = "HistoryList";

	public static final int COL_ID = FillUp.PROJECTION.indexOf(FillUp._ID);
	public static final int COL_AMOUNT = FillUp.PROJECTION.indexOf(FillUp.AMOUNT);
	public static final int COL_PRICE = FillUp.PROJECTION.indexOf(FillUp.PRICE);
	public static final int COL_TIMESTAMP = FillUp.PROJECTION.indexOf(FillUp.DATE);
	public static final int COL_COMMENT = FillUp.PROJECTION.indexOf(FillUp.COMMENT);
	public static final int COL_VEHICLEID = FillUp.PROJECTION.indexOf(FillUp.VEHICLE_ID);
	public static final int COL_ODOMETER = FillUp.PROJECTION.indexOf(FillUp.ODOMETER);

	private Map<Long, String> m_vehicleTitles = new HashMap<Long, String>();
	private double m_avgMpg;
	private AlertDialog m_deleteDialog;
	private long m_deleteId;
	private PreferencesProvider m_prefs;
	private CalculationEngine m_calcEngine;
	private ListView m_listView;
	private Spinner m_vehicles;
	private Map<Long, FillUp> m_fillupMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);

		m_deleteDialog = new AlertDialog.Builder(this).create();
		m_deleteDialog.setMessage(getString(R.string.confirm_delete));
		m_deleteDialog.setCancelable(false);
		m_deleteDialog.setButton(getString(R.string.yes), m_deleteListener);
		m_deleteDialog.setButton2(getString(R.string.no), m_deleteListener);

		m_listView = (ListView) findViewById(android.R.id.list);
		m_listView.setOnCreateContextMenuListener(this);
		m_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				HistoryView.this.onItemClick(arg2, arg3);
			}
		});

		buildVehicleSpinner();
	}

	protected void onItemClick(int arg2, long arg3) {
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), arg3);
		Intent intent = new Intent();
		intent.setData(uri);
		intent.setClass(HistoryView.this, FillUpView.class);
		startActivity(intent);
	}

	private void buildVehicleSpinner() {
		m_vehicles = (Spinner) findViewById(R.id.vehicles);

		Cursor vehicleCursor = managedQuery(Vehicle.CONTENT_URI, Vehicle.getProjection(), null, null, Vehicle.DEFAULT_SORT_ORDER);
		SimpleCursorAdapter vehicleAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, vehicleCursor, new String[] {
			Vehicle.TITLE
		}, new int[] {
			android.R.id.text1
		});
		vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		vehicleAdapter.setViewBinder(new VehicleBinder());
		m_vehicles.setAdapter(vehicleAdapter);
		m_vehicles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				onResume();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		// populate the vehicle title mapping
		vehicleCursor.moveToFirst();
		final int col_id = Vehicle.PROJECTION.indexOf(Vehicle._ID);
		final int col_title = Vehicle.PROJECTION.indexOf(Vehicle.TITLE);
		while (vehicleCursor.isAfterLast() == false) {
			m_vehicleTitles.put(vehicleCursor.getLong(col_id), vehicleCursor.getString(col_title));
			vehicleCursor.moveToNext();
		}

		if (vehicleAdapter.getCount() == 1) {
			m_vehicles.setVisibility(View.GONE);
		}
	}

	public void onResume() {
		super.onResume();

		m_prefs = PreferencesProvider.getInstance(this);
		m_calcEngine = m_prefs.getCalculator();

		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

		Intent intent = getIntent();
		if (intent.getData() == null) {
			intent.setData(FillUp.CONTENT_URI);
		}

		String[] from = new String[] {
				FillUp.AMOUNT,
				FillUp.PRICE,
				FillUp.DATE,
				FillUp.COMMENT,
				FillUp.ODOMETER
		};
		int[] to = new int[] {
				R.id.history_amount,
				R.id.history_price,
				R.id.history_date,
				R.id.history_comment,
				R.id.history_mileage
		};

		String selection;
		String[] selectionArgs;
		if (m_vehicles.getVisibility() != View.GONE) {
			selection = FillUp.VEHICLE_ID + " = ?";
			selectionArgs = new String[] {
				String.valueOf(m_vehicles.getSelectedItemId())
			};
		} else {
			selection = FillUp.VEHICLE_ID + " = (select " + Vehicle._ID + " from " + FillUpsProvider.VEHICLES_TABLE_NAME + " order by " + Vehicle.DEFAULT + " desc limit 1)";
			selectionArgs = null;
		}

		Cursor historyCursor = managedQuery(FillUp.CONTENT_URI, FillUp.getProjection(), selection, selectionArgs, FillUp.DEFAULT_SORT_ORDER);
		if (historyCursor.getCount() > 0) {
			historyCursor.moveToFirst();
			double total_distance = 0D;
			double total_fuel = 0D;
			List<FillUp> fillups = new ArrayList<FillUp>();
			m_fillupMap = new HashMap<Long, FillUp>();
			while (historyCursor.isAfterLast() == false) {
				Map<String, String> data = new HashMap<String, String>();
				data.put(FillUp.AMOUNT, historyCursor.getString(COL_AMOUNT));
				data.put(FillUp.PRICE, historyCursor.getString(COL_PRICE));
				data.put(FillUp.ODOMETER, historyCursor.getString(COL_ODOMETER));
				long id = historyCursor.getLong(COL_ID);
				data.put(FillUp._ID, String.valueOf(id));

				FillUp f = new FillUp(m_calcEngine, data);

				Map<String, String> vehicleData = new HashMap<String, String>();
				long vehicleId = historyCursor.getLong(COL_VEHICLEID);
				vehicleData.put(Vehicle._ID, String.valueOf(vehicleId));
				vehicleData.put(Vehicle.TITLE, m_vehicleTitles.get(vehicleId));

				fillups.add(0, f);

				m_fillupMap.put(id, f);

				total_fuel += f.getAmount();

				historyCursor.moveToNext();
			}

			// linkage, to avoid many look-ups
			for (int i = 0; i < fillups.size() - 1; i++) {
				FillUp current = fillups.get(i);
				FillUp next = fillups.get(i + 1);

				current.setNext(next);
				next.setPrevious(current);
			}

			total_distance = fillups.get(fillups.size() - 1).getOdometer() - fillups.get(0).getOdometer();
			m_avgMpg = m_calcEngine.calculateEconomy(total_distance, total_fuel);
		}

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.history_row, historyCursor, from, to);
		adapter.setViewBinder(m_viewBinder);
		m_listView.setAdapter(adapter);
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {
		menu.setHeaderTitle(R.string.operations);
		menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, R.string.delete);
		menu.add(Menu.NONE, MENU_EDIT, Menu.NONE, R.string.edit_fillup);
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = null;
		try {
			info = (AdapterContextMenuInfo) item.getMenuInfo();
			switch (item.getItemId()) {
				case MENU_DELETE:
					m_deleteId = m_listView.getAdapter().getItemId(info.position);
					showDialog(DELETE_DIALOG_ID);
					return true;
				case MENU_EDIT:
					long id = m_listView.getAdapter().getItemId(info.position);
					onItemClick(info.position, id);
					return true;
			}
		} catch (ClassCastException e) {
			// fail gracefully?
		}
		return super.onContextItemSelected(item);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		Mileage.createMenu(menu);
		HelpDialog.injectHelp(menu, 'h');
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = Mileage.parseMenuItem(item, this);
		if (ret) {
			return true;
		}
		switch (item.getItemId()) {
			case HelpDialog.MENU_HELP:
				HelpDialog.create(this, R.string.help_title_history, R.string.help_history);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public Dialog onCreateDialog(int id) {
		switch (id) {
			case DELETE_DIALOG_ID:
				return m_deleteDialog;
		}
		return super.onCreateDialog(id);
	}

	private void delete() {
		Uri uri = ContentUris.withAppendedId(FillUp.CONTENT_URI, m_deleteId);
		getContentResolver().delete(uri, null, null);
		onResume();
	}

	private SimpleCursorAdapter.ViewBinder m_viewBinder = new SimpleCursorAdapter.ViewBinder() {
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			String text = null;
			FillUp fillup;
			TextView textview = (TextView) view;
			if (columnIndex == COL_AMOUNT) {
				double gallons = cursor.getDouble(columnIndex);
				text = m_prefs.format(gallons) + m_calcEngine.getVolumeUnitsAbbr();
			} else if (columnIndex == COL_PRICE) {
				double price = cursor.getDouble(columnIndex);
				text = m_prefs.getCurrency() + m_prefs.format(price) + "/" + m_calcEngine.getVolumeUnitsAbbr().trim();
			} else if (columnIndex == COL_TIMESTAMP) {
				long time = cursor.getLong(columnIndex);
				Date date = new Date(time);
				text = m_prefs.format(date);
			} else if (columnIndex == COL_ODOMETER) {
				if (!cursor.isLast()) {
					fillup = m_fillupMap.get(cursor.getLong(FillUp.PROJECTION.indexOf(FillUp._ID)));
					if (fillup == null) {
						return true;
					}
					double mpg = fillup.calcEconomy();
					int color = 0xFF666666;
					if (m_calcEngine.better(mpg, m_avgMpg)) {
						color = 0xFF0AB807;
					} else if (mpg == m_avgMpg) {
						color = 0xFF2469FF;
					} else {
						color = 0xFFD90000;
					}
					textview.setTextColor(color);
					text = m_prefs.format(mpg) + m_calcEngine.getEconomyUnits();
				}
			}
			if (text != null) {
				textview.setText(text);
				return true;
			}
			return false;
		}
	};

	private DialogInterface.OnClickListener m_deleteListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			if (which == Dialog.BUTTON1) {
				delete();
			}
		}
	};
}