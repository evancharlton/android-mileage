package com.evancharlton.mileage;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

import com.evancharlton.mileage.dao.CachedValue;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.provider.Statistics;
import com.evancharlton.mileage.provider.Statistics.Statistic;
import com.evancharlton.mileage.provider.tables.CacheTable;
import com.evancharlton.mileage.provider.tables.VehiclesTable;
import com.evancharlton.mileage.tasks.VehicleStatisticsTask;

public class VehicleStatisticsActivity extends Activity {
	private static final String TAG = "VehicleStatisticsActivity";

	private final Vehicle mVehicle = new Vehicle(new ContentValues());

	private Spinner mVehicleSpinner;
	private ListView mListView;
	private VehicleStatisticsTask mCalculationTask = null;
	private ProgressBar mProgressBar;

	private SimpleCursorAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vehicle_statistics);

		Object[] saved = (Object[]) getLastNonConfigurationInstance();
		if (saved != null) {
			mCalculationTask = (VehicleStatisticsTask) saved[0];
			mAdapter = null;
		}
		if (mCalculationTask != null) {
			mCalculationTask.setActivity(this);
		}

		mListView = (ListView) findViewById(android.R.id.list);
		mVehicleSpinner = (Spinner) findViewById(R.id.vehicle);
		mProgressBar = (ProgressBar) findViewById(R.id.progress);

		mVehicleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO(release) - make this change vehicles
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		loadVehicle();
		Cursor c = getCursor();

		final ArrayList<Statistics.Statistic> statistics = Statistics.STATISTICS;
		final int numStats = statistics.size();
		Log.d(TAG, "Checking statistics ... " + numStats);
		if (c.getCount() < numStats) {
			// kick off the task
			calculate();
		}
		setAdapter(c);

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> list, View row, int position, long id) {
				Statistic statistic = Statistics.STATISTICS.get(position);
				Class<? extends ChartActivity> target = statistic.getChartClass();
				if (target != null) {
					Intent intent = new Intent(VehicleStatisticsActivity.this, target);
					intent.putExtra(ChartActivity.VEHICLE_ID, String.valueOf(mVehicle.getId()));
					startActivity(intent);
				}
			}
		});
	}

	public Cursor getCursor() {
		return managedQuery(CacheTable.BASE_URI, CacheTable.PROJECTION, CachedValue.ITEM + " = ? and " + CachedValue.VALID + " = ?", new String[] {
				String.valueOf(mVehicle.getId()),
				"1"
		}, CachedValue.GROUP + " asc, " + CachedValue.ORDER + " asc");
	}

	public void setProgressValue(int progress) {
		super.setProgress(progress);
		mProgressBar.setProgress(progress);
	}

	public void setMax(int max) {
		mProgressBar.setMax(max);
	}

	public void setProgressBarVisible(boolean visible) {
		mProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	public void setAdapter(Cursor c) {
		if (mAdapter == null) {
			String[] from = new String[] {
					CachedValue.KEY,
					CachedValue.VALUE
			};
			int[] to = new int[] {
					R.id.label,
					R.id.value
			};
			mAdapter = new SimpleCursorAdapter(this, R.layout.statistic, c, from, to);
		}
		mAdapter.setViewBinder(mViewBinder);
		mListView.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();
	}

	private void calculate() {
		mCalculationTask = new VehicleStatisticsTask();
		mCalculationTask.setActivity(this);
		mCalculationTask.execute();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return new Object[] {
				mCalculationTask,
				mAdapter
		};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 1, Menu.NONE, "Recalculate");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 1:
				calculate();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void loadVehicle() {
		long id = mVehicleSpinner.getSelectedItemId();
		Uri uri = ContentUris.withAppendedId(VehiclesTable.BASE_URI, id);
		Cursor vehicle = managedQuery(uri, VehiclesTable.PROJECTION, null, null, null);
		vehicle.moveToFirst();
		mVehicle.load(vehicle);
	}

	public Vehicle getVehicle() {
		return mVehicle;
	}

	public SimpleCursorAdapter getAdapter() {
		return mAdapter;
	}

	private final ViewBinder mViewBinder = new ViewBinder() {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			TextView textView = (TextView) view;
			String key = cursor.getString(2);
			Statistic statistic = Statistics.STRINGS.get(key);
			if (statistic != null) {
				// if it's null, then the cache is dead so ignore it
				switch (columnIndex) {
					case 2:
						// KEY
						textView.setText(statistic.getLabel(VehicleStatisticsActivity.this, mVehicle));
						return true;
					case 3:
						// VALUE
						textView.setText(statistic.format(VehicleStatisticsActivity.this, mVehicle, cursor.getDouble(3)));
						return true;
				}
			}
			return false;
		}
	};
}
