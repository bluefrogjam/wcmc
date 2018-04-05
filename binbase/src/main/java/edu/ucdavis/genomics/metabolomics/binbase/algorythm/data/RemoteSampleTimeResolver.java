package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;

import edu.ucdavis.genomics.metabolomics.binbase.bci.server.BinBaseService;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;

/**
 * uses the ejb server to calculate the access time for a sample
 * 
 * @author wohlgemuth
 */
public class RemoteSampleTimeResolver implements SampleTimeResolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Map<String, Long> cache = new ConcurrentHashMap<String, Long>();

	@Override
	public long resolveTime(String sample) throws BinBaseException {
		Logger logger = LoggerFactory.getLogger(getClass());
		if (cache.get(sample) != null) {
			logger.info("time for sample was cached...");
			return cache.get(sample);
		}
		try {
			logger.info("need to access service to calculate time, since it was not cached: " + sample);

			final BinBaseService service = BinBaseServiceFactory
					.createFactory().createService();

			Long time = service.getTimeStampForSample(sample);
			logger.info("received time was: " + time);
			cache.put(sample, time);

			return time;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new BinBaseException(e.getMessage(), e);
		}

	}

}
