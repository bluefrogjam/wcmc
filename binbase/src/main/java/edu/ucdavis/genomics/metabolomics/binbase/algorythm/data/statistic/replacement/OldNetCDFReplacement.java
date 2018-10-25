/*
 * Created on Jun 21, 2006
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.replacement;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.BinBaseResultZeroReplaceable;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.ResultDataFile;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.api.ChromatogramReader;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.api.Converter;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.api.Scan;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.api.Spectrum;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.baseline.BaselineCorrector;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.replacement.resolver.ResolverBuilder;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.replacement.resolver.SimpleResolverBuilder;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.thread.ExecutorsServiceFactory;
import edu.ucdavis.genomics.metabolomics.binbase.bci.server.exception.FileNotFoundException;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.Min;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ContentObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.FormatObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.HeaderFormat;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.NullObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.SampleObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ZeroObject;

/**
 * replace values with netcdf values ���
 * 
 * @author wohlgemuth
 * @version Jun 21, 2006
 */
public abstract class OldNetCDFReplacement extends BinBaseResultZeroReplaceable {

	/**
	 * defines the smooting of the baseline correction. If it's 1, it can cause
	 * an endless loop in some cases.
	 */
	public double baseLineSmoothingFactor = 5.0;

	/**
	 * defines the window we are allowed to search in
	 */
	public int retentionIndexSearchWindow = 1000;

	/**
	 * if you set it to high, your applicationserver might run out of memory
	 */
	public int maximalParallelDownloads = 4;

	private List<RawdataResolver> resolver = new ArrayList<RawdataResolver>();

	protected boolean filesAlreadyDownloaded = false;

	/**
	 * enables the actual baseline correction
	 */
	protected boolean enableBaseLineCorrection = false;

	public static String ENABLE_BASELINE_CORRECTION = "BINBASE_ENABLE_NETCDF_BASELINE_CORECTION";

	public OldNetCDFReplacement() {

		ResolverBuilder builder = new SimpleResolverBuilder();
		resolver = builder.build();

		filesAlreadyDownloaded = false;

		if (System.getProperty(ENABLE_BASELINE_CORRECTION) != null) {
			if (Boolean.parseBoolean(System
					.getProperty(ENABLE_BASELINE_CORRECTION))) {
				enableBaseLineCorrection = true;

			} else {
				enableBaseLineCorrection = false;

			}
		}

		getLogger().info(
				"baseline correction enabled: " + enableBaseLineCorrection);
	}

	/**
     *
     */
	private static final long serialVersionUID = 1L;

	/**
	 * do we want to replace all values or only zero values
	 */
	private boolean replaceAllValues = false;

	/**
	 * do we want to validate the dataset
	 */
	private boolean validate = false;

	/**
	 * fills the internal caches
	 * 
	 * @param node
	 */
	private synchronized List<Scan> fillCache(final Map<Scan, Double> rtCache) {

		Collection<Scan> scans = rtCache.keySet();
		List<Scan> cache = new ArrayList<Scan>(scans.size());

		for (Scan s : scans) {
			cache.add(s);
		}

		getLogger().debug("cache is filled with: " + cache.size() + " objects");

		getLogger().debug("sorting cache by retention time...");
		Collections.sort(cache, new Comparator<Scan>() {

			@Override
			public int compare(Scan o1, Scan o2) {
				return rtCache.get(o1).compareTo(rtCache.get(o2));
			}
		});
		return cache;
	}

	protected Map<Scan, Double> readNetcdfData(File file) throws Exception {
		getLogger().debug("initialize reader");

		ChromatogramReader reader = new ChromatogramReader();

		List<Spectrum> data = reader.readChromatogram(file.getAbsolutePath());

		if (enableBaseLineCorrection) {
			getLogger().info("running baseline correction...");
			BaselineCorrector corrector = new BaselineCorrector(
					10000000,
					0.001,
					true,
					baseLineSmoothingFactor,
					edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.api.ChromatogramType.TIC,
					data);

			corrector.correctBaselines();

			getLogger().info("finished correction");
			data = corrector.getSpectra();
		}
		// used to calculate retention times
		Map<Scan, Double> cachedRetentionTimes = new Converter().toMZmineV1(
				data, true);

		data.clear();
		getLogger().info("cache contains: " + cachedRetentionTimes.size());
		return cachedRetentionTimes;
	}

