package edu.ucdavis.fiehnlab.ms.carrot.core.io.mzxml;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.IonMode;

/**
 * Created by wohlgemuth on 8/8/16.
 */
public class Spectra {

    Double retentionTime;

    Ion[] ions;

    Short msLevel;

    Integer scanNumber;

    Integer parentScan;

    Double precursor;

    IonMode ionMode;

    Integer precursorCharge;

	Boolean centroided;
}
