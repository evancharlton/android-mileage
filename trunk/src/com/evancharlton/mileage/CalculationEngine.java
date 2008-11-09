package com.evancharlton.mileage;

public abstract class CalculationEngine {
	abstract double calculateEconomy(double distance, double fuel);

	abstract String getEconomyUnits();

	abstract String getVolumeUnits();

	abstract String getVolumeUnitsAbbr();

	/**
	 * See if one is better than two.
	 * 
	 * @param economy_one
	 * @param economy_two
	 * @return
	 */
	abstract boolean better(double economy_one, double economy_two);

	/**
	 * Is one worse than two?
	 * 
	 * @param one
	 * @param two
	 * @return
	 */
	abstract boolean worse(double one, double two);
}
