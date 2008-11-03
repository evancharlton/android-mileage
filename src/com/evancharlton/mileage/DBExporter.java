package com.evancharlton.mileage;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Handler;
import android.os.Message;

public class DBExporter implements Runnable {
	private Handler m_handler;

	public DBExporter(Handler handler) {
		m_handler = handler;
	}

	public void run() {
		FileReader in = null;
		FileWriter out = null;
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
					msg.what = 0;
					msg.arg2 = R.string.error_exporting_data;
					msg.obj = ioe.getMessage();
					m_handler.handleMessage(msg);
				}
			});
			return;
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
		m_handler.post(new Runnable() {
			public void run() {
				Message msg = new Message();
				msg.what = 1;
				msg.arg1 = R.string.export_finished_msg;
				msg.arg2 = R.string.export_finished;
				msg.obj = FillUpsProvider.DATABASE_NAME;
				m_handler.handleMessage(msg);
			}
		});
	}
}
