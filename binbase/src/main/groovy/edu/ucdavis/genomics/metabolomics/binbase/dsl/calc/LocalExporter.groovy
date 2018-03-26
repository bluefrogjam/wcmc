package edu.ucdavis.genomics.metabolomics.binbase.dsl.calc

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.ClassImporter

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.sql.SQLResultDataFileFactory
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.export.ExportResult
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.export.SQLExportService
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.util.SQLResultCreator
import edu.ucdavis.genomics.metabolomics.binbase.bci.Configurator
import edu.ucdavis.genomics.metabolomics.binbase.bci.server.BinBaseService


import edu.ucdavis.genomics.metabolomics.binbase.dsl.dsl.ExporterDSL
import edu.ucdavis.genomics.metabolomics.binbase.dsl.io.ConfiguratorSource
import edu.ucdavis.genomics.metabolomics.binbase.dsl.io.GenerateConfigFile
import edu.ucdavis.genomics.metabolomics.binbase.dsl.io.SopGenerator
import edu.ucdavis.genomics.metabolomics.binbase.dsl.type.CalibrationClass
import edu.ucdavis.genomics.metabolomics.binbase.dsl.validator.ValidateDSL
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException
import edu.ucdavis.genomics.metabolomics.util.config.XMLConfigurator
import edu.ucdavis.genomics.metabolomics.util.config.xml.XmlHandling
import edu.ucdavis.genomics.metabolomics.util.database.ConnectionFactory
import edu.ucdavis.genomics.metabolomics.util.io.dest.Destination
import edu.ucdavis.genomics.metabolomics.util.io.dest.FileDestination
import edu.ucdavis.genomics.metabolomics.util.io.source.Source
import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFileFactory
import org.slf4j.Logger
import org.jdom.output.Format
import org.jdom.output.XMLOutputter

import java.sql.Connection

/**
 * User: wohlgemuth
 * Date: Sep 11, 2009
 * Time: 1:35:26 PM
 *
 */
public class LocalExporter {

    Logger logger = LoggerFactory.getLogger(getClass())

    def dsl

    boolean forceCaching = false

    public LocalExporter(def dsl) {
        this.dsl = dsl
    }

    public void setForceCaching(boolean force) {
        forceCaching = force
    }

    public String run() {

        if (dsl instanceof File) {
            logger.info("dsl\n${this.dsl.getText()}")
        } else {
            logger.info("dsl\n${this.dsl}")
        }
        ExporterDSL dsl = new ExporterDSL();
        Map readDSL = dsl.readExporterRules(this.dsl)
        return calculate(readDSL)
    }

