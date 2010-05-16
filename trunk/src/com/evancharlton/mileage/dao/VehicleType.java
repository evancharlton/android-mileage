package com.evancharlton.mileage.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Dao.DataObject;
import com.evancharlton.mileage.provider.tables.VehicleTypesTable;

@DataObject(path = VehicleTypesTable.URI)
public class VehicleType extends Dao {
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";

	@Column(type = Column.STRING, name = TITLE)
	protected String mTitle;
	@Column(type = Column.STRING, name = DESCRIPTION)
	protected String mDescription;

	public VehicleType(ContentValues values) {
		super(values);
	}

	public VehicleType(Cursor cursor) {
		super(cursor);
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
