package com.evancharlton.mileage.models;

import junit.framework.TestCase;
import android.database.sqlite.SQLiteDatabase;

import com.evancharlton.mileage.FillUpsProvider;
import com.evancharlton.mileage.Mileage;

public class FillUpTest extends TestCase {
	public void setUp() {

	}

	public void tearDown() {
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME, null);
		db.delete(FillUpsProvider.FILLUPS_TABLE_NAME, "1", null);
	}

	public void testSaveAndLoad() throws Exception {
		double amount = 10;
		String comment = "This is a sample fill-up";
		double cost = 3.599;
		double latitude = 1;
		double longitude = 2;
		long odometer = 100;
		long vehicleId = 1;

		FillUp f = new FillUp();
		f.setAmount(amount);
		f.setComment(comment);
		f.setCost(cost);
		f.setLatitude(latitude);
		f.setLongitude(longitude);
		f.setOdometer(odometer);
		f.setVehicleId(vehicleId);

		long id = f.save();
		f = new FillUp(id);

		assertEquals(amount, f.getAmount());
		assertEquals(comment, f.getComment());
		assertEquals(cost, f.getCost());
		assertEquals(latitude, f.getLatitude());
		assertEquals(longitude, f.getLongitude());
		assertEquals(odometer, f.getOdometer());
		assertEquals(vehicleId, f.getVehicleId());
	}

	public void testPrevious() {
		FillUp f = new FillUp();
		f.setAmount(10);
		f.setComment("First");
		f.setCost(3.099);
		f.setOdometer(100);
		f.setVehicleId(1);
		long id = f.save();

		f = new FillUp();
		f.setAmount(10);
		f.setComment("Second (fake)");
		f.setCost(3.199);
		f.setOdometer(100);
		f.setVehicleId(2);
		f.save();

		f = new FillUp();
		f.setAmount(10);
		f.setComment("Second");
		f.setCost(3.199);
		f.setOdometer(200);
		f.setVehicleId(1);
		long id_real = f.save();

		f = new FillUp(id_real);
		FillUp previous = f.getPrevious();

		assertEquals(previous.getId(), id);
	}

	public void testNext() {
		FillUp f = new FillUp();
		f.setAmount(10);
		f.setComment("First");
		f.setCost(3.099);
		f.setOdometer(100);
		f.setVehicleId(1);
		long id = f.save();

		f = new FillUp();
		f.setAmount(10);
		f.setComment("Second (fake)");
		f.setCost(3.199);
		f.setOdometer(100);
		f.setVehicleId(2);
		f.save();

		f = new FillUp();
		f.setAmount(10);
		f.setComment("Second");
		f.setCost(3.199);
		f.setOdometer(200);
		f.setVehicleId(1);
		long id_real = f.save();

		f = new FillUp(id);
		FillUp next = f.getNext();

		assertEquals(next.getId(), id_real);
	}
}
