package com.evancharlton.mileage.io;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.evancharlton.mileage.io.importers.CsvWizardActivity;

public class CsvImportActivity extends CsvWizardActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle data = getIntent().getExtras();
		for (String key : data.keySet()) {
			Log.d("DATA", key + " : " + String.valueOf(data.get(key)));
		}
	}

	@Override
	protected void buildIntent(Intent intent) {
		// TODO Auto-generated method stub
	}
}
