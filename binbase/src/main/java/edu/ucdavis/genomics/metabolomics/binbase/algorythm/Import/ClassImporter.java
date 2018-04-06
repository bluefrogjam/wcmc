/*
 * Created on Nov 15, 2005
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.filter.MassSpecFilter;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.methods.GitterMethode;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.methods.Methodable;
import edu.ucdavis.genomics.metabolomics.binbase.bci.server.BinBaseService;
import edu.ucdavis.genomics.metabolomics.binbase.bci.setupX.SetupXProvider;
import edu.ucdavis.genomics.metabolomics.binbase.cluster.exception.ClusterException;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.Diagnostics;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.DiagnosticsService;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.DiagnosticsServiceFactory;
import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.exception.LockingException;
import edu.ucdavis.genomics.metabolomics.exception.ValidationException;
import edu.ucdavis.genomics.metabolomics.util.SQLObject;
import edu.ucdavis.genomics.metabolomics.util.config.Configable;
import edu.ucdavis.genomics.metabolomics.util.type.converter.BooleanConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

/**
 * is a basic class used to import a class and match all the samples belonging
 * to this class. we also generate bins from this and maybe delete them later.
 * <p/>
 * this importer does'nt provide any parallism, but is prepared using
 * synchronized blocks. The implementation also must provide the actual
 * functionality
 *
 * @author wohlgemuth
 * @version Nov 15, 2005
 */
public abstract class ClassImporter {

    /**
     * is used to get the generated bins by this class
     */
    private PreparedStatement bins;

    /**
     * used to delete bins from the bin table
     */
    private PreparedStatement deleteFromBin;

    /**
     * used to delete bins from the spectra table
     */
    private PreparedStatement deleteFromSpectra;

    private PreparedStatement allowNewBin;

    private DeleteSample deleteSample;

    private boolean newBinsAllowed = true;

    private MassSpecFilter massSpecFilter;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public MassSpecFilter getMassSpecFilter() {
        return massSpecFilter;
    }

    public void setMassSpecFilter(MassSpecFilter massSpecFilter) {
        this.massSpecFilter = massSpecFilter;
    }

    /**
     * imports the class to the database and matches them
     *
     * @param experiment
     * @throws ValidationException
     * @throws ConfigurationException
     * @throws Exception
     * @author wohlgemuth
     * @version Nov 15, 2005
     */
    public final void importData(ExperimentClass experiment)
            throws ConfigurationException, ValidationException, Exception {
        // save the current samples as variable

        this.currentClass = new BinBaseExperimentClass(experiment);

        logger.info("store physical data in table if not done yet");
        storeImportData(experiment);

        // validate the the experiment is complete at the rawdata table
        logger.info("validate class");
        validateSources((BinBaseExperimentImportSample[]) this.currentClass
                .getSamples());

        logger.info("import samples and calculate ids");
        // imports all the samples and matches them
        int[] sampleIds = importSample((BinBaseExperimentImportSample[]) this
                .getCurrentClass().getSamples(), experiment.getId());

        ExperimentSample[] samp = this.getCurrentClass().getSamples();

        for (int i = 0; i < sampleIds.length; i++) {
            ((BinBaseExperimentImportSample) samp[i]).setSampleId(sampleIds[i]);
        }

        logger.info("correct samples");
        // correction correct the times of the compounds
        correctSamples((BinBaseExperimentImportSample[]) this.getCurrentClass()
                .getSamples());

        logger.info("match samples");
        // matches the samples against the database
        matchSamples((BinBaseExperimentImportSample[]) this.getCurrentClass()
                .getSamples());

        logger.info("bin generation");
        // check if we can generate bins at all
        if (this.checkIfBinsAreAllowed()) {

            logger.debug("create instance of lockable factory...");
            // prepared for multi threading and clustering

            try {
                obtainBinGenerationLock(experiment.getColumn(), this
                        .getCurrentClass().getId());

                logger.debug("got lock");
                try {
                    for (int i = 0; i < sampleIds.length; i++) {
                        // create new bins for this samples and match all other
                        // again
                        if (this.canCreateBin((BinBaseExperimentImportSample) this
                                .getCurrentClass().getSamples()[i])) {
                            createBins(sampleIds[i]);
                        } else {
                            logger.info("current sample is not allowed to generate bins! - "
                                    + this.getCurrentClass().getSamples()[i]);
                        }
                    }

                    // check if new bins where generated
                    this.bins.setString(1, experiment.getId());
                    ResultSet result = this.bins.executeQuery();

                    List<Integer> ids = new Vector<Integer>();

                    // get all bin id's
                    while (result.next()) {
                        ids.add(new Integer(result.getInt(1)));
                    }

                    if (!ids.isEmpty()) {
                        // postmatching needed so that we can make a statistic
                        // afterwards
                        logger.info("postmatching samples");
                        postmatchSamples((BinBaseExperimentImportSample[]) this
                                .getCurrentClass().getSamples());
                        // convert collection to array
                        int binIds[] = new int[ids.size()];

                        for (int i = 0; i < binIds.length; i++) {
                            binIds[i] = ((Integer) ids.get(i)).intValue();
                        }

                        logger.info("postmatching is done");

                        logger.info("deleting wrongly generated bins");

                        // remove wrong bins from this class
                        deleteWrongBins(experiment.getId(), binIds,
                                Double.parseDouble(Configable.CONFIG
                                        .getValue("import.generation.factor")));
                    } else {
                        logger.info("no new bins where generated in this class: "
                                + experiment.getId());
                    }

                } catch (LockingException e) {
                    logger.warn("failed to obtain a lock, no bins generated!");
                } finally {
                    logger.info("releasaing lock");
                    releaseLock(experiment.getColumn(), this.getCurrentClass()
                            .getId());
                }

            }
            catch (LockingException e){
                logger.warn("failed to obtain a lock, no bins generated!");
            }
            catch (Exception e) {
                logger.info("something failed, we are rolling the transaction back: " + e.getMessage(),e);
                rollback((BinBaseExperimentImportSample[]) this
                        .getCurrentClass().getSamples());
                throw e;
            }

        } else {
            logger.info("not possible to generate bins from this class!");
        }

        logger.info("finish class");
        for (ExperimentSample sample : this.getCurrentClass().getSamples()) {
            logger.info("finish sample: " + sample);
            finishSample((BinBaseExperimentImportSample) sample);

        }

        logger.info("class is finished");
    }

