package com.evancharlton.mileage.dao;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.FillupsTable;

public abstract class Dao {
	private Context mContext;
	public static final String _ID = "_id";

	public static Uri createUri(String path, long id) {
		return ContentUris.withAppendedId(createUri(path), id);
	}

	public static Uri createUri(String path) {
		return Uri.withAppendedPath(Uri.parse("content://" + FillUpsProvider.AUTHORITY), path);
	}

	public static final Dao create(Context context, ContentValues values) {
		return null;
	}

	public static final Dao load(Class<? extends Dao> type, Context context, long id) {
		Cursor c = context.getContentResolver().query(createUri("fillups/", id), FillupsTable.getFullProjectionArray(), null, null, null);
		if (c.getCount() == 1) {
			c.moveToFirst();
			try {
				Constructor<? extends Dao> constructor = type.getConstructor(Cursor.class);
				Dao instance = constructor.newInstance(c);
				instance.setContext(context);
				return instance;
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		throw new IllegalArgumentException("Unknown fillup ID: " + id);
	}

	protected void setContext(Context context) {
		mContext = context;
	}

	protected Context getContext() {
		return mContext;
	}

	protected long getLong(Cursor cursor, String columnName) {
		return cursor.getLong(cursor.getColumnIndex(columnName));
	}

	protected double getDouble(Cursor cursor, String columnName) {
		return cursor.getDouble(cursor.getColumnIndex(columnName));
	}
}
