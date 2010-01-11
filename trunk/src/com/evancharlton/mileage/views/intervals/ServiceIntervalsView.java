package com.evancharlton.mileage.views.intervals;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
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

import com.evancharlton.mileage.PreferencesProvider;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.models.ServiceInterval;
import com.evancharlton.mileage.models.Vehicle;

public class ServiceIntervalsView extends ListActivity {
	private static final int MENU_ADD = Menu.FIRST;
	private static final int MENU_DELETE = Menu.FIRST + 1;
	private static final int MENU_EDIT = Menu.FIRST + 2;
	private static final String DELETE_ID = "delete_id";

	private long m_deleteId = -1;
	private AlertDialog m_deleteDialog;
	private AlertDialog m_deleteExpiredDialog;
	private Map<Long, String> m_vehicleTitles = new HashMap<Long, String>();
	private PreferencesProvider m_prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.intervals);

		setTitle(R.string.service_intervals);

		m_prefs = PreferencesProvider.getInstance(this);

		getListView().setOnCreateContextMenuListener(this);

		Cursor vehicleCursor = managedQuery(Vehicle.CONTENT_URI, Vehicle.getProjection(), null, null, Vehicle.DEFAULT_SORT_ORDER);
		vehicleCursor.moveToFirst();
		while (vehicleCursor.isAfterLast() == false) {
			m_vehicleTitles.put(vehicleCursor.getLong(vehicleCursor.getColumnIndex(Vehicle._ID)), vehicleCursor.getString(vehicleCursor.getColumnIndex(Vehicle.TITLE)));
			vehicleCursor.moveToNext();
		}

		Cursor c = managedQuery(ServiceInterval.CONTENT_URI, ServiceInterval.getProjection(), null, null, ServiceInterval.DEFAULT_SORT_ORDER);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.intervals_row, c, new String[] {
				ServiceInterval.DESCRIPTION,
				ServiceInterval.VEHICLE_ID,
				ServiceInterval.DISTANCE,
				ServiceInterval.DURATION
		}, new int[] {
				R.id.description,
				R.id.vehicle,
				R.id.distance,
				R.id.duration
		});
		adapter.setViewBinder(m_viewBinder);
		setListAdapter(adapter);

		m_deleteDialog = new AlertDialog.Builder(this).create();
		m_deleteDialog.setMessage(getString(R.string.confirm_delete));
		m_deleteDialog.setCancelable(false);
		m_deleteDialog.setButton(getString(android.R.string.yes), m_deleteListener);
		m_deleteDialog.setButton2(getString(android.R.string.no), m_deleteListener);

		m_deleteExpiredDialog = new AlertDialog.Builder(this).create();
		m_deleteExpiredDialog.setCancelable(false);
		m_deleteExpiredDialog.setButton(getString(android.R.string.yes), m_deleteExpiredListener);
		m_deleteExpiredDialog.setButton2(getString(android.R.string.no), m_deleteExpiredListener);
		m_deleteExpiredDialog.setButton3(getString(R.string.service_interval_remind), m_deleteExpiredListener);

		// see if we came here from a notification
		Intent i = getIntent();
		Bundle extras = i.getExtras();
		if (extras != null) {
			long id = extras.getLong(ServiceInterval._ID, -1L);
			if (id >= 0) {
				try {
					ServiceInterval si = new ServiceInterval(id);
					Vehicle v = new Vehicle(si.getVehicleId());
					m_deleteId = id;
					m_deleteExpiredDialog.setTitle(si.getDescription());
					m_deleteExpiredDialog.setMessage(getString(R.string.service_interval_confirm_delete_expired, v.getTitle()));
					m_deleteExpiredDialog.show();
					NotificationManager notificationMgr = (NotificationManager) this.getSystemService(Activity.NOTIFICATION_SERVICE);
					if (notificationMgr != null) {
						notificationMgr.cancel((int) id);
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
		}

		Bundle data = (Bundle) getLastNonConfigurationInstance();
		if (data != null) {
			m_deleteId = data.getLong(DELETE_ID, -1);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		Bundle data = new Bundle();

		data.putLong(DELETE_ID, m_deleteId);

		return data;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_ADD, 0, R.string.add_service_interval).setShortcut('1', 'a').setIcon(R.drawable.ic_menu_add);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ADD:
				Intent i = new Intent();
				i.setClass(ServiceIntervalsView.this, AddServiceIntervalView.class);
				startActivity(i);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView lv, View v, int position, long id) {
		Uri uri = ContentUris.withAppendedId(ServiceInterval.CONTENT_URI, id);
		Intent intent = new Intent();
		intent.setData(uri);
		intent.setClass(ServiceIntervalsView.this, EditServiceIntervalView.class);
		startActivity(intent);
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {
		menu.setHeaderTitle(R.string.operations);
		menu.add(Menu.NONE, MENU_EDIT, Menu.NONE, R.string.edit);
		menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, R.string.delete);
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = null;
		try {
			info = (AdapterContextMenuInfo) item.getMenuInfo();
			long id = getListAdapter().getItemId(info.position);
			switch (item.getItemId()) {
				case MENU_DELETE:
					m_deleteId = id;
					m_deleteDialog.show();
					return true;
				case MENU_EDIT:
					onListItemClick(getListView(), info.targetView, info.position, id);
					return true;
			}
		} catch (ClassCastException e) {
			// fail gracefully?
		}
		return super.onContextItemSelected(item);
	}

	private void delete() {
		ServiceInterval si = new ServiceInterval(m_deleteId);
		si.cancelAlarm(this);

		Uri uri = ContentUris.withAppendedId(ServiceInterval.CONTENT_URI, m_deleteId);
		getContentResolver().delete(uri, null, null);
	}

	private void procrastinate() {
		ServiceInterval interval = new ServiceInterval(m_deleteId);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		long now = cal.getTimeInMillis();
		long duration = now - interval.getCreateDate().getTimeInMillis();
		interval.setDuration(duration);
		interval.save();
		interval.scheduleAlarm(ServiceIntervalsView.this);
		finish();
	}

	private DialogInterface.OnClickListener m_deleteListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			if (which == Dialog.BUTTON1) {
				delete();
			}
		}
	};

	private DialogInterface.OnClickListener m_deleteExpiredListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			if (which == Dialog.BUTTON1) {
				delete();
			} else if (which == Dialog.BUTTON3) {
				procrastinate();
			}
		}
	};

	private SimpleCursorAdapter.ViewBinder m_viewBinder = new SimpleCursorAdapter.ViewBinder() {
		public boolean setViewValue(View view, Cursor cursor, int index) {
			String colName = cursor.getColumnName(index);
			TextView textView = (TextView) view;
			String text = null;
			if (colName.equals(ServiceInterval.VEHICLE_ID)) {
				text = String.format("%s @ ", m_vehicleTitles.get(cursor.getLong(index)));
			} else if (colName.equals(ServiceInterval.DURATION)) {
				long time = cursor.getLong(cursor.getColumnIndex(ServiceInterval.CREATE_DATE));
				time += cursor.getLong(index);
				Date d = new Date(time);
				text = String.format("%s", DateFormat.getDateFormat(ServiceIntervalsView.this).format(d));
			} else if (colName.equals(ServiceInterval.DISTANCE)) {
				double odometer = cursor.getDouble(cursor.getColumnIndex(ServiceInterval.CREATE_ODOMETER));
				odometer += cursor.getDouble(index);;
				text = String.format("%s %s | ", m_prefs.shortFormat(odometer), m_prefs.getCalculator().getDistanceUnitsAbbr().trim());
			}

			if (text != null) {
				textView.setText(text);
				return true;
			}

			return false;
		}
	};
}
