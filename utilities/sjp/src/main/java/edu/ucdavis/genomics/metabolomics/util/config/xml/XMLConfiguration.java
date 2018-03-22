/*
 * Created on 03.06.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.ucdavis.genomics.metabolomics.util.config.xml;

import edu.ucdavis.genomics.metabolomics.util.config.XMLConfigurator;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import edu.ucdavis.genomics.metabolomics.util.io.source.SourceFactory;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * @author wohlgemuth
 * 
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public final class XMLConfiguration implements XMLConfigable {
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * DOCUMENT ME!
	 */
	private Element root;

	/**
	 * DOCUMENT ME!
	 */
	private String uniuqeID = "";

	/**
	 * DOCUMENT ME!
	 */
	private XMLConfigurator configPRovider;

	/**
	 * Creates a new XMLConfiguration object.
	 */
	public XMLConfiguration() {
	}

	/**
	 * @see edu.ucdavis.genomics.metabolomics.binbase.utils.config.xml.XMLConfigable#setAttributeValue(String,
	 *      String, String)
	 */
	public void setAttributeValue(String element, String attribute, String value) {
		if (this.getElement(element) == null) {
			logger.warn("sorry no element found with this name: " + element);
		}
		org.jdom2.Attribute attributeV = this.getElement(element).getAttribute(
				attribute);

		if (attributeV == null) {
			attributeV = new org.jdom2.Attribute(attribute, value);
		} else {
			attributeV.setValue(value);
		}
		this.getElement(element).setAttribute(attributeV);

	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param cof
	 *            DOCUMENT ME!
	 */
	public void setConfigProvider(XMLConfigurator cof) {
		this.configPRovider = cof;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public XMLConfigurator getConfigProvider() {
		return this.configPRovider;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param desc
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Element getElement(String desc) {
		try {
			return getAsElement(desc);
		} catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * initialize this configuration based on the given parameters
	 * 
	 * @version Aug 6, 2003
	 * @author wohlgemuth <br>
	 * @see edu.ucdavis.genomics.metabolomics.util.xml.XMLConfigable#setParameter(org.jdom2.Element)
	 */
	public void setParameter(Element element) {
		try {
			String factory = element.getChildText("factory").trim();
			String value = element.getChildText("data").trim();

			logger.debug("using factory: " + factory);
			logger.debug("using value: " + value);
			Source source = SourceFactory.newInstance(factory).createSource(
					value);
			this.setSource(source);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @uml.property name="root"
	 */
	public Element getRoot() {
		return root;
	}

	/**
	 * @see edu.ucdavis.genomics.metabolomics.binbase.utils.config.xml.XMLConfigable#setTextValue(String,
	 *      String)
	 */
	public void setTextValue(String element, String value) {
		this.getElement(element).setText(value);
	}

	/**
	 * @see edu.ucdavis.genomics.metabolomics.binbase.utils.config.xml.XMLConfigable#getUnigueID()
	 */
	public int getUnigueID() {
		return this.toString().hashCode();
	}

	/**
	 * gibt den wert zur?ck
	 * 
	 * @param desc
	 *            beschreibung in form test.test
	 * @return wert des elementes
	 */
	public String getValue(String desc) {
		try {
			return getElement(desc).getText().trim();
		} catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		try {
			StringWriter writer = new StringWriter();
			XmlHandling.writeXml(writer, this.getRoot());
			this.uniuqeID = writer.getBuffer().toString();
			writer.close();

			return this.uniuqeID;
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	/**
	 * gibt das element zur?ck
	 * 
	 * @param desc
	 *            beschreibung in form test.test
	 * @return das letzte element
	 */
	private synchronized Element getAsElement(String desc) {
		logger.trace("loading: " + desc);

		Element returnValue = null;

		String token = "";
		StringTokenizer st = new StringTokenizer(desc, ".");

		Element elem = root;

		while (st.hasMoreTokens()) {
			token = st.nextToken();

			elem = elem.getChild(token.trim());

			if (!st.hasMoreTokens()) {

				returnValue = elem;
			}
		}

		logger.trace("success");
		return returnValue;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param file
	 *            DOCUMENT ME!
	 */
	public void setSource(Source file) {
		try {
			SAXBuilder builder = new SAXBuilder();
			root = builder.build(file.getStream()).getRootElement();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void printTree(OutputStream out) throws IOException {
		XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
		output.output(root, out);
	}

	@Override
	public void addElementToParent(String parentPath, Element element) {
		Element e = this.getElement(parentPath);
		e.addContent(element);
	}

	@Override
	public void removeChildrenFromParent(String parentPath, String childrenName) {
		this.getElement(parentPath).removeChildren(childrenName);
	}

	@Override
	public void removeAttribute(String element, String attribute) {
		this.getElement(element).removeAttribute(attribute);
	}

	@Override
	public String getAttributeValue(String element, String attribute) {
		return getElement(element).getAttributeValue(attribute);
	}

	@Override
	public Collection<String> getElementNames() {

		Collection<String> result = new HashSet<String>();

		buildNames(this.root, result);
		return result;
	}

	private void buildNames(Element element, Collection<String> result) {

		for (int i = 0; i < element.getChildren().size(); i++) {
			buildNames((Element) element.getChildren().get(i), result);

		}

		if (element.isRootElement() == false) {
			result.add(getName(element));
		}
	}

	public String getName(Element element) {

		if (element.getParentElement() != null) {
			Element parent = element.getParentElement();

			if (element.getParentElement().isRootElement()) {
				return element.getName();
			} else {
				return getName(parent) + "." + element.getName();
			}
		} else {
			return element.getName();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Element> getElements(String desc) {
		logger.trace("loading: " + desc);

		String token = "";
		StringTokenizer st = new StringTokenizer(desc, ".");

		Element elem = root;

		while (st.hasMoreTokens()) {
			token = st.nextToken();

			if (elem.getChild(token.trim()) != null
					&& st.hasMoreTokens() == false) {

				return elem.getChildren(token.trim());
			} else {
				elem = elem.getChild(token.trim());
			}
		}

		logger.trace("success");
		return null;
	}

	@Override
	public void addElementToRoot(Element element) {
		this.root.addContent(element);
	}

	@Override
	@Deprecated
	public void addConfiguration(XMLConfigable configuration) {
		throw new RuntimeException("not longer supported");
	}

}
