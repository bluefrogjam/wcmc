package edu.ucdavis.genomics.metabolomics.binbase.algorythm.tic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateSpectra;
import edu.ucdavis.genomics.metabolomics.util.math.SpectraArrayKey;

/**
 * provides us with a tic
 * @author wohlgemuth
 *
 */
public class Tic {

	Map<Double,Double> tic = null;

	public Map<Double, Double> getTic() {
		return tic;
	}
	public void setTic(Map<Double, Double> tic) {
		this.tic = tic;
	}
	
	public Tic(){
		tic = new HashMap<Double,Double>();		
	}
	
	/**
	 * adds a peak
	 * @param time
	 * @param massspec
	 */
	public void addPeak(double time, String massspec){
		addPeak(time, ValidateSpectra.convert(massspec));
	}
	
	/**
	 * adds a peak
	 * @param time
	 * @param massspec
	 */
	public void addPeak(double time, double[][] massspec){
		
		double tic = 0;
		for(int i = 0; i < SpectraArrayKey.MAX_ION; i++){
			tic = tic + massspec[i][SpectraArrayKey.FRAGMENT_ABS_POSITION];
		}
		
		this.tic.put(time, tic);
	}
	public int getCountOfPeaks(){
		return tic.size();
	}
	
	public int getCountOfPeaksWithIntensityOver(double value){
		int count = 0;
		
		Collection<Double> entry = tic.values();
		
		for(Double d : entry){
			if( d > value){
				count++;
			}
		}
		return count;
	}
	
	/**
	 * adds a peak directly from a mao
	 * @param spec
	 */
	public void addPeak(Map<?,?> spec) {
		this.addPeak(Math.round(new Double(
				((String) spec.get("Retention Index")).replace(',', '.'))
		.doubleValue()),(String) spec.get("Spectra"));
		
	}
	
}
