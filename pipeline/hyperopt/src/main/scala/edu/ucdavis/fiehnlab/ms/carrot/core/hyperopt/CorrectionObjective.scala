package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt

import com.eharmony.spotz.Preamble._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.Regression
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.CorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.exception.NotEnoughStandardsFoundException
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{CorrectedSpectra, Feature}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.callbacks.CallbackHandler
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.lossfunctions.LossFunction
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import org.springframework.context.ApplicationContext

/**
  * objective to evaluate the best possible parameters for the retention index correction for the
  *
  * @param config
  * @param profiles
  */
class CorrectionObjective(config: Class[_], profiles: Array[String], lossFunction: LossFunction[CorrectedSample], samples: List[String], methodName: String, callbacks: Seq[CallbackHandler]) extends LCMSObjective(config, profiles, callbacks) {

  /**
    * actualy apply function, providing subclasses with a correctly configured configuration class
    *
    * @param context
    * @param point
    * @return
    */
  override def apply(context: ApplicationContext, point: Point): Double = {

    val method: AcquisitionMethod = AcquisitionMethod.deserialize(methodName)
    val correction: LCMSTargetRetentionIndexCorrectionProcess = context.getBean(classOf[LCMSTargetRetentionIndexCorrectionProcess])
    val deco: PeakDetection = context.getBean(classOf[PeakDetection])
    val loader: SampleLoader = context.getBean(classOf[SampleLoader])


    //apply the hyper opt space settings
    applyCorrectionSettings(point, correction)

    try {
      //deconvolute and correct them
      val corrected = samples.map((item: String) => dropSpectra(correction.process(deco.process(loader.getSample(item), method, None), method, None), loader))

      //compute statistics
      lossFunction.lossFunction(corrected)
    }
    catch {

      case e: NotEnoughStandardsFoundException =>
        logger.warn(s"${e.getMessage}, setting where mass accuracy ${correction.massAccuracySetting} and rt accuracy ${correction.rtAccuracySetting}")
        Double.MaxValue
      case e: RejectDueToCorrectionFailed =>
        logger.warn(s"${e.getMessage}, setting where mass accuracy ${correction.massAccuracySetting} and rt accuracy ${correction.rtAccuracySetting}")
        Double.MaxValue
    }
  }


  /**
    * cache the objects which are cacheable to avoid doing the same task over and over
    *
    * @param context
    */
  override protected def warmCaches(context: ApplicationContext): Unit = {


    val method: AcquisitionMethod = AcquisitionMethod(ChromatographicMethod("teddy", Some("6530"), Some("test"), Some(PositiveMode())))
    val correction: CorrectionProcess = context.getBean(classOf[LCMSTargetRetentionIndexCorrectionProcess])
    val deco: PeakDetection = context.getBean(classOf[PeakDetection])
    val loader: SampleLoader = context.getBean(classOf[SampleLoader])

    samples.foreach { sample =>
      val rawdata = loader.getSample(sample)
      val deconvoluted = deco.process(rawdata, method, None)
      logger.info(s"sample ${sample} was deconvoluted and had ${deconvoluted.spectra.size} spectra")
    }

    logger.info(s"warmed up cache for ${samples.size} samples")
  }

  /**
    * drops all spectra here to safe some memory
    *
    * @param sample
    * @return
    */
  def dropSpectra(sample: CorrectedSample, loader: SampleLoader): CorrectedSample = {
    new CorrectedSample {
      /**
        * which sample was used to correct these data
        */
      override lazy val correctedWith: Sample = this
      /**
        * the curve, which was utilized for this correction
        */
      override val regressionCurve: Regression = sample.regressionCurve
      /**
        * these are all the targets, which were used for the retention index correction
        */
      override val featuresUsedForCorrection: Iterable[TargetAnnotation[Target, Feature]] = sample.featuresUsedForCorrection
      /**
        * the associated spectra, which are now corrected
        */
      override val spectra: Seq[_ <: Feature with CorrectedSpectra] = Seq.empty
      /**
        * the unique file name of the sample
        */
      override val fileName: String = sample.fileName
      /**
        * associated properties
        */
      override val properties: Option[SampleProperties] = None
    }
  }

  /**
    * generates the space to be used based on the given configuration
    *
    * @param config
    * @return
    */
  override def getSpace(config: Config): Map[String, Iterable[Any]] = {
    val settings = config.hyperopt.stages.correction.get.settings

    Map(
      "massAccuracySetting" -> settings.massAccuracy,
      "massAccuracyPPMSetting" -> settings.massAccuracyPPM,
      "rtAccuracySetting" -> settings.rtAccuracy,
      "minPeakIntensitySetting" -> settings.minPeakIntensity,
      "intensityPenaltyThresholdSetting" -> settings.intensityPenalty
    )
  }
}

class RejectDueToCorrectionFailed extends Exception
