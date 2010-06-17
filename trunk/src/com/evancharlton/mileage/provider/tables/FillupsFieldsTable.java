package com.evancharlton.mileage.provider.tables;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.dao.FillupField;
import com.evancharlton.mileage.provider.FillUpsProvider;

public class FillupsFieldsTable extends ContentTable {
	// make sure it's globally unique
	// TODO: rename these because it's confusing as fuck
	private static final int FILLUP_FIELDS = 20;
	private static final int FILLUP_FIELD = 21;
	private static final int FILLUPS_FIELDS = 22;

	/**
	 * Given a fillup ID, return all of the fields that were saved on that
	 * fillup
	 */
	public static final String FILLUPS_FIELDS_PATH = "fillups/fields";
	public static final Uri FILLUPS_FIELDS_URI = Uri.withAppendedPath(FillUpsProvider.BASE_URI, FILLUPS_FIELDS_PATH);

	/**
	 * Given a field ID, return the field
	 */
	public static final String FILLUPS_FIELD_PATH = "fillups/field";

	private static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evancharlton.fillup_fields";
	private static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evancharlton.fillup_field_id";
	private static final String CONTENT_ITEMS_TYPE = "vnd.android.cursor.dir/vnd.evancharlton.fillups_fields";

	public static final String[] getFullProjectionArray() {
		return new String[] {
				BaseColumns._ID,
				FillupField.FILLUP_ID,
				FillupField.TEMPLATE_ID,
				FillupField.VALUE
		};
	}

	@Override
	protected Class<? extends Dao> getDaoType() {
		return FillupField.class;
	}

	@Override
	public String getTableName() {
		return "fillups_fields";
	}

	@Override
	public String getDefaultSortOrder() {
		return FillupField.TEMPLATE_ID + " desc";
	}

	@Override
	public String getType(int type) {
		switch (type) {
			case FILLUP_FIELD:
				return CONTENT_ITEM_TYPE;
			case FILLUP_FIELDS:
				return CONTENT_TYPE;
			case FILLUPS_FIELDS:
				return CONTENT_ITEMS_TYPE;
		}
		return null;
	}

	@Override
	public String init() {
		return null;
	}

	@Override
	public long insert(int type, SQLiteDatabase db, ContentValues initialValues) {
		switch (type) {
			case FILLUP_FIELDS:
			case FILLUPS_FIELDS:
				return db.insert(getTableName(), null, initialValues);
		}
		return -1L;
	}

	@Override
	public boolean query(int type, Uri uri, SQLiteQueryBuilder queryBuilder) {
		switch (type) {
			case FILLUPS_FIELDS:
				queryBuilder.setTables(getTableName());
				queryBuilder.setProjectionMap(buildProjectionMap(getFullProjectionArray()));
				return true;
			case FILLUP_FIELDS:
				queryBuilder.setTables(getTableName());
				queryBuilder.setProjectionMap(buildProjectionMap(getFullProjectionArray()));
				queryBuilder.appendWhere(FillupField.FILLUP_ID + " = " + uri.getPathSegments().get(2));
				return true;
			case FILLUP_FIELD:
				queryBuilder.setTables(getTableName());
				queryBuilder.setProjectionMap(buildProjectionMap(getFullProjectionArray()));
				queryBuilder.appendWhere(FillupField._ID + " = " + uri.getPathSegments().get(2));
				return true;
		}
		return false;
	}

	@Override
	public void registerUris(UriMatcher uriMatcher) {
		uriMatcher.addURI(FillUpsProvider.AUTHORITY, FILLUPS_FIELDS_PATH, FILLUPS_FIELDS);
		uriMatcher.addURI(FillUpsProvider.AUTHORITY, FILLUPS_FIELDS_PATH + "/#", FILLUP_FIELDS);
		uriMatcher.addURI(FillUpsProvider.AUTHORITY, FILLUPS_FIELD_PATH + "/#", FILLUP_FIELD);
	}

	@Override
	public int update(int match, SQLiteDatabase db, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		switch (match) {
			case FILLUP_FIELD:
				return db.update(getTableName(), values, FillupField._ID + " = ?", new String[] {
					values.getAsString(FillupField._ID)
				});
			case FILLUPS_FIELDS:
				if (values.containsKey(BaseColumns._ID)) {
					values.remove(BaseColumns._ID);
				}
				return db.update(getTableName(), values, null, null);
		}
		return -1;
	}

	@Override
	public String upgrade(int currentVersion) {
		// TODO Auto-generated method stub
		return null;
	}

}
