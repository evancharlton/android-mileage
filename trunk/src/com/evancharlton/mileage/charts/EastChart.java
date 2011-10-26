
package com.evancharlton.mileage.charts;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

import android.database.Cursor;

public class EastChart extends LongitudeChart {
    @Override
    protected String getAxisTitle() {
        return getString(R.string.stat_east);
    }

    @Override
    protected void processCursor(LineChartGenerator generator, Cursor cursor, Vehicle vehicle) {
        int num = 0;
        double maximum_east = -10000;
        while (cursor.isAfterLast() == false) {
            if (generator.isCancelled()) {
                break;
            }
            double east = cursor.getDouble(1);
            if (east != 0) {
                if (east > maximum_east) {
                    maximum_east = east;
                }
                addPoint(cursor.getLong(0), maximum_east);
            }
            generator.update(num++);
            cursor.moveToNext();
        }
    }
}
