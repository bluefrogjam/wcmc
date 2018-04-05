/*
 * Created on Mar 2, 2005
 *
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.filter.BasepeakFilter;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.filter.CombinedFilter;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.filter.MassSpecFilter;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.methods.CorrectionMethod;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateSpectra;
import edu.ucdavis.genomics.metabolomics.util.database.ConnectionFactory;
import edu.ucdavis.genomics.metabolomics.util.math.SpectraArrayKey;

/**
 * @author wohlgemuth
 */
public class RecrusiveCorrection extends AbstractMatching {

    private static final String BIN_IN_ANNOTATED_MAP = "BIN";

    /**
     * contains all failed standards
     */
    private List<Map<String, Object>> failedStandards;

    /**
     * DOCUMENT ME!
     */
    private Set<Object> foundStandards;

    /**
     * used for validation
     */
    private Map<String, Object> highestHit;

    /**
     * DOCUMENT ME!
     */
    private PreparedStatement libraryAllStatement;

    /**
     * do we force distance checks during the matching process
     */
    private boolean distanceCheck = true;

    /**
     *
     */
    public RecrusiveCorrection() {
        super();

        this.setUnknownFilter(new CombinedFilter(Arrays.<MassSpecFilter>asList(new BasepeakFilter(), new BasepeakFilter(73), new BasepeakFilter(41))));
    }

    /**
     * contains a ArrayList with all failed standards so we can check these by
     * our self
     *
     * @return Returns the failedStandards.
     * @uml.property name="failedStandards"
     */
    public Collection<Map<String, Object>> getFailedStandards() {
        return failedStandards;
    }

