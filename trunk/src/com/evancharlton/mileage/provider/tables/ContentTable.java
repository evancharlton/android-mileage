
package com.evancharlton.mileage.provider.tables;

import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.dao.Dao.Column;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Set;

public abstract class ContentTable {
    protected static String TABLE_NAME = "content_table";

    abstract public String getTableName();

    public static final HashMap<String, String> buildProjectionMap(String[] map) {
        HashMap<String, String> projection = new HashMap<String, String>();
        // just in case
        projection.put(BaseColumns._ID, BaseColumns._ID);
        for (String key : map) {
            projection.put(key, key);
        }
        return projection;
    }

    public String getDefaultSortOrder() {
        return BaseColumns._ID + " desc";
    }

    abstract public void registerUris();

    public int delete(SQLiteDatabase db, Uri uri, String selection, String[] selectionArgs) {
        try {
            long id = ContentUris.parseId(uri);
            if (selection == null) {
                selection = "";
            }
            selection += BaseColumns._ID + " = ?";

            if (selectionArgs == null) {
                selectionArgs = new String[0];
            }
            final int length = selectionArgs.length + 1;
            String[] args = new String[length];
            for (int i = 0; i < length - 1; i++) {
                args[i] = selectionArgs[i];
            }
            args[length - 1] = String.valueOf(id);
            selectionArgs = args;
        } catch (UnsupportedOperationException e) {
            // silently fail
        } catch (NumberFormatException e) {
            // silently fail
        }
        return db.delete(getTableName(), selection, selectionArgs);
    }

    public final boolean isValidType(int type) {
        return getType(type) != null;
    }

    abstract public String getType(int type);

    abstract public long insert(int type, SQLiteDatabase db, ContentValues initialValues);

    abstract public boolean query(int type, Uri uri, SQLiteQueryBuilder queryBuilder,
            Context context, String[] projection);

    abstract public int update(int match, SQLiteDatabase db, Uri uri, ContentValues values,
            String selection, String[] selectionArgs);

    abstract public String[] init(boolean isUpgrade);

    abstract protected Class<? extends Dao> getDaoType();

    public final String create() throws IllegalArgumentException, IllegalAccessException {
        TableBuilder builder = new TableBuilder();
        Class<? extends Dao> cls = getDaoType();
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof Column) {
                    Column columnAnnotation = (Column) annotation;
                    String columnName = columnAnnotation.name();
                    switch (columnAnnotation.type()) {
                        case Column.INTEGER:
                        case Column.BOOLEAN:
                        case Column.LONG:
                        case Column.TIMESTAMP:
                            builder.addInteger(columnName);
                            break;
                        case Column.DOUBLE:
                            builder.addDouble(columnName);
                            break;
                        case Column.STRING:
                            builder.addText(columnName);
                            break;
                    }
                    break;
                }
            }
        }
        return builder.build();
    }

    abstract public String[] getProjection();

    protected final class TableBuilder {
        private StringBuilder mBuilder = new StringBuilder();

        public TableBuilder() {
            mBuilder.append("CREATE TABLE ").append(getTableName()).append(" (");
            mBuilder.append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT");
        }

        public TableBuilder addDouble(String fieldName) {
            return addField(fieldName, "DOUBLE");
        }

        public TableBuilder addInteger(String fieldName) {
            return addField(fieldName, "INTEGER");
        }

        public TableBuilder addText(String fieldName) {
            return addField(fieldName, "TEXT");
        }

        private TableBuilder addField(String fieldName, String fieldType) {
            mBuilder.append(", ").append(fieldName).append(" ").append(fieldType);
            return this;
        }

        public String build() {
            mBuilder.append(");");
            return mBuilder.toString();
        }

        @Override
        public String toString() {
            return build();
        }
    }

    protected final class InsertBuilder {
        private StringBuilder mBuilder = new StringBuilder();

        private HashMap<String, String> mData = new HashMap<String, String>();

        public InsertBuilder() {
            mBuilder.append("INSERT INTO ").append(getTableName()).append(" (");
        }

        public InsertBuilder add(String field, String value) {
            mData.put(field, value);
            return this;
        }

        public InsertBuilder add(String field, long value) {
            return add(field, String.valueOf(value));
        }

        public String build() {
            Set<String> keySet = mData.keySet();
            final int length = keySet.size();
            String[] values = new String[length];

            int i = 0;
            for (String key : keySet) {
                values[i] = mData.get(key);
                mBuilder.append(key);
                if (i + 1 < length) {
                    mBuilder.append(",");
                }
                i++;
            }
            mBuilder.append(") VALUES (");
            for (i = 0; i < length; i++) {
                mBuilder.append("'").append(values[i]).append("'");
                if (i + 1 < length) {
                    mBuilder.append(",");
                }
            }

            mBuilder.append(");");
            return mBuilder.toString();
        }

        @Override
        public String toString() {
            return build();
        }
    }
}
