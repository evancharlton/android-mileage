package com.evancharlton.mileage.io.input;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.evancharlton.mileage.FillUpsProvider;
import com.evancharlton.mileage.Mileage;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.models.FillUp;
import com.evancharlton.mileage.models.Vehicle;

public class CSVViewConfirm extends Dialog {
	private Context m_context;
	private ProgressDialog m_progress;
	private List<Map<String, String>> m_data;
	private List<String> m_sqls;
	private SimpleCursorAdapter m_vehiclesAdapter;
	private Map<String, Integer> m_vehicleMapping = new HashMap<String, Integer>();
	private Button m_importBtn;
	private Map<Integer, Spinner> m_vehicleSpinners = new HashMap<Integer, Spinner>();
	private Map<String, Integer> m_vehicleIdsToFakeIds = new HashMap<String, Integer>();

	private static final String PARSED_ID = "parsed_id";
	private static final String PICKED_ID = "picked_id";

	public CSVViewConfirm(Context context) {
		super(context);
		m_context = context;
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		m_data = new ArrayList<Map<String, String>>();
		m_sqls = new ArrayList<String>();

		if (m_context instanceof CSVView) {
			CSVView c = (CSVView) m_context;
			m_data = c.getData();
			m_vehicleMapping = c.getVehicleMapping();
		}

		setContentView(R.layout.import_csv_confirm);
		initUI();

		setTitle(R.string.import_options);
	}

	private void initUI() {
		// parse the date
		Map<String, String> row = m_data.get(0);
		Date d = new Date(Long.parseLong(row.get(FillUp.DATE)));
		SimpleDateFormat date_format = new SimpleDateFormat("MMMM d, yyyy");

		TextView parsed_date = (TextView) findViewById(R.id.parsed_date);
		parsed_date.setText(date_format.format(d));

		// set up the view for doing vehicle mapping
		String[] projection = new String[] {
				Vehicle._ID,
				Vehicle.TITLE
		};
		String[] vehicle_from = new String[] {
			Vehicle.TITLE
		};
		int[] vehicle_to = new int[] {
			android.R.id.text1
		};

		SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE);
		Cursor vehicleCursor = db.query(FillUpsProvider.VEHICLES_TABLE_NAME, projection, null, null, null, null, null);
		m_vehiclesAdapter = new SimpleCursorAdapter(m_context, android.R.layout.simple_spinner_item, vehicleCursor, vehicle_from, vehicle_to);
		m_vehiclesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		String[] from = new String[] {
				PARSED_ID,
				PICKED_ID
		};
		int[] to = new int[] {
				R.id.vehicle_id,
				R.id.vehicle_spinner
		};
		List<Map<String, ?>> data = new ArrayList<Map<String, ?>>();
		int def_id = -100;
		for (String vid : m_vehicleMapping.keySet()) {
			Map<String, String> vehicle = new HashMap<String, String>();
			vehicle.put(PARSED_ID, vid);
			vehicle.put(PICKED_ID, String.valueOf(def_id));

			m_vehicleSpinners.put(def_id, null);
			m_vehicleIdsToFakeIds.put(vid, def_id);
			data.add(vehicle);

			def_id--;
		}
		SimpleAdapter mappingAdapter = new SimpleAdapter(m_context, data, R.layout.import_csv_confirm_row, from, to);
		mappingAdapter.setViewBinder(m_mappingBinder);

		ListView list = (ListView) findViewById(R.id.vehicle_mapping);
		list.setAdapter(mappingAdapter);

		m_importBtn = (Button) findViewById(R.id.start);
		m_importBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				doImport();
				dismiss();
			}
		});

		vehicleCursor.close();
		db.close();
	}

	private void doImport() {
		m_progress = new ProgressDialog(m_context);
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
				int vehicle_id_column = -1;
				for (Map<String, String> row : m_data) {
					StringBuilder sb = new StringBuilder();
					sb.append("INSERT INTO ").append(FillUpsProvider.FILLUPS_TABLE_NAME).append(" (");

					String[] row_data = row.keySet().toArray(new String[row.keySet().size()]);

					for (int i = 0; i < row_data.length; i++) {
						if (vehicle_id_column < 0 && row_data[i].equalsIgnoreCase(FillUp.VEHICLE_ID)) {
							vehicle_id_column = i;
						}
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
						if (j == vehicle_id_column) {
							Integer fake_id = m_vehicleIdsToFakeIds.get(params[j]);
							if (fake_id != null) {
								Spinner s = m_vehicleSpinners.get(fake_id);
								if (s != null) {
									long v_id = s.getSelectedItemId();
									params[j] = String.valueOf(v_id);
								}
							}
						}
					}

					db.execSQL(m_sqls.get(i), params);
					m_progressHandler.post(new Runnable() {
						public void run() {
							m_progressHandler.sendEmptyMessage(1);
						}
					});
				}
				db.close();
				m_handler.post(new Runnable() {
					public void run() {
						Bundle data = new Bundle();
						data.putString(CSVView.MESSAGE, m_context.getString(R.string.import_done_msg));
						data.putBoolean(CSVView.SUCCESS, true);

						Message msg = new Message();
						msg.setData(data);
						m_importHandler.handleMessage(msg);
					}
				});
			}
		});
		t.start();
	}

	protected Handler m_handler = new Handler() {
		public void handleMessage(Message msg) {
			dismiss();
		}
	};

	private Handler m_progressHandler = new Handler() {
		public void handleMessage(Message msg) {
			m_progress.setProgress(m_progress.getProgress() + 1);
		}
	};

	private Handler m_importHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (m_progress != null) {
				m_progress.dismiss();
			}

			final Bundle data = msg.getData();

			AlertDialog dlg = new AlertDialog.Builder(m_context).create();
			final boolean success = data.getBoolean(CSVView.SUCCESS, false);
			if (success) {
				dlg.setTitle(R.string.success);
			} else {
				dlg.setTitle(R.string.error);
			}
			dlg.setMessage(data.getString(CSVView.MESSAGE) + ((CSVView) m_context).getInput());
			dlg.setButton(m_context.getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			dlg.show();
		}
	};

	private SimpleAdapter.ViewBinder m_mappingBinder = new SimpleAdapter.ViewBinder() {
		public boolean setViewValue(View view, Object data, String textRepresentation) {
			int v = 1;
			try {
				v = Integer.valueOf(textRepresentation);
			} catch (NumberFormatException e) {
				// squish!
			}
			if (v < 0) {
				Spinner s = (Spinner) view;
				s.setAdapter(m_vehiclesAdapter);
				m_vehicleSpinners.put(v, s);
				return true;
			}
			return false;
		}
	};
}
