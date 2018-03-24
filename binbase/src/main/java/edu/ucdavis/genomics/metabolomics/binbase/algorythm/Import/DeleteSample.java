/*
 * Created on Aug 9, 2006
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import;

import edu.ucdavis.genomics.metabolomics.util.SQLObject;
import org.slf4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * only use during import!!!
 * @author wohlgemuth
 *
 */
class DeleteSample extends SQLObject {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private PreparedStatement deleteFromBin;
	private PreparedStatement deleteFromSamples;
	private PreparedStatement deleteFromSpectra;
	private PreparedStatement updateSpectra;
	private PreparedStatement deleteFromComments;
	private PreparedStatement calculateLatestVersion;
	private PreparedStatement getSampleName;
	private PreparedStatement updateSampleVersion;
	private PreparedStatement selectNonFinishedSamples;
	private PreparedStatement selectNonVisibleSamples;

	protected void delete(int sampleId) throws SQLException {

		//get sample name
		this.getSampleName.setInt(1, sampleId);
		
		ResultSet result = this.getSampleName.executeQuery();
		String sampleName = null;
		int version = 0;
		if(result.next()){
			sampleName = result.getString(1);
		}
		else{
			throw new SQLException("sample not found!");
		}
				
		logger.debug("update spectra table");
		//update spectra to bin_id = null similiarty = 0 which where generated from a bin based on this sample
		this.updateSpectra.setInt(1, sampleId);
		this.updateSpectra.execute();
		//delete generated bins from this sample
		logger.debug("delete generated bins");
		this.deleteFromBin.setInt(1, sampleId);
		this.deleteFromBin.execute();
		// delete spectra of this samples
		logger.debug("delete spectra");
		this.deleteFromSpectra.setInt(1,sampleId);
		this.deleteFromSpectra.execute();
		// delete quantifactation of this sampples
		
		// delete comments for the given sample
		logger.debug("delete comments");
		this.deleteFromComments.setInt(1, sampleId);
		this.deleteFromComments.execute();
		// delete sample table
		logger.debug("delete sample");
		this.deleteFromSamples.setInt(1, sampleId);
		this.deleteFromSamples.execute();
		//set sample with last version visible
		
		//calculate latest version
		this.calculateLatestVersion.setString(1, sampleName);
		result = this.calculateLatestVersion.executeQuery();
		if(result.next()){
			version = result.getInt(1);
		}
		
		logger.debug("set latest version visible");
		this.updateSampleVersion.setString(1, sampleName);
		this.updateSampleVersion.setInt(2, version);
		
		this.updateSampleVersion.execute();
		//done
		logger.debug("done");
	}

	/**
	 * cleans not finished samples, should never be used!
	 * @throws SQLException 
	 *
	 */
	protected void cleanNotFinishedSamples() throws SQLException{
		ResultSet result = this.selectNonFinishedSamples.executeQuery();
		
		while(result.next()){
			logger.info("working on sample id: " + result.getInt(1));
			this.delete(result.getInt(1));
		}
	}
	
	/**
	 * remove all not visible samples which does not generated bins
	 * @throws SQLException
	 */
	public void cleanNotVisibleSamples() throws SQLException{
		ResultSet result = this.selectNonVisibleSamples.executeQuery();
		
		while(result.next()){
			logger.info("working on sample id: " + result.getInt(1));
			this.delete(result.getInt(1));
		}
	}
	
	@Override
	protected void prepareStatements() throws Exception {
		super.prepareStatements();
		deleteFromBin = this.getConnection().prepareStatement("delete from bin where \"sample_id\" =?");
		deleteFromSamples = this.getConnection().prepareStatement("delete from samples where \"sample_id\" =?");
		deleteFromSpectra = this.getConnection().prepareStatement("delete from spectra where \"sample_id\" =?");
		updateSpectra = this.getConnection().prepareStatement("update spectra set \"bin_id\" = null, \"match\" = null where \"bin_id\" in (select \"bin_id\" from bin where \"sample_id\" = ?)");
		
		deleteFromComments = this.getConnection().prepareStatement("delete from comments where discriminator = 5 and type = ?");
		calculateLatestVersion = this.getConnection().prepareStatement("select max(\"version\") from samples where \"sample_name\" = ?");
		getSampleName = this.getConnection().prepareStatement("select \"sample_name\" from samples where \"sample_id\" = ?");
		updateSampleVersion = this.getConnection().prepareStatement("update samples set \"visible\" = 'TRUE' where \"sample_name\" = ? and \"version\" = ?");
		this.selectNonFinishedSamples = this.getConnection().prepareStatement("select \"sample_id\" from samples where \"finished\" = 'FALSE' and \"sample_id\" not in ( select \"sample_id\" from bin)");
		this.selectNonVisibleSamples = this.getConnection().prepareStatement("select \"sample_id\" from samples where \"visible\" = 'FALSE' and \"finished\" = 'TRUE' and \"sample_id\" not in ( select \"sample_id\" from bin)");
	}

}
