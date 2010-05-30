package com.evancharlton.mileage.charts;

import com.evancharlton.mileage.charts.LineChart;
import com.evancharlton.mileage.dao.Fillup;

public abstract class LatitudeChart extends LineChart {
	@Override
	protected ChartGenerator createChartGenerator() {
		return new LineChartGenerator(this, getVehicle(), new String[] {
			Fillup.LATITUDE
		});
	}
}
