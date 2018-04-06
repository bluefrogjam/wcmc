package edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.anotation;

import java.util.Map;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateApexMasses;

/**
 * compares if the spectra unique mass is in the list of allowed masses
 *
 * @author wohlgemuth
 */
public class UniqueIonFilter extends BasicFilter {

    /**
     * we comparing if the unknown unique mass is in the bins list of unique
     * masses it will always return true if the unkwown peak is a large peak
     * because these tend to make problems
     */
    protected boolean compare(Map<String, Object> bin, Map<String, Object> unk) {

        int binId = Integer.parseInt(bin.get("bin_id").toString());
        int spectraId = Integer.parseInt(unk.get("spectra_id").toString());

        int unkUnique = FilterUtilities.getUniqueMass(unk);

        int unique = FilterUtilities.getUniqueMass(bin);
        int binIsotopeUnique = unique + 1;

        if (logger.isDebugEnabled()) {
            logger.debug("compare " + unkUnique + " to " + unique);
        }
        if (FilterUtilities.isLargePeak(FilterUtilities.getSingnalNoise(unk)) == true) {
            logger.debug("found a large peak --> ignore unique ions");
            getDiagnosticsService()
                    .diagnosticActionSuccess(
                            spectraId,
                            binId,
                            this.getClass(),
                            "filtering by unique ion",
                            "massspec was accepted, since it's a very large peak and for this reason we disable this filter",
                            new Object[]{});

            return true;
        } else {
            if ((unique == unkUnique)) {
                getDiagnosticsService()
                        .diagnosticActionSuccess(
                                spectraId,
                                binId,
                                this.getClass(),
                                "filtering by unique ion",
                                "massspec was accepted, since the unique ions were identical",
                                new Object[]{});

                return true;
            } else if ((binIsotopeUnique == unkUnique)) {
                getDiagnosticsService()
                        .diagnosticActionSuccess(
                                spectraId,
                                binId,
                                this.getClass(),
                                "filtering by unique ion",
                                "massspec was accepted, since the unique ion was the +1 isotope of the bin was identical to the unknown unique ion",
                                new Object[]{});

                return true;
            } else if ((ValidateApexMasses.contains((String) bin.get("apex"),
                    unkUnique) == true)) {
                getDiagnosticsService()
                        .diagnosticActionSuccess(
                                spectraId,
                                binId,
                                this.getClass(),
                                "filtering by unique ion",
                                "massspec was accepted, since the unique ion was in the list of bin apex masses",
                                new Object[]{});

                return true;
            } else {
                getDiagnosticsService()
                        .diagnosticActionFailed(
                                spectraId,
                                binId,
                                this.getClass(),
                                "filtering by unique ion",
                                "massspec was rejected, since neither the unique ions are identical nor the unique ion is in the list of the bins apexing masses",
                                new Object[]{});

                this.setReasonForRejection("unique mass was not in list of allowed unique masses");
                return false;
            }
        }
    }
}