    /**
     * calculates all the data on the local pc without any support of clustering
     * or threading
     */
    public String calculate(Map definition) {

        boolean caching = definition.caching

        if (forceCaching) {
            logger.info("manual override we force caching of data")
            caching = true
        }

        long begin = new Date().getTime()

        //make sure the validation works
        assert new ValidateDSL().validate(definition), "sorry the configuration is invalid"

        //generate system settings
        String server = definition.server
        String database = definition.column

        logger.debug "using server: ${server}"
        logger.debug "using database: ${database}"

        //check if we have a custom setupX provider
        if (definition.metaData == null) {
            logger.info "setting default setupx factory to: ${DelegateSetupXFactory.class.name}"
            System.setProperty SetupXFactory.DEFAULT_PROPERTY_NAME, DelegateSetupXFactory.class.name
        } else {
            logger.info "setting defined SetupXFactory factory to: ${definition.metaData}"
            System.setProperty SetupXFactory.DEFAULT_PROPERTY_NAME, definition.metaData
        }

        if (definition.multithread) {
            System.setProperty(ExportResult.BINBASE_MULTITHREAD, definition.multithread.toString())
            logger.info "multithreading exsplicitely set to ${definition.multithread}"
        }

        //create the configurator

        logger.info "configure system for the specified environment"

        XMLConfigurator.getInstance(new ConfiguratorSource(GenerateConfigFile.generateFile(server, definition.environment)))

        logger.info "access database configuration"

        //generate a conneection
        Properties p = Configurator.getDatabaseService().getProperties()

        ConnectionFactory instance = ConnectionFactory.getFactory()

        //defining our user
        p.setProperty("Binbase.user", definition.column)

        instance.setProperties(p)

        logger.info "initialize database connection"

        Connection connection = instance.getConnection()

        logger.debug "sql statements"
        ByteArrayOutputStream out = new ByteArrayOutputStream()

        XMLConfigurator.getInstance().getXMLConfigable("binbase.sql").printTree out

        logger.debug "\n${new String(out.toByteArray())}\n"

        out = new ByteArrayOutputStream()


        logger.debug "custom configuration: "
        logger.debug "\n${new String(out.toByteArray())}\n"

        XMLConfigurator.getInstance().getXMLConfigable("binbase.config").addConfiguration(definition.configuration)

        logger.info "configuration statements"

        out = new ByteArrayOutputStream()
        XMLConfigurator.getInstance().getXMLConfigable("binbase.config").printTree out

        logger.info "\n${new String(out.toByteArray())}\n"

        logger.info "sop for statistical processing"
        //generate sop file
        Source sop = SopGenerator.generateSOP(definition)

        logger.info "sop: \n\n ${new XMLOutputter(Format.prettyFormat).outputString(XmlHandling.readXml(SopGenerator.generateSOP(definition)))}"

        //create importer
        ClassImporter importer = ClassImporterFactory.newInstance().createImporter(connection)

        //check if no bins are allowed, needs support for regexpression patterns
        if (definition.newBins == true) {
            logger.info "new bins are permitted"
            importer.setNewBinsAllowed true
        } else {
            logger.info "new bins are not permitted"
            importer.setNewBinsAllowed false
        }

        //connecto to our service
        BinBaseService service = BinBaseServiceFactory.createFactory().createService()

        //define the experiment
        Experiment experiment = new Experiment()

        experiment.setColumn(database)
        experiment.setId(definition.name)

        ExperimentClass[] classes = createClasses(definition, service, database)

        //if we have custom uploads we will do these first
        //since we might run into some file issues and want to avoid these.
        upload(classes, definition, service, database, "")

        //no import required if the export is already based on other samples
        if (definition.based == null) {
            boolean needCalculation = false
            boolean ignoreMissingSamples = ignoreSamples(definition)
            List<ExperimentClass> classesToCalculate = new ArrayList<ExperimentClass>()

            int counter = 0;

            //time todo the actual import and testing of classes
            classes.each { ExperimentClass clazz ->

                logger.info("working on class: ${clazz.id}")

                if (clazz instanceof CalibrationClass && clazz.calibrationOnly) {
                    logger.info("no import needed for this class since it's only defined to provide calibration information")
                } else {
                    //assign to the classes array, since we need it later
                    classes[counter] = clazz
                    counter++

                    boolean clazzComplete = true

                    //make sure the server knows about this samples!
                    clazz.getSamples().each { ExperimentSample sample ->
                        logger.info("checking for samples: ${sample.getName()}")

                        try {

                            /**
                             * do we ignore missing samples
                             */
                            if (ignoreMissingSamples) {
                                if (Configurator.getImportService().sampleExist(sample.getName()) == false) {
                                    logger.info "file was not found..."
                                    clazzComplete = false
                                }
                            }

                                service.storeSample(new ExperimentSample(sample.getName(), sample.getId()), database)
                                logger.info "found and stored!"
                                needCalculation = true
                        }
                        catch (BinBaseException e) {
                            logger.error(e.getMessage(), e);
                            logger.info "sorry something is wrong with the given file ${sample.getName()}!", e
                        }
                    }

                    if (clazzComplete) {

                        classesToCalculate.add(clazz)


                        if (definition.useExistingResultFile != null && definition.useExistingResultFile == true) {
                            logger.info("we specified that we want to use the existing result file, so no calculations required")
                        } else {

                            //we are in clustered mode, so we just send the class to the cluster

                                if (needCalculation) {
                                    //now we import the data

                                    logger.info "importing data"
                                    importer.importData(clazz)

                                } else if (caching == false) {

                                    logger.info "importing data, since caching is disabled"
                                    importer.importData(clazz)
                                } else {
                                    logger.info "clazz ${clazz.getId()} was up to date and caching is enabled so no calculation neeeded"
                                }

                        }
                    } else {
                        logger.info "class was skipped from import, since at least one file was missing..."
                    }
                }
            }
            //new class object
            classes = new ExperimentClass[classesToCalculate.size()]

            classesToCalculate.eachWithIndex { ExperimentClass c, int index ->
                classes[index] = c
            }
        } else {
            logger.warn("no import of any kind since this was already based on an existing experiment!")
        }

        logger.info("we have to process ${classes.length} classes")

        //set the classes to the experiment
        experiment.setClasses(classes)

        try {

                System.setProperty(DataFileFactory.DEFAULT_PROPERTY_NAME, SQLResultDataFileFactory.class.getName())

                boolean overwrite = true

                if (definition.useExistingResultFile != null && definition.useExistingResultFile == true) {
                    logger.info("we specified that we want to use the existing result file, so no calculations required")
                    overwrite = false;
                } else {

                    //create the experiment definition
                    new SQLResultCreator(connection).createResultDefinition(experiment)

                }

                //generate the actual content
                Destination destination = new FileDestination()

                if (dsl instanceof File) {

                    if (dsl.getParentFile() != null) {
                        destination.setIdentifier(new File(dsl.getParentFile(), "${experiment.getId()}.zip"))
                    } else {
                        destination.setIdentifier(new File("${experiment.getId()}.zip"))
                    }
                } else {
					logger.info("storing in local directory under it's defined id!");
					destination.setIdentifier(new File("${experiment.getId()}.zip"))				
                }

                SQLExportService exporter = new SQLExportService(connection)

                //we always need to overwrite the existing result file, since we never know if the dsl
                //changed its layout
                return exporter.export(experiment.getId(), experiment.getId(), sop, destination, overwrite)

        }
        finally {

            double need = (new Date().getTime() - begin) / (double) 1000 / (double) 60

            logger.info "required time was ${need} minutes"

        }
    }

