package com.evancharlton.mileage.io.input;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Spinner;
import android.widget.TextView;
import au.com.bytecode.opencsv.CSVReader;

import com.evancharlton.mileage.FillUps;
import com.evancharlton.mileage.FillUpsProvider;
import com.evancharlton.mileage.Mileage;
import com.evancharlton.mileage.R;

public class CSVView extends ImportView {
	private List<Map<String, String>> m_data = new ArrayList<Map<String, String>>();
	private Spinner m_dateFormats;
	private List<String> m_sqls;
	private List<String[]> m_csvData;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, "csv");

		setContentView(R.layout.import_csv);

		m_title = (TextView) findViewById(R.id.title);
		m_title.setText(getString(R.string.csv_file));

		m_dateFormats = (Spinner) findViewById(R.id.date_format);

		m_importer = new Runnable() {

			@SuppressWarnings("unchecked")
			public void run() {
				final String filename = getInput();
				CSVReader csv;
				try {
					csv = new CSVReader(new FileReader(filename));
					m_csvData = csv.readAll();
					csv.close();

					String[] columns = m_csvData.get(0);
					m_csvData.remove(0);

					int date_column = 0;
					for (int i = 0; i < columns.length; i++) {
						if (columns[i].equalsIgnoreCase(FillUps.DATE)) {
							date_column = i;
							break;
						}
					}

					int id = m_dateFormats.getSelectedItemPosition();
					CharSequence[] patterns = getResources().getTextArray(R.array.date_patterns);
					String pattern = (patterns[id]).toString();
					SimpleDateFormat date_formatter = new SimpleDateFormat(pattern);

					for (String[] row : m_csvData) {
						HashMap<String, String> rowData = new HashMap<String, String>();
						for (int i = 0; i < row.length; i++) {
							String info = row[i];
							if (i == date_column) {
								Date d;
								try {
									d = date_formatter.parse(info);
									info = String.valueOf(d.getTime());
								} catch (ParseException e) {
								}
							}
							rowData.put(columns[i], info);
						}
						m_data.add(rowData);
					}

					m_handler.post(new Runnable() {
						public void run() {
							Bundle data = new Bundle();
							data.putString(MESSAGE, getString(R.string.data_parsed));
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

	@Override
	protected void input() {
		m_progress = ProgressDialog.show(this, getString(R.string.parsing_title), getString(R.string.parsing));
		Thread t = new Thread(m_importer);
		t.start();
	}

	@Override
	protected void postProcessing() {
		StringBuilder msg = new StringBuilder();
		msg.append(getString(R.string.confirm_import)).append("\n");
		msg.append(getString(R.string.date)).append(": ");

		Map<String, String> row = m_data.get(0);
		Date d = new Date(Long.parseLong(row.get(FillUps.DATE)));
		SimpleDateFormat date_format = new SimpleDateFormat("MMMM d, yyyy");
		msg.append(date_format.format(d));

		AlertDialog dlg = new AlertDialog.Builder(this).create();
		dlg.setMessage(msg);
		dlg.setTitle(R.string.confirm_data);
		dlg.setButton(getString(R.string.yes), m_confirmListener);
		dlg.setButton2(getString(R.string.no), m_confirmListener);
		dlg.show();
	}

	private void doImport() {
		m_progress = new ProgressDialog(this);
		m_progress.setTitle(R.string.importing_title);
		m_progress.setIndeterminate(false);
		m_progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		m_progress.setProgress(0);
		m_progress.setMax(m_data.size() * 2);
		m_progress.show();
		Thread t = new Thread(new Runnable() {
			public void run() {
				SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE);
				// create SQL from the parsed data
				m_sqls = new ArrayList<String>();
				for (Map<String, String> row : m_data) {
					StringBuilder sb = new StringBuilder();
					sb.append("INSERT INTO ").append(FillUpsProvider.FILLUPS_TABLE_NAME).append(" (");

					String[] row_data = row.keySet().toArray(new String[row.keySet().size()]);
					for (int i = 0; i < row_data.length; i++) {
						sb.append(row_data[i]);
						if (i < row_data.length - 1) {
							sb.append(", ");
						}
					}

					sb.append(") VALUES (");

					for (int i = 0; i < row_data.length; i++) {
						sb.append("?");
						if (i < row_data.length - 1) {
							sb.append(", ");
						}
					}
					sb.append(")");

					m_sqls.add(sb.toString());
					m_progressHandler.post(new Runnable() {
						public void run() {
							m_progressHandler.sendEmptyMessage(1);
						}
					});
				}

				for (int i = 0; i < m_sqls.size(); i++) {
					Map<String, String> row = m_data.get(i);
					String[] params = new String[row.keySet().size()];
					String[] keys = row.keySet().toArray(new String[params.length]);
					for (int j = 0; j < keys.length; j++) {
						params[j] = row.get(keys[j]);
					}

					db.execSQL(m_sqls.get(i), params);
				}
				db.close();
				m_handler.post(new Runnable() {
					public void run() {
						Bundle data = new Bundle();
						data.putString(MESSAGE, getString(R.string.import_done_msg));
						data.putBoolean(SUCCESS, true);

						Message msg = new Message();
						msg.setData(data);
						m_importHandler.handleMessage(msg);
					}
				});
			}
		});
		t.start();
	}

	private Handler m_importHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (m_progress != null) {
				m_progress.dismiss();
			}

			final Bundle data = msg.getData();

			AlertDialog dlg = new AlertDialog.Builder(CSVView.this).create();
			final boolean success = data.getBoolean(SUCCESS, false);
			if (success) {
				dlg.setTitle(R.string.success);
			} else {
				dlg.setTitle(R.string.error);
			}
			dlg.setMessage(data.getString(MESSAGE));
			dlg.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			dlg.show();
		}
	};

	private Handler m_progressHandler = new Handler() {
		public void handleMessage(Message msg) {
			m_progress.setProgress(m_progress.getProgress() + 1);
		}
	};

	private DialogInterface.OnClickListener m_confirmListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			if (which == DialogInterface.BUTTON1) {
				// yes
				doImport();
			}
			dialog.dismiss();
		}
	};
}
