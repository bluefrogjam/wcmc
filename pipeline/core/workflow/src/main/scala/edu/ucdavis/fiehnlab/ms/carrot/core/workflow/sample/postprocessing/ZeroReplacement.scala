package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.{MassAccuracy, Regression}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.{CorrectionProcess, PostProcessing}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Target, _}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter.{IncludeByMassRange, IncludeByRetentionIndexWindow}
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.TrackingData
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * Created by wohlg on 7/11/2016.
  */
abstract class ZeroReplacement extends PostProcessing[Double] with Logging {

  @Autowired
  val zeroReplacementProperties: ZeroReplacementProperties = null

  @Autowired
  val sampleLoader: SampleLoader = null

  @Autowired
  val correction: CorrectionProcess = null

  @Autowired
  val stasisClient: StasisService = null

  /**
    * replaces the given value, with the best possible value
    * based on the provided configuration settings
    *
    * @param needsReplacement
    * @param sample
    * @param rawdata
    * @return
    */
  def replaceValue(needsReplacement: QuantifiedTarget[Double], sample: QuantifiedSample[Double], rawdata: CorrectedSample): GapFilledTarget[Double]

  /**
    * actually processes the item
    *
    * @param sample
    * @return
    */
  final override def doProcess(sample: QuantifiedSample[Double], method: AcquisitionMethod, rawSample: Option[Sample]): QuantifiedSample[Double] = {

    val rawdata: Option[Sample] =
      if (rawSample.isDefined) {
        rawSample
      } else { //TODO: this block fails when rawSample not defined
        zeroReplacementProperties.fileExtension.collect {

          case extension: String =>
            val fileNameToLoad = sample.name + "." + extension
            logger.debug(s"attempting to load file: ${fileNameToLoad}")

            try {
              sampleLoader.getSample(fileNameToLoad)
            } catch {
              case e: Throwable =>
                logger.error(s"observed error: ${e.getMessage} => skip", e)
            }
        }.collectFirst { case p: Sample => p }
      }

    if (rawdata.isDefined) {
      logger.info(s"corrected data for replacement from: ${rawdata.get.name}")
      val correctedRawData: CorrectedSample = correction.doCorrection(sample.featuresUsedForCorrection, rawdata.get, sample.regressionCurve, sample, tracking = false)

      val replacedSpectra: Seq[QuantifiedTarget[Double]] = sample.quantifiedTargets.map { target =>
        if (target.quantifiedValue.isDefined) {
          target
        }
        else {
          try {
            val replaced = replaceValue(target, sample, correctedRawData)
            logger.info(f"replacing ${target.name.get} with ${replaced.spectraUsedForReplacement.massOfDetectedFeature}")
            replaced
          }
          catch {
            case e: Exception =>
              logger.error(s"replacement failed for entry, ignore for now: ${e.getMessage}, target was: ${target}.", e)
              target
          }
        }
      }

      logger.info("finished replacement")
      val gapFilledSample = new GapFilledSample[Double] {
        override val quantifiedTargets: Seq[QuantifiedTarget[Double]] = replacedSpectra
        override val noneAnnotated: Seq[_ <: Feature with CorrectedSpectra] = sample.noneAnnotated
        override val correctedWith: Sample = sample.correctedWith
        override val featuresUsedForCorrection: Iterable[TargetAnnotation[Target, Feature]] = sample.featuresUsedForCorrection
        override val regressionCurve: Regression = sample.regressionCurve
        override val fileName: String = sample.fileName
        override val gapFilledWithFile: String = rawdata.get.fileName
        override val properties: Option[SampleProperties] = sample.properties
      }

      stasisClient.addTracking(TrackingData(gapFilledSample.name, "replaced", gapFilledSample.fileName))

      gapFilledSample
    }
    //toss exception
    else {
      logger.warn(s"sorry we were not able to load the rawdata file for ${sample.name} using the loader ${sampleLoader}, we are skipping this replacement")
      sample
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
  var noiseWindowInSeconds: Int = 5

  /**
    * the defined retention index correction for the peak detection during the replacement
    * should be half of intended window
    */
  var retentionIndexWindowForPeakDetection: Double = 10

  /**
    * Use the mean of the 2 nearest ions on the left and right of matching mass to the target to estimate the
    * replacement value if no value is found within the RI window.  Takes precedence over estimateByChromatogramNoise
    */
  var estimateByNearestFourIons: Boolean = false

  /**
    * Estimate noise as the mean of the lowest 5% of ions for the given mass in the entire chromatogram
    * if no value is found within the RI window and estimateByNearestFourIons is disabled
    * Likely more expensive, but a statistically reasonable estimate
    */
  var estimateByChromatogramNoise: Boolean = false

  /**
    * utilized mass accuracy for searches
    */
  var massAccuracyInDa: Double = 0.01

  /**
    * extension of our rawdata files, to be used for replacement
    */
  var fileExtension: List[String] = "mzml" :: "cdf" :: List() //removed .d and .d.zip since mzml and cdf ARE raw data files
}

/**
  * Finds the noise value for a peak and subtract it from the local maximum for the provided ion and mass
  *
  */
@Component
@Profile(Array("carrot.processing.replacement.simple"))
class SimpleZeroReplacement @Autowired() extends ZeroReplacement {

  /**
    * replaces the given value, with the best possible value
    * based on the provided configuration settings
    *
    * @param needsReplacement
    * @param quantSample
    * @param rawdata
    * @return
    */
  override def replaceValue(needsReplacement: QuantifiedTarget[Double], quantSample: QuantifiedSample[Double], rawdata: CorrectedSample): GapFilledTarget[Double] = {
    val receivedTarget = needsReplacement

    val filterByMass = new IncludeByMassRange(receivedTarget, zeroReplacementProperties.massAccuracyInDa)

    val filterByRetentionIndexNoise = new IncludeByRetentionIndexWindow(receivedTarget.retentionTimeInSeconds, zeroReplacementProperties.noiseWindowInSeconds)

    val filterByRetentionIndex = new IncludeByRetentionIndexWindow(receivedTarget.retentionIndex, zeroReplacementProperties.retentionIndexWindowForPeakDetection)

    //first calculate noise for this ion trace
    val noiseSpectra = rawdata.spectra.filter { spectra =>

      if (zeroReplacementProperties.noiseWindowInSeconds == 0) {
        includeMass(receivedTarget, filterByMass, spectra)
      }
      else {
        includeMass(receivedTarget, filterByMass, spectra) && filterByRetentionIndexNoise.include(spectra, applicationContext)
      }
    }

    logger.debug(s"\tfound ${noiseSpectra.size} spectra (${noiseSpectra.map(p => p.massOfDetectedFeature.get.intensity).sum / noiseSpectra.size} avg int), to utilize for noise calculation")

    val noiseIons = noiseSpectra.map { spectra =>
      MassAccuracy.findClosestIon(spectra, receivedTarget.precursorMass.get, receivedTarget).get.intensity
    }

    val noise = if (noiseIons.isEmpty) {
      logger.debug("no ions found for noise calculations")
      0.0f
    }
    else {
      noiseIons.min
    }

    logger.debug(s"noise is: ${noise} for target: ${receivedTarget}")

    val replacementValueSpectra = rawdata.spectra.filter { spectra =>
      //find the closest possible mass
      includeMass(receivedTarget, filterByMass, spectra)
    }

    logger.debug(s"found ${replacementValueSpectra.size} spectra, after mass filter for target ${receivedTarget.name.getOrElse("--")}")

    val filteredByTime: Seq[Feature with CorrectedSpectra] = replacementValueSpectra.filter { spectra =>
      filterByRetentionIndex.include(spectra, applicationContext)
    }
    logger.debug(s"found ${filteredByTime.size} spectra, after retention index filter for target ${receivedTarget}")

    val value: (Feature with CorrectedSpectra) = {
      if (filteredByTime.isEmpty) {
        val intensity: Float = {
          if (zeroReplacementProperties.estimateByNearestFourIons && replacementValueSpectra.nonEmpty) {
            // estimate the replacement value using the nearest four ions of matching mass
            val leftIntensities = replacementValueSpectra
              .filter(x => receivedTarget.retentionIndex > x.retentionIndex)
              .sortBy(x => Math.abs(x.retentionIndex - receivedTarget.retentionIndex))
              .take(2)

            val rightIntensities = replacementValueSpectra
              .filter(x => receivedTarget.retentionIndex < x.retentionIndex)
              .sortBy(x => Math.abs(x.retentionIndex - receivedTarget.retentionIndex))
              .take(2)

            val nearestIntensities = (leftIntensities ++ rightIntensities)
              .map(x => MassAccuracy.findClosestIon(x, receivedTarget.precursorMass.get, receivedTarget).get.intensity)

            val intensity = nearestIntensities.sum / nearestIntensities.length
            logger.warn(s"\tCreated failsafe [Feature with CorrectedSpectra] from nearest ${nearestIntensities.length} " +
              s"ions with ${intensity} intensity")
            intensity
          } else if (zeroReplacementProperties.estimateByChromatogramNoise && replacementValueSpectra.nonEmpty) {
            // estimate the replacement value by averaging the intensities of the smallest 5% of ions
            val intensities = replacementValueSpectra
              .map(x => MassAccuracy.findClosestIon(x, receivedTarget.precursorMass.get, receivedTarget).get.intensity)
              .sorted
              .take((replacementValueSpectra.length / 20.0 + 1).toInt)

            val intensity = intensities.sum / intensities.length
            logger.warn(s"\tCreated failsafe [Feature with CorrectedSpectra] from smallest ${intensities.length} " +
              s"ions with ${intensity} intensity")
            intensity
          } else {
            logger.warn("\tCreated failsafe [Feature with CorrectedSpectra] from target data and 0 intensity")
            0.0f
          }
        }

        new Feature with CorrectedSpectra {
          override val uniqueMass: Option[Double] = None
          override val signalNoise: Option[Double] = None
          override val ionMode: Option[IonMode] = Some(receivedTarget.ionMode)
          override val purity: Option[Double] = Some(0.0)
          override val sample: String = quantSample.fileName
          override val retentionTimeInSeconds: Double = receivedTarget.retentionTimeInSeconds
          override val scanNumber: Int = 0
          override val associatedScan: Option[SpectrumProperties] = None
          override val massOfDetectedFeature: Option[Ion] = Some(Ion(receivedTarget.accurateMass.get, intensity))
          override val retentionIndex: Double = receivedTarget.retentionIndex
          /**
            * Contains random metadata associated to the object we mix this into
            */
          override val metadata: Map[String, AnyRef] = Map()
        }
      } else {
        filteredByTime.maxBy { spectra =>
          MassAccuracy.findClosestIon(spectra, receivedTarget.precursorMass.get, receivedTarget).get.intensity
        }
      }
    }

    val ion = MassAccuracy.findClosestIon(value, receivedTarget.precursorMass.get, receivedTarget).get

    logger.debug(s"found best spectra for replacement: $value")

    val noiseCorrectedValue: Float = if (noise <= ion.intensity) {
      ion.intensity - noise
    } else {
      logger.warn(s"selected ion's intensity is lower than noise, replacing with 0")
      0.0f
    }

    /**
      * build target object
      */
    new ZeroreplacedTarget(value, noiseCorrectedValue, needsReplacement, fileUsedForReplacement = rawdata.fileName, ion)
  }

  /**
    *
    * @param receivedTarget
    * @param filterByMass
    * @param spectra
    * @return
    */
  private def includeMass(receivedTarget: QuantifiedTarget[Double], filterByMass: Filter[AccurateMassSupport], spectra: Feature with CorrectedSpectra) = {
    val ion = MassAccuracy.findClosestIon(spectra, receivedTarget.precursorMass.get, receivedTarget)

    if (ion.isDefined) {
      filterByMass.include(new AccurateMassSupport {
        override def accurateMass: Option[Double] = Some(ion.get.mass)
      }, applicationContext)
    }
    else {
      false
    }
  }
}

class ZeroreplacedTarget(value: Feature with CorrectedSpectra, noiseCorrectedValue: Double, needsReplacement: QuantifiedTarget[Double], fileUsedForReplacement: String, ion: Ion) extends GapFilledTarget[Double] {

  /**
    * which actual spectra has been used for the replacement
    */
  override val spectraUsedForReplacement: Feature with GapFilledSpectra[Double] = new Feature with GapFilledSpectra[Double] {
    override val uniqueMass: Option[Double] = value.uniqueMass
    override val signalNoise: Option[Double] = value.signalNoise

    override val sample: String = value.sample
    /**
      * which sample was used for the replacement
      */
    override val sampleUsedForReplacement: String = fileUsedForReplacement
    /**
      * value for this target
      */
    override val quantifiedValue: Option[Double] = Option(noiseCorrectedValue)
    /**
      * associated target
      */
    override val target: Target = ZeroreplacedTarget.this
    /**
      * mass accuracy
      */
    override val massAccuracy: Option[Double] = None
    /**
      * accuracy in ppm
      */
    override val massAccuracyPPM: Option[Double] = None
    /**
      * distance of the retention index distance
      */
    override val retentionIndexDistance: Option[Double] = Some(target.retentionIndex - value.retentionIndex)

    override val retentionIndex: Double = value.retentionIndex
    /**
      * how pure this spectra is
      */
    override val purity: Option[Double] = value.purity
    /**
      * the local scan number
      */
    override val scanNumber: Int = value.scanNumber
    /**
      * the retention time of this spectra. It should be provided in seconds!
      */
    override val retentionTimeInSeconds: Double = value.retentionTimeInSeconds
    /**
      * specified ion mode for the given feature
      */
    override val ionMode: Option[IonMode] = value.ionMode
    /**
      * accurate mass of this feature, if applicable
      */
    override val massOfDetectedFeature: Option[Ion] = Option(ion)
    /**
      * the associated complete scan for this feature
      */
    override val associatedScan: Option[SpectrumProperties] = value.associatedScan
    /**
      * Contains random metadata associated to the object we mix this into
      */
    override val metadata: Map[String, AnyRef] = value.metadata
  }

  /**
    * value for this target
    */
  override val quantifiedValue: Option[Double] = Some(noiseCorrectedValue)
  /**
    * the unique inchi key for this spectra
    */
  override var inchiKey: Option[String] = needsReplacement.inchiKey
  /**
    * retention time in seconds of this target
    */
  override val retentionTimeInSeconds: Double = needsReplacement.retentionTimeInSeconds
  /**
    * a name for this spectra
    */
  override var name: Option[String] = needsReplacement.name
  /**
    * the mono isotopic mass of this spectra
    */
  override val precursorMass: Option[Double] = needsReplacement.precursorMass
  /**
    * is this a confirmed target
    */
  override var confirmed: Boolean = needsReplacement.confirmed
  /**
    * is this target required for a successful retention index correction
    */
  override var requiredForCorrection: Boolean = needsReplacement.requiredForCorrection
  /**
    * is this a retention index correction standard
    */
  override var isRetentionIndexStandard: Boolean = needsReplacement.isRetentionIndexStandard
  /**
    * retention time in seconds of this target
    */
  override val retentionIndex: Double = needsReplacement.retentionIndex
  /**
    * associated spectrum propties if applicable
    */
  override val spectrum: Option[SpectrumProperties] = needsReplacement.spectrum
  /**
    * unique mass for a given target
    */
  override val uniqueMass: Option[Double] = needsReplacement.uniqueMass
  override val ionMode: IonMode = value.ionMode.get
}
