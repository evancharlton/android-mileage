
package com.evancharlton.mileage.provider;

import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.dao.FillupField;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.provider.tables.CacheTable;
import com.evancharlton.mileage.provider.tables.ContentTable;
import com.evancharlton.mileage.provider.tables.FieldsTable;
import com.evancharlton.mileage.provider.tables.FillupsFieldsTable;
import com.evancharlton.mileage.provider.tables.FillupsTable;
import com.evancharlton.mileage.provider.tables.ServiceIntervalTemplatesTable;
import com.evancharlton.mileage.provider.tables.ServiceIntervalsTable;
import com.evancharlton.mileage.provider.tables.VehicleTypesTable;
import com.evancharlton.mileage.provider.tables.VehiclesTable;
import com.evancharlton.mileage.util.Debugger;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.provider.BaseColumns;
import android.util.Log;

public class DatabaseUpgrader {
    private static final String TAG = "DatabaseUpgrader";

    private static final int V1_DATABASE = 3; // Version 1.X

    private static final int V2_DATABASE = 4; // Version 2.X

    private static final int V3_DATABASE = 5; // Version 3.X

    private static final StringBuilder BUILDER = new StringBuilder();

    private static SQLiteDatabase sDatabase;

    // Note: these columns are hard-coded for now. I should back-port the old
    // column name constants but I likely won't.
    public static void upgradeDatabase(final SQLiteDatabase database) {
        sDatabase = database;
        try {
            switch (database.getVersion()) {
                case V1_DATABASE:
                    // add the partial flag
                    exec("ALTER TABLE fillups ADD COLUMN is_partial INTEGER;");

                    // add the restart flag
                    exec("ALTER TABLE fillups ADD COLUMN restart INTEGER;");

                    // add the distance units
                    exec("ALTER TABLE vehicles ADD COLUMN distance INTEGER DEFAULT -1;");

                    // add the volume units
                    exec("ALTER TABLE vehicles ADD COLUMN volume INTEGER DEFAULT -1;");

                    // create the service interval table
                    BUILDER.append("CREATE TABLE maintenance_intervals (");
                    BUILDER.append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
                    BUILDER.append("creation_date INTEGER,");
                    BUILDER.append("creation_odometer DOUBLE,");
                    BUILDER.append("description TEXT,");
                    BUILDER.append("interval_distance DOUBLE,");
                    BUILDER.append("interval_duration INTEGER,");
                    BUILDER.append("vehicle_id INTEGER,");
                    BUILDER.append("is_repeating INTEGER");
                    BUILDER.append(");");
                    flush();

                    // create the version table
                    BUILDER.append("CREATE TABLE version (");
                    BUILDER.append("version INTEGER");
                    BUILDER.append(");");
                    flush();
                case V2_DATABASE:
                    // add the economy field
                    exec("ALTER TABLE fillups ADD COLUMN economy DOUBLE;");
                case V3_DATABASE:
                    // This is the upgrade to 3.0 -- brace for impact!

                    if (backupExistingTables() && createNewTables() && migrateOldData()
                            && cleanUpOldTables()) {
                        Log.d(TAG, "Completed migration!");
                    } else {
                        Log.e(TAG, "Unable to complete migration!");
                    }
                    break;
                default:
                    // unknown version; recurse and start from the beginning
                    database.setVersion(V1_DATABASE);
                    upgradeDatabase(database);
                    return;
            }
            database.setVersion(FillUpsProvider.DATABASE_VERSION);
        } catch (SQLiteException e) {
            Log.e(TAG, "Couldn't upgrade database!", e);
        }
    }

    private static final void flush() {
        exec(BUILDER.toString());
        BUILDER.setLength(0);
    }

    private static final void exec(final String query) {
        log(query);
        sDatabase.execSQL(query);
    }

    private static final void log(final String msg) {
        Debugger.d(TAG, msg);
    }

    private static boolean backupExistingTables() {
        String[] tables = new String[] {
                "fillups", "vehicles", "maintenance_intervals"
        };

        try {
            for (String table : tables) {
                BUILDER.append("ALTER TABLE ").append(table).append(" RENAME TO OLD_")
                        .append(table);
                flush();
            }
            return true;
        } catch (SQLiteException e) {
            Log.e(TAG, "Unable to backup existing tables!", e);
        }
        return false;
    }

