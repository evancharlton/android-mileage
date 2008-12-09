package com.evancharlton.mileage.io.input;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.evancharlton.mileage.HelpDialog;
import com.evancharlton.mileage.R;

public abstract class ImportView extends Activity {
	protected final static String MESSAGE = "msg";
	protected final static String TITLE = "title";
	protected final static String SUCCESS = "success";

	protected TextView m_title;
	protected String m_ext = "";
	protected ProgressDialog m_progress = null;
	protected Spinner m_fileSelector;
	protected Button m_startBtn;

	public void onCreate(Bundle savedInstanceState, String ext) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.import_layout);
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
				HelpDialog.create(this, R.string.help_title_history, R.string.help_history);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected abstract String getHelpTitle();

	protected abstract String getHelp();

	protected void initUI() {
		m_title = (TextView) findViewById(R.id.title);
		m_fileSelector = (Spinner) findViewById(R.id.filename);
		m_startBtn = (Button) findViewById(R.id.start);

		m_startBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				input();
			}
		});

		populateFileSelector();
	}

	protected void populateFileSelector() {
		File directory = Environment.getExternalStorageDirectory();
		String[] files = directory.list(m_filter);
		Arrays.sort(files);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (String file : files) {
			adapter.add(file);
		}
		m_fileSelector.setAdapter(adapter);
	}

	public String getInput() {
		return Environment.getExternalStorageDirectory() + "/" + (String) m_fileSelector.getSelectedItem();
	}

	protected void postProcessing() {
	}

	protected void input() {
		m_progress = ProgressDialog.show(this, getString(R.string.importing_title), getString(R.string.importing));
		Thread t = new Thread(m_importer);
		t.start();
	}

	protected void showAlert(final String title, final String message) {
		m_alertHandler.post(new Runnable() {
			public void run() {
				Bundle data = new Bundle();
				data.putString(MESSAGE, message);
				data.putString(TITLE, title);
				data.putBoolean(SUCCESS, false);

				Message msg = new Message();
				msg.setData(data);
				m_alertHandler.handleMessage(msg);
			}
		});
	}

	protected Runnable m_importer = null;

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

			final Bundle data = msg.getData();

			AlertDialog dlg = new AlertDialog.Builder(ImportView.this).create();
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
					if (success) {
						postProcessing();
					}
				}
			});
			dlg.show();
		}
	};

	protected Handler m_alertHandler = new Handler() {
		public void handleMessage(Message msg) {
			final Bundle data = msg.getData();

			AlertDialog dlg = new AlertDialog.Builder(ImportView.this).create();
			dlg.setMessage(data.getString(MESSAGE));
			dlg.setTitle(data.getString(TITLE));
			dlg.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			dlg.show();
		}
	};
}
