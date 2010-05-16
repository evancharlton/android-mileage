package com.evancharlton.mileage.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Dao.DataObject;
import com.evancharlton.mileage.provider.tables.FieldsTable;

@DataObject(path = FieldsTable.URI)
public class Field extends Dao {
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String TYPE = "type"; // only text for now

	@Column(type = Column.STRING, name = TITLE)
	protected String mTitle = null;
	@Column(type = Column.STRING, name = DESCRIPTION)
	protected String mDescription = null;
	@Column(type = Column.STRING, name = TYPE)
	protected String mType = null; // TODO: Implement this in a future release.

	public Field(ContentValues values) {
		super(values);
	}

	public Field(Cursor cursor) {
		super(cursor);
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

	public String getType() {
		return mType;
	}
}
