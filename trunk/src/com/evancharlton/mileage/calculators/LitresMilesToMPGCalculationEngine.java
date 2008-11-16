package com.evancharlton.mileage.calculators;

public class LitresMilesToMPGCalculationEngine extends USCalculationEngine {
	@Override
	public double calculateEconomy(double distance, double fuel) {
		fuel = litresToGallons(fuel);
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
		return " Litres";
	}

	@Override
	public String getVolumeUnitsAbbr() {
		return " L";
	}
}
