package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.api;

import java.text.DecimalFormat;


public class Spectrum {
	private final int spectrumNumber;
	private final double retentionTime;
	private DataPoint[] dataPoints;
	private Range mzRange;
	private DataPoint basePeak;
	private double totalIonCurrent;

	/**
	 * Clone constructor.
	 *
	 * @param s Spectrum object to clone
	 */
	public Spectrum(Spectrum s) {
		this(s.getSpectrumNumber(), s.getRetentionTime(), s.getDataPoints());
	}

	/**
	 * Default constructor.
	 *
	 * @param spectrumNumber
	 * @param retentionTime
	 * @param dataPoints
	 */
	public Spectrum(int spectrumNumber, double retentionTime, DataPoint[] dataPoints) {
		this.spectrumNumber = spectrumNumber;
		this.retentionTime = retentionTime;

		setDataPoints(dataPoints);
	}

	public int getSpectrumNumber() {
		return spectrumNumber;
	}

	public DataPoint[] getDataPoints() {
		return dataPoints;
	}

	public void setDataPoints(DataPoint[] dataPoints) {
		this.dataPoints = dataPoints;

		// Determine the base peak, total ion current and m/z range
		basePeak = null;
		mzRange = new Range(0);
		totalIonCurrent = 0;

		if(dataPoints.length > 0) {
			basePeak = dataPoints[0];
			mzRange = new Range(basePeak.getMZ());

			for(DataPoint p : dataPoints) {
				if(p.getIntensity() > basePeak.getIntensity())
					basePeak = p;

				mzRange.extendRange(p.getMZ());
				totalIonCurrent += p.getIntensity();
			}
		}
	}

	public int getNumberOfDataPoints() {
		return dataPoints.length;
	}

	public double getRetentionTime() {
		return retentionTime;
	}

	public Range getMzRange() {
		return mzRange;
	}

	public DataPoint getBasePeak() {
		return basePeak;
	}

	public double getTotalIonCurrent() {
		return totalIonCurrent;
	}


	@Override
	public Spectrum clone() {
		DataPoint[] newDataPoints = new DataPoint[dataPoints.length];

		for(int i = 0; i < dataPoints.length; i++)
			newDataPoints[i] = new DataPoint(dataPoints[i]);

		return new Spectrum(spectrumNumber, retentionTime, newDataPoints);
	}

	@Override
	public String toString() {
		return "#"+ spectrumNumber +" @"+ DecimalFormat.getNumberInstance().format(retentionTime);
	}
}
