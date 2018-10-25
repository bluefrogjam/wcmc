package edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.anotation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.Semaphore;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateSpectra;
import edu.ucdavis.genomics.metabolomics.util.SQLable;

/**
 * is used to validate that certain bins are really the massspecs with want. For
 * example kestose and raffinose have very similar massspecs, except that the
 * ratio between 2 ions is different. You need to define these ratios in
 * bellerophon.
 * 
 * <binId>/<mainIon>/<compareIon>/<ratio>
 * 
 * @author wohlgemuth
 * 
 */
public class IonFilter extends BasicFilter implements Filter, SQLable {

	private Connection connection;

	private PreparedStatement selectRatio;

	private Semaphore lockingFilter;

	public IonFilter() {
		lockingFilter = new Semaphore(1);
	}

	/**
	 * to be accepted all the ions needs to be passed, wont run in a
	 * multithreaded mode, since the generation of connections will take longer
	 * than the execution. Hence the use of semaphores to avoid this
	 */
	protected boolean compare(Map<String, Object> bin,
			Map<String, Object> unknown) throws MatchingException {

		try {

			lockingFilter.acquire();

			int binId = Integer.parseInt(bin.get("bin_id").toString());
			int spectraId = Integer.parseInt(unknown.get("spectra_id").toString());

			double[][] spectra = FilterUtilities.calculateMassSpec(unknown);

			try {
				if(logger.isDebugEnabled()) {
					logger.debug("fetching ratios for: " + bin.get("name"));
				}
				this.selectRatio.setInt(1, binId);
				ResultSet result = this.selectRatio.executeQuery();

				boolean hasRatio = false;

				while (result.next()) {
					hasRatio = true;

					int mainIon = result.getInt("main_ion");
					int secondaeryIon = result.getInt("secondaery_ion");
					
					double minRatio = result.getDouble("min_ratio");
					double maxRatio = result.getDouble("max_ratio");

					double mainIonIntensity = spectra[mainIon - 1][ValidateSpectra.FRAGMENT_ABS_POSITION];
					double secondaeryIonIntensity = spectra[secondaeryIon - 1][ValidateSpectra.FRAGMENT_ABS_POSITION];

					double ionRatio = mainIonIntensity / secondaeryIonIntensity;

					if (isDebugEnabled()) {
						logger.debug("main ion: " + mainIon);
						logger.debug("main intensity: " + mainIonIntensity);

						logger.debug("sec ion: " + secondaeryIon);
						logger.debug("sec intensity: " + secondaeryIonIntensity);

						logger.debug("min ratio: " + minRatio);
						logger.debug("max ratio: " + maxRatio);

						logger.debug("mainIntensity/secondaeryIntensity: " + ionRatio);

						logger.debug("filter properties: " + mainIon + "/"
								+ secondaeryIon + " - " + mainIonIntensity
								+ "/" + secondaeryIonIntensity + " - "
								+ minRatio + "/" + maxRatio + "/" + ionRatio);
					}

					if (ionRatio > maxRatio) {
						this.setReasonForRejection("the ion ratio was to large");
						getDiagnosticsService().diagnosticActionFailed(spectraId, binId, this.getClass(), "filtering by ion ratio", "the ion ratio was to large",new Object[]{ionRatio,maxRatio});

						// if the ratio is larger than the max ratio this ratio
						// failed
						return false;
					}
					if (ionRatio < minRatio) {
						this.setReasonForRejection("the ion ratio was to small");
						getDiagnosticsService().diagnosticActionFailed(spectraId, binId, this.getClass(), "filtering by ion ratio", "the ion ratio was to small",new Object[]{ionRatio,minRatio});
						// if ratio smaller than the defined min ratio this
						// filter
						// failed
						return false;
					}
				}

				if (hasRatio == false) {
					getDiagnosticsService().diagnosticActionSuccess(spectraId, binId, this.getClass(), "filtering by ion ratio", "there were no ratio defiend, so it's always successful",new Object[]{});

					if(logger.isDebugEnabled()) {
						logger.debug("no ratios defined, accept by default");
					}
					return true;
				}
			} catch (SQLException e) {
				throw new MatchingException(e);
			}

			if(logger.isDebugEnabled()) {
				logger.debug("accepted the massspec!");
			}
			getDiagnosticsService().diagnosticActionSuccess(spectraId, binId, this.getClass(), "filtering by ion ratio", "massspec passed all available ion ratios",new Object[]{});

		} catch (InterruptedException e) {
			logger.warn(e.getMessage(), e);
		} finally {
			lockingFilter.release();
		}
		// ok all filters are ok so we accept it
		
		return true;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		if(logger.isDebugEnabled()) {
			logger.debug("setting connection");
		}
		this.connection = connection;
		try {
			this.selectRatio = this.connection.prepareStatement(SQL_CONFIG
					.getValue(this.getClass().getName() + ".select"));
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
