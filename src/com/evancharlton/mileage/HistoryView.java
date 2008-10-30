package com.evancharlton.mileage;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HistoryView extends ListActivity {
	public static final int MENU_EXPORT = Menu.FIRST;
	public static final String TAG = "HistoryList";

	private static final int ERROR = 1;
	private static final int DONE = 2;
	private static final String[] PROJECTIONS = new String[] {
			FillUps._ID, FillUps.AMOUNT, FillUps.COST, FillUps.DATE
	};

	private ProgressDialog m_progress;
	private Handler m_handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

		Intent intent = getIntent();
		if (intent.getData() == null) {
			intent.setData(FillUps.CONTENT_URI);
		}

		getListView().setOnCreateContextMenuListener(this);

		Cursor c = managedQuery(intent.getData(), PROJECTIONS, null, null, FillUps.DEFAULT_SORT_ORDER);
		ArrayList<String> history = new ArrayList<String>();

		c.moveToFirst();
		while (c.isAfterLast() == false) {
			String amt = round(c.getDouble(1), 2);
			String cost = round(c.getDouble(2), 2);
			Date d = new Date(c.getLong(3) * 1000);
			DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
			String date = df.format(d);
			String filler = " " + getString(R.string.history_description);
			history.add(date + ": " + amt + filler + cost);
			c.moveToNext();
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.history, history.toArray(new String[history.size()]));
		setListAdapter(adapter);
	}

	private String round(double d, int precision) {
		d *= Math.pow(10, precision);
		d = Math.round(d);
		d /= Math.pow(10, precision);
		return String.valueOf(d);
	}

	private void exportData() {
		Thread t = new Thread(new FileCopy());
		m_progress = new ProgressDialog(this);
		m_progress.setMessage(getString(R.string.exporting));
		m_progress.setIndeterminate(true);
		m_progress.setTitle(R.string.exporting_title);
		m_progress.show();
		t.start();
	}

	@Override
	protected void onListItemClick(ListView lv, View v, int position, long id) {
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
		Intent intent = new Intent();
		intent.setData(uri);
		intent.setClass(HistoryView.this, FillUpView.class);
		startActivity(intent);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(Menu.NONE, MENU_EXPORT, 0, R.string.export_history).setShortcut('1', 'h');

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_EXPORT:
				exportData();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private class FileCopy implements Runnable {
		public void run() {
			FileReader in = null;
			FileWriter out = null;
			boolean error = false;
			try {
				File input = new File("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME);
				File output = new File("/sdcard/" + FillUpsProvider.DATABASE_NAME);

				in = new FileReader(input);
				out = new FileWriter(output);

				int c;
				while ((c = in.read()) != -1) {
					out.write(c);
				}
			} catch (final IOException ioe) {
				m_handler.post(new Runnable() {
					public void run() {
						Message msg = new Message();
						msg.what = ERROR;
						msg.obj = ioe.getMessage();
						endThread(msg);
					}
				});
				error = true;
			} finally {
				try {
					if (in != null) {
						in.close();
					}
					if (out != null) {
						out.close();
					}
				} catch (IOException e) {
					// meh, nothing to do
				}
			}
			if (!error) {
				m_handler.post(new Runnable() {
					public void run() {
						Message msg = new Message();
						msg.what = DONE;
						endThread(msg);
					}
				});
			}
		}
	}

	public void endThread(Message msg) {
		m_progress.dismiss();
		DialogHandler handler = new DialogHandler();
		AlertDialog dlg = new AlertDialog.Builder(HistoryView.this).create();
		dlg.setButton(getString(R.string.ok), handler);
		if (msg.what == ERROR) {
			dlg.setMessage(msg.obj.toString());
			dlg.setTitle(R.string.error_exporting_data);
		} else if (msg.what == DONE) {
			dlg.setTitle(R.string.export_finished);
			dlg.setMessage(getString(R.string.export_finished_msg) + "\n" + FillUpsProvider.DATABASE_NAME);
		}
		dlg.show();
	}

	private class DialogHandler implements DialogInterface.OnCancelListener, DialogInterface.OnClickListener, DialogInterface.OnDismissListener {
		public void onCancel(DialogInterface dialog) {
			finish();
		}

		public void onClick(DialogInterface dialog, int which) {
			finish();
		}

		public void onDismiss(DialogInterface dialog) {
			finish();
		}
	}
}
