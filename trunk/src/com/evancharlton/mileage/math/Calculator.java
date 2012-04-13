
package com.evancharlton.mileage.math;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.dao.FillupSeries;
import com.evancharlton.mileage.dao.Vehicle;

import java.util.Currency;
import java.util.Date;
import java.util.Locale;

// TODO(future) - Does the name of this still make sense?
public class Calculator {
    // dates
    public static final long DAY_MILLIS = 1000L * 60L * 60L * 24L;

    public static final long MONTH_MS = DAY_MILLIS * 30L;

    public static final long YEAR_MS = DAY_MILLIS * 365L;

    public static final int DATE_DATE = 1;

    public static final int DATE_LONG = 2;

    public static final int DATE_MEDIUM = 3;

    public static final int DATE_TIME = 4;

    // distance
    public static final int KM = 1;

    public static final int MI = 2;

    // volume
    public static final int GALLONS = 3;

    public static final int LITRES = 4;

    public static final int IMPERIAL_GALLONS = 5;

    // economy
    public static final int MI_PER_GALLON = 6;

    public static final int KM_PER_GALLON = 7;

    public static final int MI_PER_IMP_GALLON = 8;

    public static final int KM_PER_IMP_GALLON = 9;

    public static final int MI_PER_LITRE = 10;

    public static final int KM_PER_LITRE = 11;

    public static final int GALLONS_PER_100KM = 12;

    public static final int LITRES_PER_100KM = 13;

    public static final int IMP_GAL_PER_100KM = 14;

    // cache
    private static String CURRENCY_SYMBOL = null;

    private static final java.text.DateFormat[] FORMATTERS = new java.text.DateFormat[4];

    private Calculator() {
        // no initialization
    }

    /**
     * Returns a positive integer if first is a *better* economy than second, a
     * negative integer if second is *better* than first, and 0 if the two are
     * equal.
     *
     * @param first value of the first economy
     * @param firstUnit units on the first economy
     * @param second value of the second economy
     * @param secondUnit units on the second economy
     * @return positive if first is better than second, negative if second is
     *         better, and 0 if equal
     */
    public static int compareEconomies(double first, int firstUnit, double second, int secondUnit) {
        if (firstUnit == secondUnit) {
            switch (firstUnit) {
                case GALLONS_PER_100KM:
                case LITRES_PER_100KM:
                case IMP_GAL_PER_100KM:
                    if (first < second) {
                        return 1;
                    } else if (first > second) {
                        return -1;
                    }
                    return 0;
                case MI_PER_GALLON:
                case KM_PER_GALLON:
                case MI_PER_IMP_GALLON:
                case KM_PER_IMP_GALLON:
                case MI_PER_LITRE:
                case KM_PER_LITRE:
                default:
                    if (first > second) {
                        return 1;
                    } else if (first < second) {
                        return -1;
                    }
                    return 0;
            }
        } else {
            double converted = convert(second, secondUnit, firstUnit);
            return compareEconomies(first, firstUnit, converted, firstUnit);
        }
    }

    public static double averageEconomy(Vehicle vehicle, Fillup fillup) {
        if (!fillup.hasPrevious()) {
            throw new IllegalArgumentException("You can't calculate economy on one fillup");
        }
        if (fillup.isPartial()) {
            return 0D;
        }
        Fillup clone = (Fillup) fillup.getPrevious().clone();
        clone.setPrevious(null);
        return averageEconomy(vehicle, new FillupSeries(clone, fillup));
    }

    /**
     * Calculate the economy of the most recent fillup of the series.
     *
     * @param vehicle
     * @param series
     * @return
     */
    public static double fillupEconomy(Vehicle vehicle, FillupSeries series) {
        Fillup current = series.last();

        if (current.isPartial()) {
            return 0D;
        }

        double nextValidOdometer = 0D;
        double topOdometer = current.getOdometer();

        double volume = 0D;
        while (current.hasPrevious()) {
            volume += current.getVolume();
            current = current.getPrevious();
            nextValidOdometer = current.getOdometer();
            if (!current.isPartial()) {
                break;
            }
        }

        double distance = topOdometer - nextValidOdometer;
        return getEconomy(vehicle, distance, volume);
    }

