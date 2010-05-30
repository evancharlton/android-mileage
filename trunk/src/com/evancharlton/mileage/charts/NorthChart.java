package com.evancharlton.mileage.charts;

import android.database.Cursor;

import com.artfulbits.aiCharts.Base.ChartPoint;
import com.artfulbits.aiCharts.Base.ChartPointCollection;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

public class NorthChart extends LatitudeChart {
	@Override
	protected String getAxisTitle() {
		return getString(R.string.stat_north);
	}

	@Override
	protected void processCursor(LineChartGenerator generator, ChartPointCollection points, Cursor cursor, Vehicle vehicle) {
		int num = 0;
		double maximum_north = -10000;
		while (cursor.isAfterLast() == false) {
			if (generator.isCancelled()) {
				break;
			}
			double north = cursor.getDouble(0);
			if (north > maximum_north) {
				maximum_north = north;
			}
			points.add(new ChartPoint(num, maximum_north));
			generator.update(num++);
			cursor.moveToNext();
		}
	}
}
