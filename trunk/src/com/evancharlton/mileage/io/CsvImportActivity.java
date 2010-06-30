package com.evancharlton.mileage.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import au.com.bytecode.opencsv.CSVReader;

import com.evancharlton.mileage.ImportActivity;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.provider.Settings;
import com.evancharlton.mileage.provider.tables.FillupsTable;

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
		final int length = columnNames.length;
		for (int i = 0; i < length; i++) {
			String columnName = columnNames[i];
			Log.d("CsvImportActivity", "Adding a UI mapping for " + columnName);
			View v = inflater.inflate(R.layout.import_csv_mapping, mMappingContainer);
			TextView title = (TextView) v.findViewById(R.id.title);
			title.setText(columnName);
			title.setId(columnName.hashCode());
			Spinner spinner = (Spinner) v.findViewById(R.id.mappings);
			spinner.setAdapter(new FieldAdapter(this));
			spinner.setId(columnName.hashCode());
			spinner.setSelection(i);
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

	private static class FieldAdapter implements SpinnerAdapter {
		private final LayoutInflater mInflater;

		public FieldAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
			mContext = context;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
			}

			Holder holder = (Holder) convertView.getTag();
			if (holder == null) {
				holder = new Holder((TextView) convertView.findViewById(android.R.id.text1));
				convertView.setTag(holder);
			}
			holder.text.setText(getText(position));

			return convertView;
		}

		@Override
		public int getCount() {
			return FillupsTable.PROJECTION.length;
		}

		@Override
		public Object getItem(int position) {
			return FillupsTable.PROJECTION[position];
		}

		@Override
		public long getItemId(int position) {
			return FillupsTable.PROJECTION[position].hashCode();
		}

		private int getText(int position) {
			return FillupsTable.PLAINTEXT[position];
		}

		@Override
		public int getItemViewType(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(android.R.layout.simple_spinner_item, parent, false);
			}

			TextView tv = (TextView) convertView.getTag();
			if (tv == null) {
				tv = (TextView) convertView.findViewById(android.R.id.text1);
				convertView.setTag(tv);
			}
			tv.setText(getText(position));

			return convertView;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public void registerDataSetObserver(DataSetObserver observer) {
			// TODO Auto-generated method stub

		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver observer) {
			// TODO Auto-generated method stub

		}

		private static class Holder {
			public final TextView text;

			public Holder(TextView text) {
				this.text = text;
			}
		}
	}
}
