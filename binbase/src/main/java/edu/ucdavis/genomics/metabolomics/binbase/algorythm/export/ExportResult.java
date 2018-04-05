/*
 * Created on Aug 18, 2005
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.export;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.SampleMatcher;
import edu.ucdavis.genomics.metabolomics.binbase.bci.Configurator;
import edu.ucdavis.genomics.metabolomics.binbase.bci.server.config.source.DatabaseConfigSource;
import edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx.ExportJMXFacadeUtil;
import edu.ucdavis.genomics.metabolomics.binbase.bdi.util.URLGenerator;
import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.SQLObject;
import edu.ucdavis.genomics.metabolomics.util.config.XMLConfigurator;
import edu.ucdavis.genomics.metabolomics.util.database.ConnectionFactory;
import edu.ucdavis.genomics.metabolomics.util.io.Copy;
import edu.ucdavis.genomics.metabolomics.util.io.source.FileSource;
import edu.ucdavis.genomics.metabolomics.util.io.source.SourceFactory;
import edu.ucdavis.genomics.metabolomics.util.type.converter.BooleanConverter;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.13 $
 */
public class ExportResult extends SQLObject {

    /**
     * erstellt die liste der bins
     */
    PreparedStatement binStatement = null;

    PreparedStatement virtualBinStatement = null;

    PreparedStatement refrencesStatement = null;

    PreparedStatement refrenceClassStatement = null;

    /**
     * calculates the version of the bins
     */
    private PreparedStatement fetchVersion;

    /**
     * calculates the version of a sample
     */
    private PreparedStatement fetchSampleVersion;
    private PreparedStatement getInchiStatement;

    public ExportResult() {
        super();
    }

    private int getVersion(int sampleId) throws SQLException, IOException {
        this.fetchSampleVersion.setInt(1, sampleId);
        ResultSet res = this.fetchSampleVersion.executeQuery();

        int result = -1;
        if (res.next()) {
            try {
                if (res.getBinaryStream(2).read() > -1) {
                    result = res.getInt(1);
                } else {
                    logger.info("no data available");
                }
            } catch (NullPointerException e) {
                logger.error("no data available: " + e.getMessage());

            }
        }
        res.close();

        return result;
    }

    /**
     * DOCUMENT ME!
     */
    private BufferedWriter writer;

    /**
     * DOCUMENT ME!
     */
    private PreparedStatement binVitualCountStatement;

    /**
     * DOCUMENT ME!
     */
    private PreparedStatement sampleCountStatement;

    /**
     * DOCUMENT ME!
     */
    private PreparedStatement binCountStatement;

    /**
     * DOCUMENT ME!
     */
    private PreparedStatement sampleStatement;

    private PreparedStatement isStandardStatement;

    private PreparedStatement binGroupStatement;

    /**
     * a special flag, which allows us to use outdated data. Should only be used
     * for debug etc. to avoid calculation. But will produce wrong results in
     * production mode
     */
    private boolean dontUpdateCache = false;

    private PreparedStatement relatedBinsStatement;

