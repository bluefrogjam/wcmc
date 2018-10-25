/*
 * Created on Jan 20, 2006
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.export;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.SimpleStatisticProcessor;
import edu.ucdavis.genomics.metabolomics.binbase.bci.export.ExportService;
import edu.ucdavis.genomics.metabolomics.binbase.bci.io.ResultDestination;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.SQLObject;
import edu.ucdavis.genomics.metabolomics.util.io.dest.Destination;
import edu.ucdavis.genomics.metabolomics.util.io.dest.FileDestination;
import edu.ucdavis.genomics.metabolomics.util.io.source.FileSource;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import edu.ucdavis.genomics.metabolomics.util.statistics.StatisticProcessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * exports data using the sql connection to the binbase
 * 
 * @author wohlgemuth
 * @version Jan 20, 2006
 */
public class SQLExportService extends SQLObject implements ExportService {
	StatisticProcessor processor = null;

	public StatisticProcessor getProcessor() {
		return processor;
	}

	public void setProcessor(final StatisticProcessor processor) {
		this.processor = processor;
	}

	/**
	 * calculates the real result id
	 */
	private PreparedStatement getId;

	/**
	 * deletes tmp files
	 */
	public final static boolean DELETE_TMP_FILES = false;

	/**
	 * @author wohlgemuth
	 * @version Jan 20, 2006
	 * @param connection
	 */
	public SQLExportService(final Connection connection) {
		setConnection(connection);

	}

	/**
	 * @author wohlgemuth
	 * @version Jan 20, 2006
	 * @see edu.ucdavis.genomics.metabolomics.binbase.bci.export.ExportService#export(String)
	 */
	public String export(final String name, final String setupXID)
			throws BinBaseException {
		return this.export(name, getID(setupXID));
	}

	/**
	 * @author wohlgemuth
	 * @version Jan 20, 2006
	 * @see edu.ucdavis.genomics.metabolomics.binbase.bci.export.ExportService#export(int)
	 */
	public String export(final String name, final int experimentID)
			throws BinBaseException {
		try {
			return writeData(name, experimentID, true);
		} catch (final Exception e) {
			throw new BinBaseException(e);
		}
	}

	/**
	 * @author wohlgemuth
	 * @version Jan 20, 2006
	 * @see edu.ucdavis.genomics.metabolomics.binbase.bci.export.ExportService#export(String,
	 *      String)
	 */
	public String export(final String name, final String setupXID,
			final Source sop) throws BinBaseException {
		return this.export(name, getID(setupXID), sop, null);
	}

	public String export(final String name, final String setupXID,
			final Source sop, final Destination destination)
			throws BinBaseException {
		return this.export(name, getID(setupXID), sop, destination, true);
	}

	public String export(final String name, final String setupXID,
			final Source sop, final Destination destination,
			final boolean overwrite) throws BinBaseException {
		return this.export(name, getID(setupXID), sop, destination, overwrite);
	}

	/**
	 * @author wohlgemuth
	 * @version Jan 20, 2006
	 * @see edu.ucdavis.genomics.metabolomics.binbase.bci.export.ExportService#export(int,
	 *      String)
	 */
	public String export(final String name, final int experimentID,
			final Source sop, Destination destination, final boolean overwrite)
			throws BinBaseException {
		try {

			final String url = writeData(name, experimentID, overwrite);

			logger.info("wrote data to: " + url);
		

			logger.info("start statistics");

			if (processor == null) {
					processor = new SimpleStatisticProcessor();
				
			}


			if (destination == null) {
				destination = new ResultDestination(name + ".zip");
			}
			final Source source = new FileSource();
			final File file = new File(System.getProperty("java.io.tmpdir")
					+ File.separator + name + ".xml");
			source.setIdentifier(file);

		
			if (source.exist() == false) {
				throw new BinBaseException("source does'nt exist: "
						+ experimentID);
			}

			if (sop.exist() == false) {
				throw new BinBaseException("sop source does'nt exist: " + sop);
			}


			processor.process(source, sop, destination);

			return name + ".zip";
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
			throw new BinBaseException(e);
		}
	}

	/**
	 * writes the rowdata into the byte array
	 * 
	 * @author wohlgemuth
	 * @version Jan 20, 2006
	 * @param id
	 * @param overwrite
	 *            should only be used for testing
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	private String writeData(final String name, final int id,
			final boolean overwrite) throws Exception {

		final FileDestination destination = new FileDestination();
		final File file = new File(System.getProperty("java.io.tmpdir")
				+ File.separator + name + ".xml");
		logger.info("writing data to local temp file: " + file);

		if (DELETE_TMP_FILES) {
			logger.info("temp file will be deleted!");
			file.deleteOnExit();
		} else {
			logger.info("temp file will be kept!");
		}

		boolean overwriteExistingData = true;

		// check if we want to overwrite existing data
		if (overwrite == false) {
			if (file.exists()) {
				logger
						.info("overwrite is disabled and we use the existing file: "
								+ file);
				overwriteExistingData = false;
			} else {
				logger.info("file does not exist yet...");

				overwriteExistingData = true;
			}
		} else {
			if (file.exists()) {
				logger
						.info("overwrite is enabled and we overwrite the existing file: "
								+ file);
			} else {
				logger.info("file does not exist yet...");
			}
			overwriteExistingData = true;

		}

		// we actually write the data
		if (overwriteExistingData) {

			logger.info("start writing...");
			ExportResult result = new ExportResult();

			result.setConnection(getConnection());

			destination.setIdentifier(file);
			result.export(id, new BufferedWriter(new OutputStreamWriter(
					destination.getOutputStream())));
			logger.info("done with writing");
		} else {
			logger
					.info("we are using the cached file and have nothing todo...");
		}
		
		logger.info("result was successfully written to: " + file.getAbsolutePath());
		return name + ".xml";
	}

	/**
	 * @author wohlgemuth
	 * @version Jan 20, 2006
	 * @see edu.ucdavis.genomics.metabolomics.util.SQLObject#prepareStatements()
	 */
	@Override
	protected void prepareStatements() throws Exception {
		super.prepareStatements();
		getId = getConnection().prepareStatement(
				SQL_CONFIG.getValue(CLASS + ".id"));
	}

	/**
	 * calculates the result set id
	 * 
	 * @author wohlgemuth
	 * @version Jan 20, 2006
	 * @param setupX
	 * @return
	 * @throws BinBaseException
	 */
	private int getID(final String setupX) throws BinBaseException {
		logger.info("get correct id for setupX id: " + setupX);
		try {
			getId.setString(1, setupX);
			final ResultSet result = getId.executeQuery();

			if (result.next()) {
				final int id = result.getInt(1);
				logger.info("correct id for setupX id is: " + id);
				result.close();
				return id;
			} else {
				result.close();
				throw new BinBaseException(
						"couldnt find any results for this id, please try again later. id =  "
								+ setupX);
			}
		} catch (final SQLException e) {
			throw new BinBaseException(e);
		}
	}

	/**
	 * by default we allow to overwrite the result
	 */
	public String export(final String name, final int experimentID,
			final Source sop, final Destination destination)
			throws BinBaseException {
		return export(name, experimentID, sop, destination, true);
	}
}
