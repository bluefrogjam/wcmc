package edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.provider;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.version.Header;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.version.HeaderFactory;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.exception.HeaderProblemException;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateApexMasses;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateSpectra;
import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.sjp.tools.ConvertAmdisToPegasus;
import edu.ucdavis.genomics.metabolomics.util.SQLObject;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import org.jdom.Element;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * import standard pegasus txt files into the database
 *
 * @author wohlgemuth
 * @version Nov 8, 2005
 */
public class AmdisDataProvider extends SQLObject implements
        SampleDataProvider {

    /**
     * configuration element
     */
    protected Element config;


    boolean containsErrors = false;
    /**
     * our datasource
     */
    private Source source;

    /**
     * DOCUMENT ME!
     *
     * @uml.property name="uniqueToIgnore"
     * @uml.associationEnd elementType="java.lang.Integer" multiplicity="(0 -1)"
     */

    /**
     * DOCUMENT ME!
     */
    private Header header;

    /**
     * seperator to split datas
     */
    private String seperator = "\t";

    /**
     * contains all spectra
     */
    private Collection<Map<String, String>> spectra = new Vector<Map<String, String>>();

    /**
     * default konstruktor
     *
     * @throws ConfigurationException
     */
    public AmdisDataProvider() throws ConfigurationException {
    }

    /**
     * import file to database
     *
     * @throws Exception Exception
     */
    public void read(InputStream stream) throws Exception {
        containsErrors = false;
        spectra.clear();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream));

        // lesen der ersten zeile welcher der header ist
        String line = reader.readLine();

        while (line.length() == 0) {
            line = reader.readLine();
        }

        logger.debug("read:\n\t\t" + line);
        logger.debug("line length was: " + line.length());

        // extract headers
        // logger.info(
        // " extract header file from file:\t" + this.getFile());
        String[] header = line.split(this.seperator);

        logger.info("trying to find implementation");

        this.header = HeaderFactory.create().getHeader(header);

        while ((line = reader.readLine()) != null) {
            String[] data = line.split(this.seperator);

            // hash for this line
            Map<String, String> hash = this.header.transform(header, data);

            // convert rt to ri
            double v = Double.parseDouble(hash.get("R.T. (seconds)").replace(
                    ',', '.')) * 1000;

            hash.put("Retention Index", String.valueOf(v));


            logger.debug(hash.get("Amdis Scan") + " received spectra: " + hash.get("Spectra"));
            String spectra = ValidateSpectra.convert(ValidateSpectra
                    .sizeDown(ValidateSpectra.convert(hash
                            .get("Spectra"))));

            String apex = ValidateApexMasses.cleanApex(
                    (hash.get("Quant Masses")), spectra,
                    Integer.parseInt(hash.get("UniqueMass")));

            hash.put("Quant Masses", apex);

            //amdis doesn't know about quant SN
            hash.put("Quant S/N", hash.get("S/N"));

            // updaten der hashmap
            hash.put("Spectra", spectra);

            for (String key : hash.keySet()) {
                if (hash.get(key) == null) {
                    throw new HeaderProblemException("key " + key + " can't be null and requires a value!");
                }
            }
            this.spectra.add(hash);
        }

        reader.close();
    }

    /**
     * @author wohlgemuth
     * @version Nov 8, 2005
     */
    public Map[] getSpectra() {
        return this.spectra.toArray(new Map[this.spectra.size()]);
    }

    /**
     * @author wohlgemuth
     * @version Nov 8, 2005
     */
    public void setSource(Source source) {

        try {
            this.source = ConvertAmdisToPegasus.convert(source);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @author wohlgemuth
     * @version Nov 9, 2005
     * @see Runnable#run()
     */
    public void run() throws Exception {
        this.read(this.source.getStream());
    }

    public boolean isContainsErrors() {
        return containsErrors;
    }

    public void setContainsErrors(boolean containsErrors) {
        this.containsErrors = containsErrors;
    }

}
