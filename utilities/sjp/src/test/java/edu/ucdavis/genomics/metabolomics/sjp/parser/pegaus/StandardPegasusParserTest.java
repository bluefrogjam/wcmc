package edu.ucdavis.genomics.metabolomics.sjp.parser.pegaus;

import edu.ucdavis.genomics.metabolomics.sjp.ParserHandler;
import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;
import edu.ucdavis.genomics.metabolomics.sjp.parser.msp.MSPParser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

public class StandardPegasusParserTest {
    class TestHandler implements ParserHandler {

        String spectra;

        public void endAttribute(String element, String name) throws ParserException {
        }

        public void endDataSet() throws ParserException {
        }

        public void endDocument() throws ParserException {
        }

        public void endElement(String name) throws ParserException {
        }

        public void setProperties(Properties p) throws ParserException {
        }

        public void startAttribute(String element, String name, String value) throws ParserException {
        }

        public void startDataSet() throws ParserException {
        }

        public void startDocument() throws ParserException {
        }

        public void startElement(String name, String value) throws ParserException {
            if (name.equals(MSPParser.SPECTRA)) {
                spectra = value;
            }

        }

    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testParseLine() throws ParserException {
        StandardPegasusParser p = new StandardPegasusParser();
        TestHandler handler = new TestHandler();

        p.parse(getClass().getResourceAsStream("/pegasus.txt"), handler);

        Assert
            .assertTrue(handler.spectra
                .trim()
                .equals(
                    "55:32 57:37 58:32 59:98 60:27 61:64 62:4 65:49 69:80 70:23 71:38 72:41 73:728 74:134 75:999 76:77 77:47 83:45 90:16 97:16 100:26 101:5 102:198 103:27 105:12 106:4 111:8 117:23 118:9 119:108 120:9 121:4 125:45 134:15 145:14 146:643 147:57 148:116 149:10 161:41 162:5 168:8 235:2 280:2 299:4 300:2"));

    }

}
