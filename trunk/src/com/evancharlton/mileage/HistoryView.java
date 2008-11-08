package com.evancharlton.mileage;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class HistoryView extends ListActivity {
	public static final int MENU_IMPORT_EXPORT = Menu.FIRST;
	public static final int MENU_EXPORT = Menu.FIRST;
	public static final int MENU_EXPORT_DB = Menu.FIRST + 1;
	public static final int MENU_EXPORT_SQL = Menu.FIRST + 2;
	public static final int MENU_EXPORT_CSV = Menu.FIRST + 3;
	public static final int MENU_IMPORT = Menu.FIRST + 4;
	public static final int MENU_IMPORT_DB = Menu.FIRST + 5;
	public static final int MENU_IMPORT_SQL = Menu.FIRST + 6;
	public static final int MENU_IMPORT_CSV = Menu.FIRST + 7;
	public static final String TAG = "HistoryList";

	private double m_avgMpg = 0.0D;
	private DecimalFormat m_formatter = new DecimalFormat("##0.00");
	private Map<Integer, String> m_vehicleTitles = new HashMap<Integer, String>();

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

		Cursor vehicleCursor = managedQuery(Vehicles.CONTENT_URI, projection, null, null, Vehicles.DEFAULT_SORT_ORDER);
		vehicleCursor.moveToFirst();
		while (vehicleCursor.isAfterLast() == false) {
			String title = vehicleCursor.getString(1);
			int index = vehicleCursor.getInt(0);
			m_vehicleTitles.put(index, title);
			vehicleCursor.moveToNext();
		}

		Cursor historyCursor = managedQuery(FillUps.CONTENT_URI, PROJECTIONS, null, null, FillUps.DEFAULT_SORT_ORDER);
		if (historyCursor.getCount() > 0) {
			// we need to calculate the average MPG
			double total_distance = 0.0D;
			double total_fuel = 0.0D;
			historyCursor.moveToFirst();
			total_distance = historyCursor.getDouble(6);
			while (historyCursor.isLast() == false) {
				total_fuel += historyCursor.getDouble(1);
				historyCursor.moveToNext();
			}
			total_distance -= historyCursor.getDouble(6);
			total_fuel += historyCursor.getDouble(1);
			historyCursor.moveToFirst();
			m_avgMpg = total_distance / total_fuel;
		}

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

		menu.add(0, MENU_IMPORT_EXPORT, Menu.NONE, R.string.import_export).setShortcut('1', 'i');
		HelpDialog.injectHelp(menu, 'h');

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_IMPORT_EXPORT:
				Intent i = new Intent();
				i.setClass(HistoryView.this, ImportExportView.class);
				startActivity(i);
				break;
			case HelpDialog.MENU_HELP:
				HelpDialog.create(this, R.string.help_title_history, R.string.help_history);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private SimpleCursorAdapter.ViewBinder m_viewBinder = new SimpleCursorAdapter.ViewBinder() {
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			String val;
			switch (columnIndex) {
				case 1:
					double gallons = cursor.getDouble(columnIndex);
					val = m_formatter.format(gallons) + " " + getString(R.string.gallons_abbr);
					((TextView) view).setText(val);
					return true;
				case 2:
					double price = cursor.getDouble(columnIndex);
					val = "$" + m_formatter.format(price) + "/" + getString(R.string.gallons_abbr);
					((TextView) view).setText(val);
					return true;
				case 3:
					long time = cursor.getLong(columnIndex);
					Date date = new Date(time);
					DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);
					String text = format.format(date);
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
				case 6:
					double mileage = cursor.getDouble(columnIndex);
					int position = cursor.getPosition();
					if (position != cursor.getCount() - 1) {
						cursor.moveToNext();
						double next_mileage = cursor.getDouble(columnIndex);
						double diff = mileage - next_mileage;
						double amount = cursor.getDouble(1);
						double mpg = diff / amount;
						TextView tv = (TextView) view;
						int color = 0xFF666666;
						if (mpg >= m_avgMpg) {
							color = 0xFF0AB807;
						} else {
							color = 0xFFD90000;
						}
						tv.setTextColor(color);
						tv.setText(m_formatter.format(mpg) + " " + getString(R.string.mpg));
						cursor.moveToPrevious();
					}
					return true;
			}
			return false;
		}
	};
}