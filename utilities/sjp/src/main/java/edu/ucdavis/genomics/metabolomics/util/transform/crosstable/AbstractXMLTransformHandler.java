/*
 * Created on Aug 29, 2003
 *
 */
package edu.ucdavis.genomics.metabolomics.util.transform.crosstable;

import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import edu.ucdavis.genomics.metabolomics.util.statistics.Statistics;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.BinFormat;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.BinObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ClassFormat;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ClassObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ContentObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ErrorObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.FormatObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.HeaderFormat;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.NullObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ProblematicObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.RefrenceObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.SampleObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.SetupXFormat;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ZeroObject;

/**
 * @author wohlgemuth
 * @version Aug 29, 2003 <br>
 *          BinBaseDatabase
 * @description
 */
public abstract class AbstractXMLTransformHandler extends DefaultHandler{
	/**
	 * DOCUMENT ME!
	 */
	private Collection<String> binHeader = new Vector<String>();

	/**
	 * DOCUMENT ME!
	 */
	private Collection<List<?>> cacheStore = new Vector<List<?>>();

	/**
	 * DOCUMENT ME!
	 */
	private Collection<String> classStore = new Vector<String>();

	/**
	 * DOCUMENT ME!
	 */
	private List<String> headerRow = null;

	/**
	 * DOCUMENT ME!
	 */
	private List<FormatObject<?>> line;

	/**
	 * DOCUMENT ME!
	 */
	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	/**
	 * DOCUMENT ME!
	 */
	private OutputStream out;

	/**
	 * DOCUMENT ME!
	 * 
	 * @uml.property name="statistics"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	private Statistics statistics = null;

	/**
	 * DOCUMENT ME!
	 */
	private String keyName;

	/**
	 * DOCUMENT ME!
	 */
	private Vector<Map<String, FormatObject<?>>> temp;

	/**
	 * DOCUMENT ME!
	 */
	private boolean firstline;

	/**
	 * DOCUMENT ME!
	 */
	private int binCount = 0;

	/**
	 * DOCUMENT ME!
	 */
	private int sampleCount = 0;

	private HashMap<String, Vector<FormatObject<?>>> refrenceTypes;

	private Object lastBin;

	private int resultId;

	private String database;

	public String getDatabase() {
		return database;
	}

	public int getResultId() {
		return resultId;
	}

	/**
	 * setzt den gew?nschten parsing key f?r das ergebniss
	 * 
	 * @version Aug 14, 2003
	 * @author wohlgemuth <br>
	 * @param key
	 */
	public final void setKey(String key) {
		this.keyName = key;
	}

	/**
	 * gibt den gew?nschten parsing key zur?ck
	 * 
	 * @version Aug 14, 2003
	 * @author wohlgemuth <br>
	 * @return
	 */
	public final String getKey() {
		return keyName;
	}

