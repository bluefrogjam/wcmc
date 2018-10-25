package edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx;

import edu.ucdavis.genomics.metabolomics.binbase.bci.server.exception.FileNotFoundException;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;

import javax.ejb.Remote;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Created by wohlgemuth on 10/18/16.
 */
public interface ExportJMXFacade {
    /**
     * @jmx.managed-operation description="cache"
     */
    boolean isEnableCache();

    /**
     * @jmx.managed-operation description="cache"
     */
    void setEnableCache(boolean enableCache);

    /**
     * @param string
     * @jmx.managed-operation description = "adds a net cdf directory"
     * @author wohlgemuth
     * @version Jul 16, 2006
     */
    void addNetCDFDirectory(String string);

    /**
     * @param string
     * @jmx.managed-operation description = "removes a net cdf directoy"
     * @author wohlgemuth
     * @version Jul 16, 2006
     */
    void removeNetCDFDirectory(String string);

    /**
     * @jmx.managed-operation description = "delete all netcdf directories"
     * @author wohlgemuth
     * @version Jul 16, 2006
     */
    void clearNetCDFDirectorys();

    /**
     * @return
     * @throws BinBaseException
     * @jmx.managed-operation description = "return all registered net cdf
     * directories where sample files can be"
     * @author wohlgemuth
     * @version Apr 21, 2006
     */
    Collection getNetCDFDirectories() throws BinBaseException;

    /**
     * @jmx.managed-operation description="clustered statistics"
     */
    Collection getSopDirs();

    /**
     * @throws FileNotFoundException
     * @jmx.managed-operation description="clustered statistics"
     */
    void addSopDir(String dir) throws FileNotFoundException;

    /**
     * @jmx.managed-operation description="clustered statistics"
     */
    void clearSopDirs();

    /**
     * @jmx.managed-operation description="clustered statistics"
     */
    void removeSopDirectory(String dir);

    /**
     * @jmx.managed-operation description="clustered statistics"
     */
    String getResultDirectory();

    /**
     * @jmx.managed-operation description="clustered statistics"
     */
    void setResultDirectory(String resultDirectory)
            throws FileNotFoundException;

    /**
     * @throws IOException
     * @throws java.io.FileNotFoundException
     * @jmx.managed-operation description="clustered statistics"
     */
    void uploadResult(String sopName, byte[] content)
            throws  IOException;

    /**
     * @throws IOException
     * @throws java.io.FileNotFoundException
     * @jmx.managed-operation description="clustered statistics"
     */
    void uploadSopToDir(String sopName, String dir, byte[] content)
            throws  IOException;

    /**
     * @throws IOException
     * @throws java.io.FileNotFoundException
     * @jmx.managed-operation description="clustered statistics"
     */
    void uploadNetCdfToDir(String fileName, String dir, byte[] content)
            throws  IOException;

    /**
     * @throws IOException
     * @throws java.io.FileNotFoundException
     * @throws FileNotFoundException
     * @jmx.managed-operation description="clustered statistics"
     */
    byte[] getSop(String sopName) throws
            IOException, FileNotFoundException;

    /**
     * @throws IOException
     * @throws java.io.FileNotFoundException
     * @throws FileNotFoundException
     * @jmx.managed-operation description="clustered statistics"
     */
    String printSoap(String sopName)
            throws FileNotFoundException,
            IOException;

    /**
     * @jmx.managed-operation description="clustered statistics"
     */
    List listSops();

    /**
     * @jmx.managed-operation description="clustered statistics"
     */
    List listResults();

    /**
     * @jmx.managed-operation description="clustered statistics"
     */
    byte[] getResult(String resultName)
            throws IOException,
            FileNotFoundException;

    /**
     * @jmx.managed-operation description="clustered statistics"
     */
    boolean hasResult(String resultName);

    /**
     * @throws BinBaseException
     * @jmx.managed-operation description="clustered statistics"
     */
    boolean hasCDF(String sampleName) throws BinBaseException;

    /**
     * @param sampleName
     * @return
     * @throws BinBaseException
     * @throws IOException
     * @jmx.managed-operation description="clustered statistics"
     */
    byte[] downloadFile(String sampleName) throws BinBaseException,
            IOException;

    /**
     * @throws java.io.FileNotFoundException
     * @throws IOException
     * @jmx.managed-operation description="clustered statistics"
     */
    void uploadSop(String sopName, byte[] content)
            throws FileNotFoundException,
            IOException;

    /**
     * @throws IOException
     * @throws BinBaseException
     * @jmx.managed-operation description="clustered statistics"
     */
    void uploadNetCdf(String fileName, byte[] content)
            throws IOException, BinBaseException;

    /**
     * @return
     * @jmx.managed-operation description = "return the default sop"
     */
    String getDefaultSop();

    /**
     * @return
     * @jmx.managed-operation description = "return the default sop"
     */
    void setDefaultSop(String defaultSop);

    /**
     * @throws BinBaseException
     * @jmx.managed-operation description = "generates the sample name"
     */
    String generateFileName(String dir, String sampleName);
}
