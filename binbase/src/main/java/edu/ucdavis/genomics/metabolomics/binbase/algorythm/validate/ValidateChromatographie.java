/*
 * Created on Aug 5, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate;

import java.util.Map;

import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.Diagnostics;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.DiagnosticsService;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.DiagnosticsServiceFactory;
import edu.ucdavis.genomics.metabolomics.util.SQLObject;

/**
 * @author wohlgemuth
 * @version Aug 5, 2003 <br>
 *          BinBaseDatabase
 * @description Dient zum validieren von Spectren, ist jedoch recht langsam!
 */
public class ValidateChromatographie extends SQLObject implements Diagnostics {
	/**
	 * DOCUMENT ME!
	 * 
	 * @uml.property name="validate"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	private static ValidateChromatographie validate = null;

	/**
	 * DOCUMENT ME!
	 */
	private Map spectra;

	/**
	 * @version Aug 5, 2003
	 * @author wohlgemuth <br>
	 * @param spectra
	 */
	/**
	 * @author wohlgemuth
	 * @version Aug 5, 2003 <br>
	 * 
	 */
	private ValidateChromatographie() {
	}

	@Override
	public DiagnosticsService getDiagnosticsService() {
		return service;
	}

	private DiagnosticsService service = DiagnosticsServiceFactory
			.newInstance().createService();

	private int spectraId;

	/**
	 * ?berpr?ft ob das spektrum valide ist
	 * 
	 * @version Aug 5, 2003
	 * @author wohlgemuth <br>
	 * @param spectra
	 * @param connection
	 * @return
	 */
	public static boolean isValidSpectra(Map spectra) {
		/**
		 * ist die apex valid
		 */
		ValidateChromatographie chroma = ValidateChromatographie
				.getInstance(spectra);

		return chroma.validate();
	}
	/**
	 * converts the internal map from pegasus format to binbasefomat
	 * @param spectra
	 * @return
	 */
	public static boolean isValidFetchedSpectra(Map spectra) {
		/**
		 * ist die apex valid
		 */

		spectra.put("Spectra", spectra.get("spectra"));
		spectra.put("Quant Masses", spectra.get("apex"));
		spectra.put("UniqueMass", spectra.get("uniquemass"));
		
		ValidateChromatographie chroma = ValidateChromatographie
				.getInstance(spectra);

		return chroma.validate();
	}

	/**
	 * die haupt testmethode welche die parameter welcher in der configdatei
	 * angegeben sind ?berpr?ft
	 * 
	 * @version Aug 5, 2003
	 * @author wohlgemuth <br>
	 * @return
	 */
	public boolean validate() {
		if (Boolean.valueOf(
				CONFIG.getValue("deconvolution.validate.uniquemass.value"))
				.booleanValue() == true) {
			if (this.isValidUnique() == false) {
				logger.debug(" unique test failed!");

				if (this.spectraId > -1)
					getDiagnosticsService().diagnosticActionFailed(spectraId,
							this.getClass(), "validate chromatography",
							"the unique ion test failed", new Object[] {});

				return false;
			}
		}

		if (Boolean.valueOf(
				CONFIG.getValue("deconvolution.validate.apexmasses"))
				.booleanValue() == true) {
			if (this.isValidApex() == false) {
				logger.debug(" apex test 1 failed!");

				if (this.spectraId > -1)
					getDiagnosticsService().diagnosticActionFailed(spectraId,
							this.getClass(), "validate chromatography",
							"the first apexing masses test failed",
							new Object[] {});

				return false;
			}

			if (this.isValidApexOne() == false) {
				logger.debug(" apex test 2 failed!");

				if (this.spectraId > -1)
					getDiagnosticsService().diagnosticActionFailed(spectraId,
							this.getClass(), "validate chromatography",
							"the second apexing masses test failed",
							new Object[] {});

				return false;
			}
		}

		if (this.spectraId > -1)
			getDiagnosticsService().diagnosticActionSuccess(spectraId,
					this.getClass(), "validate chromatography",
					"the validation was successful", new Object[] {});

		return true;
	}

	/**
	 * um eine instance zu erzeugen wird diese methode ben?tigt,
	 * 
	 * @version Aug 5, 2003
	 * @author wohlgemuth <br>
	 * @param spectra
	 * @return
	 */
	private synchronized static ValidateChromatographie getInstance(Map spectra) {
		if (validate == null) {
			validate = new ValidateChromatographie();
		}

		validate.setSpectra(spectra);

		return validate;
	}

	/**
	 * setzt die parameter, alles andere wird aus der config datei ausgelesen
	 * 
	 * @version Aug 5, 2003
	 * @author wohlgemuth <br>
	 * @param spectra
	 * 
	 * @uml.property name="spectra"
	 */
	private void setSpectra(Map spectra) {
		this.spectra = spectra;

		if (spectra.get("spectra_id") != null) {
			this.spectraId = Integer.parseInt(spectra.get("spectra_id")
					.toString());
		} else {
			this.spectraId = -1;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	private boolean isValidApex() {
		return ValidateApexMasses.contains(
				(String) spectra.get("Quant Masses"),
				Integer.parseInt((String) this.spectra.get("UniqueMass")));
	}

	/**
	 * ?berpr?ft ob die apeexmassen im spektrum enthalten sind
	 * 
	 * @version Aug 5, 2003
	 * @author wohlgemuth <br>
	 * @return
	 */
	private boolean isValidApexOne() {
		try {
			String spectra = ValidateSpectra.convert(ValidateSpectra
					.sizeDown(ValidateSpectra.convert((String) this.spectra
							.get("Spectra"))));
			String apex = ValidateApexMasses.cleanApex(
					(String) (this.spectra.get("Quant Masses")), spectra,
					Integer.parseInt((String) this.spectra.get("UniqueMass")));

			if (apex.length() == 0) {
				logger.debug(" apex masses are not included in the mass spec");

				return false;
			}

			return true;
		} catch (Exception e) {

			logger.error(e.getMessage(),e);
			return false;
		}
	}

	/**
	 * ?berpr?ft ob das unique ion bestimmte anforderungen erf?llt,
	 * 
	 * <li>
	 * <ul>
	 * gross genug, setting steht in der konfigurationsdatei
	 * </ul>
	 * <ul>
	 * im spektrum enthalten
	 * </ul>
	 * </li>
	 * 
	 * @version Aug 6, 2003
	 * @author wohlgemuth <br>
	 * @return
	 */
	private boolean isValidUnique() {
		try {
			boolean value = ValidateUniqueMass.isValidUnique(
					(String) this.spectra.get("Spectra"),
					Integer.parseInt((String) this.spectra.get("UniqueMass")));

			return value;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return false;
		}
	}
}
