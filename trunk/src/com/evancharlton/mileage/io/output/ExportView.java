package com.evancharlton.mileage.io.output;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.app.AlertDialog;
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

	protected TextView m_title;
	protected Button m_startBtn;
	protected EditText m_filename;
	protected String m_ext = "";
	protected ProgressDialog m_progress = null;

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
		m_progress = ProgressDialog.show(this, getString(R.string.exporting_title), getString(R.string.exporting));
		Thread t = new Thread(m_exporter);
		t.start();
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
			if (m_progress != null) {
				m_progress.dismiss();
			}

			Bundle data = msg.getData();

			AlertDialog dlg = new AlertDialog.Builder(ExportView.this).create();
			dlg.setTitle(data.getString(TITLE));
			dlg.setMessage(data.getString(MESSAGE));
			dlg.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					findFilename();
				}
			});
			dlg.show();

			if (data.getBoolean(SUCCESS, false)) {
				finish();
			}
		}
	};
}
