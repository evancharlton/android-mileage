package com.evancharlton.mileage.tests;

public class TestCase extends junit.framework.TestCase {
	protected void assertCloseEnough(double expected, double actual) {
		assertCloseEnough(expected, actual, 0.001);
	}

	protected void assertCloseEnough(double expected, double actual, double delta) {
		assertTrue(Math.abs(expected - actual) < delta);
	}
}
