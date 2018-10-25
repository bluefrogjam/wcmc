package edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.anotation;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateSpectra;
import edu.ucdavis.genomics.metabolomics.util.config.Configable;
import edu.ucdavis.genomics.metabolomics.util.math.Similarity;

/**
 * the main filter of the binbase where we check purity/ signal noise/
 * similarity settings
 * 
 * @author wohlgemuth
 * 
 */
public class SifterFilter extends BasicFilter implements Filter {

	private static final String SIMILARITY_CONSTANT = "similarity";

	private double similarityDistance = 100;

	private int distance = 0;

	private boolean override = false;

	private double cleanning;

	public SifterFilter() {
		try {
			Element ex = Configable.CONFIG.getElement("values.matching");
			override = new Boolean(ex.getAttribute("override").getValue())
					.booleanValue();
			distance = Integer.parseInt(ex.getAttribute("maxRiDistance")
					.getValue());
			similarityDistance = Double.parseDouble(ex.getAttribute(
					SIMILARITY_CONSTANT).getValue());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			override = false;
			distance = 0;
		}
		try {
			cleanning = Configable.CONFIG.getElement("values.filter.clean")
					.getAttribute("cut").getDoubleValue();
			logger.debug("using cleaning offset to: " + cleanning);
		} catch (Exception e) {
			logger.error("error at getting value, using default value. Exception was: "
					+ e.getMessage(), e);
			cleanning = 3;
		}
	}

