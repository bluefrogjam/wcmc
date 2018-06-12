package edu.ucdavis.fiehnlab.ms.carrot.core.msdial

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.PreProcessor
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.RawData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation._
import org.springframework.stereotype.Component

/**
  * Created by diego on 2/7/2018
  **/
@Component
@Description("this sends a sample to be processed by peak detection and deconvolution algorithms translated from msdial")
@Profile(Array("carrot.processing.peakdetection"))
class PeakDetection extends PreProcessor with LazyLogging {

  @Autowired
  private val msdialProcessor: MSDialProcessing = null

  @Autowired
  private val processingProperties: MSDialProcessingProperties = null

  override def priortiy: Int = 50

  /**
    * actually processes the item (implementations in subclasses)
    *
    * @param item
    * @return
    */
  override def doProcess(item: Sample, method: AcquisitionMethod, rawSample: Option[Sample]): Sample = {

    if (item.isInstanceOf[RawData]) {

      if (method.chromatographicMethod.ionMode.isDefined) {
        processingProperties.ionMode = method.chromatographicMethod.ionMode.get
      }
      else {
        throw new IonModeRequiredException("please ensure you provide an ion mode!")
      }

      msdialProcessor.process(item, processingProperties)
    }
    else {
      logger.info("object is not of type rawdata, no peak detection required")
      item
    }
  }
}

class IonModeRequiredException(str: String) extends Exception(str)