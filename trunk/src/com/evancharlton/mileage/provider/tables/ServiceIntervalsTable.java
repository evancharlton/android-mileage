
package com.evancharlton.mileage.provider.tables;

import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.dao.ServiceInterval;
import com.evancharlton.mileage.provider.FillUpsProvider;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class ServiceIntervalsTable extends ContentTable {
    private static final int SERVICE_INTERVALS = 70;

    private static final int SERVICE_INTERVAL_ID = 71;

    public static final String TABLE_NAME = "service_intervals";

    public static final String URI = "intervals/";

    public static final Uri BASE_URI = Uri.withAppendedPath(FillUpsProvider.BASE_URI, URI);

    private static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evancharlton.interval";

    private static final String CONTENT_ITEM_TYPE =
            "vnd.android.cursor.item/vnd.evancharlton.interval";

    public static String[] PROJECTION = new String[] {
            ServiceInterval._ID, ServiceInterval.TITLE, ServiceInterval.DESCRIPTION,
            ServiceInterval.START_DATE, ServiceInterval.START_ODOMETER,
            ServiceInterval.TEMPLATE_ID, ServiceInterval.VEHICLE_ID, ServiceInterval.DURATION,
            ServiceInterval.DISTANCE
    };

    @Override
    protected Class<? extends Dao> getDaoType() {
        return ServiceInterval.class;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getType(int type) {
        switch (type) {
            case SERVICE_INTERVALS:
                return CONTENT_TYPE;
            case SERVICE_INTERVAL_ID:
                return CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Override
    public String[] init(boolean isUpgrade) {
        return null;
    }

    @Override
    public long insert(int type, SQLiteDatabase db, ContentValues initialValues) {
        switch (type) {
            case SERVICE_INTERVALS:
                return db.insert(getTableName(), null, initialValues);
        }
        return -1L;
    }

    @Override
    public boolean query(int type, Uri uri, SQLiteQueryBuilder queryBuilder, Context context,
            String[] projection) {
        switch (type) {
            case SERVICE_INTERVALS:
                queryBuilder.setTables(getTableName());
                queryBuilder.setProjectionMap(buildProjectionMap(PROJECTION));
                return true;
            case SERVICE_INTERVAL_ID:
                queryBuilder.setTables(getTableName());
                queryBuilder.setProjectionMap(buildProjectionMap(PROJECTION));
                queryBuilder
                        .appendWhere(ServiceInterval._ID + " = " + uri.getPathSegments().get(1));
                return true;
        }
        return false;
    }

    @Override
    public void registerUris() {
        FillUpsProvider.registerUri(this, URI, SERVICE_INTERVALS);
        FillUpsProvider.registerUri(this, URI + "#", SERVICE_INTERVAL_ID);
    }

    @Override
    public int update(int match, SQLiteDatabase db, Uri uri, ContentValues values,
            String selection, String[] selectionArgs) {
        switch (match) {
            case SERVICE_INTERVAL_ID:
                return db.update(getTableName(), values, ServiceInterval._ID + " = ?",
                        new String[] {
                            values.getAsString(ServiceInterval._ID)
                        });
            case SERVICE_INTERVALS:
                return db.update(getTableName(), values, selection, selectionArgs);
        }
        return -1;
    }

    @Override
    public String[] getProjection() {
        return PROJECTION;
    }
}
