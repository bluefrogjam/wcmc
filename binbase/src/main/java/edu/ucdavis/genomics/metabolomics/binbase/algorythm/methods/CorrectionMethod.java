/*
 *
 * Created on 10.06.2003
 *
 * To change the template for this generated file go to Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.methods;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.filter.MassSpecModifier;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.filter.modify.MultiplyIonModifier;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.Matchable;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.RecrusiveCorrection;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.methods.correction.CorrectionCache;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.math.CombinedRegression;
import edu.ucdavis.genomics.metabolomics.util.math.Regression;
import edu.ucdavis.genomics.metabolomics.util.type.converter.BooleanConverter;

/**
 * @author wohlgemuth Dient zur RetentionIndexKorrektur von Binspektren
 */
public final class CorrectionMethod extends AbstractMethod {
	/**
	 * DOCUMENT ME!
	 */
	private List<Map<String, Object>> assigned;

	/**
	 * DOCUMENT ME!
	 */
	private Collection<Map<String, Object>> library;

	/**
	 * DOCUMENT ME!
	 */
	private PreparedStatement riCorrectionFailed;

	/**
	 * DOCUMENT ME!
	 */
	private PreparedStatement sameDay;

	/**
	 * DOCUMENT ME!
	 */
	private PreparedStatement sample;

	/**
	 * DOCUMENT ME!
	 */
	private PreparedStatement select;

	/**
	 * DOCUMENT ME!
	 */
	private PreparedStatement sodStatement;

	private PreparedStatement resetSpectraData;

	/**
	 * DOCUMENT ME!
	 */
	private PreparedStatement update;

	private CorrectionCache cache = null;

	/**
	 * DOCUMENT ME!
	 * 
	 * @uml.property name="poly"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	private Regression poly = new CombinedRegression();

	/**
	 * is this sample corrected with old standards and not found standard
	 */
	private boolean historic = false;

	/**
	 * DOCUMENT ME!
	 */
	private boolean riCorrection;

	/**
	 * DOCUMENT ME!
	 */
	private boolean valid = false;

	/**
	 * minimale anzahl der gefundenen standards
	 */
	private int minStandards = 0;

	private PreparedStatement correctedWith;

	private boolean allowHistoricSamples = true;

	private int correctionId;

	private int maxNumberOfDaysForCorrection;

	public boolean isAutoscale() {
		return autoscale;
	}

	public void setAutoscale(boolean autoscale) {
		this.autoscale = autoscale;
	}

	private boolean autoscale;

	private PreparedStatement quantifier;

