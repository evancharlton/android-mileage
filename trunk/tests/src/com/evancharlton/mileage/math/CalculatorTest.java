package com.evancharlton.mileage.math;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.MatrixCursor;

import com.evancharlton.mileage.dao.FillupSeries;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.provider.tables.FillupsTable;
import com.evancharlton.mileage.tests.TestCase;

public class CalculatorTest extends TestCase {
	private void convert(double from, int fromUnits, double to, int toUnits) {
		assertCloseEnough(to, Calculator.convert(from, fromUnits, toUnits));
	}

	// distance tests

	public void testConvertMilesToKilometers() {
		convert(1.0D, Calculator.MI, 1.609D, Calculator.KM);
	}

	public void testConvertKilometersToMiles() {
		convert(1.0D, Calculator.KM, 0.621D, Calculator.MI);
	}

	// volume tests

	public void testConvertGallonsToLitres() {
		convert(1.0D, Calculator.GALLONS, 3.785, Calculator.LITRES);
	}

	public void testConvertGallonsToImperialGallons() {
		convert(1.0D, Calculator.GALLONS, 0.832D, Calculator.IMPERIAL_GALLONS);
	}

	public void testConvertLitresToGallons() {
		convert(1.0D, Calculator.LITRES, 0.264D, Calculator.GALLONS);
	}

	public void testConvertLitresToImperialGallons() {
		convert(1.0D, Calculator.LITRES, 0.219D, Calculator.IMPERIAL_GALLONS);
	}

	public void testConvertImperialGallonsToGallons() {
		convert(1.0D, Calculator.IMPERIAL_GALLONS, 1.201D, Calculator.GALLONS);
	}

	public void testConvertImperialGallonsToLitres() {
		convert(1.0D, Calculator.IMPERIAL_GALLONS, 4.546D, Calculator.LITRES);
	}

	// fuel economy tests

	public void testConvertMpgToMpl() {
		convert(1, Calculator.MI_PER_GALLON, 0.264, Calculator.MI_PER_LITRE);
	}

	public void testConvertMpgToKpl() {
		convert(1, Calculator.MI_PER_GALLON, 0.425, Calculator.KM_PER_LITRE);
	}

	public void testConvertMpgToMpIg() {
		convert(1, Calculator.MI_PER_GALLON, 1.201, Calculator.MI_PER_IMP_GALLON);
	}

	public void testConvertMpgToKpIg() {
		convert(1, Calculator.MI_PER_GALLON, 1.932, Calculator.KM_PER_IMP_GALLON);
	}

	public void testConvertMpgToKpg() {
		convert(1, Calculator.MI_PER_GALLON, 1.609, Calculator.KM_PER_GALLON);
	}

	public void testConvertMpgToGpck() {
		convert(1, Calculator.MI_PER_GALLON, 62.137, Calculator.GALLONS_PER_100KM);
	}

	public void testConvertMpgToLpck() {
		convert(1, Calculator.MI_PER_GALLON, 235.214, Calculator.LITRES_PER_100KM);
	}

	public void testConvertMpgToIgpck() {
		convert(1, Calculator.MI_PER_GALLON, 51.739, Calculator.IMP_GAL_PER_100KM);
	}

	// TODO: test from -> to all unit combinations. Due to the current
	// underlying algorithm, this is sufficient to ensure correctness.
	public void testConvertKpgToMpg() {
		convert(1, Calculator.KM_PER_GALLON, 0.621, Calculator.MI_PER_GALLON);
	}

	public void testConvertMpigToMpg() {
		convert(1, Calculator.MI_PER_IMP_GALLON, 0.833, Calculator.MI_PER_GALLON);
	}

	public void testConvertKpigToMpg() {
		convert(1, Calculator.KM_PER_IMP_GALLON, 0.517, Calculator.MI_PER_GALLON);
	}

	public void testConvertMplToMpg() {
		convert(1, Calculator.MI_PER_LITRE, 3.785, Calculator.MI_PER_GALLON);
	}

	public void testConvertKplToMpg() {
		convert(1, Calculator.KM_PER_LITRE, 2.352, Calculator.MI_PER_GALLON);
	}

	public void testConvertGpckToMpg() {
		convert(1, Calculator.GALLONS_PER_100KM, 62.137, Calculator.MI_PER_GALLON);
	}

	public void testConvertLpckToMpg() {
		convert(1, Calculator.LITRES_PER_100KM, 235.214, Calculator.MI_PER_GALLON);
	}

	public void testConvertIgpckToMpg() {
		convert(1, Calculator.IMP_GAL_PER_100KM, 51.739, Calculator.MI_PER_GALLON);
	}

	// test the calculation of the fuel economy

	private static final String[] PROJECTION = FillupsTable.PROJECTION;
	private static int ID = 1;

	private static Object[] createFillup(int vehicleId, double odometer, double volume, double price, boolean partial, boolean restart) {
		Object[] values = new Object[PROJECTION.length];
		int i = 0;
		values[i++] = ID++;
		values[i++] = volume * price;
		values[i++] = price;
		values[i++] = volume;
		values[i++] = odometer;
		values[i++] = 0; // economy
		values[i++] = vehicleId;
		values[i++] = System.currentTimeMillis();
		values[i++] = 0; // latitude
		values[i++] = 0; // longitude
		values[i++] = partial ? 1 : 0;
		values[i++] = restart ? 1 : 0;
		return values;
	}

	@Override
	public void setUp() {
		ID = 1;
	}

	public void testAverageEconomyMPG() {
		MatrixCursor cursor = new MatrixCursor(FillupsTable.PROJECTION);
		int odometer = 0;
		odometer += 100;
		cursor.addRow(createFillup(1, odometer, 10, 10, false, false));
		odometer += 100;
		cursor.addRow(createFillup(1, odometer, 10, 10, false, false));

		ArrayList<FillupSeries> series = FillupSeries.load(cursor);
		FillupSeries underTest = series.get(0);

		ContentValues values = new ContentValues();
		values.put(Vehicle.PREF_DISTANCE_UNITS, Calculator.MI);
		values.put(Vehicle.PREF_VOLUME_UNITS, Calculator.GALLONS);
		values.put(Vehicle.PREF_ECONOMY_UNITS, Calculator.MI_PER_GALLON);
		Vehicle vehicle = new Vehicle(values);
		assertEquals(10D, Calculator.averageEconomy(vehicle, underTest));
	}

	public void testAverageEconomyLPCK() {
		MatrixCursor cursor = new MatrixCursor(FillupsTable.PROJECTION);
		int odometer = 0;
		odometer += 100;
		cursor.addRow(createFillup(1, odometer, 10, 10, false, false));
		odometer += 100;
		cursor.addRow(createFillup(1, odometer, 10, 10, false, false));

		ArrayList<FillupSeries> series = FillupSeries.load(cursor);
		FillupSeries underTest = series.get(0);

		ContentValues values = new ContentValues();
		values.put(Vehicle.PREF_DISTANCE_UNITS, Calculator.KM);
		values.put(Vehicle.PREF_VOLUME_UNITS, Calculator.LITRES);
		values.put(Vehicle.PREF_ECONOMY_UNITS, Calculator.LITRES_PER_100KM);
		Vehicle vehicle = new Vehicle(values);
		assertEquals(10D, Calculator.averageEconomy(vehicle, underTest));
	}
}
