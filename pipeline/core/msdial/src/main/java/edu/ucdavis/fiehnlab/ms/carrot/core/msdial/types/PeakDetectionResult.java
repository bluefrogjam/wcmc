package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types;

/**
 * Created by diego on 9/13/2016.
 */
public class PeakDetectionResult {
    public PeakDetectionResult(int peakID, double amplitudeOrderValue, double amplitudeScoreValue, double areaAboveBaseline, double areaAboveZero, double basePeakValue, double gaussianSimilarityValue, double idealSlopeValue,
                               double intensityAtLeftPeakEdge, double intensityAtPeakTop, double intensityAtRightPeakEdge, double peakPureValue, double rtAtLeftPeakEdge, double rtAtPeakTop, double rtAtRightPeakEdge,
                               int scanNumAtLeftPeakEdge, int scanNumAtPeakTop, int scanNumAtRightPeakEdge, double sharpnessValue, double symmetryValue) {
        this.peakID = peakID;
        this.amplitudeOrderValue = amplitudeOrderValue;
        this.amplitudeScoreValue = amplitudeScoreValue;
        this.areaAboveBaseline = areaAboveBaseline;
        this.areaAboveZero = areaAboveZero;
        this.basePeakValue = basePeakValue;
        this.gaussianSimilarityValue = gaussianSimilarityValue;
        this.idealSlopeValue = idealSlopeValue;

        this.intensityAtLeftPeakEdge = intensityAtLeftPeakEdge;
        this.intensityAtPeakTop = intensityAtPeakTop;
        this.intensityAtRightPeakEdge = intensityAtRightPeakEdge;
        this.peakPureValue = peakPureValue;
        this.rtAtLeftPeakEdge = rtAtLeftPeakEdge;
        this.rtAtPeakTop = rtAtPeakTop;
        this.rtAtRightPeakEdge = rtAtRightPeakEdge;

        this.scanNumAtLeftPeakEdge = scanNumAtLeftPeakEdge;
        this.scanNumAtPeakTop = scanNumAtPeakTop;
        this.scanNumAtRightPeakEdge = scanNumAtRightPeakEdge;
        this.sharpnessValue = sharpnessValue;
        this.symmetryValue = symmetryValue;
    }

    public int peakID;
    public int scanNumAtLeftPeakEdge, scanNumAtRightPeakEdge, scanNumAtPeakTop;

    public double intensityAtLeftPeakEdge, intensityAtRightPeakEdge, intensityAtPeakTop;
    public double areaAboveZero, areaAboveBaseline;
    public double rtAtLeftPeakEdge, rtAtRightPeakEdge, rtAtPeakTop;
    public double peakPureValue, sharpnessValue, gaussianSimilarityValue, idealSlopeValue, basePeakValue, symmetryValue, amplitudeScoreValue, amplitudeOrderValue;

    public double intensityAtPeakTop() {
        return intensityAtPeakTop;
    }

    public double peakID() {
        return peakID;
    }
}
