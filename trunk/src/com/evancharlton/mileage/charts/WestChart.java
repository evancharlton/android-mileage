
package com.evancharlton.mileage.charts;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

import android.database.Cursor;

public class WestChart extends LongitudeChart {
    @Override
    protected String getAxisTitle() {
        return getString(R.string.stat_west);
    }

    @Override
    protected void processCursor(LineChartGenerator generator, Cursor cursor, Vehicle vehicle) {
        int num = 0;
        double maximum_west = 10000;
        while (cursor.isAfterLast() == false) {
            if (generator.isCancelled()) {
                break;
            }
            double west = cursor.getDouble(1);
            if (west != 0) {
                if (west < maximum_west) {
                    maximum_west = west;
                }
                addPoint(cursor.getLong(0), maximum_west);
            }
            generator.update(num++);
            cursor.moveToNext();
        }
    }
}
