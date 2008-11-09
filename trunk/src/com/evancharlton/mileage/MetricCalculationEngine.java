package com.evancharlton.mileage;

public class MetricCalculationEngine extends CalculationEngine {
	public double calculateEconomy(double distance, double fuel) {
		return 100D * (fuel / distance);
	}

	public double getWorstEconomy() {
		return Double.MAX_VALUE;
	}

	public double getBestEconomy() {
		return 0.0D;
	}

	public String getEconomyUnits() {
		return " L/100km";
	}

	public String getVolumeUnits() {
		return " Litres";
	}

	public String getVolumeUnitsAbbr() {
		return " L";
	}

	public String getDistanceUnits() {
		return " Kilometers";
	}

	public String getDistanceUnitsAbbr() {
		return " K";
	}

	public boolean better(double economy_one, double economy_two) {
		return economy_one < economy_two;
	}

	public boolean worse(double economy_one, double economy_two) {
		return economy_one > economy_two;
	}
}
