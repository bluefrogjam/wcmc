package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt

import com.eharmony.spotz.Preamble.Point
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.exception.NotEnoughStandardsFoundException
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.AnnotatedSample
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.callbacks.CallbackHandler
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.lossfunctions.LossFunction
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import org.springframework.context.ApplicationContext

class AnnotationObjective(config: Class[_], profiles: Array[String], lossFunction: LossFunction[AnnotatedSample], samples: List[String], methodName: String, callbacks: Seq[CallbackHandler], correctionBestPoint: Option[Point] = None) extends LCMSObjective(config, profiles,callbacks) {
  /**
    * actual apply function, providing subclasses with a correctly configured configuration class
    *
    * @param context
    * @param point
    * @return
    */

  /**
    * generates the space to be used based on the given configuration
    *
    * @param config
    * @return
    */
  override def getSpace(config: Config): Map[String, Iterable[Any]] = {
    val settings = config.hyperopt.stages.annotation.get.settings

    Map(
      "recursive" -> settings.recursive,
      "preferMassAccuracy" -> settings.preferMassAccuracy,
      "preferGaussianSimilarity" -> settings.preferGaussianSimilarity,
      "closePeakDetection" -> settings.closePeakDetection,
      "massAccuracyPPM" -> settings.massAccuracyPPM,
      "massAccuracy" -> settings.massAccuracy,
      "rtIndexWindow" -> settings.rtIndexWindow,
      "massIntensity" -> settings.massIntensity,
      "intensityPenalty" -> settings.intensityPenalty
    )
  }


  override def apply(context: ApplicationContext, point: Point): Double = {

    val method: AcquisitionMethod = AcquisitionMethod.deserialize(methodName)
    val correction: LCMSTargetRetentionIndexCorrectionProcess = context.getBean(classOf[LCMSTargetRetentionIndexCorrectionProcess])
    val annotation: LCMSTargetAnnotationProcess = context.getBean(classOf[LCMSTargetAnnotationProcess])
    val deco: PeakDetection = context.getBean(classOf[PeakDetection])
    val loader: SampleLoader = context.getBean(classOf[SampleLoader])


    //apply the hyper opt space settings
    if (correctionBestPoint.isDefined) {
      applyCorrectionSettings(correctionBestPoint.get, correction)
    }

    applyAnnotationSettings(point, annotation)

    try {
      //deconvolute and correct them
      val annotated = samples.map((item: String) => annotation.process(correction.process(deco.process(loader.getSample(item), method, None), method, None), method, None))

      //compute statistics

      lossFunction.lossFunction(annotated)
    }
    catch {

      case e: NotEnoughStandardsFoundException =>
        logger.warn(s"${e.getMessage}, setting where mass accuracy ${correction.massAccuracySetting} and re accuracy ${correction.rtAccuracySetting}")
        Double.MaxValue
      case e: RejectDueToCorrectionFailed =>
        logger.warn(s"${e.getMessage}, setting where mass accuracy ${correction.massAccuracySetting} and re accuracy ${correction.rtAccuracySetting}")
        Double.MaxValue
    }
  }
}
