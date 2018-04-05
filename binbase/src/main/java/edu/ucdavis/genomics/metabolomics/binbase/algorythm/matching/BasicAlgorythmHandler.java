/*
 * Created on Apr 19, 2006
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching;

import java.sql.Connection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.jdom.DataConversionException;
import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.AlgorythmHandler;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateApexMasses;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateSpectra;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.DiagnosticsService;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.DiagnosticsServiceFactory;
import edu.ucdavis.genomics.metabolomics.util.config.Configable;

/**
 * a simple filter only accepts similarity and apex sn
 * 
 * @author wohlgemuth
 * @version Apr 19, 2006
 * 
 */
public abstract class BasicAlgorythmHandler implements AlgorythmHandler {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public BasicAlgorythmHandler() {
		super();
	}

	/**
	 * returns the needed retention index
	 * 
	 * @author wohlgemuth
	 * @version Apr 19, 2006
	 * @param map
	 * @return
	 */
	protected double getRi(Map map) {
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
	protected double getBinMin(Map map) {
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
	protected double getBinMax(Map map) {
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
	protected double getSingnalNoise(Map map) {
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
	protected double[][] calculateMassSpec(Map map) {
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
	protected double[][] calculateCleanMassSpec(Map map, double cleaning) {
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
	protected int getUniqueMass(Map map) {
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
	protected int calculateBasePeak(double[][] spectra) {
		return ValidateSpectra.calculateBasePeak(spectra);
	}

	/**
	 * filters the bins by range
	 * 
	 * @author wohlgemuth
	 * @version Apr 19, 2006
	 * @param ri
	 * @param min
	 * @param max
	 * @return
	 */
	protected boolean filterByRange(double ri, double min, double max) {
		return ((ri >= min) && (ri <= max));
	}

	/**
	 * filte by unique ion
	 * 
	 * @author wohlgemuth
	 * @version Apr 19, 2006
	 * @param unkUnique
	 * @param libUnique
	 * @param apexMasses
	 * @return
	 */
	protected boolean filerByUnique(int unkUnique, int libUnique, String apexMasses) {
		return ((libUnique == unkUnique) | (ValidateApexMasses.contains(apexMasses, unkUnique) == true));
	}

	/**
	 * filter by similarity
	 * 
	 * @author wohlgemuth
	 * @version Apr 19, 2006
	 * @param unkSimilarity
	 * @param binSimilarity
	 * @return
	 */
	protected boolean filterBySimilarity(double unkSimilarity, double minSimialrity) {
		return unkSimilarity > minSimialrity;
	}

	/**
	 * filter by signal noise
	 * 
	 * @author wohlgemuth
	 * @version Apr 19, 2006
	 * @param unkSignalNoise
	 * @param minSignalNoise
	 * @return
	 */
	protected boolean filterBySignalNoise(double unkSignalNoise, double minSignalNoise) {
		return unkSignalNoise > minSignalNoise;
	}

	/**
	 * filter by purity
	 * 
	 * @author wohlgemuth
	 * @version Apr 19, 2006
	 * @param unkPurity
	 * @param minPurity
	 * @return
	 */
	protected boolean filterByPurity(double unkPurity, double minPurity) {
		return unkPurity > minPurity;
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
	protected boolean isLargePeak(double sn) throws DataConversionException {
		return sn > Configable.CONFIG.getElement("values.filter.largePeakSize")
		.getAttribute("sn").getDoubleValue();
	}

	/**
	 * calculates if the unique ion is acceptable
	 * 
	 * @author wohlgemuth
	 * @version Apr 19, 2006
	 * @param unqiqueIntesity
	 * @param basePeakIntensity
	 * @param minRatio
	 * @return
	 */
	protected boolean validateUniqueRatio(double uniqueIntensity, double basePeakIntensity, double minRatio) {
		return ((uniqueIntensity / basePeakIntensity) > minRatio);
	}

	/**
	 * calculates a masspec based on the apex masses
	 * 
	 * @author wohlgemuth
	 * @version Oct 23, 2006
	 * @param map
	 * @return
	 */
	protected double[][] calculateApexSpec(Map map) {
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

	public Connection getConnection() {
		return this.c;
	}

	public void setConnection(Connection connection) {
		this.c = connection;
	}

	private Connection c;
	

	@Override
	public DiagnosticsService getDiagnosticsService() {
		return service;
	}

	private DiagnosticsService service = DiagnosticsServiceFactory
			.newInstance().createService();
}
