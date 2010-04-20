package com.evancharlton.mileage.tests;

import junit.framework.AssertionFailedError;

public class TestCase extends junit.framework.TestCase {
	protected void assertCloseEnough(double expected, double actual) {
		assertCloseEnough(expected, actual, 0.001);
	}

	protected void assertCloseEnough(double expected, double actual, double delta) {
		try {
			assertTrue(Math.abs(expected - actual) < delta);
		} catch (AssertionFailedError e) {
			throw new AssertionFailedError("Expected <" + expected + " +/- " + delta + "> but was <" + actual + ">");
		}
	}
}
