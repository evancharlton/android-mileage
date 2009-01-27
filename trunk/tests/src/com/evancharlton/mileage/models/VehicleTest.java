package com.evancharlton.mileage.models;

import junit.framework.TestCase;
import android.database.sqlite.SQLiteDatabase;

import com.evancharlton.mileage.FillUpsProvider;
import com.evancharlton.mileage.Mileage;

public class VehicleTest extends TestCase {
	protected void setUp() {
	}

	protected void tearDown() {
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME, null);
		db.delete(FillUpsProvider.VEHICLES_TABLE_NAME, "1", null);
		db.close();
	}

	public void testSaveAndLoad() {
		final String make = "Make";
		final String model = "Model";
		final String year = "Year";
		final String title = "Title";

		Vehicle v = new Vehicle();
		v.setMake(make);
		v.setModel(model);
		v.setYear(year);
		v.setTitle(title);
		long id = v.save();

		assertTrue(id > 0);

		v = new Vehicle(id);
		assertEquals(make, v.getMake());
		assertEquals(model, v.getModel());
		assertEquals(year, v.getYear());
		assertEquals(title, v.getTitle());
	}

	public void testDefault() {
		tearDown();
		final String make = "Make";
		final String model = "Model";
		final String year = "Year";
		final String title = "Title";

		Vehicle v = new Vehicle();
		v.setMake(make);
		v.setModel(model);
		v.setYear(year);
		v.setTitle(title);
		assertFalse(v.isDefault());
		long id = v.save();

		assertTrue(v.isDefault());

		v.setDefault(false);
		assertFalse(v.isDefault());
		id = v.save();

		v = new Vehicle(id);
		assertTrue(v.isDefault());
	}

	public void testTitle() {
		final String make = "Make";
		final String model = "Model";
		final String year = "Year";

		Vehicle v = new Vehicle();
		v.setMake(make);
		v.setModel(model);
		v.setYear(year);

		assertEquals(year + " " + make + " " + model, v.getTitle());
	}
}
