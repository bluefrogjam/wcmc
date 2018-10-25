package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.tool;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.annotation.Ignore;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.ResultDataFile;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.action.Action;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.database.ConnectionFactory;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;
import org.slf4j.Logger;
import org.jdom.Element;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * executes sql statements against the current database
 * 
 * @author wohlgemuth
 */
@Ignore
public class SQLExecutor extends BasicProccessable implements Processable, Action {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private String column;
	private List<Element> transformInstructions;

	protected List<Element> getTransformInstructions() {
		return transformInstructions;
	}
	
	@Override
	public boolean writeResultToFile() {
		return false;
	}

	@Override
	public void setTransformInstructions(List<Element> transformInstructions) {
		this.transformInstructions = transformInstructions;
	}

	@Override
	public String getFolder() {
		return "sql";
	}

	@Override
	public DataFile process(ResultDataFile datafile, Element configuration) throws BinBaseException {
		try {

			String column = datafile.getDatabase();
			execute(configuration, column);

			return null;
		}
		catch (Exception e) {
			throw new BinBaseException(e);
		}
	}

	private void execute(Element configuration, String column) throws BinBaseException, RemoteException, CreateException, NamingException, SQLException {
		List<Element> statements = configuration.getChild("argument").getChildren();

		ConnectionFactory factory = ConnectionFactory.getFactory();

		Connection connection = createDatabaseConnection(column, factory);

		try {
			for (Element el : statements) {
				String s = el.getText().trim();

				try {

					PreparedStatement statement = connection.prepareStatement(s);
					statement.execute();
					statement.close();
				}

				catch (SQLException e) {
					logger.warn("exception during execution of statment: " + s, e);
				}
			}
		}
		finally {
			factory.close(connection);
		}
	}

	@Override
	public void run(Element configuration, Source rawdata, Source sop) {
		try {
			execute(configuration, this.column);
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	@Override
	public void setColumn(String column) {
		this.column = column;
	}

	public String getDescription(){
		return "executes custom sql statements against the database";
	}

}
