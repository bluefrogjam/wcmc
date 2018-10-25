/*
 * Created on Jul 5, 2006
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.replacement;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.annotation.SystemVariable;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.api.Scan;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.collection.factory.MapFactory;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ContentObject;

import java.util.*;

/**
 * replace the value with the quant mass value based on a retention idex based
 * search
 * 
 * @author wohlgemuth
 * @version Jul 5, 2006
 */
public class OldReplaceWithQuantIntensity extends OldNetCDFReplacement {

	@SystemVariable(description = "if set to true, we will attach addition information for debugging to the data object, causes large amount of memory consumption", possibleValues = "true or false")
	public static final String BINBASE_NETCDF_ENABLE_GRAPH = "BINBASE_NETCDF_ENABLE_GRAPH";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean isEnablePeakDetection() {
		return enablePeakDetection;
	}

	public void setEnablePeakDetection(boolean enablePeakDetection) {
		this.enablePeakDetection = enablePeakDetection;
	}

	/**
	 * do we enable peak detection or not basically if the maximum is at the end
	 * or begin of the scan and endablePeakDetectio is true, than we continue to
	 * scan in this direction to find the peak maximum
	 */
	private boolean enablePeakDetection = true;

	/**
	 * how much should we extend the range for the retention index detections in
	 * ri units
	 */
	private int retentionIndexDynamiceSearchWindowExpansionSize = 300;

	/**
	 * the defined range to find the noise for this area
	 */
	private int noiseDetectionRange = 5000;

	/**
	 * used for the report range in seconds. Basically all the scan in this
	 * range are attached to the content object
	 */
	private int netcdfDebuggingGraphReportRange = noiseDetectionRange * 2;

	/**
	 * how many scans should be used for the range detection at the border
	 */
	private int retentionIndexPeakDetectionScanSize = 10;

	/**
	 * how often do we attempt to execute the peak detection before we give up.
	 * It's mostly used to avoid deadlocks
	 */
	private int maxAttemptsAtPeakDetection = 5;

	/**
	 * do we allow the dynamic growing of retention index windows in case we
	 * don't find a valid quant mass in this window
	 */
	private boolean dynamiceRetentionIndexExpansionInCaseOfNoneExistingQuantMasses = false;

	/**
	 * do we want to enable graphing. This can also be set by using the env
	 * variable BINBASE_NETCDF_ENABLE_GRAPH
	 */
	private boolean enableGraphing = false;

	public boolean isEnableGraphing() {
		return enableGraphing;
	}

	public void setEnableGraphing(boolean enableGraphing) {
		this.enableGraphing = enableGraphing;

		if (enableGraphing) {
			getLogger()
					.warn("graphing mode is enabled, be aware that this will use a lot of memory!");
		}
	}

	private MapFactory<String, String> mapFactory;

	public OldReplaceWithQuantIntensity() {
		super();

		if (System.getProperty(BINBASE_NETCDF_ENABLE_GRAPH) != null) {
			setEnableGraphing( Boolean.parseBoolean(System
					.getProperty(BINBASE_NETCDF_ENABLE_GRAPH)));
		} else {
			getLogger().info("graphing is disabled!");
		}

		Collection<String> hdStoredKeys = new Vector<String>();
		hdStoredKeys.add("scan");

		mapFactory = MapFactory.<String, String> newInstance();
		mapFactory.setConfiguration(hdStoredKeys);

		getLogger().info(
				"using following factory for replacement storage: "
						+ mapFactory.getClass().getName());
	}

