package me.drawethree.ultraprisoncore.utils;

import me.lucko.helper.text3.Text;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProgressBarTest {

	@Test
	public void success_no_progress() {
		String delimeter = ":";
		int numberOfDelimeters = 20;
		double required = 41412199.0;
		double current = 0.0;

		StringBuilder expected = new StringBuilder();
		for (int i = 0; i < numberOfDelimeters; i++) {
			expected.append(ProgressBar.NOT_AVAILABLE_COLOR).append(delimeter);
		}

		String actual = ProgressBar.getProgressBar(numberOfDelimeters, delimeter, current, required);

		assertEquals(Text.colorize(expected.toString()), actual);
	}

	@Test
	public void success_100_percent() {
		String delimeter = ":";
		int numberOfDelimeters = 20;
		double required = 1.0;
		double current = 1.0;

		StringBuilder expected = new StringBuilder();
		for (int i = 0; i < numberOfDelimeters; i++) {
			expected.append(ProgressBar.AVAILABLE_COLOR).append(delimeter);
		}

		String actual = ProgressBar.getProgressBar(numberOfDelimeters, delimeter, current, required);

		assertEquals(Text.colorize(expected.toString()), actual);
	}

	@Test
	public void success_no_delimeter() {

		int numberOfDelimeters = 20;
		double required = 428919.0;
		double current = 4244.0;

		double treshold = required / numberOfDelimeters;
		int numberOfGreens = (int) (current / treshold);

		StringBuilder expected = new StringBuilder();

		for (int i = 0; i < numberOfGreens; i++) {
			expected.append(ProgressBar.AVAILABLE_COLOR).append(ProgressBar.DEFAULT_DELIMITER);
		}
		for (int i = 0; i < numberOfDelimeters - numberOfGreens; i++) {
			expected.append(ProgressBar.NOT_AVAILABLE_COLOR).append(ProgressBar.DEFAULT_DELIMITER);
		}

		String actual = ProgressBar.getProgressBar(numberOfDelimeters, null, current, required);
		assertEquals(Text.colorize(expected.toString()), actual);
	}


	@Test
	public void success_50_percent() {
		String delimeter = "|";
		int numberOfDelimeters = 50;
		double required = 41851429.0;
		double current = required / 2.0;

		double treshold = required / numberOfDelimeters;
		int numberOfGreens = (int) (current / treshold);

		StringBuilder expected = new StringBuilder();

		for (int i = 0; i < numberOfGreens; i++) {
			expected.append(ProgressBar.AVAILABLE_COLOR).append(delimeter);
		}
		for (int i = 0; i < numberOfDelimeters - numberOfGreens; i++) {
			expected.append(ProgressBar.NOT_AVAILABLE_COLOR).append(delimeter);
		}

		String actual = ProgressBar.getProgressBar(numberOfDelimeters, delimeter, current, required);
		assertEquals(Text.colorize(expected.toString()), actual);
	}

	@Test
	public void success_almost_50_percent() {
		String delimeter = "|";
		int numberOfDelimeters = 50;
		double required = 41904129.0;
		double current = (required / 2.0) - 1.0;

		double treshold = required / numberOfDelimeters;
		int numberOfGreens = (int) (current / treshold);

		StringBuilder expected = new StringBuilder();

		for (int i = 0; i < numberOfGreens; i++) {
			expected.append(ProgressBar.AVAILABLE_COLOR).append(delimeter);
		}
		for (int i = 0; i < numberOfDelimeters - numberOfGreens; i++) {
			expected.append(ProgressBar.NOT_AVAILABLE_COLOR).append(delimeter);
		}

		String actual = ProgressBar.getProgressBar(numberOfDelimeters, delimeter, current, required);
		assertEquals(Text.colorize(expected.toString()), actual);
	}
}