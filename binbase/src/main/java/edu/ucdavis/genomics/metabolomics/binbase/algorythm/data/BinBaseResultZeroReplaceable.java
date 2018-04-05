/*
 * Created on Jun 21, 2006
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data;

import edu.ucdavis.genomics.metabolomics.util.config.Configable;
import edu.ucdavis.genomics.metabolomics.util.statistics.replacement.ZeroReplaceable;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.BinObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.NullObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.SampleObject;
import org.slf4j.Logger;

import java.util.HashMap;


import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.Describeable;

/**
 * a specialiced replaceable class for binbase files and calculations
 * 
 * @author wohlgemuth
 * @version Jun 21, 2006
 */
public abstract class BinBaseResultZeroReplaceable implements ZeroReplaceable,Configable,Describeable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ResultDataFile file;

	private boolean sampleBase;

	public abstract String getFolder();
	
	/**
	 * we only want to have synchronized access to the actual file
	 * @return
	 */
	public ResultDataFile getFile() {
		return file;
	}

	public void setFile(ResultDataFile file) {
        getLogger().info("setting result data file...");
		this.file = file;
		
		fireFileSet(file);
	}

	/**
	 * allows us todo things once a file was set
	 * @param file2
	 */
	protected void fireFileSet(ResultDataFile file2) {
		
	}

	public boolean isSampleBased() {
		return sampleBase;
	}

	public void setSampleBased(boolean sampleBase) {
		this.sampleBase = sampleBase;
	}

	protected Logger getLogger(){
		return LoggerFactory.getLogger(getClass());
	}
	/**
	 * provides the missing data for null objects, basically the bin_id and
	 * replaces null with an NullObject
	 */
	public void provideMissingDataForNullObjects() {
		getLogger().debug("replacing null content with null objects");
		
		for (int i = 0; i < this.file.getTotalRowCount(); i++) {
			for (int x = 0; x < this.file.getTotalColumnCount(); x++) {
				Object o = file.getCell(x, i);

				boolean skip = false;
				// igniore headers
				if (!file.skipRowIndex(i)) {
					// ignore first columns
					if (!file.skipColumnIndex(x)) {
						try {
							if (file.isZero(o)) {
								BinObject<String> bin = file.getBinForColumn(x);
								SampleObject<String> sample =file.getSampleForRow(i);

								HashMap attributes = new HashMap();
								attributes.put("id", bin.getAttributes().get("id"));
								attributes.put("sample_id", sample.getAttributes().get("id"));

								o = new NullObject(new Double(0.0), attributes);
								getLogger().debug("replace null with NullObject: " + o);
								file.setCell(x, i, o);
							}
						}
						catch (NumberFormatException e) {
						}
					}
					else{
						LoggerFactory.getLogger(getClass()).trace("ignoring this column, since its not relevant: " +x );
					}
				}
				else{
					LoggerFactory.getLogger(getClass()).trace("ignoring this row, since its not relevant: " +i );
				}
			}
		}
	}

	/**
	 * supposed to only run ones and does complex initializations
	 */
	public  void initializeReplaceable(){
		
	}
	/**
	 * are all requirements provided
	 * 
	 * @author wohlgemuth
	 * @version Jul 13, 2006
	 * @return
	 */
	public abstract boolean isValid();

}
