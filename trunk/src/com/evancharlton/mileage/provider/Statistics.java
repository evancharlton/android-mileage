package com.evancharlton.mileage.provider;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;

import com.evancharlton.mileage.ChartActivity;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.charts.AverageCostChart;
import com.evancharlton.mileage.charts.AverageDistanceChart;
import com.evancharlton.mileage.charts.AverageFuelEconomyChart;
import com.evancharlton.mileage.charts.AveragePriceChart;
import com.evancharlton.mileage.charts.AverageVolumeChart;
import com.evancharlton.mileage.charts.BestFuelEconomyChart;
import com.evancharlton.mileage.charts.EastChart;
import com.evancharlton.mileage.charts.MaximumCostChart;
import com.evancharlton.mileage.charts.MaximumDistanceChart;
import com.evancharlton.mileage.charts.MaximumPriceChart;
import com.evancharlton.mileage.charts.MaximumVolumeChart;
import com.evancharlton.mileage.charts.MinimumCostChart;
import com.evancharlton.mileage.charts.MinimumDistanceChart;
import com.evancharlton.mileage.charts.MinimumPriceChart;
import com.evancharlton.mileage.charts.MinimumVolumeChart;
import com.evancharlton.mileage.charts.NorthChart;
import com.evancharlton.mileage.charts.SouthChart;
import com.evancharlton.mileage.charts.TotalCostChart;
import com.evancharlton.mileage.charts.TotalVolumeChart;
import com.evancharlton.mileage.charts.WestChart;
import com.evancharlton.mileage.charts.WorstFuelEconomyChart;
import com.evancharlton.mileage.dao.CachedValue;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.math.Calculator;

// There be dragons in this code. Tread with caution.
public final class Statistics {
	private static final int DISTANCE = 1;
	private static final int VOLUME = 2;
	private static final int ABBR_DISTANCE = 3;
	private static final int ECONOMY = 4;
	private static final int ABBR_VOLUME = 5;
	private static final int CURRENCY = 6;
	private static final int PER_DISTANCE = 7;

	public static final HashMap<String, Statistic> STRINGS = new HashMap<String, Statistic>();
	public static final ArrayList<Statistic> STATISTICS = new ArrayList<Statistic>();
	public static final ArrayList<StatisticsGroup> GROUPS = new ArrayList<StatisticsGroup>();

	public static final Statistic AVG_ECONOMY = new Statistic(new CachedValue("average_economy"), AverageFuelEconomyChart.class,
			R.string.stat_avg_economy, null, args(ECONOMY));
	public static final Statistic MIN_ECONOMY = new Statistic(new CachedValue("minimum_economy"), WorstFuelEconomyChart.class,
			R.string.stat_min_economy, null, args(ECONOMY));
	public static final Statistic MAX_ECONOMY = new Statistic(new CachedValue("maximum_economy"), BestFuelEconomyChart.class,
			R.string.stat_max_economy, null, args(ECONOMY));
	public static final StatisticsGroup ECONOMIES = new StatisticsGroup(R.string.stat_fuel_economy, AVG_ECONOMY, MIN_ECONOMY, MAX_ECONOMY);

	public static final Statistic AVG_DISTANCE = new Statistic(new CachedValue("average_distance"), AverageDistanceChart.class,
			R.string.stat_avg_distance, null, args(ABBR_DISTANCE));
	public static final Statistic MIN_DISTANCE = new Statistic(new CachedValue("minimum_distance"), MinimumDistanceChart.class,
			R.string.stat_min_distance, null, args(ABBR_DISTANCE));
	public static final Statistic MAX_DISTANCE = new Statistic(new CachedValue("maximum_distance"), MaximumDistanceChart.class,
			R.string.stat_max_distance, null, args(ABBR_DISTANCE));
	public static final StatisticsGroup DISTANCES = new StatisticsGroup(R.string.stat_distance_between_fillups, AVG_DISTANCE, MIN_DISTANCE,
			MAX_DISTANCE);

