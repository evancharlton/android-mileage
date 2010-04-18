package com.evancharlton.mileage;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.dao.ServiceInterval;
import com.evancharlton.mileage.dao.ServiceIntervalTemplate;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.ServiceIntervalsTable;
import com.evancharlton.mileage.views.CursorSpinner;
import com.evancharlton.mileage.views.DateButton;
import com.evancharlton.mileage.views.DateDelta;
import com.evancharlton.mileage.views.DistanceDelta;

public class ServiceIntervalActivity extends BaseFormActivity {

	private final ServiceInterval mInterval = new ServiceInterval(new ContentValues());
	private CursorSpinner mVehicles;
	private CursorSpinner mIntervalTemplates;
	private EditText mTitle;
	private EditText mDescription;
	private DistanceDelta mDistance;
	private DateDelta mDuration;
	private EditText mOdometer;
	private DateButton mDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.service_interval);
	}

	@Override
	protected Dao getDao() {
		return mInterval;
	}

	@Override
	protected String[] getProjectionArray() {
		return ServiceIntervalsTable.getFullProjectionArray();
	}

	@Override
	protected Uri getUri(long id) {
		return ContentUris.withAppendedId(Uri.withAppendedPath(FillUpsProvider.BASE_URI, ServiceIntervalsTable.SERVICE_INTERVAL_URI), id);
	}

	@Override
	protected void initUI() {
		mVehicles = (CursorSpinner) findViewById(R.id.vehicles);
		mIntervalTemplates = (CursorSpinner) findViewById(R.id.types);
		mTitle = (EditText) findViewById(R.id.title);
		mDescription = (EditText) findViewById(R.id.description);
		mDistance = (DistanceDelta) findViewById(R.id.distance);
		mDuration = (DateDelta) findViewById(R.id.duration);
		mOdometer = (EditText) findViewById(R.id.odometer);
		mDate = (DateButton) findViewById(R.id.date);

		mVehicles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> list, View row, int position, long id) {
				filterTemplates(id);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	private void filterTemplates(long id) {
		StringBuilder selection = new StringBuilder();
		selection.append(ServiceIntervalTemplate.VEHICLE_TYPE).append(" = ( select ").append(Vehicle.VEHICLE_TYPE).append(" from vehicles where ")
				.append(Vehicle._ID).append(" =  ?)");
		String[] selectionArgs = new String[] {
			String.valueOf(id)
		};
		mIntervalTemplates.filter(selection.toString(), selectionArgs);
	}

	@Override
	protected void populateUI() {
		mTitle.setText(mInterval.getTitle());
		mDescription.setText(mInterval.getDescription());
		mDistance.setDelta((long) mInterval.getDistance());
		mDuration.setDelta(mInterval.getDuration());
		mOdometer.setText(String.valueOf(mInterval.getStartOdometer()));
		mDate.setDate(mInterval.getStartDate());
	}

	@Override
	protected void setFields() {
		// TODO: Error checking
		mInterval.setTitle(mTitle.getText().toString());
		mInterval.setDescription(mDescription.getText().toString());
		mInterval.setDuration(mDuration.getDelta());
		mInterval.setStartOdometer(Double.parseDouble(mOdometer.getText().toString()));
		mInterval.setStartDate(mDate.getTimestamp());
		mInterval.setDistance(mDistance.getDelta());
		mInterval.setVehicleId(mVehicles.getSelectedItemId());
	}

	@Override
	protected int getCreateString() {
		return R.string.add_service_interval;
	}
}
