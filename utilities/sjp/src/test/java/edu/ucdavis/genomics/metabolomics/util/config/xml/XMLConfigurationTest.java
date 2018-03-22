package edu.ucdavis.genomics.metabolomics.util.config.xml;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.config.XMLConfigurator;
import edu.ucdavis.genomics.metabolomics.util.io.source.ByteArraySource;
import edu.ucdavis.genomics.metabolomics.util.io.source.FileSource;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import org.jdom2.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class XMLConfigurationTest {

	private Logger logger = LoggerFactory.getLogger(getClass());
	@Before
	public void setUp() throws Exception {
		config = XMLConfigurator.getInstance();
		config.reset();
		config.addConfiguration(new FileSource(new File("src/test/resources/configurator.xml")));
		config.addConfiguration(new FileSource(new File("src/test/resources/configurator2.xml")));
		
		configuable = config.getXMLConfigable("test");
		
		configuable.printTree(System.out);
		
		logger.info("element names: {}",configuable.getElementNames());
	} 

	@After
	public void tearDown() throws Exception {
		config = null;

		configuable = null;
	}

	XMLConfigurator config;

	XMLConfigable configuable;
	
	public void testReset() throws Exception {
		Properties p = config.getProperties();

		assertTrue(p.size() == 0);

		ByteArraySource third = new ByteArraySource(
				new String(
						"<config><parameter><param name=\"c\" value=\"c\" public=\"false\" /></parameter></config>")
						.getBytes());

		config.addConfiguration(third);

		p = config.getProperties();

		assertTrue(p.size() == 1);

		config.reset();
		p = config.getProperties();

		assertTrue(p.size() == 0);

	}

	public final void testAddConfigurationToConfigurator() throws Exception {
		
		ByteArraySource first = new ByteArraySource(
				new String(
						"<config><parameter><param name=\"a\" value=\"a\" public=\"true\" /></parameter><parameter><param name=\"a\" value=\"a\" public=\"true\" /></parameter></config>")
						.getBytes());
		ByteArraySource second = new ByteArraySource(
				new String(
						"<config><parameter><param name=\"b\" value=\"b\" public=\"true\" /></parameter></config>")
						.getBytes());
		ByteArraySource third = new ByteArraySource(
				new String(
						"<config><parameter><param name=\"c\" value=\"c\" public=\"false\" /></parameter></config>")
						.getBytes());

		config.addConfiguration(first);
		config.addConfiguration(second);
		config.addConfiguration(third);

		Properties p = config.getProperties();

		assertTrue(p.size() == 3);
		assertTrue(p.getProperty("a").equals("a"));
		assertTrue(p.getProperty("b").equals("b"));
		assertTrue(p.getProperty("c").equals("c"));

		assertTrue(System.getProperty("a").equals("a"));
		assertTrue(System.getProperty("b").equals("b"));

	}

	@Test
	public void testSetAttributeValue() {

		configuable.setAttributeValue("target", "simple", "true");

		String value = configuable.getAttributeValue("target", "simple");
		
		assert(value.equals("true"));
	}

	@Test
	public void testGetElement() {

		assert(configuable.getElement("target.class") != null);
	}

	@Test
	public void testSetTextValue() {

		configuable.setTextValue("target.class", "a simple value");
		assertTrue(configuable.getValue("target.class").equals("a simple value"));
	}


	@Test
	public void testAddElementToParent() {
		configuable.addElementToParent("target.class", new Element("mine"));
		assertTrue(configuable.getElement("target.class.mine") != null);
	}

	@Test
	public void testRemoveChildrenFromParent() {
		configuable.addElementToParent("target.class", new Element("mine"));
		assertTrue(configuable.getElement("target.class.mine") != null);

		configuable.removeChildrenFromParent("target.class", "mine");
		
		assertTrue(configuable.getElement("target.class.mine") == null);
	}

	@Test
	public void testRemoveAttribute() {

		configuable.removeAttribute("target", "test");
		assertTrue(configuable.getAttributeValue("target", "test") == null);
		
	}

	@Test
	public void testGetElementsString() throws IOException{
		
		assert configuable.getElements("target.parameter").size() == 1;
		
		configuable.addElementToParent("target", new Element("parameter"));
		configuable.addElementToParent("target", new Element("parameter"));
		configuable.addElementToParent("target", new Element("parameter"));
		configuable.addElementToParent("target", new Element("parameter"));
		
		assert configuable.getElements("target.parameter").size() == 5;
		
		configuable.getElements("target.parameter").remove(configuable.getElements("target.parameter").iterator().next());
		configuable.getElements("target.parameter").remove(configuable.getElements("target.parameter").iterator().next());
		
		assert configuable.getElements("target.parameter").size() == 3;
		
		configuable.printTree(System.out);
	}

	@Test
	public void testDirectRead() throws ConfigurationException{

		Source first = new FileSource(new File("src/test/resources/configurator.xml"));
		
		XMLConfiguration config = new XMLConfiguration();
		config.setSource(first);

		assert config.getElement("parameter.param") == null;
	}
}
