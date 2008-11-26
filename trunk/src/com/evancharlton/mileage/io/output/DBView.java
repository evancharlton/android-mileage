package com.evancharlton.mileage.io.output;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.widget.TextView;

import com.evancharlton.mileage.FillUpsProvider;
import com.evancharlton.mileage.Mileage;
import com.evancharlton.mileage.R;

public class DBView extends ExportView {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, "db");
		m_title = (TextView) findViewById(R.id.title);
		m_title.setText(R.string.sqlite);

		super.m_exporter = new Runnable() {
			public void run() {
				FileInputStream in = null;
				FileOutputStream out = null;
				try {
					in = new FileInputStream("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME);
					out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/" + getFilename());

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
							Bundle data = new Bundle();
							data.putString(MESSAGE, ioe.getMessage());
							data.putString(TITLE, getString(R.string.error));

							Message msg = new Message();
							msg.setData(data);
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
						// meh, nothing to do if this fails
					}
				}
				m_handler.post(new Runnable() {
					public void run() {
						Bundle data = new Bundle();
						data.putString(MESSAGE, getString(R.string.export_finished_msg) + "\n" + getFilename());
						data.putString(TITLE, getString(R.string.success));

						Message msg = new Message();
						msg.setData(data);
						m_handler.handleMessage(msg);
					}
				});
			}
		};
	}
}