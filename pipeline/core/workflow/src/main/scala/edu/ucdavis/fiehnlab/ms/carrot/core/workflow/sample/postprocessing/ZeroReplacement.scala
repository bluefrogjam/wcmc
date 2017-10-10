package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.{MassAccuracy, Regression}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Target, _}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter.{IncludeByMassRangePPM, IncludeByRetentionIndexTimeWindow}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.LCMSTargetRetentionIndexCorrection
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * Created by wohlg on 7/11/2016.
  */
abstract class ZeroReplacement extends PostProcessing[Double] with LazyLogging {

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
	def replaceValue(needsReplacement: QuantifiedTarget[Double], sample: QuantifiedSample[Double], rawdata: CorrectedSample): GapFilledTarget[Double]

	/**
		* actually processes the item
		*
		* @param sample
		* @return
		*/
	final override def doProcess(sample: QuantifiedSample[Double]): QuantifiedSample[Double] = {

    //contains a bug doing unnessescary search against the server. A collect first would be more appropriate
    //to check if a file exist
    val rawdata: Option[Sample] = zeroReplacementProperties.fileExtension.collect {

			case extension: String =>
				val fileNameToLoad = sample.name + "." + extension
				logger.debug(s"attempting to load file: ${fileNameToLoad}")

				try {
					val result = sampleLoader.loadSample(fileNameToLoad)

          if (result.isDefined) {
            logger.info(s"loaded rawdata file: ${result.get}")
            result.get
          }
        } catch {
          case e: Throwable =>
            logger.warn(s"observed error: ${e.getMessage} => skip", e)
        }
    }.collectFirst { case p: Sample => p }

		if (rawdata.isDefined) {
			logger.info(s"replacing data with: ${rawdata.get}")
			val correctedRawData: CorrectedSample = correction.doCorrection(sample.featuresUsedForCorrection, rawdata.get, sample.regressionCurve, sample)

			logger.info(s"corrected data for: ${correctedRawData.name}")

      val replacedSpectra: Seq[QuantifiedTarget[Double]] = sample.quantifiedTargets.map { target =>
        if (target.quantifiedValue.isDefined) {
          target
        }
        else {
          try {
            replaceValue(target, sample, correctedRawData)
          }
          catch {
            case e: Exception =>
              logger.error(s"replacement failed for entry, ignore for now: ${e.getMessage}", e)
              target
          }
        }
      }

      logger.info("finished replacement")
      new GapFilledSample[Double] {
        override val quantifiedTargets: Seq[QuantifiedTarget[Double]] = replacedSpectra
        override val noneAnnotated: Seq[_ <: Feature with CorrectedSpectra] = sample.noneAnnotated
        override val correctedWith: Sample = sample.correctedWith
        override val featuresUsedForCorrection: Seq[TargetAnnotation[Target, Feature]] = sample.featuresUsedForCorrection
        override val regressionCurve: Regression = sample.regressionCurve
        override val fileName: String = sample.fileName
        /**
          * which file was used for the gap filling
          */
        override val gapFilledWithFile: String = rawdata.get.fileName
      }
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
  var noiseWindowInSeconds: Int = 0

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
  var fileExtension: List[String] = "mzML" :: List()
}

/**
  * Finds the noise value for a peak and substract it from the local maximum for the provided ion and mass
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
		* @param sample
		* @param rawdata
		* @return
		*/
	override def replaceValue(needsReplacement: QuantifiedTarget[Double], sample: QuantifiedSample[Double], rawdata: CorrectedSample): GapFilledTarget[Double] = {
		val receivedTarget = needsReplacement

    val filterByMass = new IncludeByMassRangePPM(receivedTarget, zeroReplacementProperties.massAccuracyPPM)
    val filterByRetentionIndexNoise = new IncludeByRetentionIndexTimeWindow(receivedTarget.retentionTimeInSeconds, zeroReplacementProperties.noiseWindowInSeconds)
    val filterByRetentionIndex = new IncludeByRetentionIndexTimeWindow(receivedTarget.retentionIndex, zeroReplacementProperties.retentionIndexWindowForPeakDetection)

    //first calculate noise for this ion trace
    val noiseSpectra = rawdata.spectra.filter { spectra =>

      if (zeroReplacementProperties.noiseWindowInSeconds == 0) {
        includeMass(receivedTarget, filterByMass, spectra)
      }
      else {
        includeMass(receivedTarget, filterByMass, spectra) && filterByRetentionIndexNoise.include(spectra)
      }
    }

    logger.debug(s"found ${noiseSpectra.size} spectra, to utilize for noise calculation")

    val noise = noiseSpectra.map { spectra =>
      MassAccuracy.findClosestIon(spectra, receivedTarget.precursorMass.get).get.intensity
    }.min

    logger.debug(s"noise is: ${noise} for target: ${receivedTarget}")

    val replacementValueSpectra = rawdata.spectra.filter { spectra =>

      //find the closes possible mass
      includeMass(receivedTarget, filterByMass, spectra)

    }

    logger.debug(s"found ${replacementValueSpectra.size} spectra,after mass filter for target ${receivedTarget}")

    val filteredByTime: Seq[Feature with CorrectedSpectra] = replacementValueSpectra.filter { spectra =>
      filterByRetentionIndex.include(spectra)
    }
    logger.debug(s"found ${filteredByTime.size} spectra,after mass filter for target ${receivedTarget}")

    //error here, sometime mass is not found for some reason and so things are failing
    val value: (Feature with CorrectedSpectra) = filteredByTime.maxBy { spectra =>
      MassAccuracy.findClosestIon(spectra, receivedTarget.precursorMass.get).get.intensity
    }

    logger.debug(s"found best spectra for replacement: $value")
    val noiseCorrectedValue: Double = MassAccuracy.findClosestIon(value, receivedTarget.precursorMass.get).get.intensity

    /**
      * build target object
      */
    new ZeroreplacedTarget(value, noiseCorrectedValue, needsReplacement, fileUsedForReplacement = rawdata.fileName)
  }

  /**
    *
    * @param receivedTarget
    * @param filterByMass
    * @param spectra
    * @return
    */
  private def includeMass(receivedTarget: QuantifiedTarget[Double], filterByMass: IncludeByMassRangePPM, spectra: Feature with CorrectedSpectra) = {
    val ion = MassAccuracy.findClosestIon(spectra, receivedTarget.precursorMass.get)

    if (ion.isDefined) {
      filterByMass.include(new AccurateMassSupport {
        /**
          * returns the accurate mass, of this trait
          *
          * @return
          */
        override def accurateMass: Option[Double] = Some(ion.get.mass)
      }
      )
    }
    else {
      false
    }
  }
}

class ZeroreplacedTarget(value: Feature with CorrectedSpectra, noiseCorrectedValue: Double, needsReplacement: QuantifiedTarget[Double], fileUsedForReplacement: String) extends GapFilledTarget[Double] {

