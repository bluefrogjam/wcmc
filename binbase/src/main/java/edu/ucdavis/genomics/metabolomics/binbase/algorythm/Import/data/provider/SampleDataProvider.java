/*
 * Created on Nov 8, 2005
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.provider;

import java.util.Map;

import edu.ucdavis.genomics.metabolomics.util.io.source.Source;

/**
 * @author wohlgemuth
 * @version Nov 8, 2005
 *
 * is used to provide the BinBase with data for the import of data
 */
public interface SampleDataProvider{
    /**
     * returns all massspec of this sample
     * @author wohlgemuth
     * @version Nov 8, 2005
     * @return
     */
    public Map[] getSpectra() throws Exception;
    
    /**
     * set the source of the data
     * @author wohlgemuth
     * @version Nov 8, 2005
     * @param source
     */
    public void setSource(Source source);
    
    /**
     * does some internal stuff
     * @author wohlgemuth
     * @version Nov 9, 2005
     */
    public void run() throws Exception;
}
