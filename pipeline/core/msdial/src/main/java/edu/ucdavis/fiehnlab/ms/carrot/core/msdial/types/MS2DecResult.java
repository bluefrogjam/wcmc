package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diego on 7/22/2016.
 */
public class MS2DecResult {

	public int peakTopScan;
	public double peakTopRetentionTime = -1.0;
	public double ms1AccurateMass = -1.0;
	public double uniqueMs = -1.0;
	public double ms1PeakHeight = -1.0f;
	public double ms1IsotopicIonM1PeakHeight = -1.0;
	public double ms1IsotopicIonM2PeakHeight = -1.0;

	public double ms2DecPeakHeight = -1.0f;
	public double ms2DecPeakArea = -1.0;

	public List<List<Ion>> peakListList = new ArrayList<>();
	public List<Peak> spectrum = new ArrayList<>();
	public List<double[]> baseChromatogram = new ArrayList<>();
	public List<Double> modelMasses = new ArrayList<>();


	public MS2DecResult(PeakAreaBean peak) {
		ms1IsotopicIonM1PeakHeight = peak.ms1IsotopicIonM1PeakHeight;
		ms1IsotopicIonM2PeakHeight = peak.ms1IsotopicIonM2PeakHeight;
		peakTopScan = peak.scanNumberAtPeakTop;
		peakTopRetentionTime = peak.rtAtPeakTop;
		ms1AccurateMass = peak.accurateMass;
		ms1PeakHeight = peak.intensityAtPeakTop;
		ms2DecPeakArea = -1.0;
		ms2DecPeakHeight = -1.0f;
		uniqueMs = -1.0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return sb.append(peakTopScan).append(" ")
				.append(peakTopRetentionTime).append(" ")
				.append(ms1AccurateMass).append(" ")
				.append(uniqueMs).append(" ")
				.append(ms1PeakHeight).append(" ")
				.append(ms1IsotopicIonM1PeakHeight).append(" ")
				.append(ms1IsotopicIonM2PeakHeight).append(" ")
				.append(ms2DecPeakHeight).append(" ")
				.append(ms2DecPeakArea).append(" ").toString();
	}
}

