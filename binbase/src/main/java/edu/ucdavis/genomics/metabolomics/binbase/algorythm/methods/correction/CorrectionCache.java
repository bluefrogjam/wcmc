package edu.ucdavis.genomics.metabolomics.binbase.algorythm.methods.correction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;

import edu.ucdavis.genomics.metabolomics.util.SQLObject;
import edu.ucdavis.genomics.metabolomics.util.math.CombinedRegression;
import edu.ucdavis.genomics.metabolomics.util.math.Regression;

/**
 * used to store correction data in the database
 * 
 * @author wohlgemuth
 */
public class CorrectionCache extends SQLObject {

	private PreparedStatement deleteExistingData;

	private PreparedStatement insertData;

	private PreparedStatement returnData;

	private PreparedStatement inCache;

	private Logger logger = LoggerFactory.getLogger(getClass());

	private PreparedStatement correctedWith;

	private PreparedStatement binIds;

	/**
	 * stores the current informations in the sample cache
	 * 
	 * @param sampleId
	 *            sample id on which this sample was applied
	 * @param binIds
	 *            binds of the curve
	 * @param x
	 *            x values
	 * @param y
	 *            y values
	 * @throws SQLException
	 */
	public void cache(int sampleId, int[] binIds, double[] x, double[] y) throws SQLException {

		logger.debug("caching curve for: " + sampleId + " with hits: " + x.length);

		this.deleteExistingData.setInt(1, sampleId);
		this.deleteExistingData.execute();

		for (int i = 0; i < x.length; i++) {
			this.insertData.setInt(1, sampleId);
			this.insertData.setInt(2, binIds[i]);
			this.insertData.setDouble(3, x[i]);
			this.insertData.setDouble(4, y[i]);
			this.insertData.setInt(5, i);
			this.insertData.execute();
		}

		if (isCached(sampleId) == false) {
			throw new SQLException("caching was not executed...");
		}
	}

	public boolean isCached(int sampleId) throws SQLException {
		logger.debug("checking cache for: " + sampleId);

		inCache.setInt(1, sampleId);
		ResultSet set = inCache.executeQuery();

		boolean result = set.next();

		logger.debug("in cache: " + result);
		set.close();

		return result;
	}

	/**
	 * returns the regression of the given sample id
	 * 
	 * @param sampleId
	 * @return
	 * @throws SQLException
	 */
	public Regression getRegression(int sampleId) throws SQLException {
		return getRiVsRTRegression(sampleId);
	}

	/**
	 * X: is the standard Y: is the uncorrected
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Regression getRiVsRTRegression(int sampleId) throws SQLException {
		logger.debug("query curve for sample: " + sampleId);

		Regression poly = new CombinedRegression();

		this.returnData.setInt(1, sampleId);

		ResultSet set = returnData.executeQuery();

		List<Double> x = new ArrayList<Double>();
		List<Double> y = new ArrayList<Double>();

		while (set.next()) {
			x.add(set.getDouble(1));
			y.add(set.getDouble(2));

		}

		set.close();

		double xA[] = new double[x.size()];
		double yA[] = new double[y.size()];

		for (int i = 0; i < x.size(); i++) {
			xA[i] = x.get(i);
			yA[i] = y.get(i);
		}

		logger.debug("x/y: " + x.size() + "/" + y.size());
		poly.setData(xA, yA);

		logger.debug("curve is: " + poly.toString());
		return poly;
	}

	/**
	 * Y: is the standard X: is the uncorrected
	 * 
	 * @param sampleId
	 * @return
	 * @throws SQLException
	 */
	public Regression getRTvsRIRegression(int sampleId) throws SQLException {
		logger.debug("query curve for sample: " + sampleId);

		Regression poly = new CombinedRegression();

		this.returnData.setInt(1, sampleId);

		ResultSet set = returnData.executeQuery();

		List<Double> x = new ArrayList<Double>();
		List<Double> y = new ArrayList<Double>();

		while (set.next()) {
			y.add(set.getDouble(1));
			x.add(set.getDouble(2));

		}

		set.close();

		double xA[] = new double[x.size()];
		double yA[] = new double[y.size()];

		for (int i = 0; i < x.size(); i++) {
			xA[i] = x.get(i);
			yA[i] = y.get(i);
		}

		logger.debug("x/y: " + x.size() + "/" + y.size());
		poly.setData(xA, yA);

		logger.debug("curve is: " + poly.toString());
		return poly;
	}

	/**
	 * returns a list of bin ids which were used for correction in this sample
	 * 
	 * @param sampleId
	 * @return
	 * @throws SQLException
	 */
	public List<Integer> getBinIdsInCorrectionForSample(int sampleId) throws SQLException {

		binIds.setInt(1, sampleId);
		ResultSet result = binIds.executeQuery();
		
		List<Integer> res = new Vector<Integer>();
		
		while(result.next()){
			res.add(result.getInt(1));
		}
		result.close();
		
		return res;
	}

	public int getCorrectionId(int sampleId) throws SQLException {
		this.correctedWith.setInt(1, sampleId);

		ResultSet set = correctedWith.executeQuery();

		boolean value = set.next();

		if (value) {
			int id = set.getInt(1);
			logger.debug("correction id is " + id + "  for " + sampleId);
			set.close();

			return id;
		}
		else {
			throw new SQLException("no data found for: " + sampleId);
		}
	}

	@Override
	protected void prepareStatements() throws Exception {
		super.prepareStatements();

		deleteExistingData = this.getConnection().prepareStatement("delete from correction_data where sample_id = ?");

		insertData = this.getConnection().prepareStatement("insert into correction_data(sample_id,bin_id,x,y,position) values(?,?,?,?,?)");

		returnData = this.getConnection().prepareStatement("select x,y from correction_data where sample_id = ? order by position");

		inCache = this.getConnection().prepareStatement("select x from correction_data where sample_id = ?");

		correctedWith = this.getConnection().prepareStatement("select \"correctedWith\" from samples where sample_id = ?");
		
		binIds = this.getConnection().prepareStatement("select bin_id from correction_data where sample_id = ? order by position");
	}

}
