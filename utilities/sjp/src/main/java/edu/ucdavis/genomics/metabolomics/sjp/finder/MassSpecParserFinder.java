package edu.ucdavis.genomics.metabolomics.sjp.finder;

import edu.ucdavis.genomics.metabolomics.sjp.Finder;
import edu.ucdavis.genomics.metabolomics.sjp.Parser;
import edu.ucdavis.genomics.metabolomics.sjp.ParserHandler;
import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;
import edu.ucdavis.genomics.metabolomics.sjp.parser.msp.MSPParser;
import edu.ucdavis.genomics.metabolomics.sjp.parser.msp.SpectraParser;
import edu.ucdavis.genomics.metabolomics.sjp.parser.nist.NistMassSpecParser;
import edu.ucdavis.genomics.metabolomics.sjp.parser.pegaus.StandardPegasusParser;
import edu.ucdavis.genomics.metabolomics.sjp.parser.pegaus.TableMassSpecParser;

import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

/**
 * @author wohlgemuth returns the needed parser to read msp files for the given
 *         spectra
 */
public class MassSpecParserFinder extends Finder {

	private List<Parser> registeredParsers = new Vector<Parser>();

	/**
	 * just to check if a spectra was found
	 * 
	 * @author wohlgemuth
	 */
	class Handler implements ParserHandler {

		String spectra;

		public void endAttribute(String element, String name) throws ParserException {
		}

		public void endDataSet() throws ParserException {
		}

		public void endDocument() throws ParserException {
		}

		public void endElement(String name) throws ParserException {
		}

		public void setProperties(Properties p) throws ParserException {
		}

		public void startAttribute(String element, String name, String value) throws ParserException {
		}

		public void startDataSet() throws ParserException {
		}

		public void startDocument() throws ParserException {
		}

		public void startElement(String name, String value) throws ParserException {
			if (name.equals(MSPParser.SPECTRA)) {
				spectra = value;
			}

		}

	}

	public void addParser(Parser parser) throws ParserException {
		if (parser instanceof SpectraParser) {
			this.registeredParsers.add(parser);
		}
		else {
			throw new ParserException("sorry class needs to be of type: " + SpectraParser.class.getName());
		}
	}

	@Override
	protected Parser find(BufferedReader in) throws ParserException, IOException {
		// copy the stream to an array
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		copy(in, out);
		byte[] content = out.toByteArray();

		for (Parser parser : registeredParsers) {
			try {
				Handler h = new Handler();
				parser.parse(new ByteArrayInputStream(content), h);

				if (h.spectra != null) {
					System.out.println(parser.getClass().getName() + " - " + h.spectra);
					return parser;
				}
			}
			catch (Exception e) {
				// ok wrong parser we can ignore this
			}
		}
		throw new ParserNotFoundException("sorry no implemnetation was found for this format");
	}

	/**
	 * copy the data
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	private void copy(BufferedReader in, ByteArrayOutputStream out) throws IOException {
		BufferedOutputStream o = new BufferedOutputStream(out);

		int value = 0;
		while ((value = in.read()) != -1) {
			o.write(value);
		}

		o.flush();
		o.close();
		out.flush();
	}


	/**
	 * initialized with already registered handlers
	 * @throws ParserException
	 */
	public MassSpecParserFinder() throws ParserException {
		super();
		this.addParser(new MSPParser());
		this.addParser(new TableMassSpecParser());
		this.addParser(new NistMassSpecParser());
		this.addParser(new StandardPegasusParser());
		
	}

	public MassSpecParserFinder(List<Parser> registeredParsers) {
		super();
		this.registeredParsers = registeredParsers;
	}

}
