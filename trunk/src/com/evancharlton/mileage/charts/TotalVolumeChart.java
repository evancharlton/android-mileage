package com.evancharlton.mileage.charts;

import android.database.Cursor;

import com.artfulbits.aiCharts.Base.ChartPoint;
import com.artfulbits.aiCharts.Base.ChartPointCollection;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

public class TotalVolumeChart extends VolumeChart {
	@Override
	protected String getAxisTitle() {
		return getString(R.string.stat_total_fuel);
	}

	@Override
	protected void processCursor(LineChartGenerator generator, ChartPointCollection points, Cursor cursor, Vehicle vehicle) {
		int num = 0;
		double total_volume = 0;
		while (cursor.isAfterLast() == false) {
			if (generator.isCancelled()) {
				break;
			}
			total_volume += cursor.getDouble(0);
			points.add(new ChartPoint(num, total_volume));
			generator.update(num++);
			cursor.moveToNext();
		}
	}
}