	@Override
	protected void replaceAction(List<Scan> scans, int quantmass,
								 ContentObject<Double> object, List<Scan> cache,
								 double timeOfOrigin, Map<Scan, Double> retentionTimeCache) {

		int tries = 0;
		// searches for the max intensity scan in the scans
		Peak peak = detectPeak(scans, quantmass, object, tries, cache,
				timeOfOrigin, retentionIndexSearchWindow, retentionTimeCache);

		if (peak != null) {

			StringBuilder unknownScan = new StringBuilder();

			// extracts the intensity
			double maxIntensity = 0;
			double[] mz = peak.getApex().getMZValues();
			double[] its = peak.getApex().getIntensityValues();
			for (int i = 0; i < mz.length; i++) {
				if (mz[i] == quantmass) {
					maxIntensity = its[i];
				}
				unknownScan.append((int) mz[i]);
				unknownScan.append(":");
				unknownScan.append(its[i]);
				unknownScan.append(" ");

			}

			double usedTime = retentionTimeCache.get(peak.getApex());
			getLogger().debug("looking at time: " + usedTime);
			// calculate the noise for the current scan
			double noise = this.getNoiseValue(object, quantmass,
					noiseDetectionRange, usedTime, cache, retentionTimeCache);
			getLogger().debug("noise for this ion is: " + noise);

			double value = maxIntensity - noise;

			getLogger().debug("value after noise is: " + value);
			object.setValue(value);

			Map<String, String> attributes = mapFactory.createMap();// new
																	// HDStoredMap<String,
																	// String>(hdStoredKeys);

			// used for debugging mostly
			attributes.putAll(object.getAttributes());
			attributes.put("assumed_time", String.valueOf(usedTime));

			attributes.put("start_time", String.valueOf(timeOfOrigin));

			// this crashes the applocation and causes to much memory use
			attributes.put("scan", unknownScan.toString());

			attributes.put("noise", String.valueOf(noise));
			attributes.put("max_intensity", String.valueOf(maxIntensity));
			attributes.put("calculated_value", String.valueOf(value));

			try {
				attributes.put("average retention time",
						String.valueOf(this.getRetentionTimeForBin(object)));
			} catch (NumberFormatException e) {
				getLogger().error(e.getMessage(), e);
			} catch (BinBaseException e) {
				getLogger().error(e.getMessage(), e);
			}

			object.setAttributes(attributes);

			if (enableGraphing) {

				// this crashes the applocation and causes to much memory use
				Map<Double, Double> map = calculateSearchGraph(quantmass,
						object, cache, usedTime, retentionTimeCache,
						timeOfOrigin);

				object.addAttachment("graph", map);

			}

			// setting the values
			getLogger().debug("calculate value is: " + object.getValue());

			if (object.getValue() < 0) {
				getLogger()
						.error("value is smaller than 0, can't be noise calculation errror!");
				object.setValue(0.0);
			}

		} else {
			getLogger()
					.warn("we were not able to find a max value for this object, most likley ew couldn't find a sample either!");
		}
	}

	/**
	 * calculates all the scans needed to generate a graph later to show the
	 * report range
	 * 
	 * @param quantmass
	 * @param object
	 * @param cache
	 * @param time
	 * @return
	 */
	private Map<Double, Double> calculateSearchGraph(int quantmass,
			ContentObject<Double> object, List<Scan> cache, double time,
			Map<Scan, Double> retentionTimeCache, double originalTime) {
		// attach scans for +/- the range of the scans
		List<Scan> attach = this.findCached(object,
				netcdfDebuggingGraphReportRange, time, cache,
				retentionTimeCache);

		Map<Double, Double> map = new HashMap<Double, Double>();

		for (Scan s : attach) {
			double[] intensities = s.getIntensityValues();
			double[] masses = s.getMZValues();
			double intensity = 0;
			double debugTime = retentionTimeCache.get(s);

			int position = Arrays.binarySearch(masses, quantmass);

			// needs to be larger 0 or quantmass wasn't found
			if (position >= 0) {
				intensity = intensities[position];
			}

			map.put(debugTime, intensity);
		}

		return map;
	}

	class Peak {

		public Peak(Scan apex) {
			this.apex = apex;
			this.begin = apex;
			this.end = apex;
		}

		private Scan apex;

		public Scan getApex() {
			return apex;
		}

		public Scan getBegin() {
			return begin;
		}

		public Scan getEnd() {
			return end;
		}

		private Scan begin;

		private Scan end;
	}

