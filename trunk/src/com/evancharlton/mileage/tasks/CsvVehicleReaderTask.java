package com.evancharlton.mileage.tasks;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;

import com.evancharlton.mileage.io.CsvVehicleMappingActivity;
import com.evancharlton.mileage.provider.Settings;

public class CsvVehicleReaderTask extends AttachableAsyncTask<CsvVehicleMappingActivity, String, String, Void> {
	private static final String TAG = "CsvVehicleReaderTask";

	private final int INDEX;
	private static final ArrayList<String> mTitles = new ArrayList<String>();

	public CsvVehicleReaderTask(int index) {
		INDEX = index;
		Log.d(TAG, "Looking for unique entries in column #" + index);
	}

	@Override
	protected Void doInBackground(String... inputFiles) {
		final String inputFile = inputFiles[0];
		final String absoluteInputFile = Settings.EXTERNAL_DIR + inputFile;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(absoluteInputFile));
			CSVReader csvReader = new CSVReader(reader);
			// skip the first row of headers
			csvReader.readNext();

			String[] data;
			while ((data = csvReader.readNext()) != null) {
				String title = data[INDEX];
				if (!mTitles.contains(title)) {
					Log.d(TAG, "Found a unique vehicle: " + title);
					mTitles.add(title);
					publishProgress(title);
				}
			}
			csvReader.close();
		} catch (IOException e) {
			Log.e(TAG, "Could not get columns!", e);
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(String... titles) {
		getParent().dataRead(titles[0]);
	}
}
