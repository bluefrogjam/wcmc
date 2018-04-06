/*
 * Created on Aug 8, 2003
 *
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate;

import org.slf4j.Logger;

import edu.ucdavis.genomics.metabolomics.exception.ValueNotFoundException;
import edu.ucdavis.genomics.metabolomics.util.BasicObject;
import edu.ucdavis.genomics.metabolomics.util.math.SpectraArrayKey;
import edu.ucdavis.genomics.metabolomics.util.search.BinarySearch;
import edu.ucdavis.genomics.metabolomics.util.search.Searchable;
import edu.ucdavis.genomics.metabolomics.util.sort.Quicksort;
import edu.ucdavis.genomics.metabolomics.util.sort.Sortable;

/**
 * @author wohlgemuth
 * @version Aug 8, 2003 <br>
 *          BinBaseDatabase
 * @description
 */
public class ValidateUniqueMass extends BasicObject {
	private static Logger logger = LoggerFactory.getLogger(ValidateUniqueMass.class);

	/**
	 * DOCUMENT ME!
	 * 
	 * @param spectra
	 *            DOCUMENT ME!
	 * @param uniqueMass
	 *            DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static boolean isValidUnique(String spectra, int uniqueMass) {
		try {
			Searchable search = new BinarySearch();
			Sortable sort = new Quicksort();

			/*
			 * komprimieren des array auf die en?tigten elemente
			 */
			double[][] array = ValidateSpectra.sizeDown(ValidateSpectra
					.convert(spectra));

			/*
			 * suchen des unique ions
			 */
			int position = search.search(array,
					SpectraArrayKey.FRAGMENT_ION_POSITION, uniqueMass);

			/*
			 * ermitteln der abundance
			 */
			double uniqueSn = array[position][SpectraArrayKey.FRAGMENT_ABS_POSITION];

			/*
			 * sortieren des arrays
			 */
			array = sort.sort(array, SpectraArrayKey.FRAGMENT_ABS_POSITION);

			/*
			 * abundance des base peaks
			 */
			double baseSn = array[array.length - 1][SpectraArrayKey.FRAGMENT_ABS_POSITION];

			double ratio = uniqueSn / baseSn * 100;

			if (logger.isDebugEnabled()) {
				logger.debug("base sn: " + baseSn);

				logger.debug("unique sn: " + uniqueSn);

				logger.debug("ratio: " + ratio);
				logger.debug("exspected ratio: "
						+ Double.parseDouble(CONFIG
								.getValue("deconvolution.validate.uniquemass.uniqueRatio")));
			}
			if (ratio <= Double.parseDouble(CONFIG
					.getValue("deconvolution.validate.uniquemass.uniqueRatio"))) {
				return false;
			} else {
				return true;
			}
		}
		/*
		 * wenn das unique ion nicht gefunden wird
		 */
		catch (ValueNotFoundException e) {
			return false;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
