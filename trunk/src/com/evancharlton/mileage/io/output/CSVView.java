package com.evancharlton.mileage.io.output;

import java.io.FileWriter;
import java.io.IOException;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.widget.TextView;
import au.com.bytecode.opencsv.CSVWriter;

import com.evancharlton.mileage.FillUpsProvider;
import com.evancharlton.mileage.Mileage;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.calculators.CalculationEngine;
import com.evancharlton.mileage.models.FillUp;

public class CSVView extends ExportView {

	@Override
	protected String getTag() {
		return "CSVExport";
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, "csv");
		m_title = (TextView) findViewById(R.id.title);
		m_title.setText(R.string.csv_file);

		super.m_exporter = new Runnable() {
			public void run() {
				try {
					CSVWriter csv = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory() + "/" + getFilename()));

					// write out the column headers
					String[] columns = FillUp.getCSVColumns();
					csv.writeNext(columns);

					// load all the fill-ups
					SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME, null, SQLiteDatabase.OPEN_READONLY);
					Cursor c = db.query(FillUpsProvider.FILLUPS_TABLE_NAME, FillUp.getProjection(), null, null, null, null, FillUp.DEFAULT_SORT_ORDER);
					c.moveToFirst();
					while (!c.isAfterLast()) {
						FillUp f = new FillUp((CalculationEngine) null, c);

						// write each fill-up
						csv.writeNext(f.toCSV(columns));

						c.moveToNext();
					}
					c.close();
					db.close();
					csv.close();
					m_handler.post(new Runnable() {
						public void run() {
							Bundle data = new Bundle();
							data.putString(MESSAGE, getString(R.string.export_finished_msg) + "\n" + getFilename());
							data.putString(TITLE, getString(R.string.success));
							data.putBoolean(SUCCESS, true);

							Message msg = new Message();
							msg.setData(data);
							m_handler.handleMessage(msg);
						}
					});
				} catch (final IOException e) {
					e.printStackTrace();
					m_handler.post(new Runnable() {
						public void run() {
							Bundle data = new Bundle();
							data.putString(MESSAGE, e.getMessage());
							data.putString(TITLE, getString(R.string.error));
							data.putBoolean(SUCCESS, false);

							Message msg = new Message();
							msg.setData(data);
							m_handler.handleMessage(msg);
						}
					});
					return;
				}
			}
		};
	}
}
