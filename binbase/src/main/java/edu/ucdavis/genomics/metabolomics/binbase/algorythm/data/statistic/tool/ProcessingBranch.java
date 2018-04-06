package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.tool;

import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.ResultDataFile;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;

public class ProcessingBranch extends BasicProccessable{

	@Override
	public DataFile process(ResultDataFile datafile, Element configuration)
			throws BinBaseException {
		return null;
	}

	@Override
	public String getFolder() {
		return "";
	}

	@Override
	public String getDescription() {
		return "this defines a branch in the processing step and will not modify the datafile. Underlaying tasks, might modify the dataset depending on the implementation!";
	}

}
