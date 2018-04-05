/*
 * Created on 05.06.2003
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.methods;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.AlgorythmHandler;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.Matchable;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.result.DatabaseResultHandler;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.result.ResultHandler;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.DiagnosticsService;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.DiagnosticsServiceFactory;
import org.jdom.DataConversionException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author wohlgemuth
 */
public abstract class AbstractMethod  implements Methodable {
	/**
	 * DOCUMENT ME!
	 * 
	 * @uml.property name="algorythmHandler"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	protected AlgorythmHandler algorythmHandler;

	/**
	 * DOCUMENT ME!
	 * 
	 * @uml.property name="resultHandler"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	protected ResultHandler resultHandler;

	/**
	 * DOCUMENT ME!
	 */
	protected boolean prepared = false;

	/**
	 * DOCUMENT ME!
	 */
	protected int sampleId;

	/**
	 * DOCUMENT ME!
	 * 
	 * @uml.property name="match"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	private Matchable match;

	public boolean isAllowMatching() {
		return allowMatching;
	}

	public void setAllowMatching(boolean allowMatching) {
		this.allowMatching = allowMatching;
	}

	/**
	 * DOCUMENT ME!
	 */
	private boolean allowMatching = true;

	/**
	 * DOCUMENT ME!
	 */
	private boolean correctionFailed = false;

	private boolean binNewAllowed;

	private boolean overrideAlgorythmHandler = false;

	private String sampleName;

	protected String getSampleName() {
		return sampleName;
	}

	private PreparedStatement getSampleName;

	/**
	 * @param correctionFailed
	 *            The correctionFailed to set.
	 * @uml.property name="correctionFailed"
	 */
	public final void setCorrectionFailed(boolean correctionFailed) {
		this.correctionFailed = correctionFailed;

		try {
			this.allowMatching = CONFIG
					.getElement("values.correctionFailed.matchSample")
					.getAttribute("value").getBooleanValue();
			logger.info("in case of ri correction failure matching is allowed = "
					+ this.allowMatching);
		} catch (DataConversionException e) {
			logger.error("cant convert value disable matching", e);
			this.allowMatching = true;
		}
	}

	@Override
	protected void prepareStatements() throws Exception {
		super.prepareStatements();
		this.getSampleName = this.getConnection().prepareStatement(
				"select sample_name from samples where sample_id = ?");
	}

	/**
	 * @return Returns the correctionFailed.
	 * @uml.property name="correctionFailed"
	 */
	public final boolean isCorrectionFailed() {
		return correctionFailed;
	}

	/**
	 * @see edu.ucdavis.genomics.metabolomics.binbase.binlib.algorythm.util.transform.abstracthandler.algorythm.binlib.database.Methodable#setSampleId(int)
	 * @uml.property name="sampleId"
	 */
	public final void setSampleId(int id) {
		logger.debug("setting sample_id: " + id);
		this.setCorrectionFailed(false);
		this.getMatchable().setConfig(this.getConfig().getElement("values"));
		this.sampleId = id;
		try {
			this.sampleName = generateSampleName(this.sampleId);
		} catch (SQLException e) {
			logger.warn("error: " + e.getMessage(), e);
			this.sampleName = e.getMessage();
		}
	}

	/**
	 * generate the sample name
	 * 
	 * @param sampleId
	 * @return
	 * @throws SQLException
	 */
	private String generateSampleName(int sampleId) throws SQLException {

		this.getSampleName.setInt(1, sampleId);

		ResultSet result = this.getSampleName.executeQuery();

		result.next();
		this.sampleName = result.getString(1);
		result.close();

		return sampleName;
	}

	/**
	 * f???hrt die klasse aus
	 * 
	 * @throws Exception
	 */
	public final void run() throws Exception {
		this.initLogging();

		this.getMatchable().setConnection(this.getConnection());
		this.prepareAlgorythmHandler();
		this.prepareResultHandler();
		this.getMatchable().setSampleId(this.getSampleId());

		if (this.allowMatching) {
			logger.debug("matching is allowed");

			this.resultHandler.setNewBinAllowed(this.binNewAllowed);
			this.start();
			this.resultHandler.flush();
		}

		this.getMatchable().flush();

	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public AlgorythmHandler getAlgorythmHandler() {
		return algorythmHandler;
	}

	/**
	 * @return
	 */
	public void setMatchable(Matchable match) {
		this.match = match;
	}

	/**
	 * @see edu.ucdavis.genomics.metabolomics.binbase.binlib.algorythm.methods.Methodable#getMatchable()
	 */
	public Matchable getMatchable() {
		return this.match;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public ResultHandler getResultHandler() {
		return resultHandler;
	}

	/**
	 * @throws Exception
	 */
	protected final void prepareAlgorythmHandler() throws Exception {
		if (this.overrideAlgorythmHandler == false) {
			this.algorythmHandler = (AlgorythmHandler) Class.forName(
					CONFIG.getElement("class.algorythm").getAttributeValue(
							"value")).newInstance();
		} else {
			logger.info("user decided to provide its own algorythm handler, ignore the default handler!");
		}

		this.algorythmHandler.setConnection(this.getConnection());
		this.getMatchable().setAlgorythmHandler(this.algorythmHandler);
	}

	/**
	 * @throws Exception
	 */
	protected final void prepareResultHandler() throws Exception {
		this.resultHandler = new DatabaseResultHandler();
		this.resultHandler.setConnection(this.getConnection());
		this.getMatchable().setResultHandler(this.resultHandler);

	}

	/**
	 * kann ueberschrieben werden und tut nichts weiter als die methode zu
	 * starten
	 * 
	 * @see edu.ucdavis.genomics.metabolomics.binbase.binlib.algorythm.util.transform.abstracthandler.alg
	 *      <orythm.binlib.database.AbstractMethod#start()
	 */
	protected void start() {
		logger.debug("using algorythm handler: " + this.getAlgorythmHandler());
		logger.info("start matching...");
		this.getMatchable().run();
	}

	/**
	 * @author wohlgemuth
	 * @version Nov 3, 2005
	 * @see Methodable#setNewBinAllowed(boolean)
	 */
	public void setNewBinAllowed(boolean value) {
		this.binNewAllowed = value;
	}

	public void setAlgorythmHandler(AlgorythmHandler algorythmHandler) {
		overrideAlgorythmHandler = true;
		this.algorythmHandler = algorythmHandler;
		this.getMatchable().setAlgorythmHandler(this.algorythmHandler);
	}

	public int getSampleId() {
		return sampleId;
	}

	@Override
	public DiagnosticsService getDiagnosticsService() {
		return service;
	}

	private DiagnosticsService service = DiagnosticsServiceFactory
			.newInstance().createService();
}
