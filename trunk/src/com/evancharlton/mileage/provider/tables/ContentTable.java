package com.evancharlton.mileage.provider.tables;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public abstract class ContentTable {
	protected static String TABLE_NAME = "content_table";
	protected static String DEFAULT_SORT_ORDER = "_id desc";
	protected static final HashMap<String, String> PROJECTION_MAP = new HashMap<String, String>();

	public final String getTableName() {
		return TABLE_NAME;
	}

	public final HashMap<String, String> getFullProjectionMap() {
		return PROJECTION_MAP;
	}

	public final String getDefaultSortOrder() {
		return DEFAULT_SORT_ORDER;
	}

	abstract public void registerUris(String authority, UriMatcher uriMatcher);

	abstract public int delete(SQLiteDatabase db, Uri uri, String selection, String[] selectionArgs);

	abstract public String getType(int type);

	abstract public Uri insert(int type, Uri uri, ContentValues initialValues);

	abstract public boolean query(int type, Uri uri, SQLiteQueryBuilder queryBuilder);

	abstract public int update(int match, SQLiteDatabase db, Uri uri, ContentValues values, String selection, String[] selectionArgs);

	abstract public String init();

	abstract public String create();

	abstract public String upgrade(final int currentVersion);
}
