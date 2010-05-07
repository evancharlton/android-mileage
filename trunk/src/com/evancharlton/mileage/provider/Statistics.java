package com.evancharlton.mileage.provider;

import java.util.ArrayList;
import java.util.HashMap;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.CachedValue;

public final class Statistics {
	public enum Vehicle {
		TOTAL_FUEL, MIN_FUEL, MAX_FUEL, FUEL_PER_YEAR,

		TOTAL_COST, COST_LAST_MONTH, COST_LAST_YEAR,

		FARTHEST_NORTH, FARTHEST_SOUTH, FARTHEST_EAST, FARTHEST_WEST
	}

	public static final HashMap<String, Statistic> STRINGS = new HashMap<String, Statistic>();
	public static final ArrayList<Statistic> STATISTICS = new ArrayList<Statistic>();
	public static final ArrayList<StatisticsGroup> GROUPS = new ArrayList<StatisticsGroup>();

	public static final Statistic AVG_ECONOMY = new Statistic(R.string.stat_avg_economy, new CachedValue("average_economy"));
	public static final Statistic MIN_ECONOMY = new Statistic(R.string.stat_min_economy, new CachedValue("minimum_economy"));
	public static final Statistic MAX_ECONOMY = new Statistic(R.string.stat_max_economy, new CachedValue("maximum_economy"));
	public static final StatisticsGroup ECONOMIES = new StatisticsGroup(R.string.stat_fuel_economy, AVG_ECONOMY, MIN_ECONOMY, MAX_ECONOMY);

	public static final Statistic AVG_DISTANCE = new Statistic(R.string.stat_avg_distance, new CachedValue("average_distance"));
	public static final Statistic MIN_DISTANCE = new Statistic(R.string.stat_min_distance, new CachedValue("minimum_distance"));
	public static final Statistic MAX_DISTANCE = new Statistic(R.string.stat_max_distance, new CachedValue("maximum_distance"));
	public static final StatisticsGroup DISTANCES = new StatisticsGroup(R.string.stat_distance_between_fillups, AVG_DISTANCE, MIN_DISTANCE,
			MAX_DISTANCE);

	public static final Statistic AVG_COST = new Statistic(R.string.stat_avg_cost, new CachedValue("average_cost"));
	public static final Statistic MIN_COST = new Statistic(R.string.stat_min_cost, new CachedValue("minimum_cost"));
	public static final Statistic MAX_COST = new Statistic(R.string.stat_max_cost, new CachedValue("maximum_cost"));
	public static final Statistic TOTAL_COST = new Statistic(R.string.stat_total_cost, new CachedValue("total_cost"));
	public static final StatisticsGroup COSTS = new StatisticsGroup(R.string.stat_fillup_cost, AVG_COST, MIN_COST, MAX_COST);

	// public static final Statistic AVG_COST_PER_DISTANCE = new
	// Statistic(R.string.stat_avg_cost_per_distance, new CachedValue(
	// "average_cost_per_distance"));
	// public static final Statistic MIN_COST_PER_DISTANCE = new
	// Statistic(R.string.stat_min_cost_per_distance, new CachedValue(
	// "minimum_cost_per_distance"));
	// public static final Statistic MAX_COST_PER_DISTANCE = new
	// Statistic(R.string.stat_max_cost_per_distance, new CachedValue(
	// "maximum_cost_per_distance"));
	// public static final StatisticsGroup COSTS_PER_DISTANCE = new
	// StatisticsGroup(R.string.stat_cost_per_distance, AVG_COST_PER_DISTANCE,
	// MIN_COST_PER_DISTANCE, MAX_COST_PER_DISTANCE);
	//
	// public static final Statistic AVG_PRICE = new
	// Statistic(R.string.stat_avg_price, new CachedValue("average_price"));
	// public static final Statistic MIN_PRICE = new
	// Statistic(R.string.stat_min_price, new CachedValue("minimum_price"));
	// public static final Statistic MAX_PRICE = new
	// Statistic(R.string.stat_max_price, new CachedValue("maximum_price"));
	// public static final StatisticsGroup PRICES = new
	// StatisticsGroup(R.string.stat_price, AVG_PRICE, MIN_PRICE, MAX_PRICE);

	public static class Statistic {
		private final int mLabel;
		private final CachedValue mValue;

		public Statistic(int label, CachedValue value) {
			STATISTICS.add(this);
			STRINGS.put(value.getKey(), this);
			mLabel = label;
			mValue = value;
		}

		public int getLabel() {
			return mLabel;
		}

		public void setValue(double value) {
			mValue.setValue(value);
		}

		public double getValue() {
			return mValue.getValue();
		}

		public String getKey() {
			return mValue.getKey();
		}
	}

	public static class StatisticsGroup {
		private final ArrayList<Statistic> mStatistics = new ArrayList<Statistic>();
		private final int mLabel;

		public StatisticsGroup(int label, Statistic... statistics) {
			final int length = statistics.length;
			for (int i = 0; i < length; i++) {
				mStatistics.add(statistics[i]);
			}
			mLabel = label;
			GROUPS.add(this);
		}

		public int getLabel() {
			return mLabel;
		}
	}
}
