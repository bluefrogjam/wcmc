package edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching;

import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import org.slf4j.Logger;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.SampleMatcher;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.export.SampleExport;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.methods.CorrectionMethod;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.methods.GitterMethode;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.methods.correction.CorrectionCache;
import edu.ucdavis.genomics.metabolomics.binbase.bci.Configurator;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.database.ConnectionFactory;
import edu.ucdavis.genomics.metabolomics.util.database.SimpleConnectionFactory;
import edu.ucdavis.genomics.metabolomics.util.io.dest.DestinationFactory;
import edu.ucdavis.genomics.metabolomics.util.thread.locking.LockableFactory;

/**
 * simplistic matcher, which matches one sample at a time
 *
 * @author wohlgemuth
 */
public class SimpleSampleMatcher implements SampleMatcher {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Set<Integer> samples = new HashSet<Integer>();

    private String connectionIdentifier;

    public SimpleSampleMatcher() {
    }

    @Override
    public void addSampleToCalculate(Integer sampleId) {
        samples.add(sampleId);
    }

    @Override
    public void matchSamples() throws BinBaseException {
        Iterator<Integer> it = samples.iterator();
        try {

            ConnectionFactory fact = createFactory(connectionIdentifier);
            Connection connection = fact.getConnection();

            while (it.hasNext()) {

                final Integer in = it.next();

                matchSample(in, connection);
            }

            fact.close(connection);
        } catch (Exception e) {
            throw new BinBaseException(e);
        }

        samples.clear();
    }

    /**
     * creates a connection factory
     *
     * @param classname
     * @return
     * @throws RemoteException
     * @throws BinBaseException
     * @throws CreateException
     * @throws NamingException
     */
    protected ConnectionFactory createFactory(String classname)
            throws RemoteException, BinBaseException, CreateException,
            NamingException {
        ConnectionFactory factory = ConnectionFactory.createFactory();
        Properties p = Configurator.getDatabaseService().getProperties();
        p.setProperty(SimpleConnectionFactory.KEY_USERNAME_PROPERTIE, classname);

        factory.setProperties(p);

        return factory;
    }

    /**
     * matches a single sample and updates the quantification table
     *
     * @param in
     * @param connection
     * @throws Exception
     * @throws IOException
     * @throws ConfigurationException
     */
    protected final void matchSample(Integer in, Connection connection)
            throws Exception, IOException, ConfigurationException {

        try {

            logger.info("checking if RI-Curve is available");

            CorrectionCache cache = new CorrectionCache();
            cache.setConnection(connection);

            //if caching is disabled or the curve hasn't been found
            if (!cache.isCached(in) || !Configurator.getExportService().isEnableCache()) {
                Thread.currentThread().setName(
                        "retention correction: " + in);
                logger.info("updating curve...");
                CorrectionMethod method = new CorrectionMethod();
                method.setNewBinAllowed(false);
                method.setConnection(connection);
                method.setSampleId(in);

                try {
                    method.run();
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                }
            } else {
                logger.info("curve is up to date");
            }

            Thread.currentThread().setName("postmatching: " + in);
            logger.info("postmatching: " + in);
            GitterMethode method = new GitterMethode();
            method.setConnection(connection);
            method.setNewBinAllowed(false);
            method.setSampleId(in);
            method.run();

            // update table
            Thread.currentThread().setName("exporting xml: " + in);
            logger.info("update table: " + in);
            SampleExport export = new SampleExport();
            export.setConnection(connection);

            String xmlContent = export.getQuantifiedSampleAsXml(in);
            Map<String, Connection> map = new HashMap<String, Connection>();
            map.put("CONNECTION", connection);

            if (xmlContent != null) {
                byte[] result = xmlContent.getBytes();
                OutputStream out = DestinationFactory
                        .newInstance(
                                QuantificationTableDestinationFactoryImpl.class
                                        .getName()).createDestination(in, map)
                        .getOutputStream();
                out.write(result);
                out.flush();
                out.close();
                method = null;
                result = null;
                System.gc();
            } else {
                logger.warn("no xml content was generated!");
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ConfigurationException(e);
        }

    }

    @Override
    public void setConnectionIdentifier(String name) {
        this.connectionIdentifier = name;
    }

    @Override
    public Integer getWorkLoad() {
        return samples.size();
    }

}
