package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.thread.ExecutorsServiceFactory;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ContentObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.HeaderFormat;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.SampleObject;

/**
 * an internal index over the result datafile
 * 
 * @author wohlgemuth
 */
public class ResultDataFileIndex {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private ResultDataFile file;

	private Map<Integer, List<ContentObject<Double>>> massspecs = new ConcurrentHashMap<Integer, List<ContentObject<Double>>>();

	private Map<Integer, HeaderFormat<String>> bins = new ConcurrentHashMap<Integer, HeaderFormat<String>>();

	private Map<String, SampleObject<String>> samples = new ConcurrentHashMap<String, SampleObject<String>>();

	private Map<Integer, SampleObject<String>> sampleIds = new ConcurrentHashMap<Integer, SampleObject<String>>();

	private Map<Long, Map<Integer, List<ContentObject<Double>>>> massspecsForDateAndBin;

	public ResultDataFileIndex(ResultDataFile file) {
		this.file = file;

	}

	public void build() throws BinBaseException {
		for (final HeaderFormat<String> binId : this.file.getBins()) {

			int id = Integer.parseInt(binId.getAttributes().get("id").toString());

			massspecs.put(id, file.getMassspecsForBin(id));

			bins.put(id, binId);
		}
		logger.info("index bins: " + massspecs.size());

		Set<Long> dates = new HashSet<Long>();

		for (final SampleObject<String> samples : this.file.getSamples()) {
			this.samples.put(samples.getValue(), samples);
			this.sampleIds.put(Integer.parseInt(samples.getAttributes().get("id")), samples);

			Long date = file.calculateTimeOfSample(samples.getValue());

			dates.add(date);
		}

		calculateDates(dates);

	}

	/**
	 * calculates the index over the dates
	 * 
	 * @param dates
	 * @throws BinBaseException
	 */
	private void calculateDates(Set<Long> dates) throws BinBaseException {
		logger.info("need to calculate dates: " + dates.size());
		
		this.massspecsForDateAndBin = new ConcurrentHashMap<Long, Map<Integer, List<ContentObject<Double>>>>();

		ExecutorService exec = Executors.newCachedThreadPool();

		for (final Long date : dates) {

			Callable<Long> callable = new Callable<Long>() {

				@Override
				public Long call() throws Exception {
					long begin = System.currentTimeMillis();
					if (massspecsForDateAndBin.containsKey(date) == false) {
						Map<Integer, List<ContentObject<Double>>> binSpecs = new HashMap<Integer, List<ContentObject<Double>>>();

						for (final HeaderFormat<String> binId : file.getBins()) {

							int id = Integer.parseInt(binId.getAttributes().get("id").toString());

							binSpecs.put(id, file.getMassspecsForBinAndDate(id, date));
						}

						massspecsForDateAndBin.put(date, binSpecs);
						long end = System.currentTimeMillis();
						
						logger.info("took " + ((end-begin)/1000) + " seconds for day: "+ date);
					}

					return date;
				}
			};

			exec.submit(callable);

		}

		ExecutorsServiceFactory.shutdownService(exec);
	}

	/**
	 * returns the massspecs
	 * 
	 * @param binId
	 * @return
	 */
	public List<ContentObject<Double>> getMassSpecs(int binId) {
		List<ContentObject<Double>> list = massspecs.get(binId);

		return list;
	}

	/**
	 * returns the given bin
	 * 
	 * @param id
	 * @return
	 */
	public HeaderFormat<String> getBin(final int id) {
		return bins.get(id);
	}

	/**
	 * the sample
	 * 
	 * @param name
	 * @return
	 */
	public SampleObject<String> getSample(final String name) {
		return samples.get(name);
	}

	/**
	 * the sample
	 * 
	 * @param name
	 * @return
	 */
	public SampleObject<String> getSample(final int id) {
		return sampleIds.get(id);
	}

	public void removeBin(int id) {
		this.bins.remove(id);
		this.massspecs.remove(id);
	}

	public void removeSample(String name) {
		SampleObject<String> sample = samples.get(name);
		if (sample != null) {
			Integer id = Integer.parseInt(sample.getAttributes().get("id").toString());
			this.samples.remove(name);
			this.sampleIds.remove(id);
		}
	}

	public List<ContentObject<Double>> getMassspecsForBinAndDate(int id, long date) {
		Map<Integer, List<ContentObject<Double>>> map = this.massspecsForDateAndBin.get(date);

		if (map != null) {
			return map.get(id);
		}
		return null;
	}
}