    /**
     * @param vehicle
     * @param first
     * @param second
     * @return true if first is BETTER than second
     */
    public static boolean isBetterEconomy(Vehicle vehicle, double first, double second) {
        switch (vehicle.getEconomyUnits()) {
            case GALLONS_PER_100KM:
            case LITRES_PER_100KM:
            case IMP_GAL_PER_100KM:
                return first <= second;
        }
        return first >= second;
    }

    public static double averageEconomy(Vehicle vehicle, FillupSeries series) {
        return getEconomy(vehicle, series.getTotalDistance(), series.getEconomyVolume());
    }

    private static double getEconomy(Vehicle vehicle, double distance, double volume) {
        // ALL CALCULATIONS ARE DONE IN MPG AND CONVERTED LATER
        double miles = convert(distance, vehicle.getDistanceUnits(), MI);
        double gallons = convert(volume, vehicle.getVolumeUnits(), GALLONS);

        switch (vehicle.getEconomyUnits()) {
            case KM_PER_GALLON:
                return convert(miles, KM) / gallons;
            case MI_PER_IMP_GALLON:
                return miles / convert(gallons, IMPERIAL_GALLONS);
            case KM_PER_IMP_GALLON:
                return convert(miles, KM) / convert(gallons, IMPERIAL_GALLONS);
            case MI_PER_LITRE:
                return miles / convert(gallons, LITRES);
            case KM_PER_LITRE:
                return convert(miles, KM) / convert(gallons, LITRES);
            case GALLONS_PER_100KM:
                return (100D * gallons) / convert(miles, KM);
            case LITRES_PER_100KM:
                return (100D * convert(gallons, LITRES)) / convert(miles, KM);
            case IMP_GAL_PER_100KM:
                return (100D * convert(gallons, IMPERIAL_GALLONS)) / convert(miles, KM);
            case MI_PER_GALLON:
            default:
                return miles / gallons;
        }
    }

    public static double averageDistanceBetweenFillups(FillupSeries series) {
        return series.getTotalDistance() / (series.size() - 1);
    }

    public static double averageFillupVolume(FillupSeries series) {
        return series.getTotalVolume() / series.size();
    }

    public static double averageFillupCost(FillupSeries series) {
        return series.getTotalCost() / series.size();
    }

    public static double averageCostPerDistance(FillupSeries series) {
        if (series.size() <= 1) {
            return 0D;
        }

        double totalCost = series.getTotalCost();
        double totalDistance = series.getTotalDistance();

        // We need to subtract out the cost for the very first fillup because
        // it does not have a distance associated with it.
        totalCost -= series.get(0).getTotalCost();

        return (totalCost / totalDistance);
    }

    public static double averageFuelPerDay(FillupSeries series) {
        long timeRange = series.getTimeRange();
        double numDays = Math.ceil((double) timeRange / (double) DAY_MILLIS);
        return series.getTotalVolume() / numDays;
    }

    public static double averageCostPerDay(FillupSeries series) {
        long timeRange = series.getTimeRange();
        double numDays = Math.ceil((double) timeRange / (double) DAY_MILLIS);
        return series.getTotalCost() / numDays;
    }

    public static double averagePrice(FillupSeries series) {
        double total = 0D;
        final int SIZE = series.size();
        for (int i = 0; i < SIZE; i++) {
            Fillup fillup = series.get(i);
            total += fillup.getUnitPrice();
        }
        return total / SIZE;
    }

    // yes, this method makes it possible to convert from miles to litres.
    // if you do this, I'll hunt you down and beat you with a rubber hose.
    public static double convert(double value, int from, int to) {
        // going from whatever to miles or gallons (depending on context)
        switch (from) {
            case KM:
                value *= 0.621371192;
                break;
            case LITRES:
                value *= 0.264172052;
                break;
            case IMPERIAL_GALLONS:
                value *= 1.20095042;
                break;
            case KM_PER_GALLON:
                value *= 0.621371192;
                break;
            case MI_PER_IMP_GALLON:
                value *= 0.83267384;
                break;
            case KM_PER_IMP_GALLON:
                value *= 0.517399537;
                break;
            case MI_PER_LITRE:
                value *= 3.78541178;
                break;
            case KM_PER_LITRE:
                value *= 2.35214583;
                break;
            case GALLONS_PER_100KM:
                value *= 62.1371192;
                break;
            case LITRES_PER_100KM:
                value *= 235.214583;
                break;
            case IMP_GAL_PER_100KM:
                value *= 51.7399537;
                break;
            case MI:
            case GALLONS:
            default:
                break;
        }
        // at this point, "value" is either miles or gallons
        return convert(value, to);
    }

