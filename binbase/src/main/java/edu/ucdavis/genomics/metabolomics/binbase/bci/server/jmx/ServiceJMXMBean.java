package edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx;

import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by wohlgemuth on 10/18/16.
 */
public interface ServiceJMXMBean extends
        javax.management.MBeanRegistration{
    /**
     * @jmx.managed-operation description = "return all registered directories
     *                        where sample files can be"
     * @author wohlgemuth
     * @version Apr 21, 2006
     * @return
     * @throws BinBaseException
     */
    Collection getImportDirectories() throws BinBaseException;

    /**
     * @jmx.managed-operation description = "adds a directory"
     * @author wohlgemuth
     * @version Jul 16, 2006
     * @param string
     */
    void addDirectory(String string);

    /**
     * @jmx.managed-operation description = "removes a directoy"
     * @author wohlgemuth
     * @version Jul 16, 2006
     * @param string
     */
    void removeDirectory(String string);

    /**
     * @jmx.managed-operation description = "delete all directories"
     * @author wohlgemuth
     * @version Jul 16, 2006
     */
    void clearDirectorys();

    /**
     * @throws IOException
     * @throws BinBaseException
     * @jmx.managed-operation description="clustered statistics"
     */
    void uploadImportFile(String fileName, byte[] content)
            throws FileNotFoundException, IOException, BinBaseException;

    /**
     * @throws IOException
     * @throws BinBaseException
     * @jmx.managed-operation description="clustered statistics"
     */
    void uploadConfigFile(String fileName, byte[] content)
            throws FileNotFoundException, IOException, BinBaseException;

    /**
     * @throws IOException
     * @throws BinBaseException
     * @jmx.managed-operation description="clustered statistics"
     */
    Collection listConfigFiles();

    /**
     * @throws IOException
     * @throws BinBaseException
     * @jmx.managed-operation description="clustered statistics"
     */
    byte[] getConfigFile(String fileName) throws IOException;

    /**
     * @throws IOException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws BinBaseException
     * @jmx.managed-operation description="clustered statistics"
     */
    void uploadFileToDir(String fileName, String directory,
                         byte[] content) throws FileNotFoundException, IOException;

    /**
     * @jmx.managed-operation description = "are we validating if files actually
     *                        exist"
     * @return
     */
    boolean isValidateSources();

    /**
     * @jmx.managed-operation description = "are we validating if files actually
     *                        exist"
     * @param validateSources
     */
    void setValidateSources(boolean validateSources);

    /**
     * @jmx.managed-operation description = "finds out if the given sample
     *                        exists on the harddrive"
     * @throws BinBaseException
     */
    boolean sampleExist(String sampleName) throws BinBaseException;

    /**
     * @jmx.managed-operation description="clustered statistics"
     * @param sampleName
     * @return
     * @throws BinBaseException
     * @throws IOException
     */
    byte[] downloadFile(String sampleName) throws BinBaseException,
            IOException;

    /**
     * @jmx.managed-operation description = "generates the sample name"
     * @throws BinBaseException
     */
    String generateFileName(String dir, String sampleName);

    /**
     * @jmx.managed-operation description = "name of the known databases"
     * @throws BinBaseException
     */
    String[] getDatabases();

    /**
     * @jmx.managed-operation description = "name of the known databases"
     * @throws BinBaseException
     */
    void addDatabase(String databaseName);

    /**
     * @jmx.managed-operation description = "name of the known databases"
     * @throws BinBaseException
     */
    void removeDatabase(String databaseName);

    /**
     * @jmx.managed-operation description = "name of the known databases"
     * @throws BinBaseException
     */
    void resetDatabases();
}
