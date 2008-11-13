package com.evancharlton.mileage;

public abstract class CalculationEngine {
	abstract public double calculateEconomy(double distance, double fuel);

	abstract public String getEconomyUnits();

	abstract public double getWorstEconomy();

	abstract public double getBestEconomy();

	abstract public String getVolumeUnits();

	abstract public String getVolumeUnitsAbbr();

	abstract public String getDistanceUnits();

	abstract public String getDistanceUnitsAbbr();

	/**
	 * See if one is better than two.
	 * 
	 * @param economy_one
	 * @param economy_two
	 * @return
	 */
	abstract public boolean better(double economy_one, double economy_two);

	/**
	 * Is one worse than two?
	 * 
	 * @param one
	 * @param two
	 * @return
	 */
	abstract public boolean worse(double one, double two);

	abstract public String help();
}