    /**
     * do we want to ignore missing smaples
     * @return
     */
    private boolean ignoreSamples(Map definition) {
        boolean ignoreMissingSamples = false;

        if (definition.ignoreMissingSamples != null) {
            if (definition.ignoreMissingSamples == true) {
                logger.info "set ignore missing samples to true"
                ignoreMissingSamples = true
            } else {
                logger.info "missing samples are explictetely not permitted!"
            }
        } else {
            logger.info "missing samples are not permitted!"
        }

        return ignoreMissingSamples
    }

    /**
     * creates our array of required classes
     * @param definition
     * @param service
     * @param database
     * @return
     */
    private ExperimentClass[] createClasses(Map definition, BinBaseService service, String database) {
        ExperimentClass[] classes = null

            logger.info("this experiment is class based")
            classes = definition.classes



        if (classes.length == 0) {
            throw new Exception("you need to provide at least one class!")
        }

        return classes
    }

    /**
     * uploads the given class files to the server from the directories defined in the sop
     * @param classes
     * @param definition
     * @param service
     * @param database
     * @param key
     * @return
     */
    private upload(ExperimentClass[] classes, Map definition, BinBaseService service, String database, String key) {
        if (definition.upload?.txt || definition.upload?.cdf) {

            //go over all classes and upload possible data
            classes.each { ExperimentClass clazz ->

                //make sure the server knows about this samples!
                clazz.getSamples().each { ExperimentSample sample ->
                    logger.info("checking for samples: ${sample.getName()}")

                    try {

                        /**
                         * upload local data to the server
                         */
                        if (definition.upload?.txt) {
                            if (definition.upload?.txt instanceof Collection) {
                                definition.upload?.txt.each { String currentDir ->
                                    if (uploadFileToServer(currentDir, sample, true)) {
                                        logger.info("updating file in database, since a newer version was uploaded")
                                        service.storeSample(new ExperimentSample(sample.getName(), sample.getId()), database)
                                    }
                                }
                            } else {
                                logger.warn("Variable 'upload.txt' is of wrong type! ${definition.upload.txt.class}")
                            }


                        }

                        /**
                         * upload local cdf data to the server
                         */
                        if (definition.upload?.cdf) {
                            if (definition.upload?.cdf instanceof Collection) {
                                definition.upload?.cdf.each { String currentDir ->
                                    uploadCDFFileToServer(currentDir, sample)
                                }
                            } else {
                                logger.warn("Variable 'upload.cdf' is of wrong type! ${definition.upload.cdf.class}")
                            }
                        }
                    }
                    catch (BinBaseException ex) {
                        logger.error(ex.getMessage(), ex);
                        logger.info "sorry something is wrong with the given file ${sample.getName()}!", ex

                        //clazz.removeSample sample.getName()
                    }
                }
            }
        }
    }

    /**
     * uploads a sample to the server to be processed during the import phase
     * @param dir
     * @param sample
     */
    private boolean uploadFileToServer(String dir, ExperimentSample sample, force = false) {

        //we can force the refreshing of the file on the server side
        if (force == false) {
            if (Configurator.importService.sampleExist(sample.getName())) {
                logger.info "file already exists on server, skip upload: ${sample.getName()}"
                return false
            }
        }
        logger.info "uploading ${sample.name} from dir: ${dir} to server."
        File file = new File(dir, sample.name + ".txt")

        if (file.exists()) {
            logger.info "found file and uploading: ${file.getName()}"
            Configurator.importService.uploadImportFile(file.getName(), file.getBytes())
            return true
        } else {
            logger.info("file not found: ${file.getName()} - ${file.getAbsolutePath()}")
            file = new File(dir, sample.name + ".txt.gz")

            if (file.exists()) {
                logger.info "found compressed file and uploading: ${file.getName()}"

                Configurator.importService.uploadImportFile(file.getName(), file.getBytes())
                return true

            } else {
                logger.info("file not found: ${file.getName()} - ${file.getAbsolutePath()}")
            }
        }

        return false
    }

    private void uploadCDFFileToServer(String dir, ExperimentSample sample) {
        logger.info "uploading cdf ${sample.name} from dir: ${dir} to server."
        File file = new File(dir, sample.name + ".cdf")

        if (file.exists()) {
            Configurator.exportService.uploadNetCdf(file.getName(), file.getBytes())
        } else {
            logger.info("file not found: ${file.getName()} - ${file.getAbsolutePath()}")
            file = new File(dir, sample.name + ".cdf.gz")

            if (file.exists()) {
                Configurator.exportService.uploadNetCdf(file.getName(), file.getBytes())
            } else {
                logger.info("file not found: ${file.getName()} - ${file.getAbsolutePath()}")
            }
        }
    }

}
