package com.evancharlton.mileage.models;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.evancharlton.mileage.FillUpsProvider;
import com.evancharlton.mileage.Mileage;

public abstract class Model implements BaseColumns {
	protected SQLiteDatabase m_db = null;
	protected long m_id = -1;
	private String m_tableName = null;

	public abstract long save();

	public static String[] getProjection() {
		return new String[] {};
	}

	/**
	 * See if the current state of the object is valid for saving. If invalid, a
	 * number greater than zero will be returned (usually, it's the ID of the
	 * string of the error message). Zero or below means that the object can go
	 * ahead and be saved.
	 * 
	 * @return A number less than or equal to zero if the object is valid.
	 */
	public abstract int validate();

	public Model(String tableName) {
		m_tableName = tableName;
	}

	public long getId() {
		return m_id;
	}

	public void setId(long id) {
		m_id = id;
	}

	/**
	 * Deletes the object from the database, provided that it's actually in the
	 * database.
	 */
	public void delete() {
		if (m_id >= 0 && m_tableName != null) {
			openDatabase();
			int num = m_db.delete(m_tableName, BaseColumns._ID + " = ?", new String[] {
				String.valueOf(m_id)
			});
			if (num == 1) {
				m_id = -1;
			}
			closeDatabase();
		}
	}

	/**
	 * Opens the database connection, if not already opened. Every open should
	 * be accompanied by a corresponding closeDatabase() call!
	 */
	protected void openDatabase() {
		if (m_db == null) {
			m_db = SQLiteDatabase.openOrCreateDatabase("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME, null);
		}
	}

	/**
	 * Closes the database, if opened. Note that after closing, m_db should be
	 * null.
	 */
	protected void closeDatabase() {
		if (m_db != null) {
			m_db.close();
			m_db = null;
		}
	}
}
