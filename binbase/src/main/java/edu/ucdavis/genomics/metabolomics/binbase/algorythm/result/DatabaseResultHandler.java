/*
 * Created on 04.06.2003
 *
 * To change the template for this generated file go to Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.result;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import edu.ucdavis.genomics.metabolomics.util.math.Similarity;
import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateApexMasses;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateChromatographie;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateSpectra;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateUniqueMass;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.DiagnosticsService;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.DiagnosticsServiceFactory;
import edu.ucdavis.genomics.metabolomics.util.type.converter.BooleanConverter;

/**
 * @author wohlgemuth
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DatabaseResultHandler extends AbstractResultHandler {
    /*
     * Statement to assign a Bin
	 */

    /**
     * DOCUMENT ME!
     */
    private PreparedStatement assignBinStatement;

    /**
     * DOCUMENT ME!
     */
    private PreparedStatement getBinApex;

    /**
     * DOCUMENT ME!
     */
    private PreparedStatement newBinStatementAnalysis;

	/*
     * Statement to insert a new Bin
	 */

    /**
     * DOCUMENT ME!
     */
    private PreparedStatement newBinStatementBin;

    /**
     * DOCUMENT ME!
     */
    private PreparedStatement updateBinApex;

    /**
     * DOCUMENT ME!
     */
    private PreparedStatement updateBinSpecStatement;

    /**
     * DOCUMENT ME!
     */
    private String apex;

    /**
     * DOCUMENT ME!
     */
    private String spectra;

    /**
     * welche bins werden nicht exportiert sondern nur generiert werden
     */
    private double[] dontImport = null;

    /**
     * is it tried to generate bins
     */
    private boolean createdBin = false;

    /**
     * DOCUMENT ME!
     */
    private boolean generateApex = false;

    /**
     * DOCUMENT ME!
     */
    private boolean prepared = false;

    /**
     * DOCUMENT ME!
     */
    private boolean updateBinSpec = true;

    /**
     * DOCUMENT ME!
     */
    private double maximalPurity;

    /**
     * DOCUMENT ME!
     */
    private double minimalSignalNoise;

    /**
     * DOCUMENT ME!
     */
    private double noise;

    /**
     * DOCUMENT ME!
     */
    private double purity;

    /**
     * DOCUMENT ME!
     */
    private double signal_noise;

    /**
     * DOCUMENT ME!
     */
    private double similarity;

    /**
     * DOCUMENT ME!
     */
    protected int bin_id;

    /**
     * DOCUMENT ME!
     */
    protected int id;

    /**
     * DOCUMENT ME!
     */
    private Double ri;

    /**
     * DOCUMENT ME!
     */
    private int sample_id;

    /**
     * DOCUMENT ME!
     */
    private int unique;

    /**
     * Creates a new DatabaseResultHandler object.
     */
    public DatabaseResultHandler() {
        generateApex = Boolean.getBoolean(CONFIG.getValue("bin.update.apex"));
        updateBinSpec = Boolean.getBoolean(CONFIG
                .getValue("bin.update.spectra"));
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isCreatedBin() {
        return createdBin;
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.binlib.algorythm.util.transform.abstracthandler.algorythm.binlib.algorythm.result.ResultHandler#assignBin(Map)
     */
    @SuppressWarnings("unchecked")
    public void assignBin(Map spectra) throws Exception {

        // logger.debug(" assign bin method");
        this.getValues(spectra);

        this.assignBinStatement.setInt(1, this.bin_id);
        this.assignBinStatement.setInt(4, this.id);
        this.assignBinStatement.setDouble(2, this.similarity);

        Object ric = spectra.get("RIC");

        if (ric == null) {
            this.assignBinStatement.setString(3,
                    BooleanConverter.booleanToString(false));
        } else {
            this.assignBinStatement.setString(3,
                    BooleanConverter.booleanToString(true));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("--------------------------------------------------");
            logger.debug(" assign to bin");
            logger.debug(" bin id: \t" + bin_id);
            logger.debug(" spectra id: \t" + this.id);
            logger.debug(" purity: \t" + purity);
            logger.debug(" signal noise: \t" + noise);
            logger.debug(" similarity: \t" + similarity);
            logger.debug(" signal ri: \t" + ri);
            logger.debug(" sample id: \t" + sample_id);
            logger.debug("--------------------------------------------------");
        }
        this.assignBinStatement.execute();

        getDiagnosticsService().diagnosticActionSuccess(id, bin_id,
                this.getClass(), "assign to bin",
                "all settings were accpeted and it's a succesful annotation",
                new Object[]{});

        if (generateApex) {
            this.getBinApex.setInt(1, this.bin_id);
            this.updateBinApex.setInt(2, this.bin_id);

            ResultSet result = this.getBinApex.executeQuery();

            if (result.next()) {
                String binapex = result.getString(1);
                String binSpectra = result.getString(2);
                apex = ValidateApexMasses.cleanApex(
                        ValidateApexMasses.convert(apex),
                        ValidateSpectra.add(this.spectra, binSpectra),
                        this.unique);

                String newApex = ValidateApexMasses.add(apex, binapex);

                this.updateBinApex.setString(1, newApex);
                this.updateBinApex.executeUpdate();
                result.close();
            } else {
                result.close();
                throw new RuntimeException(
                        "coud not find bin id, don't update apex masses");
            }
        }

        if (updateBinSpec) {
            Map bin = (Map) spectra.get("BIN");

            double signal_noise = Double.parseDouble((String) bin
                    .get("apex_sn"));
            double purity = Double.parseDouble((String) bin.get("purity"));

            if (this.signal_noise > signal_noise) {
                if (this.purity < purity) {
                    logger.info("update bin massspec");
                    this.updateBinSpecStatement.setString(1, this.spectra);
                    this.updateBinSpecStatement.setDouble(2, this.noise);
                    this.updateBinSpecStatement.setDouble(3, this.signal_noise);
                    this.updateBinSpecStatement.setInt(4, this.bin_id);
                    this.updateBinSpecStatement.execute();

                    getDiagnosticsService()
                            .diagnosticActionSuccess(
                                    id,
                                    bin_id,
                                    this.getClass(),
                                    "update bin massspec",
                                    "the spectras massspec was cleaner than the bin massspec and is defined as the new bin massspec",
                                    new Object[]{this.signal_noise,
                                            this.purity, signal_noise, purity});
                } else {
                    getDiagnosticsService()
                            .diagnosticActionFailed(
                                    id,
                                    bin_id,
                                    this.getClass(),
                                    "update bin massspec",
                                    "the spectras massspecs purity was too high and so the masspec is not updated",
                                    new Object[]{this.purity, purity});
                }
            } else {
                getDiagnosticsService()
                        .diagnosticActionFailed(
                                id,
                                bin_id,
                                this.getClass(),
                                "update bin massspec",
                                "the spectras signal noise was to small and so the masspec is not updated",
                                new Object[]{this.purity, purity});
            }
        } else {
            getDiagnosticsService().diagnosticActionFailed(id, bin_id,
                    this.getClass(), "update bin massspec",
                    "updating of binbase massspecs is explicitely disabled",
                    new Object[]{});
        }
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.binlib.algorythm.util.transform.abstracthandler.algorythm.binlib.algorythm.result.ResultHandler#discardBin(Map)
     */
    @SuppressWarnings("unchecked")
    public void discardBin(Map spectra) throws Exception {

        int spectraId = Integer.parseInt(spectra.get("spectra_id").toString());

        this.getValues(spectra);

        if (this.isNewBinAllowed() == false) {

            getDiagnosticsService().diagnosticAction(spectraId,
                    this.getClass(), "discard new bin",
                    "new bins are not allowed", "bin was discarded",
                    new Object[]{});
            // logger.info(" discard new bin, new bins are not allowed");
        } else {
            getDiagnosticsService().diagnosticAction(spectraId,
                    this.getClass(), "discard new bin",
                    "massspec was discared as possible bin",
                    "bin was discarded", new Object[]{});
        }
        spectra.remove("bin_id");
    }

    public synchronized void newBin(Map spectra) throws Exception {
        this.newBin(spectra, false);
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.binlib.algorythm.util.transform.abstracthandler.algorythm.binlib.algorythm.result.ResultHandler#newBin(Map)
     */
    @SuppressWarnings("unchecked")
    public synchronized void newBin(Map spectra, boolean force) throws Exception {
        String export = "TRUE";

        createdBin = true;

        if (!force) {
            if (this.isNewBinAllowed() == false) {
                logger.debug("no bins are allowed!");

                getDiagnosticsService().diagnosticActionFailed(id, this.getClass(),
                        "create new bin", "new bins are explicitely disabled",
                        new Object[]{});

                this.discardBin(spectra);

                return;
            }
        }
        this.getValues(spectra);

		/*
         * ?berpr?fung ob bestimmte bin settings erf?llt sind, k?nnte man
		 * theroretisch in eine eigene klasse auslagern
		 */
        try {

            if (!force) {
                if (ValidateChromatographie.isValidFetchedSpectra(spectra) == false) {
                    getDiagnosticsService()
                            .diagnosticActionFailed(
                                    id,
                                    this.getClass(),
                                    "create new bin",
                                    "not possible, since there was an eror with the chrommatographie for this spectra",
                                    new Object[]{});
                    this.discardBin(spectra);
                }

			/*
             * ?berpr?fung ob im processing ein deconvolution error festgestellt
			 * wurde
			 */
                if (new Boolean((String) spectra.get("decoerror")).booleanValue() == true) {
                    // logger.info(" possible deconvolution error, discard this bin");

                    getDiagnosticsService().diagnosticActionFailed(id,
                            this.getClass(), "create new bin",
                            "not possible, since there was a deconvolution error",
                            new Object[]{});
                    this.discardBin(spectra);

                    return;
                }

			/*
			 * ?berpr?fung ob das unique ion gross genug ist und nicht zu klein
			 * geraten ist
			 */
                if (ValidateUniqueMass.isValidUnique(this.spectra, this.unique) == false) {
                    // logger.info(
                    // " possibly deconvolution error at validate unique detected, discard bin");

                    getDiagnosticsService()
                            .diagnosticActionFailed(
                                    id,
                                    this.getClass(),
                                    "create new bin",
                                    "not possible, unique mass was declared as not valid in the massspecs spectra",
                                    new Object[]{this.unique});
                    this.discardBin(spectra);

                    return;
                }


                for (int i = 0; i < this.dontImport.length; i++) {
                    if (Math.abs(unique - this.dontImport[i]) < 0.0001) {
                        export = "FALSE";
                    }
                }

			/*
			 * wenn die apexmassen die unique mass nicht enthalten, handelt es
			 * sich h?chstwahrscheinlich um ein fehlerhaftes spektrum
			 */
                if (ValidateApexMasses.contains(apex, this.unique) == false) {
                    logger.info(" spectra apex masses does no include bin apex masses");

                    getDiagnosticsService()
                            .diagnosticActionFailed(
                                    id,
                                    this.getClass(),
                                    "create new bin",
                                    "not possible, since the spectras unique mass, was not in the list of the bins apexing masses",
                                    new Object[]{this.unique});
                    this.discardBin(spectra);

                    return;
                }


			/*
			 * wenn das signal noise zu klein ist handelt es sich
			 * h?chstwahrscheinlich um noise/ unsauberer peak und er wird
			 * verworfen
			 */
                if (this.noise < this.minimalSignalNoise) {
                    // logger.debug(" signal noise to small");

                    getDiagnosticsService()
                            .diagnosticActionFailed(
                                    id,
                                    this.getClass(),
                                    "create new bin",
                                    "not possible, since the signal noise of the spectra is smaller than the minimal allowed signal noise for a new bin",
                                    new Object[]{this.noise,
                                            this.minimalSignalNoise});
                    this.discardBin(spectra);

                    return;
                }


			/*
			 * wenn die purity zu gross ist, handelt es sich um biologischen
			 * d?nnschiss und das spectrum wird verworfen
			 */

                if (this.purity > this.maximalPurity) {
                    // logger.debug(" purity is to big");
                    getDiagnosticsService()
                            .diagnosticActionFailed(
                                    id,
                                    this.getClass(),
                                    "create new bin",
                                    "not possible, since the purity of the spectra is larger than the max allowed purity for a new bin",
                                    new Object[]{this.purity, this.maximalPurity});
                    this.discardBin(spectra);

                    return;
                }

                /**
                 * calculate similarity against existing bins with the same unique mass
                 */

                //get all bins with same unique mass in +/-2000 units from the db
                //calculate similarity
                //only continue when no similarity over 900 exist
                PreparedStatement suroundingBins = this.getConnection().prepareStatement("SELECT spectra FROM bin WHERE retention_index BETWEEN ? AND  ? AND uniquemass = ?");
                suroundingBins.setDouble(1, this.ri - 2000);
                suroundingBins.setDouble(2, this.ri + 2000);
                suroundingBins.setInt(3, this.unique);

                ResultSet resultSet = suroundingBins.executeQuery();

                logger.info("evaluating if we are having similar bins to this bin");
                //if we have hits, continue
                try {
                    while (resultSet.next()) {
                        String spectraToMatch = resultSet.getString(1);

                        Similarity sim = new Similarity();
                        sim.setLibrarySpectra(spectraToMatch);
                        sim.setUnknownSpectra(this.spectra);

                        double similarityResult = sim.calculateSimimlarity();

                        logger.info("bin has similarity to similar bin of " + similarityResult);

                        if (similarityResult > 850) {
                            logger.info("discarding bin, since similarity is to high to close bin");
                            this.discardBin(spectra);
                            return;
                        }
                    }
                }
                finally {
                    resultSet.close();
                    suroundingBins.close();
                }

            }
        }
		/*
		 * falls eine bis jetzt nicht bekannte exception auftritt wird das
		 * spektrum verworfen
		 */ catch (Exception e) {
            getDiagnosticsService().diagnosticActionFailed(id, bin_id,
                    this.getClass(), "create new bin",
                    "not possible, since there was an exception",
                    new Object[]{e});
            logger.error(" discard this bin after exception", e);
            discardBin(spectra);

            return;
        }

        try {
            spectra.remove("bin_id");

            logger.debug("clean apexmasses");
            apex = ValidateApexMasses
                    .cleanApex(apex, this.spectra, this.unique);

            // adding the isotope mass for the unique ion to the apex masses
            apex = apex + "+" + (this.unique + 1);

            // logger.debug(" save to database");
			/*
			 * eintragen des ergebnisses in die datenbank
			 */
            this.newBinStatementBin.setInt(1, this.id);
            this.newBinStatementBin.setInt(2, this.unique);
            this.newBinStatementBin.setInt(3, this.ri.intValue());
            this.newBinStatementBin.setDouble(4, this.noise);
            this.newBinStatementBin.setDouble(5, this.purity);
            this.newBinStatementBin.setInt(6, this.sample_id);
            this.newBinStatementBin.setString(7, this.apex);
            this.newBinStatementBin.setString(8, this.spectra);
            this.newBinStatementBin.setInt(9, this.unique);

            int binId = this.nextBinId();

            if (logger.isDebugEnabled()) {
                logger.debug("--------------------------------------------------");
                logger.debug(" found new bin");
                logger.debug(" bin id: \t" + binId);
                logger.debug(" spectra id: \t" + this.id);
                logger.debug(" purity: \t" + purity);
                logger.debug(" signal noise: \t" + noise);
                logger.debug(" signal ri: \t" + ri);
                logger.debug(" sample id: \t" + sample_id);

                logger.debug("--------------------------------------------------");
            }

            this.newBinStatementBin.setInt(10, binId);
            this.newBinStatementBin.setString(11, String.valueOf(binId));
            this.newBinStatementBin.setDouble(12, this.signal_noise);
            this.newBinStatementBin.setString(13, export);

            this.newBinStatementBin.execute();
            this.newBinStatementAnalysis.setInt(1, binId);
            this.newBinStatementAnalysis.setInt(2, this.id);
            this.newBinStatementAnalysis.execute();

            getDiagnosticsService().diagnosticActionSuccess(id, bin_id,
                    this.getClass(), "create new bin", "created a new bin",
                    new Object[]{});

        } catch (SQLException e) {
            getDiagnosticsService()
                    .diagnosticActionFailed(
                            id,
                            this.getClass(),
                            "create new bin",
                            "not possible, since there was a fatal exception during the actual insertion process",
                            new Object[]{e});

            logger.error(e.getMessage(), e);
            throw e;
        }
        return;
    }

    /**
     * bereitet die statements vor
     *
     * @see edu.ucdavis.genomics.metabolomics.binbase.binlib.algorythm.util.transform.abstracthandler.algorythm.binlib.algorythm.result.AbstractResultHandler#prepareStatements()
     */
    protected void prepareStatements() throws Exception {
        if (this.prepared == false) {
            assignBinStatement = this.getConnection().prepareStatement(
                    SQL_CONFIG.getValue(CLASS + ".assignBinStatement"));

            newBinStatementBin = this.getConnection().prepareStatement(
                    SQL_CONFIG.getValue(CLASS + ".newBinStatementBin"));

            newBinStatementAnalysis = this.getConnection().prepareStatement(
                    SQL_CONFIG.getValue(CLASS + ".newBinStatementAnalysis"));
            assignBinStatement = this.getConnection().prepareStatement(
                    SQL_CONFIG.getValue(CLASS + ".assignBinStatement"));

            this.getBinApex = this.getConnection().prepareStatement(
                    SQL_CONFIG.getValue(CLASS + ".getBinApex"));
            this.updateBinApex = this.getConnection().prepareStatement(
                    SQL_CONFIG.getValue(CLASS + ".updateBinApex"));
            this.updateBinSpecStatement = this.getConnection()
                    .prepareStatement(
                            SQL_CONFIG.getValue(CLASS + ".updateBinSpec"));

            this.prepared = true;
        }
    }

    /**
     * bereitet die variabeln vor
     */
    @SuppressWarnings("unchecked")
    protected void prepareVariables() {
        try {

            Element ne = CONFIG.getElement("bin.fragment");

            if (ne.getChildren().isEmpty() == false) {
                List list = ne.getChildren("unique");
                this.dontImport = new double[list.size()];

                for (int i = 0; i < this.dontImport.length; i++) {
                    String text = ((Element) list.get(i)).getText();

                    try {
                        if (text != null) {
                            this.dontImport[i] = Double.parseDouble(text);
                        }
                    } catch (Exception e) {
                        logger.warn(e.getMessage());
                        logger.debug(e.getMessage(), e);
                    }
                }
            } else {
                this.dontImport = new double[0];
            }
        } catch (Exception e) {
            this.sendException(e);
        }
    }

    /**
     * liest die daten aus der map aus und testet sie
     *
     * @param spectra
     */
    @SuppressWarnings("unchecked")
    protected void getValues(Map spectra) {
        id = Integer.parseInt((String) spectra.get("spectra_id"));

        sample_id = Integer.parseInt((String) spectra.get("sample_id"));
        ri = Double.parseDouble((String) spectra.get("retention_index"));
        apex = (String) spectra.get("apex");
        this.unique = (int) Double.parseDouble((String) spectra
                .get("uniquemass"));
        this.spectra = (String) spectra.get("spectra");

        // signal noise = apexsn
        this.signal_noise = Double.parseDouble((String) spectra.get("apex_sn"));
        purity = Double.parseDouble((String) spectra.get("purity"));
        noise = Double.parseDouble((String) spectra.get("apex_sn"));

        try {
            bin_id = Integer.parseInt((String) ((Map) spectra.get("BIN"))
                    .get("bin_id"));
        } catch (Exception e) {
            // wenn ein assign ausgef?hrt wir dkann es ja keine bin id geben
        }
        try {
            similarity = Double.parseDouble((String) spectra.get("similarity"));
        } catch (Exception e) {
            // wenn ein assign ausgef?hrt wir dkann es ja keine bin id geben
        }

    }

    /**
     * generiert die n?chste sampleid
     *
     * @throws Exception
     */
    private int nextBinId() throws Exception {
        Statement state = this.getConnection().createStatement();

		/*
		 * ermitteln der sample id
		 */
        ResultSet result = state.executeQuery(SQL_CONFIG
                .getValue("static.nextBinId"));
        result.next();

        int id = result.getInt(1);

        result.close();
        state.close();

        if (isValidBinId(id) == false) {
            return nextBinId();
        }
        return id;
    }

    /**
     * validates that the generated bin id is not in use yet
     *
     * @param id
     * @return
     * @throws SQLException
     */
    private boolean isValidBinId(int id) throws SQLException {
        PreparedStatement state = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue("static.binIdExist"));
        state.setInt(1, id);
        // get the next sample id
        ResultSet result = state.executeQuery();

        boolean exist = result.next();
        state.close();

        return !exist;
    }

    /**
     * we define the setting for the bin generation here
     */
    public void setConfiguration(Element configuration) {
        this.minimalSignalNoise = Double.parseDouble(configuration
                .getAttributeValue("minimalSignalNoiseBinGeneration"));
        this.maximalPurity = Double.parseDouble(configuration
                .getAttributeValue("maximalPurityBinGeneration"));
    }

    @Override
    public DiagnosticsService getDiagnosticsService() {
        return service;
    }

    private DiagnosticsService service = DiagnosticsServiceFactory
            .newInstance().createService();
}
