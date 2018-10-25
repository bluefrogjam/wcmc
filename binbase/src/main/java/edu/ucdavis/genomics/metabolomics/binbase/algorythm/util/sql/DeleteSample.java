/*
 * Created on Aug 9, 2006
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.util.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;

import edu.ucdavis.genomics.metabolomics.util.SQLObject;

/**
 * only use during import!!!
 * @author wohlgemuth
 *
 */
public class DeleteSample extends SQLObject {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private PreparedStatement deleteFromBin;
	private PreparedStatement deleteFromSamples;
	private PreparedStatement deleteFromSpectra;
	private PreparedStatement deleteFromQuantification;
	private PreparedStatement deleteFromComments;
	private PreparedStatement calculateLatestVersion;
	private PreparedStatement getSampleName;
	
	public DeleteSample(Connection connection) {
		this.setConnection(connection);
	}

	public void delete(int sampleId) throws SQLException {

		//get sample name
		
		// delete generated bins from this sample

		// delete comments from this spectra

		// update spectra to bin_id = null similiarty = 0 which where generated from a bin based on this sample
		
		// delete spectra of this samples

		// delete quantifactation of this sampples
		
		// delete sample table

		//set sample with last version visible
		
		//done
		
	}

	@Override
	protected void prepareStatements() throws Exception {
		super.prepareStatements();
		
		this.deleteFromBin = getConnection().prepareStatement("");
		this.deleteFromSpectra = getConnection().prepareStatement("");
		this.deleteFromQuantification = getConnection().prepareStatement("");
		this.deleteFromSamples = getConnection().prepareStatement("");
	}
}
