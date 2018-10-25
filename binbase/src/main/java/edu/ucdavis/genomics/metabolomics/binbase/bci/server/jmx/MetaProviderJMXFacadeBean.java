package edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx;

import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.management.*;
import java.io.Serializable;
import java.util.Collection;

/**
 * Created by wohlgemuth on 10/18/16.
 */
@Stateless
@Remote(MetaProviderJMXFacade.class)
public class MetaProviderJMXFacadeBean implements Serializable,MetaProviderJMXFacade {
    @Override
    public void addProvider(String providerClass) {
        sync();

        bean.addProvider(providerClass);
    }

    @Override
    public void removeProvider(String providerClass) {
        sync();

        bean.removeProvider(providerClass);
    }

    @Override
    public Collection getProvider() {
        sync();

        return bean.getProvider();
    }

    @Override
    public boolean isProviderRegistered(String providerClass) {
        sync();

        return bean.isProviderRegistered(providerClass);
    }

    @Override
    public String getSetupXId(String sampleName) throws BinBaseException {
        sync();

        return bean.getSetupXId(sampleName);
    }

    @Override
    public void upload(String experimentId, String content) throws BinBaseException {
        sync();

        bean.upload(experimentId, content);
    }

    @Override
    public void upload(String experimentId, byte[] data) throws BinBaseException {
        sync();

        bean.upload(experimentId, data);
    }

    @Override
    public boolean canCreateBins(String setupxId) throws BinBaseException {
        sync();
        return bean.canCreateBins(setupxId);
    }

    private MetaProviderJMXMBean bean;


    protected void sync() {
        MBeanServer server = MBeanServerFactory.findMBeanServer(null).get(0);

        try {
            bean = MBeanServerInvocationHandler
                    .newProxyInstance(server, new ObjectName(
                                    "binbase:service=MetaProvider"),
                            MetaProviderJMXMBean.class, false);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }
}

