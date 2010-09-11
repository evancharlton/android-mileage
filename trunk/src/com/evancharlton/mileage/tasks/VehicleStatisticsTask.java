package com.evancharlton.mileage.tasks;

import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.evancharlton.mileage.VehicleStatisticsActivity;
import com.evancharlton.mileage.dao.CachedValue;
import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.dao.FillupSeries;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.exceptions.InvalidFieldException;
import com.evancharlton.mileage.math.Calculator;
import com.evancharlton.mileage.provider.Statistics;
import com.evancharlton.mileage.provider.tables.CacheTable;
import com.evancharlton.mileage.provider.tables.FillupsTable;

public class VehicleStatisticsTask extends AttachableAsyncTask<VehicleStatisticsActivity, Cursor, Integer, Integer> {
	private static final String TAG = "VehicleStatisticsTask";

	private ContentResolver mContentResolver;
	private int mProgress = 0;
	private int mTotal = 0;
	private ProgressBar mProgressBar;

	@Override
	public void attach(VehicleStatisticsActivity activity) {
		super.attach(activity);
		mContentResolver = activity.getContentResolver();
	}

	@Override
	protected void onPreExecute() {
		Log.d(TAG, "Calculation starting...");

		mProgressBar = getParent().getProgressBar();
		mProgressBar.setVisibility(View.VISIBLE);
		mProgressBar.setIndeterminate(true);
	}

