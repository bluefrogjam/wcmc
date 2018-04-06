package edu.ucdavis.genomics.metabolomics.binbase.algorythm.date;

import java.util.Calendar;
import java.util.Date;

/**
 * utiltiy to compare dates
 * 
 * @author wohlgemuth
 */
public class DateUtil {

	/**
	 * checks if two dates are from the same day
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean isSameDay(Date a, Date b) {

		Calendar c = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c.setTime(a);
		c2.setTime(b);

		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.HOUR, 0);

		c2.set(Calendar.MILLISECOND, 0);
		c2.set(Calendar.SECOND, 0);
		c2.set(Calendar.MINUTE, 0);
		c2.set(Calendar.HOUR, 0);

		if (c2.get(Calendar.YEAR) == c.get(Calendar.YEAR)) {
			if (c2.get(Calendar.MONTH) == c.get(Calendar.MONTH)) {
				if (c2.get(Calendar.DAY_OF_MONTH) == c
						.get(Calendar.DAY_OF_MONTH)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * strips the time of the data
	 * 
	 * @param date
	 * @return
	 */
	public static Date stripTime(Date date) {
		Calendar c = Calendar.getInstance();

		c.setTime(date);

		Calendar r = Calendar.getInstance();
		r.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
		r.set(Calendar.MONTH, c.get(Calendar.MONTH));
		r.set(Calendar.YEAR, c.get(Calendar.YEAR));
		r.set(Calendar.MINUTE, 0);
		r.set(Calendar.HOUR, 0);
		r.set(Calendar.SECOND, 0);
		r.set(Calendar.MILLISECOND, 0);

		return r.getTime();
	}

	/**
	 * set's the time to the day begin
	 * 
	 * @param date
	 * @return
	 */
	public static Date setTimeToDayBegin(Date date) {
		Calendar c1 = Calendar.getInstance();
		c1.setTime(date);

		c1.set(Calendar.AM_PM, Calendar.AM);
		c1.set(Calendar.HOUR, c1.getActualMinimum(Calendar.HOUR));
		c1.set(Calendar.MINUTE, c1.getActualMinimum(Calendar.MINUTE));
		c1.set(Calendar.SECOND, c1.getActualMinimum(Calendar.SECOND));
		c1.set(Calendar.MILLISECOND, c1.getActualMinimum(Calendar.MILLISECOND));

		return c1.getTime();

	}

	/**
	 * sets the time to the latest possible time for a day
	 * 
	 * @param date
	 * @return
	 */
	public static Date setTimeToDayEnd(Date date) {
		Calendar c2 = Calendar.getInstance();
		c2.setTime(date);

		c2.set(Calendar.AM_PM, Calendar.PM);
		c2.set(Calendar.HOUR, c2.getActualMaximum(Calendar.HOUR));
		c2.set(Calendar.MINUTE, c2.getActualMaximum(Calendar.MINUTE));
		c2.set(Calendar.SECOND, c2.getActualMaximum(Calendar.SECOND));
		c2.set(Calendar.MILLISECOND, c2.getActualMaximum(Calendar.MILLISECOND));

		return c2.getTime();
	}

}
