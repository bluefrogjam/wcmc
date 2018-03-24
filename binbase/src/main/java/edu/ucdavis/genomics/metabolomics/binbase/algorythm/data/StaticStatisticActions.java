package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.naming.NamingException;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;


import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.output.Writer;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.output.XLS;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.action.Action;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.combiner.CombineByMax;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.tool.Processable;
import edu.ucdavis.genomics.metabolomics.binbase.bci.Configurator;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.io.dest.Destination;
import edu.ucdavis.genomics.metabolomics.util.io.dest.FileDestination;
import edu.ucdavis.genomics.metabolomics.util.io.source.ByteArraySource;
import edu.ucdavis.genomics.metabolomics.util.io.source.FileSource;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.ColumnCombiner;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;
import edu.ucdavis.genomics.metabolomics.util.statistics.replacement.NoReplacement;
import edu.ucdavis.genomics.metabolomics.util.statistics.replacement.ZeroReplaceable;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.ToDatafileTransformHandler;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ContentObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.HeaderFormat;
import edu.ucdavis.genomics.metabolomics.util.type.converter.BooleanConverter;

/**
 * class with standard statistic actions. It was a really really bad Idea to
 * design it this way...
 * 
 * @author wohlgemuth
 */
public class StaticStatisticActions {

	/**
	 * should generated source and destination files be deleted
	 */
	private final boolean deleteSourceAndDestinationFiles = false;

	/**
	 * replace the zeros in the datafile
	 * 
	 * @author wohlgemuth
	 * @version Jul 13, 2006
	 * @param file
	 * @return
	 */
	public DataFile replaceZeros(String id, DataFile file, Element element)
			throws Exception {
		ZeroReplaceable replace = null;

		try {
			String method = element.getAttributeValue("method");
			String range = element.getAttributeValue("range");

			getLogger().info(
					"create instance of " + method + " for replacement");

			replace = (ZeroReplaceable) Class.forName(method).newInstance();

			if (replace instanceof BinBaseResultZeroReplaceable) {
				getLogger().info("running initialization method...");
				((BinBaseResultZeroReplaceable) replace)
						.initializeReplaceable();
			}
			if (range.toLowerCase().equals("class")) {
				if (isDataFileValidForReplacement(id, replace, file)) {
					getLogger().info("using class mode");
					file.replaceZeros(replace, 1);
				} else {
					getLogger().info("no zero replacement possible");
				}
			} else if (range.toLowerCase().equals("experiment")) {
				if (isDataFileValidForReplacement(id, replace, file)) {
					getLogger().info("using experiment mode");
					file.replaceZeros(replace, false);
				} else {
					getLogger().info("no zero replacement possible");
				}
			} else if (range.toLowerCase().equals("sample")) {
				if (isDataFileValidForReplacement(id, replace, file)) {
					getLogger().info("using sample mode");
					file.replaceZeros(replace, true);
				} else {
					getLogger().info("no zero replacement possible");
				}
			} else {
				getLogger().warn("replace zeros by using experiment mode!");
				if (isDataFileValidForReplacement(id, replace, file)) {
					getLogger().info("using experiment mode");
					file.replaceZeros(replace, false);
				} else {
					getLogger().info("no zero replacement possible");
				}
			}
			// using predifined folders
			if (element.getAttribute("folder") == null) {
				if (replace instanceof BinBaseResultZeroReplaceable) {

					element.setAttribute(new Attribute("folder",
							((BinBaseResultZeroReplaceable) replace)
									.getFolder()));

				} else {
					if (replace instanceof NoReplacement) {
						// no folder required
					} else {
						element.setAttribute(new Attribute("folder", replace
								.getClass().getSimpleName()));
					}
				}
			}
		} catch (Exception e) {
			getLogger().warn(e.getMessage(), e);
		} finally {
			replace = null;
			System.gc();
		}
		return file;
	}

	/**
	 * runs processing instructions on this file and returns the expsected file.
	 * This can be a completely new file of a differnt type or the original
	 * file.
	 * 
	 * @param file
	 * @param element
	 * @param id
	 * @param folder
	 * @param destiantionIds
	 * @return
	 * @throws Exception
	 */
	public DataFile process(DataFile file, Element element, String id,
			String folder, Collection<String> destiantionIds) throws Exception {
		try {
			getLogger().info("start processing...");

			Processable process = null;

			process = createProcessing(id, destiantionIds, element);

			List<Element> subTasks = element.getChildren("post-processable");

			folder = calculateFolderPathForProcessingInstruction(element,
					folder, process);

			if (subTasks.size() > 0) {
				getLogger()
						.info("sub tasks discovered, cloning datafile and working on these with it's own copy, before continuing on the main file. Any changes now will not be attached to the main file!");
				DataFile internal = (DataFile) file.clone();

				for (Element task : subTasks) {
					internal = process(internal, task, id, folder,
							destiantionIds);
				}

				internal = null;
			}

			if (file instanceof ResultDataFile) {
				file = process.process((ResultDataFile) file, element);
			} else {
				getLogger()
						.info("file was not a result file, we are using instead a simple processing instruction");
				file = process.simpleProcess(file, element);
			}

			// write the calculated result to the file.
			writeProcessingResult(file, element, id, folder, destiantionIds,
					process);

			// free up some memory
			process = null;

		} catch (Exception e) {
			getLogger().warn(e.getMessage(), e);
			return null;

		} finally {
			System.gc();
		}
		getLogger().info("done...");

		/**
		 * return the file for additional processing
		 */
		return file;
	}

