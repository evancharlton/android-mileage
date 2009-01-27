package com.evancharlton.mileage;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

	public static final int COL_ID = 0;
	public static final int COL_AMOUNT = 1;
	public static final int COL_COST = 2;
	public static final int COL_DATE = 3;
	public static final int COL_COMMENT = 4;
	public static final int COL_VEHICLEID = 5;
	public static final int COL_MILEAGE = 6;

	private Map<Integer, String> m_vehicleTitles = new HashMap<Integer, String>();
	private double m_avgMpg;
	private HashMap<Double, Double> m_history;
	private AlertDialog m_deleteDialog;
	private long m_deleteId;
	private PreferencesProvider m_prefs;
	private CalculationEngine m_calcEngine;
	private ListView m_listView;
	private Spinner m_vehicles;

	private static final String[] PROJECTIONS = new String[] {
			FillUp._ID,
			FillUp.AMOUNT,
			FillUp.COST,
			FillUp.DATE,
			FillUp.COMMENT,
			FillUp.VEHICLE_ID,
			FillUp.MILEAGE
	};

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
		m_history = new HashMap<Double, Double>();

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

		String[] projection = new String[] {
				Vehicle._ID,
				Vehicle.TITLE
		};

		Cursor vehicleCursor = managedQuery(Vehicle.CONTENT_URI, projection, null, null, Vehicle.DEFAULT_SORT_ORDER);
		SimpleCursorAdapter vehicleAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, vehicleCursor, new String[] {
			Vehicle.TITLE
		}, new int[] {
			android.R.id.text1
		});
		vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		m_vehicles.setAdapter(vehicleAdapter);
		m_vehicles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				onResume();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

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
				FillUp.COST,
				FillUp.DATE,
				FillUp.COMMENT,
				FillUp.VEHICLE_ID,
				FillUp.MILEAGE
		};
		int[] to = new int[] {
				R.id.history_amount,
				R.id.history_price,
				R.id.history_date,
				R.id.history_comment,
				R.id.history_vehicle,
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

		Cursor historyCursor = managedQuery(FillUp.CONTENT_URI, PROJECTIONS, selection, selectionArgs, FillUp.DEFAULT_SORT_ORDER);
		if (historyCursor.getCount() > 0) {
			historyCursor.moveToFirst();
			Map<Double, Double> milesToAmt = new HashMap<Double, Double>();
			while (historyCursor.isAfterLast() == false) {
				milesToAmt.put(historyCursor.getDouble(COL_MILEAGE), historyCursor.getDouble(COL_AMOUNT));
				historyCursor.moveToNext();
			}

			Set<Double> keyset = milesToAmt.keySet();
			Double[] keys = keyset.toArray(new Double[keyset.size()]);
			Arrays.sort(keys);
			double total_fuel = 0;
			for (int i = keys.length - 1; i > 0; i--) {
				double amount = milesToAmt.get(keys[i]);
				double prev_mileage = keys[i - 1];
				double mileage = keys[i];
				double diff = mileage - prev_mileage;
				double mpg = m_calcEngine.calculateEconomy(diff, amount);
				m_history.put(mileage, mpg);
				total_fuel += amount;
			}

			double total_distance = keys[keys.length - 1] - keys[0];
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
			String val;
			switch (columnIndex) {
				case 1:
					double gallons = cursor.getDouble(columnIndex);
					val = m_prefs.format(gallons) + m_calcEngine.getVolumeUnitsAbbr();
					((TextView) view).setText(val);
					return true;
				case 2:
					double price = cursor.getDouble(columnIndex);
					val = m_prefs.getCurrency() + m_prefs.format(price) + "/" + m_calcEngine.getVolumeUnitsAbbr().trim();
					((TextView) view).setText(val);
					return true;
				case 3:
					long time = cursor.getLong(columnIndex);
					Date date = new Date(time);
					String text = m_prefs.format(date);
					((TextView) view).setText(text);
					return true;
				case 5:
					int id = cursor.getInt(columnIndex);
					val = m_vehicleTitles.get(id);
					boolean hide = true;
					if (m_vehicleTitles.size() == 1) {
						hide = true;
					} else {
						if (val != null) {
							((TextView) view).setText(val);
							hide = false;
						}
					}
					if (hide) {
						view.setVisibility(View.GONE);
					}
					return true;
				case COL_MILEAGE:
					double mileage = cursor.getDouble(columnIndex);
					if (!cursor.isLast()) {
						Double mpg = m_history.get(mileage);
						if (mpg == null) {
							return true;
						}
						TextView tv = (TextView) view;
						int color = 0xFF666666;
						if (m_calcEngine.better(mpg, m_avgMpg)) {
							color = 0xFF0AB807;
						} else if (mpg == m_avgMpg) {
							color = 0xFF2469FF;
						} else {
							color = 0xFFD90000;
						}
						tv.setTextColor(color);
						tv.setText(m_prefs.format(mpg) + m_calcEngine.getEconomyUnits());
					}
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