	@Override
	protected Integer doInBackground(Cursor... cursors) {
		// delete the cache
		String[] args = new String[] {
			String.valueOf(getParent().getVehicle().getId())
		};
		mContentResolver.delete(CacheTable.BASE_URI, CachedValue.ITEM + " = ?", args);

		String selection = Fillup.VEHICLE_ID + " = ?";
		args = new String[] {
			String.valueOf(getParent().getVehicle().getId())
		};

		Cursor cursor = mContentResolver.query(FillupsTable.BASE_URI, FillupsTable.PROJECTION, selection, args, Fillup.ODOMETER + " asc");
		mTotal = cursor.getCount();

		if (mTotal <= 1) {
			Log.d(TAG, "Not enough fillups to calculate statistics");
			return 0;
		}

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

		double lastMonthCost = 0D;
		double lastYearCost = 0D;

		final long lastYear = System.currentTimeMillis() - Calculator.YEAR_MS;
		final long lastMonth = System.currentTimeMillis() - Calculator.MONTH_MS;

		final Vehicle vehicle = getParent().getVehicle();
		while (cursor.moveToNext()) {
			Fillup fillup = new Fillup(cursor);
			series.add(fillup);

			if (fillup.hasPrevious()) {
				double distance = fillup.getDistance();
				if (distance > maxDistance) {
					maxDistance = distance;
					update(Statistics.MAX_DISTANCE, maxDistance);
				} else {
					publishProgress();
				}

				if (distance < minDistance) {
					minDistance = distance;
					update(Statistics.MIN_DISTANCE, minDistance);
				} else {
					publishProgress();
				}

				double economy = Calculator.averageEconomy(vehicle, fillup);
				if (economy > 0D) {
					if (Calculator.isBetterEconomy(vehicle, economy, maxEconomy)) {
						maxEconomy = economy;
						update(Statistics.MAX_ECONOMY, maxEconomy);
					} else {
						publishProgress();
					}

					if (!Calculator.isBetterEconomy(vehicle, economy, minEconomy)) {
						minEconomy = economy;
						update(Statistics.MIN_ECONOMY, minEconomy);
					} else {
						publishProgress();
					}
				}

				double costPerDistance = fillup.getCostPerDistance();
				if (costPerDistance > maxCostPerDistance) {
					maxCostPerDistance = costPerDistance;
					update(Statistics.MAX_COST_PER_DISTANCE, maxCostPerDistance);
				} else {
					publishProgress();
				}

				if (costPerDistance < minCostPerDistance) {
					minCostPerDistance = costPerDistance;
					update(Statistics.MIN_COST_PER_DISTANCE, minCostPerDistance);
				} else {
					publishProgress();
				}

				long timestamp = fillup.getTimestamp();
				if (timestamp >= lastMonth) {
					lastMonthCost += fillup.getTotalCost();
					update(Statistics.LAST_MONTH_COST, lastMonthCost);
				} else {
					publishProgress();
				}

				if (timestamp >= lastYear) {
					lastYearCost += fillup.getTotalCost();
					update(Statistics.LAST_YEAR_COST, lastYearCost);
				} else {
					publishProgress();
				}

			} else {
				publishProgress(8);
			}

			double volume = fillup.getVolume();
			if (firstVolume == -1D) {
				firstVolume = volume;
			} else {
				publishProgress();
			}

			if (volume > maxVolume) {
				maxVolume = volume;
				update(Statistics.MAX_FUEL, maxVolume);
			} else {
				publishProgress();
			}

			if (volume < minVolume) {
				minVolume = volume;
				update(Statistics.MIN_FUEL, minVolume);
			} else {
				publishProgress();
			}

			totalVolume += volume;
			update(Statistics.TOTAL_FUEL, totalVolume);

			double cost = fillup.getTotalCost();
			if (cost > maxCost) {
				maxCost = cost;
				update(Statistics.MAX_COST, maxCost);
			} else {
				publishProgress();
			}

			if (cost < minCost) {
				minCost = cost;
				update(Statistics.MIN_COST, minCost);
			} else {
				publishProgress();
			}

			totalCost += cost;
			update(Statistics.TOTAL_COST, totalCost);

			double price = fillup.getUnitPrice();
			if (price > maxPrice) {
				maxPrice = price;
				update(Statistics.MAX_PRICE, maxPrice);
			} else {
				publishProgress();
			}

			if (price < minPrice) {
				minPrice = price;
				update(Statistics.MIN_PRICE, minPrice);
			} else {
				publishProgress();
			}

			double latitude = fillup.getLatitude();
			if (latitude > maxLatitude) {
				maxLatitude = latitude;
				update(Statistics.NORTH, maxLatitude);
			} else {
				publishProgress();
			}

			if (latitude < minLatitude) {
				minLatitude = latitude;
				update(Statistics.SOUTH, minLatitude);
			} else {
				publishProgress();
			}

			double longitude = fillup.getLongitude();
			if (longitude > maxLongitude) {
				maxLongitude = longitude;
				update(Statistics.EAST, maxLongitude);
			} else {
				publishProgress();
			}

			if (longitude < minLongitude) {
				minLongitude = longitude;
				update(Statistics.WEST, minLongitude);
			} else {
				publishProgress();
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

			try {
				fillup.saveIfChanged(getParent());
			} catch (InvalidFieldException e) {
				Log.e(TAG, "Couldn't save in-memory changes.", e);
			}
		}

		cursor.close();

		return 0;
	}

	private void update(Statistics.Statistic statistic, double value) {
		statistic.setValue(value);
		getParent().getAdapter().setValue(statistic, value);
		publishProgress();
	}

	@Override
	protected void onProgressUpdate(Integer... updates) {
		if (mTotal > 0) {
			mProgressBar.setIndeterminate(false);
			mProgressBar.setMax(mTotal * Statistics.STATISTICS.size());
			mTotal = 0;
		}
		if (updates.length > 0) {
			mProgress += updates[0];
		} else {
			mProgress += 1;
		}
		mProgressBar.setProgress(mProgress);
		getParent().getAdapter().notifyDataSetChanged();
	}

	@Override
	protected void onPostExecute(Integer done) {
		if (getParent().getAdapter() == null) {
			getParent().setAdapter(getParent().getCacheCursor());
		} else {
			getParent().getAdapter().notifyDataSetChanged();
		}
		mProgressBar.setVisibility(View.GONE);

		Log.d(TAG, "Done recalculating!");

		getParent().getAdapter().flush();
	}
}
