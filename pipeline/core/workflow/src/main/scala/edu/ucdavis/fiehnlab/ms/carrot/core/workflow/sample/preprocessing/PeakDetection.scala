package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.preprocessing

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.MSDialProcessing
import org.springframework.beans.factory.annotation.Autowired

/**
  * Created by diego on 2/7/2018
  **/
class PeakDetection extends PreProcessor with LazyLogging {
  /**
    * actually processes the item (implementations in subclasses)
    *
    * @param item
    * @return
    */

  @Autowired
  val msdialProcessor: MSDialProcessing= null

  override def priortiy: Int = 10

  override def doProcess(item: Sample, method: AcquisitionMethod): Sample = {
    msdialProcessor.process()
  }
}
