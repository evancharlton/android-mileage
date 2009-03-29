package com.evancharlton.mileage.io.output;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.evancharlton.mileage.HelpDialog;
import com.evancharlton.mileage.R;

public abstract class ExportView extends Activity {
	protected final static String MESSAGE = "msg";
	protected final static String TITLE = "title";
	protected final static String SUCCESS = "success";

	protected final static int DIALOG_FINISHED = 1;
	protected final static int DIALOG_EXPORTING = 2;

	protected static String s_title = "";
	protected static String s_message = "";

	protected TextView m_title;
	protected Button m_startBtn;
	protected EditText m_filename;
	protected String m_ext = "";

	public void onCreate(Bundle savedInstanceState, String ext) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.export);
		m_ext = ext;
	}

	public void onResume() {
		super.onResume();
		initUI();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		HelpDialog.injectHelp(menu, 'h');
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case HelpDialog.MENU_HELP:
				createHelp();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void createHelp() {
		HelpDialog.create(this, getHelpTitle(), getHelp());
	}

	protected abstract String getHelpTitle();

	protected abstract String getHelp();

	protected void initUI() {
		m_title = (TextView) findViewById(R.id.title);
		m_startBtn = (Button) findViewById(R.id.start);
		m_filename = (EditText) findViewById(R.id.filename);
		m_filename.setText(findFilename());

		m_startBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				export();
			}
		});
	}

	protected String findFilename() {
		File directory = Environment.getExternalStorageDirectory();
		String[] files = directory.list(m_filter);

		String name;
		int n = 0;
		while (true) {
			// search for the file
			name = buildName(n);
			boolean found = false;
			for (int i = 0; i < files.length; i++) {
				if (files[i].equalsIgnoreCase(name)) {
					found = true;
					break;
				}
			}
			if (!found) {
				break;
			}
			n++;
		}
		return name;
	}

	protected String getFilename() {
		return m_filename.getText().toString().trim();
	}

	protected String buildName(int num) {
		return "mileage" + (num > 0 ? "." + String.valueOf(num) : "") + "." + m_ext;
	}

	protected void export() {
		showDialog(DIALOG_EXPORTING);
		new Thread(m_exporter).start();
	}

	@Override
	protected Dialog onCreateDialog(int which) {
		switch (which) {
			case DIALOG_EXPORTING:
				ProgressDialog dlg = new ProgressDialog(this);
				dlg.setTitle(R.string.exporting_title);
				dlg.setMessage(getString(R.string.exporting));
				return dlg;
			case DIALOG_FINISHED:
				return new AlertDialog.Builder(ExportView.this).setTitle(s_title).setMessage(s_message).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dismissDialog(DIALOG_FINISHED);
						finish();
					}
				}).create();
		}
		return null;
	}

	protected Runnable m_exporter = null;

	protected FilenameFilter m_filter = new FilenameFilter() {
		public boolean accept(File dir, String filename) {
			// get the extension
			int dot_loc = filename.lastIndexOf(".");
			if (dot_loc >= 0) {
				String ext = filename.substring(dot_loc + 1);
				return ext.equalsIgnoreCase(m_ext);
			}
			return false;
		}
	};

	protected Handler m_handler = new Handler() {
		public void handleMessage(Message msg) {
			dismissDialog(DIALOG_EXPORTING);

			Bundle data = msg.getData();

			s_title = data.getString(TITLE);
			s_message = data.getString(MESSAGE);

			showDialog(DIALOG_FINISHED);
		}
	};
}
