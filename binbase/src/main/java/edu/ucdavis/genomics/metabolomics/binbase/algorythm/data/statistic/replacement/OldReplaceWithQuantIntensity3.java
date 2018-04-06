/*
 * Created on Feb 26, 2007
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.replacement;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.api.Scan;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ContentObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * repalces the zero with the mass spec found at the average retionindex of the
 * anotated bin
 * 
 * @author wohlgemuth
 * @version Feb 26, 2007
 * 
 */
public class OldReplaceWithQuantIntensity3 extends OldReplaceWithQuantIntensity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected List<Scan> findCached(ContentObject<Double> object, double window,
									double ri, List<Scan> cache, Map<Scan,Double> retentionTimeCache) {

		List<Scan> result = new ArrayList<Scan>();

		double min = ri - window;
		double max = ri + window;

		if (getLogger().isDebugEnabled()) {
			getLogger().debug("searching scan cache...");
			getLogger().debug(
					"current search window: " + window + " and current ri: "
							+ ri);
			getLogger().debug("min: " + min);
			getLogger().debug("max: " + max);
			getLogger().debug("cache size is: " + cache.size());
		}

		// search the cache for a scan based on the given ri
		searchCache(result, min, max, cache,retentionTimeCache);

		// sometimes there is no hit, let's try to search by an alternate way
		if (result.isEmpty()) {
			getLogger().debug("no scans found, trying to search again by preffered retention index for bin");

			try {
				double v = this.getPrefferendRetentionTimeForBin(object);

				if (v != ri) {
					min = v - window;
					max = v + window;
					searchCache(result, min, max, cache,retentionTimeCache);
				} else {
					getLogger().debug("=> skipped since preffered rt equaled given ri");

				}
			} catch (Exception e) {
				getLogger().warn(e.getMessage(), e);
			}
		}
		if (result.isEmpty()) {
			getLogger()
					.debug("nothing was found, using super class implementation instead!");
			return super.findCached(object, window, ri, cache,retentionTimeCache);
		}
		return result;
	}

	/**
	 * a manual override in case of no peak detection, to use the preffered
	 * retention time of a bin
	 * 
	 * @param object
	 * @return
	 * @throws BinBaseException
	 * @throws NumberFormatException
	 */
	protected double getPrefferendRetentionTimeForBin(
			ContentObject<Double> object) throws NumberFormatException,
			BinBaseException {
		return this.getRetentionTimeForBin(object);
	}

	/**
	 * the list needs to be sorted!
	 * 
	 * @param result
	 * @param min
	 * @param max
	 * @param cache
	 */
	private void searchCache(List<Scan> result, double min, double max,
			List<Scan> cache,Map<Scan,Double> retentionTimeCache) {
		
		for (Scan s : cache) {
			double value = retentionTimeCache.get(s);
			getLogger().debug(value + " id: " + s.getScanNumber());

			if (value > max) {
				getLogger()
						.debug("=> abort search, since the result can't be after the actual value!");
				return;
			}
			if (value >= min && value <= max) {
				if (getLogger().isDebugEnabled()) {
					getLogger().debug(
							"accepted: " + s.getScanNumber() + "/" + value);
				}
				result.add(s);
			}
		}
	}
}
