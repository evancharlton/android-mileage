package com.evancharlton.mileage.tasks;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.evancharlton.mileage.dao.CachedValue;
import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.provider.Statistics;
import com.evancharlton.mileage.provider.tables.CacheTable;
import com.evancharlton.mileage.provider.tables.FillupsTable;

public class AverageEconomyTask extends AsyncTask<Long, Integer, Double> {
	private static final String TAG = "AverageEconomyTask";
	private Activity mActivity = null;

	public void setActivity(Activity activity) {
		if (activity instanceof AsyncCallback) {
			mActivity = activity;
		} else {
			throw new IllegalArgumentException("Activity must implement AsyncCallback");
		}
	}

	@Override
	public Double doInBackground(Long... vehicleIds) {
		Long vehicleId = vehicleIds[0];
		Log.d(TAG, "Getting average fuel economy for vehicle #" + vehicleId);
		String selection = CachedValue.KEY + " = ? AND " + CachedValue.ITEM + " = ? AND " + CachedValue.VALID + " = 1";
		String[] selectionArgs = new String[] {
				Statistics.AVG_ECONOMY.getKey(),
				String.valueOf(vehicleId)
		};

		Cursor cacheCursor = mActivity.getContentResolver().query(CacheTable.BASE_URI, new String[] {
			CachedValue.VALUE
		}, selection, selectionArgs, null);

		double avgEconomy = 0D;
		if (cacheCursor.getCount() > 0) {
			cacheCursor.moveToFirst();
			avgEconomy = cacheCursor.getDouble(0);
			Log.d(TAG, "Returning cached value of " + avgEconomy);
		} else {
			Log.d(TAG, "Calculating average economy...");
			String[] projection = FillupsTable.PROJECTION;
			Cursor fillupsCursor = mActivity.getContentResolver().query(FillupsTable.BASE_URI, projection, Fillup.VEHICLE_ID + " = ?", new String[] {
				String.valueOf(vehicleId)
			}, null);
			if (fillupsCursor.getCount() > 1) {
				double totalEconomy = 0D;
				while (fillupsCursor.isLast() == false) {
					fillupsCursor.moveToNext();
					Fillup f = new Fillup(fillupsCursor);
					totalEconomy += f.getEconomy();
				}
				// subtract 1 to account for the invalid first fillup.
				// TODO(3.1) -- add support for restarting calculations
				avgEconomy = totalEconomy / (fillupsCursor.getCount() - 1);
				Log.d(TAG, "Done! Result is " + avgEconomy);
			}
			fillupsCursor.close();
		}
		cacheCursor.close();
		// have to round the average economy
		avgEconomy *= 100;
		avgEconomy = Math.floor(avgEconomy);
		avgEconomy /= 100;
		return avgEconomy;
	}

	@Override
	protected void onPostExecute(Double avgEconomy) {
		((AsyncCallback) mActivity).calculationFinished(avgEconomy.doubleValue());
	}

	public interface AsyncCallback {
		public void calculationFinished(double avgEconomy);
	}
}