	/**
	 * creates our processing object
	 * 
	 * @param id
	 * @param destiantionIds
	 * @param method
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	private Processable createProcessing(String id,
			Collection<String> destiantionIds, Element element)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		String method = element.getAttributeValue("method");

		getLogger().info("create instance of " + method + " for processing");

		Processable process = (Processable) Class.forName(method).newInstance();
		process.setCurrentId(id);
		process.setDestinationIds(destiantionIds);

		return process;
	}

	/**
	 * calculates the local path for the processing result file and sets it to
	 * the processing object
	 * 
	 * @param element
	 * @param folder
	 * @param process
	 */
	private String calculateFolderPathForProcessingInstruction(Element element,
			String folder, Processable process) {
		// using predifined folders
		if (element.getAttribute("folder") == null) {

			List<Element> children = element.getChildren("argument");

			for (Element e : children) {
				getLogger().info(
						"checking: " + e.getAttribute("name").getValue());
				if (e.getAttributeValue("name") != null
						|| e.getAttributeValue("name").equals("folder")) {
					String f = e.getAttributeValue("value");

					if (f == null || f.isEmpty()) {
						element.setAttribute(new Attribute("folder", process
								.getFolder()));
						getLogger().info(
								"configured folder from class: "
										+ process.getFolder());

					} else {
						element.setAttribute(new Attribute("folder", f));
						getLogger()
								.info("configured folder from element: " + f);
					}
				}
			}

			if (element.getAttribute("folder") == null) {
				element.setAttribute(new Attribute("folder", process
						.getFolder()));
				getLogger().info(
						"configured folder from class since no element configuration was found: "
								+ process.getFolder());

			}
		}

		folder = addPath(folder, element.getAttribute("folder").getValue());

		// setting all the required fields for the processing
		process.setCurrentFolder(folder);

		return folder;
	}

	/**
	 * combines 2 pathes
	 * 
	 * @param original
	 * @param sub
	 * @return
	 */
	private String addPath(String original, String sub) {

		if (original.endsWith("/")) {
			if (sub.startsWith("/")) {
				return original + sub.substring(1, sub.length());
			} else {
				return original + sub;
			}
		} else {
			if (sub.startsWith("/")) {
				return original + sub;
			} else {
				return original + "/" + sub;
			}
		}
	}

	/**
	 * writes the processing result to the given folder, if the processable
	 * object allows this
	 * 
	 * @param file
	 * @param element
	 * @param id
	 * @param folder
	 * @param destiantionIds
	 * @param process
	 * @throws IOException
	 */
	private void writeProcessingResult(DataFile file, Element element,
			String id, String folder, Collection<String> destiantionIds,
			Processable process) throws IOException {
		if (file != null) {
			if (process.writeResultToFile()) {
				// write to file
				writeEntry(id, folder, file, element, destiantionIds,
						process.getFileIdentifier());
			} else {
				getLogger()
						.info("result was not supposed to be written to the result, it was just a modification of the data");
			}
		} else {
			getLogger().warn(
					"didn't return a value and so is skipped in the output");
		}
	}

	/**
	 * validate if the datafile is valid
	 * 
	 * @author wohlgemuth
	 * @version Feb 13, 2007
	 * @param replace
	 * @param file
	 * @return
	 */
	public boolean isDataFileValidForReplacement(String id,
			ZeroReplaceable replace, DataFile file) {
		if (replace instanceof BinBaseResultZeroReplaceable) {
			((BinBaseResultZeroReplaceable) replace)
					.setFile((ResultDataFile) file);

			if (((BinBaseResultZeroReplaceable) replace).isValid() == false) {
				getLogger()
						.warn("can't replace zeros with: "
								+ replace.getClass().getName()
								+ " because the class says it is not valid! Please check your configuration");
				return false;
			}
		}
		return true;
	}

	/**
	 * reads the given datafile
	 * 
	 * @param id
	 * @param rawdata
	 * @param cross
	 * @param currentFolder
	 * @param destinationIds
	 * @return
	 * @throws Exception
	 */
	public ResultDataFile readFile(String id, Source rawdata, Element cross,
			String currentFolder, Collection<String> destinationIds)
			throws Exception {
		return readFile(id, rawdata, cross, currentFolder, destinationIds,
				false);
	}

	/**
	 * reads the given datafile
	 * 
	 * @param id
	 * @param rawdata
	 * @param cross
	 * @return
	 * @throws Exception
	 */
	public ResultDataFile readFile(String id, Source rawdata, Element cross)
			throws Exception {
		return readFile(id, rawdata, cross, "data", new Vector<String>(), false);
	}

