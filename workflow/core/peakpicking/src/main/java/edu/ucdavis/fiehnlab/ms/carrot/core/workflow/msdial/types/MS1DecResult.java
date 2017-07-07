package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types;

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.deconvolution.MsDialPeak;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diego on 7/22/2016.
 */
public class MS1DecResult extends DeconvolutionResult {
	public int ms1DecID;

	public double basepeakMz;	    // mz of the model ion
	public double basepeakHeight;	// height of the biggest peak
	public double basepeakArea;     // area of the biggest peak
	public double retentionTime;	// doh!!!
	public double integratedHeight;	// sum of heights of the all contributing peaks
	public double integratedArea;	// sum of areas of the all contributing peaks
	public double amplitudeScore;   // used in peak spot display (slider cutoff)

//	// identification properties
//	// will be unused till after identification
//	public int mspDbID;
//	public String metaboliteName;

//	public double retentionIndex;
//	public double retentionTimeSimilarity;
//	public double retentionIndexSimilarity;

//	public double eiSpectrumSimilarity;	// avg of (dotProd + reverseDotProd + presence% )
//	public double dotProduct;	        //from scoring
//	public double reverseDotProduct;	//from scoring
//	public double presencePersentage;	//from scoring
//	public double totalSimilarity;	    // avg of (eiSpectrumSimilarity + (retention time || retention index)) similarity
//	 end identification properties

	public List<MsDialPeak> spectrum;
	public String splash;
	public double modelPeakPurity;
	public double modelPeakQuality;
	public double signalNoiseRatio;

	public MS1DecResult() {
//		this.mspDbID = -1;
//		this.retentionIndex = -1;
		this.retentionTime = -1;
		this.basepeakArea = -1;
		this.basepeakHeight = -1;
		this.basepeakMz = -1;
//		this.metaboliteName = "";
		this.baseChromatogram = new ArrayList<>();
		this.spectrum = new ArrayList<>();
		this.splash = "";
		this.modelMasses = new ArrayList<>();
		this.modelPeakPurity = 0;
		this.modelPeakQuality = 0;
		this.signalNoiseRatio = 0;
	}
}

