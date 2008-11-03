package com.evancharlton.mileage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;

public class CSVExporter implements Runnable {
	private Handler m_handler;

	public CSVExporter(Handler handler) {
		m_handler = handler;
	}

	public void run() {
		HashMap<String, String> fillupsProjection = FillUpsProvider.getFillUpsProjection();

		Set<String> keySet = fillupsProjection.keySet();
		keySet.remove(FillUps._ID);
		String[] proj = keySet.toArray(new String[keySet.size()]);
		SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME, null, SQLiteDatabase.OPEN_READONLY);
		Cursor c = db.query(FillUpsProvider.FILLUPS_TABLE_NAME, proj, null, null, null, null, FillUps._ID + " ASC");

		StringBuilder sb = new StringBuilder();
		c.moveToFirst();
		columnsToCSV(keySet, sb);
		while (c.isAfterLast() == false) {
			dataToCSV(keySet, sb, c);
			c.moveToNext();
		}

		db.close();

		// write to a file
		try {
			File output = new File("/sdcard/mileage.csv");
			FileWriter out = new FileWriter(output);

			out.write(sb.toString());
			out.flush();
			out.close();
		} catch (final IOException e) {
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
	}

	private void columnsToCSV(Set<String> columns, StringBuilder sb) {
		for (String key : columns) {
			sb.append("\"").append(key).append("\",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("\n");
	}

	private void dataToCSV(Set<String> columns, StringBuilder sb, Cursor c) {
		int i = 1;
		for (String key : columns) {
			String val = c.getString(c.getColumnIndex(key));
			if (val == null) {
				val = "";
			}
			val = val.replaceAll("\"", "'");
			sb.append("\"").append(val).append("\"");
			if (i != c.getColumnCount()) {
				sb.append(",");
			}
			i++;
		}
		sb.append("\n");
	}
}