	public static final Statistic AVG_COST = new Statistic(new CachedValue("average_cost"), AverageCostChart.class, R.string.stat_avg_cost, null,
			args(0, CURRENCY));
	public static final Statistic MIN_COST = new Statistic(new CachedValue("minimum_cost"), MinimumCostChart.class, R.string.stat_min_cost, null,
			args(0, CURRENCY));
	public static final Statistic MAX_COST = new Statistic(new CachedValue("maximum_cost"), MaximumCostChart.class, R.string.stat_max_cost, null,
			args(0, CURRENCY));
	public static final Statistic TOTAL_COST = new Statistic(new CachedValue("total_cost"), TotalCostChart.class, R.string.stat_total_cost, null,
			args(0, CURRENCY));
	public static final Statistic MONTHLY_COST = new Statistic(new CachedValue("monthly_cost"), R.string.stat_month_cost, null, args(
			R.string.per_month, CURRENCY));
	public static final Statistic YEARLY_COST = new Statistic(new CachedValue("yearly_cost"), R.string.stat_year_cost, null, args(R.string.per_year,
			CURRENCY));
	public static final StatisticsGroup COSTS = new StatisticsGroup(R.string.stat_fillup_cost, AVG_COST, MIN_COST, MAX_COST, TOTAL_COST,
			MONTHLY_COST, YEARLY_COST);

	public static final Statistic AVG_COST_PER_DISTANCE = new Statistic(new CachedValue("average_cost_per_distance"),
			R.string.stat_avg_cost_per_distance, args(ABBR_DISTANCE), args(PER_DISTANCE, CURRENCY));
	public static final Statistic MIN_COST_PER_DISTANCE = new Statistic(new CachedValue("minimum_cost_per_distance"),
			R.string.stat_min_cost_per_distance, args(ABBR_DISTANCE), args(PER_DISTANCE, CURRENCY));
	public static final Statistic MAX_COST_PER_DISTANCE = new Statistic(new CachedValue("maximum_cost_per_distance"),
			R.string.stat_max_cost_per_distance, args(ABBR_DISTANCE), args(PER_DISTANCE, CURRENCY));
	public static final StatisticsGroup COSTS_PER_DISTANCE = new StatisticsGroup(R.string.stat_cost_per_distance, AVG_COST_PER_DISTANCE,
			MIN_COST_PER_DISTANCE, MAX_COST_PER_DISTANCE);

	public static final Statistic AVG_PRICE = new Statistic(new CachedValue("average_price"), AveragePriceChart.class, R.string.stat_avg_price, null,
			args(VOLUME, CURRENCY));
	public static final Statistic MIN_PRICE = new Statistic(new CachedValue("minimum_price"), MinimumPriceChart.class, R.string.stat_min_price, null,
			args(VOLUME, CURRENCY));
	public static final Statistic MAX_PRICE = new Statistic(new CachedValue("maximum_price"), MaximumPriceChart.class, R.string.stat_max_price, null,
			args(VOLUME, CURRENCY));
	public static final StatisticsGroup PRICES = new StatisticsGroup(R.string.stat_price, AVG_PRICE, MIN_PRICE, MAX_PRICE);

	public static final Statistic MIN_FUEL = new Statistic(new CachedValue("minimum_fuel"), MinimumVolumeChart.class, R.string.stat_min_fuel, null,
			args(VOLUME));
	public static final Statistic MAX_FUEL = new Statistic(new CachedValue("maximum_fuel"), MaximumVolumeChart.class, R.string.stat_max_fuel, null,
			args(VOLUME));
	public static final Statistic AVG_FUEL = new Statistic(new CachedValue("average_fuel"), AverageVolumeChart.class, R.string.stat_avg_fuel, null,
			args(VOLUME));
	public static final Statistic TOTAL_FUEL = new Statistic(new CachedValue("total_fuel"), TotalVolumeChart.class, R.string.stat_total_fuel, null,
			args(VOLUME));
	public static final Statistic FUEL_PER_YEAR = new Statistic(new CachedValue("fuel_per_year"), R.string.stat_fuel_per_year, null,
			args(R.string.per_year));
	public static final StatisticsGroup VOLUMES = new StatisticsGroup(R.string.stat_fuel, MIN_FUEL, MAX_FUEL, AVG_FUEL, TOTAL_FUEL, FUEL_PER_YEAR);

	public static final Statistic NORTH = new Statistic(new CachedValue("north"), NorthChart.class, R.string.stat_north, null, null);
	public static final Statistic SOUTH = new Statistic(new CachedValue("south"), SouthChart.class, R.string.stat_south, null, null);
	public static final Statistic EAST = new Statistic(new CachedValue("east"), EastChart.class, R.string.stat_east, null, null);
	public static final Statistic WEST = new Statistic(new CachedValue("west"), WestChart.class, R.string.stat_west, null, null);
	public static final StatisticsGroup LOCATION = new StatisticsGroup(R.string.stat_location, NORTH, SOUTH, EAST, WEST);

