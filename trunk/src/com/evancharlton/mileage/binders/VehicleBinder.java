package com.evancharlton.mileage.binders;

import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.evancharlton.mileage.models.Vehicle;

public class VehicleBinder implements SimpleCursorAdapter.ViewBinder {
	private final int COL_TITLE = Vehicle.PROJECTION.indexOf(Vehicle.TITLE);

	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		if (columnIndex == COL_TITLE) {
			String title = cursor.getString(COL_TITLE);
			if (title.trim().length() == 0) {
				Map<String, String> data = new HashMap<String, String>();
				data.put(Vehicle.MAKE, cursor.getString(Vehicle.PROJECTION.indexOf(Vehicle.MAKE)));
				data.put(Vehicle.YEAR, cursor.getString(Vehicle.PROJECTION.indexOf(Vehicle.YEAR)));
				data.put(Vehicle.MODEL, cursor.getString(Vehicle.PROJECTION.indexOf(Vehicle.MODEL)));
				Vehicle vehicle = new Vehicle(data);
				title = vehicle.getTitle();
			}
			((TextView) view).setText(title);
			return true;
		}
		return false;
	}
}
