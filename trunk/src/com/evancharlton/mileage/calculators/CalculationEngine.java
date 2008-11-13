package com.evancharlton.mileage.calculators;

public abstract class CalculationEngine {
	public double litresToGallons(double amount) {
		return amount *= 0.264172052; // number of gallons per litre
	}

	public double gallonsToLitres(double amount) {
		return amount *= 3.78541178; // number of litres per gallon
	}

	public double milesToKM(double amount) {
		return amount *= 1.609344; // number of kilometers per mile
	}

	public double kmToMiles(double amount) {
		return amount *= 0.621371192; // number of miles per kilometer
	}

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
