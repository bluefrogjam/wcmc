package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.api;

import java.text.DecimalFormat;
import java.text.Format;


/**
 * This class represents one data point of a spectrum (m/z and intensity pair).
 * Data point is immutable once created, to make things simple.
 *
 * From: net.sf.mzmine.data.impl.SimpleDataPoint
 */
public class DataPoint {
	private double mz, intensity;

	/**
	 * Clone constructor.
	 *
	 * @param p DataPoint object to clone
	 */
	public DataPoint(DataPoint p) {
		this(p.getMZ(), p.getIntensity());
	}

	/**
	 *
	 * @param mz ion m/z
	 * @param intensity ion intensity
	 */
	public DataPoint(double mz, double intensity) {
		this.mz = mz;
		this.intensity = intensity;
	}


	public double getIntensity() {
		return intensity;
	}

	public double getMZ() {
		return mz;
	}


	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DataPoint))
			return false;

		DataPoint dp = (DataPoint) obj;
		return (this.mz == dp.getMZ()) && (this.intensity == dp.getIntensity());
	}

	@Override
	public int hashCode() {
		return (int) (this.mz + this.intensity);
	}

	@Override
	public String toString() {
		Format f = DecimalFormat.getNumberInstance();
		return "m/z: "+ f.format(mz) +", intensity: "+ f.format(intensity);
	}
}
