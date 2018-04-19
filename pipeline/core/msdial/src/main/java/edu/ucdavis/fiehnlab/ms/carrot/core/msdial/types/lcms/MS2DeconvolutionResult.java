package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.lcms;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.Peak;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.PeakAreaBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diego on 7/22/2016.
 */
public class MS2DeconvolutionResult {

    public int peakTopScan;
    public double peakTopRetentionTime = -1.0;
    public double ms1AccurateMass = -1.0;
    public double uniqueMs = -1.0;
    public double ms1PeakHeight = -1.0f;
    public double ms1IsotopicIonM1PeakHeight = -1.0;
    public double ms1IsotopicIonM2PeakHeight = -1.0;
    public List<Ion> ms1Spectrum = new ArrayList<>();

    public double ms2DecPeakHeight = -1.0f;
    public double ms2DecPeakArea = -1.0;
    public List<Peak> ms2Spectrum = new ArrayList<>();

    public List<List<Ion>> peakListList = new ArrayList<>();
    public List<double[]> baseChromatogram = new ArrayList<>();
    public List<Double> modelMasses = new ArrayList<>();

    public PeakAreaBean peak;

    public MS2DeconvolutionResult(PeakAreaBean peak) {
        this.peak = peak;
        this.ms1IsotopicIonM1PeakHeight = peak.ms1IsotopicIonM1PeakHeight;
        this.ms1IsotopicIonM2PeakHeight = peak.ms1IsotopicIonM2PeakHeight;
        this.peakTopScan = peak.scanNumberAtPeakTop;
        this.peakTopRetentionTime = peak.rtAtPeakTop;
        this.ms1AccurateMass = peak.accurateMass;
        this.ms1PeakHeight = peak.intensityAtPeakTop;
        this.ms2DecPeakArea = -1.0;
        this.ms2DecPeakHeight = -1.0f;
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

