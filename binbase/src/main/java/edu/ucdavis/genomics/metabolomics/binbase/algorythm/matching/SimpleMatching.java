/*
 * Created on Apr 4, 2005
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.result.ProblematicResultHandler;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateSpectra;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.Reason;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.Result;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.Step;

/**
 * match the bins again the unknows and in the other direction. So if one
 * massspec matches one bin it works but if two massspecs matches two bin is
 * currently not implemented
 *
 * @author wohlgemuth
 */
public class SimpleMatching extends AbstractMatching {

    /**
     * DOCUMENT ME!
     */
    private Collection<Map<String, Object>> bins;

    /**
     * DOCUMENT ME!
     */
    private Collection<Map<String, Object>> unknows;

    /**
     * DOCUMENT ME!
     */
    private Map<String, Collection<Map<String, Object>>> binAnotations = new HashMap<String, Collection<Map<String, Object>>>();

    /**
     * DOCUMENT ME!
     */
    private double similarityOffset;

    private double largePeakSn;

    /**
     * @uml.property name="config"
     */

	/*
     * (non-Javadoc)
	 * 
	 * @see
	 * edu.ucdavis.genomics.metabolomics.binbase.matching.MPIMPAdapter#setConfig
	 * (org.jdom.Element)
	 */
    public void setConfig(Element config) {

        try {
            similarityOffset = CONFIG.getElement("values.filter.similarity")
                    .getAttribute("offset").getDoubleValue();
            logger.debug("using similariy offset of: " + similarityOffset);
        } catch (Exception e) {
            logger.error(
                    "error at getting value, using default value. Exception was: "
                            + e.getMessage(), e);
            similarityOffset = 100;
        }

        try {
            largePeakSn = CONFIG.getElement("values.filter.largePeakSize")
                    .getAttribute("sn").getDoubleValue();
            logger.debug("using large peak value of: " + largePeakSn);
        } catch (Exception e) {
            logger.error(
                    "error at getting value, using default value. Exception was: "
                            + e.getMessage(), e);
            largePeakSn = 500;
        }
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.binlib.algorythm.matching.DefaultMatching#run()
     */
    public int run() {
        logger.info("start matching");

        this.getResultHandler().setMatchable(this);

        try {
            /**
             * l?dt die tabellen in den speicher
             */

            logger.info("load bins");
            this.bins = this.getBins();

            logger.info("load unknows");
            this.unknows = this.getUnknowns();

            if (bins.size() == 0) {
                logger.warn("no bins defined, create new library from this sample with the following result handler: "
                        + this.getResultHandler().getClass().getSimpleName());
                if (this.getResultHandler().isNewBinAllowed()
                        && (this.getResultHandler() instanceof ProblematicResultHandler == false)) {
                    for (Map<String, Object> o : unknows) {
                        int spectraId = Integer.parseInt(o.get("spectra_id")
                                .toString());

                        getDiagnosticsService()
                                .diagnosticActionSuccess(
                                        spectraId,
                                        this.getClass(),
                                        "create new bin",
                                        "we are creating a new bin, because there were no existing bin's in the library",
                                        new Object[]{getResultHandler()
                                                .getClass().getSimpleName()});

                        this.newBin(o);
                    }
                }
            } else {
                logger.info("bins defined, start matching process!");

                anotate(unknows, bins);

                logger.info("saving assigned matrix to database");

                saveAssignmentToDatabase();

                if (this.getResultHandler().isNewBinAllowed()
                        && (!(this.getResultHandler() instanceof ProblematicResultHandler))) {
                    logger.info("generating bins");

                    // copy into a new list, to avoid duplicates
                    Collection<Map<String, Object>> newBin = new ArrayList<Map<String, Object>>(
                            unknows.size());

                    for (Map<String, Object> o : unknows) {
                        if (newBin.contains(o) == false) {
                            newBin.add(o);
                        }
                    }

                    // generate the new bins
                    for (Map<String, Object> o : newBin) {
                        int spectraId = Integer.parseInt(o.get("spectra_id")
                                .toString());

                        getDiagnosticsService()
                                .diagnosticActionSuccess(
                                        spectraId,
                                        this.getClass(),
                                        "create new bin",
                                        "we are creating a new bin, because there was no match for this massspec in the library",
                                        new Object[]{getResultHandler()
                                                .getClass().getSimpleName()});

                        this.newBin(o);
                    }
                }

            }
        } catch (Exception e) {
            sendException(e);
        }

        logger.info("done matching");

        return 0;
    }

    /**
     * remove anotated references
     *
     * @see edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.AbstractMatching#addAssigned(Map)
     */
    @SuppressWarnings("unchecked")
    protected void addAssigned(Map<String, Object> map) {
        Collection<Map<String, Object>> list = this.getBins();

        /**
         * clean up the bin list
         */
        for (Map<String, Object> bin : list) {
            Collection<Map<String, Object>> ano = (Collection<Map<String, Object>>) bin
                    .get("UNKNOWNS");

            if (ano != null) {
                ano.remove(map);
            }

            ano = (Collection<Map<String, Object>>) binAnotations.get(bin
                    .get("bin_id"));

            if (ano != null) {
                ano.remove(map);
            }
        }

        list = this.getUnknowns();

        /**
         * work over the unknowns
         */
        Map<String, Object> bin = (Map<String, Object>) map.get("BIN");

        for (Map<String, Object> spec : list) {

            if (spec.get("spectra_id").equals(map.get("spectra_is"))) {
                logger.info("delete unknown: " + spec.get("spectra_id"));
                displayMapDetails(spec);
                list.remove(spec);
            }
            Collection<Map<String, Object>> ano = (Collection<Map<String, Object>>) spec
                    .get("BINS");

            if (ano != null) {
                ano.remove(bin);
            }
        }

        double similarity = similarity(bin.get("spectra")
                .toString(), map.get("spectra").toString());
        map.put("similarity", String.valueOf(similarity));

        this.binAnotations.remove(bin.get("bin_id"));

        super.addAssigned(map);
    }

    /**
     * calculates possible anotations, so for every massspec can be n bins
     * assigned
     *
     * @param List
     * @throws Exception
     */
    protected void anotate(final Collection<Map<String, Object>> unknown,
                           final Collection<Map<String, Object>> library) {

        if (logger.isDebugEnabled()) {
            logger.debug("possible matches: " + unknown.size());
            logger.debug("possible bins: " + library.size());
        }

        // there has to be some way to paralize this process
        logger.info("annotate...");
        for (final Map<String, Object> unk : unknown) {

            for (final Map<String, Object> lib : library) {

                if(logger.isDebugEnabled()) {

                    int binId = Integer.parseInt(lib.get("bin_id").toString());
                    int spectraId = Integer.parseInt(unk.get("spectra_id")
                            .toString());

                    getDiagnosticsService().diagnosticActionSuccess(spectraId,
                            binId, this.getClass(), "compare bin with unknown",
                            "enter comparison step", new Object[]{});
                }
                compare(unk, lib);
            }

        }

        // find the best annotation in this dataset. This is required to avoid
        // double generation of bins
        this.findBestAnotitation(library);
    }

    /**
     * compares an unknown massspec with a library massspec
     *
     * @param unk
     * @param lib
     */
    protected void compare(final Map<String, Object> unk,
                           final Map<String, Object> lib) {

        // use the algorithmm handler todo the actual comparisson
        boolean success = getAlgorythemHandler().compare(lib, unk,
                getHandlerConfiguration());

        // if successful add the references to the objects
        if (success) {
            if (logger.isDebugEnabled()) {

                int binId = Integer.parseInt(lib.get("bin_id").toString());
                int spectraId = Integer.parseInt(unk.get("spectra_id").toString());

                getDiagnosticsService()
                        .diagnosticActionSuccess(
                                spectraId,
                                binId,
                                this.getClass(),
                                "first step of annotation: matching",
                                "possible match found for this spectra, since it passed all filters",
                                new Object[]{});


                logger.debug("("
                        + lib.get("name")
                        + "{"
                        + lib.get("spectra_id")
                        + "}"
                        + " vs unk spectra "
                        + unk.get("spectra_id")
                        + ") - adding to list of annotations and used algoryth handler "
                        + getAlgorythemHandler().getClass().getSimpleName());
            }

            updateMatchingReferences(unk, lib);

        } else {
            if (logger.isDebugEnabled()) {

                int binId = Integer.parseInt(lib.get("bin_id").toString());
                int spectraId = Integer.parseInt(unk.get("spectra_id").toString());

                getDiagnosticsService().diagnosticActionFailed(spectraId, binId,
                        this.getClass(), "first step of annotation: matching",
                        "no possible match found for this spectra to any bins",
                        new Object[]{});

                logger.debug("no possible match was found! - "
                        + getAlgorythemHandler().getClass().getSimpleName());
            }
        }
    }

    /**
     * used to update the references between the bin and the unknown do not
     * remove the synchronized keyboard
     *
     * @param unk
     * @param lib
     */
    @SuppressWarnings("unchecked")
    private synchronized void updateMatchingReferences(
            final Map<String, Object> unk, final Map<String, Object> lib) {
        // put bin in unkown as reference

        if(logger.isDebugEnabled()) {
            int binId = Integer.parseInt(lib.get("bin_id").toString());
            int spectraId = Integer.parseInt(unk.get("spectra_id").toString());

            getDiagnosticsService().diagnosticActionSuccess(spectraId, binId,
                    this.getClass(),
                    "second step of annotation: update references",
                    "bin and spectra relations are sychronized", new Object[]{});
        }
        // logger.info("put bin as a reference to an unknown");
        Collection<Map<String, Object>> n = (Collection<Map<String, Object>>) unk
                .get("BINS");

        if (n == null) {
            n = new ArrayList<Map<String, Object>>();
            unk.put("BINS", n);
        }

        n.add(lib);

        // logger.info("put unknown as a reference to a bin");
        // put unknown as reference to a bin
        Collection<Map<String, Object>> o = (Collection<Map<String, Object>>) lib
                .get("UNKNOWNS");

        if (o == null) {
            o = new ArrayList<Map<String, Object>>();
            lib.put("UNKNOWNS", o);
        }

        o.add(unk);

        // logger.info("generate map of assigned bins");
        // generate map of assigned bins
        Collection<Map<String, Object>> p = binAnotations.get(lib.get("bin_id")
                .toString());

        // logger.info("check if list is alreadt initialized");
        if (p == null) {
            p = new ArrayList<Map<String, Object>>();
            binAnotations.put(lib.get("bin_id").toString(), p);
        }

        // something in this cayses to add an unkwnon
        // which
        // might
        // have a circular dependencies...
        p.add(unk);
    }

    /**
     * finds the best anotation to each bins and massspec
     *
     * @param unknown
     */
    @SuppressWarnings("unchecked")
    protected void findBestAnotitation(Collection<Map<String, Object>> bins) {
        logger.info("find best annotations...");

        Iterator<Map<String, Object>> it = bins.iterator();
        boolean problematic = false;

        // assign unproblematic bins
        while (it.hasNext()) {
            Map<String, Object> bin = (Map<String, Object>) it.next();
            it.remove();

            logger.debug("loading id...");
            Object id = bin.get("bin_id");

            logger.debug("success: " + id);

            logger.debug("loading annotations...");
            Collection<Map<String, Object>> anotations = (Collection<Map<String, Object>>) binAnotations
                    .get(id);
            logger.debug("checking annotations ");

            // anotate all bins without any problems
            if (anotations != null) {
                logger.debug("size: " + anotations.size());

                if (anotations.size() == 1) {

                    Map<String, Object> result = (Map<String, Object>) anotations
                            .iterator().next();
                    Collection<Map<String, Object>> l = (Collection<Map<String, Object>>) result
                            .get("BINS");

                    int binId = Integer.parseInt(bin.get("bin_id").toString());
                    int spectraId = Integer.parseInt(result.get("spectra_id")
                            .toString());

                    /**
                     * some assigment error
                     */
                    if (this.getAssigned().contains(result)) {
                        logger.info("massspec "
                                + result.get("spectra_id")
                                + "already assigned to "
                                + ((Map<String, Object>) result.get("BIN"))
                                .get("name") + "{"
                                + bin.get("spectra_id") + "}");

                        if(logger.isDebugEnabled()) {
                            getDiagnosticsService()
                                    .diagnosticAction(
                                            spectraId,
                                            binId,
                                            this.getClass(),
                                            "third step of annotation: find best hit",
                                            "for some reason this massspec was already assigned",
                                            Result.WARNING, new Object[]{});
                        }
                    }
                    /**
                     * check if this spectra matches to more than one bin
                     */
                    else {
                        /**
                         * only one bin assigned, so its easy
                         */
                        if (l.size() == 1) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("successfull found and accepted bin id is: "
                                        + bin.get("bin_id")
                                        + "{"
                                        + bin.get("spectra_id") + "}");

                                displayMapDetails(bin);

                            getDiagnosticsService()
                                    .diagnosticAction(
                                            spectraId,
                                            binId,
                                            this.getClass(),
                                            "third step of annotation: find best hit",
                                            "there was only one bin assigned to the spectra",
                                            Result.SUCCESS, new Object[]{});
                            }

                            this.acceptMassSpec(bin, result);
                        }
                        /**
                         * more than one bin assigned
                         */
                        else if (l.size() > 1) {
                            problematic = true;

                            if(logger.isDebugEnabled()) {
                                logger.debug("more than one possible anotations for this massspec");

                                getDiagnosticsService()
                                        .diagnosticAction(
                                                spectraId,
                                                binId,
                                                this.getClass(),
                                                "third step of annotation: find best hit",
                                                "there were several bins assigned to the spectra, so it matches several bins",
                                                Result.WARNING, new Object[]{});
                            }
                            handleMassSpecWithSeveralBins(result, l);
                        }
                        /**
                         * nothing assigned, the list is empty so shouldnt
                         * happen
                         */
                        else {
                            if(logger.isDebugEnabled()) {
                                getDiagnosticsService()
                                        .diagnosticAction(
                                                spectraId,
                                                this.getClass(),
                                                new Step(
                                                        "third step of annotation: find best hit"),
                                                new Reason(
                                                        "for some reason the bin collection is empty"),
                                                Result.WARNING, new Object[]{});
                            }
                        }
                    }
                }
                /**
                 * several massspecs assigned to this bin
                 */
                else if (anotations.size() > 1) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("more than one possible anotations for this bin");

                        logger.debug("problematic bin found, try to fix "
                                + bin.get("name") + "{" + bin.get("spectra_id")
                                + "}" + " - count of problems "
                                + anotations.size());
                    }
                    problematic = true;

                    handleBinWithSeveralMassSpec(bin, anotations);
                }