	/**
	 * 
	 */
	public CorrectionMethod() {
		try {
			this.setMatchable((Matchable) Class.forName(
					CONFIG.getElement("class.correction").getAttributeValue(
							"value")).newInstance());
		} catch (Exception e) {
			logger.error(
					"failed to set defined class for matching use default implemantation",
					e);
			this.setMatchable(new RecrusiveCorrection());
		}

		minStandards = new Integer(CONFIG.getValue("bin.correction.minimal"));
		// by default we read it from the configuration
		try {
			if (CONFIG.getElement("bin.correction.allowHistoric") != null) {
				this.setAllowHistoricSamples(Boolean.parseBoolean(CONFIG
						.getValue("bin.correction.allowHistoric")));
			} else {
				logger.info("setting was not defiend in configuration, we use the default approach and allow historic samples, key has to be: \"bin.correction.allowHistoric\"");
				this.setAllowHistoricSamples(true);
			}
			logger.info("allow historic samples: "
					+ this.isAllowHistoricSamples());
		} catch (Exception e) {
			logger.info("setting was not defiend in configuration, we use the default approach and allow historic samples, key has to be: \"bin.correction.allowHistoric\"");
			this.setAllowHistoricSamples(true);
		}

		if (this.isAllowHistoricSamples()) {
			if (CONFIG.getElement("bin.correction.allowHistoric.days") != null) {
				this.maxNumberOfDaysForCorrection = Integer.parseInt(CONFIG
						.getAttributeValue("bin.correction.allowHistoric",
								"days"));
			} else {
				logger.info("setting was not defiend in configuration, we use the default approach and allow historic samples for 10 days, key has to be: \"bin.correction.allowHistoric[days]\"");
				this.maxNumberOfDaysForCorrection = 10;
			}
		}

		if (CONFIG.getElement("bin.correction.quantifier.autoScale") != null) {

			this.autoscale = Boolean.parseBoolean(CONFIG.getElement(
					"bin.correction.quantifier.autoScale").getAttributeValue(
					"enable"));
		} else {
			logger.info("setting was not defiend in configuration, we use the default approach and disable the autoscale feature, key has to be: \"bin.correction.quantifier.autoScale\" with the attribute 'enable'");
			this.autoscale = false;
		}

		logger.info("quantifier auto scaling enabled: " + autoscale);

		// this.setAllowHistoricSamples(false);

	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Collection<Map<String, Object>> getAssigned() {
		return assigned;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean isHistoric() {
		return historic;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	@SuppressWarnings("unchecked")
	public Collection getLibrary() {
		return library;
	}

	/**
	 * is the correction valid or not, when the correction fails it should be
	 * set to valid = false if it is successful valid = true
	 * 
	 * @param valid
	 */
	public void setValid(boolean valid) {
		logger.info("setting correction to valid: " + valid);
		this.valid = valid;

		try {
			this.riCorrectionFailed.setInt(3, this.sampleId);
			this.riCorrectionFailed.setString(1,
					BooleanConverter.booleanToString(!valid));
			this.riCorrectionFailed.setString(2,
					BooleanConverter.booleanToString(valid));

			this.riCorrectionFailed.executeUpdate();
		} catch (Exception e) {
			logger.error("Cannot update sample table: " + e.getMessage(), e);
		}

		/**
		 * @see edu.ucdavis.genomics.metabolomics.binbase.binlib.algorythm.Correctable#valid()
		 */
	}
	public boolean valid() {
		return valid;
	}

	/**
	 * @see edu.ucdavis.genomics.metabolomics.util.SQLObject#prepareStatements()
	 */
	protected void prepareStatements() throws Exception {
		super.prepareStatements();

		this.update = this.getConnection().prepareStatement(
				SQL_CONFIG.getValue(CLASS + ".update"));
		this.select = this.getConnection().prepareStatement(
				SQL_CONFIG.getValue(CLASS + ".select"));

		this.riCorrectionFailed = this.getConnection().prepareStatement(
				SQL_CONFIG.getValue(CLASS + ".correctionFailed"));

		this.sodStatement = this.getConnection().prepareStatement(
				SQL_CONFIG.getValue(CLASS + ".sod"));
		this.sameDay = this.getConnection().prepareStatement(
				SQL_CONFIG.getValue(CLASS + ".sameDay"));

		this.sample = this.getConnection().prepareStatement(
				SQL_CONFIG.getValue(CLASS + ".sample"));

		this.correctedWith = this.getConnection().prepareStatement(
				SQL_CONFIG.getValue(CLASS + ".correctedWith"));

		this.quantifier = this.getConnection().prepareStatement(
				"select qualifier from standard");

		this.cache = new CorrectionCache();

		this.cache.setConnection(this.getConnection());


		this.resetSpectraData = this.getConnection().prepareStatement("update spectra set retention_index = retention_time, bin_id = null, match = null, found_at_correction = null where sample_id = ?");

	}

	/**
	 * insert a comment
	 * 
	 * @author wohlgemuth
	 * @version Oct 6, 2006
	 * @param message
	 */
	private void correctedWith(int correctionId) {
		try {

			logger.info("sample was corrected with id: " + correctionId);
			this.correctedWith.setInt(1, correctionId);
			this.correctedWith.setInt(2, this.sampleId);

			this.correctedWith.execute();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 */
	protected void prepareVariables() throws Exception {
		super.prepareVariables();
		riCorrection = (Boolean
				.valueOf(CONFIG.getValue("bin.correction.allow")));

		this.getMatchable().setSampleId(this.sampleId);
		this.getMatchable().setAlgorythmHandler(this.algorythmHandler);
		this.getMatchable().setResultHandler(this.resultHandler);
		this.getMatchable().setConnection(this.getConnection());
		this.getMatchable().setConfig(CONFIG.getElement("values"));
		this.historic = false;
	}

	/**
	 */
	protected void start() {
		logger.info("start correction method");
		this.getResultHandler().setNewBinAllowed(false);
		try {


			logger.info("resetting sample and spectra table");

			this.resetSpectraData.setInt(1,this.getSampleId());
			this.resetSpectraData.executeUpdate();


			if (!riCorrection) {
				logger.info("retention index correction is explicit disabled!");
				this.setValid(true);

				return;
			}

			if(this.getMatchable().getBins().isEmpty()){
				logger.info("no bin's defined, so we can't match anything!");
				return;
			}

			this.setValid(true);

			logger.info("resetting correction variables just in case");

			this.correctionId = this.sampleId;

			runMatchable(this.getMatchable());
			this.setValid(this.validateRequired());
			this.historic = false;

			handleInvalidCorrection();

			// we still did'nt found anything, time to give up
			if (!this.valid) {
				logger.warn("ri correction failed, check log for further details!");

				// JIRA ISSUE BINBASE-252
				logger.warn("correct sample with standards which were found in this sample, since it's better than nothing!");

				this.getMatchable().setSampleId(this.sampleId);
				runMatchable(this.getMatchable());
				correctSampleWithAvaialbeStandards(this.correctionId);
				return;
			}

			// do correction
			correctSampleWithAvaialbeStandards(this.correctionId);
			if (historic) {
				logger.info("is historic annotation");
				this.setValid(false);
			}

		} catch (Exception e) {
			logger.error("error at ri correction: " + e.getMessage(), e);
			logger.warn("dissable bin generation for sample " + sampleId);
			this.setValid(false);
		}

		logger.info("finished correction method");
	}

	/**
	 * trying to validate the correction in case it fails
	 */
	private void handleInvalidCorrection() {

		// if correction failed, try to auto scale quantifier
		if (!this.valid) {
			if (this.autoscale) {
				logger.warn("not enough standards found in this sample, trying to autoscale qualifiers");
				this.setValid(obtainsAutoScale());
			} else {
				logger.warn("not enough standards found and autoscale function is disabled");
			}
		}

		// correction failed try historic samples
		if (!this.valid) {
			if (this.allowHistoricSamples) {
				logger.warn("not enough standards found in this sample, try to obtain standards from histroric samples");
				historic = true;
				this.setValid(obtainHistoric());
			} else {
				logger.warn("not enough standards found and historic correction is disabled!");
			}
		}
	}

	/**
	 * an auto scale feature to scale all the quantifiers
	 * 
	 * @return
	 * @throws BinBaseException
	 */
	private boolean obtainsAutoScale() {
		try {
			Element auto = CONFIG
					.getElement("bin.correction.quantifier.autoScale");

			if (auto.getAttribute("attempts") != null) {
				int attempts = Integer.parseInt(auto
						.getAttributeValue("attempts"));

				if (attempts > 0) {
					if (auto.getAttribute("factor") != null) {
						double ionFactor = Double.parseDouble(auto
								.getAttributeValue("factor"));

						if (ionFactor > 0) {

							for (int i = 1; i <= attempts; i++) {
								logger.info("running " + i
										+ " autoscale attempts...");

								Element config = new Element("config");

								if (auto.getChildren("ion").size() == 0) {
									logger.info("using all the quantifier ion's specified in the binbase system and scale them, if you like to only subscale a subset of the markers, please provide a chield with the name 'ion'");
									ResultSet res = this.quantifier
											.executeQuery();

									while (res.next()) {
										Element e = new Element("ion");
										e.setAttribute("value", res.getInt(1)
												+ "");
										e.setAttribute("multiplier", i
												* ionFactor + "");

										config.addContent(e);
									}
									res.close();

								} else {
									logger.info("using specified ion for the autoscale and not the default ions in the database. Please make sure that these ions match your markers quantifier ion or it won't have an effect");

									for (Element ion : (List<Element>) auto
											.getChildren("ion")) {
										Element e = new Element("ion");
										e.setAttribute("value",
												Integer.parseInt(ion.getText())
														+ "");

										if (ion.getAttribute("factor") == null) {
											e.setAttribute("multiplier", i
													* ionFactor + "");
										} else {
											e.setAttribute(
													"multiplier",
													i
															* Double.parseDouble(ion
																	.getAttributeValue("factor"))
															+ "");

										}

										config.addContent(e);
									}
								}

								MassSpecModifier modifier = new MultiplyIonModifier();
								modifier.setConnection(this.getConnection());

								modifier.setConfiguration(config);

								this.getMatchable().addMassSpecModifier(
										modifier);
								this.getMatchable().setSampleId(
										this.getSampleId());
								runMatchable(this.getMatchable());
								valid = this.validateRequired();

								this.getMatchable().removeMassSpecModifier(
										modifier);

								if (valid) {
									return true;
								}
							}
						}
					} else {
						logger.warn("the attribute 'factor' of the element 'bin.correction.quantifier.autoScale' needs to have a value larger than 0 or the autoscale feature won't work");
					}
				} else {
					logger.warn("the attribute 'attempts' of the element 'bin.correction.quantifier.autoScale' needs to have a value larger than 0 or the autoscale feature won't work");
				}
			} else {
				logger.warn("the element 'bin.correction.quantifier.autoScale' needs to have the attribute 'attempts' or the autoscale feature won't work");
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	/**
	 * corrects the sample with the available standards
	 * 
	 * @param sampleId
	 * @throws SQLException
	 */
	private void correctSampleWithAvaialbeStandards(int sampleId)
			throws SQLException {
		doCorrection(sampleId);
	}

	/**
	 * make the correction itself
	 * 
	 * @param size
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	private void doCorrection(int sampleId) throws SQLException {
		logger.debug("correct times with found standards: "
				+ this.assigned.size());

		int size = this.assigned.size();
		double[] x = new double[size];
		double[] y = new double[size];

		int[] binIds = new int[size];

		int i = 0;

		Iterator it = assigned.iterator();

		while (it.hasNext()) {
			Map map = (Map) it.next();

			x[i] = Double.parseDouble((String) map.get("retention_index"));
			y[i] = Double.parseDouble((String) (((Map) map.get("BIN"))
					.get("retention_index")));
			binIds[i] = Integer.parseInt(((Map) map.get("BIN")).get("bin_id")
					.toString());

			logger.debug("name: " + (((Map) map.get("BIN")).get("name"))
					+ " bin ri x[" + i + "]/unknown ri y[" + i + "] = " + y[i]
					+ " " + x[i] + " - similarity: " + map.get("similarity"));

			i++;

		}

		try {
			poly.setData(x, y);

			logger.debug("store in cache");
			this.cache.cache(sampleId, binIds, x, y);

			logger.debug("correct sample id: " + this.sampleId);
			this.select.setInt(1, this.sampleId);

			ResultSet res = this.select.executeQuery();

			while (res.next()) {
				int id = res.getInt("spectra_id");
				int ri = res.getInt("retention_index");

				int newRi = (int) Math.round(poly.getY(ri));

				logger.trace(" old ri: " + ri + "\tnew ri: " + newRi
						+ " abs difference: " + Math.abs(ri - newRi)
						+ " difference: " + (ri - newRi));

				getDiagnosticsService().diagnosticActionSuccess(id,
						this.getClass(), "retention index correction",
						"retention time was updated to the new ri",
						new Object[] { ri, newRi });

				this.update.setInt(1, newRi);
				this.update.setInt(2, id);
				this.update.execute();
			}
			res.close();

			logger.info("formula used for correction: ");
			logger.info(poly.toString());

			logger.debug("done with correction");

			this.correctedWith(this.correctionId);
		} catch (Exception e) {
			logger.error("abort correction: " + e.getMessage());
			logger.debug(e.getMessage(),e);
			this.setValid(false);

		}
	}

	/**
	 * loads historic standards from the database 1. sod - 1 or sample from the
	 * current day or sample from yesterday
	 * 
	 * @return
	 */
	@SuppressWarnings("static-access")
	private boolean obtainHistoric() {
		try {
			String thread = Thread.currentThread().getName();

			Set<String> done = new HashSet<String>();

			try {
				logger.warn("try to calculate retention indexes from historic samples");

				// get the sample meta informations
				this.sample.setInt(1, this.sampleId);

				ResultSet result = this.sample.executeQuery();
				Timestamp date = null;
				int sod = 0;

				String machine = null;
				if (result.next()) {
					date = result.getTimestamp("date");
					sod = result.getInt("sod");
					machine = result.getString("machine");
					logger.info("failed correction: " + result.getString(1));
				} else {
					logger.error("sample id not found");

					return false;
				}

				logger.info("used machine is: " + machine);
				logger.info("current date is: " + date + " - "
						+ date.getClass().getName());
				result.close();

				boolean valid = true;

				// find the sod - 1
				this.sodStatement.setInt(1, sod - 1);
				this.sodStatement.setTimestamp(2, date);
				this.sodStatement.setString(3, machine);
				result = this.sodStatement.executeQuery();

				int i = 0;
				while (result.next()) {
					i++;

					if (done.contains(result.getString(2)) == false) {
						done.add(result.getString(2));

						logger.info("found sample of sod-" + i + " "
								+ result.getString(2) + " sample id "
								+ result.getInt(1));

						Thread.currentThread().setName(
								thread + " - last " + result.getString(2));

						this.correctionId = result.getInt(1);

						this.getMatchable().setSampleId(result.getInt(1));
						runMatchable(this.getMatchable());
						valid = this.validateRequired();

						if (valid == true) {
							result.close();
							return true;
						} else {
							logger.debug("could'nt correct with sample: "
									+ result.getString(2) + "/"
									+ result.getInt(1));
						}
					}
				}

				result.close();

				// find samples of the day
				this.sameDay.setTimestamp(1, date);
				this.sameDay.setString(2, machine);

				result = this.sameDay.executeQuery();

				while (result.next()) {
					if (done.contains(result.getString(2)) == false) {
						done.add(result.getString(2));

						logger.info("found sample of same day "
								+ result.getString(2) + " sample id "
								+ result.getInt(1));

						if (result.getString(2).equals(this.getSampleName())) {
							logger.warn("for same reason we try to correct this sample with itself => skipped");
						} else {
							Thread.currentThread().setName(
									thread + " - same day - "
											+ result.getString(2));

							// make sure that this sample is not the same
							this.correctionId = result.getInt(1);

							this.getMatchable().setSampleId(result.getInt(1));
							runMatchable(this.getMatchable());
							valid = this.validateRequired();

							if (valid == true) {
								result.close();

								return true;
							} else {
								logger.debug("could'nt correct with sample: "
										+ result.getString(2) + "/"
										+ result.getInt(1));
							}
						}
					}
				}

				result.close();

				// search for older samples
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);

				int count = maxNumberOfDaysForCorrection;

				for (int x = 0; x < count; x++) {
					logger.info("looking for older samples to use for correction");
					cal.roll(cal.DAY_OF_YEAR, false);
					logger.info("looking for day: " + cal.getTime());
					if (calculateOtherDay(machine, cal, thread, done)) {
						logger.info("successfull");
						return true;
					}
				}

				cal.setTime(date);

				// search for newer samples
				for (int x = 0; x < count; x++) {
					logger.info("looking for newer samples to use for correction");
					cal.roll(cal.DAY_OF_YEAR, true);
					logger.info("looking for day: " + cal.getTime());

					if (calculateOtherDay(machine, cal, thread, done)) {
						logger.info("successfull");
						return true;
					}
				}

				logger.warn("sorry was not possible to correct this sample");

				return false;
			} finally {
				Thread.currentThread().setName(thread);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			return false;
		}
	}

	/**
	 * does the correction with a former day
	 * 
	 * @param machine
	 * @param cal
	 * @param done
	 * @return
	 * @throws SQLException
	 */
	private boolean calculateOtherDay(String machine, Calendar cal,
			String thread, Set<String> done) throws SQLException {
		ResultSet result;
		boolean valid;
		Date date = new Date(cal.getTime().getTime());
		this.sameDay.setDate(1, date);
		this.sameDay.setString(2, machine);

		try {
			result = this.sameDay.executeQuery();

			while (result.next()) {
				if (done.contains(result.getString(2)) == false) {

					if (result.getString(2).equals(this.getSampleName())) {
						logger.warn("for same reason we try to correct this sample with itself => skipped");
					} else {

						done.add(result.getString(2));

						logger.info("found sample of day " + cal.getTime()
								+ " - " + result.getString(2) + " sample id "
								+ result.getInt(1));

						Thread.currentThread()
								.setName(
										thread + " - otherday - "
												+ result.getString(2));

						this.getMatchable().setSampleId(result.getInt(1));
						runMatchable(this.getMatchable());
						valid = this.validateRequired();

						this.correctionId = result.getInt(1);

						if (valid == true) {
							result.close();

							return true;
						} else {
							logger.info("current sample of day wasn't successfull, try next");
						}

					}
				}
			}

			result.close();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	/**
	 * runs the matching itself with the giving matchable
	 * 
	 * @param matchable
	 */
	@SuppressWarnings("unchecked")
	private void runMatchable(Matchable matchable) {
		logger.info("run matchable");

		matchable.run();
		library = matchable.getBins();

		// convert set to list
		assigned = new ArrayList<Map<String, Object>>(matchable.getAssigned());

		// first part dann
		Collections.sort(assigned, new Comparator() {
			public int compare(Object o1, Object o2) {
				Map first = (Map) o1;
				Map second = (Map) o2;
				Double firstRi = new Double((String) ((Map) first.get("BIN"))
						.get("retention_index"));
				Double secondRi = new Double((String) ((Map) second.get("BIN"))
						.get("retention_index"));

				return firstRi.compareTo(secondRi);
			}
		});

	}

	/**
	 * validate the dataset
	 * 
	 * @param valid
	 * @param have
	 */
	@SuppressWarnings("unchecked")
	private boolean validateRequired() {
		logger.info("validating correction for all found standards.");

		for (Map<String, Object> map : getAssigned()) {
			double x = Double.parseDouble((String) map.get("retention_index"));

			String name = ((Map) map.get("BIN")).get("name").toString();

			logger.debug("found " + name + " at retention index " + x);
		}

		if (assigned.isEmpty()) {
			logger.warn("no assigned ri standards in found");

			return false;
		}

		// no library so return
		if (library.size() == 0) {
			logger.warn(" no correction in this sample " + sampleId
					+ ", be carefull with the datas because librarys is empty");

			return false;
		}

		double hitRate = (double) assigned.size() / (double) library.size();

		logger.info("hit rate: " + hitRate);
		// too much standards, return
		if (hitRate > 1.0) {

			logger.fatal("more then 100% of defined standards found, problem at "
					+ this.getMatchable().getClass().toString());

			return false;
		}

		// validate if standards are in order

		logger.info("validate if standards are in order");
		double lastX = 0;

		for (Map<String, Object> map : getAssigned()) {
			double x = Double.parseDouble((String) map.get("retention_index"));

			String name = ((Map) map.get("BIN")).get("name").toString();

			logger.debug(name + " at index " + x);

			if (x > lastX) {
				lastX = x;
			} else {
				logger.warn("assigned massspecs are not in order so correction can't work");
				return false;
			}
		}

		logger.info("validate if we got enough standards");

		int valid = 0;
		int have = 0;

		if (assigned.size() >= minStandards) {
			for (Map e : library) {
				boolean required = BooleanConverter.StringtoBoolean(e.get(
						"required").toString());
				String name = e.get("name").toString();

				logger.info("standard required for correction: " + name + " - "
						+ required);

				if (required) {
					valid++;
				}

				for (Map map : assigned) {
					String assigned = (String) ((Map) map.get("BIN"))
							.get("name");

					if (assigned.equals(name)) {
						if (required) {
							have++;
							logger.info("=> found and required");

						} else {
							logger.info("=> found, but optional");
						}
					}
				}
			}

			if (valid > have) {
				logger.debug("valid was: " + valid + " and have was " + have);
				return false;
			}
		} else {
			logger.info("we only found " + assigned.size()
					+ " instead of the required minimum of " + minStandards
					+ " standards");
			return false;
		}

		return true;
	}

	public boolean isAllowHistoricSamples() {
		return allowHistoricSamples;
	}

	public void setAllowHistoricSamples(boolean allowHistoricSamples) {
		this.allowHistoricSamples = allowHistoricSamples;
	}
}
