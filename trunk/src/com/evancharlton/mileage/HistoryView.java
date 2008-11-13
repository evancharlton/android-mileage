package com.evancharlton.mileage;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
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
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.evancharlton.mileage.calculators.CalculationEngine;

public class HistoryView extends ListActivity implements View.OnCreateContextMenuListener {
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
	private Map<Integer, Double> m_avgEconomies = new HashMap<Integer, Double>();
	private HashMap<Integer, HashMap<Double, Double>> m_history;
	private AlertDialog m_deleteDialog;
	private long m_deleteId;

	private static final String[] PROJECTIONS = new String[] {
			FillUps._ID,
			FillUps.AMOUNT,
			FillUps.COST,
			FillUps.DATE,
			FillUps.COMMENT,
			FillUps.VEHICLE_ID,
			FillUps.MILEAGE
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		m_deleteDialog = new AlertDialog.Builder(this).create();
		m_deleteDialog.setMessage(getString(R.string.confirm_delete));
		m_deleteDialog.setCancelable(false);
		m_deleteDialog.setButton(getString(R.string.yes), m_deleteListener);
		m_deleteDialog.setButton2(getString(R.string.no), m_deleteListener);
	}

	public void onResume() {
		super.onResume();

		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

		Intent intent = getIntent();
		if (intent.getData() == null) {
			intent.setData(FillUps.CONTENT_URI);
		}

		getListView().setOnCreateContextMenuListener(this);
		String[] from = new String[] {
				FillUps.AMOUNT,
				FillUps.COST,
				FillUps.DATE,
				FillUps.COMMENT,
				FillUps.VEHICLE_ID,
				FillUps.MILEAGE
		};
		int[] to = new int[] {
				R.id.history_amount,
				R.id.history_price,
				R.id.history_date,
				R.id.history_comment,
				R.id.history_vehicle,
				R.id.history_mileage
		};

		String[] projection = new String[] {
				Vehicles._ID,
				Vehicles.TITLE
		};
		PreferencesProvider prefs = PreferencesProvider.getInstance(this);
		CalculationEngine engine = prefs.getCalculator();

		m_history = new HashMap<Integer, HashMap<Double, Double>>();

		Cursor vehicleCursor = managedQuery(Vehicles.CONTENT_URI, projection, null, null, Vehicles.DEFAULT_SORT_ORDER);
		vehicleCursor.moveToFirst();
		while (vehicleCursor.isAfterLast() == false) {
			String title = vehicleCursor.getString(1);
			int index = vehicleCursor.getInt(0);
			m_vehicleTitles.put(index, title);
			m_history.put(index, new HashMap<Double, Double>());
			vehicleCursor.moveToNext();
		}

		Cursor historyCursor = managedQuery(FillUps.CONTENT_URI, PROJECTIONS, null, null, FillUps.DEFAULT_SORT_ORDER);
		if (historyCursor.getCount() > 0) {
			historyCursor.moveToFirst();
			while (historyCursor.isAfterLast() == false) {
				int vehicleId = historyCursor.getInt(COL_VEHICLEID);
				HashMap<Double, Double> data = m_history.get(vehicleId);
				double mileage = historyCursor.getDouble(COL_MILEAGE);
				double amount = historyCursor.getDouble(COL_AMOUNT);
				data.put(mileage, amount);
				historyCursor.moveToNext();
			}
			historyCursor.moveToFirst();

			// calculate their respective economies
			for (Integer vehicleId : m_history.keySet()) {
				HashMap<Double, Double> data = m_history.get(vehicleId);
				Double[] keys = data.keySet().toArray(new Double[data.keySet().size()]);
				if (keys.length == 1) {
					// can't calculate the avg economy
					data.put(keys[0], null);
					continue;
				}
				double total_miles = Math.abs(keys[0] - keys[keys.length - 1]);
				double total_fuel = 0.0D;
				for (int i = keys.length - 1; i > 0; i--) {
					double key = keys[i];
					double miles = keys[i] - keys[i - 1];
					double amount = data.get(key);
					data.put(key, engine.calculateEconomy(miles, amount));
					total_fuel += amount;
				}
				double mileage = engine.calculateEconomy(total_miles, total_fuel);
				m_avgEconomies.put(vehicleId, mileage);
			}
		}

		ListView list = getListView();
		list.setFocusable(true);
		list.setOnCreateContextMenuListener(this);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.history, historyCursor, from, to);
		adapter.setViewBinder(m_viewBinder);
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView lv, View v, int position, long id) {
		super.onListItemClick(lv, v, position, id);
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
		Intent intent = new Intent();
		intent.setData(uri);
		intent.setClass(HistoryView.this, FillUpView.class);
		startActivity(intent);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		Mileage.createMenu(menu);
		HelpDialog.injectHelp(menu, 'h');
		return true;
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
					m_deleteId = getListAdapter().getItemId(info.position);
					showDialog(DELETE_DIALOG_ID);
					return true;
				case MENU_EDIT:
					long id = getListAdapter().getItemId(info.position);
					onListItemClick(getListView(), info.targetView, info.position, id);
					return true;
			}
		} catch (ClassCastException e) {
			// fail gracefully?
		}
		return super.onContextItemSelected(item);
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
		Uri uri = ContentUris.withAppendedId(FillUps.CONTENT_URI, m_deleteId);
		getContentResolver().delete(uri, null, null);
	}

	private SimpleCursorAdapter.ViewBinder m_viewBinder = new SimpleCursorAdapter.ViewBinder() {
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			PreferencesProvider prefs = PreferencesProvider.getInstance(HistoryView.this);
			CalculationEngine engine = prefs.getCalculator();
			String val;
			switch (columnIndex) {
				case 1:
					double gallons = cursor.getDouble(columnIndex);
					val = prefs.format(gallons) + engine.getVolumeUnitsAbbr();
					((TextView) view).setText(val);
					return true;
				case 2:
					double price = cursor.getDouble(columnIndex);
					val = prefs.getCurrency() + prefs.format(price) + "/" + engine.getVolumeUnitsAbbr().trim();
					((TextView) view).setText(val);
					return true;
				case 3:
					long time = cursor.getLong(columnIndex);
					Date date = new Date(time);
					String text = prefs.format(date);
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
						int vehicleId = cursor.getInt(COL_VEHICLEID);
						Double mpg = m_history.get(vehicleId).get(mileage);
						if (mpg == null) {
							// can't do anything
							return true;
						}
						double avgMpg = m_avgEconomies.get(vehicleId);
						TextView tv = (TextView) view;
						int color = 0xFF666666;
						if (engine.better(mpg, avgMpg)) {
							color = 0xFF0AB807;
						} else {
							color = 0xFFD90000;
						}
						tv.setTextColor(color);
						tv.setText(prefs.format(mpg) + engine.getEconomyUnits());
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