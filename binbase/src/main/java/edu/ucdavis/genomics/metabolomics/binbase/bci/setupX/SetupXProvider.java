/**
 *
 */
package edu.ucdavis.genomics.metabolomics.binbase.bci.setupX;

import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;

/**
 * @author wohlgemuth is an interface, used to communicate with a setup
 *         configuration
 */
public interface SetupXProvider {

    /**
     * @param sampleName
     * @return the setupX id for this sample or null if nothing found
     */
    String getSetupXId(String sampleName) throws BinBaseException;

    /**
     * send the calculated result to setupX
     *
     * @param string
     * @param content
     * @throws BinBaseException
     * @author wohlgemuth
     * @version Feb 9, 2006
     */
    void upload(String experimentId, String content) throws BinBaseException;

    void upload(String experimentId, byte[] data) throws BinBaseException;

    /**
     * can this setupxId create new bins
     *
     * @param setupxId
     * @return
     * @throws BinBaseExcpetion
     */
    boolean canCreateBins(String setupxId) throws BinBaseException;
}
