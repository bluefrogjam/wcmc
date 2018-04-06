package edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.provider;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.version.Header;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.version.HeaderFactory;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.exception.HeaderProblemException;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateApexMasses;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateSpectra;
import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.SQLObject;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;

/**
 * import standard pegasus txt files into the database
 * 
 * @author wohlgemuth
 * @version Nov 8, 2005
 */
public class PegasusASCIIIProvider extends SQLObject implements
		SampleDataProvider {

	/**
	 * configuration element
	 */
	protected Element config;


	boolean containsErrors = false;
	/**
	 * our datasource
	 */
	private Source source;

	/**
	 * DOCUMENT ME!
	 * 
	 * @uml.property name="uniqueToIgnore"
	 * @uml.associationEnd elementType="java.lang.Integer" multiplicity="(0 -1)"
	 */

	/**
	 * DOCUMENT ME!
	 */
	private Header pegasusHeader;

	/**
	 * seperator to split datas
	 */
	private String seperator = "\t";

	/**
	 * contains all spectra
	 */
	private Collection<Map<String, String>> spectra = new Vector<Map<String, String>>();

	/**
	 * default konstruktor
	 * 
	 * @throws ConfigurationException
	 */
	public PegasusASCIIIProvider() throws ConfigurationException {
	}

	/**
	 * import file to database
	 * 
	 * @param reader
	 *            reader
	 * @throws Exception
	 *             Exception
	 */
	public void read(InputStream stream) throws Exception {
		containsErrors = false;
		spectra.clear();
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));

		// lesen der ersten zeile welcher der header ist
		String line = reader.readLine();

		while (line.length() == 0) {
			line = reader.readLine();
		}

		logger.debug("read:\n\t\t"+line);
		logger.debug("line length was: " + line.length());

		// extract headers
		// logger.info(
		// " extract header file from file:\t" + this.getFile());
		String[] header = line.split(this.seperator);

		if (logger.isDebugEnabled()) {
			logger.debug("header configuration:");
			for (String s : header) {
				logger.debug(s);
			}
		}
		logger.info("trying to find implementation");

		pegasusHeader = HeaderFactory.create().getHeader(header);

		/*
		 * einlesen der spectren, zeile f?r zeile...
		 */
		while ((line = reader.readLine()) != null) {
			// check if the line contains #### errors
			if (line.indexOf("####") > -1) {
				logger.warn("this line is invalid, skip it and go to next line");
				logger.info(line);
				containsErrors = true;
			} else {
				// datas
				String[] data = line.split(this.seperator);

				// hash for this line
				Map<String, String> hash = new HashMap<String, String>();

				// copy datas to hash
				// logger.debug(" copy datas to hash map");
				for (int i = 0; i < data.length; i++) {
					try {
						if (data[i].indexOf("saturated") > -1) {
							data[i] = data[i].split(" ")[1];
							hash.put("SATURATED", "true");
						}
					} catch (Exception e) {
						logger.error(" exception at this point "
								+ e.getMessage()
								+ " - setting value for header =" + header[i]
								+ " to zero");
					}
				}

				try {
					hash = pegasusHeader.transform(header, data);

					// convert rt to ri
					double v = new Double(hash.get("R.T. (seconds)").replace(
							',', '.')).doubleValue();
					v = v * 1000;
					hash.put("Retention Index", String.valueOf(v));

					String spectra = ValidateSpectra.convert(ValidateSpectra
							.sizeDown(ValidateSpectra.convert(hash
									.get("Spectra"))));

					String apex = ValidateApexMasses.cleanApex(
							(hash.get("Quant Masses")), spectra,
							Integer.parseInt(hash.get("UniqueMass")));

					if (hash.get("Quant S/N").length() == 0) {
						logger.warn(" no quant sn definied, using signal noise and use as quantmasses only the unique mass");
						hash.put("Quant Masses", hash.get("UniqueMass"));
						hash.put("Quant S/N", hash.get("S/N"));
					} else {
						hash.put("Quant Masses", apex);
					}

					// updaten der hashmap
					hash.put("Spectra", spectra);
					this.spectra.add(hash);
				} catch (HeaderProblemException e) {
					logger.warn(e.getMessage());
				}
			}
		}

		reader.close();
	}

	/**
	 * @author wohlgemuth
	 * @version Nov 8, 2005
	 * @see edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.SampleDataProvider#getSpectra()
	 */
	public Map[] getSpectra() {
		return this.spectra.toArray(new Map[this.spectra.size()]);
	}

	/**
	 * @author wohlgemuth
	 * @version Nov 8, 2005
	 * @see edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.SampleDataProvider#setSource(edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.source.Source)
	 */
	public void setSource(Source source) {
		this.source = source;
	}

	/**
	 * @author wohlgemuth
	 * @version Nov 9, 2005
	 * @see Runnable#run()
	 */
	public void run() throws Exception {
		this.read(this.source.getStream());
	}

	public boolean isContainsErrors() {
		return containsErrors;
	}

	public void setContainsErrors(boolean containsErrors) {
		this.containsErrors = containsErrors;
	}

}
