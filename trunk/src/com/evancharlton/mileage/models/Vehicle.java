package com.evancharlton.mileage.models;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.evancharlton.mileage.FillUpsProvider;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.calculators.CalculationEngine;

public class Vehicle extends Model {
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/vehicles");
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evancharlton.vehicle";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evancharlton.vehicle";
	public static final String TITLE = "title";
	public static final String MAKE = "make";
	public static final String MODEL = "model";
	public static final String YEAR = "year";
	public static final String DEFAULT = "def";
	public static final String DISTANCE_UNITS = "distance";
	public static final String VOLUME_UNITS = "volume";
	public static final String DEFAULT_SORT_ORDER = DEFAULT + " DESC, " + TITLE + " ASC";

	public static final List<String> PROJECTION = new ArrayList<String>();

	static {
		PROJECTION.add(_ID);
		PROJECTION.add(TITLE);
		PROJECTION.add(MAKE);
		PROJECTION.add(MODEL);
		PROJECTION.add(YEAR);
		PROJECTION.add(DEFAULT);
		PROJECTION.add(DISTANCE_UNITS);
		PROJECTION.add(VOLUME_UNITS);
	}

	private static final int DEFAULT_UNKNOWN = 0;
	private static final int DEFAULT_TRUE = 1;
	private static final int DEFAULT_FALSE = 2;
	private static final int DEFAULT_FALSE_CHANGED = 3;

	private String m_title = "";
	private String m_make = "";
	private String m_model = "";
	private String m_year = "";
	private int m_defaultState = DEFAULT_UNKNOWN;
	private int m_volumeUnits = -1;
	private int m_distanceUnits = -1;

	public Vehicle() {
		super(FillUpsProvider.VEHICLES_TABLE_NAME);
	}

	public Vehicle(ContentValues values) {
		this();

		String title = values.getAsString(TITLE);
		if (title != null) {
			setTitle(title);
		}

		String make = values.getAsString(MAKE);
		if (make != null) {
			setMake(make);
		}

		String model = values.getAsString(MODEL);
		if (model != null) {
			setModel(model);
		}

		String year = values.getAsString(YEAR);
		if (year != null) {
			setYear(year);
		}
	}

	public Vehicle(long id) {
		this();

		m_id = id;

		openDatabase();
		String[] projection = getProjection();
		String selection = _ID + " = ?";
		String[] selectionArgs = new String[] {
			String.valueOf(id)
		};
		String groupBy = null;
		String orderBy = null;
		String having = null;

		Cursor c = m_db.query(FillUpsProvider.VEHICLES_TABLE_NAME, projection, selection, selectionArgs, groupBy, having, orderBy);

		if (c.getCount() == 1) {
			c.moveToFirst();
			load(c);
		}
		closeDatabase(c);
	}

	/**
	 * Populate this vehicle's data from a database Cursor instance.
	 * 
	 * @param c the cursor to use when building the object
	 */
	public Vehicle(Cursor c) {
		this();
		load(c);
	}

	private void load(Cursor c) {
		int index = c.getColumnIndex(_ID);
		if (index >= 0) {
			setId(c.getLong(index));
		}

		index = c.getColumnIndex(TITLE);
		if (index >= 0) {
			setTitle(c.getString(index));
		}

		index = c.getColumnIndex(MAKE);
		if (index >= 0) {
			setMake(c.getString(index));
		}

		index = c.getColumnIndex(MODEL);
		if (index >= 0) {
			setModel(c.getString(index));
		}

		index = c.getColumnIndex(YEAR);
		if (index >= 0) {
			setYear(c.getString(index));
		}

		index = c.getColumnIndex(DISTANCE_UNITS);
		if (index >= 0) {
			setDistanceUnits(c.getInt(index));
		}

		index = c.getColumnIndex(VOLUME_UNITS);
		if (index >= 0) {
			setVolumeUnits(c.getInt(index));
		}
	}

	public static String[] getProjection() {
		return PROJECTION.toArray(new String[PROJECTION.size()]);
	}

	@Override
	public long save() {
		openDatabase();
		ContentValues values = new ContentValues();
		values.put(Vehicle.MAKE, m_make);
		values.put(Vehicle.MODEL, m_model);
		values.put(Vehicle.TITLE, m_title);
		values.put(Vehicle.YEAR, m_year);
		if (m_defaultState != DEFAULT_UNKNOWN) {
			if (m_defaultState == DEFAULT_TRUE) {
				values.put(Vehicle.DEFAULT, System.currentTimeMillis());
			} else if (m_defaultState == DEFAULT_FALSE_CHANGED) {
				values.put(Vehicle.DEFAULT, 0);
			}
		}
		if (m_id == -1) {
			// save a new record
			m_id = m_db.insert(FillUpsProvider.VEHICLES_TABLE_NAME, null, values);
		} else {
			// update an existing record
			m_db.update(FillUpsProvider.VEHICLES_TABLE_NAME, values, Vehicle._ID + " = ?", new String[] {
				String.valueOf(m_id)
			});
		}
		m_defaultState = DEFAULT_UNKNOWN;
		closeDatabase(null);
		return m_id;
	}

	@Override
	public int validate() {
		if (m_model.length() == 0) {
			return R.string.error_model;
		} else if (m_make.length() == 0) {
			return R.string.error_make;
		} else if (m_year.length() == 0) {
			return R.string.error_year;
		}
		return -1;
	}

