package com.evancharlton.mileage.calculators;

import junit.framework.TestCase;

import com.evancharlton.mileage.PreferencesProvider;

public class CalculationEngineTest extends TestCase {
	private CalculationEngine ce;

	public void setUp() {
		ce = new CalculationEngine();
	}

	public void testMilesPerGallon() throws Exception {
		ce.setEconomy(PreferencesProvider.MI_PER_GALLON);

		ce.setInputDistance(PreferencesProvider.MILES);
		ce.setInputVolume(PreferencesProvider.GALLONS);
		assertCloseEnough(10, ce.calculateEconomy(100, 10));

		ce.setInputDistance(PreferencesProvider.KILOMETERS);
		ce.setInputVolume(PreferencesProvider.GALLONS);
		assertCloseEnough(6.21371192, ce.calculateEconomy(100, 10));

		ce.setInputDistance(PreferencesProvider.MILES);
		ce.setInputVolume(PreferencesProvider.LITRES);
		assertCloseEnough(37.8541178, ce.calculateEconomy(100, 10));

		ce.setInputDistance(PreferencesProvider.KILOMETERS);
		ce.setInputVolume(PreferencesProvider.LITRES);
		assertCloseEnough(23.5214583, ce.calculateEconomy(100, 10));

		ce.setInputDistance(PreferencesProvider.MILES);
		ce.setInputVolume(PreferencesProvider.IMP_GALLONS);
		assertCloseEnough(8.3267384, ce.calculateEconomy(100, 10));

		ce.setInputDistance(PreferencesProvider.KILOMETERS);
		ce.setInputVolume(PreferencesProvider.IMP_GALLONS);
		assertCloseEnough(5.17399537, ce.calculateEconomy(100, 10));
	}

	public void testMilesPerLitre() throws Exception {
		ce.setEconomy(PreferencesProvider.MI_PER_LITRE);

		ce.setInputDistance(PreferencesProvider.MILES);
		ce.setInputVolume(PreferencesProvider.GALLONS);
		assertCloseEnough(2.64172052, ce.calculateEconomy(100, 10));

		ce.setInputDistance(PreferencesProvider.KILOMETERS);
		ce.setInputVolume(PreferencesProvider.GALLONS);
		assertCloseEnough(1.64148903, ce.calculateEconomy(100, 10));

		ce.setInputDistance(PreferencesProvider.MILES);
		ce.setInputVolume(PreferencesProvider.LITRES);
		assertCloseEnough(10, ce.calculateEconomy(100, 10));

		ce.setInputDistance(PreferencesProvider.KILOMETERS);
		ce.setInputVolume(PreferencesProvider.LITRES);
		assertCloseEnough(6.21371192, ce.calculateEconomy(100, 10));

		ce.setInputDistance(PreferencesProvider.MILES);
		ce.setInputVolume(PreferencesProvider.IMP_GALLONS);
		assertCloseEnough(2.19969157, ce.calculateEconomy(100, 10));

		ce.setInputDistance(PreferencesProvider.KILOMETERS);
		ce.setInputVolume(PreferencesProvider.IMP_GALLONS);
		assertCloseEnough(1.36682498, ce.calculateEconomy(100, 10));
	}

	public void testMilesPerImperialGallon() throws Exception {
		ce.setEconomy(PreferencesProvider.MI_PER_IMP_GALLON);

		ce.setInputDistance(PreferencesProvider.MILES);
		ce.setInputVolume(PreferencesProvider.GALLONS);
		assertCloseEnough(12.0095042, ce.calculateEconomy(100, 10));

		ce.setInputDistance(PreferencesProvider.KILOMETERS);
		ce.setInputVolume(PreferencesProvider.GALLONS);
		assertCloseEnough(7.46235995, ce.calculateEconomy(100, 10));

		ce.setInputDistance(PreferencesProvider.MILES);
		ce.setInputVolume(PreferencesProvider.LITRES);
		assertCloseEnough(45.4609188, ce.calculateEconomy(100, 10));

		ce.setInputDistance(PreferencesProvider.KILOMETERS);
		ce.setInputVolume(PreferencesProvider.LITRES);
		assertCloseEnough(28.2481053, ce.calculateEconomy(100, 10));

		ce.setInputDistance(PreferencesProvider.MILES);
		ce.setInputVolume(PreferencesProvider.IMP_GALLONS);
		assertCloseEnough(10, ce.calculateEconomy(100, 10));

		ce.setInputDistance(PreferencesProvider.KILOMETERS);
		ce.setInputVolume(PreferencesProvider.IMP_GALLONS);
		assertCloseEnough(6.21371192, ce.calculateEconomy(100, 10));
	}

	public void testKilometersPerGallon() throws Exception {
		ce.setEconomy(PreferencesProvider.KM_PER_GALLON);

		ce.setInputDistance(PreferencesProvider.MILES);
		ce.setInputVolume(PreferencesProvider.GALLONS);
		assertCloseEnough(16.09344, ce.calculateEconomy(100, 10));

		ce.setInputDistance(PreferencesProvider.KILOMETERS);
		ce.setInputVolume(PreferencesProvider.GALLONS);
		assertCloseEnough(10, ce.calculateEconomy(100, 10));

		ce.setInputDistance(PreferencesProvider.MILES);
		ce.setInputVolume(PreferencesProvider.LITRES);
		assertCloseEnough(60.9202974, ce.calculateEconomy(100, 10));

		ce.setInputDistance(PreferencesProvider.KILOMETERS);
		ce.setInputVolume(PreferencesProvider.LITRES);
		assertCloseEnough(37.8541178, ce.calculateEconomy(100, 10));

		ce.setInputDistance(PreferencesProvider.MILES);
		ce.setInputVolume(PreferencesProvider.IMP_GALLONS);
		assertCloseEnough(13.4005865, ce.calculateEconomy(100, 10));

		ce.setInputDistance(PreferencesProvider.KILOMETERS);
		ce.setInputVolume(PreferencesProvider.IMP_GALLONS);
		assertCloseEnough(8.3267384, ce.calculateEconomy(100, 10));
	}

	private void assertCloseEnough(double expected, double actual) {
		assertTrue(Math.abs(expected - actual) < 0.01);
	}
}
