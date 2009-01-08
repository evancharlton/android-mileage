package com.evancharlton.mileage.io.output;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.widget.TextView;
import au.com.bytecode.opencsv.CSVWriter;

import com.evancharlton.mileage.FillUps;
import com.evancharlton.mileage.FillUpsProvider;
import com.evancharlton.mileage.Mileage;
import com.evancharlton.mileage.R;

public class CSVView extends ExportView {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, "csv");
		m_title = (TextView) findViewById(R.id.title);
		m_title.setText(R.string.csv_file);

		super.m_exporter = new Runnable() {
			public void run() {
				try {
					CSVWriter csv = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory() + "/" + getFilename()));

					HashMap<String, String> fillupsProjection = FillUpsProvider.getFillUpsProjection();
					Set<String> tmp_keySet = fillupsProjection.keySet();
					ArrayList<String> keySet = new ArrayList<String>(tmp_keySet);
					keySet.remove(FillUps._ID);
					String[] proj = keySet.toArray(new String[keySet.size()]);
					SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME, null, SQLiteDatabase.OPEN_READONLY);
					Cursor c = db.query(FillUpsProvider.FILLUPS_TABLE_NAME, proj, null, null, null, null, FillUps._ID + " ASC");
					c.moveToFirst();

					// transform the column keySet into the plain English form
					int i = 0;
					for (String key : keySet) {
						String english = FillUps.PLAINTEXT.get(key);
						if (english != null) {
							keySet.set(i, english);
						}
						i++;
					}

					csv.writeNext(keySet.toArray(new String[keySet.size()]));
					while (c.isAfterLast() == false) {
						String[] data = new String[keySet.size()];
						for (i = 0; i < c.getColumnCount(); i++) {
							data[i] = c.getString(i);
						}
						csv.writeNext(data);
						c.moveToNext();
					}
					c.close();
					csv.close();
					m_handler.post(new Runnable() {
						public void run() {
							Bundle data = new Bundle();
							data.putString(MESSAGE, getString(R.string.export_finished_msg) + "\n" + getFilename());
							data.putString(TITLE, getString(R.string.success));

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

	@Override
	protected String getHelp() {
		return getString(R.string.help_export_csv);
	}

	@Override
	protected String getHelpTitle() {
		return getString(R.string.help_export_csv_title);
	}
}
