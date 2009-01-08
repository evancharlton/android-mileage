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

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Spinner;
import android.widget.TextView;
import au.com.bytecode.opencsv.CSVReader;

import com.evancharlton.mileage.FillUps;
import com.evancharlton.mileage.R;

public class CSVView extends ImportView {
	private List<Map<String, String>> m_data = new ArrayList<Map<String, String>>();
	private Spinner m_dateFormats;
	private List<String[]> m_csvData;
	private Map<String, Integer> m_vehicleMapping = new HashMap<String, Integer>();

	public static final String INTENT_EXTRA = "intent_extra";

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
					int vehicle_id_column = 0;
					int amount_column = 0;
					for (int i = 0; i < columns.length; i++) {
						columns[i] = columns[i].trim();

						// translate them back into SQL columns
						for (String key : FillUps.PLAINTEXT.keySet()) {
							String val = FillUps.PLAINTEXT.get(key).trim();
							if (val.equalsIgnoreCase(columns[i])) {
								columns[i] = key;
							}
						}

						if (columns[i].equalsIgnoreCase(FillUps.DATE)) {
							date_column = i;
						} else if (columns[i].equalsIgnoreCase(FillUps.VEHICLE_ID)) {
							vehicle_id_column = i;
						} else if (columns[i].equalsIgnoreCase(FillUps.AMOUNT)) {
							amount_column = i;
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
							} else if (i == vehicle_id_column) {
								m_vehicleMapping.put(info, 1);
							} else if (i == amount_column) {
								if (info.length() == 0) {
									// we have a dummy row, most likely a bad
									// export. Just ignore it and move on.
									rowData = new HashMap<String, String>();
									break;
								}
							}
							rowData.put(columns[i], info);
						}
						if (rowData.keySet().size() > 0) {
							m_data.add(rowData);
						}
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

		m_handler = new Handler() {
			public void handleMessage(Message msg) {
				Bundle data = msg.getData();
				if (data.getBoolean(SUCCESS, false)) {
					m_progress.dismiss();
					CSVViewConfirm confirm = new CSVViewConfirm(CSVView.this);
					confirm.show();
				}
			}
		};
	}

	public List<Map<String, String>> getData() {
		return m_data;
	}

	public Map<String, Integer> getVehicleMapping() {
		return m_vehicleMapping;
	}

	@Override
	protected void input() {
		m_progress = ProgressDialog.show(this, getString(R.string.parsing_title), getString(R.string.parsing));
		Thread t = new Thread(m_importer);
		t.start();
	}

	@Override
	protected String getHelp() {
		return getString(R.string.help_import_csv);
	}

	@Override
	protected String getHelpTitle() {
		return getString(R.string.help_import_csv_title);
	}
}
