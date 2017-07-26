package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.WorkflowProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.LCMSTargetRetentionIndexCorrection
import org.springframework.beans.factory.annotation.Autowired

/**
	* Created by diego on 7/25/2017.
	*/
class BetterZeroReplacement @Autowired()(properties: WorkflowProperties) extends PostProcessing[Double](properties) with LazyLogging {
	@Autowired
	val correction: LCMSTargetRetentionIndexCorrection = null

	/**
		* actually processes the item (implementations in subclasses)
		*
		* @param sample
		* @return
		*/
	override def doProcess(sample: QuantifiedSample[Double]): QuantifiedSample[Double] = {

		logger.info(s"${sample.name} -- ${sample.correctedWith}")
		logger.info(sample.spectra.mkString(" "))
		logger.info(sample.regressionCurve.getFormulas.mkString(" -- "))
		logger.info(sample.regressionCurve.coefficient.mkString(" -- "))
		logger.info(sample.correctedWith.name)

		sample
	}

}
