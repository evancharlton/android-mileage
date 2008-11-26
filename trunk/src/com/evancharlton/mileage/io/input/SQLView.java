package com.evancharlton.mileage.io.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.evancharlton.mileage.FillUps;
import com.evancharlton.mileage.FillUpsProvider;
import com.evancharlton.mileage.Mileage;
import com.evancharlton.mileage.R;

public class SQLView implements Runnable {
	private Handler m_handler;

	public SQLView(Handler handler) {
		m_handler = handler;
	}

	public void run() {
		File input = new File(Environment.getExternalStorageDirectory() + "/mileage.sql");
		try {
			BufferedReader in = new BufferedReader(new FileReader(input));
			HashMap<String, String> fillupsProjection = FillUpsProvider.getFillUpsProjection();
			Set<String> keySet = fillupsProjection.keySet();
			keySet.remove(FillUps._ID);
			SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE);

			String line;
			while ((line = in.readLine()) != null) {
				db.execSQL(line);
			}
			db.close();

			m_handler.post(new Runnable() {
				public void run() {
					Message msg = new Message();
					msg.what = 0;
					msg.arg1 = R.string.import_done_msg;
					msg.arg2 = R.string.import_done;
					msg.obj = "mileage.sql";
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
}
