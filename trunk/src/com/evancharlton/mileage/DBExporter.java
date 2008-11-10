package com.evancharlton.mileage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

public class DBExporter implements Runnable {
	private Handler m_handler;

	public DBExporter(Handler handler) {
		m_handler = handler;
	}

	public void run() {
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME);
			out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/" + FillUpsProvider.DATABASE_NAME);

			FileChannel inChannel = in.getChannel();
			FileChannel outChannel = out.getChannel();

			outChannel.transferFrom(inChannel, 0, inChannel.size());

			inChannel.close();
			outChannel.close();
			in.close();
			out.close();
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
