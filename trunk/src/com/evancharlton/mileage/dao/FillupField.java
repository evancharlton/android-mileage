package com.evancharlton.mileage.dao;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.FillupsFieldsTable;

public class FillupField extends Dao {
	public static final String FILLUP_ID = "fillup_id";
	public static final String TEMPLATE_ID = "template_id";
	public static final String VALUE = "value";

	private long mTemplateId = 0L;
	private long mFillupId = 0L;
	private String mValue = null;

	public FillupField(ContentValues values) {
		super(values);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void load(Cursor cursor) {
		super.load(cursor);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Uri getUri() {
		Uri base = FillUpsProvider.BASE_URI;
		if (isExistingObject()) {
			base = Uri.withAppendedPath(base, FillupsFieldsTable.FILLUPS_FIELD_URI);
			base = ContentUris.withAppendedId(base, getId());
		} else {
			base = Uri.withAppendedPath(base, FillupsFieldsTable.FILLUPS_FIELDS_URI);
		}
		return base;
	}

	@Override
	protected void validate(ContentValues values) {
		if (mTemplateId <= 0) {
			throw new InvalidFieldException(R.string.error_invalid_template_id);
		}
		values.put(TEMPLATE_ID, mTemplateId);

		if (mFillupId <= 0) {
			throw new InvalidFieldException(R.string.error_invalid_fillup_id);
		}
		values.put(FILLUP_ID, mFillupId);

		values.put(VALUE, mValue);
	}

	public Fillup getFillup(Context context) {
		return null;
	}

	public String getValue() {
		return mValue;
	}

	public Field getFieldTemplate(Context context) {
		return null;
	}

	public void setFillupId(long id) {
		mFillupId = id;
	}

	public void setTemplateId(long id) {
		mTemplateId = id;
	}

	public void setValue(String value) {
		mValue = value;
	}
}