	/**
	 * @return Returns the logger.
	 * @uml.property name="logger"
	 */
	public Logger getLogger() {
		return this.logger;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param out
	 *            DOCUMENT ME!
	 */
	public void setStream(OutputStream out) {
		this.out = out;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public OutputStream getStream() {
		return this.out;
	}

	/**
	 * @version Aug 13, 2003
	 * @author wohlgemuth <br>
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public final void characters(char[] ch, int start, int length) {
	}

	/**
	 * DOCUMENT ME!
	 */
	@SuppressWarnings("unchecked")
	public final void endDocument() {
		if (statistics != null) {
			Collection<?> result = statistics.doStatistics(cacheStore, classStore);

			Iterator<?> itx = result.iterator();

			while (itx.hasNext()) {
				writeLine((List<FormatObject<?>>) itx.next());
			}
		}

		cacheStore.clear();
		classStore.clear();

		endTransform();
	}

	/**
	 * @version Aug 13, 2003
	 * @author wohlgemuth <br>
	 * @see org.xml.sax.ContentHandler#endElement(String,
	 *      String, String)
	 */
	public final void endElement(String uri, String localName, String qName) {
		if ("sample".equals(qName)) {
			writeLine();
		} else if ("header".equals(qName)) {
			writeLine();
			writeBinHeader();
			writeRefrences();
		} else if ("dimension".equals(qName)) {
			setDimension(binCount, sampleCount);
		} else if ("entry".equals(qName)) {
		}
	}

	/**
	 * writes our refrences
	 * 
	 * @author wohlgemuth
	 * @version Jun 7, 2006
	 */
	private void writeRefrences() {
		if (this.statistics == null) {
			if (refrenceTypes != null) {
				if (refrenceTypes.isEmpty() == false) {
					Iterator<String> it = refrenceTypes.keySet().iterator();

					while (it.hasNext()) {
						this.line = refrenceTypes.get(it.next());
						writeLine();
					}
				}
			}
		} else {
			logger.info("refrences are disabled because statistics will be generated!");
		}
	}

	/**
	 * @version Aug 13, 2003
	 * @author wohlgemuth <br>
	 * @see org.xml.sax.ContentHandler#startElement(String,
	 *      String, String, Attributes)
	 */
	public final void startElement(String uri, String localName, String qName,
			Attributes attributes) {

		final String attributeName = attributes.getValue("name");
		if ("sample".equals(qName)) {
			line.set(0, new SampleObject<String>(attributeName,
					attributes));
			final String attributeClass = attributes.getValue("class");
			line.set(1, new ClassObject<String>(attributeClass,
					attributes));
			line.set(2, new ClassObject<String>(attributes.getValue("setupX"),
					attributes));

			if (classStore.contains(attributeClass) == false) {
				classStore.add(attributeClass);
			}
		}
		// write the headers
		else if ("header".equals(qName)) {
			line.set(0, new HeaderFormat<String>("header"));
			line.set(1, new ClassFormat<String>("class"));
			line.set(2, new SetupXFormat<String>("setupX"));

			headerRow.add("header");
			headerRow.add("class");
			headerRow.add("setupX");

			temp = new Vector<Map<String, FormatObject<?>>>(binCount + 3);

			for (int i = 0; i < (binCount + 3); i++) {
				temp.add(null);
			}
		} else {
			final String attributeValue = attributes.getValue("id");
			 int indexOf = 0;
			 
			 if(attributeValue != null){
			
					 indexOf = headerRow.indexOf(attributeValue);
			 }
			if ("entry".equals(qName)) {
				headerRow.add(attributeValue);
				indexOf = headerRow.indexOf(attributeValue);
				
				line.set(indexOf,
						new BinFormat<String>(attributeName,
								attributes));

				Map<String, FormatObject<?>> hash = new HashMap<String, FormatObject<?>>();

				for (int i = 0; i < attributes.getLength(); i++) {
					hash.put(attributes.getQName(i), new BinObject<String>(
							attributes.getValue(i), attributes));
				}

				temp.set(indexOf, hash);

				Map<String, String> binProperties = new HashMap<String, String>();
				int l = attributes.getLength();

				for (int i = 0; i < l; i++) {
					binProperties.put(attributes.getQName(i),
							attributes.getValue(i));
				}

				lastBin = attributeValue;

			}
			// write the numbers
			else if ("bin".equals(qName)) {
				logger.trace("found bin...");
				String value = new String(attributes.getValue(this.getKey())
						.replace(',', '.'));

				try {
					logger.trace("attributes: " + attributeValue + " - "
							+ headerRow.size() + " - "
							+ indexOf);

					if (indexOf < 0) {
						logger.debug("position: "
								+ indexOf
								+ " - "
								+ attributeValue
								+ " - "
								+ attributeName
								+ " ignore this bin since it's related to issue BINBASE-369");
					} else {
						if (Integer.parseInt(attributes.getValue("spectra_id")) == 0) {
							logger.trace("empty annotation!");

							line.set(indexOf,
									null);

						} else if (Double.parseDouble(value) == 0) {
							logger.trace("empty anotation!");
							line.set(
									indexOf,
									new ZeroObject<Double>(Double
											.parseDouble(value), attributes));
							// line.set(headerRow.indexOf(attributes.getValue("id")),
							// null);

						} else if (attributes != null) {
							logger.trace("attributes are not null");
							if (attributes.getValue("problematic").equals("TRUE")) {
								logger.trace("problemeatic anotation!");

								line.set(
										indexOf,
										new ProblematicObject<Double>(Double
												.parseDouble(value), attributes));
							}

							else {
								logger.trace("found successfull anotation: "
										+ attributes.getValue("spectra_id"));
								line.set(
										indexOf,
										new ContentObject<Double>(Double
												.parseDouble(value), attributes));
							}
						}
					}
				} catch (Exception e) {
					logger.warn(e.getMessage(), e);
					line.set(indexOf,
							new ErrorObject<String>(value, attributes));
				}

			} else if ("size".equals(qName)) {

				this.binCount = Integer.parseInt(attributes.getValue("bin"));
				this.sampleCount = Integer.parseInt(attributes.getValue("sample"));
				this.resultId = Integer.parseInt(attributes.getValue("result"));
				this.database = attributes.getValue("database");

				headerRow = new Vector<String>(binCount + 3);

				createVector();
			}

			// save all refrence types in a hasmap
			else if ("refrenceTypes".equals(qName)) {
				refrenceTypes = new HashMap<String, Vector<FormatObject<?>>>();
			}

			// create the content of the hashmap, one refrence entry for each bin
			// for each refrence class
			else if ("refrence".equals(qName)) {
				String name = attributeName;
				String value = (attributes.getValue("value"));
				String url = (attributes.getValue("link"));

				if (refrenceTypes.get(name) == null) {
					Vector<FormatObject<?>> refrences = new Vector<FormatObject<?>>(
							binCount + 3);

					for (int i = 0; i < (binCount + 3); i++) {
						refrences.add(new RefrenceObject<String>(""));
					}
					refrences.set(0, new RefrenceObject<String>(name, url));
					refrenceTypes.put(name, refrences);
				}

				Vector<FormatObject<?>> current = refrenceTypes.get(name);
				current.set(headerRow.indexOf(lastBin), new RefrenceObject<String>(
						value, attributes, url));

			}
		}
	}

	/**
	 * writes the header for bins, if requested
	 */
	public final void writeBinHeader() {
		if (this.statistics == null) {
			Iterator<String> it = this.binHeader.iterator();

			while (it.hasNext()) {
				writeAttribute(it.next().toString());
			}
		} else {
			logger.info("bin header are disabled because statistics will be generated!");
		}
	}

	/**
	 * gibt eine linie der tabelle aus und muss ?von eine kindesklasse
	 * ?berschrieben werden
	 * 
	 * @version Aug 14, 2003
	 * @author wohlgemuth <br>
	 * @param line
	 */
	public abstract void writeLine(List<FormatObject<?>> line);

	/**
	 * DOCUMENT ME!
	 * 
	 * @param binHeader
	 *            DOCUMENT ME!
	 */
	public void setBinHeader(Collection<String> binHeader) {
		this.binHeader = binHeader;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Collection<String> getHeader() {
		return binHeader;
	}

	/**
	 * f?gt eine statistische auswertung hinzu
	 * 
	 * @param stat
	 * @uml.property name="statistics"
	 */
	public void setStatistics(Statistics stat) {
		this.statistics = stat;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param key
	 *            DOCUMENT ME!
	 */
	public void addHeader(String key) {
		this.getHeader().add(key);
	}

	/**
	 * DOCUMENT ME!
	 */
	public void endTransform() {
	}

	/**
	 * @version Aug 29, 2003
	 * @author wohlgemuth <br>
	 * @see org.xml.sax.ErrorHandler#error(SAXParseException)
	 */
	public void error(SAXParseException e) {
		logger.trace("error " + e.getLineNumber() + " " + e.getSystemId() + " "
				+ e.getMessage());
	}

	/**
	 * @version Aug 29, 2003
	 * @author wohlgemuth <br>
	 * @see org.xml.sax.ErrorHandler#fatalError(SAXParseException)
	 */
	public void fatalError(SAXParseException e) {
		logger.trace("fatal " + e.getLineNumber() + " " + e.getSystemId() + " "
				+ e.getMessage());
	}

	/**
	 * @version Aug 29, 2003
	 * @author wohlgemuth <br>
	 * @see org.xml.sax.ErrorHandler#warning(SAXParseException)
	 */
	public void warning(SAXParseException e) {
		logger.trace("warning " + e.getLineNumber() + " " + e.getSystemId()
				+ " " + e.getMessage());
	}

	/**
	 * schreibt die bin properties und muss ?berschrieben werden
	 * 
	 * @param line
	 *            die bin properties als map
	 */
	public void writeBinProperties(Map<String, FormatObject<?>> map) {
	}

	/**
	 * setzt die ausmasse der resultierenden matrix
	 * 
	 * @param binCount
	 * @param sampleCount
	 */
	protected void setDimension(int binCount, int sampleCount) {
	}

	/**
	 * 
	 */
	private void createVector() {
		line = new Vector<FormatObject<?>>(binCount + 3);

		for (int i = 0; i < (binCount + 3); i++) {
			line.add(new NullObject<Double>(0.0));
		}
	}

	/**
	 * @param attribute
	 */
	private void writeAttribute(String attribute) {
		line.set(0, new BinObject<String>(attribute));
		line.set(1, new BinObject<String>(""));
		line.set(2, new BinObject<String>(""));

		Iterator<Map<String, FormatObject<?>>> it = temp.iterator();
		it.next();
		it.next();
		it.next();

		for (int i = 0; it.hasNext(); i++) {
			Map<String, FormatObject<?>> current = it.next();
			line.set(i + 3, current.get(attribute));
		}

		this.writeLine();
	}

	/**
	 * count of rows with refrences
	 * 
	 * @author wohlgemuth
	 * @version Jun 8, 2006
	 * @return
	 */
	public int getRefrenceCount() {
		return this.refrenceTypes.size();
	}

	/**
	 * schreibt eine linie und normalisiert sie falls gew?nscht
	 * 
	 * @version Aug 14, 2003
	 * @author wohlgemuth <br>
	 * @param args
	 */
	private final void writeLine() {
		try {
			if (firstline == false) {
				firstline = true;
			}

			if (statistics != null) {
				cacheStore.add(line);
				createVector();
			} else {
				writeLine(line);
			}

			createVector();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
