package com.evancharlton.mileage.tasks;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;

import com.evancharlton.mileage.io.CsvImportActivity;
import com.evancharlton.mileage.provider.Settings;

public class CsvColumnReaderTask extends AsyncTask<String, Void, String[]> {
	private CsvImportActivity mActivity;

	public void attach(CsvImportActivity activity) {
		mActivity = activity;
	}

	@Override
	protected String[] doInBackground(String... inputFiles) {
		final String inputFile = inputFiles[0];
		final String absoluteInputFile = Settings.EXTERNAL_DIR + inputFile;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(absoluteInputFile));
			CSVReader csvReader = new CSVReader(reader);
			// Note: it's assumed that the first row is the column names.
			String[] columnNames = csvReader.readNext();
			csvReader.close();
			return columnNames;
		} catch (IOException e) {
			Log.e("ColumnReaderTask", "Could not get columns!", e);
		}
		return null;
	}

	@Override
	protected void onPostExecute(String[] columnNames) {
		mActivity.setColumns(columnNames);
	}
}
