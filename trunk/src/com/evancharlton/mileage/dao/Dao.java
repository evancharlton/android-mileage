
package com.evancharlton.mileage.dao;

import com.evancharlton.mileage.exceptions.InvalidFieldException;
import com.evancharlton.mileage.provider.FillUpsProvider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

/**
 * A base data access object (DAO). Exposes/provides the common functionality
 * such as persisting objects.
 */
public abstract class Dao implements Cloneable {
    private static final String TAG = "Dao";

    public static final String _ID = BaseColumns._ID;

    @Column(type = Column.LONG, name = BaseColumns._ID)
    private long mId;

    private Uri mUriBase = null;

    private boolean mInMemoryDataChanged = false;

    protected Dao(final ContentValues values) {
        load(values);
    }

    public Dao(final Cursor cursor) {
        load(cursor);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void load(Cursor cursor) {
        if (cursor.isBeforeFirst()) {
            cursor.moveToFirst();
        }
        mId = cursor.getLong(cursor.getColumnIndex(_ID));

        // automagically populate based on @Column annotation definitions
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
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
                        Long ms = cursor.getLong(columnIndex);
                        if (ms != null) {
                            value = new Date(ms);
                        } else {
                            value = new Date(System.currentTimeMillis());
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

    // TODO(3.1) - Remove this code duplication.
    public void load(ContentValues values) {
        if (values == null) {
            mId = -1;
            return;
        }
        Long id = values.getAsLong(_ID);
        if (id == null) {
            mId = -1;
        } else {
            mId = id.longValue();
        }

        // automagically populate based on @Column annotation definitions
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                Object value = null;
                switch (column.type()) {
                    case Column.BOOLEAN:
                        value = values.getAsBoolean(column.name());
                        if (value == null) {
                            value = new Boolean(column.value() == 1);
                        }
                        break;
                    case Column.DOUBLE:
                        value = values.getAsDouble(column.name());
                        if (value == null) {
                            value = new Double(column.value());
                        }
                        break;
                    case Column.INTEGER:
                        value = values.getAsInteger(column.name());
                        if (value == null) {
                            value = new Integer(column.value());
                        }
                        break;
                    case Column.LONG:
                        value = values.getAsLong(column.name());
                        if (value == null) {
                            value = new Long(column.value());
                        }
                        break;
                    case Column.STRING:
                        value = values.getAsString(column.name());
                        if (value == null) {
                            value = "";
                        }
                        break;
                    case Column.TIMESTAMP:
                        Long ms = values.getAsLong(column.name());
                        if (ms != null) {
                            value = new Date(ms);
                        } else {
                            value = new Date(System.currentTimeMillis());
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
     * @throws InvalidFieldException in the event of a validation error
     */
    protected final void validate(ContentValues values) throws InvalidFieldException {
        preValidate();
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            Validate validate = field.getAnnotation(Validate.class);
            if (validate == null) {
                continue;
            }
            int errorMessage = validate.value();
            try {
                Object value = field.get(this);
                if (validate != null) {
                    if (errorMessage > 0) {
                        // see if it's null when it shouldn't be
                        if (value == null && field.getAnnotation(Nullable.class) != null) {
                            throw new InvalidFieldException(errorMessage);
                        }

                        // check strings
                        if (value instanceof String
                                && field.getAnnotation(CanBeEmpty.class) == null) {
                            if (((String) value).length() == 0) {
                                throw new InvalidFieldException(errorMessage);
                            }
                        }

                        // check the numeric types
                        if (value instanceof Number) {
                            boolean checkPast = field.getAnnotation(Past.class) != null;
                            boolean checkPositive =
                                    field.getAnnotation(Range.Positive.class) != null;
                            if (value instanceof Double) {
                                if (checkPositive && ((Double) value) <= 0D) {
                                    throw new InvalidFieldException(errorMessage);
                                }
                            }

                            if (value instanceof Long) {
                                if (checkPositive && ((Long) value) <= 0L) {
                                    throw new InvalidFieldException(errorMessage);
                                }
                                if (checkPast && ((Long) value) >= System.currentTimeMillis()) {
                                    throw new InvalidFieldException(errorMessage);
                                }
                            }

                            if (value instanceof Integer) {
                                if (checkPositive && ((Integer) value) <= 0) {
                                    throw new InvalidFieldException(errorMessage);
                                }
                            }
                        }
                    }

                    // we made it without any errors (or no validation needed)
                    Column column = field.getAnnotation(Column.class);
                    if (column != null) {
                        String data = null;
                        if (value instanceof Date) {
                            data = String.valueOf(((Date) value).getTime());
                        } else if (value instanceof Boolean) {
                            data = ((Boolean) value).booleanValue() ? "1" : "0";
                        } else {
                            data = String.valueOf(value);
                        }
                        values.put(column.name(), data);
                    }
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG, e.getMessage(), e);
            } catch (IllegalAccessException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    protected void preValidate() {
    }

    public boolean save(Context context) throws InvalidFieldException {
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

    public boolean saveIfChanged(Context context) throws InvalidFieldException {
        if (mInMemoryDataChanged) {
            return save(context);
        }
        return false;
    }

    public boolean delete(Context context) {
        return context.getContentResolver().delete(getUri(), null, null) > 0;
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

    protected void setInMemoryDataChanged() {
        mInMemoryDataChanged = true;
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

        String name();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface DataObject {
        String path();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Validate {
        int value() default 0;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Nullable {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface CanBeEmpty {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Past {
    }

    public static class Range {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.FIELD)
        public @interface Positive {
        }
    }
}
