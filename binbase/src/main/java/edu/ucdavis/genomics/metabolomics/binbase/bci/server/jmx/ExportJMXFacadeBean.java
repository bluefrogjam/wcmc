package edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx;

import edu.ucdavis.genomics.metabolomics.binbase.bci.server.exception.FileNotFoundException;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.management.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Created by wohlgemuth on 10/18/16.
 */
@Remote(ExportJMXFacade.class)
@Stateless
public class ExportJMXFacadeBean implements ExportJMXFacade,Serializable{
    @Override
    public boolean isEnableCache() {
        sync();
        return bean.isEnableCache();
    }

    @Override
    public void setEnableCache(boolean enableCache) {
        sync();

        bean.setEnableCache(enableCache);
    }

    @Override
    public void addNetCDFDirectory(String string) {
        sync();

        bean.addNetCDFDirectory(string);
    }

    @Override
    public void removeNetCDFDirectory(String string) {
        sync();

        bean.removeNetCDFDirectory(string);
    }

    @Override
    public void clearNetCDFDirectorys() {
        sync();

        bean.clearNetCDFDirectorys();
    }

    @Override
    public Collection getNetCDFDirectories() throws BinBaseException {
        sync();

        return bean.getNetCDFDirectories();
    }

    @Override
    public Collection getSopDirs() {
        sync();

        return bean.getSopDirs();
    }

    @Override
    public void addSopDir(String dir) throws FileNotFoundException {
        sync();

        bean.addSopDir(dir);
    }

    @Override
    public void clearSopDirs() {
        sync();

        bean.clearSopDirs();
    }

    @Override
    public void removeSopDirectory(String dir) {
        sync();

        bean.removeSopDirectory(dir);
    }

    @Override
    public String getResultDirectory() {
        sync();

        return bean.getResultDirectory();
    }

    @Override
    public void setResultDirectory(String resultDirectory) throws FileNotFoundException {
        sync();

        bean.setResultDirectory(resultDirectory);
    }

    @Override
    public void uploadResult(String sopName, byte[] content) throws IOException {
        sync();

        bean.uploadResult(sopName, content);
    }

    @Override
    public void uploadSopToDir(String sopName, String dir, byte[] content) throws IOException {
        sync();

        bean.uploadSopToDir(sopName, dir, content);
    }

    @Override
    public void uploadNetCdfToDir(String fileName, String dir, byte[] content) throws IOException {
        sync();

        bean.uploadNetCdfToDir(fileName, dir, content);
    }

    @Override
    public byte[] getSop(String sopName) throws IOException, FileNotFoundException {
        sync();

        return bean.getSop(sopName);
    }

    @Override
    public String printSoap(String sopName) throws FileNotFoundException, IOException {
        sync();

        return bean.printSoap(sopName);
    }

    @Override
    public List listSops() {
        sync();

        return bean.listSops();
    }

    @Override
    public List listResults() {
        sync();

        return bean.listResults();
    }

    @Override
    public byte[] getResult(String resultName) throws IOException, FileNotFoundException {
        sync();

        return bean.getResult(resultName);
    }

    @Override
    public boolean hasResult(String resultName) {
        sync();

        return bean.hasResult(resultName);
    }

    @Override
    public boolean hasCDF(String sampleName) throws BinBaseException {
        sync();

        return bean.hasCDF(sampleName);
    }

    @Override
    public byte[] downloadFile(String sampleName) throws BinBaseException, IOException {
        sync();

        return bean.downloadFile(sampleName);
    }

    @Override
    public void uploadSop(String sopName, byte[] content) throws FileNotFoundException, IOException {
        sync();

        bean.uploadSop(sopName, content);
    }

    @Override
    public void uploadNetCdf(String fileName, byte[] content) throws IOException, BinBaseException {
        sync();

        bean.uploadNetCdf(fileName, content);
    }

    @Override
    public String getDefaultSop() {
        sync();

        return bean.getDefaultSop();
    }

    @Override
    public void setDefaultSop(String defaultSop) {
        sync();

        bean.setDefaultSop(defaultSop);
    }

    @Override
    public String generateFileName(String dir, String sampleName) {
        sync();

        return bean.generateFileName(dir, sampleName);
    }

    private ExportJMXMBean bean;


    protected void sync() {
        MBeanServer server = MBeanServerFactory.findMBeanServer(null).get(0);

        try {
            bean =  MBeanServerInvocationHandler
                    .newProxyInstance(server, new ObjectName(
                                    "binbase:service=Export"),
                            ExportJMXMBean.class, false);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }
}
