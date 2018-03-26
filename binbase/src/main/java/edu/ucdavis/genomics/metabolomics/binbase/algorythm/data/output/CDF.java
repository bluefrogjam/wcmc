package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.output;

import edu.ucdavis.genomics.metabolomics.util.io.Copy;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CDF implements Writer {

	Logger logger = Logger.getLogger(getClass());

	@Override
	public void write(OutputStream out, DataFile file) throws IOException {
		throw new IOException("not supported by writer!");

	}

	@Override
	public void write(OutputStream out, Source content) throws IOException {
		throw new IOException("not supported by writer!");

	}

	@Override
	public void write(OutputStream out, Object content) throws IOException {
		InputStream in = null;

		logger.info("content of type: " + content.getClass());
		if (content instanceof InputStream) {
			in = (InputStream) content;
			Copy.copy(in, out, false);
			in.close();
		} else if (content instanceof byte[]) {
			in = new ByteArrayInputStream((byte[]) content);
			Copy.copy(in, out, false);
			in.close();
		} else {
			throw new IOException("not supported by writer!");
		}
	}

	@Override
	public String toString() {
		return "cdf";
	}

	@Override
	public boolean isDatafileSupported() {
		return false;
	}

	@Override
	public boolean isSourceSupported() {
		return false;
	}

}
