/**
 *
 */
package edu.ucdavis.genomics.metabolomics.binbase.bci.server;

import edu.ucdavis.genomics.metabolomics.binbase.bci.Configurator;
import edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx.ExportJMXFacade;
import edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx.ServiceJMXFacade;
import edu.ucdavis.genomics.metabolomics.binbase.bci.server.types.DSL;
import edu.ucdavis.genomics.metabolomics.binbase.cluster.util.JMSScheduler;
import edu.ucdavis.genomics.metabolomics.binbase.cluster.util.SchedulingObject;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.database.ConnectionFactory;
import edu.ucdavis.genomics.metabolomics.util.database.SimpleConnectionFactory;
import edu.ucdavis.genomics.metabolomics.util.database.WrappedConnection;
import org.slf4j.Logger;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.Queue;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

@Stateless
public class BinBaseServiceBean implements BinBaseService, Serializable {

    private static final long serialVersionUID = 2L;

    private Logger logger = LoggerFactory.getLogger(BinBaseServiceBean.class);


    @Resource
    private javax.jms.ConnectionFactory factory;

    @Resource(mappedName="java:/queue/binbase")  // inject Queue (more)
    private Queue target;


    private void storeSample(Connection connection, ExperimentSample sample)
            throws Exception {
        logger.info("store file: " + sample.getName());
        FileStoreUtil util = new FileStoreUtil();
        util.storeFile(sample.getName(), connection);
    }

    public void storeSample(ExperimentSample sample, String column)
            throws BinBaseException {

        Connection connection = getConnection(column);

        try {
            storeSample(connection, sample);
        } catch (Exception e) {
            throw new BinBaseException(e);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * checks if we have the specified netcdf sample
     *
     * @param sampleName
     * @return
     * @throws BinBaseException
     */
    public boolean hasNetCdfFile(String sampleName)
            throws BinBaseException {


        try {
            logger.debug("create export jmx connection");
            ExportJMXFacade local = Configurator.getExportServiceLocal();
            return local.hasCDF(sampleName);
        } catch (BinBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BinBaseException(e);
        } finally {
            logger.debug("done with providing netcdf files");
        }
    }

    /**
     * checks if we have the specified netcdf sample
     *
     * @param sampleName
     * @return
     * @throws BinBaseException
     */
    public boolean hasTextFile(String sampleName)
            throws BinBaseException {


        try {
            logger.debug("create export jmx connection");
            ServiceJMXFacade local = Configurator.getImportServiceLocal();

            return local.sampleExist(sampleName);
        } catch (BinBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BinBaseException(e);
        } finally {
            logger.debug("done with providing netcdf files");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.ucdavis.genomics.metabolomics.binbase.bci.server.BinBaseService#
     * getNetCdfFile(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public byte[] getNetCdfFile(String sampleName)
            throws BinBaseException {
        try {

            logger.debug("create export jmx connection");
            ExportJMXFacade local = Configurator.getExportServiceLocal();

            byte[] content = local.downloadFile(sampleName);

            if (content == null) {

                throw new BinBaseException(
                        "sorry couldn't find the object in all paths: "
                                + sampleName);
            }
            return content;
        } catch (BinBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BinBaseException(e);
        } finally {
            logger.debug("done with providing netcdf files");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.ucdavis.genomics.metabolomics.binbase.bci.server.BinBaseService#
     * getAvailableSops(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public String[] getAvailableSops() throws BinBaseException {

        List<String> result;
        try {
            result = Configurator.getExportServiceLocal().listSops();
            String[] resultS = new String[result.size()];

            for (int i = 0; i < result.size(); i++) {
                resultS[i] = result.get(i);
            }
            return resultS;
        } catch (Exception e) {
            throw new BinBaseException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * edu.ucdavis.genomics.metabolomics.binbase.bci.server.BinBaseService#uploadSop
     * (java.lang.String, java.lang.String, java.lang.String)
     */
    public void uploadSop(String name, String xmlContent)
            throws BinBaseException {

        try {
            Configurator.getExportServiceLocal().uploadSop(name,
                    xmlContent.getBytes());
        } catch (Exception e) {
            throw new BinBaseException(e);
        }

    }

    /**
     * returns the registered columns
     */
    public String[] getRegisteredColumns() throws BinBaseException {


        try {
            ServiceJMXFacade facade = Configurator.getImportServiceLocal();
            return facade.getDatabases();
        } catch (Exception e) {
            throw new BinBaseException(e);
        }
    }


    private void closeConnection(Connection connection) {
        if (connection instanceof WrappedConnection) {
            WrappedConnection c = (WrappedConnection) connection;
            try {
                c.getConnection().close();
            } catch (SQLException e) {
                logger.warn(e.getMessage(), e);
            }
        }
    }

    /**
     * gives us a connection for the specified username
     */
    public Connection getConnection(String userName) throws BinBaseException {
        // helps us that postgres has case sensitive usernames
        userName = userName.toLowerCase();
        // take care of a connection

        try {

            ConnectionFactory factory = ConnectionFactory.createFactory();
            Properties p = Configurator.getDatabaseServiceLocal().getProperties();

            p.setProperty(SimpleConnectionFactory.KEY_USERNAME_PROPERTIE,
                    userName);

            factory.setProperties(p);
            logger.debug("done with factory setup!");

            return factory.getConnection();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BinBaseException(e);
        }
    }

    public long getTimeStampForSample(String sample)
            throws BinBaseException {


        try {
            // calculate from file
            long date = new FileStoreUtil().calculateTimeStamp(sample);

            // calculate from the database to look if there is already a
            // timestamp stored, if this is the case we use the existing
            // timestamp
            return date;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BinBaseException(e.getMessage());
        }
    }

    /**
     * schedules a DSL for processing
     * @param dsl
     * @throws BinBaseException
     */
    public void triggerDSLCalculations(DSL dsl)
            throws BinBaseException {
        try {
            JMSScheduler.getInstance().schedule(new SchedulingObject(dsl),1,"",target,factory);
        } catch (Exception e) {
            throw new BinBaseException(e);
        }
    }

}
