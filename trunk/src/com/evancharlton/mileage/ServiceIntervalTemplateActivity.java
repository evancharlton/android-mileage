package com.evancharlton.mileage;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;

import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.dao.ServiceIntervalTemplate;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.ServiceIntervalTemplatesTable;
import com.evancharlton.mileage.views.CursorSpinner;
import com.evancharlton.mileage.views.DateDelta;
import com.evancharlton.mileage.views.DistanceDelta;

public class ServiceIntervalTemplateActivity extends BaseFormActivity {

	protected final ServiceIntervalTemplate mTemplate = new ServiceIntervalTemplate(new ContentValues());
	protected EditText mTitle;
	protected EditText mDescription;
	protected DistanceDelta mDistance;
	protected DateDelta mDuration;
	protected CursorSpinner mVehicleTypes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.service_interval_template);
	}

	@Override
	protected Dao getDao() {
		return mTemplate;
	}

	@Override
	protected String[] getProjectionArray() {
		return ServiceIntervalTemplatesTable.getFullProjectionArray();
	}

	@Override
	protected Uri getUri(long id) {
		return ContentUris.withAppendedId(Uri.withAppendedPath(FillUpsProvider.BASE_URI, ServiceIntervalTemplatesTable.URI), id);
	}

	@Override
	protected void initUI() {
		mTitle = (EditText) findViewById(R.id.title);
		mDescription = (EditText) findViewById(R.id.description);
		mDistance = (DistanceDelta) findViewById(R.id.distance);
		mDuration = (DateDelta) findViewById(R.id.duration);
		mVehicleTypes = (CursorSpinner) findViewById(R.id.types);
	}

	@Override
	protected void populateUI() {
		mTitle.setText(mTemplate.getTitle());
		mDescription.setText(mTemplate.getDescription());
		mDistance.setDelta(mTemplate.getDistance());
		mDuration.setDelta(mTemplate.getDuration());
	}

	@Override
	protected void setFields() {
		// TODO: Error checking
		mTemplate.setTitle(mTitle.getText().toString());
		mTemplate.setDescription(mDescription.getText().toString());
		mTemplate.setDistance(mDistance.getDelta());
		mTemplate.setDuration(mDuration.getDelta());
		mTemplate.setVehicleTypeId(mVehicleTypes.getSelectedItemId());
	}

	@Override
	protected int getCreateString() {
		return R.string.add_service_interval_template;
	}
}
