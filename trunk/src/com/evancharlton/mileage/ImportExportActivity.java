package com.evancharlton.mileage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ImportExportActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.import_export);

		map(R.id.import_button, ImportActivity.class);
		map(R.id.export_button, ExportActivity.class);
	}

	private final void map(final int id, final Class<? extends Activity> cls) {
		findViewById(id).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(ImportExportActivity.this, cls));
			}
		});
	}
}