  /**
    * which actual spectra has been used for the replacement
    */
  override val spectraUsedForReplacement: Feature with GapFilledSpectra[Double] = new Feature with GapFilledSpectra[Double] {
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
      * accyracy in ppm
      */
    override val massAccuracyPPM: Option[Double] = None
    /**
      * distance of the retention index distance
      */
    override val retentionIndexDistance: Option[Double] = None

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
    override val massOfDetectedFeature: Option[Ion] = value.massOfDetectedFeature


  }

  /**
    * value for this target
    */
  override val quantifiedValue: Option[Double] = Some(noiseCorrectedValue)
  /**
    * the unique inchi key for this spectra
    */
  override val inchiKey: Option[String] = needsReplacement.inchiKey
  /**
    * retention time in seconds of this target
    */
  override val retentionTimeInSeconds: Double = needsReplacement.retentionTimeInSeconds
  /**
    * a name for this spectra
    */
  override val name: Option[String] = needsReplacement.name
  /**
    * the mono isotopic mass of this spectra
    */
  override val precursorMass: Option[Double] = needsReplacement.precursorMass
  /**
    * is this a confirmed target
    */
  override val confirmed: Boolean = needsReplacement.confirmed
  /**
    * is this target required for a successful retention index correction
    */
  override val requiredForCorrection: Boolean = needsReplacement.requiredForCorrection
  /**
    * is this a retention index correction standard
    */
  override val isRetentionIndexStandard: Boolean = needsReplacement.isRetentionIndexStandard
  /**
    * retention time in seconds of this target
    */
  override val retentionIndex: Double = needsReplacement.retentionIndex
  /**
    * associated spectrum propties if applicable
    */
  override val spectrum: Option[SpectrumProperties] = needsReplacement.spectrum
}
