package com.evancharlton.mileage.models;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.evancharlton.mileage.FillUpsProvider;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.calculators.CalculationEngine;

/**
 * Provide a data model to encapsulate the logic for a fill-up. Note that this
 * class is very lazy and will ideally never do calculations twice, cache
 * everything, and just generally be as efficient as possible so that callers
 * don't have to worry about caching stuff on their end. The convention this
 * class should use is that methods that begin with calc*() act like getters,
 * but have to do some calculation (most likely, involving database action). The
 * results of this calculation should be cached, so it should not be a
 * significant performance impact, but the caller should be aware of the
 * implications.
 * 
 */
public class FillUp extends Model {

	public static final String AUTHORITY = "com.evancharlton.provider.Mileage";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/fillups");
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evancharlton.fillup";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evancharlton.fillup";
	public static final String DEFAULT_SORT_ORDER = "mileage DESC";
	public static final String COST = "cost";
	public static final String AMOUNT = "amount";
	public static final String MILEAGE = "mileage";
	public static final String DATE = "date";
	public static final String VEHICLE_ID = "vehicle_id";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String COMMENT = "comment";
	public static final Map<String, String> PLAINTEXT = new HashMap<String, String>();

	static {
		PLAINTEXT.put(DATE, "Date");
		PLAINTEXT.put(COST, "Price per gallon");
		PLAINTEXT.put(AMOUNT, "Gallons of fuel");
		PLAINTEXT.put(MILEAGE, "Odometer");
		PLAINTEXT.put(VEHICLE_ID, "Vehicle");
		PLAINTEXT.put(LATITUDE, "Latitude");
		PLAINTEXT.put(LONGITUDE, "Longitude");
		PLAINTEXT.put(COMMENT, "Fill-Up Comment");
	}

	private double m_odometer = 0;
	private Calendar m_date = GregorianCalendar.getInstance();
	private double m_price = 0D; // per unit volume price
	private double m_amount = 0D;
	private double m_latitude = 0L;
	private double m_longitude = 0L;
	private String m_comment = "";
	private long m_vehicleId = -1;

	private double m_economy = 0D;
	private double m_distance = 0D;

	private Vehicle m_vehicle = null;
	private FillUp m_previous = null;
	private FillUp m_next = null;

	private CalculationEngine m_calculator = null;

	public FillUp(CalculationEngine calculator) {
		super(FillUpsProvider.FILLUPS_TABLE_NAME);
		m_calculator = calculator;
	}

	public FillUp(CalculationEngine calculator, long id) {
		this(calculator);
		String selection = _ID + " = ?";
		String[] selectionArgs = new String[] {
			String.valueOf(id)
		};
		String groupBy = null;
		String having = null;
		String orderBy = null;

		String[] projection = getProjection();

		openDatabase();

		Cursor c = m_db.query(FillUpsProvider.FILLUPS_TABLE_NAME, projection, selection, selectionArgs, groupBy, having, orderBy);

		if (c.getCount() == 1) {
			m_id = id;
			c.moveToFirst();
			for (int i = 0; i < c.getColumnCount(); i++) {
				String name = c.getColumnName(i);
				Log.d("COLUMN", name);
				if (name.equals(AMOUNT)) {
					setAmount(c.getLong(i));
				} else if (name.equals(COMMENT)) {
					setComment(c.getString(i));
				} else if (name.equals(COST)) {
					setPrice(c.getDouble(i));
				} else if (name.equals(DATE)) {
					long time = c.getLong(i);
					Calendar cal = GregorianCalendar.getInstance();
					cal.setTimeInMillis(time);
					setDate(cal);
				} else if (name.equals(LATITUDE)) {
					setLatitude(c.getDouble(i));
				} else if (name.equals(LONGITUDE)) {
					setLongitude(c.getDouble(i));
				} else if (name.equals(MILEAGE)) {
					setOdometer(c.getLong(i));
				} else if (name.equals(VEHICLE_ID)) {
					m_vehicleId = c.getLong(i);
				}
			}
		}
		c.close();
		closeDatabase();
	}

