
package edu.ucdavis.genomics.metabolomics.binbase.dsl.processable

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.ResultDataFile
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.StaticStatisticActions
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.action.Action
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.tool.BasicProccessable
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.tool.Processable
import edu.ucdavis.genomics.metabolomics.binbase.bci.Configurator
import edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx.DatabaseJMXFacade
import edu.ucdavis.genomics.metabolomics.binbase.bdi.util.hibernate.HibernateFactory
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException
import edu.ucdavis.genomics.metabolomics.util.database.ConnectionFactory
import edu.ucdavis.genomics.metabolomics.util.database.DriverUtilities
import edu.ucdavis.genomics.metabolomics.util.io.source.Source
import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile
import org.slf4j.Logger
import org.jdom.Element

import java.sql.Connection

/**
 * basic support for sql statments
 * @author wohlgemuth
 *
 */
abstract class SQLProcessable extends BasicProccessable implements Action,Processable{
	
	private Logger logger = LoggerFactory.getLogger(getClass())
	
	private String column
	
	private List<Element> transformInstructions;
	
	protected List<Element> getTransformInstructions() {
		return transformInstructions;
	}
	
	
	@Override
	public void setTransformInstructions(List<Element> transformInstructions) {
		this.transformInstructions = transformInstructions;
	}
	
	@Override
	public void run(Element configuration, Source rawdata, Source sop) {
		logger.info "reading datafile for this class: ${getClass().getName()}} - id: ${this.getId()}}"
		
		//we assume we want to work on the default transformation
		ResultDataFile current = StaticStatisticActions.readFile(this.getId(), rawdata, getTransformInstructions()[0]);
		process (current, configuration)
	}
	
	@Override
	public void setColumn(String column) {
		this.column = column
	}
	
	@Override
	public DataFile process(ResultDataFile datafile, Element configuration)
	throws BinBaseException {
		
		ConnectionFactory factory = ConnectionFactory.getFactory();
		
		Connection connection = createDatabaseConnection(column, factory);
		
		
		try {
			return processWithSQL (datafile, connection,configuration)
		}
		finally {
			factory.close(connection);
		}
	}
	
	/**
	 * initialize the hibernate framework in case it's required
	 */
	protected void initializeHibernate(){
		
		final DatabaseJMXFacade facade = Configurator.getDatabaseService();
		
		Properties p = new Properties();
		
		p.setProperty(ConnectionFactory.KEY_USERNAME_PROPERTIE, this.column);
		p.setProperty(ConnectionFactory.KEY_DATABASE_PROPERTIE,
				facade.getDatabase());
		p.setProperty(ConnectionFactory.KEY_HOST_PROPERTIE, facade.getDatabaseServer());
		p.setProperty(ConnectionFactory.KEY_TYPE_PROPERTIE, DriverUtilities.POSTGRES.toString());
		p.setProperty(ConnectionFactory.KEY_PASSWORD_PROPERTIE, facade.getDatabaseServerPassword());
		
		
		p.put("hibernate.cache.use_minimal_puts", "false");
		p.put("hibernate.cache.use_query_cache", "false");
		p.put("hibernate.cache.use_second_level_cache", "false");
		
		HibernateFactory.newInstance(p);
	}
	
	/**
	 * processes this datafile with an sql connection
	 * @param dataFile
	 * @param connection
	 * @return
	 */
	protected abstract DataFile processWithSQL(ResultDataFile dataFile, Connection connection,Element configuration)
}