    /**
     * s
     *
     * @see Annotation#run()
     */
    public int run() {
        failedStandards = new ArrayList<Map<String, Object>>();
        foundStandards = new HashSet<Object>();

        this.getResultHandler().setMatchable(this);

        highestHit = null;

        Collection<Map<String, Object>> list = this.getBins();

        logger.debug("bins available: " + list.size());
        logger.debug("unknown available: " + this.getUnknowns().size());

        // looking for the best hit at first, need for validation
        logger.info("find bin for validation for sample: " + this.getSampleId());

        for (Map<String, Object> bin : list) {
            this.findBest(bin);
        }

        if (highestHit == null) {
            logger.warn("nothing found in this sample!");

            return -1;
        }

        logger.debug("find other bins");

        for (Map<String, Object> entry : list) {
            this.match(entry);
        }

        logger.info("deal with failed standards");

        if (this.getFailedStandards().isEmpty() == false) {
            logger.info("calculate failed standards");

            for (Map<String, Object> next : getFailedStandards()) {

                // we need to check if this one was already assigned

                boolean contains = false;
                for (Map<String, Object> spectra : getAssigned()) {
                    if (next.get("name").equals(
                            ((Map<String, Object>) spectra
                                    .get(BIN_IN_ANNOTATED_MAP)).get("name"))) {
                        contains = true;
                    }
                }

                if (contains == false) {
                    this.match(next);
                } else {
                    logger.debug(next.get("name") + " already assigned!");
                }
            }
        } else {
            logger.info("no failed standards found");
        }

        // save calculation
        logger.debug("save assigned standards");

        for (Map<String, Object> map : this.getAssigned()) {

            try {
                this.getResultHandler().assignBin(map);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        try {
            this.setBins(this.getData(this.libraryAllStatement));
        } catch (SQLException e) {
            logger.error("Exception at all available standard loading for binlist");
        }

        logger.debug("found standards: " + this.getAssigned().size());

        return 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @param map DOCUMENT ME!
     */
    protected void addAssigned(Map<String, Object> map) {
        Map<String, Object> bin = (Map<String, Object>) map
                .get(BIN_IN_ANNOTATED_MAP);

        // is this bin already assigned
        if (foundStandards.contains(bin)) {
            logger.info("running duplicated assigned bin test...");
            Iterator<Map<String, Object>> it = this.getAssigned().iterator();

            while (it.hasNext()) {
                Map<String, Object> spectra = it.next();
                Map<String, Object> assignedBin = (Map<String, Object>) spectra
                        .get(BIN_IN_ANNOTATED_MAP);

                if (bin.equals(assignedBin)) {
                    logger.info("already assigned, make similarity test to get the right one");

                    double simOld = Double.parseDouble(spectra
                            .get("similarity").toString());
                    double simNew = Double.parseDouble(map.get("similarity")
                            .toString());

                    if (simNew > simOld) {
                        logger.info("new similarity is higher");
                        it.remove();
                    } else {
                        logger.info("stay with old assigment");

                        return;
                    }
                } else {
                    logger.info("bin is not yet assigned so we can take it!");
                }
            }
        } else {
            logger.info("found standards, adding: " + bin.get("name") + " - "
                    + map.get("retention_index"));
            foundStandards.add(bin);
        }

        map.put("RIC", "true");
        super.addAssigned(map);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * edu.ucdavis.genomics.metabolomics.binbase.binlib.algorythm.SQLObject#
     * prepareStatements()
     */
    protected void prepareStatements() throws Exception {
        super.prepareStatements();
        this.libraryAllStatement = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue(CLASS + ".libraryAllStatement"));

    }

    /**
     * @param result
     */
    private boolean calculateDistance(List<Map<String, Object>> result) {

        logger.debug("calculate distance since for " + result.size()
                + " objects");

        try {

            boolean accepted = false;

            Iterator<Map<String, Object>> it = result.iterator();
            while (it.hasNext()) {
                Map<String, Object> one = it.next();
                Map<String, Object> bin = (Map) one.get(BIN_IN_ANNOTATED_MAP);
                Map<String, Object> two = highestHit;
                Map<String, Object> bin2 = (Map) two.get(BIN_IN_ANNOTATED_MAP);

                logger.debug("calculate distance for bin: " + bin.get("name")
                        + " against " + bin2.get("name"));

                double distance = this.calculateDistance(one, two);

                double minRatio = Double.parseDouble(bin.get(
                        "min_distance_ratio").toString());
                double maxRatio = Double.parseDouble(bin.get(
                        "max_distance_ratio").toString());

                if (Math.abs(minRatio - (-1)) < 0.0001) {
                    logger.warn("min ratio not set, set it to default");
                    minRatio = 0.9;
                }

                if (Math.abs(maxRatio - (-1)) < 0.0001) {
                    logger.warn("max ratio not set, set it to default");
                    maxRatio = 1.1;
                }

                logger.debug("distance for: " + bin.get("name") + " vs "
                        + ((Map) two.get(BIN_IN_ANNOTATED_MAP)).get("name")
                        + " " + distance);

                if ((distance >= minRatio) && (distance <= maxRatio)) {
                    logger.debug("accepted "
                            + ((Map) one.get(BIN_IN_ANNOTATED_MAP)).get("name"));
                    accepted = true;
                } else {
                    logger.debug("rejected for bin: "
                            + one.get("retention_index"));

                    logger.debug("rejected bin details: "
                            + ((Map) one.get(BIN_IN_ANNOTATED_MAP)).keySet());

                    if (this.getFailedStandards().contains(
                            one.get(BIN_IN_ANNOTATED_MAP)) == false) {
                        logger.debug("add bin to list of failed standards");
                        this.getFailedStandards().add(
                                (Map<String, Object>) one
                                        .get(BIN_IN_ANNOTATED_MAP));
                    }

                    logger.debug("removing bin from map");
                    one.put(BIN_IN_ANNOTATED_MAP, null);
                    accepted = false;
                    it.remove();
                }

            }

            return accepted;
        } catch (Exception e) {
            logger.error(
                    "Exception occurred during distance calculation: "
                            + e.getMessage(), e);

            return false;
        }

        // logger.debug("was not able to validate any of the bins");
        // return false;
    }

    /**
     * calculates the dinstance between two maps
     *
     * @param map
     * @param map2
     */
    private double calculateDistance(Map map, Map map2) {
        Map spectraOne = map;
        Map binOne = (Map) spectraOne.get(BIN_IN_ANNOTATED_MAP);

        Map spectraTwo = map2;
        Map binTwo = (Map) spectraTwo.get(BIN_IN_ANNOTATED_MAP);

        return calculateDistance(spectraOne, binOne, spectraTwo, binTwo);
    }

    private double calculateDistance(Map spectraOne, Map binOne,
                                     Map spectraTwo, Map binTwo) {

        double binRetentionIndexOne = Double.parseDouble(binOne.get(
                "retention_index").toString());
        double binRetentionIndexTwo = Double.parseDouble(binTwo.get(
                "retention_index").toString());
        double spectraRetentionIndexOne = Double.parseDouble(spectraOne.get(
                "retention_index").toString());
        double spectraRetentionIndexTwo = Double.parseDouble(spectraTwo.get(
                "retention_index").toString());

        double binDifference = 0;
        double spectraDifference = 0;

        if (binRetentionIndexOne >= binRetentionIndexTwo) {
            binDifference = binRetentionIndexOne - binRetentionIndexTwo;
            spectraDifference = spectraRetentionIndexOne
                    - spectraRetentionIndexTwo;
        } else {
            binDifference = binRetentionIndexTwo - binRetentionIndexOne;
            spectraDifference = spectraRetentionIndexTwo
                    - spectraRetentionIndexOne;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("");
            logger.debug("calculate distance result");
            logger.debug("");
            logger.debug("(1) bin ri " + binOne.get("name") + ": "
                    + binRetentionIndexOne);
            logger.debug("(2) bin ri " + binTwo.get("name") + ": "
                    + binRetentionIndexTwo);
            logger.debug("");
            logger.debug("(1) spec ri: " + spectraRetentionIndexOne);
            logger.debug("(2) spec ri: " + spectraRetentionIndexTwo);
            logger.debug("bin difference    : " + binDifference);
            logger.debug("spectra difference: " + spectraDifference);
        }

        logger.info("abs spec diff: " + Math.abs(spectraDifference - 0));
        logger.info("abs bin diff: " + Math.abs(binDifference - 0));

        if (Math.abs(spectraDifference - 0) < 0.0001) {
            return 0;
        } else if (Math.abs(binDifference - 0) < 0.0001) {
            return 0;
        }

        double ratio = binDifference / spectraDifference;
        logger.debug("ratio = " + ratio);

        return ratio;

    }

    /**
     * calculates the quantifier itself to the given basepeak
     *
     * @param quantifier
     * @param spectra
     * @return
     */
    private double calculateQuantifier(int quantifier, int mass,
                                       double[][] spectra) {

        logger.debug("using quantifier: " + quantifier);
        logger.debug("ion: " + spectra[quantifier - 1][SpectraArrayKey.FRAGMENT_ION_POSITION] + "/" + spectra[mass - 1][SpectraArrayKey.FRAGMENT_ION_POSITION]);
        logger.debug("rel intensity: " + spectra[quantifier - 1][SpectraArrayKey.FRAGMENT_REL_POSITION] + "/" + spectra[mass - 1][SpectraArrayKey.FRAGMENT_REL_POSITION]);
        logger.debug("abs intensity: " + spectra[quantifier - 1][SpectraArrayKey.FRAGMENT_ABS_POSITION] + "/" + spectra[mass - 1][SpectraArrayKey.FRAGMENT_ABS_POSITION]);

        double binQuantify = spectra[quantifier - 1][SpectraArrayKey.FRAGMENT_REL_POSITION]
                / spectra[mass - 1][SpectraArrayKey.FRAGMENT_REL_POSITION];

        logger.debug("ratio is: " + String.format("%.6f", binQuantify));

        return binQuantify;
    }

    /**
     * required to find the first best hit on which everything depends
     *
     * @param bin the masspec map
     */
    private void findBest(Map<String, Object> bin) {
        Collection<Map<String, Object>> unknowns = this.getUnknowns();
        Iterator<Map<String, Object>> it = unknowns.iterator();

        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

        double quantifierMinRatio = Double.parseDouble(bin.get("min_ratio")
                .toString());
        double quantifierMaxRatio = Double.parseDouble(bin.get("max_ratio")
                .toString());
        double minApexSn = Double
                .parseDouble(bin.get("min_apex_sn").toString());

        int quantifier = (int) Double.parseDouble(bin.get("qualifier").toString());

        if(quantifier > 0) {
            double[][] binSpectra = ValidateSpectra.convert((String) bin
                    .get("spectra"));

            logger.debug("current bin: " + bin.get("name"));

            while (it.hasNext()) {
                List<Map<String, Object>> temp = (findHit(bin, it.next(),
                        quantifierMinRatio, quantifierMaxRatio, minApexSn,
                        quantifier, binSpectra, false));

                result.addAll(temp);
            }

            logger.debug("find best result = " + result.size());

            if (result.size() == 1) {
                Map<String, Object> spectra = result.get(0);
                spectra.put(BIN_IN_ANNOTATED_MAP, bin);

                if (highestHit == null) {
                    highestHit = spectra;
                }

                double simBest = Double.parseDouble(highestHit.get(
                        "SPECTRA_SIMILARITY").toString());
                double simCurrent = Double.parseDouble(spectra.get(
                        "SPECTRA_SIMILARITY").toString());

                if (simCurrent >= simBest) {
                    logger.info(bin.get("name")
                            + " acceppted as bin for validation with sim: "
                            + simCurrent);
                    highestHit = spectra;
                } else {
                    logger.info(bin.get("name")
                            + " rejected as bin for validation with sim: "
                            + simCurrent);
                }
            }
        }
        else{
            logger.warn("ignored current bin, since it's standard configuration was not finished yet! Please ensure that it was tuned! - " + bin.get("name") + "/" + bin.get("bin_id"));
        }
    }

    /**
     * finds a hit for this given bin, with the set parameters
     *
     * @param bin
     * @param unk
     * @param quantifierMinRatio
     * @param quantifierMaxRatio
     * @param minApexSn
     * @param quantifier
     * @param binSpectra
     * @return
     */
    private List<Map<String, Object>> findHit(Map<String, Object> bin,
                                              Map<String, Object> unk, double quantifierMinRatio,
                                              double quantifierMaxRatio, double minApexSn, int quantifier,
                                              double[][] binSpectra, boolean includeDistanceCheck) {

        List<Map<String, Object>> result = new Vector<Map<String, Object>>();

        try {

            double[][] spectra = ValidateSpectra.convert((String) unk
                    .get("spectra"));

            double intensity = spectra[Integer.parseInt(bin.get("quantmass").toString())-1][ValidateSpectra.FRAGMENT_ABS_POSITION];
            if (logger.isDebugEnabled()) {
                logger.debug("rt of bin: " + bin.get("retention_index"));

                logger.debug("rt of spectra: " + unk.get("retention_index"));

                logger.debug("id of spectra: " + unk.get("spectra_id"));
                logger.debug("intensity of spectra: " + intensity);


            }




            double minIntensity = 5000.0;

            double similarity = similarity(binSpectra, spectra);
            int basePeak = ValidateSpectra.calculateBasePeak(spectra);
            double apexSn = Double.parseDouble(unk.get("apex_sn").toString());
            double minSimilarity = Double.parseDouble(bin.get("min_similarity")
                    .toString());
            double binQuantify = calculateQuantifier(quantifier, basePeak,
                    spectra);

            double unkRetentionTime = Double.parseDouble(unk.get("retention_index").toString());
            double riDistance = Math.abs(Double.parseDouble(bin.get("retention_index").toString()) - Double.parseDouble(unk.get("retention_index").toString()));

            String binName = bin.get("name").toString();

            if(intensity > minIntensity) {
                logger.debug("> min intensity test was successful");
                if (apexSn > minApexSn) {
                    logger.debug("-> apex sn test was successful");

                    if (binQuantify > quantifierMinRatio) {
                        logger.debug("--> min quantifier test was successful");

                        if (binQuantify < quantifierMaxRatio) {
                            logger.debug("---> max quantifier test was successful");
                            if (similarity > minSimilarity) {
                                logger.debug("----> similarity test was successful");

                                // if there is no highest hit yet, we are just
                                // running a check to find the best hit

                                unk.put("intensity",intensity);
                                if (includeDistanceCheck) {

                                    // if this is the highest hit, the distance will
                                    // always be null
                                    if (bin.get("name").equals(
                                            ((Map) highestHit
                                                    .get(BIN_IN_ANNOTATED_MAP))
                                                    .get("name"))) {

                                        assignUnknownToBin(bin, unk, result,
                                                similarity);

                                        if (logger.isDebugEnabled()) {
                                            logger.debug(binName + " sim: "
                                                    + similarity + " apex: "
                                                    + apexSn + " quantify: "
                                                    + binQuantify + " rt: "
                                                    +unkRetentionTime
                                                    + " accepted since it was the best hit already!");
                                        }
                                    } else {
                                        // calculates the distance for us
                                        double distance = calculateDistance(unk,
                                                bin, highestHit,
                                                (Map) highestHit
                                                        .get(BIN_IN_ANNOTATED_MAP));

                                        double minRatio = Double.parseDouble(bin
                                                .get("min_distance_ratio")
                                                .toString());
                                        double maxRatio = Double.parseDouble(bin
                                                .get("max_distance_ratio")
                                                .toString());

                                        logger.info("measured distance to highest similarity hit: "
                                                + distance
                                                + " name is "
                                                + binName
                                                + " min ratio is "
                                                + minRatio
                                                + " and max ratio is " + maxRatio);

                                        if (distance > minRatio) {
                                            if (distance < maxRatio) {
                                                assignUnknownToBin(bin, unk,
                                                        result, similarity);
                                                if (logger.isDebugEnabled()) {
                                                    logger.debug(binName
                                                            + " sim: "
                                                            + similarity
                                                            + " apex: "
                                                            + apexSn
                                                            + " quantify: "
                                                            + binQuantify
                                                            + " rt: "
                                                            + unkRetentionTime
                                                            + " accepted"
                                                            + " with distance "
                                                            + distance);
                                                }
                                            } else {
                                                //check if it's in the window as ugly failback
                                                if(riDistance < 65000){
                                                    assignUnknownToBin(bin, unk,
                                                            result, similarity);
                                                    if (logger.isDebugEnabled()) {
                                                        logger.debug(binName
                                                                + " sim: "
                                                                + similarity
                                                                + " apex: "
                                                                + apexSn
                                                                + " quantify: "
                                                                + binQuantify
                                                                + " rt: "
                                                                + unkRetentionTime
                                                                + " accepted"
                                                                + " with ri distance "
                                                                + riDistance);
                                                    }
                                                }
                                                else {
                                                    if (logger.isDebugEnabled()) {
                                                        logger.debug(binName
                                                                + " sim: "
                                                                + similarity
                                                                + " apex: "
                                                                + apexSn
                                                                + " quantify: "
                                                                + binQuantify
                                                                + " rt: "
                                                                + unkRetentionTime
                                                                + " rejected because distance ratio was to high "
                                                                + distance + " as was the ri distance with " + riDistance);
                                                    }
                                                }

                                            }
                                        } else {
                                            if (logger.isDebugEnabled()) {
                                                logger.debug(binName
                                                        + " sim: "
                                                        + similarity
                                                        + " apex: "
                                                        + apexSn
                                                        + " quantify: "
                                                        + binQuantify
                                                        + " rt: "
                                                        + unkRetentionTime
                                                        + " rejected because distance ratio was to small "
                                                        + distance);
                                            }
                                        }
                                    }
                                } else {
                                    assignUnknownToBin(bin, unk, result, similarity);
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(bin.get("name") + " sim: "
                                                + similarity + " apex: " + apexSn
                                                + " quantify: " + binQuantify
                                                + " rt: "
                                                + unk.get("retention_index")
                                                + " accepted");
                                    }
                                }

                            } else {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("----> similarity test failed "
                                            + similarity + " vs " + minSimilarity);
                                }
                            }

                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug("---> max quantify test failed "
                                        + String.format("%.6f", binQuantify) + " vs " + String.format("%.6f", quantifierMaxRatio));
                            }
                        }

                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("--> min quantify test failed "
                                    + binQuantify + " vs " + quantifierMinRatio);
                        }
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("-> min apex test failed " + apexSn + " vs "
                                + minApexSn);
                    }
                }
            }
            else{
                if (logger.isDebugEnabled()) {
                    logger.debug("> intensity for bin's quantmass was " + intensity + " but needed to be larger than "
                            + minIntensity);
                }
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

        return result;
    }

    /**
     * links the unknown spectra to the bin and adds it to the result set
     *
     * @param bin
     * @param unk
     * @param result
     * @param similarity
     */
    private void assignUnknownToBin(Map<String, Object> bin,
                                    Map<String, Object> unk, List<Map<String, Object>> result,
                                    double similarity) {
        // next step test the distance ratios
        unk.put("SPECTRA_SIMILARITY", String.valueOf(similarity));
        unk.put("similarity", String.valueOf(similarity));
        unk.put(BIN_IN_ANNOTATED_MAP, bin);

        result.add(unk);
    }

    /**
     * match one bin massspec against all unknowns
     *
     * @param bin the masspec map
     */
    protected void match(Map<String, Object> bin) {
        Iterator<Map<String, Object>> it = this.getUnknowns().iterator();

        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

        double quantifierMinRatio = Double.parseDouble(bin.get("min_ratio")
                .toString());
        double quantifierMaxRatio = Double.parseDouble(bin.get("max_ratio")
                .toString());
        double minApexSn = Double
                .parseDouble(bin.get("min_apex_sn").toString());

        int retentionIndex = Integer.parseInt(bin.get("retention_index")
                .toString());

        int quantifier = Integer.parseInt(bin.get("qualifier").toString());

        double[][] binSpectra = ValidateSpectra.convert((String) bin
                .get("spectra"));

        if (logger.isDebugEnabled()) {
            logger.debug("");
            logger.debug("Bin Details");
            logger.debug("retention index = " + retentionIndex);
            logger.debug("name = " + bin.get("name"));
            logger.debug("qualifier = " + quantifier);
            logger.debug("min apex sn = " + minApexSn);
            logger.debug("min quantify = " + quantifierMinRatio);
            logger.debug("max quantify = " + quantifierMaxRatio);
            logger.debug("");
        }

        if(quantifier > 0 ) {
            while (it.hasNext()) {

                List<Map<String, Object>> temp = findHit(bin, it.next(),
                        quantifierMinRatio, quantifierMaxRatio, minApexSn,
                        quantifier, binSpectra, this.distanceCheck);
                logger.debug("");

                result.addAll(temp);
            }

            if (result.isEmpty()) {
                logger.warn("nothing found for " + bin.get("name")
                        + ", check log and chromatogram!");
            } else {
                if (result.size() != 1) {
                    logger.info("validate calculation found " + result.size()
                            + " possibilities for: " + bin.get("name"));

                    for (Map<String, Object> map : result) {
                        logger.info("ri: " + map.get("retention_index"));
                    }


                    //bin with the largest intensity wins

                    Collections.sort(result, new Comparator<Map>() {
                        @Override
                        public int compare(Map o1, Map o2) {

                            Double int1 = Double.parseDouble(o1.get("intensity").toString());

                            Double int2 = Double.parseDouble(o2.get("intensity").toString());

                            return int2.compareTo(int1);
                        }
                    });

                }

                Map best = result.get(0);
                logger.info("assigning: " + best.get("retention_index") + " as correct spectra");
                this.addAssigned(best);

            }
        }
        else{
            logger.warn("skipped bin, quantifier was not defined yet");
        }
    }

    public static void main(String[] args) throws Exception{

        CorrectionMethod correction = new CorrectionMethod();

        ConnectionFactory factory = ConnectionFactory.createFactory();

        Properties p = System.getProperties();
        p.setProperty("Binbase.database", "binbase");
        p.setProperty("Binbase.user", args[0]);
        p.setProperty("Binbase.password", args[1]);
        p.setProperty("Binbase.host", "venus.fiehnlab.ucdavis.edu");

        p.setProperty("Binbase.type", "3");


        factory.setProperties(p);
        Connection c = factory.getConnection();


        correction.setAllowHistoricSamples(false);
        correction.setAutoscale(false);


        correction.setConnection(c);
        correction.setSampleId(1050328);
        correction.run();

        System.out.println(correction.valid());

    }
}