	public Vehicle getVehicle() {
		if (m_vehicle == null) {
			m_vehicle = new Vehicle(m_vehicleId);
		}
		return m_vehicle;
	}

	/**
	 * Calculates the fuel economy (based on the user's preferences) since the
	 * previous fill-up.
	 * 
	 * @return the fuel economy since the previous fill-up.
	 */
	public double calcEconomy() {
		if (m_economy == 0D) {
			m_previous = getPrevious();
			if (m_previous == null) {
				// we're at the first fill-up, so there's nothing we can do
				return -1D;
			}
			double distance = calcDistance();
			double fuel = m_amount;
			m_economy = m_calculator.calculateEconomy(distance, fuel);
		}
		return m_economy;
	}

	/**
	 * Calculates the distance since the previous fill-up.
	 * 
	 * @return the distance since the previous fill-up
	 */
	public double calcDistance() {
		if (m_distance == 0D) {
			m_previous = getPrevious();
			if (m_previous == null) {
				// we're at the first fill-up, so there's nothing we can do
				return 0D;
			}
			m_distance = m_odometer - m_previous.getOdometer();
		}
		return m_distance;
	}

	/**
	 * Calculates the total cost for this fill-up
	 * 
	 * @return the total cost for this fill-up
	 */
	public double calcCost() {
		return m_amount * m_price;
	}

	/**
	 * Gets the next (higher mileage) fill-up (if necessary) and returns it. If
	 * there isn't a next one, null is returned.
	 * 
	 * @return The next fill-up
	 */
	public FillUp getNext() {
		if (m_next == null) {
			// get the ID for the next fill-up, if any
			// TODO: this and getPrevious() are basically identical; refactor it
			String selection = MILEAGE + " > ? AND " + VEHICLE_ID + " = ?";
			String[] selectionArgs = new String[] {
					String.valueOf(m_odometer),
					String.valueOf(m_vehicleId)
			};
			String orderBy = MILEAGE + " ASC, " + _ID + " ASC";

			openDatabase();
			Cursor c = m_db.query(FillUpsProvider.FILLUPS_TABLE_NAME, new String[] {
				_ID
			}, selection, selectionArgs, null, null, orderBy, "1");

			if (c.getCount() == 1) {
				c.moveToFirst();
				long id = c.getLong(0);
				closeDatabase(); // we should close before we recurse
				m_next = new FillUp(m_calculator, id);
			}
			closeDatabase(); // just in case the previous block didn't execute
		}
		return m_next;
	}

	/**
	 * Gets the previous (lower mileage) fill-up (if necessary) and returns it.
	 * If there isn't a previous one, null is returned.
	 * 
	 * @return The previous fill-up
	 */
	public FillUp getPrevious() {
		if (m_previous == null) {
			// get the ID for the previous fill-up, if any
			// TODO: this and getNext() are basically identical; refactor it
			String selection = MILEAGE + " < ? AND " + VEHICLE_ID + " = ?";
			String[] selectionArgs = new String[] {
					String.valueOf(m_odometer),
					String.valueOf(m_vehicleId)
			};
			String orderBy = MILEAGE + " DESC, " + _ID + " DESC";

			openDatabase();
			Cursor c = m_db.query(FillUpsProvider.FILLUPS_TABLE_NAME, new String[] {
				_ID
			}, selection, selectionArgs, null, null, orderBy, "1");

			if (c.getCount() == 1) {
				c.moveToFirst();
				long id = c.getLong(0);
				closeDatabase(); // we should close before we recurse
				m_previous = new FillUp(m_calculator, id);
			}
			closeDatabase(); // just in case the previous block didn't execute
		}
		return m_previous;
	}

	@Override
	public String[] getProjection() {
		return new String[] {
				AMOUNT,
				COMMENT,
				COST,
				DATE,
				LATITUDE,
				LONGITUDE,
				MILEAGE,
				VEHICLE_ID
		};
	}

