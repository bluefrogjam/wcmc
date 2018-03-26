package edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx;

import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.management.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;

/**
 * Created by wohlgemuth on 10/18/16.
 */
@Remote(ServiceJMXFacade.class)
@Stateless
public class ServiceJMXFacadeBean implements Serializable,ServiceJMXFacade{
    @Override
    public Collection getImportDirectories() throws BinBaseException {
        sync();
        return bean.getImportDirectories();
    }

    @Override
    public void addDirectory(String string) {
        sync();
        bean.addDirectory(string);
    }

    @Override
    public void removeDirectory(String string) {
        sync();
        bean.removeDirectory(string);
    }

    @Override
    public void clearDirectorys() {
        sync();
        bean.clearDirectorys();
    }

    @Override
    public void uploadImportFile(String fileName, byte[] content) throws FileNotFoundException, IOException, BinBaseException {
        sync();
        bean.uploadImportFile(fileName, content);
    }

    @Override
    public void uploadConfigFile(String fileName, byte[] content) throws FileNotFoundException, IOException, BinBaseException {
        sync();
        bean.uploadConfigFile(fileName, content);
    }

    @Override
    public Collection listConfigFiles() {
        sync();
        return bean.listConfigFiles();
    }

    @Override
    public byte[] getConfigFile(String fileName) throws IOException {
        sync();
        return bean.getConfigFile(fileName);
    }

    @Override
    public void uploadFileToDir(String fileName, String directory, byte[] content) throws FileNotFoundException, IOException {
        sync();
        bean.uploadFileToDir(fileName, directory, content);
    }

    @Override
    public boolean isValidateSources() {
        sync();
        return bean.isValidateSources();
    }

    @Override
    public void setValidateSources(boolean validateSources) {
        sync();
        bean.setValidateSources(validateSources);
    }

    @Override
    public boolean sampleExist(String sampleName) throws BinBaseException {
        sync();
        return bean.sampleExist(sampleName);
    }

    @Override
    public byte[] downloadFile(String sampleName) throws BinBaseException, IOException {
        sync();
        return bean.downloadFile(sampleName);
    }

    @Override
    public String generateFileName(String dir, String sampleName) {
        sync();
        return bean.generateFileName(dir, sampleName);
    }

    @Override
    public String[] getDatabases() {
        sync();
        return bean.getDatabases();
    }

    @Override
    public void addDatabase(String databaseName) {
        sync();
        bean.addDatabase(databaseName);
    }

    @Override
    public void removeDatabase(String databaseName) {
        sync();
        bean.removeDatabase(databaseName);
    }

    @Override
    public void resetDatabases() {
        sync();
        bean.resetDatabases();
    }

    private ServiceJMXMBean bean;


    protected void sync() {
        MBeanServer server = MBeanServerFactory.findMBeanServer(null).get(0);

        try {
            bean = MBeanServerInvocationHandler
                    .newProxyInstance(server, new ObjectName(
                                    "binbase:service=Import"),
                            ServiceJMXMBean.class, false);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }
}
