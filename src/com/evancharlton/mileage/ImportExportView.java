package com.evancharlton.mileage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

public class ImportExportView extends Activity {
	private ProgressDialog m_progress;
	private static final int EXPORT_DB = 1;
	private static final int EXPORT_SQL = 2;
	private static final int EXPORT_CSV = 3;
	private static final int IMPORT_DB = 4;
	private static final int IMPORT_SQL = 5;
	private static final int IMPORT_CSV = 6;
	private static final int MENU_HELP = Menu.FIRST;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.importexport);

		initUI();
	}

	private void initUI() {
		ImageButton importDB = (ImageButton) findViewById(R.id.import_db_btn);
		ImageButton importSQL = (ImageButton) findViewById(R.id.import_sql_btn);
		ImageButton importCSV = (ImageButton) findViewById(R.id.import_csv_btn);
		ImageButton exportDB = (ImageButton) findViewById(R.id.export_db_btn);
		ImageButton exportSQL = (ImageButton) findViewById(R.id.export_sql_btn);
		ImageButton exportCSV = (ImageButton) findViewById(R.id.export_csv_btn);

		importDB.setOnClickListener(new ButtonHandler(IMPORT_DB));
		importSQL.setOnClickListener(new ButtonHandler(IMPORT_SQL));
		importCSV.setOnClickListener(new ButtonHandler(IMPORT_CSV));
		exportDB.setOnClickListener(new ButtonHandler(EXPORT_DB));
		exportSQL.setOnClickListener(new ButtonHandler(EXPORT_SQL));
		exportCSV.setOnClickListener(new ButtonHandler(EXPORT_CSV));
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HELP:
				HelpDialog.create(this, R.string.help_title_import_export, new int[] {
						R.string.help_import_db,
						R.string.help_export_db,
						R.string.help_import_csv,
						R.string.help_export_csv,
						R.string.help_import_sql,
						R.string.help_export_sql
				});
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(Menu.NONE, MENU_HELP, Menu.NONE, R.string.help).setShortcut('1', 'h');

		return super.onCreateOptionsMenu(menu);
	}

	private class ButtonHandler implements View.OnClickListener {
		private int m_btn = 0;

		public ButtonHandler(int btn) {
			m_btn = btn;
		}

		public void onClick(View v) {
			Thread t = null;
			int title = R.string.exporting_title;
			switch (m_btn) {
				case EXPORT_DB:
					t = new Thread(new DBExporter(m_handler));
					break;
				case EXPORT_SQL:
					t = new Thread(new SQLExporter(m_handler));
					break;
				case EXPORT_CSV:
					t = new Thread(new CSVExporter(m_handler));
					break;
				case IMPORT_CSV:
					t = new Thread(new CSVImporter(m_handler));
					title = R.string.importing_title;
					break;
				case IMPORT_SQL:
					t = new Thread(new SQLImporter(m_handler));
					title = R.string.importing_title;
					break;
				case IMPORT_DB:
					t = new Thread(new DBImporter(m_handler));
					title = R.string.importing_title;
					break;
			}
			if (t != null) {
				m_progress = new ProgressDialog(ImportExportView.this);
				m_progress.setMessage(getString(R.string.exporting));
				m_progress.setIndeterminate(true);
				m_progress.setTitle(title);
				m_progress.show();
				t.start();
			}
		}
	}

	private Handler m_handler = new Handler() {
		public void handleMessage(Message msg) {
			m_progress.dismiss();
			AlertDialog dlg = new AlertDialog.Builder(ImportExportView.this).create();
			dlg.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			if (msg.what == 0) {
				dlg.setMessage(msg.obj.toString());
				dlg.setTitle(msg.arg2);
			} else if (msg.what == 1) {
				dlg.setTitle(msg.arg2);
				dlg.setMessage(getString(msg.arg1) + "\n" + msg.obj.toString());
			}
			dlg.show();
		}
	};
}
