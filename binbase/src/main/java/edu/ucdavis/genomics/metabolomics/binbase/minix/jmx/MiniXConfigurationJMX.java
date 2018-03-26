package edu.ucdavis.genomics.metabolomics.binbase.minix.jmx;

import edu.ucdavis.genomics.metabolomics.binbase.bci.Configurator;
import edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx.MetaProviderJMXMBean;
import edu.ucdavis.genomics.metabolomics.binbase.bci.setupX.SetupXProvider;
import edu.ucdavis.genomics.metabolomics.binbase.cluster.util.JBosssPropertyHolder;
import edu.ucdavis.genomics.metabolomics.binbase.minix.wsdl.MiniXProvider;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import org.slf4j.Logger;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import java.io.*;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public class MiniXConfigurationJMX implements
        SetupXProvider, MiniXConfigurationJMXMBean {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String url;

    private boolean automaticURLDetection = false;


    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;

        this.unregister();
        this.register();

        this.save();
    }

    /**
     * @author wohlgemuth
     * @version Mar 30, 2006
     */
    @Override
    public void register() {
        try {
            logger.warn("registering provider");

            MBeanServer server = MBeanServerFactory.findMBeanServer(null).get(0);

            MBeanServerInvocationHandler
                    .newProxyInstance(server, new ObjectName(
                                    "binbase:service=MetaProvider"),
                            MetaProviderJMXMBean.class, false).addProvider(MiniXProvider.class.getName());


        } catch (Exception e) {
            logger.error("could'nt register the service", e);
        }

    }

    public void postDeregister() {
        unregister();
    }

    public void preDeregister() throws Exception {
    }

    /**
     * @author wohlgemuth
     * @version Mar 30, 2006
     */
    @Override
    public void unregister() {
        try {
            MBeanServer server = MBeanServerFactory.findMBeanServer(null).get(0);

            MBeanServerInvocationHandler
                    .newProxyInstance(server, new ObjectName(
                                    "binbase:service=MetaProvider"),
                            MetaProviderJMXMBean.class, false).removeProvider(MiniXProvider.class.getName());

        } catch (Exception e) {
            logger.error("could'nt unregister the service", e);
        }
    }

    private void save() {

        try {
            File file = JBosssPropertyHolder.getPropertyFile(getClass());
            Properties p = new Properties();

            p.setProperty("url", this.url);

            FileOutputStream out = new FileOutputStream(file);
            p.store(out, "-- no comment --");
            out.close();

        } catch (InvalidPropertiesFormatException e) {
            logger.error("postRegister(Boolean)", e); //$NON-NLS-1$
        } catch (FileNotFoundException e) {
            logger.error("postRegister(Boolean)", e); //$NON-NLS-1$
        } catch (IOException e) {
            logger.error("postRegister(Boolean)", e); //$NON-NLS-1$
        }
    }

    public ObjectName preRegister(MBeanServer arg0, ObjectName arg1)
            throws Exception {
        Properties p = new Properties();

        try {
            File file = JBosssPropertyHolder.getPropertyFile(getClass());

            if (file.exists()) {
                FileInputStream in = new FileInputStream(file);
                p.load(in);

                if (p.getProperty("url") != null) {
                    this.setUrl(p.getProperty("url"));
                }
            } else {
                this.setUrl("http://minix.fiehnlab.ucdavis.edu/services/communications?wsdl");
            }


        } catch (InvalidPropertiesFormatException e) {
            logger.error("postRegister(Boolean)", e); //$NON-NLS-1$
        } catch (FileNotFoundException e) {
            logger.error("postRegister(Boolean)", e); //$NON-NLS-1$
        } catch (IOException e) {
            logger.error("postRegister(Boolean)", e); //$NON-NLS-1$

        }
        return arg1;
    }

    /**
     * add our service to the mainservice as implmentation
     *
     * @author wohlgemuth
     * @version Feb 9, 2006
     * @see javax.management.MBeanRegistration#postRegister(Boolean)
     */
    public void postRegister(Boolean arg0) {
    }


    @Override
    public String getSetupXId(String sampleName) throws BinBaseException {
        logger.info("looking up: " + sampleName);
        return new MiniXProvider().getSetupXId(sampleName);
    }

    @Override
    public void upload(String experimentId, String content)
            throws BinBaseException {
        logger.info("using minix provider to upload: " + experimentId);
        new MiniXProvider().upload(experimentId, content);
    }

    public void upload(String experimentId, byte[] data) throws BinBaseException {
        logger.info("using minix provider to upload: " + experimentId);
        new MiniXProvider().upload(experimentId, data);

    }

    /**
     * @return
     * @jmx.managed-operation description = "url of the webservice"
     */
    public boolean canCreateBins(String setupxId) throws BinBaseException {
        return new MiniXProvider().canCreateBins(setupxId);
    }
}