	/**
	 * searches the internal cache
	 * 
	 * @param object
	 * @param window
	 * @param assumedRetentionTime
	 * @param cache
	 * @return
	 */
	protected abstract List<Scan> findCached(ContentObject<Double> object,
			double window, double assumedRetentionTime, List<Scan> cache,
			Map<Scan, Double> retentionTimeCache);

	/**
	 * searching for a valid object with attributes
	 * 
	 * @param it
	 * @return
	 */
	private ContentObject<Double> findValidObject(
			Iterator<ContentObject<Double>> it) {
		ContentObject<Double> object = it.next();

		if (object.getAttributes().isEmpty()) {
			getLogger().error(
					"something wrong with object, it has not attributes!"
							+ object.getClass() + " - " + object
							+ " ignoring it!");
			while (it.hasNext()) {
				return findValidObject(it);
			}
		}
		return object;
	}

	/**
	 * calculates all the noises in the given window for the given mass
	 * 
	 * @param time
	 * @param quant
	 * @param windows
	 * @return
	 * @author wohlgemuth
	 * @version Feb 26, 2007
	 */
	public double getNoiseValue(ContentObject<Double> object, int quant,
			int window, double rt, List<Scan> cache,
			Map<Scan, Double> retentionTimeCache) {
		// search for a possible value from the given scan
		getLogger().debug("retention time for noise calculation: " + rt);

		List<Scan> scans = findCached(object, window, rt, cache,
				retentionTimeCache);
		List<Double> intensities = new ArrayList<Double>(scans.size());

		getLogger().debug("using window of: " + window);
		getLogger().debug(
				"using number of scans for noise calculation: " + scans.size());

		for (Scan s : scans) {
			double mz[] = s.getMZValues();
			double intensity[] = s.getIntensityValues();

			for (int x = 0; x < mz.length; x++) {
				if (mz[x] == quant && intensity[x] > 0.0) {
					intensities.add(intensity[x]);
				}
			}
		}

		getLogger().debug("found intensisties for this mass: " + intensities);

		return new Min().calculate(intensities);
	}

	/**
	 * calculates the rentention index
	 * 
	 * @param object
	 * @return
	 * @throws BinBaseException
	 * @throws NumberFormatException
	 */
	protected double getRetentionTimeForBin(ContentObject<Double> object)
			throws NumberFormatException, BinBaseException {
		// based on the average retention time for this bin
		return this.getFile().getAverageRetentionTimeForBin(
				Integer.parseInt(object.getAttributes().get("id").toString()));
	}

	/**
	 * calculates the filepath and name of the file
	 * 
	 * @param nameOfSample
	 * @return
	 * @author wohlgemuth
	 * @version Jun 22, 2006
	 */
	private final File getTempFile(String nameOfSample) {
		getLogger().info("aquire file: " + nameOfSample);
		try {

			if (resolver.isEmpty()) {
				getLogger()
						.warn("we don't have any resolvers registered and so won't find files!");
			}

			for (RawdataResolver res : resolver) {
				getLogger().debug("searching with: " + res);
				try {
					File file = res.resolveNetcdfFile(nameOfSample);
					if (file != null) {
						if (file.exists()) {
							getLogger().info("found file with: " + res);
							return file;
						}
					}
				} catch (FileNotFoundException e) {
					getLogger().info(
							"sorry couldn't find file with this resolver!");
				}
			}

			getLogger().info("not found! - " + nameOfSample);

			return new File(nameOfSample);

		} catch (Exception e) {
			getLogger().error(e.getMessage(), e);
			return new File(nameOfSample);

		} finally {
			getLogger().info("aquired");
		}
	}

	protected List<RawdataResolver> registerResolvers() {
		return resolver;
	}

	public boolean isReplaceAllValues() {
		return replaceAllValues;
	}

