package com.evancharlton.mileage.models;

import android.database.sqlite.SQLiteDatabase;

import com.evancharlton.mileage.FillUpsProvider;
import com.evancharlton.mileage.Mileage;
import com.evancharlton.mileage.PreferencesProvider;
import com.evancharlton.mileage.calculators.CalculationEngine;
import com.evancharlton.mileage.tests.TestCase;

public class FillUpTest extends TestCase {
	private CalculationEngine ce;

	public void setUp() {
		ce = new CalculationEngine();
		ce.setInputDistance(PreferencesProvider.MILES);
		ce.setInputVolume(PreferencesProvider.GALLONS);
		ce.setEconomy(PreferencesProvider.MI_PER_GALLON);
	}

	public void tearDown() {
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME, null);
		db.delete(FillUpsProvider.FILLUPS_TABLE_NAME, "1", null);
		db.close();
	}

	public void testSaveAndLoad() throws Exception {
		double amount = 10;
		String comment = "This is a sample fill-up";
		double price = 3.599;
		double latitude = 1;
		double longitude = 2;
		double odometer = 100;
		long vehicleId = 1;

		FillUp f = new FillUp(ce);
		f.setAmount(amount);
		f.setComment(comment);
		f.setPrice(price);
		f.setLatitude(latitude);
		f.setLongitude(longitude);
		f.setOdometer(odometer);
		f.setVehicleId(vehicleId);

		long id = f.save();
		f = new FillUp(ce, id);

		assertEquals(amount, f.getAmount());
		assertEquals(comment, f.getComment());
		assertEquals(price, f.getPrice());
		assertEquals(latitude, f.getLatitude());
		assertEquals(longitude, f.getLongitude());
		assertEquals(odometer, f.getOdometer());
		assertEquals(vehicleId, f.getVehicleId());
	}

	public void testPrevious() {
		FillUp f = new FillUp(ce);
		f.setAmount(10);
		f.setComment("First");
		f.setPrice(3.099);
		f.setOdometer(100);
		f.setVehicleId(1);
		long id = f.save();

		f = new FillUp(ce);
		f.setAmount(10);
		f.setComment("Second (fake)");
		f.setPrice(3.199);
		f.setOdometer(100);
		f.setVehicleId(2);
		f.save();

		f = new FillUp(ce);
		f.setAmount(10);
		f.setComment("Second");
		f.setPrice(3.199);
		f.setOdometer(200);
		f.setVehicleId(1);
		long id_real = f.save();

		f = new FillUp(ce, id_real);
		FillUp previous = f.getPrevious();

		assertEquals(previous.getId(), id);
	}

	public void testNext() {
		FillUp f = new FillUp(ce);
		f.setAmount(10);
		f.setComment("First");
		f.setPrice(3.099);
		f.setOdometer(100);
		f.setVehicleId(1);
		long id = f.save();

		f = new FillUp(ce);
		f.setAmount(10);
		f.setComment("Second (fake)");
		f.setPrice(3.199);
		f.setOdometer(100);
		f.setVehicleId(2);
		f.save();

		f = new FillUp(ce);
		f.setAmount(10);
		f.setComment("Second");
		f.setPrice(3.199);
		f.setOdometer(200);
		f.setVehicleId(1);
		long id_real = f.save();

		f = new FillUp(ce, id);
		FillUp next = f.getNext();

		assertEquals(next.getId(), id_real);
	}

	public void test_calcEconomy() {
		FillUp f = new FillUp(ce);
		f.setAmount(10);
		f.setComment("First");
		f.setPrice(3.099);
		f.setOdometer(100);
		f.setVehicleId(1);
		f.save();

		f = new FillUp(ce);
		f.setAmount(10);
		f.setComment("Second");
		f.setPrice(3.199);
		f.setOdometer(200);
		f.setVehicleId(1);
		f.save();

		assertCloseEnough(10, f.calcEconomy());
	}

	public void test_calcDistance() {
		FillUp f = new FillUp(ce);
		f.setAmount(10);
		f.setComment("First");
		f.setPrice(3.099);
		f.setOdometer(100);
		f.setVehicleId(1);
		f.save();

		f = new FillUp(ce);
		f.setAmount(10);
		f.setComment("Second");
		f.setPrice(3.199);
		f.setOdometer(200);
		f.setVehicleId(1);
		f.save();

		assertCloseEnough(100, f.calcDistance());
	}

	public void test_calcCost() {
		FillUp f = new FillUp(ce);
		f.setAmount(10);
		f.setComment("Second");
		f.setPrice(3.199);
		f.setOdometer(200);
		f.setVehicleId(1);
		f.save();

		assertCloseEnough(31.99, f.calcCost());
	}
}
