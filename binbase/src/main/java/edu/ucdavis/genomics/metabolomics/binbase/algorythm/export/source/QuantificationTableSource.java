/*
 * Created on Nov 17, 2005
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.export.source;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.config.SQLConfigable;
import edu.ucdavis.genomics.metabolomics.util.io.source.DatabaseSource;

/**
 * reads from the destination table
 * @author wohlgemuth
 * @version Nov 17, 2005
 *
 */
public class QuantificationTableSource extends DatabaseSource implements SQLConfigable{
	private Integer identifier;
	
	private Logger logger = LoggerFactory.getLogger(getClass().getName());
	
	/**
	 * needed to get the given value
	 */
	private PreparedStatement select;

	protected ResultSet executeStatement() throws SQLException {
		logger.debug("query for id: " + this.identifier);
		select.setInt(1,this.identifier.intValue());
		
		return select.executeQuery();
	}

	public String getSourceName() {
		return String.valueOf(this.identifier);
	}

	protected void prepareStatements() throws SQLException {
		this.select = this.getConnection().prepareStatement(SQL_CONFIG.getValue(this.getClass().getName() + ".select"));
	}

	
	public void setIdentifier(Object o) throws ConfigurationException {
		if(o instanceof Integer){
			this.identifier = (Integer) o;
		}
		else{
			this.identifier = new Integer(o.hashCode());
		}
	}
}