    // convert from (miles|gallons) to the other unit
    private static double convert(double value, int to) {
        // value is now converted to miles or gallons
        switch (to) {
            case MI:
                return value;
            case KM:
                return value /= 0.621371192;
            case GALLONS:
                return value;
            case LITRES:
                return value /= 0.264172052;
            case IMPERIAL_GALLONS:
                return value /= 1.20095042;
            case MI_PER_GALLON:
                return value;
            case MI_PER_LITRE:
                return value *= 0.264172052;
            case MI_PER_IMP_GALLON:
                return value *= 1.20095042;
            case KM_PER_GALLON:
                return value *= 1.609344;
            case KM_PER_LITRE:
                return value *= 0.425143707;
            case KM_PER_IMP_GALLON:
                return value *= 1.93274236;
            case GALLONS_PER_100KM:
                return value *= 62.1371192;
            case LITRES_PER_100KM:
                return value *= 235.214583;
            case IMP_GAL_PER_100KM:
                return value *= 51.7399537;
        }
        return value;
    }

    public static String getVolumeUnits(Context context, Vehicle vehicle) {
        switch (vehicle.getVolumeUnits()) {
            case LITRES:
                return context.getString(R.string.units_litres);
            case IMPERIAL_GALLONS:
            case GALLONS:
            default:
                return context.getString(R.string.units_gallons);
        }
    }

    public static String getVolumeUnitsAbbr(Context context, Vehicle vehicle) {
        switch (vehicle.getVolumeUnits()) {
            case LITRES:
                return context.getString(R.string.units_litres_abbr);
            case IMPERIAL_GALLONS:
            case GALLONS:
            default:
                return context.getString(R.string.units_gallons_abbr);
        }
    }

    public static String getDistanceUnits(Context context, Vehicle vehicle) {
        switch (vehicle.getDistanceUnits()) {
            case KM:
                return context.getString(R.string.units_kilometers);
            case MI:
            default:
                return context.getString(R.string.units_miles);
        }
    }

    public static String getDistanceUnitsAbbr(Context context, Vehicle vehicle) {
        switch (vehicle.getDistanceUnits()) {
            case KM:
                return context.getString(R.string.units_kilometers_abbr);
            case MI:
            default:
                return context.getString(R.string.units_miles_abbr);
        }
    }

    public static String getEconomyUnitsAbbr(Context context, Vehicle vehicle) {
        switch (vehicle.getEconomyUnits()) {
            case KM_PER_GALLON:
            case KM_PER_IMP_GALLON:
                return context.getString(R.string.units_kmpg);
            case MI_PER_LITRE:
                return context.getString(R.string.units_mpl);
            case KM_PER_LITRE:
                return context.getString(R.string.units_kmpl);
            case LITRES_PER_100KM:
                return context.getString(R.string.units_lpckm);
            case GALLONS_PER_100KM:
            case IMP_GAL_PER_100KM:
                return context.getString(R.string.units_gpckm);
            case MI_PER_GALLON:
            case MI_PER_IMP_GALLON:
            default:
                return context.getString(R.string.units_mpg);
        }
    }

    public static String getCurrencySymbol(Vehicle vehicle) {
        String savedCurrency = vehicle.getCurrency();
        if (TextUtils.isEmpty(savedCurrency)) {
            savedCurrency = getCurrencySymbol();
        }
        return savedCurrency;
    }

    public static String getCurrencySymbol() {
        return Currency.getInstance(Locale.getDefault()).getSymbol();
    }

    public static String getDateString(Context context, int type, Date date) {
        if (FORMATTERS[type] == null) {
            switch (type) {
                case DATE_DATE:
                    FORMATTERS[DATE_DATE] = DateFormat.getDateFormat(context);
                    break;
                case DATE_LONG:
                    FORMATTERS[DATE_LONG] = DateFormat.getTimeFormat(context);
                    break;
                case DATE_MEDIUM:
                    FORMATTERS[DATE_MEDIUM] = DateFormat.getMediumDateFormat(context);
                    break;
                case DATE_TIME:
                    FORMATTERS[DATE_TIME] = DateFormat.getMediumDateFormat(context);
            }
        }

        return FORMATTERS[type].format(date);
    }
};
