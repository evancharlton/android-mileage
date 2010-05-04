package com.evancharlton.mileage.provider;

import java.util.ArrayList;
import java.util.HashMap;

import com.evancharlton.mileage.R;

public final class Statistics {
	public enum Vehicle {
		AVG_COST_PER_DISTANCE, MIN_COST_PER_DISTANCE, MAX_COST_PER_DISTANCE,

		AVG_PRICE, MIN_PRICE, MAX_PRICE,

		TOTAL_FUEL, MIN_FUEL, MAX_FUEL, FUEL_PER_YEAR,

		TOTAL_COST, COST_LAST_MONTH, COST_LAST_YEAR,

		FARTHEST_NORTH, FARTHEST_SOUTH, FARTHEST_EAST, FARTHEST_WEST
	}

	public static final HashMap<String, Statistic> STATISTICS = new HashMap<String, Statistic>();
	public static final ArrayList<StatisticsGroup> GROUPS = new ArrayList<StatisticsGroup>();

	public static final Statistic AVG_ECONOMY = new Statistic(R.string.stat_avg_economy, "average_economy");
	public static final Statistic MIN_ECONOMY = new Statistic(R.string.stat_min_economy, "minimum_economy");
	public static final Statistic MAX_ECONOMY = new Statistic(R.string.stat_max_economy, "maximum_economy");
	public static final StatisticsGroup ECONOMIES = new StatisticsGroup(R.string.stat_fuel_economy, AVG_ECONOMY, MIN_ECONOMY, MAX_ECONOMY);

	public static final Statistic AVG_DISTANCE = new Statistic(R.string.stat_avg_distance, "average_distance");
	public static final Statistic MIN_DISTANCE = new Statistic(R.string.stat_min_distance, "minimum_distance");
	public static final Statistic MAX_DISTANCE = new Statistic(R.string.stat_max_distance, "maximum_distance");
	public static final StatisticsGroup DISTANCES = new StatisticsGroup(R.string.stat_distance_between_fillups, AVG_DISTANCE, MIN_DISTANCE,
			MAX_DISTANCE);

	public static final Statistic AVG_COST = new Statistic(R.string.stat_avg_cost, "average_cost");
	public static final Statistic MIN_COST = new Statistic(R.string.stat_min_cost, "minimum_cost");
	public static final Statistic MAX_COST = new Statistic(R.string.stat_max_cost, "maximum_cost");
	public static final Statistic TOTAL_COST = new Statistic(R.string.stat_total_cost, "total_cost");
	public static final StatisticsGroup COSTS = new StatisticsGroup(R.string.stat_fillup_cost, AVG_COST, MIN_COST, MAX_COST);

	// TODO: merge with the statistic dao?
	public static class Statistic {
		private final String mKey;
		private final int mLabel;
		private double mValue = 0D;

		public Statistic(int label, String key) {
			mKey = key;
			mLabel = label;
			STATISTICS.put(key, this);
		}

		public int getLabel() {
			return mLabel;
		}

		public double getValue() {
			return mValue;
		}

		public void setValue(double value) {
			mValue = value;
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
