package com.evancharlton.mileage.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import au.com.bytecode.opencsv.CSVReader;

import com.evancharlton.mileage.ImportActivity;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.provider.Settings;

public class CsvImportActivity extends Activity {
	private ColumnReaderTask mColumnReaderTask;
	private LinearLayout mMappingContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.import_progress_csv);

		mMappingContainer = (LinearLayout) findViewById(R.id.mapping_container);

		mColumnReaderTask = (ColumnReaderTask) getLastNonConfigurationInstance();
		if (mColumnReaderTask == null) {
			mColumnReaderTask = new ColumnReaderTask();
		}
		mColumnReaderTask.attach(this);
		if (mColumnReaderTask.getStatus() == AsyncTask.Status.PENDING) {
			mColumnReaderTask.execute(getIntent().getStringExtra(ImportActivity.FILENAME));
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return mColumnReaderTask;
	}

	public void setColumns(String[] columnNames) {
		LayoutInflater inflater = LayoutInflater.from(this);
		for (String columnName : columnNames) {
			Log.d("CsvImportActivity", "Adding a UI mapping for " + columnName);
			View v = inflater.inflate(R.layout.import_csv_mapping, mMappingContainer);
			TextView title = (TextView) v.findViewById(R.id.title);
			title.setText(columnName);
		}
	}

	private static class ColumnReaderTask extends AsyncTask<String, Void, String[]> {
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
}
