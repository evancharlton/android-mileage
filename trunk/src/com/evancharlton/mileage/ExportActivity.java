package com.evancharlton.mileage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.evancharlton.mileage.io.CsvExportActivity;
import com.evancharlton.mileage.io.DbExportActivity;

public class ExportActivity extends Activity {
	public static final String FILENAME = "filename";

	private static final String[] FILE_TYPES = new String[] {
			".db",
			".csv"
	};

	@SuppressWarnings("unchecked")
	private static final Class[] EXPORTERS = new Class[] {
			DbExportActivity.class,
			CsvExportActivity.class
	};

	private Spinner mFileTypes;
	private EditText mFilename;
	private Button mSubmitButton;
	private TextView mFileExt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.export_form);

		mFileTypes = (Spinner) findViewById(R.id.exporter);
		mFilename = (EditText) findViewById(R.id.output_file);
		mFileExt = (TextView) findViewById(R.id.file_extension);
		mSubmitButton = (Button) findViewById(R.id.submit);
		mSubmitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ExportActivity.this, EXPORTERS[mFileTypes.getSelectedItemPosition()]);
				intent.putExtra(ExportActivity.FILENAME, getFilename());
				startActivity(intent);
			}
		});

		mFileTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				mFileExt.setText(getExtension());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	private final String getExtension() {
		return FILE_TYPES[mFileTypes.getSelectedItemPosition()];
	}

	private String getFilename() {
		return mFilename.getText() + getExtension();
	}
}
