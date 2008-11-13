package com.evancharlton.mileage.calculators;

public class GallonsKilometersToLPKCalculationEngine extends MetricCalculationEngine {
	@Override
	public double calculateEconomy(double distance, double fuel) {
		fuel = gallonsToLitres(fuel);
		return super.calculateEconomy(distance, fuel);
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
		return "This system expects:\n fuel: gallons\n odometer: kilometers\n economy: litres / 100 km";
	}
}
