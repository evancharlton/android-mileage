
package com.evancharlton.mileage.charts;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.math.Calculator;

import android.database.Cursor;

public class WorstFuelEconomyChart extends FuelEconomyChart {
    @Override
    protected void processCursor(LineChartGenerator generator, Cursor cursor, Vehicle vehicle) {
        int num = 0;
        double worst_fuel_economy = 100000;
        while (cursor.isAfterLast() == false) {
            if (generator.isCancelled()) {
                break;
            }
            if (num > 0) {
                double economy = cursor.getDouble(1);
                if (Calculator.isBetterEconomy(vehicle, economy, worst_fuel_economy) == false) {
                    worst_fuel_economy = economy;
                }
                addPoint(cursor.getLong(0), worst_fuel_economy);
            }
            generator.update(num++);
            cursor.moveToNext();
        }
    }

    @Override
    protected String getAxisTitle() {
        return getString(R.string.stat_min_economy);
    }
}
