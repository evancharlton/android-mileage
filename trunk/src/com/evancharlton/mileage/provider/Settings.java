package com.evancharlton.mileage.provider;

public final class Settings {
	public static final String NAME = "com.evancharlton.mileage_preferences";
	public static final String STORE_LOCATION = "location_data";
	public static final String BACKUPS = "backups";
	public static final String DATA_FORMAT = "data_format";

	public static final class DataFormats {
		// These *must* be kept in sync with @arrays/data_formats !
		public static final int UNIT_PRICE_VOLUME = 0;
		public static final int TOTAL_COST_VOLUME = 1;
		public static final int TOTAL_COST_UNIT_PRICE = 2;
	}
}