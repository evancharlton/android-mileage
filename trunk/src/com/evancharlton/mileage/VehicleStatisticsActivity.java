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
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.dao.FillupSeries;
import com.evancharlton.mileage.dao.Statistic;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.math.Calculator;
import com.evancharlton.mileage.provider.Statistics;
import com.evancharlton.mileage.provider.tables.CacheTable;
import com.evancharlton.mileage.provider.tables.FillupsTable;
import com.evancharlton.mileage.provider.tables.VehiclesTable;
import com.evancharlton.mileage.views.StatisticView;

public class VehicleStatisticsActivity extends Activity {
	private static final String TAG = "VehicleStatisticsActivity";

	private final ArrayList<Fillup> mFillups = new ArrayList<Fillup>();
	private final HashMap<Statistics.Statistic, Double> mStatistics = new HashMap<Statistics.Statistic, Double>();
	private final Vehicle mVehicle = new Vehicle(new ContentValues());

	private LinearLayout mContainer;
	private Spinner mVehicleSpinner;
	private CalculateTask mCalculationTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vehicle_statistics);

		mCalculationTask = (CalculateTask) getLastNonConfigurationInstance();
		if (mCalculationTask != null) {
			mCalculationTask.activity = this;
		}

		mVehicleSpinner = (Spinner) findViewById(R.id.vehicle);
		mContainer = (LinearLayout) findViewById(R.id.container);

		loadVehicle();
		preloadStatisticsCache();
		loadStatisticsFromDatabase();

		showStatistics();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return mCalculationTask;
	}

	private void loadVehicle() {
		long id = mVehicleSpinner.getSelectedItemId();
		Uri uri = ContentUris.withAppendedId(VehiclesTable.BASE_URI, id);
		Cursor vehicle = managedQuery(uri, VehiclesTable.PROJECTION, null, null, null);
		vehicle.moveToFirst();
		mVehicle.load(vehicle);
	}

	private void preloadStatisticsCache() {
		// preload the local statistics cache
		for (Statistics.Statistic statistic : Statistics.STATISTICS.values()) {
			mStatistics.put(statistic, null);
		}
	}

	private void loadStatisticsFromDatabase() {
		String query = Statistic.ITEM + " = ? AND " + Statistic.VALID + " = 1";
		String[] args = new String[] {
			mVehicle.getUri().toString()
		};
		Cursor cached = managedQuery(CacheTable.BASE_URI, CacheTable.PROJECTION, query, args, null);
		while (cached.moveToNext()) {
			String key = cached.getString(2);
			Double value = cached.getDouble(3);
			Statistics.Statistic stat = Statistics.STATISTICS.get(key);
			if (stat != null) {
				mStatistics.put(stat, value);
			}
		}
	}

	private void showStatistics() {
		// find all of the dirty statistics
		ArrayList<Statistics.Statistic> dirtyStatistics = new ArrayList<Statistics.Statistic>();
		for (Statistics.Statistic statistic : mStatistics.keySet()) {
			if (mStatistics.get(statistic) == null) {
				dirtyStatistics.add(statistic);
			}
		}

		// if we have any dirty statistics, recalculate
		if (dirtyStatistics.size() > 0) {
			String selection = Fillup.VEHICLE_ID + " = ?";
			String[] selectionArgs = new String[] {
				String.valueOf(mVehicleSpinner.getSelectedItemId())
			};
			Cursor fillups = managedQuery(FillupsTable.BASE_URI, FillupsTable.PROJECTION, selection, selectionArgs, null);
			mCalculationTask = new CalculateTask();
			mCalculationTask.activity = this;
			mCalculationTask.execute(fillups);
		}

		// add all of the statistics to the UI with placeholders (or data)
		displayStatistics();
	}

	private void displayStatistics() {
		LayoutInflater inflater = LayoutInflater.from(this);
		for (Statistics.Statistic stat : Statistics.STATISTICS.values()) {
			StatisticView view = (StatisticView) inflater.inflate(R.layout.statistic, mContainer);
			view.update(stat);
		}
	}

	private void updateStatistic(Statistics.Statistic statistic) {
		// TODO
	}

	private static class CalculateTask extends AsyncTask<Cursor, Statistics.Statistic, Integer> {
		public VehicleStatisticsActivity activity;

		@Override
		protected Integer doInBackground(Cursor... cursors) {
			// recalculate a whole bunch of shit
			Cursor cursor = cursors[0];
			FillupSeries series = new FillupSeries();
			double minOdometer = Double.MAX_VALUE;
			double maxOdometer = Double.MIN_VALUE;
			double totalVolume = 0D;
			double firstVolume = -1D;
			while (cursor.moveToNext()) {
				Fillup fillup = new Fillup(cursor);

				double odometer = fillup.getOdometer();
				if (odometer < minOdometer) {
					minOdometer = odometer;
				}
				if (odometer > maxOdometer) {
					maxOdometer = odometer;
				}

				double volume = fillup.getVolume();
				if (firstVolume == -1D) {
					firstVolume = volume;
				}
				totalVolume += volume;

				series.add(fillup);
			}
			double avgEconomy = Calculator.averageEconomy(activity.mVehicle, series);
			Statistics.AVG_ECONOMY.setValue(avgEconomy);
			publishProgress(Statistics.AVG_ECONOMY);

			return 0;
		}

		@Override
		protected void onProgressUpdate(Statistics.Statistic... updates) {
			for (int i = 0; i < updates.length; i++) {
				activity.updateStatistic(updates[i]);
			}
		}
	}
}
