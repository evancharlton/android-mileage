package com.evancharlton.mileage;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;

import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.VehiclesTable;
import com.evancharlton.mileage.views.CursorSpinner;

public class VehicleActivity extends BaseFormActivity {
	private EditText mTitle;
	private EditText mDescription;
	private EditText mMake;
	private EditText mModel;
	private EditText mYear;
	private CheckBox mSetDefault;
	private CursorSpinner mVehicleTypes;
	private Vehicle mVehicle = new Vehicle(new ContentValues());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.vehicle);
	}

	@Override
	protected Dao getDao() {
		return mVehicle;
	}

	@Override
	protected String[] getProjectionArray() {
		return VehiclesTable.getFullProjectionArray();
	}

	@Override
	protected Uri getUri(long id) {
		return ContentUris.withAppendedId(Uri.withAppendedPath(FillUpsProvider.BASE_URI, VehiclesTable.VEHICLE_URI), id);
	}

	@Override
	protected void initUI() {
		mTitle = (EditText) findViewById(R.id.title);
		mDescription = (EditText) findViewById(R.id.description);
		mMake = (EditText) findViewById(R.id.make);
		mModel = (EditText) findViewById(R.id.model);
		mYear = (EditText) findViewById(R.id.year);
		mVehicleTypes = (CursorSpinner) findViewById(R.id.type);
	}

	@Override
	protected void populateUI() {
		mTitle.setText(mVehicle.getTitle());
		mDescription.setText(mVehicle.getDescription());
		mMake.setText(mVehicle.getMake());
		mModel.setText(mVehicle.getModel());
		mYear.setText(mVehicle.getYear());
	}

	@Override
	protected void setFields() {
		mVehicle.setTitle(mTitle.getText().toString());
		mVehicle.setDescription(mDescription.getText().toString());
		mVehicle.setMake(mMake.getText().toString());
		mVehicle.setModel(mModel.getText().toString());
		mVehicle.setYear(mYear.getText().toString());
		mVehicle.setVehicleType(mVehicleTypes.getSelectedItemId());
		if (mSetDefault.isChecked()) {
			mVehicle.setDefaultTime(System.currentTimeMillis());
		}
	}

	@Override
	protected int getCreateString() {
		return R.string.add_vehicle;
	}
}