	/**
	 * reads a blank datafile from the harddisk
	 * 
	 * @author wohlgemuth
	 * @version Jul 13, 2006
	 * @param rawdata
	 * @param cross
	 * @param currentFolder
	 * @param first
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ResultDataFile readFile(String id, Source rawdata, Element cross,
			String currentFolder, Collection<String> destinationIds,
			boolean first) throws Exception {

		// parses the xml content of the file
		ResultDataFile file = parseXMLContent(rawdata, cross);

		getLogger().info(
				"reading file of instance: " + file.getClass().getName());

		double sizeDown = 0;

		if (cross.getAttribute("sizedown") != null) {
			// how much do we want to reduce the dataset size
			sizeDown = Double.parseDouble(cross.getAttributeValue("sizedown"));
		} else {
			sizeDown = 0;
		}

		if (cross.getChild("filter") != null) {
			if (((Element) cross.getChild("filter")).getChildren("bin").size() > 0) {
				getLogger()
						.warn("it was definied that we filter this result by specified bins, ignoring sizedown settings!");
				sizeDown = 0;
			}
		}
		// if there is no reference experiment defiened
		if (cross.getChild("reference") == null) {

			getLogger().info("using normal processing...");
			file = processNormal(cross, sizeDown, file);
		}
		// we have a reference chrommatogram and want to see all the bins in
		// there and don't care if they
		// are empty values or not. so there is no downsizing for this
		// chrommatogram
		// but the reference chrommatogram is downsized
		else {
			getLogger().info("using reference file based processing...");
			file = refrenceBased(cross, sizeDown, rawdata, currentFolder, id,
					destinationIds, first);
		}

		// combines the bins of the same groups
		combineBins(cross, file);

		getLogger().info("initialzing file...");
		file.initialize();
		getLogger().info("done with initializing...");
		return file;
	}

	private ResultDataFile processNormal(Element cross, double sizeDown,
			ResultDataFile file) {
		// filter data
		filterContent(cross, file);

		// removes failed samples if this is wished
		cleanUp(cross, file);

		if (sizeDown > 0) {
			// size the dataset down based on the finding rate for each class
			sizeDown(sizeDown, file);
		}

		return file;
	}

	/**
	 * only exports bins which are found in the reference experiment
	 * 
	 */
	private ResultDataFile refrenceBased(Element cross, double sizeDown,
			Source rawdata, String currentFolder, String id,
			Collection<String> destiantionIds, boolean first)
			throws FileNotFoundException,
			IOException,
			edu.ucdavis.genomics.metabolomics.binbase.bci.server.exception.FileNotFoundException,
			BinBaseException, RemoteException,
			NamingException, FactoryConfigurationError,
			ParserConfigurationException, SAXException {

		getLogger().info("reference mode is enabled");
		Element reference = cross.getChild("reference");

		if (reference.getAttributeValue("experiment") != null) {
			String referenceId = reference.getAttributeValue("experiment");

			getLogger().info("loading: " + rawdata.getSourceName());

			// load our actual datafile needed for this operation
			ResultDataFile file = parseXMLContent(rawdata, cross);

			getLogger().info("using as reference: " + referenceId);

			String server = System.getProperty("java.naming.provider.url");

			// load the reference source
			Source referenceSource = loadReferenceSource(referenceId,
					file.getDatabase(), server);

			// load the reference data file
			ResultDataFile referenceFile = parseXMLContent(referenceSource,
					cross);

			// size down the reference file to find the right number of bins
			// for the given setting
			sizeDown(sizeDown, referenceFile);

			// size down the current file to 0, just to make sure that both
			// are processed the same
			sizeDown(0, file);

			getLogger()
					.info("removing bins and groups which are not in the frefrence file");
			// remove all bins which are not contained in the reference file
			for (HeaderFormat<String> fb : file.getBins(true)) {

				int group = Integer.parseInt(fb.getAttributes().get("group"));

				if (group == 0) {
					if (referenceFile.containsBin(Integer.parseInt(fb
							.getAttributes().get("id"))) == false) {
						file.removeBin(fb);
						getLogger().info(
								"bin was not contained in reference file: "
										+ fb.getValue());
					}
				} else {
					if (referenceFile.containsGroup(group) == false) {
						file.removeBin(fb);
						getLogger().info(
								"bin group was not contained in reference file: "
										+ fb.getValue());

					}
				}
			}

			getLogger()
					.info("adding bins and groups which are not in the file, but found in the reference file");
			// find all bins which are not contained in the file, but contained
			// in the reference file
			for (HeaderFormat<String> rb : referenceFile.getBins(true)) {

				int group = Integer.parseInt(rb.getAttributes().get("group"));

				if (group == 0) {
					if (file.containsBin(Integer.parseInt(rb.getAttributes()
							.get("id"))) == false) {
						getLogger().info(
								"bin was not contained in  file: "
										+ rb.getValue());

						mergeBin(file, referenceFile, rb);
					}
				} else {
					if (file.containsGroup(group) == false) {
						getLogger().info(
								"bin group was not contained in file: "
										+ rb.getValue());
						mergeBin(file, referenceFile, rb);
					}
				}
			}
			// if this is the first call than we should save the reference and
			// unmodified file
			// for debugging
			if (first) {
				// save the modfied file
				writeEntry(id, currentFolder + "/reference/" + referenceId
						+ "/", referenceFile, cross, destiantionIds);

				// just saves a unmodified copy in a subdirectory for
				// comparisons

				ResultDataFile file2 = parseXMLContent(rawdata, cross);
				processNormal(cross, sizeDown, file2);

				writeEntry(id, currentFolder + "/reference/original/", file2,
						cross, destiantionIds);
			}

			return file;
		} else {
			throw new BinBaseException(
					"sorry we need the attribute experiment with the experiment id as value!");
		}
	}

