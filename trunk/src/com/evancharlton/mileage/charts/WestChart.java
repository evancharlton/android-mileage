package com.evancharlton.mileage.charts;

import android.database.Cursor;

import com.artfulbits.aiCharts.Base.ChartPoint;
import com.artfulbits.aiCharts.Base.ChartPointCollection;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

public class WestChart extends LongitudeChart {
	@Override
	protected String getAxisTitle() {
		return getString(R.string.stat_west);
	}

	@Override
	protected void processCursor(LineChartGenerator generator, ChartPointCollection points, Cursor cursor, Vehicle vehicle) {
		int num = 0;
		double maximum_west = 10000;
		while (cursor.isAfterLast() == false) {
			if (generator.isCancelled()) {
				break;
			}
			double west = cursor.getDouble(0);
			if (west < maximum_west) {
				maximum_west = west;
			}
			points.add(new ChartPoint(num, maximum_west));
			generator.update(num++);
			cursor.moveToNext();
		}
	}
}
