package com.evancharlton.mileage.tasks;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.CachedValue;
import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.provider.Statistics;
import com.evancharlton.mileage.provider.tables.CacheTable;
import com.evancharlton.mileage.provider.tables.FillupsTable;

public class AverageEconomyTask extends AttachableAsyncTask<Activity, Long, Integer, Double> {
	private static final String TAG = "AverageEconomyTask";

	@Override
	public void attach(Activity parent) {
		if (parent instanceof AsyncCallback) {
			super.attach(parent);
		} else {
			throw new IllegalArgumentException("parent must implement AsyncCallback");
		}
	}

	@Override
	public void onProgressUpdate(Integer... update) {
		Toast.makeText(getParent(), getParent().getString(R.string.toast_calculating_avg_economy), Toast.LENGTH_SHORT).show();
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

		Cursor cacheCursor = getParent().getContentResolver().query(CacheTable.BASE_URI, new String[] {
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
			Cursor fillupsCursor = getParent().getContentResolver().query(FillupsTable.BASE_URI, projection, Fillup.VEHICLE_ID + " = ?",
					new String[] {
						String.valueOf(vehicleId)
					}, null);
			if (fillupsCursor.getCount() > 1) {
				publishProgress();
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

			// cache the new value
			ContentValues values = new ContentValues();
			values.put(CachedValue.ITEM, vehicleId);
			values.put(CachedValue.VALID, true);
			values.put(CachedValue.KEY, Statistics.AVG_ECONOMY.getKey());
			values.put(CachedValue.VALUE, avgEconomy);
			values.put(CachedValue.GROUP, Statistics.AVG_ECONOMY.getGroup());
			values.put(CachedValue.ORDER, Statistics.AVG_ECONOMY.getOrder());
			getParent().getContentResolver().insert(CacheTable.BASE_URI, values);
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
		((AsyncCallback) getParent()).calculationFinished(avgEconomy.doubleValue());
	}

	public interface AsyncCallback {
		public void calculationFinished(double avgEconomy);
	}
}