    /**
     * export the files in the given outputstream
     *
     * @param resultID the given result id
     * @throws Exception
     */
    public void export(int resultID, BufferedWriter writer) throws Exception {
        this.writer = writer;
        this.writeStartTag("output");

        logger.info("writing dimensions...");
        createDimension(resultID);

        logger.info("writing header...");
        writeHeader();

        logger.info("writing content...");
        writeContent(resultID, writer);
        this.writeEndTag("output");

        this.writer.flush();
        this.writer.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void prepareStatements() throws Exception {
        this.binStatement = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue(CLASS + ".bin"));
        this.virtualBinStatement = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue(CLASS + ".virtualbin"));
        this.binCountStatement = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue(CLASS + ".binCount"));
        this.binVitualCountStatement = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue(CLASS + ".virtualbinCount"));
        this.sampleStatement = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue(CLASS + ".sample"));
        this.sampleCountStatement = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue(CLASS + ".sampleCount"));

        this.fetchVersion = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue(CLASS + ".fetchVersion"));
        this.fetchSampleVersion = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue(CLASS + ".sampleVersion"));

        this.refrencesStatement = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue(CLASS + ".fetchRefrences"));
        this.refrenceClassStatement = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue(CLASS + ".fetchRefrencesClass"));

        this.isStandardStatement = this.getConnection().prepareStatement(
                SQL_CONFIG.getValue(CLASS + ".isStandard"));

        this.binGroupStatement = this.getConnection().prepareStatement(
                "select name from bin_group where group_id = ?");

        this.relatedBinsStatement = this.getConnection().prepareStatement(
                "select bin_id from bin where group_id = ?");

        this.getInchiStatement = this.getConnection().prepareStatement(
                "select inchi_key from bin where bin_id = ?");

    }

    /**
     * creates the dimension tag of the dataset
     *
     * @param resultID
     * @throws SQLException
     * @throws IOException
     */
    private void createDimension(int resultID) throws SQLException, IOException {
        // create the header
        sampleCountStatement.setInt(1, resultID);

        ResultSet resA = binCountStatement.executeQuery();
        ResultSet resB = binVitualCountStatement.executeQuery();
        ResultSet resC = sampleCountStatement.executeQuery();

        resA.next();
        resB.next();
        resC.next();

        int binSize = resA.getInt(1) + resB.getInt(1);
        int sampleSize = resC.getInt(1);

        resA.close();
        resB.close();
        resC.close();

        // gibt die gr?sse des datensets aus
        this.writeStartTag("dimension");

        this.writer.write("<size bin=\"" + binSize + "\" sample=\""
                + sampleSize + "\" database=\""
                + this.getConnection().getMetaData().getUserName()
                + "\" result = \"" + resultID + "\" />\n");

        this.writeStartTag("refrenceTypes");

        ResultSet result = this.refrenceClassStatement.executeQuery();

        while (result.next()) {
            this.writer.write("<type name=\"" + result.getString("name")
                    + "\" description=\"" + result.getString("description")
                    + "\" />\n");
        }
        result.close();
        this.writeEndTag("refrenceTypes");
        this.writeEndTag("dimension");

        System.gc();
    }

    /**
     * @param resultID
     * @param writer
     * @throws IOException
     * @throws SQLException
     * @throws ConfigurationException
     */
    private void writeContent(int resultID, BufferedWriter writer)
            throws Exception {

        this.writeStartTag("quantification");
        this.sampleStatement.setInt(1, resultID);

        SampleMatcher matcher = SampleMatcherFactory
                .newInstance()
                .createMatcher(this.getConnection().getMetaData().getUserName());

        ResultSet result = null;
        Collection<Integer> samplesToWrite = new HashSet<Integer>();
        try {
            result = this.sampleStatement.executeQuery();

            // create calculation queue
            while (result.next()) {
                Integer in = result.getInt(1);

                logger.debug("current sample id: " + in);
                // validate if this sample is uptodate
                int sampleVersion = getVersion(in);

                matcher.addSampleToCalculate(in);
                // keep track of samples we need to write out
                samplesToWrite.add(in);
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            result.close();
        }

        Collection<Integer> content = new Vector<Integer>();
        matcher.matchSamples();

        // load the data to be written ou
        for (Integer in : samplesToWrite) {
            getCachedSample(in, content);
        }

        logger.info("generating output after matching for " + content.size()
                + " samples");
        // write output

        for (Integer id : content) {
            String contentString = getContent(id);
            writer.write(contentString);
        }

        this.writeEndTag("quantification");
        writer.flush();
        logger.info("wrote content");
        System.gc();
    }

    /**
     * fetch cached results
     *
     * @param in
     * @param content
     * @throws ConfigurationException
     * @throws IOException
     * @author wohlgemuth
     * @version Nov 8, 2006
     */
    protected void getCachedSample(Integer in, Collection<Integer> content)
            throws ConfigurationException, IOException {
        content.add(in);
    }

    protected String getContent(Integer in) throws ConfigurationException,
            IOException {
        Map<String, Connection> map = new HashMap<String, Connection>();
        map.put("CONNECTION", this.getConnection());
        InputStream input = SourceFactory
                .newInstance(
                        QuantificationTableSourceFactoryImpl.class.getName())
                .createSource(in, map).getStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Copy.copy(input, out);

        input.close();
        out.flush();
        out.close();

        return out.toString();
    }

    private int getVersionBin() throws SQLException {
        ResultSet res = this.fetchVersion.executeQuery();

        if (res.next()) {
            return res.getInt(1);
        } else {
            return 0;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param tag DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    private void writeEndTag(String tag) throws IOException {
        writer.write("</" + tag + ">\n");
    }

    /**
     * writes the header which contains all bins
     *
     * @throws SQLException
     * @throws IOException
     */
    private void writeHeader() throws SQLException, IOException {
        // rufe die bins ab
        ResultSet bin = this.binStatement.executeQuery();

        this.writeStartTag("header");

        // schreibt bins
        while (bin.next()) {
            String spectra = bin.getString("spectra");
            String apexsn = bin.getString("apex_sn");
            String quantmass = bin.getString("quantmass");

            String ri = bin.getString("retention_index");

            // let's find out to which group this bin belongs
            String groupName = "none";

            // all bins with the same group id
            String relatedBins = "";

            if (bin.getInt("group_id") != 0) {
                Integer it = Integer.parseInt(bin.getString("group_id"));

                binGroupStatement.setInt(1, it);
                ResultSet set = binGroupStatement.executeQuery();

                if (set.next()) {
                    groupName = set.getString(1);
                }

                set.close();

                relatedBinsStatement.setInt(1, it);
                set = relatedBinsStatement.executeQuery();

                while (set.next()) {
                    Integer id = set.getInt(1);

                    if (!id.equals(bin.getInt("bin_id"))) {
                        if (relatedBins.isEmpty()) {
                            relatedBins = id.toString();
                        } else {
                            relatedBins = relatedBins + "," + id;
                        }
                    }
                }
            }

            // is this bin a standard
            boolean standard = isStandard(bin.getInt("bin_id"));

            String inchi = getInchi(bin.getInt("bin_id"));

            this.writer.write("<entry name=\"" + bin.getString("name")
                    + "\" spectra = \"" + spectra + "\" apexsn=\"" + apexsn
                    + "\" quantmass=\"" + quantmass
                    + "\" mostabundant=\"not available"
                    + "\" retention_index=\"" + ri + "\" id=\""
                    + bin.getInt("bin_id") + "\" standard=\""
                    + BooleanConverter.booleanToString(standard)
                    + "\" group=\"" + bin.getInt("group_id") + "\""
                    + " group_name=\"" + groupName + "\" "
                    + " other_bins_in_group=\"" + relatedBins + "\" "
                    + " inchi_key=\""
                    + inchi + "\" >\n ");

            this.createRefrences(writer, bin.getInt("bin_id"));
            this.writer.write("</entry>");
        }

        bin.close();
        bin = this.virtualBinStatement.executeQuery();

        // schreibt virtuell bins
        while (bin.next() == true) {
            this.writer.write("<entry name=\"" + bin.getString("name")
                    + "\" virtual=\"true\"/>\n");
        }

        bin.close();
        this.writeEndTag("header");
    }

    private String getInchi(int binId) throws SQLException {
        this.getInchiStatement.setInt(1, binId);
        ResultSet set = this.getInchiStatement.executeQuery();

        try {
            if (set.next()) {
                String inchi = set.getString("inchi_key");
                return inchi;
            }
            return null;
        } finally {
            set.close();
        }
    }

    private boolean isStandard(int binId) throws SQLException {
        this.isStandardStatement.setInt(1, binId);

        ResultSet set = this.isStandardStatement.executeQuery();

        boolean next = set.next();

        set.close();
        return next;
    }

    /**
     * writes the refrences to each bin in the database
     *
     * @throws SQLException
     * @throws IOException
     * @author wohlgemuth
     * @version Jun 7, 2006
     */
    private void createRefrences(BufferedWriter writer, int binId)
            throws SQLException, IOException {
        ResultSet result = this.refrenceClassStatement.executeQuery();

        writer.write("<refrences>\n");
        while (result.next()) {
            int id = result.getInt("id");
            String name = result.getString("name");
            String urlPattern = result.getString("pattern");

            this.refrencesStatement.setInt(1, binId);
            this.refrencesStatement.setInt(2, id);

            ResultSet res = this.refrencesStatement.executeQuery();
            String value = getRefrenceValue(res);

            if (value.length() == 0) {

                // just write the data
                writer.write("<refrence name=\"" + name + "\" value=\"" + value
                        + "\" link = \"\" />\n");
            } else {
                if (URLGenerator.validateURLPattern(urlPattern)) {
                    writer.write("<refrence name=\"" + name + "\" value=\""
                            + value + "\" link = \""
                            + URLGenerator.generateURL(urlPattern, value)
                            + "\" />\n");
                } else {
                    writer.write("<refrence name=\"" + name + "\" value=\""
                            + value + "\" link = \"\" />\n");
                }
            }
            res.close();
        }

        writer.write("</refrences>\n");

        result.close();
    }

    private String getRefrenceValue(ResultSet set) throws SQLException {
        if (set.next()) {
            return set.getString("value");
        }
        return "";
    }

    /**
     * DOCUMENT ME!
     *
     * @param tag DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    private void writeStartTag(String tag) throws IOException {
        writer.write("<" + tag + ">\n");
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 3) {
            XMLConfigurator config = XMLConfigurator.getInstance();

            config.addConfiguration(new FileSource(new File(System
                    .getProperty("user.home")
                    + "/.config/applicationServer.xml")));
            config.addConfiguration(new DatabaseConfigSource(Configurator
                    .getDatabaseService().createProperties()));

            System.getProperties().putAll(config.getProperties());
            System.getProperties().put("Binbase.user", args[0]);

            ConnectionFactory fact = ConnectionFactory.getFactory();
            fact.setProperties(System.getProperties());
            ExportResult result = new ExportResult();
            result.setConnection(fact.getConnection());
            result.export(Integer.parseInt(args[1]), new BufferedWriter(
                    new FileWriter(args[2])));
        } else {
            System.out.println("USAGE: database id file");
        }
    }

}
