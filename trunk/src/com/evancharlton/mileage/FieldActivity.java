package com.evancharlton.mileage;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.evancharlton.mileage.dao.Field;
import com.evancharlton.mileage.dao.Dao.InvalidFieldException;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.FieldsTable;

public class FieldActivity extends Activity {
	public static final String EXTRA_FIELD_ID = "field_id";

	private EditText mTitle;
	private EditText mDescription;
	private Button mSubmit;
	private final Field mField = new Field(new ContentValues());
	private Uri mUri = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.field_form);

		mTitle = (EditText) findViewById(R.id.title);
		mDescription = (EditText) findViewById(R.id.description);
		mSubmit = (Button) findViewById(R.id.save_btn);
	}

	@Override
	protected void onResume() {
		super.onResume();

		Intent intent = getIntent();
		long id = intent.getLongExtra(EXTRA_FIELD_ID, -1L);
		if (id >= 0) {
			mUri = ContentUris.withAppendedId(Uri.withAppendedPath(FillUpsProvider.BASE_URI, FieldsTable.FIELD_URI), id);
			Cursor c = getContentResolver().query(mUri, FieldsTable.getFullProjectionArray(), null, null, null);
			if (c.getCount() > 0) {
				c.moveToFirst();
				mTitle.setText(c.getString(c.getColumnIndex(Field.TITLE)));
				mDescription.setText(c.getString(c.getColumnIndex(Field.DESCRIPTION)));
				mField.setId(id);
				mSubmit.setText(R.string.save_changes);
			}
		}

		mSubmit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				save();
			}
		});
	}

	private void save() {
		mField.setTitle(mTitle.getText().toString());
		mField.setDescription(mDescription.getText().toString());
		try {
			if (mField.save(this)) {
				finish();
			}
		} catch (InvalidFieldException e) {
			Toast.makeText(this, getString(e.getErrorMessage()), Toast.LENGTH_LONG).show();
		}
	}
}
