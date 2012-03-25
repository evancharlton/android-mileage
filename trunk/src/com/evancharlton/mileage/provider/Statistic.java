
package com.evancharlton.mileage.provider;

import com.evancharlton.mileage.ChartActivity;
import com.evancharlton.mileage.dao.CachedValue;
import com.evancharlton.mileage.dao.Vehicle;

import android.content.Context;

import java.text.DecimalFormat;

public abstract class Statistic {
    protected final int mLabel;

    private final CachedValue mValue;

    private final Class<? extends ChartActivity> mChartClass;

    protected final DecimalFormat mFormatter = new DecimalFormat("0.00");

    public Statistic(int label) {
        this(null, null, label);
    }

    public Statistic(String value, Class<? extends ChartActivity> chartClass, int label) {
        if (value != null) {
            Statistics.STATISTICS.add(this);
        }
        mLabel = label;
        mValue = new CachedValue(value);
        mChartClass = chartClass;
    }

    public abstract String format(final Context context, final Vehicle vehicle, final double value);

    public String getLabel(Context context, Vehicle vehicle) {
        return context.getString(mLabel);
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

    public int getLabel() {
        return mLabel;
    }

    public Class<? extends ChartActivity> getChartClass() {
        return mChartClass;
    }

    @Override
    public String toString() {
        return mValue.getKey() + " - " + mValue.getValue();
    }
}
