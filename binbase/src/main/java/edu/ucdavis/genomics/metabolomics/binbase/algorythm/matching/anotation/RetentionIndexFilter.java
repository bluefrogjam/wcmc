package edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.anotation;

import java.util.Map;

/**
 * filters by retention index
 * 
 * @author wohlgemuth
 * 
 */
public class RetentionIndexFilter extends BasicFilter implements Filter {

	/**
	 * if the unknown retention index is in the range of the bin retention index
	 * we return true
	 */
	protected boolean compare(Map<String, Object> bin,
			Map<String, Object> unknown) {
		

		int binId = Integer.parseInt(bin.get("bin_id").toString());
		int spectraId = Integer.parseInt(unknown.get("spectra_id").toString());
		
		double ri = getRi(unknown);
		double binRi = getRi(bin);
		double min = getBinMin(bin);
		double max = getBinMin(bin);

		min = binRi - min;
		max = binRi + max;

		if (isDebugEnabled()) {
			logger.debug("compare: " + ri + " against " + binRi);
			logger.debug("distance: " + ( ri - binRi ) );
		}
		if (ri >= min) {
			if (ri <= max) {
				getDiagnosticsService().diagnosticActionSuccess(spectraId, binId, this.getClass(), "filtering by retention index", "massspec was accepted, since it was in the defined window",new Object[]{ri,min,max});

				return true;
			} else {
				getDiagnosticsService().diagnosticActionFailed(spectraId, binId, this.getClass(), "filtering by retention index", "massspec was rejected since the retention index was to large",new Object[]{ri,max});

				setReasonForRejection("ri to large");
			}
		} else {
			setReasonForRejection("ri to small");
			getDiagnosticsService().diagnosticActionFailed(spectraId, binId, this.getClass(), "filtering by retention index", "massspec was rejected since the retention index was to small",new Object[]{ri,min});

		}
		return false;
	}

	/**
	 * returns the needed retention index
	 * 
	 * @author wohlgemuth
	 * @version Apr 19, 2006
	 * @param map
	 * @return
	 */
	protected double getRi(Map<String, Object> map) {
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
	protected double getBinMin(Map<String, Object> map) {
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
	protected double getBinMax(Map<String, Object> map) {
		return Double.parseDouble(map.get("plus").toString());
	}

}
