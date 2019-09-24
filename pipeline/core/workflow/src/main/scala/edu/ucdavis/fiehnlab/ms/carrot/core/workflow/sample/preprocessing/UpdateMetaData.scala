package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.preprocessing

import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.PreProcessor
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import org.springframework.context.annotation.{Description, Profile}
import org.springframework.stereotype.Component

/**
  * updates the available metadata for this given sample
  */
@Profile(Array("carrot.processing.metadata.update"))
@Description("this updates the sample information management system with metadata from this sample")
@Component
class UpdateMetaData extends PreProcessor {
  /**
    * actually processes the item (implementations in subclasses)
    *
    * @param item
    * @return
    */
  override def doProcess(item: Sample, method: AcquisitionMethod, rawSample: Option[Sample]): Sample = item

  override def priority: Int = 99
}
