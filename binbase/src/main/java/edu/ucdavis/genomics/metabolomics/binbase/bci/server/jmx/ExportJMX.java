package edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx;

import edu.ucdavis.genomics.metabolomics.binbase.bci.server.exception.FileNotFoundException;
import edu.ucdavis.genomics.metabolomics.binbase.cluster.util.JBosssPropertyHolder;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.io.Copy;
import org.slf4j.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class ExportJMX implements ExportJMXMBean {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private boolean enableCache = false;

    /**
     * place where we can find netcdf's
     */
    private Collection<String> netCDFDirs = new HashSet<>();

    /**
     * place where we can find sops
     */
    private Collection<String> sopDirs = new HashSet<>();

    /**
     * place where we can store our results
     */
    private String resultDirectory = "result";

    private String defaultSop = "";

    @Override
    public boolean isEnableCache() {
        return enableCache;
    }

    @Override
    public void setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
        this.store();
    }

    private void store() {
        try {
            File file = JBosssPropertyHolder.getPropertyFile(getClass());

            ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(file));

            HashMap map = new HashMap();

            map.put("enableCache", this.isEnableCache());
            map.put("netCDFDirs", this.getNetCDFDirectories());
            map.put("sopDirs", this.getSopDirs());
            map.put("resultDirectory", this.getResultDirectory());
            map.put("defaultSop", this.getDefaultSop());

            out.writeObject(map);
            out.flush();
            out.close();

        } catch (Exception e) {
            logger.error(e.getMessage(), e); //$NON-NLS-1$
        }
    }

    @Override
    public void addNetCDFDirectory(String string) {
        if (!string.endsWith(File.separator)) {
            string = string + File.separator;
        }

        if (!netCDFDirs.contains(string)) {

            File file = new File(string);
            file.mkdirs();
            netCDFDirs.add(string);
            this.store();

        } else {
            logger.info("directory already exist: " + string);
        }
    }

    @Override
    public void removeNetCDFDirectory(String string) {
        netCDFDirs.remove(string);
        this.store();
    }

    @Override
    public void clearNetCDFDirectorys() {
        netCDFDirs.clear();
        this.store();
    }

    @Override
    public Collection getNetCDFDirectories() throws BinBaseException {
        return netCDFDirs;
    }

    public void postDeregister() {
    }

    public void postRegister(Boolean arg0) {
        try {
            File file = JBosssPropertyHolder.getPropertyFile(getClass());
            if (file.exists()) {

                ObjectInputStream in = new ObjectInputStream(new FileInputStream(
                        file));

                Map map = (Map) in.readObject();

                if (map.containsKey("enableCache")) {
                    this.enableCache = (Boolean) map.get("enableCache");
                }
                if (map.containsKey("netCDFDirs")) {
                    this.netCDFDirs = (Vector) map.get("netCDFDirs");
                }
                if (map.containsKey("sopDirs")) {
                    this.sopDirs = (Vector) map.get("sopDirs");
                }
                if (map.containsKey("resultDirectory")) {
                    this.resultDirectory = (String) map.get("resultDirectory");
                }
                if (map.containsKey("defaultSop")) {
                    this.defaultSop = (String) map.get("defaultSop");
                }

            } else {
                this.setDefaultSop("");
                this.addNetCDFDirectory("/opt/jboss/.binbase/netcdf");
                this.addSopDir("/opt/jboss/.binbase/sop");
                this.setResultDirectory("/opt/jboss/.binbase/result");
                this.setEnableCache(false);
            }
        } catch (Exception e) {
            logger.debug("postRegister(Boolean)", e); //$NON-NLS-1$

        }
    }

    public void preDeregister() throws Exception {
    }

    public ObjectName preRegister(MBeanServer arg0, ObjectName arg1)
            throws Exception {
        return null;
    }

    @Override
    public Collection getSopDirs() {
        return sopDirs;
    }

    @Override
    public void addSopDir(String dir) throws FileNotFoundException {
        dir = dir.trim();

        this.createDir(dir);
        if (!this.sopDirs.contains(dir)) {
            this.sopDirs.add(dir);
        }
        this.store();
    }

    @Override
    public void clearSopDirs() {
        this.sopDirs.clear();
        store();
    }

    @Override
    public void removeSopDirectory(String dir) {
        this.sopDirs.remove(dir);
        store();
    }

    @Override
    public String getResultDirectory() {
        return resultDirectory;
    }

    @Override
    public void setResultDirectory(String resultDirectory)
            throws FileNotFoundException {
        this.resultDirectory = resultDirectory;
        createDir(resultDirectory);
        this.store();
    }

    private void createDir(String resultDirectory) throws FileNotFoundException {
        File file = new File(resultDirectory);
        if (!file.exists()) {
            logger.info("creating new directory");
            file.mkdirs();
            logger.info("created at: " + file.getAbsolutePath());
        }
    }

    @Override
    public void uploadResult(String sopName, byte[] content)
            throws IOException {

        File file = new File(this.getResultDirectory());

        if (!file.exists()) {
            file.mkdirs();
        }

        Copy.copy(new ByteArrayInputStream(content),
                new FileOutputStream(this.getResultDirectory() + "/" + sopName));
    }

    @Override
    public void uploadSopToDir(String sopName, String dir, byte[] content)
            throws IOException {
        Copy.copy(new ByteArrayInputStream(content), new FileOutputStream(dir
                + "/" + sopName));

    }

    @Override
    public void uploadNetCdfToDir(String fileName, String dir, byte[] content)
            throws IOException {
        if (fileName.toLowerCase().endsWith(".cdf.gz")) {

        } else if (!fileName.toLowerCase().endsWith(".cdf")) {
            fileName = fileName + ".cdf";
        }
        Copy.copy(new ByteArrayInputStream(content), new FileOutputStream(dir
                + "/" + fileName));
    }

    @Override
    public byte[] getSop(String sopName) throws
            IOException, FileNotFoundException {
        for (Object sopDir : this.sopDirs) {
            String dir = (String) sopDir;
            File file = new File(new File(dir), sopName);
            if (file.exists()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Copy.copy(new FileInputStream(file), out);
                return out.toByteArray();
            }
        }
        throw new FileNotFoundException(sopName
                + " was not found in any specified directory!");
    }

    @Override
    public String printSoap(String sopName)
            throws FileNotFoundException,
            IOException {
        return new String(getSop(sopName));
    }

    @Override
    public List listSops() {
        List result = new Vector();

        for (Object sopDir : this.sopDirs) {
            String dir = sopDir.toString();
            File file = new File(dir);
            File files[] = file.listFiles();

            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    result.add(files[i].getName());
                }
            }
        }

        return result;
    }

    @Override
    public List listResults() {
        List result = new Vector();
        File file = new File(this.getResultDirectory());
        File files[] = file.listFiles();

        for (File file1 : files) {
            if (file1.isFile()) {
                result.add(file1.getName());
            }
        }

        return result;
    }

    @Override
    public byte[] getResult(String resultName)
            throws IOException,
            FileNotFoundException {
        File file = new File(this.getResultDirectory() + "/" + resultName);
        if (file.exists()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Copy.copy(new FileInputStream(file), out);
            return out.toByteArray();
        } else if (!resultName.endsWith(".zip")) {
            return getResult(resultName + ".zip");
        }

        throw new FileNotFoundException("sorry result file was not found! "
                + resultName);
    }

    @Override
    public boolean hasResult(String resultName) {
        File file = new File(this.getResultDirectory() + "/" + resultName);
        return file.exists();
    }

    @Override
    public boolean hasCDF(String sampleName) throws BinBaseException {

        Iterator it = this.getNetCDFDirectories().iterator();
        while (it.hasNext()) {
            String dir = it.next().toString();

            File file = new File(generateFileName(dir, sampleName));

            if (file.exists()) {
                return true;
            }

            file = new File(generateFileName(dir, sampleName) + ".gz");
            if (file.exists()) {
                return true;
            }

        }
        return false;
    }


    @Override
    public byte[] downloadFile(String sampleName) throws BinBaseException,
            IOException {
        Iterator it = this.getNetCDFDirectories().iterator();

        while (it.hasNext()) {
            String dir = it.next().toString();

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
    public void uploadSop(String sopName, byte[] content)
            throws FileNotFoundException,
            IOException {

        this.uploadSopToDir(sopName, sopDirs.iterator().next(), content);
    }

    @Override
    public void uploadNetCdf(String fileName, byte[] content)
            throws IOException, BinBaseException {

        this.uploadNetCdfToDir(fileName, (String) getNetCDFDirectories()
                .iterator().next(), content);
    }

    @Override
    public String getDefaultSop() {
        return defaultSop;
    }

    @Override
    public void setDefaultSop(String defaultSop) {
        this.defaultSop = defaultSop;
        this.store();
    }


    @Override
    public String generateFileName(String dir, String sampleName) {
        if (!dir.endsWith("/")) {
            dir = dir + "/";
        }

        return dir + sampleName.replace(':', '_') + ".cdf";
    }
}
