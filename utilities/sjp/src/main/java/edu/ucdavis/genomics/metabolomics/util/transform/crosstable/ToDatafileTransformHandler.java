/*
 * Created on Nov 19, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.transform.crosstable;

import java.util.ArrayList;
import java.util.List;

import edu.ucdavis.genomics.metabolomics.util.statistics.data.SimpleDatafile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.FormatObject;

/**
 * creates datafile from the given stream
 * 
 * @author wohlgemuth
 * @version Nov 19, 2005
 * 
 */
@Component
public class ToDatafileTransformHandler extends AbstractXMLTransformHandler {
	/**
	 * our datafile
	 */
	@Autowired
	private DataFile dataFile = null;

	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * writes a line to the datafile
	 * 
	 * @author wohlgemuth
	 * @version Nov 19, 2005
	 * @see edu.ucdavis.genomics.metabolomics.util.transform.crosstable.AbstractXMLTransformHandler#writeLine(List)
	 */
	public void writeLine(List<FormatObject<?>> line) {
		// convert line to numbers
		List<FormatObject<?>> result = new ArrayList<FormatObject<?>>();

		for (int i = 0; i < line.size(); i++) {
			result.add(line.get(i));
		}
		dataFile.addRow(line);
	}

	/**
	 * returns the generated datafile
	 * 
	 * @author wohlgemuth
	 * @version Nov 19, 2005
	 * @return
	 */
	public DataFile getFile() {
		return dataFile;
	}

	/**
	 * generates a datafile from this
	 * 
	 * @author wohlgemuth
	 * @version Nov 19, 2005
	 * @see edu.ucdavis.genomics.metabolomics.util.Describealbe#getDescription()
	 */
	public String getDescription() {
		return "create a datafile from the given xml stream";
	}

	public void startDocument() throws SAXException {
		super.startDocument();
		dataFile = new SimpleDatafile();
	}
}
