package com.evancharlton.mileage.provider.tables;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.dao.Field;
import com.evancharlton.mileage.provider.FillUpsProvider;

public class FieldsTable extends ContentTable {
	// make sure it's globally unique
	private static final int FIELDS = 30;
	private static final int FIELD_ID = 31;

	private static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evancharlton.fields";
	private static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evancharlton.field_id";

	/**
	 * All saved field templates
	 */
	public static final String URI = "fields/";

	public static String[] getFullProjectionArray() {
		return new String[] {
				Dao._ID,
				Field.TITLE,
				Field.DESCRIPTION,
				Field.TYPE
		};
	}

	@Override
	public String getDefaultSortOrder() {
		return Field.TITLE + " asc";
	}

	@Override
	public String getTableName() {
		return "fields";
	}

	@Override
	public int delete(SQLiteDatabase db, Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(int type) {
		switch (type) {
			case FIELDS:
				return CONTENT_TYPE;
			case FIELD_ID:
				return CONTENT_ITEM_TYPE;
		}
		return null;
	}

	@Override
	public String init() {
		return new InsertBuilder().add(Field.TITLE, "Comment").add(Field.DESCRIPTION, "Comment about your fillup.").build();
	}

	@Override
	public long insert(int type, SQLiteDatabase db, ContentValues initialValues) {
		switch (type) {
			case FIELDS:
				return db.insert(getTableName(), null, initialValues);
		}
		return -1L;
	}

	@Override
	public boolean query(int type, Uri uri, SQLiteQueryBuilder queryBuilder) {
		switch (type) {
			case FIELDS:
				queryBuilder.setTables(getTableName());
				queryBuilder.setProjectionMap(buildProjectionMap(getFullProjectionArray()));
				return true;
			case FIELD_ID:
				queryBuilder.setTables(getTableName());
				queryBuilder.setProjectionMap(buildProjectionMap(getFullProjectionArray()));
				queryBuilder.appendWhere(Dao._ID + " = " + uri.getPathSegments().get(1));
				return true;
		}
		return false;
	}

	@Override
	public void registerUris(UriMatcher uriMatcher) {
		uriMatcher.addURI(FillUpsProvider.AUTHORITY, URI, FIELDS);
		uriMatcher.addURI(FillUpsProvider.AUTHORITY, URI + "#", FIELD_ID);
	}

	@Override
	public int update(int match, SQLiteDatabase db, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		switch (match) {
			case FIELD_ID:
				// TODO: use selection(Args)
				return db.update(getTableName(), values, Field._ID + " = ?", new String[] {
					values.getAsString(Field._ID)
				});
		}
		return -1;
	}

	@Override
	public String upgrade(int currentVersion) {
		return null;
	}

}
