
package com.evancharlton.mileage.charts;

import com.evancharlton.mileage.dao.Fillup;

public abstract class FuelEconomyChart extends LineChart {
    @Override
    protected final ChartGenerator createChartGenerator() {
        return new LineChartGenerator(this, getVehicle(), new String[] {
                Fillup.DATE,
                Fillup.ECONOMY
        });
    }
}
