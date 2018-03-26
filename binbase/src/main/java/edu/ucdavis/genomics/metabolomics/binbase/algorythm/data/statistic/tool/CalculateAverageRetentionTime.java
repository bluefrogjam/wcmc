package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.tool;

import java.io.IOException;

import org.slf4j.Logger;
import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.annotation.Unique;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.Function;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.ResultDataFile;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.BinObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.SampleObject;

/**
 * calculates the average retention time for each bin based on the sample
 * information
 * 
 * @author wohlgemuth
 * 
 */
@Unique
public class CalculateAverageRetentionTime extends BasicProccessable  implements Processable {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public boolean writeResultToFile() {
		return false;
	}
	
	/**
	 * 
	 */
	public DataFile process(final ResultDataFile datafile, Element configuration)
			throws BinBaseException {

		try {
			logger.info("cloning data set...");
			final ResultDataFile file = (ResultDataFile) datafile.clone();
			logger.info("done...");

			datafile.applyFunction(new Function() {

				public Object apply(Object object, BinObject<String> bin,
						SampleObject<String> sample) throws NumberFormatException, BinBaseException {
					double value = file.getAverageRetentionTimeForBin(Integer
							.parseInt(bin.getAttributes().get("id")), sample
							.getValue());

					return value;
				}
			});
			try {
				writeObject(file, configuration, "averageRetentionTime");
			} catch (IOException e) {
				throw new BinBaseException(e);
			}

			return null;
		} catch (Exception e) {
			throw new BinBaseException(e);
		}
	}

	public String getFolder() {
		return "report";
	}

	
	public String getDescription(){
		return "calculates all the average retention times of this bin";
	}
}
