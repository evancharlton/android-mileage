package com.evancharlton.mileage.dao;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.List;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.evancharlton.mileage.provider.FillUpsProvider;

/**
 * A base data access object (DAO). Exposes/provides the common functionality
 * such as persisting objects.
 * 
 * @author evan
 * 
 */
public abstract class Dao {
	private static final String TAG = "Dao";
	public static final String _ID = "_id";

	@Column(type = Column.LONG, name = _ID)
	private long mId;

	private Uri mUriBase = null;

	protected Dao(final ContentValues values) {
		Long id = values.getAsLong(_ID);
		if (id != null) {
			mId = id;
		}
	}

	public Dao(final Cursor cursor) {
		load(cursor);
	}

	public void load(Cursor cursor) {
		mId = cursor.getLong(cursor.getColumnIndex(_ID));

		// automagically populate based on @Column annotation definitions
		Field[] fields = getClass().getDeclaredFields();
		for (Field field : fields) {
			Annotation[] annotations = field.getAnnotations();
			for (Annotation annotation : annotations) {
				if (annotation instanceof Column) {
					Column column = (Column) annotation;
					int columnIndex = cursor.getColumnIndex(column.name());
					Object value = null;
					switch (column.type()) {
						case Column.BOOLEAN:
							value = cursor.getInt(columnIndex);
							if (value == null) {
								value = new Boolean(column.value() == 1);
							} else {
								value = ((Integer) value).intValue() == 1;
							}
							break;
						case Column.DOUBLE:
							value = cursor.getDouble(columnIndex);
							if (value == null) {
								value = new Double(column.value());
							}
							break;
						case Column.INTEGER:
							value = cursor.getInt(columnIndex);
							if (value == null) {
								value = new Integer(column.value());
							}
							break;
						case Column.LONG:
							value = cursor.getLong(columnIndex);
							if (value == null) {
								value = new Long(column.value());
							}
							break;
						case Column.STRING:
							value = cursor.getString(columnIndex);
							if (value == null) {
								value = "";
							}
							break;
						case Column.TIMESTAMP:
							// TODO: set Date?
							value = cursor.getLong(columnIndex);
							if (value == null) {
								value = System.currentTimeMillis();
							}
							break;
					}
					if (value != null) {
						try {
							field.set(this, value);
						} catch (IllegalArgumentException e) {
							Log.e(TAG, "Couldn't set value for " + field.getName(), e);
						} catch (IllegalAccessException e) {
							Log.e(TAG, "Couldn't access " + field.getName(), e);
						}
					}
				}
			}
		}
	}

	/**
	 * Get the URI for this instance of a DAO.
	 * 
	 * @return the URI for the DAO instance.
	 */
	public Uri getUri() {
		if (mUriBase == null) {
			DataObject annotation = getClass().getAnnotation(DataObject.class);
			mUriBase = Uri.withAppendedPath(FillUpsProvider.BASE_URI, annotation.path());
		}
		if (isExistingObject()) {
			return ContentUris.withAppendedId(mUriBase, getId());
		}
		return mUriBase;
	}

	/**
	 * Validate the data object (intended to be done before saving). If there is
	 * an invalid field value, throw an InvalidFieldException
	 * 
	 * @return the ContentValues to be passed to persistent storage.
	 */
	// TODO: use annotations for this?
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

	protected long getLong(ContentValues values, String key, long defaultValue) {
		Long value = values.getAsLong(key);
		if (value != null) {
			return value.longValue();
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

	// TODO: make this a series of annotations instead?
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Column {
		public static final int STRING = 0;
		public static final int INTEGER = 1;
		public static final int DOUBLE = 2;
		public static final int BOOLEAN = 3;
		public static final int TIMESTAMP = 4;
		public static final int LONG = 5;

		int value() default 0;

		int type();

		String name() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface DataObject {
		String path();
	}
}