    /**
     * does the postmatching
     *
     * @param samples
     * @throws Exception
     */
    protected abstract void postmatchSamples(
            BinBaseExperimentImportSample[] samples) throws Exception;

    /**
     * releases the locking for the given column
     *
     * @param column
     * @throws LockingException
     */
    private void releaseLock(String column, String className)
            throws LockingException {
        edu.ucdavis.genomics.metabolomics.binbase.util.locking.BinGenerationLock
                .getInstance().releaseLock(column, className);
    }

    /**
     * obtains a lock for this specific column
     *
     * @param column
     * @throws LockingException
     * @throws ClusterException
     * @throws RemoteException
     * @throws NamingException
     */
    private void obtainBinGenerationLock(String column, String className)
            throws LockingException, ClusterException, RemoteException,
            NamingException {

        edu.ucdavis.genomics.metabolomics.binbase.util.locking.BinGenerationLock
                .getInstance().obtainLock(column, className);
    }

    /**
     * attemps to store the data in the rawdata table if not done yet is needed
     * if the export is not scheduled and started by hand
     *
     * @param experiment
     * @throws Exception
     */
    protected void storeImportData(ExperimentClass experiment) throws Exception {
        Thread.currentThread().setName("store data");
        BinBaseService service = BinBaseServiceFactory.createFactory().createService();
        for (ExperimentSample sample : experiment.getSamples()) {
            service.storeSample(sample,experiment.getColumn());
        }
    }

    /**
     * does a simple rollback and deletes related samples to this
     *
     * @param sampleIds2
     * @throws SQLException
     */
    protected void rollback(BinBaseExperimentImportSample[] samples)
            throws SQLException {
        logger.warn("need to perform a database rollback for this class");
        for (BinBaseExperimentImportSample sample : samples) {
            deleteSample.delete(sample.getSampleId());

        }
        logger.info("done");
    }

    /**
     * correct the retention times of the given samples and generate ri's out of
     * these
     *
     * @param sampleIds
     * @throws Exception
     * @author wohlgemuth
     * @version Nov 16, 2005
     */
    protected abstract void correctSamples(
            BinBaseExperimentImportSample[] samples) throws Exception;

    /**
     * matches all the given sample ids against the database
     *
     * @param sampleIds
     * @throws Exception
     * @author wohlgemuth
     * @version Nov 16, 2005
     */
    protected abstract void matchSamples(
            BinBaseExperimentImportSample samples[]) throws Exception;

    /**
     * set a flag that the import of this class is complete
     *
     * @param sample
     * @throws SQLException
     * @author wohlgemuth
     * @version Mar 22, 2006
     */
    protected abstract void finishSample(BinBaseExperimentImportSample sample)
            throws SQLException;