    private static boolean createNewTables() {
        ContentTable[] tables =
                new ContentTable[] {
                        new FillupsTable(), new FillupsFieldsTable(), new FieldsTable(),
                        new VehiclesTable(), new VehicleTypesTable(), new ServiceIntervalsTable(),
                        new ServiceIntervalTemplatesTable(), new CacheTable()
                };

        try {
            for (ContentTable table : tables) {
                exec(table.create());
                String[] upgradeSql = table.init(true);
                if (upgradeSql != null) {
                    for (String sql : upgradeSql) {
                        exec(sql);
                    }
                }
            }
            return true;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unable to create new table!", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Unable to create new table!", e);
        }
        return false;
    }

    private static boolean migrateOldData() {
        try {
            // migrate vehicle data
            BUILDER.append("INSERT INTO ").append(VehiclesTable.TABLE_NAME).append(" (");
            BUILDER.append(Vehicle.MAKE).append(", ");
            BUILDER.append(Vehicle.MODEL).append(", ");
            BUILDER.append(Vehicle.TITLE).append(", ");
            BUILDER.append(Vehicle.YEAR).append(", ");
            BUILDER.append(Vehicle.DEFAULT_TIME).append(", ");
            BUILDER.append(Vehicle.VEHICLE_TYPE);
            BUILDER.append(") SELECT make, model, ");
            BUILDER.append("CASE WHEN title IS NULL OR title=\"\" THEN (year||\" \"||make||\" \"||model) ELSE title END AS d_title, ");
            BUILDER.append("year, def, '1' FROM OLD_vehicles;");
            flush();

            // TODO(3.1) - migrate service intervals.

            // migrate fillup data
            BUILDER.append("INSERT INTO ").append(FillupsTable.TABLE_NAME).append(" (");
            BUILDER.append(Fillup.DATE).append(", ");
            BUILDER.append(Fillup.ECONOMY).append(", ");
            BUILDER.append(Fillup.LATITUDE).append(", ");
            BUILDER.append(Fillup.LONGITUDE).append(", ");
            BUILDER.append(Fillup.ODOMETER).append(", ");
            BUILDER.append(Fillup.PARTIAL).append(", ");
            BUILDER.append(Fillup.RESTART).append(", ");
            BUILDER.append(Fillup.TOTAL_COST).append(", ");
            BUILDER.append(Fillup.UNIT_PRICE).append(", ");
            BUILDER.append(Fillup.VEHICLE_ID).append(", ");
            BUILDER.append(Fillup.VOLUME);
            BUILDER.append(") SELECT date, '0', latitude, longitude, mileage, is_partial, restart, ");
            BUILDER.append("(cost * amount), cost, vehicle_id, amount FROM OLD_fillups;");
            flush();

            // migrate the fillup comments
            BUILDER.append("INSERT INTO ").append(FillupsFieldsTable.TABLE_NAME).append(" (");
            BUILDER.append(FillupField.FILLUP_ID).append(", ");
            BUILDER.append(FillupField.TEMPLATE_ID).append(", ");
            BUILDER.append(FillupField.VALUE);
            BUILDER.append(") SELECT _id, '1', comment FROM OLD_fillups;");
            flush();

            // Update the vehicle IDs
            BUILDER.append("UPDATE fillups SET vehicle_id = (SELECT vehicles._id FROM vehicles, OLD_vehicles WHERE vehicles.title = OLD_vehicles.title AND OLD_vehicles._id = vehicle_id)");
            flush();

            return true;
        } catch (SQLiteException e) {
            Log.e(TAG, "Unable to migrate data!", e);
            return false;
        }
    }

    private static boolean cleanUpOldTables() {
        try {
            // exec("DROP TABLE OLD_vehicles");
            // exec("DROP TABLE OLD_fillups");
            // exec("DROP TABLE OLD_maintenance_intervals");
            return true;
        } catch (SQLiteException e) {
            Log.e(TAG, "Unable to clean up old tables!", e);
            return false;
        }
    }
}