	public FillUp getOldestFillUp(CalculationEngine ce) {
		openDatabase();
		String[] projection = new String[] {
			FillUp._ID
		};
		String selection = FillUp.VEHICLE_ID + " = ?";
		String[] selectionArgs = new String[] {
			String.valueOf(m_id)
		};
		String groupBy = null;
		String orderBy = FillUp.ODOMETER + " ASC";
		String having = null;
		String limit = "1";

		Cursor c = m_db.query(FillUpsProvider.FILLUPS_TABLE_NAME, projection, selection, selectionArgs, groupBy, having, orderBy, limit);
		c.moveToFirst();

		long id = c.getLong(0);
		closeDatabase(c);

		return new FillUp(ce, id);
	}

	public FillUp getNewestFillUp(CalculationEngine ce) {
		openDatabase();
		String[] projection = new String[] {
			FillUp._ID
		};
		String selection = FillUp.VEHICLE_ID + " = ?";
		String[] selectionArgs = new String[] {
			String.valueOf(m_id)
		};
		String groupBy = null;
		String orderBy = FillUp.ODOMETER + " DESC";
		String having = null;
		String limit = "1";

		Cursor c = m_db.query(FillUpsProvider.VEHICLES_TABLE_NAME, projection, selection, selectionArgs, groupBy, having, orderBy, limit);
		c.moveToFirst();

		long id = c.getLong(0);
		closeDatabase(c);

		return new FillUp(ce, id);
	}

	/**
	 * See if this is the default vehicle. This field is lazy-loaded, so the
	 * first call to this will be expensive (but is then cached).
	 * 
	 * @return true if default, false if not
	 */
	public boolean isDefault() {
		if (m_defaultState == DEFAULT_UNKNOWN) {
			if (m_id < 0) {
				m_defaultState = DEFAULT_FALSE;
			} else {
				// look it up from the database
				openDatabase();
				String[] projection = new String[] {
					_ID
				};
				String selection = "1";
				String[] selectionArgs = null;
				String groupBy = null;
				String orderBy = DEFAULT + " DESC";
				String having = null;
				String limit = "1";

				Cursor c = m_db.query(FillUpsProvider.VEHICLES_TABLE_NAME, projection, selection, selectionArgs, groupBy, having, orderBy, limit);
				if (c.getCount() == 1) {
					c.moveToFirst();
					if (c.getLong(0) == m_id) {
						m_defaultState = DEFAULT_TRUE;
					} else {
						m_defaultState = DEFAULT_FALSE;
					}
				}
				closeDatabase(c);
			}
		}
		return (m_defaultState == DEFAULT_TRUE);
	}

	/**
	 * Set whether default or not
	 * 
	 * @param def whether default or not
	 */
	public void setDefault(boolean def) {
		if (!def) {
			m_defaultState = DEFAULT_FALSE_CHANGED;
		} else {
			m_defaultState = DEFAULT_TRUE;
		}
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		if (m_title.length() == 0) {
			return m_year + " " + m_make + " " + m_model;
		}
		return m_title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		m_title = title.trim();
	}

	/**
	 * @return the make
	 */
	public String getMake() {
		return m_make;
	}

	/**
	 * @param make the make to set
	 */
	public void setMake(String make) {
		m_make = make.trim();
	}

	/**
	 * @return the model
	 */
	public String getModel() {
		return m_model;
	}

	/**
	 * @param model the model to set
	 */
	public void setModel(String model) {
		m_model = model.trim();
	}

	/**
	 * @return the year
	 */
	public String getYear() {
		return m_year;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(String year) {
		m_year = year.trim();
	}

	public int getFillUpCount() {
		openDatabase();
		String[] projection = new String[] {
			FillUp._ID
		};
		String selection = FillUp.VEHICLE_ID + " = ?";
		String[] selectionArgs = new String[] {
			String.valueOf(m_id)
		};
		String groupBy = null;
		String orderBy = null;
		String having = null;

		Cursor c = m_db.query(FillUpsProvider.FILLUPS_TABLE_NAME, projection, selection, selectionArgs, groupBy, having, orderBy);
		c.moveToFirst();

		int count = c.getCount();
		closeDatabase(c);
		return count;
	}

	/**
	 * Return a List of all the FillUps for this Vehicle. Note that this is
	 * *not* cached and will be run every single time this is called.
	 * 
	 * @return a List of all FillUps for this Vehicle.
	 */
	public List<FillUp> getAllFillUps(CalculationEngine engine) {
		List<FillUp> all = new ArrayList<FillUp>();
		openDatabase();
		String[] projection = FillUp.getProjection();
		String selection = FillUp.VEHICLE_ID + " = ?";
		String[] selectionArgs = new String[] {
			String.valueOf(m_id)
		};
		String groupBy = null;
		String orderBy = FillUp.ODOMETER + " ASC";
		String having = null;

		Cursor c = m_db.query(FillUpsProvider.FILLUPS_TABLE_NAME, projection, selection, selectionArgs, groupBy, having, orderBy);
		c.moveToFirst();

		while (c.isAfterLast() == false) {
			all.add(new FillUp(engine, c));
			c.moveToNext();
		}

		closeDatabase(c);
		return all;
	}

	/**
	 * @return the volumeUnits
	 */
	public int getVolumeUnits() {
		return m_volumeUnits;
	}

	/**
	 * @param volumeUnits the volumeUnits to set
	 */
	public void setVolumeUnits(int volumeUnits) {
		m_volumeUnits = volumeUnits;
	}

	/**
	 * @return the distanceUnits
	 */
	public int getDistanceUnits() {
		return m_distanceUnits;
	}

	/**
	 * @param distanceUnits the distanceUnits to set
	 */
	public void setDistanceUnits(int distanceUnits) {
		m_distanceUnits = distanceUnits;
	}
}