                this.binAnotations.remove(bin.get("bin_id"));
            } else {

                logger.debug("no annotations available for this bin, skip this step");
            }
        }

        if (problematic) {
            logger.info("problematic point of anotation found, run again!");
            anotate(this.getUnknowns(), this.getBins());
        }
    }

    /**
     * iterates over all anotationes to find the best massspec for the given bin
     * ones we find the massspec with the highest hit we accept it and put the
     * other massspecs ion the list of unknown. In the hope they fit to another
     * bin.
     *
     * @param bin
     * @param anotations
     * @throws NumberFormatException
     * @author wohlgemuth
     * @version Oct 24, 2006
     */
    private void handleBinWithSeveralMassSpec(Map<String, Object> bin,
                                              Collection<Map<String, Object>> anotations)
            throws NumberFormatException {

        logger.info("handle bin => n massspecs");

        logger.debug("several massspec are anotated to one bin mode is activated...");

        logger.debug("\nbin details:");

        displayMapDetails(bin);

        Iterator<Map<String, Object>> it = anotations.iterator();

        Map<String, Object> one = it.next();

        int binId = Integer.parseInt(bin.get("bin_id").toString());

        while (it.hasNext()) {
            int spectraId = Integer.parseInt(one.get("spectra_id").toString());

            Map<String, Object> two = (Map<String, Object>) it.next();

            getDiagnosticsService()
                    .diagnosticAction(
                            spectraId,
                            binId,
                            this.getClass(),
                            new Step(
                                    "forth step of annotation: handleBinWithSeveralMassSpec"),
                            new Reason(
                                    "there were several massspecs annotated to this bin"),
                            Result.WARNING,
                            new Object[]{
                                    "spectra id: "
                                            + Integer.parseInt(one.get(
                                            "spectra_id").toString()),
                                    "spectra id: "
                                            + Integer.parseInt(two.get(
                                            "spectra_id").toString())});

            logger.debug("\nspectra details:");

            displayMapDetails(two);

            Map<String, Object> result = compare2MassSpecs(bin, one, two, true);

            if (!result.equals(one)) {
                getDiagnosticsService()
                        .diagnosticAction(
                                spectraId,
                                binId,
                                this.getClass(),
                                new Step(
                                        "forth step of annotation: handleBinWithSeveralMassSpec"),
                                new Reason(
                                        "there were several massspecs annotated to this bin and we are dropping this spectra id, since the 2nd one was a better hit"),
                                Result.WARNING,
                                new Object[]{
                                        "spectra id: "
                                                + Integer.parseInt(one.get(
                                                "spectra_id")
                                                .toString()),
                                        "spectra id: "
                                                + Integer.parseInt(two.get(
                                                "spectra_id")
                                                .toString())});

                // keep two drop the other one
                this.dropMassSpecFromBin(bin, one);
            } else if (!result.equals(two)) {
                getDiagnosticsService()
                        .diagnosticAction(
                                Integer.parseInt(two.get("spectra_id")
                                        .toString()),
                                binId,
                                this.getClass(),
                                new Step(
                                        "forth step of annotation: handleBinWithSeveralMassSpec"),
                                new Reason(
                                        "there were several massspecs annotated to this bin and we are dropping this spectra id, since the first one was a better hit"),
                                Result.WARNING,
                                new Object[]{
                                        "spectra id: "
                                                + Integer.parseInt(one.get(
                                                "spectra_id")
                                                .toString()),
                                        "spectra id: "
                                                + Integer.parseInt(two.get(
                                                "spectra_id")
                                                .toString())});
                // keep two drop the other one
                this.dropMassSpecFromBin(bin, two);
            }

            one = result;
        }

        // accept this massspec and bin
        this.acceptMassSpec(bin, one);
    }

    /**
     * compares the two massspecs to find out which one fits better to the given
     * bin
     *
     * @param bin
     * @param one
     * @param two
     * @return
     * @author wohlgemuth
     * @version Oct 24, 2006
     */
    private Map<String, Object> compare2MassSpecs(Map<String, Object> spectra,
                                                  Map<String, Object> one, Map<String, Object> two, boolean isBin) {

        logger.debug("compare 2 massspecs to find the best annotation");

        if (logger.isDebugEnabled()) {
            StringBuffer content = new StringBuffer();
            if (isBin == true) {
                content.append("(" + spectra.get("name") + "{"
                        + spectra.get("spectra_id") + "}" + " vs ");
                content.append(one.get("spectra_id") + ",");
                content.append(two.get("spectra_id"));
                content.append(") - ");

            } else {
                content.append("(");
                content.append(one.get("name") + "{" + one.get("spectra_id")
                        + "}" + "/");
                content.append(two.get("name"));
                content.append("{" + two.get("spectra_id") + "}");
                content.append(" vs " + spectra.get("spectra_id") + ") - ");

            }

            logger.debug(content);
        }

        try {
            double unk1Sn = Double.parseDouble(one.get("apex_sn").toString());
            double unk2Sn = Double.parseDouble(two.get("apex_sn").toString());

            // if both are larger than the defined sn we take the one with the
            // higher number of apex masses
            if (unk1Sn > largePeakSn && unk2Sn > largePeakSn) {
                logger.debug("both are larger than " + largePeakSn);

                double heightFirst = ValidateSpectra.convert(one.get("spectra")
                        .toString())[Integer.parseInt(spectra.get("quantmass")
                        .toString()) + 1][ValidateSpectra.FRAGMENT_ABS_POSITION];
                double heightSecond = ValidateSpectra.convert(two
                        .get("spectra").toString())[Integer.parseInt(spectra
                        .get("quantmass").toString()) + 1][ValidateSpectra.FRAGMENT_ABS_POSITION];

                if (logger.isDebugEnabled()) {
                    logger.debug("calculated height for one is: " + heightFirst);
                    logger.debug("calculated height for two is: "
                            + heightSecond);
                }

                if (heightFirst > heightSecond) {
                    logger.debug("one is bigger");
                    return one;
                } else if (heightFirst < heightSecond) {
                    logger.debug("two is bigger");
                    return two;
                } else {
                    logger.debug("both have the same height, so use the default similarity based algorythm");
                    return compareSmallMassSpecsToBin(spectra, one, two, isBin);
                }
            }
            // if the first is larger than the second one we accept the first as
            // best
            else if (unk1Sn > largePeakSn && unk2Sn < largePeakSn) {
                logger.debug("one is larger than two and will be accepted");
                return one;
            }
            // if the second is larger than the first one we accept the second
            // one
            // as best
            else if (unk1Sn < largePeakSn && unk2Sn > largePeakSn) {
                logger.debug("two is larger than one and will be accepted");
                return two;
            }
            // if both are small peak we try to analyze them using the
            // ri/similarity
            // offsets
            else {
                logger.debug("both massspecs are smaller than: " + largePeakSn);
                return compareSmallMassSpecsToBin(spectra, one, two, isBin);
            }
        } finally {
        }
    }

    /**
     * specialiced method to compare two massspecs against one bin to find the
     * best one. This supposed to work only for small massspecs, cause big
     * massspecs shift in the ri range and the similarity is not the best
     *
     * @param bin
     * @param one
     * @param two
     * @param isBin
     * @throws NumberFormatException
     * @author wohlgemuth
     * @version Oct 24, 2006
     */
    private Map<String, Object> compareSmallMassSpecsToBin(
            Map<String, Object> spectra, Map<String, Object> one,
            Map<String, Object> two, boolean isBin)
            throws NumberFormatException {

        if (logger.isDebugEnabled()) {
            StringBuffer content = new StringBuffer();
            if (isBin == true) {
                content.append("(" + spectra.get("name") + " vs ");
                content.append(one.get("spectra_id") + ",");
                content.append(two.get("spectra_id"));
                content.append(") - ");
            } else {
                content.append("(");
                content.append(one.get("name") + "/");
                content.append(two.get("name"));
                content.append(" vs " + spectra.get("spectra_id") + ") - ");
            }

            logger.debug(content);
        }

        try {
            double binRi = Double.parseDouble(spectra.get("retention_index")
                    .toString());
            double unk1Ri = Double.parseDouble(one.get("retention_index").toString());
            double unk2Ri = Double.parseDouble(two.get("retention_index").toString());

            double unk1Diff = Math.abs(unk1Ri - binRi);
            double unk2Diff = Math.abs(unk2Ri - binRi);

            double sim1 = 0;
            double sim2 = 0;

            if (isBin) {
                // one and two are spectra compared against a bin
                sim1 = similarity(spectra.get("spectra")
                        .toString(), one.get("spectra").toString());
                sim2 = similarity(spectra.get("spectra")
                        .toString(), two.get("spectra").toString());

                if (logger.isDebugEnabled()) {
                    logger.debug("similarity spectra one = " + sim1 + " vs bin");
                    logger.debug("similarity spectra two = " + sim2 + " vs bin");
                }
            } else {
                // one and two are bins compared against a spectra
                sim1 = similarity(
                        one.get("spectra").toString(), spectra.get("spectra")
                                .toString());
                sim2 = similarity(
                        two.get("spectra").toString(), spectra.get("spectra")
                                .toString());

                if (logger.isDebugEnabled()) {
                    logger.debug("similarity bin one = " + sim1 + " vs spectra");
                    logger.debug("similarity bin two = " + sim2 + " vs spectra");
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("using similarity offset of: " + similarityOffset);
                logger.debug("distance one: " + unk1Diff);
                logger.debug("distance two: " + unk2Diff);
            }

            /**
             * checking if the first one is closer than the second one
             */
            if (unk1Diff < unk2Diff) {
                logger.info("retention index distance of one is smaller than two");

                /**
                 * similarity 2 is bigger
                 */
                if (sim2 > sim1) {
                    logger.debug("similarity of two is larger");

                    if ((sim2 - similarityOffset) > sim1) {
                        logger.debug("two has significant higher similarity so we accept this");
                        return two;
                    } else {
                        logger.debug("similarity of two is not significant higher we accept one because it's closer");
                        return one;
                    }
                } else {
                    logger.debug("similarity of one is higher and its closer so we accept one");
                    return one;
                }
            }
            /**
             * the second one is closer than the first one
             */
            else {
                logger.info("retention index distance of two is smaller than one");

                /**
                 * similarity 1 is bigger
                 */
                if (sim1 > sim2) {
                    logger.debug("similarity of one is larger");

                    if ((sim1 - similarityOffset) > sim2) {
                        logger.debug("one has significant higher similarity so we accept this");
                        return one;
                    } else {
                        logger.debug("similarity of one is not significant higher we accept two because it's closer");
                        return two;
                    }
                } else {
                    logger.debug("similarity of two is higher and its closer so we accept two");
                    return two;
                }
            }
        } finally {
        }
    }

    /**
     * accepts the masspec for this bin
     *
     * @param bin
     * @param one
     * @param two
     * @author wohlgemuth
     * @version Oct 24, 2006
     */
    private void acceptMassSpec(Map<String, Object> bin, Map<String, Object> map) {

        int binId = Integer.parseInt(bin.get("bin_id").toString());
        int spectraId = Integer.parseInt(map.get("spectra_id").toString());

        if (logger.isDebugEnabled()) {
            logger.debug("accept massspec with id " + map.get("spectra_id")
                    + " for bin " + bin.get("name") + "/" + bin.get("bin_id")
                    + " ri/rt for massspec was: " + map.get("retention_index")
                    + "/" + map.get("retention_time"));
        }

        getDiagnosticsService().diagnosticAction(spectraId, binId,
                this.getClass(), "step of annotation: accept massspec",
                "this massspec was accepted by the annotation process",
                Result.SUCCESS, new Object[]{});

        map.put("BIN", bin);
        this.addAssigned(map);
        this.binAnotations.remove(bin.get("bin_id"));

    }

    /**
     * @param result
     * @param l
     * @throws NumberFormatException
     * @author wohlgemuth
     * @version Oct 24, 2006
     */
    private void handleMassSpecWithSeveralBins(Map<String, Object> spectra,
                                               Collection<Map<String, Object>> l) throws NumberFormatException {

        logger.info("handle spec => n bin annotations");

        logger.debug("several bins are anotated to one massspec mode is activated...");

        Iterator<Map<String, Object>> it = l.iterator();

        Map<String, Object> one = it.next();

        /**
         * go over all spectra till nothing is left
         */
        while (it.hasNext()) {
            Map<String, Object> two = it.next();

            Map<String, Object> result = compareSmallMassSpecsToBin(spectra,
                    one, two, false);

            if (!result.equals(one)) {
                // keep two drop the other one
                this.dropBinFromMassSpec(spectra, one);
            } else if (!result.equals(two)) {
                // keep two drop the other one
                this.dropBinFromMassSpec(spectra, two);
            }

            one = result;
        }

        // accept this massspec and bin
        this.acceptMassSpec(one, spectra);
    }

    /**
     * removes the bin from possible anotations for this massspec
     *
     * @param spectra
     * @param two
     * @author wohlgemuth
     * @version Oct 25, 2006
     */
    @SuppressWarnings("unchecked")
    private void dropBinFromMassSpec(Map<String, Object> spectra,
                                     Map<String, Object> binToDrop) {

        logger.debug("discard " + binToDrop.get("name") + "/"
                + binToDrop.get("bin_id") + " from massspec "
                + spectra.get("spectra_id") + " ri/rt for massspec was: "
                + spectra.get("retention_index"));

        int binId = Integer.parseInt(binToDrop.get("bin_id").toString());
        int spectraId = Integer.parseInt(spectra.get("spectra_id").toString());

        getDiagnosticsService().diagnosticAction(spectraId, binId,
                this.getClass(), "step of annotation: drop bin from massspec",
                "the bin was removed from this spectra for some reason",
                Result.SUCCESS, new Object[]{});

        Collection<Map<String, Object>> bins = ((Collection<Map<String, Object>>) binToDrop
                .get("UNKNOWNS"));
        bins.remove(spectra);
        bins = (Collection<Map<String, Object>>) this.binAnotations
                .get(binToDrop.get("bin_id"));
        bins.remove(spectra);

    }

    /**
     * drops a from a bin massspec and moves it back into the unknown lis
     *
     * @param bin
     * @param one
     * @param two
     * @author wohlgemuth
     * @version Oct 24, 2006
     */
    @SuppressWarnings("unchecked")
    private void dropMassSpecFromBin(Map<String, Object> bin,
                                     Map<String, Object> spectra) {

        int binId = Integer.parseInt(bin.get("bin_id").toString());
        int spectraId = Integer.parseInt(spectra.get("spectra_id").toString());

        if (logger.isDebugEnabled()) {
            logger.debug("drop massspec with id " + spectra.get("spectra_id")
                    + " for bin " + bin.get("name") + "/" + bin.get("bin_id")
                    + " ri/rt for massspec was: "
                    + spectra.get("retention_index") + "/"
                    + spectra.get("retention_time"));
        }

        getDiagnosticsService().diagnosticAction(spectraId, binId,
                this.getClass(), "step of annotation: drop masspec from bin",
                "the spectra was removed from this bin for some reason",
                Result.SUCCESS, new Object[]{});

        ((Collection<Map<String, Object>>) spectra.get("BINS")).remove(bin);
        this.unknows.add(spectra);
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.AbstractMatching#loadBins()
     */
    protected Collection<Map<String, Object>> loadBins() throws SQLException {
        this.getLibraryStatement().setInt(1, this.getSampleId());

        return super.loadBins();

    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.AbstractMatching#loadUnknows()
     */
    protected Collection<Map<String, Object>> loadUnknows() throws SQLException {
        this.getUnknownStatement().setInt(1, this.getSampleId());

        return super.loadUnknows();
    }

    /**
     * erstellt daraus die neuen bins
     *
     * @param bin
     * @throws Exception
     */
    protected void newBins(Collection<Map<String, Object>> bin)
            throws Exception {
        logger.debug("try to generate new bins, possible: " + bin.size());

        Iterator<Map<String, Object>> it = bin.iterator();

        while (it.hasNext()) {
            this.getResultHandler().newBin((Map<String, Object>) it.next());
            this.getResultHandler().flush();
        }

    }

    protected void newBin(Map<String, Object> bin) throws Exception {
        this.getResultHandler().newBin((Map<String, Object>) bin);
        this.getResultHandler().flush();
    }
}
