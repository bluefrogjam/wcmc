package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types;

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.peakdetection.DetectedPeakArea;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diego on 7/22/2016.
 */
public class MS2DecResult extends DeconvolutionResult {
	public double peakTopRetentionTime = -1.0;
	public double ms1AccurateMass = -1.0;
	public double uniqueMs = -1.0;
	public double ms1PeakHeight = -1.0f;
	public double ms1IsotopicIonM1PeakHeight = -1.0;
	public double ms1IsotopicIonM2PeakHeight = -1.0;

	public double ms2DecPeakHeight = -1.0f;
	public double ms2DecPeakArea = -1.0;

	public List<Double> modelMasses = new ArrayList<>();
	public List<List<double[]>> peaklistList = new ArrayList<>();
	public List<Ion> spectrum = new ArrayList<>();

	public MS2DecResult() {
		super();
		scanNumber = 0;
	}

	public MS2DecResult(DetectedPeakArea currentPeak){
		scanNumber = currentPeak.scanNumAtPeakTop();
		peakTopRetentionTime = currentPeak.rtAtPeakTop();
		ms1AccurateMass = currentPeak.accurateMass();
		uniqueMs = -1.0;
		ms1PeakHeight = currentPeak.intensityAtPeakTop();
		ms1IsotopicIonM1PeakHeight = currentPeak.ms1IsotopicIonM1PeakHeight();
		ms1IsotopicIonM2PeakHeight = currentPeak.ms1IsotopicIonM2PeakHeight();
		ms2DecPeakHeight = -1.0f;
		ms2DecPeakArea = -1.0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return sb.append(scanNumber).append(" ")
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

