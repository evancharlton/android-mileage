package com.evancharlton.mileage;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

import com.evancharlton.mileage.dao.CachedValue;
import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.dao.FillupSeries;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.math.Calculator;
import com.evancharlton.mileage.provider.Statistics;
import com.evancharlton.mileage.provider.Statistics.Statistic;
import com.evancharlton.mileage.provider.tables.CacheTable;
import com.evancharlton.mileage.provider.tables.FillupsTable;
import com.evancharlton.mileage.provider.tables.VehiclesTable;

public class VehicleStatisticsActivity extends Activity {
	private static final String TAG = "VehicleStatisticsActivity";

	private final Vehicle mVehicle = new Vehicle(new ContentValues());

	private Spinner mVehicleSpinner;
	private ListView mListView;
	private CalculateTask mCalculationTask = null;

	private SimpleCursorAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vehicle_statistics);

		mCalculationTask = (CalculateTask) getLastNonConfigurationInstance();
		if (mCalculationTask != null) {
			mCalculationTask.activity = this;
		}

		mListView = (ListView) findViewById(android.R.id.list);
		mVehicleSpinner = (Spinner) findViewById(R.id.vehicle);

		loadVehicle();
		Cursor c = getCursor();

		final ArrayList<Statistics.Statistic> statistics = Statistics.STATISTICS;
		final int numStats = statistics.size();
		Log.d(TAG, "Checking statistics ... " + numStats);
		if (c.getCount() < numStats) {
			populateCache(Statistics.STATISTICS, false);
			// kick off the task
			calculate();
		} else {
			setAdapter(c);
		}
	}

	private Cursor getCursor() {
		return managedQuery(CacheTable.BASE_URI, CacheTable.PROJECTION, CachedValue.ITEM + " = ? and " + CachedValue.VALID + " = ?", new String[] {
				String.valueOf(mVehicle.getId()),
				"1"
		}, null);
	}

	private void setAdapter(Cursor c) {
		String[] from = new String[] {
				CachedValue.KEY,
				CachedValue.VALUE
		};
		int[] to = new int[] {
				R.id.label,
				R.id.value
		};
		mAdapter = new SimpleCursorAdapter(this, R.layout.statistic, c, from, to);
		mAdapter.setViewBinder(mViewBinder);
		mListView.setAdapter(mAdapter);
	}

	private void calculate() {
		mCalculationTask = new CalculateTask();
		mCalculationTask.activity = this;
		mCalculationTask.execute();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return mCalculationTask;
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

	private void populateCache(ArrayList<Statistic> statistics, boolean valid) {
		final int numStats = statistics.size();
		ContentValues[] bulkValues = new ContentValues[numStats];
		final long vehicleId = mVehicle.getId();
		int position = 0;
		for (int i = 0; i < numStats; i++) {
			Statistics.Statistic statistic = statistics.get(i);
			ContentValues values = new ContentValues();
			values.put(CachedValue.ITEM, vehicleId);
			values.put(CachedValue.KEY, statistic.getKey());
			values.put(CachedValue.VALID, valid);
			values.put(CachedValue.VALUE, statistic.getValue());
			bulkValues[position++] = values;
		}
		getContentResolver().bulkInsert(CacheTable.BASE_URI, bulkValues);
	}

	private static class CalculateTask extends AsyncTask<Cursor, Statistics.Statistic, Integer> {
		public VehicleStatisticsActivity activity;
		private final HashMap<String, Statistic> mStatistics = new HashMap<String, Statistic>();

		@Override
		protected void onPreExecute() {
			String[] args = new String[] {
				String.valueOf(activity.mVehicle.getId())
			};
			activity.getContentResolver().delete(CacheTable.BASE_URI, CachedValue.ITEM + " = ?", args);
		}

		@Override
		protected Integer doInBackground(Cursor... cursors) {
			String selection = Fillup.VEHICLE_ID + " = ?";
			String[] args = new String[] {
				String.valueOf(activity.mVehicle.getId())
			};
			Cursor cursor = activity.getContentResolver().query(FillupsTable.BASE_URI, FillupsTable.PROJECTION, selection, args,
					Fillup.ODOMETER + " asc");
			Log.d("CalculateTask", "Recalculating...");
			// recalculate a whole bunch of shit
			FillupSeries series = new FillupSeries();
			double minOdometer = Double.MAX_VALUE;
			double maxOdometer = Double.MIN_VALUE;

			double totalVolume = 0D;
			double firstVolume = -1D;
			double minVolume = Double.MAX_VALUE;
			double maxVolume = Double.MIN_VALUE;

			double minDistance = Double.MAX_VALUE;
			double maxDistance = Double.MIN_VALUE;

			double totalCost = 0D;
			double minCost = Double.MAX_VALUE;
			double maxCost = Double.MIN_VALUE;

			double minEconomy = Double.MAX_VALUE;
			double maxEconomy = Double.MIN_VALUE;

			final Vehicle vehicle = activity.mVehicle;
			while (cursor.moveToNext()) {
				Fillup fillup = new Fillup(cursor);
				series.add(fillup);

				double odometer = fillup.getOdometer();
				if (odometer < minOdometer) {
					minOdometer = odometer;
				}
				if (odometer > maxOdometer) {
					maxOdometer = odometer;
				}

				if (fillup.hasPrevious()) {
					double distance = fillup.getDistance();
					if (distance > maxDistance) {
						maxDistance = distance;
						update(Statistics.MAX_DISTANCE, maxDistance);
					}
					if (distance < minDistance) {
						minDistance = distance;
						update(Statistics.MIN_DISTANCE, minDistance);
					}
				}

				double volume = fillup.getVolume();
				if (firstVolume == -1D) {
					firstVolume = volume;
				}
				if (volume > maxVolume) {
					maxVolume = volume;
				}
				if (volume < minVolume) {
					minVolume = volume;
				}
				totalVolume += volume;

				double cost = fillup.getTotalCost();
				if (cost > maxCost) {
					maxCost = cost;
					update(Statistics.MAX_COST, maxCost);
				}
				if (cost < minCost) {
					minCost = cost;
					update(Statistics.MIN_COST, minCost);
				}
				totalCost += cost;
				update(Statistics.TOTAL_COST, totalCost);

				if (fillup.hasPrevious()) {
					double economy = Calculator.averageEconomy(vehicle, fillup);
					if (economy > maxEconomy) {
						maxEconomy = economy;
						update(Statistics.MAX_ECONOMY, maxEconomy);
					}
					if (economy < minEconomy) {
						minEconomy = economy;
						update(Statistics.MIN_ECONOMY, minEconomy);
					}
				}
			}
			final int NUM_FILLUPS = series.size();

			double avgEconomy = Calculator.averageEconomy(vehicle, series);
			update(Statistics.AVG_ECONOMY, avgEconomy);

			double avgDistance = series.getTotalDistance() / (NUM_FILLUPS - 1);
			update(Statistics.AVG_DISTANCE, avgDistance);

			double avgCost = totalCost / series.size();
			update(Statistics.AVG_COST, avgCost);

			cursor.close();
			return 0;
		}

		private void update(Statistics.Statistic statistic, double value) {
			statistic.setValue(value);
			publishProgress(statistic);
		}

		@Override
		protected void onProgressUpdate(Statistics.Statistic... updates) {
			Statistic update = updates[0];
			mStatistics.put(update.getKey(), update);
		}

		@Override
		protected void onPostExecute(Integer done) {
			Log.d("CalculateTask", "Done recalculating!");
			ArrayList<Statistic> stats = new ArrayList<Statistic>(mStatistics.values());
			activity.populateCache(stats, true);
			activity.setAdapter(activity.getCursor());
		}
	}

	private final ViewBinder mViewBinder = new ViewBinder() {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			TextView textView = (TextView) view;
			switch (columnIndex) {
				case 2:
					// KEY
					String key = cursor.getString(columnIndex);
					textView.setText(Statistics.STRINGS.get(key).getLabel());
					return true;
			}
			return false;
		}
	};
}
