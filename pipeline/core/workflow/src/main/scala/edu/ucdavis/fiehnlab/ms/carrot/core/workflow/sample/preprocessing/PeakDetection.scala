package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.preprocessing

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.{MSDialProcessing, MSDialProcessingProperties}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Bean, Configuration, Description, Profile}
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
  override def doProcess(item: Sample, method: AcquisitionMethod): Sample = {
    if (method.chromatographicMethod.get.ionMode.isDefined) {
      processingProperties.ionMode = method.chromatographicMethod.get.ionMode.get
    }
    msdialProcessor.process(item, processingProperties)
  }
}

@Configuration
class PeakDetectionConfiguration {
  @Bean
  def msdialProcessor: MSDialProcessing = new MSDialProcessing()

  @Bean
  def processingProperties: MSDialProcessingProperties = new MSDialProcessingProperties()
}
