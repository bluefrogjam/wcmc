/*
 * Created on Sep 26, 2005
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.date;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;

/**
 * converts the ucdavis sample date
 * 
 * @author wohlgemuth
 * 
 */
public class UCSampleDate extends SampleDate {
	private Logger logger = LoggerFactory.getLogger(getClass());
	/**
	 * DOCUMENT ME!
	 */
	public static final String PATTERN_SHORT = "[0-9][0-9][0-9][0-9][0-9][0-9][a-zA-Z][a-zA-Z][a-zA-Z][a-zA-Z][0-9]_[0-9]";

	/**
	 * DOCUMENT ME!
	 */
	public static final String PATTERN_SHORT_2 = "[0-9][0-9][0-9][0-9][0-9][0-9][a-zA-Z][a-zA-Z][0-9][0-9]_[0-9]";

	/**
	 * DOCUMENT ME!
	 */
	public static final String PATTERN_LONG = "[0-9][0-9][0-9][0-9][0-9][0-9][a-zA-Z][a-zA-Z][a-zA-Z][a-zA-Z][0-9][0-9]_[0-9]";

	/**
	 * DOCUMENT ME!
	 */
	public static final String PATTERN_LONG_2 = "[0-9][0-9][0-9][0-9][0-9][0-9][a-zA-Z][a-zA-Z][a-zA-Z][a-zA-Z][a-zA-Z][0-9][0-9]_[0-9]";

	public static final String PATTERN_LONG_3 = "[0-9][0-9][0-9][0-9][0-9][0-9][a-zA-Z][a-zA-Z][a-zA-Z][a-zA-Z][a-zA-Z][0-9][0-9][0-9]_[0-9]";

	public static final String PATTERN_SHORT_3 = "[0-9][0-9][0-9][0-9][0-9][0-9][a-zA-Z][a-zA-Z][0-9][0-9][0-9]_[0-9]";

	public static final String PATTERN_LONG_4 = "[0-9][0-9][0-9][0-9][0-9][0-9][a-zA-Z][a-zA-Z][a-zA-Z][a-zA-Z][a-zA-Z][0-9][0-9][0-9][0-9]_[0-9]";

	public static final String PATTERN_SHORT_4 = "[0-9][0-9][0-9][0-9][0-9][0-9][a-zA-Z][a-zA-Z][0-9][0-9][0-9][0-9]_[0-9]";

	public static final String PATTERN_LONG_5 = "[0-9][0-9][0-9][0-9][0-9][0-9][a-zA-Z][a-zA-Z][a-zA-Z][a-zA-Z][0-9][0-9][0-9][0-9]_[0-9]";

	/**
	 * DOCUMENT ME!
	 */
	private Date date;

	/**
	 * DOCUMENT ME!
	 */
	private int number;

	private String operator;

	private String machine;

	private int runNumber;

	/**
	 * Creates a new UCSampleDate object.
	 * 
	 * @param sample
	 *            DOCUMENT ME!
	 */
	public UCSampleDate(String sample) {
		if (sample.matches(PATTERN_SHORT)) {
			// logger.debug("using pattern short " + sample);
			int year = Integer.parseInt("20" + sample.substring(0, 2));
			int day = Integer.parseInt(sample.substring(4, 6));
			int month = Integer.parseInt(sample.substring(2, 4));

			number = Integer.parseInt(sample.substring(10, 11));

			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.DAY_OF_MONTH, day);
			c.set(Calendar.MONTH, month - 1);

			operator = sample.substring(7, 9);
			machine = sample.substring(6, 7);
			runNumber = Integer.parseInt(sample.substring(12));

			// logger.debug(operator + " - " + machine + " - " + runNumber);
			date = c.getTime();
		} else if (sample.matches(PATTERN_SHORT_2)) {

			int year = Integer.parseInt("20" + sample.substring(0, 2));
			int day = Integer.parseInt(sample.substring(4, 6));
			int month = Integer.parseInt(sample.substring(2, 4));

			number = Integer.parseInt(sample.substring(8, 10));

			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.DAY_OF_MONTH, day);
			c.set(Calendar.MONTH, month - 1);

			date = c.getTime();

