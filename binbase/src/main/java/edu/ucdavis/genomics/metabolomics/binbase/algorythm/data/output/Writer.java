/*
 * Created on Feb 8, 2007
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.output;

import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;

import java.io.IOException;
import java.io.OutputStream;

/**
 * used to write stuff into stream
 * @author wohlgemuth
 * @version Feb 8, 2007
 *
 */
public interface Writer {
	
	public void write(OutputStream out, DataFile file)throws IOException;
	
	public void write(OutputStream out, Source content) throws IOException;
	
	public void write(OutputStream out, Object content) throws IOException;
	
	
	/**
	 * do we support datafiles
	 * @author wohlgemuth
	 * @version Feb 12, 2007
	 * @return
	 */
	public boolean isDatafileSupported();
	
	/**
	 * do we support sources
	 * @author wohlgemuth
	 * @version Feb 12, 2007
	 * @return
	 */
	public boolean isSourceSupported();

	
}
