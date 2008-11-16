package com.evancharlton.mileage.calculators;

public class LitreToMileCalculationEngine extends USCalculationEngine {
	/**
	 * Calculate the economy in miles per gallon, but with fuel in litres and
	 * distance in kilometers
	 */
	@Override
	public double calculateEconomy(double distance, double fuel) {
		distance = kmToMiles(distance);
		fuel = litresToGallons(fuel);
		return super.calculateEconomy(distance, fuel);
	}

	@Override
	public String getDistanceUnits() {
		return " Kilometers";
	}

	@Override
	public String getDistanceUnitsAbbr() {
		return " K";
	}

	@Override
	public String getVolumeUnits() {
		return " Litres";
	}

	@Override
	public String getVolumeUnitsAbbr() {
		return " L";
	}
}
