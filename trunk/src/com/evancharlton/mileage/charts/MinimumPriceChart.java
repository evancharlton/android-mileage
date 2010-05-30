package com.evancharlton.mileage.charts;

import android.database.Cursor;

import com.artfulbits.aiCharts.Base.ChartPoint;
import com.artfulbits.aiCharts.Base.ChartPointCollection;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

public class MinimumPriceChart extends PriceChart {
	@Override
	protected String getAxisTitle() {
		return getString(R.string.stat_min_price);
	}

	@Override
	protected void processCursor(LineChartGenerator generator, ChartPointCollection points, Cursor cursor, Vehicle vehicle) {
		int num = 0;
		double minimum_price = 10000;
		while (cursor.isAfterLast() == false) {
			if (generator.isCancelled()) {
				break;
			}
			double price = cursor.getDouble(0);
			if (price < minimum_price) {
				minimum_price = price;
			}
			points.add(new ChartPoint(num, minimum_price));
			generator.update(num++);
			cursor.moveToNext();
		}
	}
}
