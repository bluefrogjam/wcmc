package edu.ucdavis.genomics.metabolomics.binbase.minix.jmx;

import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;

import javax.ejb.Remote;

/**
 * Created by wohlgemuth on 10/21/16.
 */
@Remote
public interface MiniXConfigurationJMXFacade {

    /**
     * @return
     * @jmx.managed-operation description = "url of the webservice"
     */
    String getUrl();

    /**
     * @param url
     * @jmx.managed-operation description = "url of the webservice"
     */
    void setUrl(String url);

    /**
     * @return
     * @jmx.managed-operation description = "url of the webservice"
     */
    String getSetupXId(String sampleName) throws BinBaseException;

    /**
     * @return
     * @jmx.managed-operation description = "url of the webservice"
     */
    void upload(String experimentId, String content)
            throws BinBaseException;

    /**
     * @return
     * @jmx.managed-operation description = "url of the webservice"
     */
    boolean canCreateBins(String setupxId) throws BinBaseException;

    void register();

    void unregister();
}
