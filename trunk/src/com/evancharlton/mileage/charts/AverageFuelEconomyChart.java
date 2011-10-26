
package com.evancharlton.mileage.charts;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

import android.database.Cursor;

public class AverageFuelEconomyChart extends FuelEconomyChart {
    @Override
    protected String getAxisTitle() {
        return getString(R.string.stat_avg_economy);
    }

    @Override
    protected void processCursor(LineChartGenerator generator, Cursor cursor, Vehicle vehicle) {
        int num = 0;
        while (cursor.moveToNext()) {
            if (generator.isCancelled()) {
                break;
            }
            if (num > 0) {
                double economy = cursor.getDouble(1);
                if (economy > 0) {
                    addPoint(cursor.getLong(0), economy);
                }
            }
            generator.update(num++);
        }
    }
}
