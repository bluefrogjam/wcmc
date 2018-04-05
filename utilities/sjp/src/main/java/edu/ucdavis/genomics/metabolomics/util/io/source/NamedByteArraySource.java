package edu.ucdavis.genomics.metabolomics.util.io.source;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 4/15/15
 * Time: 3:47 PM
 */
public class NamedByteArraySource extends ByteArraySource {
    public NamedByteArraySource(byte[] bytes) {
        super(bytes);
    }

    String identifier = "none set";

    @Override
    public void setIdentifier(Object o) throws ConfigurationException {
        this.identifier = o.toString();
    }

    @Override
    public String getSourceName() {
        return this.identifier;
    }
}