	/**
	 * if the unknown does not satisfy this filter we return false otherwise it
	 * gets accepted
	 */
	@SuppressWarnings("unchecked")
	protected boolean compare(Map<String, Object> bin,
			Map<String, Object> spectra) {

		// time find our configuration settings
		Element element = findConfig(spectra);

		Element settingPurity = null;
		Element settingSignalNoise = null;
		Element settingSimilarity = null;

		Iterator purityIterator;
		Iterator similarityIterator;
		Iterator snIterator;

		List purityList;
		List similarityList;
		List snList;

		int binId = Integer.parseInt(bin.get("bin_id").toString());
		int spectraId = Integer.parseInt(spectra.get("spectra_id").toString());

		double signalNoise = Double
				.parseDouble((String) spectra.get("apex_sn"));
		double purity = Double.parseDouble((String) spectra.get("purity"));

		double spectraUnique = Double.parseDouble(spectra.get("uniquemass")
				.toString());
		double binUnique = Double.parseDouble(bin.get("uniquemass").toString());

		double spectraRi = Double.parseDouble(spectra.get("retention_index")
				.toString());
		double binRi = Double.parseDouble(bin.get("retention_index").toString());

		double[][] libSpectra = FilterUtilities.calculateMassSpec(bin);
		double[][] cleanSpectra = FilterUtilities.calculateCleanMassSpec(
				spectra, cleanning);

		if (element == null) {
			logger.warn("no configuration provided!");
			this.setReasonForRejection("no configuration provided");
			return false;
		}
		try {
			String enable = element.getAttributeValue("enable");

			if (enable == null) {

			} else {
				// allow database matching
				if (new Boolean(enable).booleanValue() == true) {
					// logger.info(" allow database matching");
				}
				// disable database matching, every spectra is a new bin
				else {
					// logger.warn(" disable database matching, every spectra is
					// a new bin");
					this.setReasonForRejection("database matching is disabled");
					return false;
				}
			}

			// //logger.debug(" recieve datas from subsetting");
			purityList = element.getChildren("purity");

			purityIterator = purityList.iterator();
			try {
				while (purityIterator.hasNext()) {

					try {
						settingPurity = (Element) purityIterator.next();

						if (checkElement(binId, spectraId, settingPurity,
								purity) == true) {
							snList = settingPurity.getChildren("signalnoise");

							if (snList == null) {
							} else {
								snIterator = snList.iterator();

								while (snIterator.hasNext()) {
									try {
										settingSignalNoise = (Element) snIterator
												.next();

										// check signal noise
										logger.debug(" check signalnoise");
										if (checkElement(binId, spectraId,
												settingSignalNoise, signalNoise) == true) {
											logger.debug(" succes");
											similarityList = settingSignalNoise
													.getChildren(SIMILARITY_CONSTANT);

											if (similarityList == null) {
											} else {
												similarityIterator = similarityList
														.iterator();

												while (similarityIterator
														.hasNext()) {
													try {
														settingSimilarity = (Element) similarityIterator
																.next();

														// speed hack
														double similarity;

														// calculates by
														// similarity

														similarity = similarity(
																libSpectra,
																cleanSpectra);

														spectra.put(
																SIMILARITY_CONSTANT,
																String.valueOf(similarity));
														// get similaritys
														// ceck similarity
														logger.debug(" check similarity");
														if (checkElement(
																binId,
																spectraId,
																settingSimilarity,
																similarity) == true) {
															return true;
														} else {
															if (override) {
																if (spectraUnique == binUnique) {
																	if (Math.abs(spectraRi
																			- binRi) <= distance) {
																		if (similarity > this.similarityDistance) {
																			logger.debug("accepted");
																			getDiagnosticsService()
																					.diagnosticActionSuccess(
																							spectraId,
																							binId,
																							this.getClass(),
																							"override annotation process",
																							"massspecs unique ion was identical with bin unique ion, in the ri range and the similarity was significant higher",
																							new Object[] {
																									similarity,
																									similarityDistance });
																			return true;
																		} else {
																			getDiagnosticsService()
																					.diagnosticActionFailed(
																							spectraId,
																							binId,
																							this.getClass(),
																							"override annotation process",
																							"massspec was rejected at the override annotation proccess, since the similarity was not signficant enough",
																							new Object[] {
																									similarity,
																									similarityDistance });
																			return false;
																		}
																	} else {
																		getDiagnosticsService()
																				.diagnosticActionFailed(
																						spectraId,
																						binId,
																						this.getClass(),
																						"override annotation process",
																						"massspec was rejected at the override annotation proccess, since the retention index was out of the window",
																						new Object[] {
																								Math.abs(spectraRi
																										- binRi),
																								distance });
																		return false;
																	}
																} else {
																	getDiagnosticsService()
																			.diagnosticActionFailed(
																					spectraId,
																					binId,
																					this.getClass(),
																					"override annotation process",
																					"massspec was rejected at the override annotation proccess, since the unique ions were not identical",
																					new Object[] {
																							spectraUnique,
																							binUnique });
																	return false;
																}
															} else {
																logger.debug("override not enabled");

																this.setReasonForRejection("similarity was not high enough and override was disabled");
																getDiagnosticsService()
																		.diagnosticActionFailed(
																				spectraId,
																				binId,
																				this.getClass(),
																				"override annotation process",
																				"override mode was disabled and similarity was not high enough",
																				new Object[] {});
																return false;
															}
														}
													} catch (Exception e) {
														logger.error(e.getMessage(), e);

														return false;
													}
												}
											}
										}
									} catch (Exception e) {
										logger.error(e.getMessage(), e);
										return false;
									}
								}
							}
							return false;
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						return false;
					}
				}
			} finally {
				if(logger.isDebugEnabled()) {
					logger.debug("");
					logger.debug("leave method");
					logger.debug("");
				}
			}

			return false;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}

	}

	/**
	 * 
	 * @param element
	 *            Element with the parameters minimal and maximal as doubles
	 * @param value
	 *            value to check
	 * @return true if value beetween minimal and maximal, false if not
	 */
	private boolean checkElement(int binId, int spectraId, Element element,
			double value) throws Exception {
		// check for attribut
		if(logger.isDebugEnabled()) {
			logger.debug("checking: " + element.getName());
		}
		String minValue = element.getAttribute("minimal").getValue();
		String maxValue = element.getAttribute("maximal").getValue();

		boolean minND = false;
		boolean maxND = false;

		double minimal = 0;
		double maximal = 0;

		try {
			// check for attribut
			if (minValue == null) {
				throw new Exception("needed attribut minimal!");
			}

			// check for attribut
			if (maxValue == null) {
				throw new Exception("needed attribut maximal!");
			}

			// convert values
			if ("nd".equals(minValue)) {
				minND = true;

			} else {
				minimal = new Double(minValue).doubleValue();
			}

			if ("nd".equals(maxValue)) {
				maxND = true;
			} else {
				maximal = new Double(maxValue).doubleValue();
			}

			if ((minND == true) && (maxND == true)) {
				throw new Exception("sorry on parameter must unequal \"nd\"");
			} else {
				if (minND == true) {
					if(logger.isDebugEnabled()) {

						logger.debug(" checkElement: " + element.getName()
								+ "  maximal = " + maximal + " have = " + value);
					}

					if (value <= maximal) {
						if(logger.isDebugEnabled()) {

							logger.debug("accept");
						}
						getDiagnosticsService()
								.diagnosticActionSuccess(
										spectraId,
										binId,
										this.getClass(),
										"filtering by " + element.getName(),
										"spectras value was less than the maximal value and so accepted",
										new Object[] { value, maximal });

						return true;
					} else {
						logger.debug("failed");
						getDiagnosticsService()
								.diagnosticActionFailed(
										spectraId,
										binId,
										this.getClass(),
										"filtering by " + element.getName(),
										"massspec was rejected since its value was more than the maximal value",
										new Object[] { value, maximal });

						return false;
					}
				}

				if (maxND == true) {
					if(logger.isDebugEnabled()) {

						logger.debug(" checkElement: " + element.getName() + " minimal = "
								+ minimal + " have = " + value);
					}
					if (value >= minimal) {
						logger.debug("accept");
						getDiagnosticsService()
								.diagnosticActionSuccess(
										spectraId,
										binId,
										this.getClass(),
										"filtering by " + element.getName(),
										"spectras value was more than the minimal value and so accepted",
										new Object[] { value, minimal });
						return true;
					} else {
						logger.debug("failed");
						getDiagnosticsService()
								.diagnosticActionFailed(
										spectraId,
										binId,
										this.getClass(),
										"filtering by " + element.getName(),
										"massspec was rejected since its value was less than the minimal value",
										new Object[] { value, minimal });
						return false;
					}
				}
			}

			// check values
			if(logger.isDebugEnabled()) {

				logger.debug(" checkElement: " + element.getName() + " minimal = "
						+ minimal + " maximal = " + maximal + " have = " + value);
			}
			if ((value >= minimal) && (value <= maximal)) {
				if(logger.isDebugEnabled()) {

					logger.debug("accept");
					getDiagnosticsService()
							.diagnosticActionSuccess(
									spectraId,
									binId,
									this.getClass(),
									"filtering by " + element.getName(),
									"spectras value was more than the minimal value, less than the maximal value and so accepted",
									new Object[]{value, minimal, maximal});
				}
				return true;
			} else {
				logger.debug("failed");
				getDiagnosticsService()
						.diagnosticActionFailed(
								spectraId,
								binId,
								this.getClass(),
								"filtering by " + element.getName(),
								"massspec was rejected since its value was outside of the minimal and maximal value",
								new Object[] { value, minimal, maximal });
				return false;
			}
		} catch (Exception e) {
			getDiagnosticsService().diagnosticActionFailed(spectraId, binId,
					this.getClass(), "filtering by " + element.getName(),
					"massspec was rejected since an exception was thrown",
					new Object[] { e.getMessage() });

			throw e;
		}

	}

	/**
	 * determines whats the leco version of the massspec is and provides the
	 * correct implementations
	 * 
	 * @param spectra
	 * @return
	 */
	private Element findConfig(Map spectra) {
		Element elementValues = Configable.CONFIG.getElement("values.matching");
		List<Element> elementValuesList = elementValues.getChildren("leco");

		String version = "no version defiend";

		if (spectra.get("leco") != null) {
			version = spectra.get("leco").toString();
		}

		// contains the correct parameter
		Element element = null;
		Element failback = null;

		for (Element current : elementValuesList) {
			if (current.getAttributeValue("version").equals(version)) {
				element = current;
			} else if (current.getAttributeValue("version").equals("default")) {
				failback = current;
			}
		}

		if (element == null) {
			element = failback;
		}
		return element;
	}

	double similarity(double[][] library, double[][] unknown) {
		try {
			Similarity sim = new Similarity();
			sim.setLibrarySpectra((library));
			sim.setUnknownSpectra((unknown));

			return sim.calculateSimimlarity();
		} catch (ArrayIndexOutOfBoundsException e) {
			throw e;
		}
	}
}
