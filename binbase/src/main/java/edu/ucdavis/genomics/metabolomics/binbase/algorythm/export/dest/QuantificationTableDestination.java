/*
 * Created on Nov 17, 2005
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.export.dest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.config.SQLConfigable;
import edu.ucdavis.genomics.metabolomics.util.io.dest.DatabaseDestination;

/**
 * returns objects from the quantification table
 * @author wohlgemuth
 * @version Nov 17, 2005
 *
 */
public class QuantificationTableDestination extends DatabaseDestination implements SQLConfigable{
	/**
	 * needed to insert new data
	 */
	private PreparedStatement insert;
	
	/**
	 * needed to check if data exist and if we override them
	 */
	private PreparedStatement select;
	
	/**
	 * need to override data
	 */
	private PreparedStatement update;
	
	/**
	 * the current version
	 */
	private PreparedStatement version;
	
	
	private Integer identifier;

	public void setIdentifier(Object o) throws ConfigurationException {
		if(o instanceof Integer){
			this.identifier = (Integer) o;
		}
		else{
			this.identifier = new Integer(o.hashCode());
		}
	}

	protected void prepareStatements() throws SQLException  {
		this.insert = this.getConnection().prepareStatement(SQL_CONFIG.getValue(this.getClass().getName() + ".insert"));
		this.update = this.getConnection().prepareStatement(SQL_CONFIG.getValue(this.getClass().getName() + ".update"));
		this.select = this.getConnection().prepareStatement(SQL_CONFIG.getValue(this.getClass().getName() + ".select"));
		this.version = this.getConnection().prepareStatement(SQL_CONFIG.getValue(this.getClass().getName() + ".version"));

	}
	
	/**
	 * 
	 * @author wohlgemuth
	 * @version Nov 18, 2005
	 * @see edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.dest.DatabaseDestination#executeInsert(ByteArrayOutputStream)
	 */
	protected void executeInsert(ByteArrayOutputStream stream) throws SQLException {

		ResultSet set = version.executeQuery();
		set.next();
		int version = set.getInt(1);
	
		set.close();
		
		select.setInt(1,this.identifier.intValue());
		set = select.executeQuery();
		
		if(set.next()){
			//need update
			update.setBinaryStream(1,new ByteArrayInputStream(stream.toByteArray()), stream.toByteArray().length);
			update.setInt(2,version);
			update.setInt(3,this.identifier.intValue());			
			update.execute();
		}
		else{
			//need insert
			insert.setInt(1,version);
			insert.setInt(2,this.identifier.intValue());
			insert.setBinaryStream(3,new ByteArrayInputStream(stream.toByteArray()), stream.toByteArray().length);			
			insert.execute();
		}

		set.close();
	}
}
