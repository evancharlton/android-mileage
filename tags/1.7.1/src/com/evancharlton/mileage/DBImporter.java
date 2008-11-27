package com.evancharlton.mileage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

public class DBImporter implements Runnable {
	private Handler m_handler;

	public DBImporter(Handler handler) {
		m_handler = handler;
	}

	public void run() {
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(Environment.getExternalStorageDirectory() + "/mileage.db");
			out = new FileOutputStream("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME);

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
		}
		m_handler.post(new Runnable() {
			public void run() {
				Message msg = new Message();
				msg.what = 1;
				msg.arg1 = R.string.import_done_msg;
				msg.arg2 = R.string.import_done;
				msg.obj = FillUpsProvider.DATABASE_NAME;
				m_handler.handleMessage(msg);
			}
		});
	}
}
