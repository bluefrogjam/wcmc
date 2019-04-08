package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.action

import edu.ucdavis.fiehnlab.ms.carrot.core.api.action.PostAction
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSMSSpectra
import org.apache.logging.log4j.scala.Logging
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile(Array("carrot.targets.dummy"))
class DummyPostAction extends PostAction with Logging {
  /**
    * executes this action by doing nothing
    *
    * @param sample
    * @param experimentClass
    * @param experiment
    */
  override def run(sample: Sample, experimentClass: ExperimentClass, experiment: Experiment): Unit = {
    logger.info(s"Fake uploading to libraries ${sample.spectra.count(_.isInstanceOf[MSMSSpectra])} MSMS spectra")
  }
}