	/**
	 * makes sure that all needed netcdf files exist
	 * 
	 * @author wohlgemuth
	 * @version Jul 13, 2006
	 * @see edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.BinBaseResultZeroReplaceable#isValid()
	 */
	@Override
	public boolean isValid() {
		if (validate) {
			List<SampleObject<String>> samples = getFile().getSamples();
			boolean successfull = true;
			for (int i = 0; i < samples.size(); i++) {
				File sampleFile = getTempFile(samples.get(i).getValue());
				if (sampleFile.exists() == false) {
					successfull = false;
					getLogger()
							.warn(sampleFile.getName()
									+ " does not exist in all available sources!");
				}
			}
			return successfull;
		}
		getLogger().warn("validation is disabled!");
		return true;
	}

	/**
	 * @param object
	 * @param retentionTimeCache
	 * @author wohlgemuth
	 * @version Jul 7, 2006
	 */
	private void obtainNode(ContentObject<Double> object, List<Scan> cache,
			Map<Scan, Double> retentionTimeCache) {
		// get the ri of the current bin
		Map<?, ?> attributes = object.getAttributes();

		if (attributes.isEmpty()) {
			getLogger().info(
					"no attributes found for object: " + object + " - "
							+ object.getClass());
			return;
		}

		getLogger().debug(
				"current bin: "
						+ Integer.parseInt(attributes.get("id").toString()));
		FormatObject<?> bin = this.getFile().getBin(
				Integer.parseInt(attributes.get("id").toString()));

		try {
			int quantMass = Integer.parseInt(bin.getAttributes()
					.get("quantmass").toString());

			// browsing the cache to find a result

			double assumedTime = getRetentionTimeForBin(object);
			getLogger().debug("assuming time: " + assumedTime);

			List<Scan> result = findCached(object, retentionIndexSearchWindow, assumedTime,
					cache, retentionTimeCache);

			if (result.isEmpty()) {
				getLogger().warn(
						"nothing found in cache for this object - "
								+ this.getFile().getBin(
										Integer.parseInt(object.getAttributes()
												.get("id"))));
			} else {
				try {
					getLogger().debug("before: " + object);
					replaceAction(result, quantMass, object, cache,
							assumedTime, retentionTimeCache);
					getLogger().debug("after: " + object);
				} catch (Exception e) {
					getLogger().error("mass: " + quantMass);
					getLogger().error("object: " + object);
					getLogger().error(e.getMessage(), e);
				}
			}

			result.clear();
		} catch (Exception e) {
			getLogger().error(e.getMessage(), e);
		}
	}

	/**
	 * does some fancy pre processing and should be used by sub classes
	 * 
	 * @param file
	 */
	protected final synchronized void preReplaclement(ResultDataFile file) {

	}

	/**
	 * download all required sample files
	 */
	protected void fireFileSet(ResultDataFile file) {

		if (filesAlreadyDownloaded == false) {
			List<SampleObject<String>> samples = file.getSamples();

			String name = Thread.currentThread().getName();
			ExecutorService service = null;

			if (Runtime.getRuntime().availableProcessors() > maximalParallelDownloads) {
				service = ExecutorsServiceFactory
						.createService(maximalParallelDownloads);
			} else {
				service = ExecutorsServiceFactory.createService();
			}
			getLogger().debug("download netcdf files...");
			// fetch file...
			for (final SampleObject<String> sample : samples) {

				Runnable run = new Runnable() {

					@Override
					public void run() {
						Thread.currentThread().setName(
								"downloading cdf for " + sample.getValue());
						getTempFile(sample.getValue());
					}
				};

				if (maximalParallelDownloads == 1) {
					getLogger().debug("running in single thread mode!");
					run.run();
				} else {
					service.submit(run);
				}
			}
			getLogger().debug("files are downloaded to the temp directory");
			ExecutorsServiceFactory.shutdownService(service);

			Thread.currentThread().setName(name);

			filesAlreadyDownloaded = true;
		}

	}

	/**
	 * does some fancy pre processing and should be used by sub classes
	 * 
	 * @param file
	 */
	protected void preReplaclement(ResultDataFile file,
			SampleObject<String> sample) {
	}

	/**
	 * actually does the replacement
	 * 
	 * @param finalScan
	 * @param quantmass
	 * @param object
	 * @param node
	 */
	protected abstract void replaceAction(List<Scan> finalScan, int quantmass,
			ContentObject<Double> object, List<Scan> cache,
			double timeOfOrigin, Map<Scan, Double> retentionTimeCache);

