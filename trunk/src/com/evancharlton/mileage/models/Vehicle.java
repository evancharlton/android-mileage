package com.evancharlton.mileage.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.evancharlton.mileage.FillUpsProvider;
import com.evancharlton.mileage.R;

public class Vehicle extends Model {
	public static final String AUTHORITY = "com.evancharlton.provider.Mileage";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/vehicles");
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evancharlton.vehicle";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evancharlton.vehicle";
	public static final String TITLE = "title";
	public static final String MAKE = "make";
	public static final String MODEL = "model";
	public static final String YEAR = "year";
	public static final String DEFAULT = "def";
	public static final String DEFAULT_SORT_ORDER = DEFAULT + " DESC, " + TITLE + " ASC";

	public static final List<String> PROJECTION = new ArrayList<String>();

	static {
		PROJECTION.add(_ID);
		PROJECTION.add(TITLE);
		PROJECTION.add(MAKE);
		PROJECTION.add(MODEL);
		PROJECTION.add(YEAR);
		PROJECTION.add(DEFAULT);
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

	public Vehicle() {
		super(FillUpsProvider.VEHICLES_TABLE_NAME);
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
			for (int i = 0; i < c.getColumnCount(); i++) {
				String name = c.getColumnName(i);
				if (name.equals(TITLE)) {
					setTitle(c.getString(i));
				} else if (name.equals(MAKE)) {
					setMake(c.getString(i));
				} else if (name.equals(MODEL)) {
					setModel(c.getString(i));
				} else if (name.equals(YEAR)) {
					setYear(c.getString(i));
				}
			}
		}

		closeDatabase();
	}

	public Vehicle(Map<String, String> data) {
		this();

		String make = data.get(MAKE);
		String model = data.get(MODEL);
		String year = data.get(YEAR);
		String title = data.get(TITLE);
		String id = data.get(_ID);

		if (id != null) {
			setId(Long.parseLong(id));
		}
		if (make != null) {
			setMake(make);
		}
		if (model != null) {
			setModel(model);
		}
		if (year != null) {
			setYear(year);
		}
		if (title != null) {
			setTitle(title);
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
		closeDatabase();
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

	// From here down, it's just getters and setters

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
				closeDatabase();
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
}