	private void mergeBin(ResultDataFile file, ResultDataFile referenceFile,
			HeaderFormat<String> rb) {
		if (rb.getAttributes().get("standard") != null) {

			// we do not want to merge standards
			if (rb.getAttributes().get("standard").toLowerCase().equals("true")) {

				getLogger().info("a standard not merging...");

			} else {
				getLogger().info("not a standard merging...");

				merge(file, referenceFile, rb);
			}
		} else {
			getLogger().info("merging...");

			merge(file, referenceFile, rb);
		}
	}

	@SuppressWarnings("unchecked")
	private void merge(ResultDataFile file, ResultDataFile referenceFile,
			HeaderFormat<String> rb) {
		int pos = referenceFile.getBinPosition(Integer.parseInt(rb
				.getAttributes().get("id")));
		List list = referenceFile.getColumn(pos);

		pos = file.addEmptyColumn();
		List nc = file.getColumn(pos);

		for (int i = 0; i < nc.size(); i++) {
			if (list.get(i) instanceof ContentObject<?>) {
				nc.set(i, null);
			} else {
				nc.set(i, list.get(i));
			}
		}

		file.setColumn(pos, nc);
	}

	/**
	 * looks in the temporary directory for the result file and if not locally
	 * found it loads it from the server
	 * 
	 * @param referenceId
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws edu.ucdavis.genomics.metabolomics.binbase.bci.server.exception.FileNotFoundException
	 * @throws BinBaseException
	 * @throws RemoteException
	 * @throws NamingException
	 */
	private Source loadReferenceSource(String referenceId, String column,
			String server)
			throws FileNotFoundException,
			IOException,
			edu.ucdavis.genomics.metabolomics.binbase.bci.server.exception.FileNotFoundException,
			BinBaseException, RemoteException, NamingException {

		try {
			getLogger().info(
					"looking in the temporary directory for the file: " + System.getProperty("java.io.tmpdir"));
			Source localSource = new FileSource(new File(
					System.getProperty("java.io.tmpdir") + File.separator
							+ referenceId + ".xml"));

			if (localSource.exist()) {
				return localSource;
			}
		} catch (Exception e) {
			getLogger().info(e.getMessage(), e);
		}

		try {
			getLogger().info("loading it from the server");
			Source referenceSource = new ByteArraySource(Configurator
					.getExportService().getResult(referenceId + ".xml"));

			if (referenceSource.exist()) {
				return referenceSource;
			}
		} catch (Exception e) {
			getLogger().info(e.getMessage(), e);
		}

		getLogger()
				.warn("no reference file found locally or on the server, calculating it on the fly");

		generateReferenceFile(referenceId, column, server);

		return loadReferenceSource(referenceId, column, server);
	}

	/**
	 * runs an internal calculation of the reference file
	 * 
	 */
	private void generateReferenceFile(String referenceFileId, String column,
			String server) {
		/*
		GenerateDSLFromDatabase generate = new GenerateDSLFromDatabase();
		String remoteDSL = generate.dslGenerateForExperiment(referenceFileId,
				column, server, false);

		getLogger().info("generated DSL for reference: \n\n${remoteDSL}");

		LocalExporter exporter = new LocalExporter(remoteDSL);
		exporter.setForceCaching(true);
		exporter.run();

		getLogger().info("finished calculating reference file");
*/
	}

	/**
	 * parses the actual xml content and generate the datafile out of it
	 * 
	 * @param rawdata
	 * @param cross
	 * @return
	 * @throws FactoryConfigurationError
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	@SuppressWarnings("unchecked")
	public ResultDataFile parseXMLContent(Source rawdata, Element cross)
			throws FactoryConfigurationError, IOException,
			ParserConfigurationException, SAXException {
		getLogger().debug("create transfomer");
		ToDatafileTransformHandler handler = new ToDatafileTransformHandler();

		getLogger().debug("calculate parameters");


		List<Element> headers = cross.getChild("header").getChildren("param");

		Iterator<Element> it = headers.iterator();
		while (it.hasNext()) {
			String headerContent = ((Element) it.next())
					.getAttributeValue("value");
			getLogger().debug("defining header: " + headerContent);
			handler.addHeader(headerContent);
		}

		getLogger().info("set key: " + cross.getAttributeValue("attribute"));
		handler.setKey(cross.getAttributeValue("attribute"));

		getLogger().debug("create parser");
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);

		InputStream stream = rawdata.getStream();
		SAXParser builder = factory.newSAXParser();

		getLogger().debug("parse document into handler");
		builder.parse(stream, handler);
		stream.close();

		getLogger().debug("parsing is done");
		stream = null;
		builder = null;
		factory = null;

		System.gc();

		// masks the columns which should be preserved
		DataFile file = maskDatafile(handler, headers);

		if (file instanceof ResultDataFile) {
			((ResultDataFile) file).updateGroupCache();

			((ResultDataFile) file).setResultId(handler.getResultId());
			((ResultDataFile) file).setDatabase(handler.getDatabase());

			return ((ResultDataFile) file);
		} else {
			throw new FactoryConfigurationError(
					"the generated file, was of the wrong datatype: "
							+ file.getClass().getName()
							+ " it needs to be of the type result datafile, so please ensure you have the right factory set!");
		}
	}

	/**
	 * removes all bins from the datafile, which have not been found to at least
	 * n percent
	 * 
	 * @param sizeDown
	 * @param file
	 */
	private void sizeDown(double sizeDown, ResultDataFile file) {
		if (sizeDown <= 0) {
			getLogger().info("no sizedown requested!");
		} else {
			getLogger()
					.info("size of datafile before sizedown "
							+ file.getColumnCount());
			file.sizeDown(true, 1, sizeDown);
			getLogger().info(
					"size of datafile after sizedown " + file.getColumnCount());
		}
	}

