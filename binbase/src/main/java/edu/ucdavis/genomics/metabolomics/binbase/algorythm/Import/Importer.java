/*
 * Created on Nov 8, 2005
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.handler.ImportHandler;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.provider.SampleDataProvider;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.type.TypeFinder;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.version.handler.InvalidVersionException;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.date.PatternException;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.date.SampleDate;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.tic.Tic;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateChromatographie;
import edu.ucdavis.genomics.metabolomics.binbase.bci.mail.MailService;
import edu.ucdavis.genomics.metabolomics.binbase.bci.mail.MailServiceFactory;
import edu.ucdavis.genomics.metabolomics.binbase.bci.setupX.SetupXFactory;
import edu.ucdavis.genomics.metabolomics.binbase.bci.setupX.SetupXProvider;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.SQLObject;
import edu.ucdavis.genomics.metabolomics.util.io.source.DatabaseSourceFactoryImpl;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import edu.ucdavis.genomics.metabolomics.util.io.source.SourceFactory;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * @author wohlgemuth
 * @version Nov 8, 2005 is used to import data to the database and matches them
 */
@Component
public class Importer {

    /**
     * handler used for the import
     */
    @Autowired
    private ImportHandler handler;

    /**
     * contains a list of unwanted unique ions
     */
    private List<Integer> uniqueToIgnore = new Vector<Integer>();

    /**
     * used to prepare the type finder
     */
    @Autowired
    private TypeFinder finder;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SampleDataProvider sampleDataProvider;

    @Autowired
    private SetupXProvider setupXProvider;

    private Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * @author wohlgemuth
     * @version Nov 8, 2005
     */
    public Importer() {
        super();
    }

