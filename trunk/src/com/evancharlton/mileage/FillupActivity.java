package com.evancharlton.mileage;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.evancharlton.mileage.dao.Field;
import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.dao.FillupField;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.FieldsTable;
import com.evancharlton.mileage.provider.tables.FillupsFieldsTable;
import com.evancharlton.mileage.provider.tables.FillupsTable;
import com.evancharlton.mileage.views.FieldView;

public class FillupActivity extends Activity {
	private EditText mOdometer;
	private EditText mVolume;
	private EditText mPrice;
	private Button mDate;
	private Button mSave;
	private CheckBox mPartial;
	private LinearLayout mFieldsContainer;
	private final ArrayList<FieldView> mFields = new ArrayList<FieldView>();
	private ContentValues mData;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fillup);

		mOdometer = (EditText) findViewById(R.id.odometer);
		mVolume = (EditText) findViewById(R.id.volume);
		mPrice = (EditText) findViewById(R.id.price);
		mDate = (Button) findViewById(R.id.date);
		mSave = (Button) findViewById(R.id.save_btn);
		mPartial = (CheckBox) findViewById(R.id.partial);
		mFieldsContainer = (LinearLayout) findViewById(R.id.container);
		Cursor fields = managedQuery(Uri.withAppendedPath(FillUpsProvider.BASE_URI, FieldsTable.FIELDS_URI), FieldsTable.getFullProjectionArray(),
				null, null, null);
		LayoutInflater inflater = LayoutInflater.from(this);
		while (fields.moveToNext()) {
			String hint = fields.getString(fields.getColumnIndex(Field.TITLE));
			long id = fields.getLong(fields.getColumnIndex(Field._ID));
			View container = inflater.inflate(R.layout.fillup_field, null);
			FieldView field = (FieldView) container.findViewById(R.id.field);
			field.setFieldId(id);
			field.setId((int) id);
			field.setHint(hint);
			mFieldsContainer.addView(container);
			mFields.add(field);

			if (savedInstanceState != null) {
				String value = savedInstanceState.getString(field.getKey());
				if (value != null) {
					field.setText(value);
				}
			}
		}
		if (fields.getCount() == 0) {
			mFieldsContainer.setVisibility(View.GONE);
		}

		mSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mData.put(Fillup.VOLUME, mVolume.getText().toString());
				mData.put(Fillup.PRICE, mPrice.getText().toString());
				mData.put(Fillup.ODOMETER, mOdometer.getText().toString());
				mData.put(Fillup.DATE, System.currentTimeMillis());
				mData.put(Fillup.PARTIAL, mPartial.isChecked());

				// save the fillup
				Long id = mData.getAsLong(Fillup._ID);
				Uri uri = Uri.withAppendedPath(FillUpsProvider.BASE_URI, FillupsTable.FILLUPS_URI);
				if (id == null) {
					uri = getContentResolver().insert(uri, mData);
					id = Long.parseLong(uri.getPathSegments().get(1));
				} else {
					uri = ContentUris.withAppendedId(uri, id);
					getContentResolver().update(uri, mData, Fillup._ID + " = ?", new String[] {
						String.valueOf(id)
					});
				}

				// save the meta data
				ContentValues[] values = new ContentValues[mFields.size()];
				int i = 0;
				for (FieldView fieldView : mFields) {
					ContentValues fieldValues = new ContentValues();
					fieldValues.put(FillupField.FILLUP_ID, id);
					fieldValues.put(FillupField.TEMPLATE_ID, fieldView.getFieldId());
					fieldValues.put(FillupField.VALUE, fieldView.getText().toString());
					values[i++] = fieldValues;
				}
				Uri fieldsUri = Uri.withAppendedPath(FillUpsProvider.BASE_URI, FillupsFieldsTable.FILLUPS_FIELDS_URI);
				getContentResolver().bulkInsert(fieldsUri, values);
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		for (FieldView fieldView : mFields) {
			outState.putString(fieldView.getKey(), fieldView.getText().toString());
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
}
