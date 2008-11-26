package com.evancharlton.mileage.io.input;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.evancharlton.mileage.R;

public abstract class ImportView extends Activity {
	protected final static String MESSAGE = "msg";
	protected final static String TITLE = "title";

	protected TextView m_title;
	protected String m_ext = "";
	protected ProgressDialog m_progress = null;

	public void onCreate(Bundle savedInstanceState, String ext) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.import);
		m_ext = ext;
	}

	public void onResume() {
		super.onResume();
		initUI();
	}

	protected void initUI() {
		m_title = (TextView) findViewById(R.id.title);
	}

	protected void input() {
		m_progress = ProgressDialog.show(this, getString(R.string.exporting_title), getString(R.string.exporting));
		Thread t = new Thread(m_importer);
		t.start();
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

			Bundle data = msg.getData();

			AlertDialog dlg = new AlertDialog.Builder(ImportView.this).create();
			dlg.setTitle(data.getString(TITLE));
			dlg.setMessage(data.getString(MESSAGE));
			dlg.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			dlg.show();
		}
	};
}
