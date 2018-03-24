

package edu.ucdavis.genomics.metabolomics.binbase.dsl.processable.statistic

import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.SampleObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.HeaderFormat;

import org.slf4j.Logger;
import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.ResultDataFile;
import edu.ucdavis.genomics.metabolomics.binbase.dsl.processable.SQLProcessable;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.SimpleDatafile;

/**
 * contains a list of the generated bins in this dataset
 * @author wohlgemuth
 *
 */
class GeneratedBinForDataFile extends SQLProcessable{
	
	private Logger logger = LoggerFactory.getLogger(getClass())
	
	@Override
	protected DataFile processWithSQL(ResultDataFile dataFile,
	Connection connection, Element configuration) {
		
		Set<Integer> bins = generatedBinsInDataFile(dataFile, connection)
		DataFile file = new SimpleDatafile()
		file.addEmptyColumn "generated bin id"
		bins.each {int value ->			
			file.addRow([value])
		}
		
		//output the data to the zipfile
		writeObject(file, configuration, "generatedBins");
		
		return file;
	}
	
	/**
	 * returns a set containing all bins, which were generated in this dataFile
	 * @param dataFile
	 * @param connection
	 * @return
	 */
	protected Set<Integer> generatedBinsInDataFile(ResultDataFile dataFile, Connection connection) {
		logger.info "generating list of generated bins in this dataset"
		Set samples = new HashSet()
		
		//build list of samples of this experimenet
		dataFile.getSamples().each {SampleObject<String> sample ->
			samples.add Integer.parseInt (sample.attributes['id'].toString())
		}
		
		//fetch bins
		PreparedStatement statment = connection.prepareStatement ("select name, bin_id from bin where sample_id = ?")
		
		Set binIdsGenerated = new HashSet()
		samples.each { int id -> 
			statment.setInt( 1, id)
			ResultSet res = statment.executeQuery()
			
			if(res.next()){
				binIdsGenerated.add (res.getInt (1))
			}	
			res.close()
		}
		
		statment.close()
		
		return binIdsGenerated
	}
	
	@Override
	public String getFolder() {
		return "statistics";
	}
	
	
	public String getDescription(){
		return "a report of all created bins in this datafile"
	}
}
