
package com.evancharlton.mileage.provider.tables;

import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.dao.ServiceIntervalTemplate;
import com.evancharlton.mileage.provider.FillUpsProvider;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.Map.Entry;

public class ServiceIntervalTemplatesTable extends ContentTable {

    private static final int SERVICE_TEMPLATES = 60;

    private static final int SERVICE_TEMPLATE_ID = 61;

    public static final String URI = "intervals/templates";

    public static final Uri BASE_URI = Uri.withAppendedPath(FillUpsProvider.BASE_URI, URI);

    private static final String CONTENT_TYPE =
            "vnd.android.cursor.dir/vnd.evancharlton.interval_template";

    private static final String CONTENT_ITEM_TYPE =
            "vnd.android.cursor.item/vnd.evancharlton.interval_template";

    public static final String[] PROJECTION = new String[] {
            ServiceIntervalTemplate._ID, ServiceIntervalTemplate.TITLE,
            ServiceIntervalTemplate.DESCRIPTION, ServiceIntervalTemplate.DISTANCE,
            ServiceIntervalTemplate.DURATION, ServiceIntervalTemplate.VEHICLE_TYPE
    };

    // FIXME - Hard-coded strings.
    public static final ContentValues[] TEMPLATES = new ContentValues[] {
            createInterval("Transmission fluid", "Replace transmission fluid", 25000, 25),
            createInterval("Timing belt", "Replace timing belt", 60000, 60),
            createInterval("Fuel filter", "Replace fuel filter", 25000, 25),
            createInterval("Power steering fluid", "Replace power steering fluid", 30000, 30),
            createInterval("Replace air filter", "Replace air filter", 15000, 15),
            createInterval("Oil change (synthetic)", "Synthetic oil change", 1000, 10),
            createInterval("Oil change (standard)", "Standard oil change", 3000, 3)
    };

    @Override
    protected Class<? extends Dao> getDaoType() {
        return ServiceIntervalTemplate.class;
    }

    @Override
    public String getTableName() {
        return "service_interval_templates";
    }

    @Override
    public String getType(int type) {
        switch (type) {
            case SERVICE_TEMPLATES:
                return CONTENT_TYPE;
            case SERVICE_TEMPLATE_ID:
                return CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Override
    public String[] init(boolean isUpgrade) {
        String[] sql = new String[TEMPLATES.length];

        for (int i = 0; i < TEMPLATES.length; i++) {
            sql[i] = createIntervalSQL(TEMPLATES[i]);
        }

        return sql;
    }

    private String createIntervalSQL(ContentValues values) {
        InsertBuilder builder = new InsertBuilder();
        for (Entry<String, Object> entry : values.valueSet()) {
            builder.add(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return builder.build();
    }

    private static ContentValues createInterval(String title, String description, long miles,
            long months) {
        ContentValues values = new ContentValues();
        values.put(ServiceIntervalTemplate.TITLE, title);
        values.put(ServiceIntervalTemplate.DESCRIPTION, description);
        values.put(ServiceIntervalTemplate.DISTANCE, miles(miles));
        values.put(ServiceIntervalTemplate.DURATION, months(months));
        values.put(ServiceIntervalTemplate.VEHICLE_TYPE, 1); // FIXME
        return values;
    }

    private static long miles(long num) {
        return 1609L * 100L * num;
    }

    private static long months(long num) {
        return 1000L * 60L * 60L * 24L * 30L * num;
    }

    @Override
    public long insert(int type, SQLiteDatabase db, ContentValues initialValues) {
        switch (type) {
            case SERVICE_TEMPLATES:
                return db.insert(getTableName(), null, initialValues);
        }
        return -1L;
    }

    @Override
    public boolean query(int type, Uri uri, SQLiteQueryBuilder queryBuilder, Context context,
            String[] projection) {
        switch (type) {
            case SERVICE_TEMPLATES:
                queryBuilder.setTables(getTableName());
                queryBuilder.setProjectionMap(buildProjectionMap(PROJECTION));
                return true;
            case SERVICE_TEMPLATE_ID:
                queryBuilder.setTables(getTableName());
                queryBuilder.setProjectionMap(buildProjectionMap(PROJECTION));
                queryBuilder.appendWhere(BaseColumns._ID + " = " + uri.getPathSegments().get(2));
                return true;
        }
        return false;
    }

    @Override
    public void registerUris() {
        FillUpsProvider.registerUri(this, URI, SERVICE_TEMPLATES);
        FillUpsProvider.registerUri(this, URI + "/#", SERVICE_TEMPLATE_ID);
    }

    @Override
    public int update(int match, SQLiteDatabase db, Uri uri, ContentValues values,
            String selection, String[] selectionArgs) {
        switch (match) {
            case SERVICE_TEMPLATE_ID:
                return db.update(getTableName(), values, ServiceIntervalTemplate._ID + " = ?",
                        new String[] {
                            values.getAsString(ServiceIntervalTemplate._ID)
                        });
            case SERVICE_TEMPLATES:
                return db.update(getTableName(), values, selection, selectionArgs);
        }
        return -1;
    }

    @Override
    public String[] getProjection() {
        return PROJECTION;
    }
}
