package com.evancharlton.mileage.charts;

import android.database.Cursor;

import com.artfulbits.aiCharts.Base.ChartPoint;
import com.artfulbits.aiCharts.Base.ChartPointCollection;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

public class MaximumPriceChart extends PriceChart {
	@Override
	protected String getAxisTitle() {
		return getString(R.string.stat_max_price);
	}

	@Override
	protected void processCursor(LineChartGenerator generator, ChartPointCollection points, Cursor cursor, Vehicle vehicle) {
		int num = 0;
		double maximum_price = -10000;
		while (cursor.isAfterLast() == false) {
			if (generator.isCancelled()) {
				break;
			}
			double price = cursor.getDouble(0);
			if (price > maximum_price) {
				maximum_price = price;
			}
			points.add(new ChartPoint(num, maximum_price));
			generator.update(num++);
			cursor.moveToNext();
		}
	}
}
