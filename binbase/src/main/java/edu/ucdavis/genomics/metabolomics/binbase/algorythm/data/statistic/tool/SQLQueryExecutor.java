package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.tool;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.jdom2.Element;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.annotation.Ignore;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.ResultDataFile;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.action.Action;
import edu.ucdavis.genomics.metabolomics.binbase.bci.Configurator;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.SimpleDatafile;

/**
 * executes sql statements against the current database
 * 
 * @author wohlgemuth
 */
@Ignore
public class SQLQueryExecutor extends BasicProccessable implements Processable,
		Action {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private String column;

	/**
	 * internal counter
	 */
	private static int counter = 0;

	private List<Element> transformInstructions;

	@Override
	public boolean writeResultToFile() {
		return false;
	}

	protected List<Element> getTransformInstructions() {
		return transformInstructions;
	}

	@Override
	public void setTransformInstructions(List<Element> transformInstructions) {
		this.transformInstructions = transformInstructions;
	}

	@Override
	public String getFolder() {
		return "sql";
	}

	protected synchronized int increaseCounter() {
		return counter++;
	}

	@Override
	public DataFile process(ResultDataFile datafile, Element configuration)
			throws BinBaseException {

		this.column = datafile.getDatabase();

		DataFile file = execute(configuration);

		try {
			writeObject(file, configuration, "queryResult_" + increaseCounter());
		} catch (IOException e) {
			throw new BinBaseException(e);
		}

		return null;
	}

	private DataFile execute(Element configuration) throws BinBaseException {
		try {

			List<Element> statements = configuration.getChild("argument")
					.getChildren();

			ConnectionFactory factory = ConnectionFactory.getFactory();

			Properties p = Configurator.getDatabaseService().getProperties();
			p.setProperty(SimpleConnectionFactory.KEY_USERNAME_PROPERTIE,
					column);
			Connection connection = factory.getConnection();

			int x = 0;

			try {
				for (Element el : statements) {
					String s = el.getText().trim();

					x++;
					try {

						PreparedStatement statement = connection
								.prepareStatement(s);

						ResultSet rs = statement.executeQuery();

						// Get the metadata
						ResultSetMetaData md = rs.getMetaData();

						SimpleDatafile result = new SimpleDatafile();

						// Print the column labels
						for (int i = 1; i <= md.getColumnCount(); i++) {
							result.addEmptyColumn(md.getColumnLabel(i));
						}

						// Loop through the result set
						int y = 1;
						while (rs.next()) {

							result.addEmptyRow("");

							for (int i = 1; i <= md.getColumnCount(); i++) {
								result.setCell(i - 1, y, rs.getObject(i));
							}

							y++;
						}

						result.addEmptyRow("");
						result.addEmptyRow("");
						result.addEmptyRow("");

						result.setCell(0, result.getRowCount() - 2, "query");
						result.setCell(0, result.getRowCount() - 1, s);

						writeObject(result, configuration, "query_" + x);
						// Close the result set, statement and the connection
						rs.close();
						statement.close();
					}

					catch (SQLException e) {
						logger.warn("exception during execution of statment: "
								+ s, e);
					}
				}
			} finally {
				factory.close(connection);
			}

			return null;
		} catch (Exception e) {
			throw new BinBaseException(e);
		}
	}

	@Override
	public void run(Element configuration, Source rawdata, Source sop) {
		try {
			execute(configuration);
		} catch (BinBaseException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void setColumn(String column) {
		this.column = column;
	}

	public String getDescription() {
		return "executes sql queries against the database";
	}
}
