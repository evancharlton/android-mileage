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

	@Validate(R.string.error_invalid_template_id)
	@Range.Positive
	@Column(type = Column.LONG, name = TEMPLATE_ID)
	protected long mTemplateId;

	@Validate(R.string.error_invalid_fillup_id)
	@Range.Positive
	@Column(type = Column.LONG, name = FILLUP_ID)
	protected long mFillupId;

	@Validate
	@Column(type = Column.STRING, name = VALUE)
	protected String mValue;

	public FillupField(ContentValues values) {
		super(values);
	}

	public FillupField(Cursor cursor) {
		super(cursor);
	}

	@Override
	public Uri getUri() {
		Uri base = FillUpsProvider.BASE_URI;
		if (isExistingObject()) {
			base = Uri.withAppendedPath(base, FillupsFieldsTable.FILLUPS_FIELD_PATH);
			base = ContentUris.withAppendedId(base, getId());
		} else {
			base = Uri.withAppendedPath(base, FillupsFieldsTable.FILLUPS_FIELDS_PATH);
		}
		return base;
	}

	@Override
	public boolean save(Context context) {
		ContentValues values = new ContentValues();
		validate(values);
		String selection = FillupField.FILLUP_ID + " = ? AND " + FillupField.TEMPLATE_ID + " = ?";
		String[] selectionArgs = new String[] {
				String.valueOf(mFillupId),
				String.valueOf(mTemplateId)
		};
		Cursor c = context.getContentResolver().query(FillupsFieldsTable.FILLUPS_FIELDS_URI, FillupsFieldsTable.getFullProjectionArray(), selection,
				selectionArgs, null);
		long id = 0;
		if (c.getCount() > 0) {
			c.moveToFirst();
			id = c.getLong(c.getColumnIndex(FillupField._ID));
		}
		c.close();
		if (id != 0 || isExistingObject()) {
			// update
			values.put(_ID, id);
			context.getContentResolver().update(getUri(), values, null, null);
			return true;
		} else {
			return super.save(context);
		}
	}

	public Fillup getFillup(Context context) {
		return null;
	}

	public String getValue() {
		return mValue;
	}

	public Field getFieldTemplate(Context context) {
		// TODO
		return null;
	}

	public long getTemplateId() {
		return mTemplateId;
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
