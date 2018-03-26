package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.action;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.ResultDataFile;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import org.slf4j.Logger;
import org.jdom2.Element;
import org.slf4j.LoggerFactory;

/**
 * simple helper class to simplify access to direct default datafiles
 * 
 * @author wohlgemuth
 * 
 */
public abstract class ResultFileAction extends BasicAction {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public Logger getLogger() {
		return logger;
	}

	@Override
	public void run(Element configuration, Source rawdata, Source sop) {

		try {
			Element transformInstructions = action.readSource(sop).getChild(
					"transform");

			if(transformInstructions == null){
				throw new Exception("instructions can't be null, skipping action");
			}
			ResultDataFile file = action.readFile("none", rawdata,
					transformInstructions);

			runDatafile(file, configuration, rawdata, sop);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public abstract String getFolder();

	/**
	 * provides us with an already parsed datafile
	 * 
	 * @param file
	 * @param configuration
	 * @param rawdata
	 * @param sop
	 */
	public abstract void runDatafile(ResultDataFile file,
			Element configuration, Source rawdata, Source sop);

}
