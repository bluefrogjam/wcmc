package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.preprocessing

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.WorkflowProperties
import org.springframework.beans.factory.annotation.Autowired

/**
  * Created by diego on 11/30/2016.
  */
class AlreadyDeconvolutedDeconvolution @Autowired()(workflowProperties: WorkflowProperties) extends PreProcessor(workflowProperties) with LazyLogging {
  logger.debug("\n\tCreated Dummy PreProcessor")

  override def doProcess(sample: Sample): Sample = sample
}
