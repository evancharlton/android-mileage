package com.evancharlton.mileage.tasks;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;

import com.evancharlton.mileage.ImportActivity;
import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.exceptions.InvalidFieldException;
import com.evancharlton.mileage.io.CsvImportActivity;
import com.evancharlton.mileage.provider.Settings;

public class CsvImportTask extends AttachableAsyncTask<CsvImportActivity, Bundle, Integer, Integer> {
	private static final String TAG = "CsvImportTask";

	@Override
	protected Integer doInBackground(Bundle... params) {
		Bundle args = params[0];
		String base = args.getString(ImportActivity.FILENAME);
		String filename = Settings.EXTERNAL_DIR + base;
		CSVReader csvReader = null;
		int i = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			csvReader = new CSVReader(reader);

			// skip the row of headers
			csvReader.readNext();
			String[] data;
			while ((data = csvReader.readNext()) != null) {
				try {
					ContentValues values = new ContentValues();
					// have to enumerate; can't iterate over the list because of
					// the
					// special cases (vehicle, dates, etc)
					setDouble(values, Fillup.TOTAL_COST, args, data);
					setDouble(values, Fillup.UNIT_PRICE, args, data);
					setDouble(values, Fillup.VOLUME, args, data);
					setDouble(values, Fillup.ODOMETER, args, data);
					setDouble(values, Fillup.ECONOMY, args, data);
					setDouble(values, Fillup.LATITUDE, args, data);
					setDouble(values, Fillup.LONGITUDE, args, data);
					setBoolean(values, Fillup.PARTIAL, args, data);
					setBoolean(values, Fillup.RESTART, args, data);

					int vehicleIndex = args.getInt(Fillup.VEHICLE_ID);
					String vehicle = data[vehicleIndex];
					long vehicleId = args.getLong("vehicle_" + vehicle);
					values.put(Fillup.VEHICLE_ID, vehicleId);

					int dateIndex = args.getInt(Fillup.DATE);
					String date = data[dateIndex];
					long timestamp = Date.parse(date);
					values.put(Fillup.DATE, timestamp);

					Fillup f = new Fillup(values);
					f.save(getParent());
					publishProgress(++i);
				} catch (InvalidFieldException e) {
					publishProgress(++i, e.getErrorMessage());
				}
			}
		} catch (IOException e) {
		} finally {
			try {
				if (csvReader != null) {
					csvReader.close();
				}
			} catch (IOException e2) {
			}
		}

		return i;
	}

	@Override
	protected void onProgressUpdate(Integer... updates) {
		if (updates.length == 2) {
			getParent().error(updates[1]);
		}

		getParent().update(updates[0]);
	}

	@Override
	protected void onPostExecute(Integer result) {
		getParent().completed(result);
	}

	private String getData(Bundle args, String column, String[] data) {
		return data[args.getInt(column)];
	}

	private void setLong(ContentValues values, String column, Bundle args, String[] data) throws InvalidFieldException {
		try {
			String value = getData(args, column, data);
			Log.d(TAG, "Parsing '" + value + "' for " + column);
			long parsed = Long.parseLong(value);
			values.put(column, parsed);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	private void setDouble(ContentValues values, String column, Bundle args, String[] data) throws InvalidFieldException {
		try {
			String value = getData(args, column, data);
			Log.d(TAG, "Parsing '" + value + "' for " + column);
			double parsed = Double.parseDouble(value);
			values.put(column, parsed);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	private void setBoolean(ContentValues values, String column, Bundle args, String[] data) throws InvalidFieldException {
		boolean parsed = Boolean.parseBoolean(getData(args, column, data));
		values.put(column, parsed);
	}
}
