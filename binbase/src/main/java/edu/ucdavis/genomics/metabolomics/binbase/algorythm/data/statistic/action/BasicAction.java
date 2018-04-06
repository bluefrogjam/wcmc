package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.action;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.StaticStatisticActions;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.output.*;
import edu.ucdavis.genomics.metabolomics.binbase.bci.Configurator;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.database.ConnectionFactory;
import edu.ucdavis.genomics.metabolomics.util.database.SimpleConnectionFactory;
import org.jdom.Element;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * a basic action class to easy the implementation
 * 
 * @author wohlgemuth
 * 
 */
public abstract class BasicAction implements Action {

	/**
	 * acton object to help us with some stuff
	 */
	protected StaticStatisticActions action = new StaticStatisticActions();
	/**
	 * used database column of this experiment
	 */
	private String column;

	/**
	 * the current folder in the dataset
	 */
	private String currentFolder;

	private String id;

	private List<Element> transformInstructions;

	private Collection<String> destinationIds;

	protected Collection<String> getDestinationIds() {
		return destinationIds;
	}

	protected void setId(String id) {
		this.id = id;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public void setCurrentFolder(String folder) {
		this.currentFolder = folder;
	}

	public void setCurrentId(String id) {
		this.id = id;
	}

	public void setDestinationIds(Collection<String> destinationIds) {
		this.destinationIds = destinationIds;
	}

	public void setTransformInstructions(List<Element> transformInstructions) {
		this.transformInstructions = transformInstructions;
	}

	protected String getColumn() {
		return column;
	}

	protected String getCurrentFolder() {
		return currentFolder;
	}

	protected String getId() {
		return id;
	}

	protected List<Element> getTransformInstructions() {
		return transformInstructions;
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

		action.writeEntry(id, currentFolder, content, configuration,
				destinationIds, identifier);
	}

	/**
	 * writes an object to the result file
	 * 
	 * @throws IOException
	 */
	public void writeText(Object content, Element configuration,
			String identifier, String filename) throws IOException {
		if (currentFolder == null) {
			currentFolder = getFolder();
		}

		action.writeOutput(id, filename, content, destinationIds, identifier,
				new TXT());
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

		action.writeOutput(id, currentFolder, content, destinationIds,
				identifier, writer);
	}


	public void writePNGImage(BufferedImage content, String identifier)
			throws IOException {
		if (currentFolder == null) {
			currentFolder = getFolder();
		}
		action.writeOutput(id, currentFolder, content, destinationIds,
				identifier, new PNG());
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

		action.writeEntry(id, currentFolder + "/" + subFolder, content,
				configuration, destinationIds, identifier);
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

		action.writeOutput(id, currentFolder + "/" + subFolder, content,
				destinationIds, identifier, writer);
	}

	/**
	 * writes an object to the result file
	 * 
	 * @throws IOException
	 */
	public void writeJPEGImage(BufferedImage content, String identifier,
			String subFolder) throws IOException {
		if (currentFolder == null) {
			currentFolder = getFolder();
		}

		action.writeOutput(id, currentFolder + "/" + subFolder, content,
				destinationIds, identifier, new PNG());
	}

	public void writeCDFFile(InputStream content, String identifier,
			String subFolder) throws IOException {
		if (currentFolder == null) {
			currentFolder = getFolder();
		}

		action.writeOutput(id, currentFolder + "/" + subFolder, content,
				destinationIds, identifier, new CDF());
	}

	public void writeTXTFile(String content, String identifier, String subFolder)
			throws IOException {
		if (currentFolder == null) {
			currentFolder = getFolder();
		}

		action.writeOutput(id, currentFolder + "/" + subFolder, content,
				destinationIds, identifier, new TXT());
	}

	public void writePNGImage(BufferedImage content, String identifier,
			String subFolder) throws IOException {
		if (currentFolder == null) {
			currentFolder = getFolder();
		}

		action.writeOutput(id, currentFolder + "/" + subFolder, content,
				destinationIds, identifier, new PNG());
	}

	protected Connection createDatabaseConnection(String column,
			ConnectionFactory factory) throws BinBaseException,
			RemoteException, NamingException, CreateException {
		Properties p = Configurator.getDatabaseService().getProperties();
		p.setProperty(SimpleConnectionFactory.KEY_USERNAME_PROPERTIE, column);
		Connection connection = factory.getConnection();
		return connection;
	}
}
