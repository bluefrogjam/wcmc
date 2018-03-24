/*
 * Created on Feb 12, 2007
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.output;

import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;
import edu.ucdavis.genomics.metabolomics.util.xls.DataFileToXLSConverter;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.io.OutputStream;

public class XLS implements Writer {

	public boolean isDatafileSupported() {
		return true;
	}

	public boolean isSourceSupported() {
		return false;
	}

	public void write(OutputStream out, DataFile file) throws IOException {

		DataFileToXLSConverter c = new DataFileToXLSConverter();
		Workbook book = c.convert(file,16000);
		book.write(out);

		c = null;
		book = null;
		
		System.gc();
	}

	public void write(OutputStream out, Source content) throws IOException {
		throw new IOException("not supported method!");
	}

	@Override
	public String toString() {
		return "xlsx";
	}

	@Override
	public void write(OutputStream out, Object content) throws IOException {
		throw new IOException("not supported method!");

	}

}
