package edu.ucdavis.genomics.metabolomics.util.io.source;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.io.Copy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * provides an easy access to a ressource
 *
 * @author wohlgemuth
 */
public class ResourceSource implements Source {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public ResourceSource(String name) {
        this.name = name;
    }

    private String name;

    public void configure(Map<?, ?> p) throws ConfigurationException {
    }

    public boolean exist() {
        try {
            logger.info("checking for: " + getClass().getResource(name).getFile());
            getClass().getResource(name).openConnection();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
        return true;
    }

    public String getSourceName() {
        return name.substring(name.lastIndexOf("/") + 1);
    }

    public String getSourceAbsoluteName() {
        return name;
    }

    public InputStream getStream() throws IOException {
        assert (exist());
        return getClass().getResourceAsStream(name);
    }

    public byte[] getBytes() throws IOException {
        assert (exist());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = getStream();
        Copy.copy(in, out);

        return out.toByteArray();
    }

    public long getVersion() {
        try {
            return getStream().hashCode();
        } catch (IOException e) {
            return name.hashCode();
        }
    }

    public void setIdentifier(Object o) throws ConfigurationException {
        this.name = o.toString();
    }

}
