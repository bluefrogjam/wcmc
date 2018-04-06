package edu.ucdavis.genomics.metabolomics.binbase.bci.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;

import edu.ucdavis.genomics.metabolomics.binbase.bci.Configurator;
import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;

/**
 * easy access to a result
 * 
 * @author wohlgemuth
 * 
 */
public class ResultSource implements Source {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private String name;

	public ResultSource(String name) {
		super();
		this.name = name;
	}

	public void configure(Map<?, ?> p) throws ConfigurationException {
	}

	public boolean exist() {
		try {
			Configurator.getExportService().getResult(name);
			return true;

		} catch (Exception e) {
			logger.warn(e.getMessage());
			return false;
		}
	}

	public String getSourceName() {
		return name;
	}

	public InputStream getStream() throws IOException {
		try {
			return new ByteArrayInputStream(Configurator.getExportService().getResult(name));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new IOException(e.getMessage());
		}
	}

	public long getVersion() {
		try {
			return Configurator.getExportService().getResult(name).hashCode();
		} catch (Exception e) {
			logger.warn(e.getMessage());
			return -1;
		}
	}

	public void setIdentifier(Object o) throws ConfigurationException {
		this.name = o.toString();
	}

}
