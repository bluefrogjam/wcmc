package edu.ucdavis.genomics.metabolomics.binbase.algorythm.filter.modify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.filter.MassSpecModifier;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateSpectra;

/**
 * multiplies an ion with a given factor and is mostly needed, when there are issues with the detector or the instrument tuning
 * @author wohlgemuth
 *
 */
public class MultiplyIonModifier extends MassSpecModifier {

	private Map<Integer, Double> ions = new HashMap<Integer, Double>();

	@Override
	protected void doConfigure(List<Element> e) {
		for (Element el : e) {
			if (el.getName().equals("ion")) {
				ions.put(Integer.parseInt(el.getAttributeValue("value")),
						Double.parseDouble(el.getAttributeValue("multiplier")));
				logger.info("configure -  ion: "
						+ Integer.parseInt(el.getAttributeValue("value"))
						+ " factor: "
						+ Double.parseDouble(el.getAttributeValue("multiplier")));
			}
		}
	}

	/**
	 * removes the specified ions from the spectra
	 */
	@Override
	public Map<String, Object> modify(Map<String, Object> spectra) {

		String spectraString = (String) spectra.get("spectra");
		double[][] converted = ValidateSpectra.convert(spectraString);

		for (Integer i : ions.keySet()) {
			if (logger.isDebugEnabled()) {
				logger.debug("before: "
						+ converted[i - 1][ValidateSpectra.FRAGMENT_ABS_POSITION]
						+ " and ion: "
						+ converted[i - 1][ValidateSpectra.FRAGMENT_ION_POSITION]
						+ " - current ion: " + i);
			}
			converted[i - 1][ValidateSpectra.FRAGMENT_ABS_POSITION] = converted[i - 1][ValidateSpectra.FRAGMENT_ABS_POSITION]
					* ions.get(i);
			converted[i - 1][ValidateSpectra.FRAGMENT_REL_POSITION] = converted[i - 1][ValidateSpectra.FRAGMENT_REL_POSITION]
					* ions.get(i);

			if (logger.isDebugEnabled()) {
				logger.debug("after: "
						+ converted[i + 1][ValidateSpectra.FRAGMENT_ABS_POSITION]);
			}
		}

		spectraString = ValidateSpectra.convert(converted);
		spectra.put("spectra", spectraString);
		spectra.put("CALCULATED_SPECTRA",
				ValidateSpectra.convert(spectraString));

		return spectra;
	}

}
