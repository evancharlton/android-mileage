package com.evancharlton.mileage.io.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Message;
import android.widget.TextView;

import com.evancharlton.mileage.FillUpsProvider;
import com.evancharlton.mileage.Mileage;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.models.FillUp;

public class SQLView extends ImportView {

	@Override
	protected String getTag() {
		return "SQLImport";
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, "sql");

		m_title = (TextView) findViewById(R.id.title);
		m_title.setText(getString(R.string.sql_file));

		m_importer = new Runnable() {
			public void run() {
				final String filename = getInput();
				File input = new File(filename);
				try {
					BufferedReader in = new BufferedReader(new FileReader(input));
					HashMap<String, String> fillupsProjection = FillUpsProvider.getFillUpsProjection();
					Set<String> keySet = fillupsProjection.keySet();
					keySet.remove(FillUp._ID);
					SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE);

					String line;
					while ((line = in.readLine()) != null) {
						if (line.startsWith("--")) {
							continue;
						}
						line = line.trim();
						if (line.endsWith("\\")) {
							line += "\n" + in.readLine();
						}
						db.execSQL(line);
					}
					db.close();
					in.close();

					m_handler.post(new Runnable() {
						public void run() {
							Bundle data = new Bundle();
							data.putString(MESSAGE, getString(R.string.import_done_msg) + "\n" + filename);
							data.putBoolean(SUCCESS, true);

							Message msg = new Message();
							msg.setData(data);
							m_handler.handleMessage(msg);
						}
					});
				} catch (final FileNotFoundException e) {
					m_handler.post(new Runnable() {
						public void run() {
							Bundle data = new Bundle();
							data.putString(MESSAGE, e.getMessage());
							data.putBoolean(SUCCESS, false);

							Message msg = new Message();
							msg.setData(data);
							m_handler.handleMessage(msg);
						}
					});
				} catch (final IOException e) {
					m_handler.post(new Runnable() {
						public void run() {
							Bundle data = new Bundle();
							data.putString(MESSAGE, e.getMessage());
							data.putBoolean(SUCCESS, false);

							Message msg = new Message();
							msg.setData(data);
							m_handler.handleMessage(msg);
						}
					});
				}
			}
		};
	}
}
