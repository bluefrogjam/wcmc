/*
 * Created on Nov 18, 2005
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data;

import edu.ucdavis.genomics.metabolomics.binbase.cluster.handler.AbstractClusterHandler;
import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.config.XMLConfigurator;
import edu.ucdavis.genomics.metabolomics.util.io.dest.Destination;
import edu.ucdavis.genomics.metabolomics.util.io.dest.DestinationFactory;
import edu.ucdavis.genomics.metabolomics.util.io.source.FileSource;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import edu.ucdavis.genomics.metabolomics.util.io.source.SourceFactory;
import edu.ucdavis.genomics.metabolomics.util.statistics.StatisticProcessor;
import org.slf4j.Logger;
import org.jdom.Element;

import javax.naming.Context;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class SimpleStatisticProcessor implements StatisticProcessor {
	private Logger logger = LoggerFactory.getLogger(getClass());

	private AbstractClusterHandler handler;

	private StaticStatisticActions statistic = new StaticStatisticActions();

	public SimpleStatisticProcessor() {
		super();
	}


	public SimpleStatisticProcessor(StaticStatisticActions actions) {
		super();
		this.statistic  = actions;
	}
	public SimpleStatisticProcessor(AbstractClusterHandler handler) {
		this.handler = handler;
	}

	/**
	 * provides the schema to validate the sop
	 * 
	 * @return
	 */
	public String getXSD() {
		return "config/web/sop.xsd";
	}

	/**
	 * creates a zipfile containing the processed result this file will be save
	 * in the destination
	 * 
	 * @author wohlgemuth
	 * @version Nov 18, 2005
	 * @throws ConfigurationException
	 * @throws IOException
	 * @see edu.ucdavis.genomics.metabolomics.binbase.algorythm.statistic.StatisticProcessor#process(edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.source.Source,
	 *      edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.source.Source,
	 *      edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.dest.Destination)
	 */
	@SuppressWarnings("unchecked")
	public void process(Source rawdata, Source sop, Destination destination) throws ConfigurationException {
		try {
			String id = rawdata.getSourceName() + "_" + System.currentTimeMillis();
			logger.info("unique id is: " + id);
			// just for validation, sucks and will be replaced later
			// Sop sops = SopLoader.loadSop(sop);

			Element root = statistic.readSource(sop);

			Collection<String> destiantionIds = new HashSet<String>();

			if (this.getHandler() == null) {
				logger.info("using local mode...");
				localMode(rawdata, id, root, destiantionIds,sop);
			} else {

				logger.info("cluster mode is disabled for statistics, using local mode!");
				localMode(rawdata, id, root, destiantionIds,sop);
			}
			
			logger.info("pack all into one zip");
			statistic.createFinalZip(destination, id, destiantionIds);

			logger.info("done with statistics");
		} catch (Exception e) {
			logger.error(e.getMessage(),e);

			throw new ConfigurationException(e);
		}
	}


	

	/***************************************************************************
	 * runs iterative on the local machine
	 * 
	 * @param rawdata
	 * @param id
	 * @param transform
	 * @param sop 
	 * @throws Exception
	 */
	private void localMode(Source rawdata, String id, Element root, Collection<String> destiantionIds, Source sop) throws Exception {
		List<Element> transform = root.getChildren("transform");
		List<Element> preAction = root.getChildren("pre-action");
		List<Element> postAction = root.getChildren("post-action");
		
		//runs before transformations
		for(Element cross : preAction){
			logger.info("running pre actions");
			Thread.currentThread().setName("pre-action");
			statistic.action(cross, rawdata, id, destiantionIds,sop,transform);
		}
		
		logger.info("running transformations");

		//runs the transformations
		for (Element cross : transform) {
				Thread.currentThread().setName("transform");
				statistic.transform(cross, rawdata, id, destiantionIds,transform.size() == 1);
		}

		logger.info("running post actions");

		//runs post actions
		for(Element cross : postAction){
			Thread.currentThread().setName("post-action");
			statistic.action(cross, rawdata, id, destiantionIds,sop,transform);
		}		
	}

	/**
	 * an unknown tag can be overwriten by other classes
	 * 
	 * @author wohlgemuth
	 * @version Feb 20, 2007
	 * @param cross
	 * @param rawdata
	 * @param id
	 */
	protected void unknown(Element cross, Source rawdata, String id, Collection<String> destiantionIds) {

	}

	public AbstractClusterHandler getHandler() {
		return handler;
	}

	public void setHandler(AbstractClusterHandler handler) {
		this.handler = handler;
	}
}
