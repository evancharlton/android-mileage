package com.evancharlton.mileage.io.input;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.evancharlton.mileage.MileageActivity;
import com.evancharlton.mileage.R;

public abstract class ImportView extends MileageActivity {
	protected final static String MESSAGE = "msg";
	protected final static String TITLE = "title";
	protected final static String SUCCESS = "success";

	protected final static int DIALOG_FINISHED = 1;
	protected final static int DIALOG_IMPORTING = 2;

	protected TextView m_title;
	protected String m_ext = "";
	protected Spinner m_fileSelector;
	protected Button m_startBtn;

	protected static int s_title;
	protected static String s_message;
	protected static boolean s_success = false;

	public void onCreate(Bundle savedInstanceState, String ext) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.import_layout);
		m_ext = ext;
	}

	public void onResume() {
		super.onResume();
		initUI();
	}

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
		s_success = false;
		new Thread(m_importer).start();
		showDialog(DIALOG_IMPORTING);
	}

	@Override
	protected Dialog onCreateDialog(int which) {
		switch (which) {
			case DIALOG_FINISHED:
				return new AlertDialog.Builder(ImportView.this).setTitle(s_title).setMessage(s_message).setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dismissDialog(DIALOG_FINISHED);
						if (s_success) {
							postProcessing();
						}
					}
				}).create();
			case DIALOG_IMPORTING:
				ProgressDialog dlg = new ProgressDialog(this);
				dlg.setTitle(R.string.importing_title);
				dlg.setMessage(getString(R.string.importing));
				return dlg;
		}
		return null;
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
			dismissDialog(DIALOG_IMPORTING);

			final Bundle data = msg.getData();

			final boolean success = data.getBoolean(SUCCESS, false);
			s_title = success ? R.string.success : R.string.error;
			s_success = success;
			s_message = data.getString(MESSAGE);

			showDialog(DIALOG_FINISHED);
		}
	};
}
