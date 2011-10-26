
package com.evancharlton.mileage.provider;

import com.evancharlton.mileage.ChartActivity;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.charts.AverageCostChart;
import com.evancharlton.mileage.charts.AverageDistanceChart;
import com.evancharlton.mileage.charts.AverageFuelEconomyChart;
import com.evancharlton.mileage.charts.AveragePriceChart;
import com.evancharlton.mileage.charts.AverageVolumeChart;
import com.evancharlton.mileage.charts.BestFuelEconomyChart;
import com.evancharlton.mileage.charts.EastChart;
import com.evancharlton.mileage.charts.LastMonthCostChart;
import com.evancharlton.mileage.charts.LastYearCostChart;
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

import android.content.Context;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

// There be dragons in this code. Tread with caution.
public final class Statistics {
    private static final int DISTANCE = 1;
    private static final int VOLUME = 2;
    private static final int ABBR_DISTANCE = 3;
    private static final int ECONOMY = 4;
    private static final int ABBR_VOLUME = 5;
    private static final int CURRENCY = 6;
    private static final int PER_DISTANCE = 7;

    // the length shouldn't ever exceed 200 chars, right?
    private static final StringBuilder BUILDER = new StringBuilder(200);

    public static final HashMap<String, Statistic> STRINGS = new HashMap<String, Statistic>();
    public static final ArrayList<Statistic> STATISTICS = new ArrayList<Statistic>();
    public static final ArrayList<StatisticsGroup> GROUPS = new ArrayList<StatisticsGroup>();

    public static final Statistic AVG_ECONOMY = new Statistic("avg_economy",
            AverageFuelEconomyChart.class, R.string.stat_avg_economy, ECONOMY);
    public static final Statistic MIN_ECONOMY = new Statistic("min_economy",
            WorstFuelEconomyChart.class, R.string.stat_min_economy, ECONOMY);
    public static final Statistic MAX_ECONOMY = new Statistic("max_economy",
            BestFuelEconomyChart.class, R.string.stat_max_economy, ECONOMY);
    public static final StatisticsGroup ECONOMIES = new StatisticsGroup(R.string.stat_fuel_economy,
            AVG_ECONOMY, MIN_ECONOMY, MAX_ECONOMY);

    public static final Statistic AVG_DISTANCE = new Statistic("avg_distance",
            AverageDistanceChart.class, R.string.stat_avg_distance, ABBR_DISTANCE);
    public static final Statistic MIN_DISTANCE = new Statistic("min_distance",
            MinimumDistanceChart.class, R.string.stat_min_distance, ABBR_DISTANCE);
    public static final Statistic MAX_DISTANCE = new Statistic("max_distance",
            MaximumDistanceChart.class, R.string.stat_max_distance, ABBR_DISTANCE);
    public static final StatisticsGroup DISTANCES = new StatisticsGroup(
            R.string.stat_distance_between_fillups, AVG_DISTANCE, MIN_DISTANCE,
            MAX_DISTANCE);

    public static final Statistic AVG_COST = new Statistic("avg_cost", AverageCostChart.class,
            R.string.stat_avg_cost, 0, CURRENCY);
    public static final Statistic MIN_COST = new Statistic("min_cost", MinimumCostChart.class,
            R.string.stat_min_cost, 0, CURRENCY);
    public static final Statistic MAX_COST = new Statistic("max_cost", MaximumCostChart.class,
            R.string.stat_max_cost, 0, CURRENCY);
    public static final Statistic TOTAL_COST = new Statistic("total_cost", TotalCostChart.class,
            R.string.stat_total_cost, 0, CURRENCY);
    public static final Statistic LAST_MONTH_COST = new Statistic("last_month_cost",
            LastMonthCostChart.class, R.string.stat_last_month_cost, 0,
            CURRENCY);
    public static final Statistic AVG_MONTHLY_COST = new Statistic("monthly_cost",
            R.string.stat_avg_month_cost, R.string.per_month, CURRENCY);
    public static final Statistic LAST_YEAR_COST = new Statistic("last_year_cost",
            LastYearCostChart.class, R.string.stat_last_year_cost, 0, CURRENCY);
    public static final Statistic AVG_YEARLY_COST = new Statistic("yearly_cost",
            R.string.stat_avg_year_cost, R.string.per_year, CURRENCY);
    public static final StatisticsGroup COSTS = new StatisticsGroup(R.string.stat_fillup_cost,
            AVG_COST, MIN_COST, MAX_COST, TOTAL_COST,
            LAST_MONTH_COST, AVG_MONTHLY_COST, LAST_YEAR_COST, AVG_YEARLY_COST);

