package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.tool;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.annotation.Unique;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.ResultDataFile;
import edu.ucdavis.genomics.metabolomics.binbase.bci.server.BinBaseService;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.SimpleDatafile;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.SampleObject;
import org.apache.commons.lang.time.FastDateFormat;
import org.slf4j.Logger;
import org.jdom2.Element;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * gives an overview of all samples of this experiment, there bins and the
 * average retentiontime of the bins
 * 
 * @author wohlgemuth
 * 
 */
@Component
public class ShowRealMeasurmentDatesForSamples extends BasicProccessable  implements
		Processable {

	@Autowired
	private BinBaseService binBaseService;
	Logger logger = LoggerFactory.getLogger(getClass());
	@Override
	public boolean writeResultToFile() {
		return false;
	}

	public DataFile process(ResultDataFile datafile, Element configuration) throws BinBaseException {

		//needed to format the dates
		FastDateFormat formatter = FastDateFormat.
		    getInstance( "yyyy-MM-dd",
		                         TimeZone.getDefault( ),
		                         Locale.getDefault( ) );

		//get the samples
		List<SampleObject<String>> samples = datafile.getSamples();
		
		//generates the sample date relations
		Map<Date, List<SampleObject<String>>> dates = generateRelations(samples);
		
		//stores our result data in it
		DataFile file = new SimpleDatafile();
		Set<Date> set = dates.keySet();

		//list of keys, just needed for sorting
		List<Date> keys = new Vector<Date>();
		
		//the max amount of samples to a date
		int max = 0;
		
		logger.info("calculating dimensions of result file");
		for(Date date : set){
			keys.add(date);
			if(dates.get(date).size() >= max){
				max = dates.get(date).size();
			}
		}
		
		//sorting all the keys
		Collections.sort(keys);
		
		//setting the max dimension
		file.setDimension(max+1,keys.size());

		//actually write the content out
		for(int i = 0; i < keys.size(); i++){
			file.setCell(i, 0, formatter.format(keys.get(i)));

			List<SampleObject<String>> list = dates.get(keys.get(i));

			for(int x = 0; x < list.size(); x++){
				
				file.setCell(i, x+1, list.get(x));
			}

		}
		
		try {
			writeObject(file, configuration, "file aquisition dates");
		} catch (IOException e) {
			throw new BinBaseException(e);
		}

		//done
		return null;
	}

	private Map<Date, List<SampleObject<String>>> generateRelations(
			List<SampleObject<String>> samples) throws BinBaseException {
		//stores the sample date relations
		Map<Date, List<SampleObject<String>>> dates = new HashMap<Date, List<SampleObject<String>>>();

		//needed to get the correct tiemstamps


		for(SampleObject<String> so : samples){
			logger.debug("calculate date for: " + so.getValue());
			
			Date date = new Date(binBaseService.getTimeStampForSample(so.getValue()));
			
			date = org.apache.commons.lang.time.DateUtils.round(date, Calendar.DAY_OF_MONTH);
			List<SampleObject<String>> list = dates.get(date);
			
			if(list == null){
				list = new Vector<SampleObject<String>>();
			}
			list.add(so);
			
			dates.put(date, list);
		}
		return dates;
	}


	public String getFolder() {
		return "report";
	}
	
	public String getDescription(){
		return "a report of the real measurement times of a sample";
	}
}
