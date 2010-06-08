package com.evancharlton.mileage.charts;

import android.database.Cursor;

import com.artfulbits.aiCharts.Base.ChartPoint;
import com.artfulbits.aiCharts.Base.ChartPointCollection;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

public class AverageFuelEconomyChart extends FuelEconomyChart {
	@Override
	protected String getAxisTitle() {
		return getString(R.string.stat_avg_economy);
	}

	@Override
	protected void processCursor(LineChartGenerator generator, ChartPointCollection points, Cursor cursor, Vehicle vehicle) {
		int num = 0;
		while (cursor.isAfterLast() == false) {
			if (generator.isCancelled()) {
				break;
			}
			if (num > 0) {
				points.add(new ChartPoint(num, cursor.getDouble(1)));
			}
			generator.update(num++);
			cursor.moveToNext();
		}
	}
}
