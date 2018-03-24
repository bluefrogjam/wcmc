/*
 * Created on Apr 19, 2006
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching;

import java.sql.Connection;
import java.util.Map;

import org.jdom.DataConversionException;
import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.anotation.IonFilter;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.anotation.MatchingException;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.anotation.logger.PrefixedLogger;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateSpectra;
import edu.ucdavis.genomics.metabolomics.util.config.Configable;
import edu.ucdavis.genomics.metabolomics.util.math.Similarity;

/**
 * a really simple algorythm handler only checks for:
 * 
 * ri sim sn
 * 
 * @author wohlgemuth
 * @version Apr 19, 2006
 * 
 */
public class SimpleAlgorythmHandler extends BasicAlgorythmHandler {
	private PrefixedLogger logger = PrefixedLogger
			.getLogger(SimpleAlgorythmHandler.class);

	private double similarity;

	private double sn;

	private double cleanning;

	IonFilter filter = new IonFilter();

	public SimpleAlgorythmHandler() {
		super();

		try {
			Element ex = Configable.CONFIG.getElement("values.matchingFailed");

			similarity = ex.getAttribute("similarity").getDoubleValue();
			sn = ex.getAttribute("signalnoise").getDoubleValue();
		} catch (DataConversionException e) {
			logger.error(
					"error at getting value, using default value. Exception was: "
							+ e.getMessage(), e);
			similarity = 800;
			sn = 250;
		}

		try {
			cleanning = Configable.CONFIG.getElement("values.filter.clean")
					.getAttribute("cut").getDoubleValue();
			logger.debug("using cleaning offset to: " + cleanning);
		} catch (Exception e) {
			logger.error(
					"error at getting value, using default value. Exception was: "
							+ e.getMessage(), e);
			cleanning = 3;
		}

	}

	/**
	 * filters by ri, sn, similarity
	 * 
	 * @author wohlgemuth
	 * @version Apr 19, 2006
	 * @see edu.ucdavis.genomics.metabolomics.binbase.algorythm.AlgorythmHandler#compare(Map,
	 *      Map)
	 */
	public boolean compare(Map lib, Map unk, Element configuration) {
		logger.setLoggingPrefix(lib.get("name"),lib.get("spectra_id"), unk.get("spectra_id"));
		try {
			double ri = getRi(unk);
			double binRi = getRi(lib);
			double min = getBinMin(lib);
			double max = getBinMin(lib);

			double signalNoise = Double.parseDouble(unk.get("apex_sn")
					.toString());

			// calculate unique setting
			min = binRi - min;
			max = binRi + max;

			if (filterByRange(ri, min, max)) {

				// get similaritys
				unk.put("similarity", String.valueOf(similarity));

				if (signalNoise > this.sn) {

					// calculates by similarity
					double[][] libSpectra = calculateMassSpec(lib);
					double[][] cleanSpectra = calculateCleanMassSpec(unk, cleanning);

					double similarity = similarity(libSpectra,
							cleanSpectra);

					if (similarity > this.similarity) {

						try {
							return filter.compareTo(lib, unk);
						} catch (MatchingException e) {
							logger.error(e.getMessage(), e);
							return false;
						}
					}
					return false;
				}
				return false;
			}
			return false;
		} finally {
			logger.clearPrefix();
		}
	}

	@Override
	public void setConnection(Connection connection) {
		if (connection != null) {
			super.setConnection(connection);
			filter.setConnection(connection);
		}
	}
	
	double similarity(double[][] library, double[][] unknown) {
		try {
			Similarity sim = new Similarity();
			sim.setLibrarySpectra((library));
			sim.setUnknownSpectra((unknown));

			return sim.calculateSimimlarity();
		} catch (ArrayIndexOutOfBoundsException e) {
			throw e;
		}
	}
}
