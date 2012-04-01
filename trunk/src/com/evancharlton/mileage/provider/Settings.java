
package com.evancharlton.mileage.provider;

import android.os.Environment;

public final class Settings {
    public static final String NAME = "com.evancharlton.mileage_preferences";

    public static final String STORE_LOCATION = "location_data";
    public static final String DATA_FORMAT = "data_format";

    public static final String NOTIFICATIONS_ENABLED = "interval_notification_enabled";

    public static final String NOTIFICATIONS_RINGTONE = "interval_notification_ringtone";

    public static final String NOTIFICATIONS_LED = "interval_notification_led";

    public static final String NOTIFICATIONS_VIBRATE = "interval_notification_vibrate";

    public static final String META_FIELD = "meta_field";

    public static final String AUTO_BACKUPS = "auto_backup";

    public static final String EXTERNAL_DIR = Environment.getExternalStorageDirectory()
            + "/mileage/";

    public static final class DataFormats {
        // These *must* be kept in sync with @arrays/data_formats !
        public static final int UNIT_PRICE_VOLUME = 0;

        public static final int TOTAL_COST_VOLUME = 1;

        public static final int TOTAL_COST_UNIT_PRICE = 2;
    }
}
