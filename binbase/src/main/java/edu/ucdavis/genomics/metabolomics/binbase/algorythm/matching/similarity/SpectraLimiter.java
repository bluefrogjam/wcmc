package edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.similarity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateSpectra;

/**
 * this class is used to reduce the masss spec to the important ammount of
 * masses and is related to BINBASE-408
 * 
 * @author wohlgemuth
 */
public class SpectraLimiter {

	/**
	 * the minimum fragment, if we didn't find a fragment
	 */
	private int minFragment = 0;

	/**
	 * the maximum fragment
	 */
	private int maxFragment = 500;
	/**
	 * our logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * contsrtuctor which does the heavy lifting
	 * 
	 * @param configuration
	 * @param database
	 */
	@SuppressWarnings("unchecked")
	public SpectraLimiter(Element configuration, String database) {
		if (configuration != null) {
			logger.info("\n" + new XMLOutputter(Format.getPrettyFormat()).outputString(configuration));

			List<Element> columns = configuration.getChildren("column");

			if (columns != null) {
				if (columns.isEmpty() == false) {
					for (Element e : columns) {
						if (e.getAttribute("name") != null) {

							Pattern pattern = Pattern.compile(e.getAttributeValue("name"));

							if (pattern.matcher(database.toLowerCase()).matches()) {
								if (e.getAttribute("beginFragment") != null) {
									try {
										this.minFragment = e.getAttribute("beginFragment").getIntValue();
									}
									catch (DataConversionException e1) {
										logger.warn("error: " + e1.getMessage());
									}
								}
								else {
									logger.debug("sorry attribute \"beginFragment\" was not found so we use: " + minFragment);
								}
								if (e.getAttribute("endFragment") != null) {
									try {
										this.maxFragment = e.getAttribute("endFragment").getIntValue();
									}
									catch (DataConversionException e1) {
										logger.debug("error: " + e1.getMessage());
									}
								}
								else {
									logger.debug("sorry attribute \"endFragment\" was not found so we use: " + maxFragment);
								}

								return;
							}
						}
						else {
							logger.debug("sorry no attribute \"name\" provided, so we skip this element");
						}
					}
					logger.debug("sorry no matching configuration provided, so we use the default values of: " + minFragment + "  and " + maxFragment);
				}
				else {
					logger.debug("sorry no configuration provided, so we use the default values of: " + minFragment + "  and " + maxFragment);
				}
			}
			else {
				logger.debug("sorry no column provided, so we use the default values of: " + minFragment + "  and " + maxFragment);
			}
		}
		else {
			logger.warn("sorry no configuration provided, so we use the default values of: " + minFragment + "  and " + maxFragment);
		}
	}

	/**
	 * factory method which generates a limiter, depending on our configurations
	 * 
	 * @param configuration
	 * @return
	 */
	public static SpectraLimiter createLimiter(Element configuration, String database) {
			return new SpectraLimiter(configuration, database);
	}

	/**
	 * set;s the ions which are smaller or larger than the fragment ions to 0
	 * 
	 * @param spectra
	 * @return
	 */
	public String limitSpectra(String spectra) {
		double[][] spec = ValidateSpectra.convert(spectra);

		for (int i = 0; i < ValidateSpectra.MAX_ION; i++) {
			if (spec[i][ValidateSpectra.FRAGMENT_ION_POSITION] < minFragment) {
				spec[i][ValidateSpectra.FRAGMENT_ABS_POSITION] = 0;
				spec[i][ValidateSpectra.FRAGMENT_REL_POSITION] = 0;

			}
			else if (spec[i][ValidateSpectra.FRAGMENT_ION_POSITION] > maxFragment) {
				spec[i][ValidateSpectra.FRAGMENT_ABS_POSITION] = 0;
				spec[i][ValidateSpectra.FRAGMENT_REL_POSITION] = 0;
			}
		}
		return ValidateSpectra.convert(spec);
	}

	public int getMinFragment() {
		return minFragment;
	}

	public int getMaxFragment() {
		return maxFragment;
	}
}
