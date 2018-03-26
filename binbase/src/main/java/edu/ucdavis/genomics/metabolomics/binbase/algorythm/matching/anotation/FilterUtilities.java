package edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.anotation;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.jdom.DataConversionException;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateApexMasses;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateSpectra;
import edu.ucdavis.genomics.metabolomics.util.config.Configable;

/**
 * just  common utilities which are usefull for filter operations
 * @author wohlgemuth
 *
 */
public abstract class FilterUtilities {

	/**
	 * returns the needed retention index
	 * 
	 * @author wohlgemuth
	 * @version Apr 19, 2006
	 * @param map
	 * @return
	 */
	public static double getRi(Map<String, Object> map) {
		return Double.parseDouble(map.get("retention_index").toString());
	}

	/**
	 * return the minus offset
	 * 
	 * @author wohlgemuth
	 * @version Apr 19, 2006
	 * @param map
	 * @return
	 */
	public static double getBinMin(Map<String, Object> map) {
		return Double.parseDouble(map.get("minus").toString());
	}

	/**
	 * return the plus offset
	 * 
	 * @author wohlgemuth
	 * @version Apr 19, 2006
	 * @param map
	 * @return
	 */
	public static double getBinMax(Map<String, Object> map) {
		return Double.parseDouble(map.get("plus").toString());
	}

	/**
	 * returns the signal noise from this map
	 * 
	 * @author wohlgemuth
	 * @version Oct 23, 2006
	 * @param map
	 * @return
	 */
	public static double getSingnalNoise(Map<String, Object> map) {
		return Double.parseDouble(map.get("apex_sn").toString());
	}

	/**
	 * uses caching to optimze speed creates the massspec
	 * 
	 * @author wohlgemuth
	 * @version Apr 19, 2006
	 * @param map
	 * @return
	 */
	public static double[][] calculateMassSpec(Map<String, Object> map) {
		if (map.containsKey("CALCULATED_SPECTRA")) {
			return (double[][]) map.get("CALCULATED_SPECTRA");
		} else {
			map.put("CALCULATED_SPECTRA", ValidateSpectra.convert(map.get("spectra").toString()));
			return (double[][]) map.get("CALCULATED_SPECTRA");
		}
	}

	/**
	 * cleans the massspec
	 * 
	 * @author wohlgemuth
	 * @version Apr 19, 2006
	 * @param map
	 * @param cleaning
	 * @return
	 */
	public static double[][] calculateCleanMassSpec(Map<String, Object> map, double cleaning) {
		return ValidateSpectra.renoising(calculateMassSpec(map), cleaning, map.get("apex").toString());
	}

	/**
	 * gets the unique mass
	 * 
	 * @author wohlgemuth
	 * @version Apr 19, 2006
	 * @param map
	 * @return
	 */
	public static int getUniqueMass(Map<String, Object> map) {
		return (int) Double.parseDouble((String) map.get("uniquemass"));
	}

	/**
	 * calculates the base peak
	 * 
	 * @author wohlgemuth
	 * @version Apr 19, 2006
	 * @param spectra
	 * @return
	 */
	public static int calculateBasePeak(double[][] spectra) {
		return ValidateSpectra.calculateBasePeak(spectra);
	}

	/**
	 * if this is a large peak or a small peak
	 * 
	 * @author wohlgemuth
	 * @version Oct 23, 2006
	 * @param sn
	 * @return
	 * @throws DataConversionException 
	 */
	public static boolean isLargePeak(double sn){
		return sn > Double.parseDouble(Configable.CONFIG.getElement("values.filter.largePeakSize")
		.getAttribute("sn").getValue());
	}

	/**
	 * calculates a masspec based on the apex masses
	 * 
	 * @author wohlgemuth
	 * @version Oct 23, 2006
	 * @param map
	 * @return
	 */
	public static double[][] calculateApexSpec(Map<String, Object> map) {
		Collection apex = ValidateApexMasses.convert(map.get("apex").toString());
		double[][] spectra = calculateMassSpec(map);
		double[][] result = new double[ValidateSpectra.MAX_ION][ValidateSpectra.ARRAY_WIDTH];

		Iterator<String> it = apex.iterator();
		
		while(it.hasNext()){
			int value = Integer.parseInt(it.next());
			
			result[value+1][ValidateSpectra.FRAGMENT_ION_POSITION] = spectra[value+1][ValidateSpectra.FRAGMENT_ION_POSITION];
			result[value+1][ValidateSpectra.FRAGMENT_ABS_POSITION] = spectra[value+1][ValidateSpectra.FRAGMENT_ABS_POSITION];
			result[value+1][ValidateSpectra.FRAGMENT_REL_POSITION] = spectra[value+1][ValidateSpectra.FRAGMENT_REL_POSITION];
		}
		
		return result;
	}
}
