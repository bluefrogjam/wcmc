package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.gcms;

import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.Peak;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sajjan on 04/16/2018.
 */
public class MS1DeconvolutionResult {

    public int scanNumber;
	public int ms1DecID;

    // mz of the model ion
	public double basepeakMz = -1;
    // height of the biggest peak
	public double basepeakHeight = -1;
    // area of the biggest peak
	public double basepeakArea;
    // doh!!!
	public double retentionTime = -1;
    // sum of heights of the all contributing peaks
	public double integratedHeight;
    // sum of areas of the all contributing peaks
	public double integratedArea;
    // used in peak spot display (slider cutoff)
	public double amplitudeScore;

    public List<Peak> baseChromatogram = new ArrayList<>();
	public List<Peak> spectrum = new ArrayList<>();
    public List<Double> modelMasses = new ArrayList<>();
	public double modelPeakPurity = 0;
	public double modelPeakQuality = 0;
	public double signalNoiseRatio = 0;
}

