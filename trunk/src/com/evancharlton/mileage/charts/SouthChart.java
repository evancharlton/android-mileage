package com.evancharlton.mileage.charts;

import android.database.Cursor;

import com.artfulbits.aiCharts.Base.ChartPoint;
import com.artfulbits.aiCharts.Base.ChartPointCollection;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

public class SouthChart extends LatitudeChart {
	@Override
	protected String getAxisTitle() {
		return getString(R.string.stat_south);
	}

	@Override
	protected void processCursor(LineChartGenerator generator, ChartPointCollection points, Cursor cursor, Vehicle vehicle) {
		int num = 0;
		double maximum_south = 10000;
		while (cursor.isAfterLast() == false) {
			if (generator.isCancelled()) {
				break;
			}
			double south = cursor.getDouble(0);
			if (south < maximum_south) {
				maximum_south = south;
			}
			points.add(new ChartPoint(num, maximum_south));
			generator.update(num++);
			cursor.moveToNext();
		}
	}
}
