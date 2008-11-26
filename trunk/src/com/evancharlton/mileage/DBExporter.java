package com.evancharlton.mileage;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class DBExporter extends Activity {
	private TextView m_title;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.export);

		m_title = (TextView) findViewById(R.id.title);
		m_title.setText(R.string.sqlite);
	}
}
