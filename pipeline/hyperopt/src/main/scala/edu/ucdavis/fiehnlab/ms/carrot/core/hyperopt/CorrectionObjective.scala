package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt

import com.eharmony.spotz.Preamble._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.Regression
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{CorrectedSpectra, Feature, MSSpectra, MetadataSupport}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import org.springframework.context.ApplicationContext

/**
  * objective to evaluate the best possible parameters for the retention index correction for the
  *
  * @param config
  * @param profiles
  */
class CorrectionObjective(config: Class[_], profiles: Array[String], samples: List[String]) extends SpringBootObjective(config, profiles) {

  def getSpace(massAccuracySetting: Seq[Double], rtAccuracySetting: Seq[Double]): Map[String, Iterable[Any]] = {
    Map(
      "massAccuracySetting" -> massAccuracySetting,
      "rtAccuracySetting" -> rtAccuracySetting
    )
  }

  /**
    * actualy apply function, providing subclasses with a correctly configured configuration class
    *
    * @param context
    * @param point
    * @return
    */
  override def apply(context: ApplicationContext, point: Point): Double = {

    val method: AcquisitionMethod = AcquisitionMethod(ChromatographicMethod("teddy", Some("6530"), Some("test"), Some(PositiveMode())))
    val correction: LCMSTargetRetentionIndexCorrectionProcess = context.getBean(classOf[LCMSTargetRetentionIndexCorrectionProcess])
    val deco: PeakDetection = context.getBean(classOf[PeakDetection])
    val loader: SampleLoader = context.getBean(classOf[SampleLoader])


    //apply the hyper opt space settings
    correction.massAccuracySetting = point.get("massAccuracySetting").asInstanceOf[Double]
    correction.rtAccuracySetting = point.get("rtAccuracySetting").asInstanceOf[Double]

    //deconvolute and correct them
    val corrected = samples.map((item: String) => dropSpectra(correction.process(deco.process(loader.getSample(item), method, None), method, None), loader))

    //compute statistics

    try {
      val targetsAndAnnotationsForAllSamples = corrected.flatMap {
        item: CorrectedSample =>
          if (item.correctionFailed) {
            throw new RejectDueToCorrectionFailed
          }
          else {
            item.featuresUsedForCorrection.map {
              annotation =>
                (annotation.target, annotation.annotation)
            }
          }
      }.groupBy(_._1)

      val rsd = targetsAndAnnotationsForAllSamples.map {
        item =>
          val annotations = item._2.map(_._2)
          val heights = annotations.collect {
            case feature: MSSpectra with MetadataSupport =>
              feature.metadata("peakHeight").asInstanceOf[Some[Double]].get
          }

          val stdDev = Statistics.rsdDev(heights)
          (item._1, stdDev)

      }

      val averageRst = Statistics.mean(rsd.values)

      averageRst
    }
    catch {

      case e: RejectDueToCorrectionFailed =>
        e.printStackTrace()
        Double.MaxValue
    }
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
}

class RejectDueToCorrectionFailed extends Exception