	private void cleanUp(Element cross, ResultDataFile file) {
		// remove unnecessary samples
		if (cross.getAttribute("keepFailed") != null) {
			try {
				boolean failed = cross.getAttribute("keepFailed")
						.getBooleanValue();

				if (failed == false) {
					file.removeFailedSamples();
				}

			} catch (Exception e) {
				getLogger().error(e.getMessage(), e);
			}
		} else {
			getLogger().info("attribute keepFailed not found");
		}
	}

	private DataFile maskDatafile(ToDatafileTransformHandler handler,
			List<Element> headers) {
		DataFile file = handler.getFile();

		// ignore the first n+1 lines
		int[] rows = new int[headers.size() + 1 + handler.getRefrenceCount()];

		for (int i = 0; i < rows.length; i++) {
			rows[i] = i;
		}

		file.setIgnoreRows(rows);

		// ignore the first two columns containning the class and
		// samplename,setupXId from statistics
		file.setIgnoreColumns(new int[] { 0, 1, 2 });
		return file;
	}

	@SuppressWarnings("unchecked")
	private void filterContent(Element cross, ResultDataFile file) {
		getLogger().info("filtering data");
		if (cross.getChild("filter") != null) {
			Element filter = cross.getChild("filter");

			List<Element> bins = filter.getChildren("bin");
			List<Element> samples = filter.getChildren("sample");

			filterBins(file, bins, false);

			filterSamples(file, samples);
		} else {
			getLogger().info("no filter defined!");
		}
	}

	private void combineBins(Element cross, DataFile file) {
		// combine wrongly generated bins
		if (cross.getAttribute("combine") != null) {
			String className = cross.getAttributeValue("combine");
			getLogger().info("combine datasets using: " + className);

			try {
				getLogger().info(
						"size of datafile before combined: "
								+ file.getColumnCount());

				if (BooleanConverter.StringtoBoolean(cross.getAttribute(
						"combine").getValue()) == true) {
					getLogger().info("combine columns --> enabled");
					file.combineColumns(new CombineByMax());
				} else if ((cross.getAttribute("combine").getValue())
						.toLowerCase().equals("false")) {
					getLogger().info("combine columns --> disabled");
				} else {
					getLogger().info(
							"combine columns --> enabled which specific combiner "
									+ className);
					ColumnCombiner combiner = (ColumnCombiner) Class.forName(
							className).newInstance();
					if (combiner.isConfigNeeded()) {
						getLogger().info("set configuration of combiner");
						combiner.setConfig(cross.getChild("combiner"));
					}
					file.combineColumns(combiner);
				}
				getLogger().info(
						"size of datafile after combined: "
								+ file.getColumnCount());
			} catch (Exception e) {
				getLogger().error(e.getMessage(), e);
			}
		} else {
			getLogger().info("attribute combine not found!");
		}
	}

	/**
	 * filter samples out of the result file
	 * 
	 * @author wohlgemuth
	 * @version Nov 2, 2006
	 * @param file
	 * @param samples
	 */
	public void filterSamples(ResultDataFile file, List<Element> samples) {
		if (samples != null) {
			if (samples.isEmpty()) {
				return;
			}
			getLogger().info(
					"size of datafile before removing samples: "
							+ file.getRowCount());
			List<String> keep = new Vector<String>();

			for (int i = 0; i < samples.size(); i++) {
				Element b = samples.get(i);
				if (b.getAttribute("match") != null) {
					keep.add(b.getAttributeValue("match"));
				} else {
					getLogger().debug(
							"attribute match not defined for sample, ignore");
				}
			}

			file.filterSamples(keep);
			getLogger().info(
					"size of datafile after removing samples: "
							+ file.getColumnCount());
		} else {
			getLogger().info("samples will not be filtered");
		}
	}

	/**
	 * filter bins out of the result file
	 * 
	 * @author wohlgemuth
	 * @version Nov 2, 2006
	 * @param file
	 * @param bins
	 */
	public void filterBins(ResultDataFile file, List<Element> bins,
			boolean ignoreGroupedBins) {
		if (bins != null) {
			if (bins.isEmpty()) {
				return;
			}
			getLogger().info(
					"size of datafile before removing bins: "
							+ file.getColumnCount());
			List<String> keep = new Vector<String>();

			for (int i = 0; i < bins.size(); i++) {
				Element b = bins.get(i);
				if (b.getAttribute("match") != null) {
					getLogger().info(
							"keeping bin: " + b.getAttributeValue("match"));
					keep.add(b.getAttributeValue("match"));
				} else {
					getLogger().warn(
							"attribute match not defined for bin, ignore");
				}
			}

			getLogger().info("keeping bins: " + keep.size());

			file.filterBins(keep, ignoreGroupedBins);

			getLogger().info(
					"size of datafile after filtering bins: "
							+ file.getBins().size());

			getLogger().info("done with filtering...");

		} else {
			getLogger().info("bins will not be filtered");
		}
	}

	public void writeEntry(String id, String fileName, final Object current,
			Element data, Collection<String> destiantionIds) throws IOException {
		writeEntry(id, fileName, current, data, destiantionIds, "result");
	}

