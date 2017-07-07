package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types;

/**
 * Created by diego on 9/13/2016.
 */
public class PeakAreaBean {
	public PeakAreaBean(){};

	public double accurateMass;
//	public double accurateMassSimilarity;
//	public double adductIonAccurateMass;
//	public int adductIonChargeNumber;
//	public String adductIonName;
//	public int adductIonXmer;
//	public int adductParent;
//	public double alignedRetentionTime;
	public double amplitudeOrderValue;
//	public double amplitudeRatioSimilatiryValue;
	public double amplitudeScoreValue;
	public double areaAboveBaseline;
	public double areaAboveZero;
	public double basePeakValue;
	public int deconvolutionID;
	public double gaussianSimilarityValue;
	public double idealSlopeValue;
	public double intensityAtLeftPeakEdge;
	public double intensityAtPeakTop;
	public double intensityAtRightPeakEdge;
	public int isotopeParentPeakID;
	public int isotopeWeightNumber;
//	public double isotopeSimilarityValue;
//	public int libraryID;
//	public double massSpectraSimilarityValue;
//	public String metaboliteName;
	public int ms1LevelDatapointNumber;
	public int ms2LevelDatapointNumber;
	public double ms1IsotopicIonM1PeakHeight;
	public double ms1IsotopicIonM2PeakHeight;
	public double normalizedValue;
	public int peakID;
	public double peakPureValue;
//	public double peakShapeSimilarityValue;
//	public double peakTopDifferencialValue;
//	public int postIdentificationLibraryId;
//	public double presenseSimilarityValue;
//	public double reverseSearchSimilarityValue;
	public double rtAtLeftPeakEdge;
	public double rtAtPeakTop;
	public double rtAtRightPeakEdge;
//	public double rtSimilarityValue;
	public int scanNumberAtLeftPeakEdge;
	public int scanNumberAtPeakTop;
	public int scanNumberAtRightPeakEdge;
	public double sharpenessValue;
	public double symmetryValue;
//	public double totalScore;

	public double rtAtPeakTop() { return rtAtPeakTop; }
	public double accurateMass() { return accurateMass; }
	public double intensityAtPeakTop() { return intensityAtPeakTop; }
	public double peakID() { return peakID; }
	public double scanNumAtPeakTop() { return scanNumberAtPeakTop; }
	public double sharpenessValue() { return sharpenessValue; }
	public double idealSlopeValue() { return idealSlopeValue; }

	@Override
	public String toString() {
		return new StringBuilder()
				.append(this.scanNumberAtPeakTop).append(", ")
				.append(String.format("%#.5f",this.rtAtPeakTop)).append(", ")
				.append(String.format("%#.5f",this.accurateMass)).append(", ")
				.append(String.format("%#.5f",this.intensityAtPeakTop))//.append(", ")
				.toString();
	}
}
