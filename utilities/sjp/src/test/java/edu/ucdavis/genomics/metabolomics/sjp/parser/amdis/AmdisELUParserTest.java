package edu.ucdavis.genomics.metabolomics.sjp.parser.amdis;

import edu.ucdavis.genomics.metabolomics.sjp.ParserHandler;
import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;
import edu.ucdavis.genomics.metabolomics.sjp.parser.msp.SpectraParser;
import junit.framework.TestCase;

import java.io.InputStream;
import java.util.Properties;

public class AmdisELUParserTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();

    }

    public void tearDown() throws Exception {

    }

    public void testParse1() throws Exception {
        parseFile("/amdis.ELU");
    }

    public void testParse2() throws Exception {
        parseFile("/amdis2.ELU");
    }


    public void parseFile(String file) throws Exception {

        InputStream stream = getClass().getResourceAsStream(file);

        AmdisELUParser parser = new AmdisELUParser();
        parser.parse(stream, new ParserHandler() {

                boolean purityFound = false;
                boolean snFound = false;
                boolean spectraFound = false;
                boolean rtFound = false;
                boolean quantMassesFound = false;
                boolean widthFound = false;

                boolean spectraWasMatched = false;

                @Override
                public void setProperties(Properties p) throws ParserException {

                }

                @Override
                public void endAttribute(String element, String name) throws ParserException {
                    if (name.equals("S/N"))
                        snFound = true;
                    if (name.equals("Width"))
                        widthFound = true;
                    if (name.equals("Purity"))
                        purityFound = true;
                    if (name.equals("Quant Masses"))
                        quantMassesFound = true;
                    if (name.equals(SpectraParser.SPECTRA))
                        spectraFound = true;
                    if (name.equals("R.T. (minutes)"))
                        rtFound = true;

                }

                @Override
                public void endDataSet() throws ParserException {
                    assertTrue(snFound);
                    assertTrue(widthFound);
                    assertTrue(purityFound);
                    assertTrue(quantMassesFound);
                    assertTrue(spectraFound);
                    assertTrue(rtFound);
                    assertTrue(spectraWasMatched);
                }

                @Override
                public void endDocument() throws ParserException {

                }

                @Override
                public void endElement(String name) throws ParserException {

                }

                @Override
                public void startAttribute(String element, String name, String value) throws ParserException {

                    if (name.equals(SpectraParser.SPECTRA)) {

                        String pattern = "(([0-9]+:[0-9]+)+ )*([0-9]+:[0-9]+)";

                        assertTrue("invalid spectra string: " + value, value.matches(pattern));
                        spectraWasMatched = true;
                    }
                }

                @Override
                public void startDataSet() throws ParserException {

                    purityFound = false;
                    snFound = false;
                    spectraFound = false;
                    rtFound = false;
                    quantMassesFound = false;
                    widthFound = false;
                    spectraWasMatched = false;

                }

                @Override
                public void startDocument() throws ParserException {

                }

                @Override
                public void startElement(String name, String value) throws ParserException {

                }
            }

        );
    }

}
