package com.evancharlton.mileage.io.input;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.os.Bundle;
import android.os.Message;
import android.widget.TextView;

import com.evancharlton.mileage.FillUpsProvider;
import com.evancharlton.mileage.Mileage;
import com.evancharlton.mileage.R;

public class DBView extends ImportView {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, "db");

		m_title = (TextView) findViewById(R.id.title);
		m_title.setText(getString(R.string.sqlite));

		super.m_importer = new Runnable() {
			public void run() {
				final String filename = getInput();
				FileInputStream in = null;
				FileOutputStream out = null;
				try {
					in = new FileInputStream(filename);
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
							Bundle data = new Bundle();
							data.putString(MESSAGE, ioe.getMessage());
							data.putBoolean(SUCCESS, false);

							Message msg = new Message();
							msg.setData(data);
							m_handler.handleMessage(msg);
						}
					});
					return;
				}
				m_handler.post(new Runnable() {
					public void run() {
						Bundle data = new Bundle();
						data.putString(MESSAGE, getString(R.string.import_done_msg) + "\n" + filename);
						data.putBoolean(SUCCESS, true);

						Message msg = new Message();
						msg.setData(data);
						m_handler.handleMessage(msg);
					}
				});
			}
		};
	}
}
