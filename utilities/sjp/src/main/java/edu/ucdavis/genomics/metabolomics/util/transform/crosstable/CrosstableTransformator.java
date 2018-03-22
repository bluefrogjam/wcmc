/*
 * Created on Aug 30, 2003
 *
 */
package edu.ucdavis.genomics.metabolomics.util.transform.crosstable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import edu.ucdavis.genomics.metabolomics.util.statistics.Statistics;
import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.Max;
import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.Mean;
import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.Median;
import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.Modus;
import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.NonZeroMean;
import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.NonZeroMedian;
import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.NonZeroMin;
import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.RandomSamplingVariance;
import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.StandardDeviation;
import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.Variance;
import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.VariationCoefficient;
import edu.ucdavis.genomics.metabolomics.util.statistics.replacement.ReplaceWithMean;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.format.xls.XMLtoXLSTransformHandler;


/**
 * @author wohlgemuth
 * @version Aug 30, 2003 <br>
 *          BinBaseDatabase
 * @description
 */
public class CrosstableTransformator implements Runnable, Transformator {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 2L;

    /**
     *
     * @uml.property name="transformer"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    AbstractXMLTransformHandler transformer = null;
    InputStream in = null;
    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     *
     * @uml.property name="normalize"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    OutputStream out = null;
    String key = "height";

    /**
     * @version Sep 1, 2003
     * @author wohlgemuth <br>
     * @param stream
     *
     * @uml.property name="in"
     */
    public void setIn(InputStream stream) {
        in = stream;
    }

    /**
     * @version Aug 30, 2003
     * @author wohlgemuth <br>
     * @param string
     *
     * @uml.property name="key"
     */
    public void setKey(String string) {
        key = string;
    }

    /**
     * @version Aug 30, 2003
     * @author wohlgemuth <br>
     * @return
     *
     * @uml.property name="key"
     */
    public String getKey() {
        return key;
    }

    /**
     * @version Sep 1, 2003
     * @author wohlgemuth <br>
     * @param stream
     *
     * @uml.property name="out"
     */
    public void setOut(OutputStream stream) {
        out = stream;
    }

    /**
     * @version Sep 1, 2003
     * @author wohlgemuth <br>
     * @return
     *
     * @uml.property name="out"
     */
    public OutputStream getOut() {
        return out;
    }

    /**
     * DOCUMENT ME!
     *
     * @param stat DOCUMENT ME!
     */
    public void setStatistics(Statistics stat) {
        if (stat != null) {
            transformer.setStatistics(stat);
        } else {
            this.logger.error("Statistic cannot be null!");
        }
    }

    /**
     * @version Aug 30, 2003
     * @author wohlgemuth <br>
     * @param handler
     *
     * @uml.property name="transformer"
     */
    public void setTransformer(AbstractXMLTransformHandler handler) {
        transformer = handler;
    }

    /**
     * @version Aug 30, 2003
     * @author wohlgemuth <br>
     * @return
     *
     * @uml.property name="transformer"
     */
    public AbstractXMLTransformHandler getTransformer() {
        return transformer;
    }

    /**
     * @version Sep 1, 2003
     * @author wohlgemuth <br>
     * @return
     *
     * @uml.property name="in"
     */
    public InputStream getIn() {
        return in;
    }

    /**
     * DOCUMENT ME!
     *
     * @param header DOCUMENT ME!
     */
    public void addHeader(String header) {
        if (this.transformer == null) {
            throw new RuntimeException("you must set a transformer first");
        }

        this.transformer.addHeader(header);
    }

    /**
     * @version Aug 30, 2003
     * @author wohlgemuth <br>
     * @see Runnable#run()
     */
    public void run() {
        this.transformer.setKey(this.getKey());
        this.transformer.setStream(this.getOut());

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);

        try {
            SAXParser builder = factory.newSAXParser();
            builder.parse(in, transformer);
        } catch (SAXException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