	/**
	 * write a result file to the zipfile
	 * 
	 * @author wohlgemuth
	 * @version Nov 19, 2005
	 * @param current
	 *            the content to copy to the outputstream
	 * @throws IOException
	 */
	public void writeEntry(String id, String fileName, final Object current,
			Element data, Collection<String> destiantionIds, String identifier)
			throws IOException {

		if (data.getChildren("format").size() == 0) {
			Element e = new Element("format");
			e.setAttribute("type", XLS.class.getName());
			tableOutput(id, fileName, current, e, destiantionIds, identifier);
		} else {
			for (Element e : (List<Element>) data.getChildren("format")) {
				if (e.getAttribute("type").getValue().equals("NULL") == false) {
					tableOutput(id, fileName, current, (Element) e,
							destiantionIds, identifier);
				}
			}
		}
	}

	public void writeOutput(String id, String fileName, final Object current,
			Collection<String> destinationIds, String identifier, Writer writer) {

		if (writer != null) {

			try {

				OutputStream stream = createOutputStream(id, fileName,
						destinationIds, identifier, writer);

				try {
					if (writer.isDatafileSupported()) {
						if (current instanceof DataFile) {
							writer.write(stream, (DataFile) current);
						} else {
							writer.write(stream, current);
						}
					} else if (writer.isSourceSupported()) {
						if (current instanceof Source) {
							writer.write(stream, (Source) current);
						} else if (current instanceof DataFile) {
							if (writer.isDatafileSupported() == false) {
								writer.write(stream, new Source() {

									public void configure(Map<?, ?> p)
											throws ConfigurationException {
									}

									public boolean exist() {
										return true;
									}

									public String getSourceName() {
										return "datfile converted to input stream";
									}

									public InputStream getStream()
											throws IOException {
										return ((DataFile) current)
												.toInputStream();
									}

									public long getVersion() {
										return 0;
									}

									public void setIdentifier(Object o)
											throws ConfigurationException {
									}
								});
							}
						} else {
							writer.write(stream, current);
						}
					} else {
						writer.write(stream, current);
					}

				} catch (Exception ex) {
					getLogger().error(ex.getMessage(), ex);
				}

				stream.flush();
				stream.close();

			} catch (Exception ex) {
				getLogger().error(ex.getMessage(), ex);
			}
		}
	}

	/**
	 * @author wohlgemuth
	 * @version Feb 13, 2007
	 * @param fileName
	 * @param current
	 * @param e
	 * @throws IOException
	 */
	public void tableOutput(String id, String fileName, final Object current,
			Element e, Collection<String> destinationIds, String identifier)
			throws IOException {
		String format = e.getAttributeValue("type");

		Writer writer = null;

		if (format.indexOf(".") > -1) {
			// represents a class
			try {
				writer = (Writer) Class.forName(format).newInstance();
			} catch (InstantiationException e1) {
				getLogger().error(e1.getMessage(), e1);
			} catch (IllegalAccessException e1) {
				getLogger().error(e1.getMessage(), e1);
			} catch (ClassNotFoundException e1) {
				getLogger().error(e1.getMessage(), e1);
			}
		} else {
			// represents a short cut to a class in the package of the
			// writers
			try {
				getLogger().info(
						"writing out put using: "
								+ Writer.class.getPackage().getName() + "."
								+ format);
				writer = (Writer) Class.forName(
						Writer.class.getPackage().getName() + "." + format)
						.newInstance();
			} catch (InstantiationException e1) {
				getLogger().error(e1.getMessage(), e1);
			} catch (IllegalAccessException e1) {
				getLogger().error(e1.getMessage(), e1);
			} catch (ClassNotFoundException e1) {
				getLogger().error(e1.getMessage(), e1);
			}
		}

		writeOutput(id, fileName, current, destinationIds, identifier, writer);

	}

	private OutputStream createOutputStream(String id, String fileName,
			Collection<String> destinationIds, String identifier, Writer writer)
			throws ConfigurationException, IOException {
		getLogger().info("using id: " + id);
		getLogger().info("using filename: " + fileName);
		if (fileName == null) {
			fileName = "/";
		}
		if (fileName.startsWith("/") == false) {
			fileName = "/" + fileName;
		}
		if (fileName.endsWith("/") == false) {
			fileName = fileName + "/";
		}

		String internalId = id + fileName + identifier + "."
				+ writer.toString();

		getLogger().info("final filename: " + internalId);
		destinationIds.add(internalId);
		Destination dest = createDestination(id + fileName);

		dest.setIdentifier(identifier + "." + writer.toString());

		OutputStream stream = dest.getOutputStream();
		return stream;
	}

	/**
	 * creates a destination with the given id we basically store subdirectories
	 * and stuff like this there anf once its done we all zip it to one big file
	 * 
	 * @param id
	 * @return
	 */
	private Destination createDestination(String id) {
		File file = new File("result" + File.separator + id);
		if (deleteSourceAndDestinationFiles) {
			file.deleteOnExit();
		}
		getLogger().info("storing content at: " + file.getAbsolutePath());
		file.mkdirs();
		getLogger().info("location exist: " + file.exists());
		return new FileDestination(file.getAbsolutePath());
	}

