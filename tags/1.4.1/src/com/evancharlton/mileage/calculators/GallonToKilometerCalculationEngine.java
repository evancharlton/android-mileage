package com.evancharlton.mileage.calculators;

public class GallonToKilometerCalculationEngine extends MetricCalculationEngine {

	/**
	 * Calculate the economy in litres per 100km, but with fuel in gallons and
	 * distance in miles
	 */
	@Override
	public double calculateEconomy(double distance, double fuel) {
		distance = milesToKM(distance);
		fuel = gallonsToLitres(fuel);
		return super.calculateEconomy(distance, fuel);
	}

	@Override
	public String getDistanceUnits() {
		return " Miles";
	}

	@Override
	public String getDistanceUnitsAbbr() {
		return " Mi";
	}

	@Override
	public String getVolumeUnits() {
		return " Gallons";
	}

	@Override
	public String getVolumeUnitsAbbr() {
		return " Gal";
	}

	public String help() {
		return "This system expects:\n fuel: gallons\n odometer: miles\n economy: litres / 100 km";
	}
}
