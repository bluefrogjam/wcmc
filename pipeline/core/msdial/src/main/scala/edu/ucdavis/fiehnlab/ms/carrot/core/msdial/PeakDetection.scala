package edu.ucdavis.fiehnlab.ms.carrot.core.msdial

import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.PreProcessor
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{RawData, Sample}
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.TrackingData
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.{CacheEvict, Cacheable}
import org.springframework.context.annotation._
import org.springframework.stereotype.Component

abstract class PeakDetection extends PreProcessor {}

/**
  * Created by diego on 2/7/2018
  **/
@Component
@Description("this sends a sample to be processed by peak detection and deconvolution algorithms translated from msdial")
@Profile(Array("carrot.processing.peakdetection"))
class PeakDetectionImpl extends PeakDetection with Logging {

  @Autowired
  private val msdialProcessor: MSDialProcessing = null

  @Autowired
  private val processingProperties: MSDialProcessingProperties = null

  @Autowired
  val stasisClient: StasisService = null

  override def priority: Int = 50

  /**
    * actually processes the item (implementations in subclasses)
    *
    * @param item
    * @return
    */
  @Cacheable(value = Array("process-peak-detection"), key = "#item.getFileName() + '_' + #method.toString()")
  override def doProcess(item: Sample, method: AcquisitionMethod, rawSample: Option[Sample]): Sample = {

    if (item.isInstanceOf[RawData]) {

      if (method.chromatographicMethod.ionMode.isDefined) {
        processingProperties.ionMode = method.chromatographicMethod.ionMode.get
      }
      else {
        throw new IonModeRequiredException("please ensure you provide an ion mode!")
      }

      val deconvolutedSample = msdialProcessor.process(item, processingProperties)
      stasisClient.addTracking(TrackingData(deconvolutedSample.name, "deconvoluted", deconvolutedSample.fileName))
      deconvolutedSample
    }
    else {
      logger.info("object is not of type rawdata, no peak detection required")
      item
    }
  }

  @CacheEvict(value = Array("process-peak-detection"), allEntries = true)
  override def clearCache() = {}
}

class IonModeRequiredException(str: String) extends Exception(str)
