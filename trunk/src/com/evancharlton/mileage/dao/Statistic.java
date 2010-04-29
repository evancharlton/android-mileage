package com.evancharlton.mileage.dao;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.provider.tables.CacheTable;

public class Statistic extends Dao {
	public static final String ITEM = "item";
	public static final String KEY = "key";
	public static final String VALUE = "value";
	public static final String VALID = "is_valid";

	private String mItem = null;
	private String mKey = null;
	private double mValue = 0D;
	private boolean mIsValid = false;

	public Statistic(ContentValues values) {
		super(values);

		mItem = getString(values, ITEM, null);
		mKey = getString(values, KEY, null);
		mValue = getDouble(values, VALUE, 0D);
		mIsValid = getBoolean(values, VALID, false);
	}

	public Statistic(Cursor cursor) {
		super(cursor);

		mItem = getString(cursor, ITEM);
		mKey = getString(cursor, KEY);
		mValue = getDouble(cursor, VALUE);
		mIsValid = getBoolean(cursor, VALID);
	}

	@Override
	protected Uri getUri() {
		Uri base = CacheTable.BASE_URI;
		if (isExistingObject()) {
			return ContentUris.withAppendedId(base, getId());
		}
		return base;
	}

	@Override
	protected void validate(ContentValues values) {
		if (mItem == null) {
			throw new InvalidFieldException(R.string.error_invalid_statistic_item);
		}
		values.put(ITEM, mItem);

		if (mKey == null) {
			throw new InvalidFieldException(R.string.error_invalid_statistic_key);
		}
		values.put(KEY, mKey);

		values.put(VALUE, mValue);
		values.put(VALID, mIsValid);
	}
}
