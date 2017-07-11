package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import java.io.FileNotFoundException

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.{IncludeByMassRangePPM, IncludeByRetentionIndexTimeWindow}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.{MassAccuracy, Regression, RetentionTimeDifference}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSSpectra}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{RetentionIndexTarget, _}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.WorkflowProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.LCMSTargetRetentionIndexCorrection
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
  * Created by wohlg on 7/11/2016.
  */
abstract class ZeroReplacement(properties: WorkflowProperties) extends PostProcessing[Double](properties) with LazyLogging {

  @Autowired
  val zeroReplacementProperties: ZeroReplacementProperties = null

  @Autowired
  val sampleLoader: SampleLoader = null
  @Autowired
  val correction: LCMSTargetRetentionIndexCorrection = null

  /**
    * replaces the given value, with the best possible value
    * based on the provided configuration settings
    *
    * @param needsReplacement
    * @param sample
    * @param rawdata
    * @return
    */
  def replaceValue(needsReplacement: QuantifiedSpectra[Double], sample: QuantifiedSample[Double], rawdata: CorrectedSample): QuantifiedSpectra[Double]

  /**
    * actually processes the item
    *
    * @param sample
    * @return
    */
  override def doProcess(sample: QuantifiedSample[Double]): QuantifiedSample[Double] = {


    val rawdata: Option[Sample] = zeroReplacementProperties.fileExtension.collect {

      case extension: String =>
        val fileNameToLoad = sample.name + "." + extension
        val result = sampleLoader.loadSample(fileNameToLoad)

        if (result.isDefined) {
          logger.info(s"loaded rawdata file: ${result.get}")
          result.get
        }
    }.collect { case p: Sample => p }.headOption

    if (rawdata.isDefined) {
      logger.info(s"replacing data with: ${rawdata}")
      val correctedRawData: CorrectedSample = correction.doCorrection(sample.annotationsUsedForCorrection, rawdata.get, sample.regressionCurve, sample)

      logger.info(s"corrected data for: ${correctedRawData}")

      val replacedSpectra = sample.quantifiedTargets.par.collect {
        case spectra: QuantifiedSpectra[Double] if spectra.quantifiedValue.isDefined => spectra
        case spectra: QuantifiedSpectra[Double] if spectra.quantifiedValue.isEmpty => replaceValue(spectra, sample, correctedRawData)
      }.seq

      new QuantifiedSample[Double] {
        override val quantifiedTargets: Seq[QuantifiedSpectra[Double]] = replacedSpectra
        override val noneAnnotated: Seq[_ <: Feature with CorrectedSpectra] = sample.noneAnnotated
        override val correctedWith: Sample = sample.correctedWith
        override val annotationsUsedForCorrection: Seq[TargetAnnotation[RetentionIndexTarget, MSSpectra]] = sample.annotationsUsedForCorrection
        override val regressionCurve: Regression = sample.regressionCurve
        override val fileName: String = sample.fileName
      }
    }
    //toss exception
    else {
      logger.warn(s"sorry we were not able to load the rawdata file for ${sample.name} using the loader ${sampleLoader}")
      throw new FileNotFoundException(s"sorry we were not able to load the rawdata file for ${sample.name} using the loader ${sampleLoader}")
    }
  }
}

@Component
@ConfigurationProperties(prefix = "zero-replacement")
class ZeroReplacementProperties {

  /**
    * replacement is enabled
    */
  var enabled: Boolean = true

  /**
    * window used for noise calculations in seconds, if 0 the whole chromatography will be used
    */
  var noiseWindowInSeconds: Int = 90

  /**
    * the defined retention index correction for the peak detection during the replacement
    */
  var retentionIndexWindowForPeakDetection: Double = 12

  /**
    * utilized mass accuracy for searches in ppm
    */
  var massAccuracyPPM: Double = 30

  /**
    * extension of our rawdata files, to be used for replacement
    */
  var fileExtension: List[String] = "mzXML"  :: "mzML" :: List()
}

/**
  * Finds the noise value for a peak and substract it from the local maximum for the provided ion and mass
  *
  * @param properties
  */
@Component
class SimpleZeroReplacement @Autowired()(properties: WorkflowProperties) extends ZeroReplacement(properties) {
  /**
    * replaces the given value, with the best possible value
    * based on the provided configuration settings
    *
    * @param needsReplacement
    * @param sample
    * @param rawdata
    * @return
    */
  override def replaceValue(needsReplacement: QuantifiedSpectra[Double], sample: QuantifiedSample[Double], rawdata: CorrectedSample): GapFillerSpectra[Double] = {
    val receivedTarget = needsReplacement.target

    val filterByMass = new IncludeByMassRangePPM(receivedTarget, zeroReplacementProperties.massAccuracyPPM)
    val filterByRetentionIndexNoise = new IncludeByRetentionIndexTimeWindow(receivedTarget.retentionTimeInSeconds, zeroReplacementProperties.noiseWindowInSeconds)
    val filterByRetentionIndex = new IncludeByRetentionIndexTimeWindow(receivedTarget.retentionTimeInSeconds, zeroReplacementProperties.retentionIndexWindowForPeakDetection)

    //first calculate noise for this ion trace
    val noiseSpectra = rawdata.spectra.filter { spectra =>

      if (zeroReplacementProperties.noiseWindowInSeconds == 0) {
        filterByMass.include(spectra)
      }
      else {
        filterByMass.include(spectra) && filterByRetentionIndexNoise.include(spectra)
      }
    }

    logger.debug(s"found ${noiseSpectra.size} spectra, to utilize for noise calculation")

    val noise = noiseSpectra.map { spectra =>
      MassAccuracy.findClosestIon(spectra, receivedTarget.monoIsotopicMass.get).get.intensity
    }.min

    logger.debug(s"noise is: ${noise} for target: ${receivedTarget}")

    val replacementValueSpectra = rawdata.spectra.par.filter { spectra =>
      filterByMass.include(spectra)
    }

    logger.debug(s"found ${replacementValueSpectra.size} spectra,after mass filter for target ${receivedTarget}")

    val filteredByTime = replacementValueSpectra.filter { spectra =>
      filterByRetentionIndex.include(spectra)
    }
    logger.debug(s"found ${filteredByTime.size} spectra,after mass filter for target ${receivedTarget}")

    val value = filteredByTime.maxBy { spectra =>
      MassAccuracy.findClosestIon(spectra, receivedTarget.monoIsotopicMass.get).get.intensity
    }

    logger.debug(s"found best spectra for replacement: ${value}")
    val noiseCorrectedValue = MassAccuracy.findClosestIon(value, receivedTarget.monoIsotopicMass.get).get.intensity

    //create replacement object
    new GapFillerSpectra[Double] {
      override val spectra: Option[_ <: Feature with CorrectedSpectra] = None
      //Some(value)
      override val quantifiedValue: Option[Double] = Some(noiseCorrectedValue)
      override val massAccuracy: Option[Double] = MassAccuracy.calculateMassError(value, receivedTarget)
      override val target: Target = receivedTarget
      override val retentionIndexDistance: Option[Double] = Some(RetentionTimeDifference.inSeconds(receivedTarget, value))
      override val massAccuracyPPM: Option[Double] = MassAccuracy.calculateMassErrorPPM(value, receivedTarget)
      override val retentionIndex: Double = value.retentionIndex
    }
  }
}
