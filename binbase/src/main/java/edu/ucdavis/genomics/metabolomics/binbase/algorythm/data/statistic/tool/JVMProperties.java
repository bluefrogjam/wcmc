package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.tool;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.annotation.Unique;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.ResultDataFile;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.action.Action;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.SimpleDatafile;

/**
 * generates jvm properties into a file
 * @author wohlgemuth
 *
 */
@Unique
public class JVMProperties extends BasicProccessable implements Action{

	private String column;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private List<Element> transformInstructions;

	protected List<Element> getTransformInstructions() {
		return transformInstructions;
	}
	

	@Override
	public void setTransformInstructions(List<Element> transformInstructions) {
		this.transformInstructions = transformInstructions;
	}
	
	@Override
	public String getFolder() {
		return "report";
	}
	
	@Override
	public boolean writeResultToFile() {
		return false;
	}
	
	@Override
	public DataFile process(ResultDataFile datafile, Element configuration) throws BinBaseException {
	    DataFile file =  execute();
	    
		try {
			writeObject(file, configuration, "jvm statistics");
		} catch (IOException e) {
			throw new BinBaseException(e);
		}
	    return null;
	}

	private DataFile execute() {
		RuntimeMXBean mx = ManagementFactory.getRuntimeMXBean();

	    Date begin = new Date(mx.getStartTime());
	    
	    SimpleDatafile dataFile = new SimpleDatafile();
	    
	    dataFile.addEmptyColumn("name");
	    dataFile.addEmptyColumn("value");
	    
	    dataFile.addEmptyRow("");
	    dataFile.addEmptyRow("");
	    dataFile.addEmptyRow("");
	    dataFile.addEmptyRow("");
	    
	    dataFile.setCell(0, 1, "start time");
	    dataFile.setCell(0, 2, "uptime in seconds");
	    dataFile.setCell(0, 3, "uptime in minutes");
	    
	    dataFile.setCell(1, 1, begin);
	    dataFile.setCell(1, 2, mx.getUptime()/1000);
	    dataFile.setCell(1, 3, ((double)mx.getUptime())/1000/60);
	    
	    
		return dataFile;
	}

	@Override
	public void run(Element configuration, Source rawdata, Source sop) {
		DataFile file = execute();
		try {
			writeObject(file, configuration, "result");
		}
		catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
	}

	@Override
	public void setColumn(String column) {
		this.column = column;
	}

	
	public String getDescription(){
		return "a report of the runtime statistics";
	}
}
