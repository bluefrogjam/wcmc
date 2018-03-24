package edu.ucdavis.genomics.metabolomics.binbase.minix.jmx;

import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;

import javax.ejb.Stateless;
import javax.management.*;

/**
 * Created by wohlgemuth on 10/21/16.
 */
@Stateless
public class MiniXConfigurationJMXFacadeBean implements MiniXConfigurationJMXFacade{

    @Override
    public String getUrl() {
        sync();
        return bean.getUrl();
    }

    @Override
    public void setUrl(String url) {
        sync();
        bean.setUrl(url);
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
    public boolean canCreateBins(String setupxId) throws BinBaseException {
        sync();
        return bean.canCreateBins(setupxId);
    }

    @Override
    public void register() {
        sync();
        bean.register();
    }

    @Override
    public void unregister() {
        sync();
        bean.unregister();
    }

    private MiniXConfigurationJMX bean;


     void sync() {
        MBeanServer server = MBeanServerFactory.findMBeanServer(null).get(0);

        try {
            bean = MBeanServerInvocationHandler
                    .newProxyInstance(server, new ObjectName(
                                    "binbase.miniX:service=MiniXConfigurationJMX"),
                            MiniXConfigurationJMX.class, false);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }
}
