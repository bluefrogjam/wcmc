package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.gcms;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sajjan on 04/16/2018.
 */
public class ModelChromatogramVector {

	public MS1DeconvolutionPattern ms1DeconvolutionPattern = MS1DeconvolutionPattern.C;

	public int targetScanLeftInModelChromatogramVector;
	public int targetScanTopInModelChromatogramVector;
	public int targetScanRightInModelChromatogramVector;

	public List<Integer> chromatogramScanList = new ArrayList<>();
	public List<Integer> rawScanList = new ArrayList<>();
	public List<Double> retentionTimeList = new ArrayList<>();
	public List<Float> targetIntensityArray = new ArrayList<>();

	public List<Float> oneLeftIntensityArray = new ArrayList<>();
	public List<Float> oneRightIntensityArray = new ArrayList<>();

	public List<Float> twoLeftIntensityArray = new ArrayList<>();
	public List<Float> twoRightInetnsityArray = new ArrayList<>();

	public List<Double> modelMasses = new ArrayList<>();
	public double sharpness = 0;
}
