package edu.ucdavis.genomics.metabolomics.sjp.parser.pegaus;

import edu.ucdavis.genomics.metabolomics.sjp.Parser;
import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;
import edu.ucdavis.genomics.metabolomics.sjp.parser.msp.SpectraParser;

/**
 * parses data like this
 * http://binbase.fiehnlab.ucdavis.edu:8080/jira/browse/BINBASE-276
 * 
 * @author wohlgemuth
 */
public class TableMassSpecParser extends Parser implements SpectraParser {

	boolean spectraFound = false;
	StringBuffer buffer;

	@Override
	protected void parseLine(String line) throws ParserException {


		//avoid lines with | or : or ; since other formats use this
		if (line.matches(".*[\\|:;].*") == false) {
			// check for NUMBER whitespace NUMBER
			if (line.matches("[0-9]*\\b.*[0-9]\\s*")) {
				if (spectraFound == false) {
					spectraFound = true;
					buffer = new StringBuffer();
				}

				// check that the second number is not zero
				if (line.matches("[0-9]*\\b*0") == false) {
					buffer.append(line.trim().replaceAll("\\s", ":"));
					buffer.append(" ");
				}
			}
			else {
				if (spectraFound == true) {
					this.getHandler().startElement(SpectraParser.SPECTRA, buffer.toString().trim());
					this.getHandler().endElement(SpectraParser.SPECTRA);
					spectraFound = false;
					buffer = new StringBuffer();
				}
			}
		}
	}

	@Override
	protected void finish() throws ParserException {
		if (spectraFound == true) {
			this.getHandler().startElement(SpectraParser.SPECTRA, buffer.toString().trim());
			this.getHandler().endElement(SpectraParser.SPECTRA);
		}
		super.finish();
	}

}
