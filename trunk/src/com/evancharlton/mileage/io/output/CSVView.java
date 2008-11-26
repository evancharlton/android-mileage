package com.evancharlton.mileage.io.output;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import au.com.bytecode.opencsv.CSVWriter;

import com.evancharlton.mileage.FillUps;
import com.evancharlton.mileage.FillUpsProvider;
import com.evancharlton.mileage.Mileage;
import com.evancharlton.mileage.R;

public class CSVView implements Runnable {
	private Handler m_handler;

	public CSVView(Handler handler) {
		m_handler = handler;
	}

	public void run() {
		try {
			CSVWriter csv = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory() + "/mileage.csv"));

			HashMap<String, String> fillupsProjection = FillUpsProvider.getFillUpsProjection();
			Set<String> keySet = fillupsProjection.keySet();
			keySet.remove(FillUps._ID);
			String[] proj = keySet.toArray(new String[keySet.size()]);
			SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME, null, SQLiteDatabase.OPEN_READONLY);
			Cursor c = db.query(FillUpsProvider.FILLUPS_TABLE_NAME, proj, null, null, null, null, FillUps._ID + " ASC");
			c.moveToFirst();
			csv.writeNext(keySet.toArray(new String[keySet.size()]));
			while (c.isAfterLast() == false) {
				String[] data = new String[keySet.size()];
				for (int i = 0; i < c.getColumnCount(); i++) {
					data[i] = c.getString(i);
				}
				csv.writeNext(data);
				c.moveToNext();
			}
			c.close();
			csv.close();
			m_handler.post(new Runnable() {
				public void run() {
					Message msg = new Message();
					msg.what = 1;
					msg.arg1 = R.string.export_finished_msg;
					msg.obj = "mileage.csv";
					msg.arg2 = R.string.export_finished;
					m_handler.handleMessage(msg);
				}
			});
		} catch (final IOException e) {
			e.printStackTrace();
			m_handler.post(new Runnable() {
				public void run() {
					Message msg = new Message();
					msg.what = 0;
					msg.obj = e.getMessage();
					msg.arg2 = R.string.error_exporting_data;
					m_handler.handleMessage(msg);
				}
			});
			return;
		}
	}
}
