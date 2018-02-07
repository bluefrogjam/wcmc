package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.preprocessing

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.{MSDialProcessing, MSDialProcessingProperties}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * Created by diego on 2/7/2018
  **/
@Component
class PeakDetection extends PreProcessor with LazyLogging {

  @Autowired
  val msdialProcessor: MSDialProcessing = null

  @Autowired
  val processingProperties: MSDialProcessingProperties = null

  override def priortiy: Int = 10

  /**
    * actually processes the item (implementations in subclasses)
    *
    * @param item
    * @return
    */
  override def doProcess(item: Sample, method: AcquisitionMethod): Sample = {
    msdialProcessor.process(item, processingProperties)
  }
}