	public static class Statistic {
		private final int mLabel;
		private final int[] mLabelArgs;
		private final int[] mValueArgs;
		private final CachedValue mValue;
		private final Class<? extends ChartActivity> mChartClass;

		// FIXME: Remove this
		public Statistic(CachedValue value, int label, int[] labelArgs, int[] valueArgs) {
			this(value, null, label, labelArgs, valueArgs);
		}

		public Statistic(CachedValue value, Class<? extends ChartActivity> chartClass, int label, int[] labelArgs, int[] valueArgs) {
			STATISTICS.add(this);
			STRINGS.put(value.getKey(), this);
			mLabel = label;
			mValue = value;
			mChartClass = chartClass;
			mLabelArgs = labelArgs;
			mValueArgs = valueArgs;
		}

		public String getLabel(Context context, Vehicle vehicle) {
			if (mLabelArgs == null) {
				return context.getString(mLabel);
			}
			final int length = mLabelArgs.length;
			Object[] args = new String[length];
			for (int i = 0; i < length; i++) {
				switch (mLabelArgs[i]) {
					case ABBR_DISTANCE:
						args[i] = Calculator.getDistanceUnitsAbbr(context, vehicle);
						break;
					case ECONOMY:
						args[i] = Calculator.getEconomyUnitsAbbr(context, vehicle);
						break;
					case ABBR_VOLUME:
						args[i] = Calculator.getVolumeUnitsAbbr(context, vehicle);
						break;
					case DISTANCE:
						args[i] = Calculator.getDistanceUnits(context, vehicle);
						break;
					case VOLUME:
						args[i] = Calculator.getVolumeUnits(context, vehicle);
						break;
				}
			}
			return context.getString(mLabel, args);
		}

		public String getValueSuffix(Context context, Vehicle vehicle) {
			return getValue(context, vehicle, 0);
		}

		public String getValuePrefix(Context context, Vehicle vehicle) {
			return getValue(context, vehicle, 1);
		}

		private String getValue(Context context, Vehicle vehicle, int index) {
			if (mValueArgs != null && mValueArgs.length >= index + 1 && mValueArgs[index] != 0) {
				switch (mValueArgs[index]) {
					case ABBR_DISTANCE:
						return Calculator.getDistanceUnitsAbbr(context, vehicle);
					case ECONOMY:
						return Calculator.getEconomyUnitsAbbr(context, vehicle);
					case ABBR_VOLUME:
						return Calculator.getVolumeUnitsAbbr(context, vehicle);
					case DISTANCE:
						return Calculator.getDistanceUnits(context, vehicle);
					case VOLUME:
						return Calculator.getVolumeUnits(context, vehicle);
					case CURRENCY:
						return Calculator.getCurrencySymbol();
					case PER_DISTANCE:
						return context.getString(R.string.units_per, "", Calculator.getDistanceUnitsAbbr(context, vehicle));
					default:
						return context.getString(mValueArgs[index]);
				}
			}
			return "";
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

		public void setGroup(long group) {
			mValue.setGroup(group);
		}

		public long getGroup() {
			return mValue.getGroup();
		}

		public void setOrder(long order) {
			mValue.setOrder(order);
		}

		public long getOrder() {
			return mValue.getOrder();
		}

		public Class<? extends ChartActivity> getChartClass() {
			return mChartClass;
		}

		@Override
		public String toString() {
			return mValue.getKey() + " - " + mValue.getValue();
		}
	}

	public static class StatisticsGroup {
		private final ArrayList<Statistic> mStatistics = new ArrayList<Statistic>();
		private final int mLabel;

		public StatisticsGroup(int label, Statistic... statistics) {
			GROUPS.add(this);
			final int length = statistics.length;
			int group = GROUPS.size();
			for (int i = 0; i < length; i++) {
				statistics[i].setGroup(group);
				statistics[i].setOrder(i + 1);
				mStatistics.add(statistics[i]);
			}
			mLabel = label;
		}

		public int getLabel() {
			return mLabel;
		}
	}

	private static int[] args(int... args) {
		return args;
	}
}
