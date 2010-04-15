package com.evancharlton.mileage.dao;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.VehicleTypesTable;

public class VehicleType extends Dao {
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";

	private String mTitle = null;
	private String mDescription = null;

	public VehicleType(ContentValues values) {
		super(values);
	}

	@Override
	public void load(Cursor cursor) {
		super.load(cursor);
		mTitle = getString(cursor, TITLE);
		mDescription = getString(cursor, DESCRIPTION);
	}

	@Override
	protected Uri getUri() {
		Uri base = FillUpsProvider.BASE_URI;
		if (isExistingObject()) {
			base = Uri.withAppendedPath(base, VehicleTypesTable.TYPE_URI);
			base = ContentUris.withAppendedId(base, getId());
		} else {
			base = Uri.withAppendedPath(base, VehicleTypesTable.TYPES_URI);
		}
		return base;
	}

	@Override
	protected void validate(ContentValues values) {
		if (mTitle == null || mTitle.length() == 0) {
			throw new InvalidFieldException(R.string.error_invalid_vehicle_type_title);
		}
		values.put(TITLE, mTitle);

		if (mDescription == null) {
			throw new InvalidFieldException(R.string.error_invalid_vehicle_type_description);
		}
		values.put(DESCRIPTION, mDescription);
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public void setDescription(String description) {
		mDescription = description;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getDescription() {
		return mDescription;
	}
}
