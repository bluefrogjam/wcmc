package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.action

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.action.PostAction
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{MSMSSpectra, MSSpectra}
import org.springframework.stereotype.Component

/**
	* Created by diego on 7/17/2017.
	*/
@Component
class MSMSScannerAction extends PostAction with LazyLogging {
	/**
		* executes this action
		*
		* @param sample
		* @param experimentClass
		* @param experiment
		*/
	override def run(sample: Sample, experimentClass: ExperimentClass, experiment: Experiment): Unit = {
		logger.info(s"Scanning for msms spectra on sample: ${sample.name}, class: ${experimentClass.name.getOrElse("not available")}, experiment: ${experiment.name.getOrElse("not available")}")
		val msmsSpec = sample.spectra match {
			case clazz: MSMSSpectra if clazz.msLevel.equals(2) =>

				clazz.spectraString
		}
	}
}
