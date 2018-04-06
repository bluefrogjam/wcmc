/*
 * Created on Jul 14, 2005
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.filter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateSpectra;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.DiagnosticsService;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.DiagnosticsServiceFactory;
import edu.ucdavis.genomics.metabolomics.util.config.Configable;

/**
 * filters the massspecs by there basepeaks
 * 
 * @author wohlgemuth
 */
public class BasepeakFilter implements MassSpecFilter {

	private Logger logger = LoggerFactory.getLogger(getClass());
	/**
	 * possible basepeaks
	 */
	int[] basePeakFilter = new int[] {};

	@SuppressWarnings("unchecked")
	public BasepeakFilter() {
		// loads the configuration from the registered configuration and stores
		// it in the internal array
		List<Element> ions = Configable.CONFIG.getElement(
				"bin.correction.filter.basepeak").getChildren();

		basePeakFilter = new int[ions.size()];

		for (int i = 0; i < basePeakFilter.length; i++) {
			basePeakFilter[i] = Integer.parseInt(ions.get(i).getText());
			logger.info("adding ion to filter: " + basePeakFilter[i]);
		}

	}

	public BasepeakFilter(int i){
		basePeakFilter = new int[]{i};
	}

	/**
	 * @see edu.ucdavis.genomics.metabolomics.binbase.algorythm.filter.MassSpecFilter#accept(Map)
	 */
	public boolean accept(Map<String,Object> map) {
		String spectra = (String) map.get("spectra");
		double[][] spectraArray = ValidateSpectra.convert(spectra);

		int basePeak = ValidateSpectra.calculateBasePeak(spectraArray);

		boolean result = false;

		for (int y = 0; y < basePeakFilter.length; y++) {
			if (basePeak == basePeakFilter[y]) {
				result = true;
			}
		}
		if (result == false) {
			if (logger.isDebugEnabled()) {
				logger.debug("rejected spectra: " + map.get("retention_index") + "/" + map.get("spectra_id")
						+ " because basepeak " + basePeak
						+ " was not in the list ("
						+ Arrays.toString(basePeakFilter) + ")");
			}
		}
		else {
			logger.debug("accepted spectra: " + map.get("retention_index") + "/" + map.get("spectra_id"));
		}
		return result;
	}

	@Override
	public DiagnosticsService getDiagnosticsService() {
		return service;
	}

	private DiagnosticsService service = DiagnosticsServiceFactory
			.newInstance().createService();
}
