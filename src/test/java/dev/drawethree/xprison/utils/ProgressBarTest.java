package dev.drawethree.xprison.utils;


import org.junit.jupiter.api.Test;

import static dev.drawethree.xprison.utils.misc.ProgressBar.getProgressBar;
import static dev.drawethree.xprison.utils.text.TextUtils.applyColor;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProgressBarTest {

	@Test
	public void success_no_progress() {
		String delimeter = ":";
		int numberOfDelimeters = 20;
		double required = 41412199.0;
		double current = 0.0;

		String expected = "&a&c::::::::::::::::::::";
		String actual = getProgressBar(numberOfDelimeters, delimeter, current, required);

		assertEquals(applyColor(expected), actual);
	}

	@Test
	public void success_100_percent() {
		String delimeter = ":";
		int numberOfDelimeters = 20;
		double required = 1.0;
		double current = 1.0;


		String expected = "&a::::::::::::::::::::&c";
		String actual = getProgressBar(numberOfDelimeters, delimeter, current, required);

		assertEquals(applyColor(expected), actual);
	}

	@Test
	public void success_no_delimeter() {

		int numberOfDelimeters = 20;
		double required = 428919.0;
		double current = 4244.0;

		String expected = "&a&c::::::::::::::::::::";
		String actual = getProgressBar(numberOfDelimeters, null, current, required);
		assertEquals(applyColor(expected), actual);
	}


	@Test
	public void success_50_percent() {
		String delimeter = "|";
		int numberOfDelimeters = 50;
		double required = 41851429.0;
		double current = required / 2.0;


		String expected = "&a|||||||||||||||||||||||||&c|||||||||||||||||||||||||";

		String actual = getProgressBar(numberOfDelimeters, delimeter, current, required);
		assertEquals(applyColor(expected), actual);
	}

	@Test
	public void success_almost_50_percent() {
		String delimeter = "|";
		int numberOfDelimeters = 50;
		double required = 41904129.0;
		double current = (required / 2.0) - 1.0;


		String expected = "&a||||||||||||||||||||||||&c||||||||||||||||||||||||||";

		String actual = getProgressBar(numberOfDelimeters, delimeter, current, required);
		assertEquals(applyColor(expected), actual);
	}
}