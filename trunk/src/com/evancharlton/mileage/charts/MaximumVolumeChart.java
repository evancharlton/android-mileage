
package com.evancharlton.mileage.charts;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

import android.database.Cursor;

public class MaximumVolumeChart extends VolumeChart {
    @Override
    protected String getAxisTitle() {
        return getString(R.string.stat_max_fuel);
    }

    @Override
    protected void processCursor(LineChartGenerator generator, Cursor cursor, Vehicle vehicle) {
        int num = 0;
        double maximum_volume = -10000;
        while (cursor.isAfterLast() == false) {
            if (generator.isCancelled()) {
                break;
            }
            double volume = cursor.getDouble(1);
            if (volume > maximum_volume) {
                maximum_volume = volume;
            }
            addPoint(cursor.getLong(0), maximum_volume);
            generator.update(num++);
            cursor.moveToNext();
        }
    }
}
