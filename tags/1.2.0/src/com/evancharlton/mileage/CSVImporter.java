package com.evancharlton.mileage;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import au.com.bytecode.opencsv.CSVReader;

public class CSVImporter implements Runnable {
	private Handler m_handler;

	public CSVImporter(Handler handler) {
		m_handler = handler;
	}

	@SuppressWarnings("unchecked")
	public void run() {
		try {
			CSVReader csv = new CSVReader(new FileReader("/sdcard/mileage.csv"));
			List<String[]> data = csv.readAll();

			// columns will be the first line
			String[] columns = data.get(0);
			data.remove(0);
			StringBuilder colStrBuilder = new StringBuilder();
			colStrBuilder.append(" (");
			for (String col : columns) {
				colStrBuilder.append(col).append(", ");
			}
			colStrBuilder.deleteCharAt(colStrBuilder.length() - 1);
			colStrBuilder.deleteCharAt(colStrBuilder.length() - 1);
			colStrBuilder.append(") ");
			final String COLUMNS = colStrBuilder.toString();

			// loop over the rest of the data
			SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE);
			for (String[] row : data) {
				StringBuilder sql = new StringBuilder();
				sql.append("INSERT INTO ").append(FillUpsProvider.FILLUPS_TABLE_NAME).append(COLUMNS).append("VALUES (");
				for (int i = 0; i < row.length; i++) {
					sql.append("?");
					if (i != row.length - 1) {
						sql.append(", ");
					}
				}
				sql.append(");");
				db.execSQL(sql.toString(), row);
			}

			db.close();
			m_handler.post(new Runnable() {
				public void run() {
					Message msg = new Message();
					msg.what = 0;
					msg.arg1 = R.string.import_done_msg;
					msg.arg2 = R.string.import_done;
					msg.obj = "mileage.csv";
					m_handler.handleMessage(msg);
				}
			});

		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			m_handler.post(new Runnable() {
				public void run() {
					Message msg = new Message();
					msg.what = 0;
					msg.obj = e.getLocalizedMessage();
					msg.arg2 = R.string.error_importing_data;
					m_handler.handleMessage(msg);
				}
			});

		} catch (final IOException e) {
			e.printStackTrace();
			m_handler.post(new Runnable() {
				public void run() {
					Message msg = new Message();
					msg.what = 0;
					msg.obj = e.getLocalizedMessage();
					msg.arg2 = R.string.error_importing_data;
					m_handler.handleMessage(msg);
				}
			});

		}
	}
}
