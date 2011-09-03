
package com.evancharlton.mileage.dao;

import android.database.Cursor;

import java.util.ArrayList;

public class FillupSeries extends ArrayList<Fillup> {
    private static final long serialVersionUID = 5304523564485608182L;

    private double mTotalCost = 0D;
    private double mEconomyVolume = 0D;

    public FillupSeries(Fillup... fillups) {
        final int length = fillups.length;
        Fillup previous = null;
        Fillup current = null;
        for (int i = 0; i < length; i++) {
            current = fillups[i];
            if (previous != null && previous.hasNext() == false) {
                previous.setNext(current);
            }
            if (current.hasPrevious() == false) {
                current.setPrevious(previous);
            }
            super.add(current);
            previous = current;

            mTotalCost += current.getTotalCost();
        }
    }

    @Override
    public boolean add(Fillup fillup) {
        if (size() > 0) {
            last().setNext(fillup);
            fillup.setPrevious(last());
        }
        super.add(fillup);

        mTotalCost += fillup.getTotalCost();
        mEconomyVolume = 0D;
        return true;
    }

    public double getTotalDistance() {
        if (size() >= 2) {
            Fillup last = last();

            // find the newest non-partial fillup
            while (last.hasPrevious() && last.isPartial()) {
                last = last.getPrevious();
            }
            Fillup first = first();
            if (last == first) {
                return 0D;
            }
            return Math.abs(last.getOdometer() - first.getOdometer());
        }
        return 0D;
    }

    public double getTotalVolume() {
        return getEconomyVolume() + get(0).getVolume();
    }

    public long getTimeRange() {
        return Math.abs(last().getTimestamp() - first().getTimestamp());
    }

    /**
     * Gets the sum of all the volume values except for the first one, since
     * it's not used in the calculation of fuel economy.
     * 
     * @return
     */
    public double getEconomyVolume() {
        if (mEconomyVolume == 0) {
            double total = 0D;
            for (Fillup fillup : this) {
                if (fillup.getPrevious() == null) {
                    continue; // ignore the first fillup for economy calculation
                }
                if (fillup.validForEconomy()) {
                    total += fillup.getVolume();
                }
            }
            mEconomyVolume = total;
        }
        return mEconomyVolume;
    }

    public double getTotalCost() {
        return mTotalCost;
    }

    public Fillup first() {
        return get(0);
    }

    public Fillup last() {
        return get(size() - 1);
    }

    /**
     * In order for this to work, it's expected that the fillups are sorted in
     * ascending order by odometer.
     * 
     * @param cursor
     * @return
     */
    public static ArrayList<FillupSeries> load(Cursor cursor) {
        ArrayList<FillupSeries> output = new ArrayList<FillupSeries>();

        FillupSeries series = new FillupSeries();
        while (cursor.moveToNext()) {
            Fillup fillup = new Fillup(cursor);
            if (fillup.isRestart()) {
                output.add(series);
                series = new FillupSeries();
            }
            series.add(fillup);
        }
        output.add(series);

        return output;
    }
}
