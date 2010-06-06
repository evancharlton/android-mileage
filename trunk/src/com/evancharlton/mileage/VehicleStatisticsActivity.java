package com.evancharlton.mileage;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

		Object[] saved = (Object[]) getLastNonConfigurationInstance();
		if (saved != null) {
			mCalculationTask = (CalculateTask) saved[0];
			mAdapter = null;
		}
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

	private Cursor getCursor() {
		return managedQuery(CacheTable.BASE_URI, CacheTable.PROJECTION, CachedValue.ITEM + " = ? and " + CachedValue.VALID + " = ?", new String[] {
				String.valueOf(mVehicle.getId()),
				"1"
		}, CachedValue.GROUP + " asc, " + CachedValue.ORDER + " asc");
	}

	private void setAdapter(Cursor c) {
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
		mCalculationTask = new CalculateTask();
		mCalculationTask.activity = this;
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

	private void populateCache(ArrayList<Statistic> statistics, boolean valid) {
		final long vehicleId = mVehicle.getId();
		ContentResolver resolver = getContentResolver();

		// clear the cache first
		String where = CachedValue.ITEM + " = ?";
		String[] selectionArgs = new String[] {
			String.valueOf(vehicleId)
		};
		resolver.delete(CacheTable.BASE_URI, where, selectionArgs);

		// fill with the new values
		final int numStats = statistics.size();
		ContentValues[] bulkValues = new ContentValues[numStats];
		int position = 0;
		for (int i = 0; i < numStats; i++) {
			Statistics.Statistic statistic = statistics.get(i);
			ContentValues values = new ContentValues();
			values.put(CachedValue.ITEM, vehicleId);
			values.put(CachedValue.KEY, statistic.getKey());
			values.put(CachedValue.VALID, valid);
			values.put(CachedValue.VALUE, statistic.getValue());
			values.put(CachedValue.GROUP, statistic.getGroup());
			values.put(CachedValue.ORDER, statistic.getOrder());
			bulkValues[position++] = values;
		}
		resolver.bulkInsert(CacheTable.BASE_URI, bulkValues);
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
			// math gets understandably funky around Double.(MAX|MIN)_VALUE
			final double MAX = 10000D;
			final double MIN = -10000D;

			double totalVolume = 0D;
			double firstVolume = -1D;
			double minVolume = MAX;
			double maxVolume = MIN;

			double minDistance = MAX;
			double maxDistance = MIN;

			double totalCost = 0D;
			double minCost = MAX;
			double maxCost = MIN;

			double minEconomy = MAX;
			double maxEconomy = MIN;

			double minCostPerDistance = MAX;
			double maxCostPerDistance = MIN;

			double minPrice = MAX;
			double maxPrice = MIN;

			double minLatitude = MAX;
			double maxLatitude = MIN;
			double minLongitude = MAX;
			double maxLongitude = MIN;

			final Vehicle vehicle = activity.mVehicle;
			while (cursor.moveToNext()) {
				Fillup fillup = new Fillup(cursor);
				series.add(fillup);

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

					double economy = Calculator.averageEconomy(vehicle, fillup);
					if (Calculator.isBetterEconomy(vehicle, economy, maxEconomy)) {
						maxEconomy = economy;
						update(Statistics.MAX_ECONOMY, maxEconomy);
					}
					if (!Calculator.isBetterEconomy(vehicle, economy, minEconomy)) {
						minEconomy = economy;
						update(Statistics.MIN_ECONOMY, minEconomy);
					}

					double costPerDistance = fillup.getCostPerDistance();
					if (costPerDistance > maxCostPerDistance) {
						maxCostPerDistance = costPerDistance;
						update(Statistics.MAX_COST_PER_DISTANCE, maxCostPerDistance);
					}
					if (costPerDistance < minCostPerDistance) {
						minCostPerDistance = costPerDistance;
						update(Statistics.MIN_COST_PER_DISTANCE, minCostPerDistance);
					}
				}

				double volume = fillup.getVolume();
				if (firstVolume == -1D) {
					firstVolume = volume;
				}
				if (volume > maxVolume) {
					maxVolume = volume;
					update(Statistics.MAX_FUEL, maxVolume);
				}
				if (volume < minVolume) {
					minVolume = volume;
					update(Statistics.MIN_FUEL, minVolume);
				}
				totalVolume += volume;
				update(Statistics.TOTAL_FUEL, totalVolume);

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

				double price = fillup.getUnitPrice();
				if (price > maxPrice) {
					maxPrice = price;
					update(Statistics.MAX_PRICE, maxPrice);
				}
				if (price < minPrice) {
					minPrice = price;
					update(Statistics.MIN_PRICE, minPrice);
				}

				double latitude = fillup.getLatitude();
				if (latitude > maxLatitude) {
					maxLatitude = latitude;
					update(Statistics.NORTH, maxLatitude);
				}
				if (latitude < minLatitude) {
					minLatitude = latitude;
					update(Statistics.SOUTH, minLatitude);
				}

				double longitude = fillup.getLongitude();
				if (longitude > maxLongitude) {
					maxLongitude = longitude;
					update(Statistics.EAST, maxLongitude);
				}
				if (longitude < minLongitude) {
					minLongitude = longitude;
					update(Statistics.WEST, minLongitude);
				}
			}
			double avgFuel = totalVolume / series.size();
			update(Statistics.AVG_FUEL, avgFuel);

			double avgEconomy = Calculator.averageEconomy(vehicle, series);
			update(Statistics.AVG_ECONOMY, avgEconomy);

			double avgDistance = Calculator.averageDistanceBetweenFillups(series);
			update(Statistics.AVG_DISTANCE, avgDistance);

			double avgCost = Calculator.averageFillupCost(series);
			update(Statistics.AVG_COST, avgCost);

			double avgCostPerDistance = Calculator.averageCostPerDistance(series);
			update(Statistics.AVG_COST_PER_DISTANCE, avgCostPerDistance);

			double avgPrice = Calculator.averagePrice(series);
			update(Statistics.AVG_PRICE, avgPrice);

			double fuelPerDay = Calculator.averageFuelPerDay(series);
			update(Statistics.FUEL_PER_YEAR, fuelPerDay * 365);

			double costPerDay = Calculator.averageCostPerDay(series);
			update(Statistics.AVG_MONTHLY_COST, costPerDay * 30);
			update(Statistics.AVG_YEARLY_COST, costPerDay * 365);

			cursor.close();
			return 0;
		}

		private void update(Statistics.Statistic statistic, double value) {
			statistic.setValue(value);
			mStatistics.put(statistic.getKey(), statistic);
		}

		@Override
		protected void onProgressUpdate(Statistics.Statistic... updates) {
			Statistic update = updates[0];
			mStatistics.put(update.getKey(), update);
		}

		@Override
		protected void onPostExecute(Integer done) {
			// FIXME: we can't do this on the UI thread
			Log.d("CalculateTask", "Done recalculating!");
			ArrayList<Statistic> stats = new ArrayList<Statistic>(mStatistics.values());
			activity.populateCache(stats, true);
			if (activity.mAdapter == null) {
				activity.setAdapter(activity.getCursor());
			} else {
				activity.mAdapter.notifyDataSetChanged();
			}
		}
	}

	private final ViewBinder mViewBinder = new ViewBinder() {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			TextView textView = (TextView) view;
			String key = cursor.getString(2);
			Statistic statistic = Statistics.STRINGS.get(key);
			switch (columnIndex) {
				case 2:
					// KEY
					textView.setText(statistic.getLabel(VehicleStatisticsActivity.this, mVehicle));
					return true;
				case 3:
					// VALUE
					String prefix = statistic.getValuePrefix(VehicleStatisticsActivity.this, mVehicle);
					String suffix = statistic.getValueSuffix(VehicleStatisticsActivity.this, mVehicle);
					textView.setText(prefix + cursor.getString(3) + suffix);
					return true;
			}
			return false;
		}
	};
}