			operator = sample.substring(6, 7);
			machine = sample.substring(7, 8);
			runNumber = Integer.parseInt(sample.substring(11));

		} else if (sample.matches(PATTERN_SHORT_3)) {
			int year = Integer.parseInt("20" + sample.substring(0, 2));
			int day = Integer.parseInt(sample.substring(4, 6));
			int month = Integer.parseInt(sample.substring(2, 4));

			number = Integer.parseInt(sample.substring(8, 11));

			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.DAY_OF_MONTH, day);
			c.set(Calendar.MONTH, month - 1);

			date = c.getTime();
		} else if (sample.matches(PATTERN_SHORT_4)) {
			int year = Integer.parseInt("20" + sample.substring(0, 2));
			int day = Integer.parseInt(sample.substring(4, 6));
			int month = Integer.parseInt(sample.substring(2, 4));

			number = Integer.parseInt(sample.substring(8, 12));

			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.DAY_OF_MONTH, day);
			c.set(Calendar.MONTH, month - 1);

			date = c.getTime();

			operator = "?";
			machine = "?";
			runNumber = Integer.parseInt(sample.substring(13));
		}

		else if (sample.matches(PATTERN_LONG)) {

			int year = Integer.parseInt("20" + sample.substring(0, 2));
			int day = Integer.parseInt(sample.substring(4, 6));
			int month = Integer.parseInt(sample.substring(2, 4));

			number = Integer.parseInt(sample.substring(10, 12));

			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.DAY_OF_MONTH, day);
			c.set(Calendar.MONTH, month - 1);

			date = c.getTime();

			operator = sample.substring(7, 9);
			machine = sample.substring(6, 7);
			runNumber = Integer.parseInt(sample.substring(13));

		} else if (sample.matches(PATTERN_LONG_2)) {

			int year = Integer.parseInt("20" + sample.substring(0, 2));
			int day = Integer.parseInt(sample.substring(4, 6));
			int month = Integer.parseInt(sample.substring(2, 4));

			number = Integer.parseInt(sample.substring(11, 13));

			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.DAY_OF_MONTH, day);
			c.set(Calendar.MONTH, month - 1);

			date = c.getTime();

			operator = sample.substring(7, 9);
			machine = sample.substring(6, 7);
			runNumber = Integer.parseInt(sample.substring(14));
		} else if (sample.matches(PATTERN_LONG_3)) {
			int year = Integer.parseInt("20" + sample.substring(0, 2));
			int day = Integer.parseInt(sample.substring(4, 6));
			int month = Integer.parseInt(sample.substring(2, 4));

			number = Integer.parseInt(sample.substring(11, 14));

			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.DAY_OF_MONTH, day);
			c.set(Calendar.MONTH, month - 1);

			date = c.getTime();

			operator = sample.substring(7, 9);
			machine = sample.substring(6, 7);
			runNumber = Integer.parseInt(sample.substring(15));
		} else if (sample.matches(PATTERN_LONG_4)) {

			int year = Integer.parseInt("20" + sample.substring(0, 2));
			int day = Integer.parseInt(sample.substring(4, 6));
			int month = Integer.parseInt(sample.substring(2, 4));

			number = Integer.parseInt(sample.substring(11, 15));

			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.DAY_OF_MONTH, day);
			c.set(Calendar.MONTH, month - 1);

			date = c.getTime();
			operator = sample.substring(7, 9);
			machine = sample.substring(6, 7);
			runNumber = Integer.parseInt(sample.substring(16));
		} else if (sample.matches(PATTERN_LONG_5)) {
			logger.debug("using pattern " + sample);

			int year = Integer.parseInt("20" + sample.substring(0, 2));
			int day = Integer.parseInt(sample.substring(4, 6));
			int month = Integer.parseInt(sample.substring(2, 4));

			number = Integer.parseInt(sample.substring(10, 14));

			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.DAY_OF_MONTH, day);
			c.set(Calendar.MONTH, month - 1);

			date = c.getTime();
			
			date = c.getTime();
			operator = sample.substring(7, 9);
			machine = sample.substring(6, 7);
			runNumber = Integer.parseInt(sample.substring(15));

			logger.debug(operator + " - " + machine + " - " + runNumber);

		}

		else {
			throw new PatternException("pattern didn't match this sample: "
					+ sample);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public int getNumberOfDay() {
		return number;
	}

	@Override
	public String getMachine() {
		return machine;
	}

	@Override
	public String getOperator() {
		return operator;
	}

	@Override
	public int getRunNumber() {
		return runNumber;
	}
}
