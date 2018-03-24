package edu.ucdavis.genomics.metabolomics.binbase.bci.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import edu.ucdavis.genomics.metabolomics.binbase.bci.Configurator;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.io.dest.Destination;

/**
 * simple way to access a sop
 * 
 * @author wohlgemuth
 * 
 */
public class SopDestination implements Destination {

	private String name = null;

	public SopDestination(String name) {
		super();
		this.name = name;
	}

	/**
	 * standard configure
	 */
	public void configure(Map<?, ?> p) throws ConfigurationException {

	}

	/**
	 * write the result
	 */
	public OutputStream getOutputStream() throws IOException {
		if (this.name == null) {
			throw new IOException("please provide a name!");
		}
		return new MyOutputStream();
	}

	/**
	 * name of the result
	 */
	public void setIdentifier(Object o) throws ConfigurationException {
		this.name = o.toString();
	}

	private class MyOutputStream extends ByteArrayOutputStream {

		@Override
		public void close() throws IOException {
			super.close();
			try {
				Configurator.getExportService().uploadSopToDir(name, (String) Configurator.getExportService().getSopDirs().iterator().next(), this.toByteArray());
			} catch (Exception e) {
				throw new IOException(e.getMessage());
			}
		}

	}
}
