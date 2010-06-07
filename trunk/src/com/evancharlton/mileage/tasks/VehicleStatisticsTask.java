package com.evancharlton.mileage.tasks;

import java.util.ArrayList;
import java.util.HashMap;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.evancharlton.mileage.VehicleStatisticsActivity;
import com.evancharlton.mileage.dao.CachedValue;
import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.dao.FillupSeries;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.math.Calculator;
import com.evancharlton.mileage.provider.Statistics;
import com.evancharlton.mileage.provider.Statistics.Statistic;
import com.evancharlton.mileage.provider.tables.CacheTable;
import com.evancharlton.mileage.provider.tables.FillupsTable;

public class VehicleStatisticsTask extends AsyncTask<Cursor, Statistics.Statistic, Integer> {
	public VehicleStatisticsActivity activity;
	private final HashMap<String, Statistic> mStatistics = new HashMap<String, Statistic>();

	@Override
	protected void onPreExecute() {
		String[] args = new String[] {
			String.valueOf(activity.getVehicle().getId())
		};
		activity.getContentResolver().delete(CacheTable.BASE_URI, CachedValue.ITEM + " = ?", args);
	}

	@Override
	protected Integer doInBackground(Cursor... cursors) {
		String selection = Fillup.VEHICLE_ID + " = ?";
		String[] args = new String[] {
			String.valueOf(activity.getVehicle().getId())
		};
		Cursor cursor = activity.getContentResolver()
				.query(FillupsTable.BASE_URI, FillupsTable.PROJECTION, selection, args, Fillup.ODOMETER + " asc");
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

		final Vehicle vehicle = activity.getVehicle();
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

				long timestamp = fillup.getTimestamp();
				if (timestamp >= lastMonth) {
					lastMonthCost += fillup.getTotalCost();
					update(Statistics.LAST_MONTH_COST, lastMonthCost);
				}

				if (timestamp >= lastYear) {
					lastYearCost += fillup.getTotalCost();
					update(Statistics.LAST_YEAR_COST, lastYearCost);
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
		if (activity.getAdapter() == null) {
			activity.setAdapter(activity.getCursor());
		} else {
			activity.getAdapter().notifyDataSetChanged();
		}
	}
}
