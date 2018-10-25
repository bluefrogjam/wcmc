package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.tool;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.StaticStatisticActions;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.output.PNG;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.output.Writer;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;

/**
 * basic processable class
 * 
 * @author wohlgemuth
 */
public abstract class BasicProccessable implements Processable {

	private Logger logger = LoggerFactory.getLogger(getClass());

	protected StaticStatisticActions action = new StaticStatisticActions();
	
	@Override
	public String getFileIdentifier() {
		return "result";
	}

	@Override
	public DataFile simpleProcess(DataFile datafile, Element configuration)
			throws BinBaseException {
		logger.info("nothing done in the default implementation");
		return null;
	}

	private String id;

	protected String getId() {
		return id;
	}

	private String currentFolder;
	private Collection<String> destinationIds;

	@Override
	public void setCurrentFolder(String folder) {
		this.currentFolder = folder;
	}

	@Override
	public void setCurrentId(String id) {
		this.id = id;
	}

	public void setDestinationIds(Collection<String> destinationIds) {
		this.destinationIds = destinationIds;
	}

	/**
	 * writes an object to the result file
	 * 
	 * @throws IOException
	 */
	public void writeObject(Object content, Element configuration,
			String identifier) throws IOException {
		if (currentFolder == null) {
			currentFolder = getFolder();
		}

		action.writeEntry(id, currentFolder, content,
				configuration, destinationIds, identifier);
	}

	/**
	 * writes an object to the result file
	 * 
	 * @throws IOException
	 */
	public void writeObject(Object content, String identifier, Writer writer)
			throws IOException {
		if (currentFolder == null) {
			currentFolder = getFolder();
		}

		action.writeOutput(id, currentFolder, content,
				destinationIds, identifier, writer);
	}

	/**
	 * writes an object to the result file
	 * 
	 * @throws IOException
	 */
	public void writeJPEGImage(BufferedImage content, String identifier)
			throws IOException {
		if (currentFolder == null) {
			currentFolder = getFolder();
		}

		action.writeOutput(id, currentFolder, content,
				destinationIds, identifier, new PNG());
	}

	public void writePNGImage(BufferedImage content, String identifier)
			throws IOException {
		if (currentFolder == null) {
			currentFolder = getFolder();
		}
		action.writeOutput(id, currentFolder, content,
				destinationIds, identifier, new PNG());
	}

	/**
	 * writes an object to the result file
	 * 
	 * @throws IOException
	 */
	public void writeObject(Object content, Element configuration,
			String identifier, String subFolder) throws IOException {
		if (currentFolder == null) {
			currentFolder = getFolder();
		}

		action.writeEntry(id, currentFolder + "/" + subFolder,
				content, configuration, destinationIds, identifier);
	}

	/**
	 * writes an object to the result file
	 * 
	 * @throws IOException
	 */
	public void writeObject(Object content, String identifier, Writer writer,
			String subFolder) throws IOException {
		if (currentFolder == null) {
			currentFolder = getFolder();
		}

		action.writeOutput(id, currentFolder + "/" + subFolder,
				content, destinationIds, identifier, writer);
	}

	public void writePNGImage(BufferedImage content, String identifier,
			String subFolder) throws IOException {
		if (currentFolder == null) {
			currentFolder = getFolder();
		}

		action.writeOutput(id, currentFolder + "/" + subFolder,
				content, destinationIds, identifier, new PNG());
	}

	/**
	 * should the result be written to a file
	 * 
	 * @return
	 */
	public boolean writeResultToFile() {
		return true;
	}
}
