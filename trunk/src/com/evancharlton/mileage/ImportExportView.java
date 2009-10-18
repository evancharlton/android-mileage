package com.evancharlton.mileage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class ImportExportView extends Activity {
	private static final int MENU_ERASE = Menu.FIRST;
	private static final int ERASE_DIALOG_ID = 0;
	private static final int DIALOG_RESTORED = 1;
	private static final String BACKUP_FILENAME = Environment.getExternalStorageDirectory() + "/mileage/backup.db";

	private Map<ImageButton, Class<?>> m_actions = new HashMap<ImageButton, Class<?>>();
	private Button m_restoreBtn;

	private AlertDialog m_eraseDialog;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.importexport);

		initUI();
	}

	private void initUI() {
		m_actions.put((ImageButton) findViewById(R.id.import_db_btn), com.evancharlton.mileage.io.input.DBView.class);
		m_actions.put((ImageButton) findViewById(R.id.import_sql_btn), com.evancharlton.mileage.io.input.SQLView.class);
		m_actions.put((ImageButton) findViewById(R.id.import_csv_btn), com.evancharlton.mileage.io.input.CSVView.class);
		m_actions.put((ImageButton) findViewById(R.id.export_db_btn), com.evancharlton.mileage.io.output.DBView.class);
		m_actions.put((ImageButton) findViewById(R.id.export_sql_btn), com.evancharlton.mileage.io.output.SQLView.class);
		m_actions.put((ImageButton) findViewById(R.id.export_csv_btn), com.evancharlton.mileage.io.output.CSVView.class);

		for (ImageButton btn : m_actions.keySet()) {
			btn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent();
					Class<?> cls = m_actions.get(v);
					if (cls != null) {
						intent.setClass(ImportExportView.this, cls);
						startActivity(intent);
					}
				}
			});
		}

		m_eraseDialog = new AlertDialog.Builder(this).create();
		m_eraseDialog.setMessage(getString(R.string.confirm_erase));
		m_eraseDialog.setCancelable(false);
		m_eraseDialog.setButton(getString(android.R.string.yes), m_eraseListener);
		m_eraseDialog.setButton2(getString(android.R.string.no), m_eraseListener);

		m_restoreBtn = (Button) findViewById(R.id.restore_btn);
		m_restoreBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				FileInputStream in = null;
				FileOutputStream out = null;
				try {
					in = new FileInputStream(BACKUP_FILENAME);
					out = new FileOutputStream("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME);

					FileChannel inChannel = in.getChannel();
					FileChannel outChannel = out.getChannel();

					outChannel.transferFrom(inChannel, 0, inChannel.size());

					inChannel.close();
					outChannel.close();
					in.close();
					out.close();

					// make sure the schema is up to date
					SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE);
					FillUpsProvider.upgradeDatabase(db);
					db.close();
				} catch (final IOException ioe) {
					Log.e("Mileage", "Could not restore from backup!", ioe);
				}
				showDialog(DIALOG_RESTORED);
			}
		});
		m_restoreBtn.setEnabled(new File(BACKUP_FILENAME).exists());
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case HelpDialog.MENU_HELP:
				HelpDialog.create(this, R.string.help_title_import_export, R.string.help_import_export);
				break;
			case MENU_ERASE:
				showDialog(ERASE_DIALOG_ID);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_ERASE, Menu.NONE, R.string.erase).setShortcut('0', 'e').setIcon(R.drawable.ic_menu_delete);
		HelpDialog.injectHelp(menu, 'h');
		return super.onCreateOptionsMenu(menu);
	}

	private void erase() {
		SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE);
		String sql = "DELETE FROM " + FillUpsProvider.FILLUPS_TABLE_NAME;
		db.execSQL(sql);
		sql = "DELETE FROM " + FillUpsProvider.VEHICLES_TABLE_NAME;
		db.execSQL(sql);
		sql = "DELETE FROM " + FillUpsProvider.MAINTENANCE_TABLE_NAME;
		db.execSQL(sql);
		FillUpsProvider.initTables(db);
		db.close();
	}

	protected Dialog onCreateDialog(final int id) {
		switch (id) {
			case ERASE_DIALOG_ID:
				return m_eraseDialog;
			case DIALOG_RESTORED:
				return new AlertDialog.Builder(this).setTitle(R.string.backup_restored).setMessage(R.string.backup_restored_msg).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						removeDialog(id);
					}
				}).create();
		}
		return null;
	}

	private DialogInterface.OnClickListener m_eraseListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			dismissDialog(ERASE_DIALOG_ID);
			if (which == Dialog.BUTTON1) {
				erase();
			}
		}
	};
}
