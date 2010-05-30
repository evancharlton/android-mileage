package com.evancharlton.mileage.charts;

import android.database.Cursor;

import com.artfulbits.aiCharts.Base.ChartPoint;
import com.artfulbits.aiCharts.Base.ChartPointCollection;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

public class AverageDistanceChart extends DistanceChart {

	@Override
	protected String getAxisTitle() {
		return getString(R.string.stat_avg_distance);
	}

	@Override
	protected void processCursor(LineChartGenerator generator, ChartPointCollection points, Cursor cursor, Vehicle vehicle) {
		int num = 0;
		double last_odometer = 0;;
		while (cursor.isAfterLast() == false) {
			if (generator.isCancelled()) {
				break;
			}
			double odometer = cursor.getDouble(0);
			if (num > 0) {
				points.add(new ChartPoint(num, odometer - last_odometer));
			}
			last_odometer = odometer;
			generator.update(num++);
			cursor.moveToNext();
		}
	}
}
