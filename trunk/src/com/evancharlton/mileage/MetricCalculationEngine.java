package com.evancharlton.mileage;

public class MetricCalculationEngine extends CalculationEngine {
	public double calculateEconomy(double distance, double fuel) {
		return 100D * (fuel / distance);
	}

	public String getEconomyUnits() {
		return "l/100km";
	}

	public String getVolumeUnits() {
		return "Litres";
	}

	public String getVolumeUnitsAbbr() {
		return "L";
	}

	public boolean better(double economy_one, double economy_two) {
		return economy_one < economy_two;
	}

	public boolean worse(double economy_one, double economy_two) {
		return economy_one > economy_two;
	}
}
