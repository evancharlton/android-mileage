
package com.evancharlton.mileage.charts;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

import android.database.Cursor;

public class TotalVolumeChart extends VolumeChart {
    @Override
    protected String getAxisTitle() {
        return getString(R.string.stat_total_fuel);
    }

    @Override
    protected void processCursor(LineChartGenerator generator, Cursor cursor, Vehicle vehicle) {
        int num = 0;
        double total_volume = 0;
        while (cursor.isAfterLast() == false) {
            if (generator.isCancelled()) {
                break;
            }
            total_volume += cursor.getDouble(1);
            addPoint(cursor.getLong(0), total_volume);
            generator.update(num++);
            cursor.moveToNext();
        }
    }
}
