package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.deconvolution;

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types.PeakQuality;

/**
 * Created by diego on 9/14/2016.
 */
public class MsDialPeak {
	public int scanNum;
	public double rtMin;
	public double mass;
	public double intensity;
	public int charge;
	public boolean isotopeFrag;
	public PeakQuality peakQuality;

	public MsDialPeak(int scanNum, double rtMin, double mass, double intensity) {
		this.scanNum = scanNum;
		this.rtMin = rtMin;
		this.mass = mass;
		this.intensity = intensity;
	}

	public MsDialPeak(int scanNum, double rtMin, double mass, double intensity, String quality, boolean isotopeFrag) {
		this.scanNum = scanNum;
		this.rtMin = rtMin;
		this.mass = mass;
		this.intensity = intensity;
		this.isotopeFrag = isotopeFrag;

		switch (quality.toUpperCase()) {
			case "LEADING":
				this.peakQuality = PeakQuality.LEADING;
				break;
			case "SATURATED":
				this.peakQuality = PeakQuality.SATURATED;
				break;
			case "TAILING":
				this.peakQuality = PeakQuality.TAILING;
				break;
			case "IDEAL":
			default:
				this.peakQuality = PeakQuality.IDEAL;
		}
	}

	public int scanNum() { return scanNum; }
	public double rtMin() { return rtMin; }
	public double mass() { return mass; }
	public double intensity() { return intensity; }

	@Override
	public String toString() { return String.format("%d, %.5f, %.5f, %.5f", scanNum, rtMin, mass, intensity); }
}
