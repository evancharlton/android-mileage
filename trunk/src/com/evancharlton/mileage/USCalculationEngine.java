package com.evancharlton.mileage;

public class USCalculationEngine extends CalculationEngine {
	public double calculateEconomy(double distance, double fuel) {
		return distance / fuel;
	}

	public boolean better(double one, double two) {
		return one > two;
	}

	public boolean worse(double one, double two) {
		return one < two;
	}

	public String getEconomyUnits() {
		return "mpg";
	}

	public String getVolumeUnits() {
		return "Gallons";
	}

	public String getVolumeUnitsAbbr() {
		return "gal";
	}
}
