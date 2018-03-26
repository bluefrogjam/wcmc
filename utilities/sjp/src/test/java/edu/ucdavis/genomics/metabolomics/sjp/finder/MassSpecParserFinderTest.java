package edu.ucdavis.genomics.metabolomics.sjp.finder;


import edu.ucdavis.genomics.metabolomics.sjp.Parser;
import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;
import edu.ucdavis.genomics.metabolomics.sjp.parser.msp.MSPParser;
import edu.ucdavis.genomics.metabolomics.sjp.parser.nist.NistMassSpecParser;
import edu.ucdavis.genomics.metabolomics.sjp.parser.pegaus.StandardPegasusParser;
import edu.ucdavis.genomics.metabolomics.sjp.parser.pegaus.TableMassSpecParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MassSpecParserFinderTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws ParserException, IOException{
		MassSpecParserFinder finder = new MassSpecParserFinder();
		finder.addParser(new StandardPegasusParser());
		finder.addParser(new MSPParser());
		finder.addParser(new TableMassSpecParser());
		finder.addParser(new NistMassSpecParser());
		
		Parser parser = null;
		
		parser = finder.find(getClass().getResourceAsStream("/msp1.txt"));
		assertTrue(parser != null);
		assertTrue(parser instanceof MSPParser);
		assertFalse(parser instanceof NistMassSpecParser);
		assertFalse(parser instanceof TableMassSpecParser);
		assertFalse(parser instanceof StandardPegasusParser);

		parser = finder.find(getClass().getResourceAsStream("/msp2.txt"));
		assertTrue(parser != null);
		assertTrue(parser instanceof MSPParser);
		assertFalse(parser instanceof NistMassSpecParser);
		assertFalse(parser instanceof TableMassSpecParser);
		assertFalse(parser instanceof StandardPegasusParser);

		parser = finder.find(getClass().getResourceAsStream("/msp3.txt"));
		assertTrue(parser != null);
		assertTrue(parser instanceof MSPParser);
		assertFalse(parser instanceof NistMassSpecParser);
		assertFalse(parser instanceof TableMassSpecParser);
		assertFalse(parser instanceof StandardPegasusParser);

		parser = finder.find(getClass().getResourceAsStream("/msp4.txt"));
		assertTrue(parser != null);
		assertTrue(parser instanceof MSPParser);
		assertFalse(parser instanceof NistMassSpecParser);
		assertFalse(parser instanceof TableMassSpecParser);
		assertFalse(parser instanceof StandardPegasusParser);

		parser = finder.find(getClass().getResourceAsStream("/msp5.txt"));
		assertTrue(parser != null);
		assertTrue(parser instanceof MSPParser);
		assertFalse(parser instanceof NistMassSpecParser);
		assertFalse(parser instanceof TableMassSpecParser);

		parser = finder.find(getClass().getResourceAsStream("/msp6.txt"));
		assertTrue(parser != null);
		assertTrue(parser instanceof MSPParser);
		assertFalse(parser instanceof NistMassSpecParser);
		assertFalse(parser instanceof TableMassSpecParser);

		parser = finder.find(getClass().getResourceAsStream("/msp7.txt"));
		assertTrue(parser != null);
		assertTrue(parser instanceof MSPParser);
		assertFalse(parser instanceof NistMassSpecParser);
		assertFalse(parser instanceof TableMassSpecParser);

		parser = finder.find(getClass().getResourceAsStream("/nist.txt"));
		assertTrue(parser != null);
		assertFalse(parser instanceof MSPParser);
		assertTrue(parser instanceof NistMassSpecParser);
		assertFalse(parser instanceof TableMassSpecParser);

		parser = finder.find(getClass().getResourceAsStream("/nist2.txt"));
		assertTrue(parser != null);
		assertFalse(parser instanceof MSPParser);
		assertTrue(parser instanceof NistMassSpecParser);
		assertFalse(parser instanceof TableMassSpecParser);
		assertFalse(parser instanceof StandardPegasusParser);

		parser = finder.find(getClass().getResourceAsStream("/nist3.txt"));
		assertTrue(parser != null);
		assertFalse(parser instanceof MSPParser);
		assertTrue(parser instanceof NistMassSpecParser);
		assertFalse(parser instanceof TableMassSpecParser);
		assertFalse(parser instanceof StandardPegasusParser);

		parser = finder.find(getClass().getResourceAsStream("/nist4.txt"));
		assertTrue(parser != null);
		assertFalse(parser instanceof MSPParser);
		assertTrue(parser instanceof NistMassSpecParser);
		assertFalse(parser instanceof TableMassSpecParser);
		assertFalse(parser instanceof StandardPegasusParser);
		
		parser = finder.find(getClass().getResourceAsStream("/table.txt"));
		assertTrue(parser != null);
		assertFalse(parser instanceof MSPParser);
		assertFalse(parser instanceof NistMassSpecParser);
		assertTrue(parser instanceof TableMassSpecParser);
		assertFalse(parser instanceof StandardPegasusParser);
		
		System.out.println("checking pegasus mass spec parser...");

		finder = new MassSpecParserFinder();
		finder.addParser(new StandardPegasusParser());
		finder.addParser(new MSPParser());
		finder.addParser(new NistMassSpecParser());
		
		parser = finder.find(getClass().getResourceAsStream("/pegasus.txt"));
		assertTrue(parser != null);
		assertFalse(parser instanceof MSPParser);
		assertFalse(parser instanceof NistMassSpecParser);
		assertTrue(parser instanceof StandardPegasusParser);
		
	}
}
