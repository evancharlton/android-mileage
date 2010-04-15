package com.evancharlton.mileage;

import java.util.ArrayList;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.evancharlton.mileage.adapters.SpinnerCursorAdapter;
import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.dao.Field;
import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.dao.FillupField;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.dao.Dao.InvalidFieldException;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.FieldsTable;
import com.evancharlton.mileage.provider.tables.FillupsTable;
import com.evancharlton.mileage.provider.tables.VehiclesTable;
import com.evancharlton.mileage.views.FieldView;

public class FillupActivity extends BaseFormActivity {
	private EditText mOdometer;
	private EditText mVolume;
	private EditText mPrice;
	private Button mDate;
	private Spinner mVehicles;
	private CheckBox mPartial;
	private LinearLayout mFieldsContainer;
	private final ArrayList<FieldView> mFields = new ArrayList<FieldView>();
	private final Fillup mFillup = new Fillup(new ContentValues());

	private Bundle mIcicle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.fillup);
		// TODO: this seems shady
		mIcicle = savedInstanceState;
	}

	@Override
	protected void onResume() {
		super.onResume();

		Cursor fields = managedQuery(Uri.withAppendedPath(FillUpsProvider.BASE_URI, FieldsTable.FIELDS_URI), FieldsTable.getFullProjectionArray(),
				null, null, null);
		LayoutInflater inflater = LayoutInflater.from(this);
		mFieldsContainer.removeAllViews();
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

			if (mIcicle != null) {
				String value = mIcicle.getString(field.getKey());
				if (value != null) {
					field.setText(value);
				}
			}
		}
		if (fields.getCount() == 0) {
			mFieldsContainer.setVisibility(View.GONE);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		for (FieldView fieldView : mFields) {
			outState.putString(fieldView.getKey(), fieldView.getText().toString());
		}
	}

	@Override
	protected boolean postSaveValidation() {
		try {
			for (FieldView fieldView : mFields) {
				FillupField field = new FillupField(new ContentValues());
				field.setFillupId(mFillup.getId());
				field.setTemplateId(fieldView.getFieldId());
				field.setValue(fieldView.getText().toString());
				field.save(this);
			}
			return true;
		} catch (InvalidFieldException exception) {
			Toast.makeText(this, getString(exception.getErrorMessage()), Toast.LENGTH_LONG).show();
		}
		return false;
	}

	@Override
	protected void saved() {
		startActivity(new Intent(this, FillupListActivity.class));
	}

	@Override
	protected Dao getDao() {
		return mFillup;
	}

	@Override
	protected String[] getProjectionArray() {
		return FillupsTable.getFullProjectionArray();
	}

	@Override
	protected Uri getUri(long id) {
		return ContentUris.withAppendedId(Uri.withAppendedPath(FillUpsProvider.BASE_URI, FillupsTable.FILLUP_URI), id);
	}

	@Override
	protected void initUI() {
		mOdometer = (EditText) findViewById(R.id.odometer);
		mVolume = (EditText) findViewById(R.id.volume);
		mPrice = (EditText) findViewById(R.id.price);
		mDate = (Button) findViewById(R.id.date);
		mPartial = (CheckBox) findViewById(R.id.partial);
		mFieldsContainer = (LinearLayout) findViewById(R.id.container);
		mVehicles = (Spinner) findViewById(R.id.vehicle);

		Uri uri = Uri.withAppendedPath(FillUpsProvider.BASE_URI, VehiclesTable.VEHICLES_URI);
		Cursor cursor = managedQuery(uri, VehiclesTable.getFullProjectionArray(), null, null, null);
		SpinnerCursorAdapter adapter = new SpinnerCursorAdapter(this, cursor, Vehicle.TITLE);
		mVehicles.setAdapter(adapter);

		if (cursor.getCount() == 1) {
			// mVehicles.setVisibility(View.GONE);
		}
	}

	@Override
	protected void populateUI() {
		// TODO
	}

	@Override
	protected void setFields() {
		// TODO: add error catching
		mFillup.setVolume(Double.parseDouble(mVolume.getText().toString()));
		mFillup.setPrice(Double.parseDouble(mPrice.getText().toString()));
		mFillup.setOdometer(Double.parseDouble(mOdometer.getText().toString()));
		mFillup.setPartial(mPartial.isChecked());
		mFillup.setVehicleId(mVehicles.getSelectedItemId());
	}
}
