package edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx;

import edu.ucdavis.genomics.metabolomics.binbase.bci.setupX.SetupXProvider;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;

import javax.ejb.Remote;
import java.util.Collection;

/**
 * Created by wohlgemuth on 10/18/16.
 */
public interface MetaProviderJMXFacade extends SetupXProvider {
    /**
     * @param providerClass
     * @jmx.managed-operation description = "adds a new setup x provider" adds a
     * new setup x provider
     */
    void addProvider(String providerClass);

    /**
     * @param providerClass
     * @jmx.managed-operation description = "removes a setupX provider" removes
     * a setupX provider
     */
    void removeProvider(String providerClass);

    /**
     * @return
     * @jmx.managed-attribute description = "returns allProvider which are
     * possible to use" returns allProvider which are
     * possible to use
     */
    Collection getProvider();

    /**
     * @param providerClass
     * @return true if the providerClass already added, false if the provider
     * class is not added
     * @jmx.managed-operation description = "true if the providerClass already
     * added, false if the provider class is not added"
     */
    boolean isProviderRegistered(String providerClass);

    /**
     * @jmx.managed-operation description = "setupx"
     */
    String getSetupXId(String sampleName) throws BinBaseException;

    /**
     * @jmx.managed-operation description = "setupx"
     */
    void upload(String experimentId, String content)
            throws BinBaseException;

    /**
     * by default it should always return true, only if one implementation says no, we do not generate new bins
     *
     * @jmx.managed-operation description = "setupx"
     */
    boolean canCreateBins(String setupxId) throws BinBaseException;

    void upload(String experimentId, byte[] data) throws BinBaseException;
}
