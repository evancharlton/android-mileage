package com.evancharlton.mileage.dao;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.FieldsTable;

public class Field extends Dao {
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	// for now, it's all text
	public static final String TYPE = "type";

	private String mTitle = null;
	private String mDescription = null;
	private String mType = null;

	public Field(ContentValues values) {
		super(values);
	}

	@Override
	public void load(Cursor cursor) {
		super.load(cursor);
		mTitle = getString(cursor, TITLE);
		mDescription = getString(cursor, DESCRIPTION);
	}

	@Override
	protected void validate(ContentValues values) {
		if (mTitle == null || mTitle.length() == 0) {
			throw new InvalidFieldException(R.string.error_invalid_field_title);
		} else {
			values.put(TITLE, mTitle);
		}

		if (mDescription == null) {
			throw new InvalidFieldException(R.string.error_invalid_field_description);
		} else {
			values.put(DESCRIPTION, mDescription);
		}

		// TODO
		values.put(TYPE, "string");
	}

	@Override
	protected Uri getUri() {
		Uri base = FillUpsProvider.BASE_URI;
		if (isExistingObject()) {
			base = Uri.withAppendedPath(base, FieldsTable.FIELD_URI);
			base = ContentUris.withAppendedId(base, getId());
		} else {
			base = Uri.withAppendedPath(base, FieldsTable.FIELDS_URI);
		}
		return base;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public void setDescription(String description) {
		mDescription = description;
	}

	public void setType(String type) {
		mType = type;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getDescription() {
		return mDescription;
	}
}