	@Override
	public long save() {
		openDatabase();
		ContentValues values = new ContentValues();
		values.put(AMOUNT, m_amount);
		values.put(COMMENT, m_comment);
		values.put(COST, m_price);
		values.put(DATE, m_date.getTimeInMillis());
		values.put(LATITUDE, m_latitude);
		values.put(LONGITUDE, m_longitude);
		values.put(MILEAGE, m_odometer);
		values.put(VEHICLE_ID, m_vehicleId);
		if (m_id == -1) {
			// save a new record
			m_id = m_db.insert(FillUpsProvider.FILLUPS_TABLE_NAME, null, values);
		} else {
			// update an existing record
			m_db.update(FillUpsProvider.FILLUPS_TABLE_NAME, values, _ID + " = ?", new String[] {
				String.valueOf(m_id)
			});
		}
		closeDatabase();
		return m_id;
	}

	@Override
	public int validate() {
		if (m_odometer == 0D) {
			return R.string.error_mileage;
		} else if (m_amount == 0D) {
			return R.string.error_amount;
		} else if (m_price == 0D) {
			return R.string.error_cost;
		} else if (m_vehicleId == -1) {
			return R.string.error_vehicle;
		}
		return -1;
	}

	// from here down, it's nothing but getters and setters. Boring!

	/**
	 * @param odometer the odometer to set
	 */
	public void setOdometer(double odometer) {
		m_odometer = odometer;
	}

	/**
	 * Set odometer in string format, so that patterns can be parsed out (such
	 * as the '+' prefix notation. Note that if you are using this feature, the
	 * caller needs to set the vehicle ID before calling this in order to get
	 * the previous odometer value!
	 * 
	 * @param odometer The string representing the odometer value
	 */
	public void setOdometer(String odometer) throws NumberFormatException {
		if (odometer.startsWith("+")) {
			m_odometer = Double.MAX_VALUE;
			FillUp previous = getPrevious();
			if (previous == null) {
				setOdometer(odometer.substring(1));
				return;
			}
			m_odometer = previous.getOdometer() + Double.parseDouble(odometer.substring(1));
		} else {
			m_odometer = Double.parseDouble(odometer);
		}
	}

	/**
	 * @return the odometer
	 */
	public double getOdometer() {
		return m_odometer;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(Calendar date) {
		m_date = date;
	}

	/**
	 * Set the date in month/day/year format
	 * 
	 * @param year the year
	 * @param month the month
	 * @param day the day
	 */
	public void setDate(int day, int month, int year) {
		m_date.set(year, month, day);
	}

	/**
	 * @return the date
	 */
	public Calendar getDate() {
		return m_date;
	}

	/**
	 * @param price the price per unit volume
	 */
	public void setPrice(double price) {
		m_price = price;
	}

	/**
	 * @return the price per unit volume
	 */
	public double getPrice() {
		return m_price;
	}

	/**
	 * @param amount the amount to set
	 */
	public void setAmount(double amount) {
		m_amount = amount;
	}

	/**
	 * @return the amount
	 */
	public double getAmount() {
		return m_amount;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		m_comment = comment.trim();
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return m_comment;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(double latitude) {
		m_latitude = latitude;
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return m_latitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(double longitude) {
		m_longitude = longitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return m_longitude;
	}

	/**
	 * @return the vehicleId
	 */
	public long getVehicleId() {
		return m_vehicleId;
	}

	/**
	 * @param vehicleId the vehicleId to set
	 */
	public void setVehicleId(long vehicleId) {
		m_vehicleId = vehicleId;
	}

	/**
	 * @param the CalculationEngine to use
	 */
	public void setCalculationEngine(CalculationEngine calculator) {
		m_calculator = calculator;
	}

	/**
	 * @return the CalculationEngine being used
	 */
	public CalculationEngine getCalculationEngine() {
		return m_calculator;
	}

	/**
	 * Sets the total cost per fill-up. If one (but not both) of either volume
	 * or price is already set, the other will be calculated. If both or neither
	 * are set, then nothing is done. Keep in mind that for in order for the
	 * auto-calculation to work, the other value needs to be set first.
	 * 
	 * @param cost the total cost of the fill-up
	 */
	public void setCost(double cost) {
		if (m_amount == 0D && m_price != 0D) {
			m_amount = cost / m_price;
		} else if (m_amount != 0D && m_price == 0D) {
			m_price = cost / m_amount;
		}
	}
}
