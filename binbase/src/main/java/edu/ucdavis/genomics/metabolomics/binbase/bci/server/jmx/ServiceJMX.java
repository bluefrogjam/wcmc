/*
 * Created on Apr 21, 2006
 */
package edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.zip.GZIPInputStream;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import edu.ucdavis.genomics.metabolomics.binbase.cluster.util.JBosssPropertyHolder;
import org.slf4j.Logger;

import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.io.Copy;

public class ServiceJMX implements ServiceJMXMBean {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private Collection dirs = new Vector();

    private Vector databases = new Vector();

    private boolean validateSources = true;

    public ServiceJMX() {
        super();
    }

    @Override
    public Collection getImportDirectories() throws BinBaseException {
        return dirs;
    }

    @Override
    public void addDirectory(String string) {
        if (!string.endsWith(File.separator)) {
            string = string + File.separator;
        }

        if (!dirs.contains(string)) {
            File dir = new File(string);
            if (!dir.exists()) {
                logger.info("creating directory for import: " + string);
                dir.mkdirs();
            }

            dirs.add(string);
            this.store();
        }
    }

    @Override
    public void removeDirectory(String string) {
        dirs.remove(string);
        this.store();
    }

    @Override
    public void clearDirectorys() {
        dirs.clear();
        this.store();
    }

    public ObjectName preRegister(MBeanServer server, ObjectName name)
            throws Exception {
        return null;
    }

    public void postRegister(Boolean registrationDone) {
        try {
            File file = JBosssPropertyHolder.getPropertyFile(getClass());
            if (file.exists()) {


                ObjectInputStream in = new ObjectInputStream(new FileInputStream(
                        file));

                Map map = (Map) in.readObject();

                if (map.get("dirs") != null) {
                    this.dirs = (Collection) map.get("dirs");
                }
                if (map.get("validateSources") != null) {
                    this.validateSources = (Boolean) map.get("validateSources");
                }
                if (map.get("databases") != null) {
                    this.databases = (Vector) map.get("databases");
                }

            } else {
                this.addDatabase("binbase");
                this.addDirectory("/opt/jboss/.binbase/txt");
                this.setValidateSources(true);
            }
        } catch (Exception e) {
            logger.error("postRegister(Boolean)", e); //$NON-NLS-1$

        }
    }

    @Override
    public void uploadImportFile(String fileName, byte[] content)
            throws FileNotFoundException, IOException, BinBaseException {
        if (this.getImportDirectories().isEmpty()) {
            logger.info("creating directory for txt...");
            addDirectory(new File(JBosssPropertyHolder.getConfigLocation(), "import").getAbsolutePath());
        }
        this.uploadFileToDir(fileName, (String) getImportDirectories()
                .iterator().next(), content);
    }

    @Override
    public void uploadConfigFile(String fileName, byte[] content)
            throws FileNotFoundException, IOException, BinBaseException {

        File file = new File(JBosssPropertyHolder.getConfigLocation(), "config");
        if (!file.exists()) {
            file.mkdirs();
        }
        Copy.copy(new ByteArrayInputStream(content), new FileOutputStream(new File(file, fileName)));
    }

    @Override
    public Collection listConfigFiles() {

        File file = new File(JBosssPropertyHolder.getConfigLocation(), "config");
        if (!file.exists()) {
            file.mkdirs();
        }

        File[] files = file.listFiles();
        Collection result = new Vector();
        for (int i = 0; i < files.length; i++) {
            result.add(files[i].getName());
        }

        return result;
    }

    @Override
    public byte[] getConfigFile(String fileName) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

		logger.info("loading config file");
		File file = new File(new File(JBosssPropertyHolder.getConfigLocation(),"config"), fileName);
		logger.info("looking for file: " + file.getAbsolutePath());
		if (!file.exists()) {
			logger.info("we did not find you file: " + fileName );
			throw new IOException("source not found, name was: " + fileName);
		}

        FileInputStream in = new FileInputStream(file);
        Copy.copy(in, out);

		byte[] result = out.toByteArray();

		logger.info("finished");
		return result;
	}

    @Override
    public void uploadFileToDir(String fileName, String directory,
                                byte[] content) throws FileNotFoundException, IOException {
        logger.info("uploading file: " + fileName);
        if (fileName.toLowerCase().endsWith(".txt.gz")) {
        } else if (!fileName.toLowerCase().endsWith(".txt")) {
            fileName = fileName + ".txt";
        }
        Copy.copy(new ByteArrayInputStream(content), new FileOutputStream(
                directory + "/" + fileName));
    }

    private void store() {
        try {
            File file = JBosssPropertyHolder.getPropertyFile(getClass());

            ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(file));
            HashMap map = new HashMap();

            map.put("dirs", dirs);
            map.put("validateSources", validateSources);
            map.put("databases", this.databases);

            out.writeObject(map);
            out.flush();
            out.close();

        } catch (Exception e) {
            logger.debug(e.getMessage(), e); //$NON-NLS-1$
        }
    }

    public void preDeregister() throws Exception {
        store();
    }

    public void postDeregister() {
    }


    @Override
    public boolean isValidateSources() {
        return validateSources;
    }

    @Override
    public void setValidateSources(boolean validateSources) {
        this.validateSources = validateSources;
        this.store();
    }

    @Override
    public boolean sampleExist(String sampleName) throws BinBaseException {

        logger.info(this.getImportDirectories().size() + " directories defined for sample locations");
        for (Object o : this.getImportDirectories()) {
            String dir = o.toString();

            File file = new File(generateFileName(dir, sampleName));

            logger.info("checking for file:" + file.getAbsolutePath());
            if (file.exists()) {
                return true;
            }

            file = new File(generateFileName(dir, sampleName) + ".gz");
            logger.info("checking for compressed file:" + file.getAbsolutePath());
            if (file.exists()) {
                return true;
            }

        }
        logger.info("file was not found: " + sampleName);
        return false;
    }

    @Override
    public byte[] downloadFile(String sampleName) throws BinBaseException,
            IOException {

        for (Object o : this.getImportDirectories()) {
            String dir = o.toString();

            File file = new File(generateFileName(dir, sampleName));

            if (file.exists()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                FileInputStream in = new FileInputStream(file);
                Copy.copy(in, out);

                byte[] result = out.toByteArray();
                return result;
            }

            file = new File(generateFileName(dir, sampleName) + ".gz");
            if (file.exists()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                FileInputStream in = new FileInputStream(file);
                Copy.copy(new GZIPInputStream(in), out);

                byte[] result = out.toByteArray();
                return result;
            }
        }
        return null;
    }

    @Override
    public String generateFileName(String dir, String sampleName) {
        if (!dir.endsWith("/")) {
            dir = dir + "/";
        }

        if (sampleName.contains(".txt")) {
            return dir + sampleName.replace(':', '_');
        } else {

            return dir + sampleName.replace(':', '_') + ".txt";
        }
    }

    @Override
    public String[] getDatabases() {
        String[] result = new String[this.databases.size()];

        for (int i = 0; i < result.length; i++) {
            result[i] = (String) this.databases.get(i);
        }

        return result;
    }

    @Override
    public void addDatabase(String databaseName) {

        if (!this.databases.contains(databaseName)) {
            this.databases.add(databaseName);
        }
        this.store();
    }

    @Override
    public void removeDatabase(String databaseName) {
        this.databases.remove(databaseName);
        this.store();
    }

    @Override
    public void resetDatabases() {
        this.databases.clear();
        this.store();
    }
}