	/**
	 * finds the scan with the max intensity, which is basically the peak we
	 * will use to determine the value
	 * 
	 * @param scans
	 * @param quantmass
	 * @param object
	 * @param node
	 * @param tries
	 * @return
	 */
	private Peak detectPeak(List<Scan> scans, int quantmass,
			ContentObject<Double> object, int tries, List<Scan> cache,
			double timeOfOrigin, double riWindow,
			Map<Scan, Double> retentionTimeCache) {
		double max = Double.MIN_VALUE;
		Scan used = null;

		if (getLogger().isDebugEnabled()) {
			getLogger()
					.debug("searching for max intensity for the current mass and object");
			getLogger().debug("number of scans: " + scans.size());
		}
		int positionOfMax = -1;

		double windowBegin = (timeOfOrigin - riWindow) / 1000;
		double windowEnd = (timeOfOrigin + riWindow) / 1000;

		getLogger().debug(
				"amount of scans to check: " + scans.size()
						+ " and we will be in a window of: " + windowBegin
						+ " and " + windowEnd);
		for (int x = 0; x < scans.size(); x++) {
			final Scan finalScan = scans.get(x);

			double[] intensity = finalScan.getIntensityValues();
			double[] mz = finalScan.getMZValues();

			int position = Arrays.binarySearch(mz, quantmass);

			// if the value is not found the index is negative
			if (position >= 0) {
				if (intensity[position] > max) {

					getLogger().trace(
							"new top hit - " + positionOfMax + " - " + max);

					max = intensity[position];
					used = finalScan;
					positionOfMax = x;

				}
			} else {
				getLogger().debug(
						"quant mass did not exist in this scan: "
								+ finalScan.getScanNumber());
			}

		}

		boolean searchAgain = false;

		// only null if no matching quant mass is found
		if (used != null) {
			if (getLogger().isDebugEnabled()) {

				getLogger().debug(
						"using scan: " + retentionTimeCache.get(used) / 1000);
				getLogger().debug("intensity at this point: " + max);
				getLogger().debug("current mass: " + quantmass);
				getLogger().debug(
						"current file: "
								+ this.getFile()
										.getSample(
												Integer.parseInt(object
														.getAttributes().get(
																"sample_id")))
										.getValue());

				getLogger().debug(
						"position: " + positionOfMax + " of " + scans.size());
			}
			if (positionOfMax < retentionIndexPeakDetectionScanSize) {
				getLogger()
						.debug("maximum was at the begin of the search window, looking for more scans in this direction");
				searchAgain = true;
			} else if (positionOfMax > (scans.size() - retentionIndexPeakDetectionScanSize)) {
				getLogger()
						.debug("maximum was at the end of the search window, looking for more scans in this direction");
				searchAgain = true;
			} else {
				getLogger()
						.debug("maximum was neither at the end or the beginning and so we assume it's all good");
				searchAgain = false;
			}
		} else {
			if (dynamiceRetentionIndexExpansionInCaseOfNoneExistingQuantMasses) {
				riWindow = riWindow * 1.25;

				getLogger()
						.warn("no matching quant mass ("
								+ quantmass
								+ ") found, searching again in adjusted RI_WINDOW in this sample, new window is: "
								+ riWindow);

				tries++;

				List<Scan> result = findCached(object, riWindow, timeOfOrigin,
						cache, retentionTimeCache);

				return detectPeak(result, quantmass, object, tries, cache,
						timeOfOrigin, riWindow, retentionTimeCache);
			} else {
				getLogger().warn(
						"DYNAMIC_RI_WINDOW is disabled and so we give up here");
				return null;
			}
		}

		if (searchAgain) {
			if (enablePeakDetection == true) {
				getLogger().debug("execute a new search for the max intensity");
				double rt = retentionTimeCache.get(used);
				getLogger().debug(
						"using rt (" + rt + ") of the scan at position: "
								+ positionOfMax);
				getLogger().debug(
						"current scan number: " + used.getScanNumber());

				if (used.getScanNumber() == 0) {
					getLogger()
							.debug("we were at the first scan already and need to abort!");
					return new Peak(used);
				}
				if (tries == maxAttemptsAtPeakDetection) {
					getLogger()
							.warn("hit max try setting and giving up! so we are returning the current scan");
					return new Peak(used);
				} else {
					tries++;
				}
				List<Scan> result = findCached(object, riWindow, rt, cache,
						retentionTimeCache);

				// you can't make the begin-middle-end test on less than
				// BORDER_RANGE_DETECTION_SIZE * 2 values, it would genereate an
				// endless loop
				if (result.size() > (retentionIndexPeakDetectionScanSize * 2)) {
					riWindow = riWindow
							+ retentionIndexDynamiceSearchWindowExpansionSize;
					getLogger().info(
							"rerunning search since peak detection is enabled and searching again at time "
									+ rt + " and an adjusted window of: "
									+ riWindow);
					return detectPeak(result, quantmass, object, tries, cache,
							rt, riWindow, retentionTimeCache);
				} else {
					getLogger()
							.warn("only found 2 more scans, so we give up and return the curren scan value");
					return new Peak(used);
				}
			} else {
				getLogger()
						.debug("dynamic peak detection is disabled, so we just use the current scan as the best scan");
				return new Peak(used);
			}
		} else {
			getLogger().debug(
					"for final calculation we used scan: "
							+ used.getScanNumber());
			return new Peak(used);
		}
	}

