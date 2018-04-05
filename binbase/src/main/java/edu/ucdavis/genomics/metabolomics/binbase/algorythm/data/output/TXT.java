/*
 * Created on Feb 8, 2007
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.output;

import edu.ucdavis.genomics.metabolomics.util.io.dest.Destination;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * writes result to txt
 * 
 * @author wohlgemuth
 * @version Feb 8, 2007
 * 
 */
public class TXT implements Writer {

	public void write(Destination destination, DataFile file) {
	}

	public boolean isDatafileSupported() {
		return true;
	}

	public boolean isSourceSupported() {
		return true;
	}

	public void write(OutputStream out, DataFile file) throws IOException {
		write(out, file.toInputStream());
	}

	private void write(OutputStream out, InputStream in) throws IOException {
		InputStream stream2 = in;
		byte[] buf = new byte[1024];
		int len;

		int size = 0;
		while ((len = stream2.read(buf)) > 0) {
			out.write(buf, 0, len);
			size = size + len;
		}

		stream2.close();
		out.flush();
	}

	public void write(OutputStream out, Source content) throws IOException {
		if (content.exist()) {
			write(out, content.getStream());
		}
	}

	@Override
	public String toString() {
		return "txt";
	}

	@Override
	public void write(OutputStream out, Object content) throws IOException {
		java.io.Writer writer = new OutputStreamWriter(out);
		writer.write(content.toString());
		writer.flush();

	}

}
