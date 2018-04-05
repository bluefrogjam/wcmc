/*
 * Created on Jun 10, 2005
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.version;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.version.handler.InvalidVersionException;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.exception.HeaderProblemException;
import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;

import org.slf4j.Logger;
import org.jdom.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wohlgemuth base class to transform different pegasus header in one
 *         conform header.
 */
public class Header {
	private static final String VERSION = "PEGASUS_VERSION";
	/**
	 * contains the configurations
	 */
	private Element element;

	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * transforms the pegasus header in a binbase header
	 * 
	 * @param header
	 * @return
	 * @throws ConfigurationException
	 * @throws HeaderProblemException 
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> transform(String[] header, String[] data) throws ConfigurationException, HeaderProblemException {
		List<Element> list = this.getElement().getChildren("entry");
		Map<String, String> map = new HashMap<String, String>();

		map.put(VERSION, String.valueOf(getVersion()));
		if (header.length == data.length) {

			for (int i = 0; i < list.size(); i++) {
				Element e = (Element) list.get(i);
				String binbase = e.getAttributeValue("binbase");
				String specifiedFileHeader = e.getAttributeValue("file");

				boolean found = false;

				for (int x = 0; x < header.length; x++) {
					if (header[x].equals(specifiedFileHeader)) {
						map.put(binbase, data[x]);
						x = header.length + 1;
						found = true;
					}
				}

				if (found == false) {
					throw new InvalidVersionException("element: " + binbase + " not in configuration found");
				}
			}
		}
		else {
			logger.warn("the count of headers is different than the count of data, skipping!");
			throw new HeaderProblemException("sorry header size is not equal to the data size, we are skipping this line! " + header.length + "/" + data.length);
		}

		return map;
	}

	/**
	 * sets the configuration of this header
	 * 
	 * @param element
	 */
	public void setElement(Element element) {
		this.element = element;
	}

	/**
	 * returns the configuration of this header
	 * 
	 * @return
	 */
	public Element getElement() {
		return element;
	}

	public int getVersion() {
		if (this.getElement() == null) {
			return -1;
		}
		return Integer.parseInt(this.getElement().getAttributeValue("id"));
	}

	public String getType(){
		return this.getElement().getAttributeValue("type");
	}
}