	@SuppressWarnings("unchecked")
	public List replaceZeros(final List list) {
		// check if we actually have null values to save calculation time
		boolean containsNull = false;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) instanceof NullObject
					| list.get(i) instanceof ZeroObject | list.get(i) == null) {
				containsNull = true;
				i = list.size();
			}
		}

		if (containsNull == false) {
			return list;
		}

		getLogger()
				.info("running pre run method todo internal calculations, for whatever reason");
		long begin = System.currentTimeMillis();
		try {
			preReplaclement(this.getFile());

			Iterator<ContentObject<Double>> it = list.iterator();

			if (this.isSampleBased()) {

				if (it.hasNext()) {

					ContentObject<Double> object = findValidObject(it);

					return replaceSingleSample(list, object);

				}
			} else {
				getLogger()
						.error("sorry only sample based mode is possible for this replacement");
			}

		} catch (Exception e) {
			getLogger()
					.error("there seemed be some kind of error with this sample and we skipped the replacement for it",
							e);
		} finally {
			long end = System.currentTimeMillis();
			long time = (end - begin) / 1000;

			getLogger().info(
					"requried time for replacement of this sample was: " + time
							+ " seconds");
		}
		return list;
	}

	/**
	 * replaces a single sample
	 * 
	 * @param list
	 *            massspecs of this sample
	 * @param object
	 * @return
	 */
	protected List<ContentObject<Double>> replaceSingleSample(
			final List<ContentObject<Double>> list, ContentObject<Double> obj) {
		if (obj.getAttributes().isEmpty()) {
			getLogger()
					.error("can't find an object with attributes, giving up");
			return list;
		}

		// do correction of the samples
		SampleObject<String> sample = getFile().getSample(
				Integer.parseInt((obj.getAttributes().get("sample_id"))));
		getLogger().info("current sample: " + sample.getValue());

		// load netcdf file
		File sampleFile = getTempFile(sample.getValue());
		if (!sampleFile.exists()) {
			getLogger().error(
					"can't replace zeros, cause file not found on harddisk for this sample: "
							+ sampleFile);
			return list;
		}

		try {
			final Map<Scan, Double> retentionTimeCache = readNetcdfData(sampleFile);
			final List<Scan> cache = fillCache(retentionTimeCache);

			try {
				getLogger().info(
						"running pre replacement method for current sample");
				try {
					preReplaclement(getFile(), sample);
				} catch (Exception e) {
					getLogger().warn(
							"error in pre processing: " + e.getMessage(), e);
					return list;
				}

				for (int i = 0; i < list.size(); i++) {

					final int current = i;

					ContentObject<Double> object = (ContentObject<Double>) list
							.get(current);

					Thread.currentThread().setName(
							"bin id:" + object.getAttributes().get("id"));

					getLogger().debug("received object: " + object);

					HeaderFormat<String> bin = getFile().getBin(
							Integer.parseInt(object.getAttributes().get("id")
									.toString()));
					getLogger().debug("working on bin: " + bin);

					if (isReplaceAllValues()) {
						if (object != null && bin != null) {
							getLogger().debug(
									"we are in the replace all value mode...");
							obtainNode(object, cache, retentionTimeCache);
						} else {
							getLogger()
									.warn("for some reason, we didn't find a bin/object!");
						}
					} else {

						if (object instanceof NullObject
								| object instanceof ZeroObject | object == null) {
							getLogger()
									.debug("object has no value -> needs to be replaced!");
							obtainNode(object, cache, retentionTimeCache);
						}
					}
				}

				try {
					postReplaclement(getFile(), sample);
				} catch (Exception e) {
					getLogger().warn(
							"error in post processing: " + e.getMessage(), e);
				}
			} finally {
				// cache shall always be cleared at the end
				cache.clear();
			}
			return list;
		} catch (Exception e) {
			getLogger().warn("error in loading data: " + e.getMessage(), e);
			return list;
		} finally {
			// clean up ressources
			System.gc();
		}
	}

	protected void postReplaclement(ResultDataFile file,
			SampleObject<String> sample) {

	}

	public void setReplaceAllValues(boolean replaceAllValues) {
		this.replaceAllValues = replaceAllValues;
	}

}
