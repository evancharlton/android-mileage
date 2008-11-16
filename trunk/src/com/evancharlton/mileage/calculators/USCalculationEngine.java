package com.evancharlton.mileage.calculators;

public class USCalculationEngine extends CalculationEngine {
	public double calculateEconomy(double distance, double fuel) {
		return distance / fuel;
	}

	public double getWorstEconomy() {
		return 0.0D;
	}

	public double getBestEconomy() {
		return Double.MAX_VALUE;
	}

	public boolean better(double one, double two) {
		return one > two;
	}

	public boolean worse(double a, double b) {
		return a < b;
	}

	public String getEconomyUnits() {
		return " mpg";
	}

	public String getVolumeUnits() {
		return " Gallons";
	}

	public String getVolumeUnitsAbbr() {
		return " gal";
	}

	public String getDistanceUnits() {
		return " miles";
	}

	public String getDistanceUnitsAbbr() {
		return " mi";
	}
}
