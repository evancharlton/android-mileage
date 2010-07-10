package com.evancharlton.mileage.io.importers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.evancharlton.mileage.R;

public abstract class CsvWizardActivity extends Activity implements View.OnClickListener {
	private Button mNextButton;
	private Button mPrevButton;
	protected LinearLayout mContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.wizard);

		mContainer = (LinearLayout) findViewById(R.id.container);

		mNextButton = (Button) findViewById(R.id.next);
		mNextButton.setOnClickListener(this);
		mPrevButton = (Button) findViewById(R.id.previous);
		mPrevButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.next:
				Intent intent = getIntent();
				Bundle extras = intent.getExtras();
				if (extras != null) {
					for (String key : extras.keySet()) {
						intent.putExtra(key, extras.getString(key));
					}
				}
				buildIntent(intent);
				startActivity(intent);
				break;
			case R.id.previous:
				finish();
				break;
		}
	}

	protected abstract void buildIntent(Intent intent);
}
