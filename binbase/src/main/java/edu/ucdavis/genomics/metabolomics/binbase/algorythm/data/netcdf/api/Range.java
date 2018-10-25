package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.api;

import java.util.Comparator;


public class Range {
	private double min, max;

	/**
	 * Clone constructor.
	 *
	 * @param range range to copy
	 */
	public Range(Range range) {
		this(range.getMin(), range.getMax());
	}

	/**
	 * Create a range with only one value, representing both minimum and
	 * maximum. Such range can later be extended using extendRange().
	 *
	 * @param minAndMax range minimum and maximum
	 */
	public Range(double minAndMax) {
		this(minAndMax, minAndMax);
	}

	/**
	 * Create a range from min to max.
	 *
	 * @param min range minimum
	 * @param max range maximum
	 */
	public Range(double min, double max) {
		if(min > max)
			throw new IllegalArgumentException("Range minimum ("+ min +") must be <= maximum ("+ max +")");

		this.min = min;
		this.max = max;
	}

	/**
	 * @return range minimum
	 */
	public double getMin() {
		return min;
	}

	/**
	 * @return range maximum
	 */
	public double getMax() {
		return max;
	}

	/**
	 * Returns whether this range contains given value.
	 *
	 * @param value value to check
	 * @return whether this range contains the given value
	 */
	public boolean contains(double value) {
		return (value >= min) && (value <= max);
	}

	/**
	 * Returns whether this range contains the entire given range as a subset.
	 *
	 * @param checkRange range to check
	 * @return True if this range contains given range
	 */
	public boolean containsRange(Range checkRange) {
		return containsRange(checkRange.getMin(), checkRange.getMax());
	}

	/**
	 * Returns whether this range contains the entire given range as a subset.
	 *
	 * @param checkMin Minimum of range to check
	 * @param checkMax Maximum of range to check
	 * @return whether this range contains given range
	 */
	public boolean containsRange(double checkMin, double checkMax) {
		return (checkMin >= min) && (checkMax <= max);
	}

	/**
	 * Returns whether this range lies within the given range.
	 *
	 * @param checkRange given range
	 * @return whether this range lies within given range
	 */
	public boolean isWithin(Range checkRange) {
		return isWithin(checkRange.getMin(), checkRange.getMax());
	}

	/**
	 * Returns whether this range lies within the given range.
	 *
	 * @param checkMin minimum of range to check against
	 * @param checkMax maximum of range to check against
	 * @return whether this range lies within given range
	 */
	public boolean isWithin(double checkMin, double checkMax) {
		return ((checkMin <= min) && (checkMax >= max));
	}

	/**
	 * Returns whether this range overlaps with the given range.
	 * http://world.std.com/~swmcd/steven/tech/interval.html
	 *
	 * @param checkRange given range
	 * @return whether this range overlaps with the given range
	 */
	public boolean rangeOverlaps(Range checkRange) {
		return rangeOverlaps(checkRange.getMin(), checkRange.getMax());
	}

	/**
	 * Returns whether this range overlaps with the given range.
	 * http://world.std.com/~swmcd/steven/tech/interval.html
	 *
	 * @param checkMin minimum of range to check against
	 * @param checkMax maximum of range to check against
	 * @return whether this range overlaps with the given range
	 */
	public boolean rangeOverlaps(double checkMin, double checkMax) {
		return (min <= checkMax) && (max >= checkMin);
	}


	/**
	 * Extends this range (if necessary) to include the given range
	 *
	 * @param extension range by which extend this range
	 */
	public void extendRange(Range extension) {
		if(min > extension.getMin()) min = extension.getMin();
		if(max < extension.getMax()) max = extension.getMax();
	}

	/**
	 * Extends this range (if necessary) to include the given value
	 *
	 * @param value value to extends this range
	 */
	public void extendRange(double value) {
		if(min > value) min = value;
		if(max < value) max = value;
	}

	/**
	 * Returns the size of this range.
	 *
	 * @return size of this range
	 */
	public double getSize() {
		return (max - min);
	}

	/**
	 * Returns the average point of this range.
	 *
	 * @return average
	 */
	public double getAverage() {
		return (min + max) / 2;
	}

	/**
	 * Returns the String representation. We use the '~' character for
	 * separation, not '-', to avoid ranges like 1E-1-2E-1.
	 *
	 * @return This range as string
	 */
	public String toString() {
		return String.valueOf(min) + "~" + String.valueOf(max);
	}

	/**
	 * Splits the range in numOfBins bins and then returns the index of the bin
	 * which contains given value. Indexes are from 0 to (numOfBins - 1).
	 */
	public int binNumber(int numOfBins, double value) {
		double rangeLength = max - min;
		double valueDistanceFromStart = value - min;

		return (int) Math.round((valueDistanceFromStart / rangeLength) * (numOfBins - 1));
	}

	/**
	 * Comparison operator between two Range objects. Performs natural
	 * comparison on the equivalent tuple.
	 */
	public class RangeComparator implements Comparator<Range> {
		@Override
		public int compare(Range a, Range b) {
			if (a.getMin() < b.getMin())
				return -1;
			else if (a.getMin() > b.getMin())
				return 1;
			else {
				if (a.getMax() < b.getMax())
					return -1;
				else if (a.getMax() > b.getMax())
					return 1;
				else
					return 0;
			}
		}
	}
}
