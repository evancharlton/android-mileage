package com.evancharlton.mileage.dao;

import java.util.ArrayList;

import android.database.Cursor;

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
			if (previous != null) {
				previous.setNext(current);
			}
			current.setPrevious(previous);
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
		final int size = size();
		if (size >= 2) {
			// TODO: will this work for partials? check the edge case here
			return Math.abs(last().getOdometer() - first().getOdometer());
		}
		return 0D;
	}

	public double getTotalVolume() {
		return getEconomyVolume() + get(0).getVolume();
	}

	public long getTimeRange() {
		return last().getTimestamp() - first().getTimestamp();
	}

	/**
	 * Gets the sum of all the volume values except for the first one, since
	 * it's not used in the calculation of fuel economy.
	 * 
	 * @return
	 */
	public double getEconomyVolume() {
		if (mEconomyVolume == 0) {
			final int size = size();
			double total = 0D;
			for (int i = 1; i < size; i++) {
				total += get(i).getVolume();
			}
			mEconomyVolume = total;
		}
		return mEconomyVolume;
	}

	public double getTotalCost() {
		return mTotalCost;
	}

	private Fillup first() {
		return get(0);
	}

	private Fillup last() {
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
