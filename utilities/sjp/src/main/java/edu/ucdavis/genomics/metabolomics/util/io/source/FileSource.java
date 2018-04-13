/*
 * Created on Nov 8, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.io.source;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;

import java.io.*;
import java.util.Map;

/**
 * defines a file as source
 *
 * @author wohlgemuth
 * @version Nov 8, 2005
 */
public class FileSource implements Source {

    /**
     * internal file represantation
     */
    private File file;

    public FileSource(File file) throws ConfigurationException {
        this.setIdentifier(file);
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " - " + this.file.getAbsolutePath();
    }

    /**
     * @throws IOException
     * @author wohlgemuth
     * @version Nov 8, 2005
     * @see edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.source.Source#getStream()
     */
    public InputStream getStream() throws IOException {
        BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));
        return stream;
    }

    /**
     * @author wohlgemuth
     * @version Nov 8, 2005
     * @see edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.source.Source#getSourceName()
     */
    public String getSourceName() {
        return file.getName();
    }

    /**
     * @author wohlgemuth
     * @version Nov 8, 2005
     * @see edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.source.Source#configure(java.util.Properties)
     */
    public void configure(Map<?, ?> p) throws ConfigurationException {

    }

    public void setIdentifier(Object o) throws ConfigurationException {
        if (o == null) {
            throw new ConfigurationException("o must point to a valid file");
        }

        if (o instanceof String) {
            file = new File((String) o);
        } else if (!(o instanceof File)) {
            throw new ConfigurationException("o is not of type java.io.File");
        } else {
            file = (File) o;
        }

        if (!file.exists()) {
            throw new ConfigurationException("o does not exist, " + file);
        }

        if (!file.isFile()) {
            throw new ConfigurationException("o is not a file, " + file);
        }

        if (file.isDirectory()) {
            throw new ConfigurationException("o is a directory, must be a file! " + file);
        }
    }

    /**
     * @author wohlgemuth
     * @version Nov 17, 2005
     * @see edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.source.Source#exist()
     */
    public boolean exist() {
        return file.exists();
    }

    public long getVersion() {
        return file.length();
    }

    public File getFile() {
        return file;
    }

}