	@Override
	protected List<Scan> findCached(ContentObject<Double> object,
			double window, double ri, List<Scan> cache,
			Map<Scan, Double> retentionTimeCache) {

		getLogger().debug(
				"searching in cache (" + cache.size()
						+ " objects) for this object: " + object);
		List<Scan> result = new Vector<Scan>();

		if (getLogger().isDebugEnabled()) {
			getLogger().debug(
					"searching in window: " + ri + " - min: " + (ri - window)
							+ " - max: " + (ri + window));
		}

		for (Scan scan : cache) {
			double value = retentionTimeCache.get(scan);

			if (value >= ri - window && value <= window + ri) {
				result.add(scan);
			}
			// outside of range so no further hits possible
			else if (value > window + ri) {
				return result;
			}
		}

		getLogger().debug("result size: " + result.size());
		return result;
	}

	@Override
	public String getFolder() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getDescription() {
		return "replace's zeros with the corresponding peak found in a netcdf file and also allows to generate informations, which peak was picked.";
	}

	public int getRetentionIndexDynamiceSearchWindowExpansionSize() {
		return retentionIndexDynamiceSearchWindowExpansionSize;
	}

	public void setRetentionIndexDynamiceSearchWindowExpansionSize(
			int retentionIndexDynamiceSearchWindowExpansionSize) {
		this.retentionIndexDynamiceSearchWindowExpansionSize = retentionIndexDynamiceSearchWindowExpansionSize;
	}

	public int getNoiseDetectionRange() {
		return noiseDetectionRange;
	}

	public void setNoiseDetectionRange(int noiseDetectionRange) {
		this.noiseDetectionRange = noiseDetectionRange;
	}

	public int getNetcdfDebuggingGraphReportRange() {
		return netcdfDebuggingGraphReportRange;
	}

	public void setNetcdfDebuggingGraphReportRange(
			int netcdfDebuggingGraphReportRange) {
		this.netcdfDebuggingGraphReportRange = netcdfDebuggingGraphReportRange;
	}

	public int getRetentionIndexPeakDetectionScanSize() {
		return retentionIndexPeakDetectionScanSize;
	}

	public void setRetentionIndexPeakDetectionScanSize(
			int retentionIndexPeakDetectionScanSize) {
		this.retentionIndexPeakDetectionScanSize = retentionIndexPeakDetectionScanSize;
	}

	public int getMaxAttemptsAtPeakDetection() {
		return maxAttemptsAtPeakDetection;
	}

	public void setMaxAttemptsAtPeakDetection(int maxAttemptsAtPeakDetection) {
		this.maxAttemptsAtPeakDetection = maxAttemptsAtPeakDetection;
	}

	public boolean isDynamiceRetentionIndexExpansionInCaseOfNoneExistingQuantMasses() {
		return dynamiceRetentionIndexExpansionInCaseOfNoneExistingQuantMasses;
	}

	public void setDynamiceRetentionIndexExpansionInCaseOfNoneExistingQuantMasses(
			boolean dynamiceRetentionIndexExpansionInCaseOfNoneExistingQuantMasses) {
		this.dynamiceRetentionIndexExpansionInCaseOfNoneExistingQuantMasses = dynamiceRetentionIndexExpansionInCaseOfNoneExistingQuantMasses;
	}

}
