package com.evancharlton.mileage.dao;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * A base data access object (DAO). Exposes/provides the common functionality
 * such as persisting objects.
 * 
 * @author evan
 * 
 */
public abstract class Dao {
	/**
	 * Unique record ID
	 */
	public static final String _ID = "_id";

	private long mId = 0L;

	protected Dao(final ContentValues values) {
		Long id = values.getAsLong(_ID);
		if (id != null) {
			mId = id;
		}
	}

	protected Dao(final Cursor cursor) {
		load(cursor);
	}

	public void load(Cursor cursor) {
		mId = cursor.getLong(cursor.getColumnIndex(_ID));
	}

	/**
	 * Get the URI for this instance of a DAO.
	 * 
	 * @return the URI for the DAO instance.
	 */
	abstract protected Uri getUri();

	/**
	 * Validate the data object (intended to be done before saving). If there is
	 * an invalid field value, throw an InvalidFieldException
	 * 
	 * @return the ContentValues to be passed to persistent storage.
	 */
	abstract protected void validate(ContentValues values);

	public final boolean save(Context context) {
		ContentValues values = new ContentValues();
		validate(values);
		if (isExistingObject()) {
			// update
			values.put(_ID, mId);
			context.getContentResolver().update(getUri(), values, null, null);
		} else {
			// insert
			Uri uri = context.getContentResolver().insert(getUri(), values);
			List<String> segments = uri.getPathSegments();
			String id = segments.get(segments.size() - 1);
			mId = Long.parseLong(id);
		}
		return true;
	}

	public final boolean isExistingObject() {
		return mId > 0;
	}

	public final long getId() {
		return mId;
	}

	public final void setId(long id) {
		mId = id;
	}

	protected long getLong(Cursor cursor, String columnName) {
		return cursor.getLong(cursor.getColumnIndex(columnName));
	}

	protected double getDouble(Cursor cursor, String columnName) {
		return cursor.getDouble(cursor.getColumnIndex(columnName));
	}

	protected String getString(Cursor cursor, String columnName) {
		return cursor.getString(cursor.getColumnIndex(columnName));
	}

	protected boolean getBoolean(Cursor cursor, String columnName) {
		return cursor.getInt(cursor.getColumnIndex(columnName)) == 1;
	}

	protected int getInt(Cursor cursor, String columnName) {
		return cursor.getInt(cursor.getColumnIndex(columnName));
	}

	protected int getInt(ContentValues values, String key, int defaultValue) {
		Integer value = values.getAsInteger(key);
		if (value != null) {
			return value.intValue();
		}
		return defaultValue;
	}

	protected String getString(ContentValues values, String key, String defaultValue) {
		String value = values.getAsString(key);
		if (value != null) {
			return value;
		}
		return defaultValue;
	}

	protected double getDouble(ContentValues values, String key, double defaultValue) {
		Double value = values.getAsDouble(key);
		if (value != null) {
			return value.doubleValue();
		}
		return defaultValue;
	}

	protected boolean getBoolean(ContentValues values, String key, boolean defaultValue) {
		Boolean value = values.getAsBoolean(key);
		if (value != null) {
			return value.booleanValue();
		}
		return defaultValue;
	}

	// TODO: break this out into its own class. Make it checked?
	public static class InvalidFieldException extends RuntimeException {
		private static final long serialVersionUID = 3415877365632636406L;

		private int mErrorMessage = 0;

		public InvalidFieldException(int errorMessage) {
			mErrorMessage = errorMessage;
		}

		public int getErrorMessage() {
			return mErrorMessage;
		}
	}
}
