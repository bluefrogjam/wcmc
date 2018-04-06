/*
 * Created on Jun 20, 2006
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data;

import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFileFactory;

public class ResultDataFileFactory extends DataFileFactory{

	@Override
	public DataFile createDataFile() {
		return new ResultDataFile();
	}

}
