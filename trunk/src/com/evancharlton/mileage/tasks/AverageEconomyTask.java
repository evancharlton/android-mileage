package com.evancharlton.mileage.tasks;

import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.adapters.FillupAdapter;
import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.dao.FillupSeries;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.exceptions.InvalidFieldException;
import com.evancharlton.mileage.math.Calculator;
import com.evancharlton.mileage.provider.tables.FillupsTable;

public class AverageEconomyTask extends AttachableAsyncTask<FillupAdapter, Vehicle, Integer, Double> {
	private static final String TAG = "AverageEconomyTask";
	private ContentResolver mContentResolver;
	private Vehicle mVehicle = null;

	@Override
	public void attach(FillupAdapter parent) {
		super.attach(parent);
		mContentResolver = parent.getContext().getContentResolver();
		if (mVehicle != null) {
			parent.setVehicle(mVehicle);
		}
	}

	@Override
	public void onProgressUpdate(Integer... update) {
		Toast.makeText(getParent().getContext(), getParent().getContext().getString(R.string.toast_calculating_avg_economy), Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public Double doInBackground(Vehicle... vehicles) {
		Vehicle vehicle = vehicles[0];
		mVehicle = vehicle;
		String[] args = new String[] {
			String.valueOf(vehicle.getId())
		};

		String selection = Fillup.VEHICLE_ID + " = ?";

		Cursor cursor = mContentResolver.query(FillupsTable.BASE_URI, FillupsTable.PROJECTION, selection, args, Fillup.ODOMETER + " asc");
		int mTotal = cursor.getCount();

		if (mTotal <= 1) {
			Log.d(TAG, "Not enough fillups to calculate statistics");
			return 0D;
		}

		Log.d(TAG, "Recalculating...");
		// recalculate a whole bunch of shit
		FillupSeries series = new FillupSeries();

		while (cursor.moveToNext()) {
			Fillup fillup = new Fillup(cursor);
			series.add(fillup);

			if (fillup.hasPrevious()) {
				double economy = Calculator.fillupEconomy(vehicle, series);
				if (economy != fillup.getEconomy()) {
					fillup.setEconomy(economy);
				}
			} else {
				fillup.setEconomy(0D);
			}

			try {
				fillup.saveIfChanged(getParent().getContext());
			} catch (InvalidFieldException e) {
				return 0D;
			}
		}

		double avgEconomy = Calculator.averageEconomy(vehicle, series);

		cursor.close();

		return avgEconomy;
	}

	@Override
	protected void onPostExecute(Double avgEconomy) {
		getParent().calculationFinished(avgEconomy.doubleValue());
	}
}
