package com.evancharlton.mileage.charts;

import android.database.Cursor;

import com.artfulbits.aiCharts.Base.ChartPoint;
import com.artfulbits.aiCharts.Base.ChartPointCollection;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

public class EastChart extends LongitudeChart {
	@Override
	protected String getAxisTitle() {
		return getString(R.string.stat_east);
	}

	@Override
	protected void processCursor(LineChartGenerator generator, ChartPointCollection points, Cursor cursor, Vehicle vehicle) {
		int num = 0;
		double maximum_east = -10000;
		while (cursor.isAfterLast() == false) {
			if (generator.isCancelled()) {
				break;
			}
			double east = cursor.getDouble(0);
			if (east > maximum_east) {
				maximum_east = east;
			}
			points.add(new ChartPoint(num, maximum_east));
			generator.update(num++);
			cursor.moveToNext();
		}
	}
}
