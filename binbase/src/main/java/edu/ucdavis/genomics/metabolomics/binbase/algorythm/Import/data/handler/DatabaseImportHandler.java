/*
 * Created on Aug 26, 2003
 *
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.handler;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.event.SpectraEvent;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.event.SpectraEventListener;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateChromatographie;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.Diagnostics;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.DiagnosticsService;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.DiagnosticsServiceFactory;
import edu.ucdavis.genomics.metabolomics.util.SQLObject;
import edu.ucdavis.genomics.metabolomics.util.thread.locking.Lockable;
import edu.ucdavis.genomics.metabolomics.util.thread.locking.LockableFactory;
import edu.ucdavis.genomics.metabolomics.util.thread.locking.SimpleLockingFactory;

/**
 * @author wohlgemuth
 * @version Aug 26, 2003 <br>
 *          BinBaseDatabase
 * @description
 */
public class DatabaseImportHandler extends SQLObject implements ImportHandler,
        Diagnostics {


    private Collection<SpectraEventListener> spectraEventListeners = new Vector<SpectraEventListener>();

    private static final String UNIQUE_CONFIGURATION_ID = "unique-configuration-id";

    /**
     * DOCUMENT ME!
     */
    private PreparedStatement statementGetConfiguration;

    /**
     * calculates the version of the sample
     */
    private PreparedStatement statementGetVersion;

    /**
     * statement for import querys -> data
     */
    private PreparedStatement statementImportData;

    /**
     * statement for import querys -> samples
     */
    private PreparedStatement statementImportSamples;

    /**
     * statement for import querys -> samples
     */
    private PreparedStatement statementImportSamples2;

    /**
     * statement for import querys -> samples
     */
    private PreparedStatement statementImportSamples3;

    /**
     * DOCUMENT ME!
     */
    private PreparedStatement statementInsertConfiguration;

    private Lockable lock = null;

    /**
     * @version Aug 26, 2003
     * @author wohlgemuth <br>
     * @see edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.ImportHandler#importSampleMap(Map)
     */
    public void importSampleMap(Map data) throws Exception {
        try {

            lock = LockableFactory.newInstance(
                    SimpleLockingFactory.class.getName()).create(
                    getClass().getName());

            logger.info("locking ressouce so that we can save a new configuration object...");

            lock.aquireRessource(UNIQUE_CONFIGURATION_ID, 100000000);

            logger.debug("import sample " + data.get("sample_id"));

            int uniuqeConfId = CONFIG.getUnigueID();

            try {

                this.statementGetConfiguration.setInt(1, uniuqeConfId);

                ResultSet config = this.statementGetConfiguration
                        .executeQuery();

                if (!config.next()) {
                    this.statementInsertConfiguration.setInt(1, uniuqeConfId);
                    this.statementInsertConfiguration.setString(2,
                            CONFIG.toString());
                    this.statementInsertConfiguration.execute();
                }
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
            lock.releaseRessource(UNIQUE_CONFIGURATION_ID);

            int id = Integer.parseInt((String) data.get("sample_id"));
            String file = (String) data.get("file");
            String newBin = (String) data.get("allowNewBin");
            String saturated = (String) data.get("saturated");
            String clazz = (String) data.get("class");
            String setupXId = (String) data.get("setupX");

            Date date = (Date) data.get("date");
            int sod = (Integer) data.get("sod");
            int type = (Integer) data.get("type");
            int version = 0;
            int runId = (Integer) data.get("rid");
            String machine = data.get("mach").toString();
            String operator = data.get("operator").toString();

            this.statementGetVersion.setString(1, file);

            ResultSet result = this.statementGetVersion.executeQuery();

            while (result.next()) {
                version = result.getInt(1);
            }

            version = version + 1;

            this.statementImportSamples2.setInt(1, id);
            this.statementImportSamples.setInt(1, id);
            this.statementImportSamples.setString(2, file);
            this.statementImportSamples.setString(3, clazz);
            this.statementImportSamples.setString(4, newBin.toUpperCase());
            this.statementImportSamples.setString(5, saturated.toUpperCase());
            this.statementImportSamples.setInt(6, version);
            this.statementImportSamples.setInt(7, uniuqeConfId);
            this.statementImportSamples.setInt(8, 0);
            this.statementImportSamples.setInt(9, type);
            this.statementImportSamples.setDate(10, date);
            this.statementImportSamples.setInt(11, sod);
            this.statementImportSamples.setString(12, setupXId);
            this.statementImportSamples.setString(13, operator);
            this.statementImportSamples.setString(14, machine);
            this.statementImportSamples.setInt(15, runId);
            this.statementImportSamples.setTimestamp(16, new Timestamp(
                    new java.util.Date().getTime()));

            logger.info("id: " + id);

            if (this.supportBatchMode()) {
                this.statementImportSamples.addBatch();
                this.statementImportSamples2.addBatch();
            } else {
                this.statementImportSamples.execute();
                this.statementImportSamples2.execute();
            }

            this.statementImportSamples3.setString(1, file);
            this.statementImportSamples3.setInt(2, id);

            if (this.supportBatchMode()) {
                this.statementImportSamples3.addBatch();
            } else {
                this.statementImportSamples3.execute();
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * @version Aug 26, 2003
     * @author wohlgemuth <br>
     */
    @SuppressWarnings("unchecked")
    public void importSpectraMap(Map data) throws Exception {
        int spectraId = 0;

        try {
            int id = Integer.parseInt((String) data.get("sample_id"));

            spectraId = nextSpectraId();

            this.statementImportData.setInt(1,
                    Integer.parseInt((String) data.get("UniqueMass")));
            this.statementImportData.setDouble(2,
                    Double.parseDouble((String) data.get("S/N")));
            this.statementImportData.setDouble(3,
                    Double.parseDouble((String) data.get("Purity")));
            this.statementImportData.setInt(4, (int) Math.round(Double.parseDouble(((String) data.get("Retention Index")).replace(',', '.'))));
            this.statementImportData.setInt(5, id);
            this.statementImportData.setDouble(
                    6,
                    Double.parseDouble(((String) data.get("Retention Index")).replace(
                            ',', '.')));
            this.statementImportData.setString(7,
                    (String) data.get("Quant Masses"));
            this.statementImportData.setString(8, (String) data.get("Spectra"));
            this.statementImportData.setDouble(9,
                    Double.parseDouble((String) data.get("Quant S/N")));
            this.statementImportData.setString(10,
                    (String) data.get("PEGASUS_VERSION"));
            this.statementImportData.setInt(11, spectraId);

            if (this.supportBatchMode()) {
                this.statementImportData.addBatch();
            } else {
                this.statementImportData.execute();
            }
            getDiagnosticsService().diagnosticActionSuccess(spectraId,
                    this.getClass(), "import spectra",
                    "spectra was successfully imported", new Object[]{});

            for(SpectraEventListener listener : spectraEventListeners){
                listener.fireEvent(SpectraEvent.IMPORTED,spectraId);
            }

            if (ValidateChromatographie.isValidSpectra(data)) {
                getDiagnosticsService().diagnosticActionSuccess(spectraId,
                        this.getClass(), "validate spectra",
                        "spectra validation was successful", new Object[]{});

                for(SpectraEventListener listener : spectraEventListeners){
                    listener.fireEvent(SpectraEvent.VALIDATION_SUCCESS,spectraId);
                }

            } else {
                getDiagnosticsService().diagnosticActionFailed(spectraId,
                        this.getClass(), "validate spectra",
                        "spectra validation failed for some reason",
                        new Object[]{});

                for(SpectraEventListener listener : spectraEventListeners){
                    listener.fireEvent(SpectraEvent.VALIDATION_FAILED,spectraId);
                }

            }
        } catch (NumberFormatException e) {
            logger.debug(" number format exception, ignore masspec at ri = "
                    + data.get("	Retention Index"), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            getDiagnosticsService().diagnosticActionFailed(spectraId,
                    this.getClass(), "import spectra",
                    "there was an exception", new Object[]{e});

            throw e;
        }
    }

    /**
     * fetcheds a spectra id from the database
     *
     * @return
     * @throws SQLException
     */
    private synchronized int generateSpectraId() throws SQLException {
        Statement state = this.getConnection().createStatement();

        // get the next sample id
        ResultSet result = state.executeQuery(SQL_CONFIG
                .getValue("static.nextSpectraId"));
        result.next();

        int id = result.getInt(1);
        state.close();

        logger.trace("created spectra id: " + id);
        return id;
    }

    /**
     * returns a validated unique spectra id
     *
     * @return
     * @throws SQLException
     */
    private synchronized int nextSpectraId() throws SQLException {

        int id = generateSpectraId();

        while (!isValidSpectraId(id)) {
            logger.info("spectra id is already in use, creating a new one: "
                    + id);
            id = generateSpectraId();
        }

        logger.trace("validated spectra id: " + id);
        return id;
    }

    /**
     * checks if this id is already taken
     *
     * @param id
     * @return
     * @throws SQLException
     */
    private boolean isValidSpectraId(int id) throws SQLException {
        PreparedStatement state = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue("static.spectraIdExist"));
        state.setInt(1, id);
        // get the next sample id
        ResultSet result = state.executeQuery();

        boolean exist = result.next();
        state.close();

        return !exist;
    }

    /**
     * @version Aug 26, 2003
     * @author wohlgemuth <br>
     * @see edu.ucdavis.genomics.metabolomics.util.SQLObject#prepareStatements()
     */
    protected void prepareStatements() throws Exception {
        super.prepareStatements();

        this.statementImportSamples = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue(CLASS + ".sample"));
        this.statementImportSamples2 = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue(CLASS + ".sample2"));
        this.statementImportSamples3 = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue(CLASS + ".sample3"));

        this.statementImportData = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue(CLASS + ".entry"));
        this.statementGetVersion = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue(CLASS + ".version"));
        this.statementGetConfiguration = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue(CLASS + ".configuration"));
        this.statementInsertConfiguration = this.getConnection()
                .prepareStatement(
                        SQL_CONFIG.getValue(CLASS + ".insertConfiguration"));


        for (SpectraEventListener listener : spectraEventListeners) {
            listener.setConnection(this.getConnection());
        }

    }

    @Override
    public void fireBatchInsert() throws Exception {
        if (supportBatchMode()) {
            this.statementImportSamples.executeBatch();
            this.statementImportSamples2.executeBatch();
            this.statementImportSamples3.executeBatch();
            this.statementImportData.executeBatch();
        }
    }

    @Override
    public boolean supportBatchMode() {
        return true;
    }

    @Override
    public DiagnosticsService getDiagnosticsService() {
        return service;
    }

    private DiagnosticsService service = DiagnosticsServiceFactory
            .newInstance().createService();
}
