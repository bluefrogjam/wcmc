package edu.ucdavis.genomics.metabolomics.sjp.parser.nist;

import edu.ucdavis.genomics.metabolomics.sjp.Parser;
import edu.ucdavis.genomics.metabolomics.sjp.ParserHandler;
import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.util.Properties;

/**
 * simple test to test for exceptions and nothing else
 * 
 * @author nase
 */
public class NistMassSpecParserTest {

	class TestHandler implements ParserHandler {

		String spectra;
		String identifier;
		String synonyms;

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

			if (name.equals(NistMassSpecParser.SPECTRA)) {
				spectra = value;
			}
			else if (name.equals(NistMassSpecParser.NAME)) {
				identifier = value;
			}
			else if (name.equals(NistMassSpecParser.SYNONYMS)) {
				synonyms = value;
			}

		}

	}

	@Test
	public void testParseLine() throws ParserException {
		Parser p = new NistMassSpecParser();
		TestHandler handler = new TestHandler();

		p.parse(getClass().getResourceAsStream("/nist.txt"), handler);
		Assert.assertTrue(handler.spectra != null);
		Assert.assertTrue(handler.synonyms != null);
		Assert.assertTrue(handler.identifier != null);
		Assert
				.assertTrue(handler.spectra
						.trim()
						.equals(
								"55:32 57:37 58:32 59:98 60:27 61:64 62:4 65:49 69:80 70:23 71:38 72:41 73:728 74:134 75:999 76:77 77:47 83:45 90:16 97:16 100:26 101:5 102:198 103:27 105:12 106:4 111:8 117:23 118:9 119:108 120:9 121:4 125:45 134:15 145:14 146:643 147:57 148:116 149:10 161:41 162:5 168:8 235:2 280:2 299:4 300:2"));

	}

	@Test
	public void testParseLine4() throws ParserException {
		Parser p = new NistMassSpecParser();
		TestHandler handler = new TestHandler();

		p.parse(getClass().getResourceAsStream("/nist2.txt"), handler);
		Assert.assertTrue(handler.spectra != null);
		Assert.assertTrue(handler.synonyms == null);
		Assert.assertTrue(handler.identifier == null);
		Assert
				.assertTrue(handler.spectra
						.trim()
						.equals(
								"55:32 57:37 58:32 59:98 60:27 61:64 62:4 65:49 69:80 70:23 71:38 72:41 73:728 74:134 75:999 76:77 77:47 83:45 90:16 97:16 100:26 101:5 102:198 103:27 105:12 106:4 111:8 117:23 118:9 119:108 120:9 121:4 125:45 134:15 145:14 146:643 147:57 148:116 149:10 161:41 162:5 168:8 235:2 280:2 299:4 300:2"));

	}

	@Test
	public void testParseLine5() throws ParserException {
		Parser p = new NistMassSpecParser();
		TestHandler handler = new TestHandler();

		p.parse(getClass().getResourceAsStream("/nist3.txt"), handler);
		Assert.assertTrue(handler.spectra != null);
		Assert.assertTrue(handler.synonyms == null);
		Assert.assertTrue(handler.identifier == null);
		Assert
				.assertTrue(handler.spectra
						.trim()
						.equals(
								"55:32 57:37 58:32 59:98 60:27 61:64 62:4 65:49 69:80 70:23 71:38 72:41 73:728 74:134 75:999 76:77 77:47 83:45 90:16 97:16 100:26 101:5 102:198 103:27 105:12 106:4 111:8 117:23 118:9 119:108 120:9 121:4 125:45 134:15 145:14 146:643 147:57 148:116 149:10 161:41 162:5 168:8 235:2 280:2 299:4 300:2"));

	}
	
	@Test
	public void testParseLine6() throws ParserException {
		Parser p = new NistMassSpecParser();
		TestHandler handler = new TestHandler();

		p.parse(getClass().getResourceAsStream("/nist4.txt"), handler);
		Assert.assertTrue(handler.spectra != null);
		Assert.assertTrue(handler.synonyms == null);
		Assert.assertTrue(handler.identifier == null);
		Assert
				.assertTrue(handler.spectra
						.trim()
						.equals(
								"55:32 57:37 58:32 59:98 60:27 61:64 62:4 65:49 69:80 70:23 71:38 72:41 73:728 74:134 75:999 76:77 77:47 83:45 90:16 97:16 100:26 101:5 102:198 103:27 105:12 106:4 111:8 117:23 118:9 119:108 120:9 121:4 125:45 134:15 145:14 146:643 147:57 148:116 149:10 161:41 162:5 168:8 235:2 280:2 299:4 300:2"));

	}
	@Test
	public void testParseLine2() throws ParserException {
		Parser p = new NistMassSpecParser();
		TestHandler handler = new TestHandler();

		String nistData = "Name: Component at scan 123 (6.749 min) [Model = +75u, -63u] in \n" + "X:\\AGILENT5975\\APRIL2008\\042508-017.D\\DATA.MS \n"
				+ "Formula: \n" + "MW: N/A CAS#: N/A NIST#: N/A ID#: 6 DB: Text File \n" + "Other DBs: None \n" + "46 m/z Values and Intensities: \n"
				+ "       55 32 | 57 37 | 58 32 | 59 98 | 60 \n" + "27 | \n" + "       61 64 | 62 4 | 65 49 | 69 80 | 70 \n" + "23 | \n"
				+ "       71 38 | 72 41 | 73 728 | 74 134 | 75 \n" + "999 | \n" + "       76 77 | 77 47 | 83 45 | 90 16 | 97 \n" + "16 | \n"
				+ "       100 26 | 101 5 | 102 198 | 103 27 | 105 \n" + "12 | \n" + "       106 4 | 111 8 | 117 23 | 118 9 | 119 \n" + "108 | \n"
				+ "       120 9 | 121 4 | 125 45 | 134 15 | 145 \n" + "14 | \n" + "       146 643 | 147 57 | 148 116 | 149 10 | 161 \n" + "41 | \n"
				+ "       162 5 | 168 8 | 235 2 | 280 2 | 299 \n" + "4 | \n" + "       300 2 | \n" + "Synonyms: \n" + "no synonyms. \n";

		p.parse(new StringReader(nistData), handler);
		Assert.assertTrue(handler.spectra != null);
		Assert.assertTrue(handler.synonyms != null);
		Assert.assertTrue(handler.identifier != null);
		Assert
				.assertTrue(handler.spectra
						.trim()
						.equals(
								"55:32 57:37 58:32 59:98 60:27 61:64 62:4 65:49 69:80 70:23 71:38 72:41 73:728 74:134 75:999 76:77 77:47 83:45 90:16 97:16 100:26 101:5 102:198 103:27 105:12 106:4 111:8 117:23 118:9 119:108 120:9 121:4 125:45 134:15 145:14 146:643 147:57 148:116 149:10 161:41 162:5 168:8 235:2 280:2 299:4 300:2"));

	}

	@Test
	public void testParseLine3() throws ParserException {
		Parser p = new NistMassSpecParser();
		TestHandler handler = new TestHandler();

		String nistData = "46 m/z Values and Intensities: \n" + "       55 32 | 57 37 | 58 32 | 59 98 | 60 \n" + "27 | \n"
				+ "       61 64 | 62 4 | 65 49 | 69 80 | 70 \n" + "23 | \n" + "       71 38 | 72 41 | 73 728 | 74 134 | 75 \n" + "999 | \n"
				+ "       76 77 | 77 47 | 83 45 | 90 16 | 97 \n" + "16 | \n" + "       100 26 | 101 5 | 102 198 | 103 27 | 105 \n" + "12 | \n"
				+ "       106 4 | 111 8 | 117 23 | 118 9 | 119 \n" + "108 | \n" + "       120 9 | 121 4 | 125 45 | 134 15 | 145 \n" + "14 | \n"
				+ "       146 643 | 147 57 | 148 116 | 149 10 | 161 \n" + "41 | \n" + "       162 5 | 168 8 | 235 2 | 280 2 | 299 \n" + "4 | \n"
				+ "       300 2 | \n";

		p.parse(new StringReader(nistData), handler);
		Assert.assertTrue(handler.spectra != null);
		Assert.assertTrue(handler.synonyms == null);
		Assert.assertTrue(handler.identifier == null);
		Assert
				.assertTrue(handler.spectra
						.trim()
						.equals(
								"55:32 57:37 58:32 59:98 60:27 61:64 62:4 65:49 69:80 70:23 71:38 72:41 73:728 74:134 75:999 76:77 77:47 83:45 90:16 97:16 100:26 101:5 102:198 103:27 105:12 106:4 111:8 117:23 118:9 119:108 120:9 121:4 125:45 134:15 145:14 146:643 147:57 148:116 149:10 161:41 162:5 168:8 235:2 280:2 299:4 300:2"));

	}
}
