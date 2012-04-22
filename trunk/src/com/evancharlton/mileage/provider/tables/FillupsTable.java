
package com.evancharlton.mileage.provider.tables;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.provider.FillUpsProvider;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class FillupsTable extends ContentTable {
    // make sure it's globally unique
    private static final int FILLUPS = 10;

    private static final int FILLUP_ID = 11;

    private static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evancharlton.fillup";

    private static final String CONTENT_ITEM_TYPE =
            "vnd.android.cursor.item/vnd.evancharlton.fillup";

    public static final String TABLE_NAME = "fillups";

    public static final String URI = "fillups/";

    public static final Uri BASE_URI = Uri.withAppendedPath(FillUpsProvider.BASE_URI, URI);

    public static final String[] PROJECTION = new String[] {
            Fillup._ID, Fillup.TOTAL_COST, Fillup.UNIT_PRICE, Fillup.VOLUME, Fillup.ODOMETER,
            Fillup.ECONOMY, Fillup.VEHICLE_ID, Fillup.DATE, Fillup.LATITUDE, Fillup.LONGITUDE,
            Fillup.PARTIAL, Fillup.RESTART
    };

    public static final String[] CSV_PROJECTION = new String[] {
            Fillup._ID, Fillup.TOTAL_COST, Fillup.UNIT_PRICE, Fillup.VOLUME, Fillup.ODOMETER,
            Fillup.ECONOMY, Fillup.VEHICLE_ID, Fillup.DATE, Fillup.LATITUDE, Fillup.LONGITUDE,
            Fillup.PARTIAL, Fillup.RESTART
    };

    public static final int[] PLAINTEXT = new int[] {
            R.string.column_id, R.string.column_total_cost, R.string.column_unit_price,
            R.string.column_volume, R.string.column_odometer, R.string.column_economy,
            R.string.column_vehicle, R.string.column_date, R.string.column_latitude,
            R.string.column_longitude, R.string.column_partial, R.string.column_restart
    };

    @Override
    protected Class<? extends Dao> getDaoType() {
        return Fillup.class;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public void registerUris() {
        FillUpsProvider.registerUri(this, URI, FILLUPS);
        FillUpsProvider.registerUri(this, URI + "#", FILLUP_ID);
    }

    @Override
    public String getType(final int type) {
        switch (type) {
            case FILLUPS:
                return CONTENT_TYPE;
            case FILLUP_ID:
                return CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Override
    public long insert(int type, SQLiteDatabase db, ContentValues initialValues) {
        switch (type) {
            case FILLUPS:
                return db.insert(getTableName(), null, initialValues);
        }
        return -1L;
    }

    @Override
    public boolean query(final int type, Uri uri, SQLiteQueryBuilder queryBuilder, Context context,
            String[] projection) {
        switch (type) {
            case FILLUP_ID:
                queryBuilder.appendWhere(TABLE_NAME + "." + Fillup._ID + " = "
                        + uri.getPathSegments().get(1));
            case FILLUPS:
                queryBuilder.setTables(getTableName());
                queryBuilder.setProjectionMap(buildProjectionMap(PROJECTION));
                return true;
        }
        return false;
    }

    @Override
    public int update(int match, SQLiteDatabase db, Uri uri, ContentValues values,
            String selection, String[] selectionArgs) {
        switch (match) {
            case FILLUPS:
                return db.update(getTableName(), values, selection, selectionArgs);
            case FILLUP_ID:
                String fillUpId = uri.getPathSegments().get(1);
                String clause =
                        Fillup._ID + " = " + fillUpId
                                + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
                return db.update(getTableName(), values, clause, selectionArgs);
        }
        return -1;
    }

    @Override
    public String[] init(boolean isUpgrade) {
        return null;
    }

    @Override
    public String getDefaultSortOrder() {
        return Fillup.ODOMETER + " desc";
    }

    @Override
    public String[] getProjection() {
        return PROJECTION;
    }
}
