package edu.ucdavis.genomics.metabolomics.sjp.parser.nist;

import edu.ucdavis.genomics.metabolomics.sjp.Parser;
import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;
import edu.ucdavis.genomics.metabolomics.sjp.parser.msp.SpectraParser;

/**
 * parses the nist ms data
 * 
 * @author nase
 * 
 */
public class NistMassSpecParser extends Parser implements SpectraParser {

	public static final String OTHER_DBS = "Other DBs";

	public static final String SYNONYMS = "Synonyms";

	public static final String NAME = "Name";

	@Override
	protected void finish() throws ParserException {
		if (buffer.length() > 0) {
			if (spectra == true) {
				spectra = false;
				parseMassSpec(buffer.toString());
				buffer = new StringBuffer();
			}
		}
		super.finish();
	}

	/**
	 * fields delimitter
	 */
	private static String FIELD_DELIMITER = ":";

	boolean multiStarted = false;

	boolean single = false;

	boolean spectra = false;

	String key = null;

	StringBuffer buffer = new StringBuffer();

	@Override
	protected void parseLine(String line) throws ParserException {
		if(line == null || line.length() == 0){
			return;
		}
		if (multiStarted) {
			buffer.append(" ");
			buffer.append(line.trim());
			if (single) {
				getHandler().startElement(key, buffer.toString());

				multiStarted = false;
				buffer = new StringBuffer();
				single = false;
			}
			if (multiStarted == false) {
				getHandler().endElement(key);
			}
			return;
		} else if (line.indexOf(FIELD_DELIMITER) > -1) {
			String[] content = line.split(FIELD_DELIMITER);
			if (content.length == 1 | content.length == 2) {
				if (content[0].equals(NAME) || content[0].equals(SYNONYMS)) {
					multiStarted = true;
					single = true;

					if (buffer.length() > 0) {
						if (spectra == true) {
							spectra = false;
							parseMassSpec(buffer.toString());
							buffer = new StringBuffer();
						}
					}
					if (content.length == 2) {
						buffer.append(content[1].trim());
					}
					key = content[0];
				} else if (content[0].equals(OTHER_DBS)) {
					multiStarted = false;
					single = true;
					spectra = false;

					key = content[0];

					if (content.length == 2) {
						getHandler().startElement(key, buffer.toString());
						getHandler().endElement(key);
					}
				} else if (content[0].indexOf("Values and Intensities") > -1) {
					spectra = true;
					multiStarted = false;
					single = false;
				} 
			}
		} else if (spectra) {
			buffer.append(line.trim() + " ");
		}else if(spectra == false && line.indexOf('|') > -1){
			spectra = true;
			multiStarted = false;
			single = false;			
			buffer.append(line.trim() + " ");
			
		}
	}

	/**
	 * converts the spectra to a leco spectra
	 * @param string
	 * @throws ParserException 
	 */
	private void parseMassSpec(String string) throws ParserException {
		String[] spectra = spectra = string.trim().split("\\|");			

		StringBuffer buffer = new StringBuffer();
		
		for(int i = 0; i < spectra.length; i++){
			String s = spectra[i];
			s = s.trim();
			String[] pair = s.split(" ");
			buffer.append(pair[0] + ":" + pair[1]);

			if(i < spectra.length){
				buffer.append(" ");
			}
		}
		
		getHandler().startElement(SpectraParser.SPECTRA, buffer.toString());
		getHandler().endElement(SpectraParser.SPECTRA);
	}

}
