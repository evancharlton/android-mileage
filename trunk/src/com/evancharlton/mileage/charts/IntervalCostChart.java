package com.evancharlton.mileage.charts;

import android.database.Cursor;

import com.artfulbits.aiCharts.Base.ChartPoint;
import com.artfulbits.aiCharts.Base.ChartPointCollection;
import com.evancharlton.mileage.dao.Vehicle;

public abstract class IntervalCostChart extends CostChart {
	protected abstract long getInterval();

	@Override
	protected final void processCursor(LineChartGenerator generator, ChartPointCollection points, Cursor cursor, Vehicle vehicle) {
		int num = 0;
		final long limit = System.currentTimeMillis() - getInterval();
		double totalCost = 0D;
		while (cursor.isAfterLast() == false) {
			if (generator.isCancelled()) {
				break;
			}
			if (cursor.getLong(1) > limit) {
				totalCost += cursor.getDouble(0);
				points.add(new ChartPoint(num, totalCost));
			}
			generator.update(num++);
			cursor.moveToNext();
		}
	}
}
