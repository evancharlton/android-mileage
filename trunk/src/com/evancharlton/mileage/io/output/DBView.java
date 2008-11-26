package com.evancharlton.mileage.io.output;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.evancharlton.mileage.R;

public class DBView extends Activity {
	private TextView m_title;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.export);

		m_title = (TextView) findViewById(R.id.title);
		m_title.setText(R.string.sqlite);
	}
}