    /**
     * prepare the statement to get all the bins
     *
     * @author wohlgemuth
     * @version Nov 16, 2005
     * @see edu.ucdavis.genomics.metabolomics.util.SQLObject#prepareStatements()
     */
    protected void prepareStatements() throws Exception {
        super.prepareStatements();

        this.bins = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue(CLASS + ".bin"));
        this.deleteFromBin = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue(CLASS + ".deleteBin"));
        this.deleteFromSpectra = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue(CLASS + ".deleteSpectra"));
        this.allowNewBin = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue(CLASS + ".allowNewBin"));

        this.deleteSample = new DeleteSample();
        this.deleteSample.setConnection(this.getConnection());

    }

    /**
     * delete bins from this samples
     *
     * @param bins  list of bin ids, generated during this run
     * @param ratio the minal given ratio that a bin must have to be not deleted
     * @throws SQLException
     * @author wohlgemuth
     * @version Nov 15, 2005
     */
    protected abstract void deleteWrongBins(String classname, int[] bins,
                                            double ratio) throws SQLException;

    /**
     * create bins from this samples and matches them again
     *
     * @param i the sample id
     * @throws Exception
     * @author wohlgemuth
     * @version Nov 15, 2005
     */
    protected void createBins(int i) throws Exception {
        Methodable method = new GitterMethode();
        method.setConnection(this.getConnection());

        method.setSampleId(i);
        method.setNewBinAllowed(true);
        method.run();
    }

    /**
     * imports the sample itsel
     *
     * @param samples
     * @param string  the experiment class id
     * @return the generated sample id's, its an array so the implementation is
     * free to use threads or not threads for the operation
     * @throws Exception
     * @author wohlgemuth
     * @version Nov 15, 2005
     */
    protected abstract int[] importSample(
            BinBaseExperimentImportSample samples[], String className)
            throws Exception;

    /**
     * deletes the bin with the given id
     *
     * @param binId
     * @throws SQLException
     */
    protected void deleteBin(int binId) throws SQLException {
        logger.info("deleting bin: " + binId);
        this.deleteFromBin.setInt(1, binId);
        this.deleteFromSpectra.setInt(1, binId);
        this.deleteFromSpectra.execute();
        this.deleteFromBin.execute();
    }

    /**
     * check against database, if we can create bins or not
     * @param binBaseExperimentImportSample
     * @return
     * @throws SQLException
     */
    private boolean canCreateBin(
            BinBaseExperimentImportSample binBaseExperimentImportSample)
            throws SQLException {
        this.allowNewBin.setInt(1, binBaseExperimentImportSample.getSampleId());
        ResultSet set = this.allowNewBin.executeQuery();

        boolean result = false;

        if (set.next()) {
            String value = set.getString(1);
            result = BooleanConverter.StringtoBoolean(value);
        }
        set.close();
        return result;
    }

    /**
     * validates all sources to make sure we don't run in any problems!
     *
     * @param clazz
     * @throws ConfigurationException
     * @throws ValidationException
     * @author wohlgemuth
     * @version Nov 17, 2005
     */
    protected abstract void validateSources(
            BinBaseExperimentImportSample[] samples)
            throws ConfigurationException, ValidationException;

    /**
     * @return
     * @throws SQLException
     * @author wohlgemuth
     * @version Apr 23, 2006
     */
    protected boolean checkIfBinsAreAllowed() throws SQLException {

        SetupXProvider provider = SetupXFactory.newInstance().createProvider();

        try {
            if (this.isNewBinsAllowed()) {
                boolean allowBin = false;

                logger.debug("bins are explicit enabled");

                // are they allowed by the configuration
                logger.info("config allow new bins: "
                        + CONFIG.getValue("bin.allow"));
                allowBin = Boolean.valueOf(CONFIG.getValue("bin.allow"))
                        .booleanValue();

                if (allowBin) {
                    logger.debug("bins are enabled by configuration");

                    int minCount = Integer.parseInt(CONFIG.getElement("bin.allow")
                            .getAttribute("minimumClassSize").getValue());
                    int count = this.getCurrentClass().getSamples().length;

                    // are they allowed by the sample table
                    for (int i = 0; i < this.getCurrentClass().getSamples().length; i++) {
                        allowBin = this
                                .canCreateBin((BinBaseExperimentImportSample) this
                                        .getCurrentClass().getSamples()[i]);

                        if (allowBin != false) {
                            i = this.getCurrentClass().getSamples().length + 1;
                        }
                    }

                    if (allowBin) {
                        logger.debug("bins are enabled by sample");

                        // are they allowed by the minimal class size
                        allowBin = (count >= minCount);

                        if (allowBin) {
                            logger.debug("bins are enabled by class size");

                            logger.debug("checking against metadata system, if we can generate new bins...");
                            for (ExperimentSample s : this.getCurrentClass().getSamples()) {
                                String sampleName = s.getName();
                                String id = provider.getSetupXId(sampleName);

                                if (provider.canCreateBins(id) == false) {
                                    logger.debug("generation of new bin's disabled by meta data provider for sample: " + sampleName);
                                    return false;
                                }

                            }
                        } else {
                            logger.debug("bins are disabled by class");
                            return false;
                        }
                    } else {
                        logger.debug("bins are disabled by sample");
                        return false;
                    }
                } else {
                    logger.debug("bins are disabled by configuration");
                    return false;
                }
                return allowBin;
            } else {
                logger.debug("new bins are explicit disabled by the instance of this class!");
                return false;
            }
        } catch (Exception e) {
            logger.warn(e.getMessage() + " no new bins possible! ", e);
            return false;
        }
    }


    public ExperimentClass getCurrentClass() {
        return currentClass;
    }


    public boolean isNewBinsAllowed() {
        return newBinsAllowed;
    }

    public void setNewBinsAllowed(boolean newBinsAllowed) {
        this.newBinsAllowed = newBinsAllowed;
    }

}