	/**
	 * transform the data into a table
	 * 
	 * @author wohlgemuth
	 * @version Feb 20, 2007
	 * @param cross
	 * @param rawdata
	 * @param id
	 * @param id
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void transform(Element cross, Source rawdata, String id,
			Collection<String> destiantionIds, boolean singleTransformation)
			throws Exception {

		String currentFolder = "";

		if (cross.getAttributeValue("folder") == null) {
			getLogger().info(
					"using folder ==> " + cross.getAttributeValue("attribute"));

			if (!singleTransformation) {
				currentFolder = cross.getAttributeValue("sizedown") + "/";
			}

			currentFolder = currentFolder
					+ cross.getAttributeValue("attribute");
		} else {
			getLogger().info(
					"using folder ==> " + cross.getAttributeValue("folder"));

			if (!singleTransformation) {
				currentFolder = cross.getAttributeValue("sizedown") + "/";
			}

			currentFolder = currentFolder + cross.getAttributeValue("folder");
		}

		DataFile file = null;

		try {
			file = readFile(id, rawdata, cross, currentFolder, destiantionIds,
					true);
		} catch (NullPointerException e) {
			getLogger().warn("couldn't find key (likley)", e);
			return;
		}

		getLogger().info("process rawdata");

		// write rawdata output
		Element rawdataOut = cross.getChild("rawdata");

		if (rawdataOut != null) {
			String folder = rawdataOut.getAttributeValue("folder");

			if (folder == null) {
				folder = "rawdata";
			}
			writeEntry(id, currentFolder + "/" + folder, file, rawdataOut,
					destiantionIds);
		}
		// replace zeros

		getLogger().info("generate statistics");

		// contains all the calibration instructions, if we have this enabled
		Element calibration = cross.getChild("calibration");

		// calculate the statistics and apply the calibration
		for (Element statistic : (List<Element>) cross
				.getChildren("processing")) {
			List<Element> stat = statistic.getChildren("zero-replacement");
			List<Element> proc = statistic.getChildren("processable");

			// does zero replacing
			getLogger().info("doing generic zero replacement work");

			// apply calibration even if there are no replaced values
				calculateStatistics(cross, rawdata, id, destiantionIds,
						currentFolder, stat, calibration,
						(DataFile) file.clone());

			// post processing
			getLogger()
					.info("do proccessing after zero replacement, this does not affect the replaced output and the used data file will have no replacements in this step!");
			processing(cross, rawdata, id, destiantionIds, currentFolder, proc,
					(DataFile) file.clone());

		}
	}

	/**
	 * does the calculation of statistics for all the settings
	 * 
	 * @param cross
	 * @param rawdata
	 * @param id
	 * @param destiantionIds
	 * @param currentFolder
	 * @param stat
	 * @throws Exception
	 * @throws IOException
	 */
	private void calculateStatistics(Element cross, Source rawdata, String id,
			Collection<String> destiantionIds, String currentFolder,
			List<Element> stat, Element calibration, DataFile dataFile)
			throws Exception, IOException {

		String folder = currentFolder;
		for (Element element : stat) {

			List<Element> prePro = element.getChildren("pre-processable");
			List<Element> postPro = element.getChildren("post-processable");

			// get datafile
			DataFile current = dataFile;// readFile(id, rawdata, cross,
										// currentFolder,destiantionIds);

			if (element.getAttributeValue("folder") != null) {
				folder = addPath(currentFolder,
						element.getAttributeValue("folder"));

			} else {
				folder = addPath(currentFolder, "replace");
			}

			// does the datafile processing before the zero replacement
			DataFile preProcessed = chainedPreProcessing(id, destiantionIds,
					folder, element, prePro, current);

			if (preProcessed != null) {
				current = preProcessed;
			} else {
				getLogger()
						.warn("preProcessing returned null, continue with original datafile!");
			}
			// replace zeros

			current = replaceZeros(id, current, element);

			getLogger()
					.info("found " + postPro.size()
							+ " post processing instructions");

			// does the datafile post processing for the zero replacement
			chainedPostProcessing(id, destiantionIds, folder, element, postPro,
					current);

			current = null;
			System.gc();
		}
	}

	/**
	 * calculates a relative path where to put this file and is used to 'move'
	 * files up in the hirachy
	 * 
	 * @param currentFolder
	 * @param element
	 * @return
	 */
	private String changeFileHirachy(String currentFolder, Element config) {
		String writeFolder = currentFolder;

		getLogger().info("current element: " + config.getName());

		List<Element> children = config.getChildren("argument");

		if (children != null) {
			// find a child which has the right attribute
			for (Element element : children) {
				try {
					getLogger().info(
							"current argument: " + element.getName() + " - "
									+ element.getAttributes());

					// let's see if we want to move the result file up in the
					// hirachy of
					// our folders
					if (element.getAttributeValue("name") != null
							&& element.getAttributeValue("name").equals(
									"moveUp")) {
						getLogger().info("base folder: " + currentFolder);

						int foldersToMove = element.getAttribute("value")
								.getIntValue();

						getLogger().info("moving folders: " + foldersToMove);
						for (int i = 1; i < foldersToMove; i++) {
							writeFolder = writeFolder + "../";
						}
						getLogger().info("generated file path: " + writeFolder);

						return writeFolder;
					} else {
						getLogger().info(
								"attribute 'moveUp' not found, moving on in element: "
										+ element.getName());
					}
				} catch (Exception e) {
					getLogger().warn(
							"was not able to move the file upstairs: "
									+ e.getMessage(), e);
					return currentFolder;
				}
			}
		} else {
			getLogger().info("no configuration found... => skip");
		}
		return writeFolder;
	}