    public static final Statistic AVG_COST_PER_DISTANCE = new Statistic("avg_cost_per_mi",
            R.string.stat_avg_cost_per_distance, args(ABBR_DISTANCE),
            PER_DISTANCE, CURRENCY);
    public static final Statistic MIN_COST_PER_DISTANCE = new Statistic("min_cost_per_mi",
            R.string.stat_min_cost_per_distance, args(ABBR_DISTANCE),
            PER_DISTANCE, CURRENCY);
    public static final Statistic MAX_COST_PER_DISTANCE = new Statistic("max_cost_per_mi",
            R.string.stat_max_cost_per_distance, args(ABBR_DISTANCE),
            PER_DISTANCE, CURRENCY);
    public static final StatisticsGroup COSTS_PER_DISTANCE = new StatisticsGroup(
            R.string.stat_cost_per_distance, AVG_COST_PER_DISTANCE,
            MIN_COST_PER_DISTANCE, MAX_COST_PER_DISTANCE);

    public static final Statistic AVG_PRICE = new Statistic("avg_price", AveragePriceChart.class,
            R.string.stat_avg_price, VOLUME, CURRENCY);
    public static final Statistic MIN_PRICE = new Statistic("min_price", MinimumPriceChart.class,
            R.string.stat_min_price, VOLUME, CURRENCY);
    public static final Statistic MAX_PRICE = new Statistic("max_price", MaximumPriceChart.class,
            R.string.stat_max_price, VOLUME, CURRENCY);
    public static final StatisticsGroup PRICES = new StatisticsGroup(R.string.stat_price,
            AVG_PRICE, MIN_PRICE, MAX_PRICE);

    public static final Statistic MIN_FUEL = new Statistic("min_fuel", MinimumVolumeChart.class,
            R.string.stat_min_fuel, VOLUME);
    public static final Statistic MAX_FUEL = new Statistic("max_fuel", MaximumVolumeChart.class,
            R.string.stat_max_fuel, VOLUME);
    public static final Statistic AVG_FUEL = new Statistic("avg_fuel", AverageVolumeChart.class,
            R.string.stat_avg_fuel, VOLUME);
    public static final Statistic TOTAL_FUEL = new Statistic("total_fuel", TotalVolumeChart.class,
            R.string.stat_total_fuel, VOLUME);
    public static final Statistic FUEL_PER_YEAR = new Statistic("fuel_per_year",
            R.string.stat_fuel_per_year, R.string.per_year);
    public static final StatisticsGroup VOLUMES = new StatisticsGroup(R.string.stat_fuel, MIN_FUEL,
            MAX_FUEL, AVG_FUEL, TOTAL_FUEL, FUEL_PER_YEAR);

    public static final Statistic NORTH = new Statistic("north", NorthChart.class,
            R.string.stat_north);
    public static final Statistic SOUTH = new Statistic("south", SouthChart.class,
            R.string.stat_south);
    public static final Statistic EAST = new Statistic("east", EastChart.class, R.string.stat_east);
    public static final Statistic WEST = new Statistic("west", WestChart.class, R.string.stat_west);
    public static final StatisticsGroup LOCATION = new StatisticsGroup(R.string.stat_location,
            NORTH, SOUTH, EAST, WEST);

    public static class Statistic {
        private final int mLabel;
        private final int[] mLabelArgs;
        private final int[] mValueArgs;
        private final CachedValue mValue;
        private final Class<? extends ChartActivity> mChartClass;
        private final DecimalFormat mFormatter = new DecimalFormat("0.00");

        private Statistic(String value, int label, int... valueArgs) {
            this(value, null, label, null, valueArgs);
        }

        private Statistic(String value, int label, int[] labelArgs, int... valueArgs) {
            this(value, null, label, labelArgs, valueArgs);
        }

        private Statistic(String value, Class<? extends ChartActivity> chartClass, int label,
                int... valueArgs) {
            this(value, chartClass, label, null, valueArgs);
        }

        private Statistic(String value, Class<? extends ChartActivity> chartClass, int label,
                int[] labelArgs, int... valueArgs) {
            STATISTICS.add(this);
            STRINGS.put(value, this);
            mLabel = label;
            mValue = new CachedValue(value);
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
            return " " + getValueTrimmings(context, vehicle, 0);
        }

        public String getValuePrefix(Context context, Vehicle vehicle) {
            return getValueTrimmings(context, vehicle, 1);
        }

        private String getValueTrimmings(Context context, Vehicle vehicle, int index) {
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
                        return Calculator.getCurrencySymbol(vehicle);
                    case PER_DISTANCE:
                        return context.getString(R.string.units_per, "",
                                Calculator.getDistanceUnitsAbbr(context, vehicle));
                    default:
                        return context.getString(mValueArgs[index]);
                }
            }
            return "";
        }

        public String format(final Context context, final Vehicle vehicle, final double value) {
            BUILDER.setLength(0);
            BUILDER.append(getValuePrefix(context, vehicle));
            if (mFormatter != null) {
                BUILDER.append(mFormatter.format(value));
            } else {
                BUILDER.append(String.valueOf(value));
            }
            BUILDER.append(getValueSuffix(context, vehicle));
            return BUILDER.toString();
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

        public ArrayList<Statistic> getStatistics() {
            return mStatistics;
        }
    }

    private static int[] args(int... args) {
        return args;
    }
}
