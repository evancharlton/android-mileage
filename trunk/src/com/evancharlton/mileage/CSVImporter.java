package com.evancharlton.mileage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;

public class CSVImporter implements Runnable {
	private Handler m_handler;

	public CSVImporter(Handler handler) {
		m_handler = handler;
	}

	public void run() {
		File input = new File("/sdcard/mileage.csv");
		try {
			FileReader in = new FileReader(input);

			StringBuilder sb = new StringBuilder();
			int c;
			while ((c = in.read()) != -1) {
				sb.append((char) c);
			}

			in.close();
			String data = sb.toString();
			String[] lines = data.split("\n");
			for (int i = 0; i < lines.length; i++) {
				lines[i] = lines[i].trim();
			}

			// get the headers
			String[] columns = explode(lines[0]);

			// get the data
			ArrayList<HashMap<String, String>> rows = new ArrayList<HashMap<String, String>>();
			for (int i = 1; i < lines.length; i++) {
				String[] d = explode(lines[i]);
				HashMap<String, String> row = new HashMap<String, String>();
				for (int j = 0; j < columns.length; j++) {
					row.put(columns[j], d[j]);
				}
				rows.add(row);
			}

			insertRows(rows);
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

	@SuppressWarnings("unchecked")
	private void insertRows(ArrayList<HashMap<String, String>> rows) {
		HashMap<String, String> fillupsProjection = FillUpsProvider.getFillUpsProjection();
		Set<String> keySet = fillupsProjection.keySet();
		keySet.remove(FillUps._ID);
		SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE);

		for (HashMap<String, String> row : rows) {
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(FillUpsProvider.FILLUPS_TABLE_NAME).append(" (");
			HashMap<String, String> rowClone = (HashMap<String, String>) row.clone();
			for (String key : row.keySet()) {
				if (keySet.contains(key)) {
					sb.append("'").append(key).append("', ");
				} else {
					rowClone.remove(key);
				}
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.deleteCharAt(sb.length() - 1);
			sb.append(") VALUES (");

			String[] data = new String[rowClone.keySet().size()];
			int i = 0;
			for (String key : rowClone.keySet()) {
				String val = rowClone.get(key);
				if (val == null) {
					val = "";
				}
				data[i++] = val;
				sb.append("?");
				if (i != data.length) {
					sb.append(", ");
				}
			}
			sb.append(");");
			db.execSQL(sb.toString(), data);
		}
		db.close();
	}

	private String[] explode(String str) {
		String[] columns = str.split(",");
		// remove the quotes
		for (int i = 0; i < columns.length; i++) {
			int len = columns[i].length();
			columns[i] = columns[i].substring(1, len - 1);
		}

		return columns;
	}
}