	/**
	 * runs a chained pre processing over the datafile and builds the initial
	 * file for the netcdf replacement
	 * 
	 * @param id
	 * @param destiantionIds
	 * @param currentFolder
	 * @param element
	 * @param prePro
	 * @param current
	 * @return
	 * @throws Exception
	 */
	private DataFile chainedPreProcessing(String id,
			Collection<String> destiantionIds, String currentFolder,
			Element element, List<Element> prePro, DataFile current)
			throws Exception {
		getLogger().info(
				"found " + prePro.size() + " pre processing instructions");
		// does the datafile pre processing for the zero replacement
		for (Element e : prePro) {
			getLogger().info("doing generic pre processing work");

			String writeFolder = changeFileHirachy(addPath(currentFolder, "pre-process"), e);

			DataFile result = process(current, e, id, writeFolder,
					destiantionIds);

			if (result != null) {
				current = result;
			}
		}
		return current;
	}

	/**
	 * runs a chained post processing and each result will be used as the input
	 * file for the next processing isntruction
	 * 
	 * @param id
	 * @param destiantionIds
	 * @param currentFolder
	 * @param element
	 * @param postPro
	 * @param current
	 * @throws Exception
	 */
	private void chainedPostProcessing(String id,
			Collection<String> destiantionIds, String currentFolder,
			Element element, List<Element> postPro, DataFile current)
			throws Exception {

		String folder = addPath(currentFolder, "post-process");

		for (Element e : postPro) {
			getLogger().info("doing generic post processing work");

			// allows us to place the elemnt into the current folder and not 15
			// sub folders

			String writeFolder = changeFileHirachy(folder, e);

			try {
				DataFile result = process(current, e, id, writeFolder,
						destiantionIds);

				if (result != null) {
					current = result;
				}

			} catch (Throwable ex) {
				getLogger().error(ex.getMessage(), ex);
			}
		}
	}

	/**
	 * converts a source to an element
	 * 
	 * @param sop
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 */
	public Element readSource(Source sop) throws JDOMException, IOException {
		Document sopDefinition = new SAXBuilder().build(sop.getStream());
		Element root = sopDefinition.getRootElement();
		return root;
	}

	/**
	 * doing generic processing work. The result will be chained into the next
	 * direction
	 * 
	 * @param cross
	 * @param rawdata
	 * @param id
	 * @param destiantionIds
	 * @param currentFolder
	 * @param proc
	 * @throws Exception
	 * @throws IOException
	 */
	private void processing(Element cross, Source rawdata, String id,
			Collection<String> destiantionIds, String currentFolder,
			List<Element> proc, DataFile dataFile) throws Exception,
			IOException {
		getLogger().info("found " + proc.size() + "  processing instructions");
		for (Element element : proc) {
			getLogger().info("doing generic processing work");
			DataFile current = dataFile;// readFile(id, rawdata, cross,
										// currentFolder,destiantionIds);

			process(current, element, id, addPath(currentFolder, "process"), destiantionIds);

			current = null;
			System.gc();
		}
	}

	/**
	 * creates the final zip file
	 */
	public void createFinalZip(Destination destination, String id,
			Collection<String> entryIds) throws Exception {
		getLogger().info("create zip file for id: " + id);
		ZipOutputStream ou = new ZipOutputStream(destination.getOutputStream());
		byte[] buf = new byte[1024];

		for (String file : entryIds) {
			getLogger().info("working on: " + file);
			String name = file.substring(file.indexOf(id) + id.length() + 1,
					file.length());
			getLogger().info("generated internal name: " + name);

			Source source = createSource(file);
			InputStream in = source.getStream();

			getLogger().info("write...");
			ou.putNextEntry(new ZipEntry(name));

			int len;
			while ((len = in.read(buf)) > 0) {
				ou.write(buf, 0, len);
			}

			in.close();
			ou.closeEntry();

		}
		ou.flush();
		ou.close();
		getLogger().info("done with zip creation");
	}

	/**
	 * creates a source to access an entry
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Source createSource(String id) throws Exception {
		File file = new File("result" + File.separator + id);
		if (deleteSourceAndDestinationFiles) {
			file.deleteOnExit();
		}
		getLogger().info("retrieving content from: " + file.getAbsolutePath());
		getLogger().info("location exist: " + file.exists());
		return new FileSource(file);
	}

	private Logger getLogger() {
		return LoggerFactory.getLogger(getClass());
	}

	/**
	 * executes an action, which can happen before or after the transformation
	 * 
	 * @param cross
	 * @param rawdata
	 * @param id
	 * @param destiantionIds
	 * @param sop
	 */
	public void action(Element cross, Source rawdata, String id,
			Collection<String> destiantionIds, Source sop,
			List<Element> transformInstructions) throws Exception {
		try {
			String className = cross.getAttributeValue("method");

			String column = cross.getAttributeValue("column");

			Action action = (Action) Class.forName(className).newInstance();

			action.setTransformInstructions(transformInstructions);
			action.setColumn(column);
			action.setCurrentId(id);
			action.setDestinationIds(destiantionIds);

			getLogger().info("running action: " + className);
			action.run(cross, rawdata, sop);
		} catch (Exception e) {
			getLogger().error(e.getMessage(), e);
		}
	}
}
