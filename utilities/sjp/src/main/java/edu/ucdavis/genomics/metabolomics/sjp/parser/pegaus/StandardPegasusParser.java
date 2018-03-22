package edu.ucdavis.genomics.metabolomics.sjp.parser.pegaus;

import edu.ucdavis.genomics.metabolomics.sjp.Parser;
import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;
import edu.ucdavis.genomics.metabolomics.sjp.parser.msp.SpectraParser;

/**
 * finds the standard pegasus export string in a set of data
 * @author wohlgemuth
 *
 */
public class StandardPegasusParser extends Parser implements SpectraParser{

	@Override
	protected void parseLine(String line) throws ParserException {
		
		if(line.matches("(([0-9]*:[0-9]*).)+?")){
			this.getHandler().startElement(SPECTRA, line.trim());
			this.getHandler().endElement(SPECTRA);
		}
	}

}
