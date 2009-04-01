package com.evancharlton.mileage.io.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.widget.TextView;

import com.evancharlton.mileage.FillUpsProvider;
import com.evancharlton.mileage.Mileage;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.models.FillUp;
import com.evancharlton.mileage.models.Vehicle;

public class SQLView extends ExportView {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, "sql");
		m_title = (TextView) findViewById(R.id.title);
		m_title.setText(R.string.sql_file);

		super.m_exporter = new Runnable() {
			public void run() {
				HashMap<String, String> fillupsProjection = FillUpsProvider.getFillUpsProjection();
				HashMap<String, String> vehiclesProjection = FillUpsProvider.getVehiclesProjection();

				Set<String> keySet = fillupsProjection.keySet();
				String[] proj = keySet.toArray(new String[keySet.size()]);
				SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME, null, SQLiteDatabase.OPEN_READONLY);
				Cursor c = db.query(FillUpsProvider.FILLUPS_TABLE_NAME, proj, null, null, null, null, FillUp._ID + " ASC");

				StringBuilder sb = new StringBuilder();
				sb.append("-- Exported database: ").append(FillUpsProvider.DATABASE_NAME).append("\n");
				sb.append("-- Exported version: ").append(FillUpsProvider.DATABASE_VERSION).append("\n");
				sb.append("-- Begin table: ").append(FillUpsProvider.FILLUPS_TABLE_NAME).append("\n");
				c.moveToFirst();
				while (c.isAfterLast() == false) {
					sb.append("INSERT INTO ").append(FillUpsProvider.FILLUPS_TABLE_NAME).append(" ");
					keySetToSQL(keySet, sb);
					keySetToValues(keySet, sb, c);
					c.moveToNext();
				}
				sb.append("-- End table: ").append(FillUpsProvider.FILLUPS_TABLE_NAME).append("\n");

				sb.append("-- Begin table: ").append(FillUpsProvider.VEHICLES_TABLE_NAME).append("\n");
				keySet = vehiclesProjection.keySet();
				proj = keySet.toArray(new String[keySet.size()]);
				c = db.query(FillUpsProvider.VEHICLES_TABLE_NAME, proj, null, null, null, null, Vehicle._ID + " ASC");
				c.moveToFirst();
				while (c.isAfterLast() == false) {
					sb.append("INSERT INTO ").append(FillUpsProvider.VEHICLES_TABLE_NAME);
					keySetToSQL(keySet, sb);
					keySetToValues(keySet, sb, c);
					c.moveToNext();
				}
				sb.append("-- End table: ").append(FillUpsProvider.VEHICLES_TABLE_NAME).append("\n");

				c.close();
				db.close();

				// write to a file
				try {
					File output = new File(Environment.getExternalStorageDirectory() + "/" + getFilename());
					FileWriter out = new FileWriter(output);

					out.write(sb.toString());
					out.flush();
					out.close();
				} catch (final IOException e) {
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
			}

			private void keySetToSQL(Set<String> columns, StringBuilder sb) {
				sb.append(" (");
				for (String key : columns) {
					sb.append("'").append(key).append("', ");
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.deleteCharAt(sb.length() - 1);
				sb.append(") ");
			}

			private void keySetToValues(Set<String> columns, StringBuilder sb, Cursor c) {
				sb.append(" VALUES (");
				int i = 1;
				for (String key : columns) {
					String val = c.getString(c.getColumnIndex(key));
					if (val == null) {
						val = "";
					}
					val = val.replaceAll("'", "\\'");
					sb.append("'").append(val).append("'");
					if (i != c.getColumnCount()) {
						sb.append(", ");
					}
					i++;
				}
				sb.append(");\n");
			}
		};
	}

	@Override
	protected String getHelp() {
		return getString(R.string.help_export_sql);
	}

	@Override
	protected String getHelpTitle() {
		return getString(R.string.help_export_sql_title);
	}
}