    /**
     * imports the sample into the database and creates some basic statistics
     *
     * @param sampleName the samplename, is used to generate a source from it
     * @param classname  the classname of the given experiment class
     * @return the current calculated sample id
     * @throws Exception
     * @author wohlgemuth
     * @version Nov 15, 2005
     */
    @SuppressWarnings("unchecked")
    public int importData(ExperimentSample sample, String classname)
            throws Exception {


        String sampleName = sample.getName();

        logger.debug("run provider");
        try {
            sampleDataProvider.run();
        } catch (InvalidVersionException e) {
            logger.error("ignore sample! " + sampleName + "/" + classname);
            return -1;
        } catch (ConfigurationException e) {
            logger.error("ignore sample! " + sampleName + "/" + classname);
            return -1;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        // get sample id
        int sampleId = this.nextSampleId();
        try {

            logger.debug("calculated sample id is: " + sampleId);

            // define needed variables
            logger.debug("get provided massspecs");
            Map[] spectra = sampleDataProvider.getSpectra();

            logger.debug("count of provided massspecs: " + spectra.length);
            int maxCount = Integer.parseInt(CONFIG
                    .getValue("deconvolution.error.count"));

            boolean newBin = true;
            boolean saturated = false;
            boolean parseError = false;

            int count = 0;

            // calculate the tic here

            logger.debug("generating tic for this sample...");
            Tic tic = new Tic();
            for (int i = 0; i < spectra.length; i++) {
                Map spec = spectra[i];
                tic.addPeak(spec);
            }

            double parseDouble = Double.parseDouble(CONFIG
                    .getValue("deconvolution.overload.tic"));
            if (tic.getCountOfPeaksWithIntensityOver(parseDouble) >= Double
                    .parseDouble(CONFIG
                            .getValue("deconvolution.overload.count"))) {
                newBin = false;
            }
            // validate data and generate needed informations about this
            // chrommatogramm
            for (int i = 0; i < spectra.length; i++) {
                Map spec = spectra[i];
                spec.put("sample_id", String.valueOf(sampleId));

                Object p = spec.get("SATURATED");

                if (p != null) {
                    saturated = true;
                    newBin = false;
                }

                int um = Integer.parseInt(spec.get("UniqueMass").toString());

                // test if the unique ion is allowed in the list of unique
                // masses
                if (this.uniqeTester(um) == true) {
                    logger.debug(" forbidden unique so spec not imported! Mass was: "
                            + um);
                    spectra[i] = null;
                } else {
                    // is this massspec is ok
                    if (ValidateChromatographie.isValidSpectra(spec)) {
                        // is ok we are fine
                    } else {
                        // ok just another deco error
                        count++;
                        logger.info(" spectra, is invalid! sample name: "
                                + source.getSourceName() + " retention index: "
                                + spec.get("Retention Index")
                                + " propertly deconvolution error!");
                    }
                }
            }

            // create needed metainformations
            Map<String, Comparable<?>> metaInformation = createMetaInformation(classname, source,
                    sampleId, maxCount, newBin, saturated, parseError, count);

            // import metainformations

            // check if the machine name is forced

            logger.info("importing sample data..." + metaInformation);
            handler.importSampleMap(metaInformation);


            logger.info("importing spectra data...");

            // import spectra
            for (Map aSpectra : spectra) {
                if (aSpectra != null) {
                    handler.importSpectraMap(aSpectra);
                }
            }

            if (handler.supportBatchMode()) {
                logger.info("firing up a batch insert...");
                handler.fireBatchInsert();
            }

        }
        catch (Exception e){
            logger.error(e.getMessage(),e);
        }

        return sampleId;

    }

    /**
     * is used to genereate the map with all the metainformations
     *
     * @param classname
     * @param source
     * @param sampleId
     * @param maxCount
     * @param newBin
     * @param saturated
     * @param parseError
     * @param count
     * @param highCount
     * @param highValue
     * @return
     * @author wohlgemuth
     * @version Nov 15, 2005
     */
    private Map<String, Comparable<?>> createMetaInformation(String classname,
                                                             Source source, int sampleId, int maxCount, boolean newBin,
                                                             boolean saturated, boolean parseError, int count) {


        Map<String, Comparable<?>> metaInformation = new HashMap<String, Comparable<?>>();
        // check if there where problems with the file at all
        if (parseError == true) {
            // logger.info(
            // " check this file for parse erros");
            logger.warn(" parse error found at sample ("
                    + source.getSourceName()
                    + ") and disable new bin generation!");

            newBin = false;
        } else if (count >= maxCount) {
            // logger.info(
            // " check this file for count of deconvolution erros");
            logger.warn(" to many deconvolution erros found at sample ("
                    + source.getSourceName() + ") count (" + count
                    + ") and disable new bin generation!");

            newBin = false;
        }

        // provide metainformations
        metaInformation.put("class", classname);
        metaInformation.put("file", source.getSourceName());
        metaInformation.put("sample_id", String.valueOf(sampleId));
        metaInformation.put("allowNewBin", String.valueOf(newBin));
        metaInformation.put("saturated", String.valueOf(saturated));

        Date date;
        int sod = 0;
        int rui = 0;
        String op = "?";
        String mach = "?";
        try {
            SampleDate d = SampleDate.createInstance(source.getSourceName());

            // first we try to generate the date from the time stamp

            try {
                long stamp = BinBaseServiceFactory
                        .createFactory()
                        .createService()
                        .getTimeStampForSample(source.getSourceName());
                date = new Date(stamp);
                logger.info("timestamp date for sample: " + date);
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
                // if this doesn't work we generate the date from the file name
                date = d.getDateAsSQL();
                logger.info("name date for sample: " + date);

            }
            sod = d.getNumberOfDay();
            op = d.getOperator();
            mach = d.getMachine();
            rui = d.getRunNumber();
        } catch (PatternException e) {
            logger.info("unknown pattern detected, assuming default values!");
            sod = 0;
            op = "?";
            mach = "?";
            rui = 0;

            try {
                long stamp = BinBaseServiceFactory
                        .createFactory()
                        .createService()
                        .getTimeStampForSample(source.getSourceName());
                date = new Date(stamp);
                logger.info("timestamp date for sample: " + date);
            } catch (Exception ex) {
                logger.debug(e.getMessage(), e);
                // if this doesn't work we generate the date from the file name
                logger.info("no timestamp using current date");
                date = new Date(new java.util.Date().getTime());

            }
        } catch (Exception e) {
            date = new Date(new java.util.Date().getTime());
            logger.error("could'nt calculate date from sample name, use current date! "
                    + source.getSourceName());
            logger.error(e.getMessage(), e);
        }

        try {
            logger.info("using sourcename: " + source.getSourceName());

            String fechingSX = setupXProvider.getSetupXId(source.getSourceName());
            logger.info("got setupX id of: " + fechingSX);
            metaInformation.put("setupX", fechingSX);
        } catch (BinBaseException e) {
            metaInformation.put("setupX",
                    "an error occured please check the log!");
            logger.warn(e.getMessage());
        } catch (Exception e) {
            metaInformation.put("setupX",
                    "an error occured please check the log!");
            logger.error(e.getMessage(), e);
        }

        if (metaInformation.get("setupX") == null) {
            logger.warn("received no setupx Id for some reason, using sample name");
            metaInformation.put("setupX", source.getSourceName());
        }
        metaInformation.put("date", date);

        metaInformation.put("rid", new Integer(rui));
        metaInformation.put("operator", op);
        metaInformation.put("mach", mach);

        metaInformation.put("sod", new Integer(sod));
        metaInformation.put("type",
                new Integer(finder.getType(source.getSourceName())));

        logger.debug("done with generation metainformations");

        return metaInformation;
    }

    /**
     * prepare all the variables
     *
     * @author wohlgemuth
     * @version Nov 15, 2005
     * @see edu.ucdavis.genomics.metabolomics.util.SQLObject#prepareVariables()
     */
    @SuppressWarnings("unchecked")
    protected void prepareVariables() throws Exception {
        this.handler.setConnection(this.getConnection());
        this.finder.setConnection(this.getConnection());

        Element temp = CONFIG.getElement("bin.forbidden");

        if (temp.getChildren().isEmpty() == false) {
        } else {
            Iterator<?> it = temp.getChildren("unique").iterator();

            while (it.hasNext()) {
                String u = ((Element) it.next()).getText();
                this.uniqueToIgnore.add(new Integer(u));
            }
        }
    }

    /**
     * generate the next sample id
     *
     * @throws Exception
     */
    private int generateSampleId() throws Exception {
        Statement state = this.dataSource.getConnection().createStatement();

        // get the next sample id
        ResultSet result = state.executeQuery(SQL_CONFIG
                .getValue("static.nextSampleId"));
        result.next();

        int id = result.getInt(1);
        state.close();

        logger.trace("created sample id: " + id);
        return id;
    }

    /**
     * calculates the next sample id
     *
     * @return
     * @throws Exception
     */
    private synchronized int nextSampleId() throws Exception {
        int id = generateSampleId();

        if (id == 0) {
            logger.info("id was null for some reason, let's generate another one...");
            id = generateSampleId();
        }
        while (!isValidSampleId(id)) {
            logger.info("sample id is already in use, creating a new one: "
                    + id);

            id = generateSampleId();
        }

        logger.info("validated sample id is: " + id);

        return id;
    }

    /**
     * checks that this sampleid does not exist yet
     *
     * @param id
     * @return
     * @throws SQLException
     */
    private boolean isValidSampleId(int id) throws SQLException {

        PreparedStatement state = this.dataSource.getConnection().prepareStatement(
                SQL_CONFIG.getValue("static.idExist"));
        state.setInt(1, id);
        // get the next sample id
        ResultSet result = state.executeQuery();

        boolean exist = result.next();
        state.close();

        return !exist;
    }

    /**
     * validates that the massspecs contains the given unique ion
     *
     * @param unique
     * @return
     * @author wohlgemuth
     * @version Nov 15, 2005
     */
    private boolean uniqeTester(int unique) {
        for (int i = 0; i < this.uniqueToIgnore.size(); i++) {
            if (unique == ((Integer) uniqueToIgnore.get(i)).intValue()) {
                return true;
            }
        }

        return false;
    }
}
