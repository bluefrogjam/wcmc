package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms

import edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.MergeLibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.{MassAccuracy, Regression}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.CorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.exception.NotEnoughStandardsDefinedException
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms._
import edu.ucdavis.fiehnlab.ms.carrot.math.{CombinedRegression, OutlierMethods, SimilarityMethods}
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import javax.annotation.PostConstruct
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import scala.beans.BeanProperty

@Component
@Profile(Array("carrot.lcms"))
class LCMSTargetRetentionIndexCorrectionProcess @Autowired()(libraryAccess: MergeLibraryAccess, stasisClient: StasisService, correctionProperties: LCMSCorrectionProcessProperties) extends CorrectionProcess(libraryAccess, stasisClient) with Logging {
  var massAccuracySetting: Double = correctionProperties.massAccuracySetting
  var massAccuracyPPMSetting: Double = correctionProperties.massAccuracyPPMSetting
  var rtAccuracySetting: Double = correctionProperties.rtAccuracySetting
  var intensityPenaltyThreshold: Float = correctionProperties.intensityPenaltyThreshold
  var minPeakIntensity: Float = correctionProperties.minPeakIntensity
  var minimumDefinedStandard: Int = correctionProperties.minimumDefinedStandard
  var minimumFoundStandards: Int = correctionProperties.minimumFoundStandards
  var linearSamples: Int = correctionProperties.linearSamples
  var polynomialOrder: Int = correctionProperties.polynomialOrder
  var groupCloseByRetentionIndexStandardDifference: Int = correctionProperties.groupCloseByRetentionIndexStandardDifference

  @PostConstruct
  def printConfig(): Unit = {}

  /**
    * this defines our regression curve, which is supposed to be utilized during the correction. Lazy loading is required to avoid null pointer exception of the configuration settings
    */
  override lazy val regression: Regression = new CombinedRegression(linearSamples, polynomialOrder)

  /**
    * attmeps to find a Seq of possible matching spectra
    *
    * @param standard
    * @param spectra
    */
  def findMatch(standard: Target, spectra: Seq[_ <: Feature], filters: SequentialAnnotate): Seq[_ <: Feature] = {
    //evaluate all our filters
    spectra.filter(filters.isMatch(_, standard)).seq
  }

  /**
    * attempts to find the best hit. In case we have multiple annotations
    *
    * @param standard
    * @param spectra
    * @return
    */
  def findBestHit(standard: Target, spectra: Seq[_ <: Feature]): TargetAnnotation[Target, Feature] = {
    //best hit is defined as the mass with the smallest mass error
    //if we prefer mass accuracy
    //    TargetAnnotation(standard, spectra.minBy(spectra => MassAccuracy.calculateMassError(spectra, standard)))
    //if we we prefer mass intensity
    //    TargetAnnotation(standard, spectra.minBy(x => Math.abs(x.retentionTimeInSeconds - standard.retentionIndex)))
    //if we we prefer mass difference
    //     if we prefer a combination of the two
    //    TargetAnnotation(standard, spectra.maxBy(x =>
    //      SimilarityMethods.featureTargetSimilarity(x, standard, massAccuracySetting, rtAccuracySetting, intensityPenaltyThreshold))
    //    )

    TargetAnnotation(standard, spectra.maxBy(x =>
      SimilarityMethods.featureTargetSimilarity(x, standard, massAccuracySetting, rtAccuracySetting))
    )
  }

  /**
    * runs several optimization algorithms over the Seq of matches and returns the same Seq of a subset
    *
    * @param matches
    * @return
    */
  def optimize(matches: Seq[TargetAnnotation[Target, Feature]]): Seq[TargetAnnotation[Target, Feature]] = {
    //if several adducts are found, which come at the same time, only use the one which is closest to it's accurate mass
    //sorting is required since groupBy doesn't respect order
    val result = matches.groupBy(_.target.retentionIndex).collect {
      case x if x._2.nonEmpty =>
        // x._2.minBy(y => Math.abs(y.target.accurateMass.get - y.annotation.accurateMass.get))

        // Instead of using the best mass accuracy, use the most abundant adduct for correction
        x._2.maxBy(_.annotation.massOfDetectedFeature.get.intensity)
    }.toSeq.sortBy(_.target.retentionIndex)

    logger.info(s"after optimization, we kept ${result.size} ri standards out of ${matches.size}")

    result
  }

  override protected def findCorrectionTargets(input: Sample, targets: Iterable[Target], method: AcquisitionMethod): Seq[TargetAnnotation[Target, Feature]] = {

    val istdTargets: Iterable[Target] = targets.filter(_.isRetentionIndexStandard)

    if (istdTargets.size < minimumDefinedStandard) {
      throw new NotEnoughStandardsDefinedException(s"we require a defined minimum of ${minimumDefinedStandard} retention index standard for this correction to work. But only ${istdTargets.size} standards were provided")
    }
    else {
      logger.info(s"${istdTargets.size} standards were defined")
    }

    /**
      * allows us to filter the data by the height of the ion
      */
    val massIntensity = new MassAccuracyPPMorDalton(massAccuracyPPMSetting, massAccuracySetting, minIntensity = minPeakIntensity)
    logger.info(s"correction minimum peak intensity: ${minPeakIntensity}")

    //our defined filters to find possible matches are registered in here
    val filters: SequentialAnnotate = new SequentialAnnotate(massIntensity :: List())

    /**
      * find most likely correct matches for our specified standards
      *   1) require an intensity of at least intensityPenaltyThreshold
      *   2) remove outliers, keeping at least the top 3 hits
      *   3) calculate the average difference between the feature RT and the library RI
      *   4) use this offset to correct the target library and rerun target matching
      */
    def accessorFunction(x: TargetAnnotation[Target, Feature]): Double = x.annotation.retentionTimeInSeconds

    val initialFilteredMatches: Iterable[TargetAnnotation[Target, Feature]] = OutlierMethods.eliminateOutliers(
      matchTargetsToFeatures(input, istdTargets, filters)
        .filter(x => x.annotation.massOfDetectedFeature.get.intensity >= intensityPenaltyThreshold),
      accessorFunction
    )

    val rtDiff: Double = initialFilteredMatches
      .map(x => x.annotation.retentionTimeInSeconds - x.target.retentionIndex)
      .sum / initialFilteredMatches.size

    // create updated set of targets with shifted retention times
    val rtShiftedIstdTargets: Iterable[Target] = targets.map(x =>
      new Target {
        override var name: Option[String] = x.name
        override val retentionIndex: Double = x.retentionIndex - rtDiff
        override var inchiKey: Option[String] = x.inchiKey
        override val precursorMass: Option[Double] = x.precursorMass
        override val uniqueMass: Option[Double] = x.uniqueMass
        override var confirmed: Boolean = x.confirmed
        override var requiredForCorrection: Boolean = x.requiredForCorrection
        override var isRetentionIndexStandard: Boolean = x.isRetentionIndexStandard
        override val spectrum: Option[SpectrumProperties] = x.spectrum
      }
    )

    /**
      * find best matches based on updated RI library
      */
    val matches: Seq[TargetAnnotation[Target, Feature]] = matchTargetsToFeatures(input, rtShiftedIstdTargets, filters)

    logger.info(s"${matches.size} possible matches for RI markers were found")

    //do some optimization for us
    val optimizedMatches = optimize(matches)
    optimizedMatches
  }

  /**
    *
    * @param input
    * @param targets
    * @param filters
    * @return
    */
  private def matchTargetsToFeatures(input: Sample, targets: Iterable[Target], filters: SequentialAnnotate): Seq[TargetAnnotation[Target, Feature]] = {

    targets.toSeq.sortBy(_.retentionTimeInMinutes).collect {

      //find a possible match
      case target: Target =>

        logger.debug(s"looking for matches for ${target.name.get} (${target.accurateMass.get}@${target.retentionTimeInSeconds})")
        val result = findMatch(target, input.spectra, filters)

        //nothing found, return null
        if (result.isEmpty) {
          logger.debug("\t=>\tno hits found for this standard")
          None
        }
        //1 found, perfect
        else if (result.size == 1) {
          logger.debug(s"\t=>\t(${result.head.accurateMass.get}@${result.head.retentionTimeInSeconds} <${result.head.massOfDetectedFeature.get}>) found for this target")
          TargetAnnotation[Target, Feature](target, result.head)
        }
        //otherwise let's find the best hit
        else {
          logger.debug(s"\t=>\t${result.size} hits found for this standard")
          result.foreach(h => logger.debug(s"\t\t-\t${h.accurateMass.get}@${result.head.retentionTimeInSeconds} <${result.head.massOfDetectedFeature.get}>)"))
          val best = findBestHit(target, result)
          logger.debug(s"\tbest: (${result.head.accurateMass.get}@${result.head.retentionTimeInSeconds} <${result.head.massOfDetectedFeature.get}>)")
          best
        }
    }.collect {
      //just a quick filter so we only return objects of type hit
      case hit: TargetAnnotation[Target, Feature] =>
        logger.info(f"annotated: ${hit.target} with Annotation(rt(s):${hit.annotation.retentionTimeInSeconds}%.2f, mass:${hit.annotation.massOfDetectedFeature.get.mass}%.4f), " +
          f"massErrorDa: ${MassAccuracy.calculateMassError(hit.annotation, hit.target).get}%.6f, " +
          f"massErrorPPM: ${MassAccuracy.calculateMassErrorPPM(hit.annotation, hit.target).get}%.2f")
        hit
    }.seq
  }


  /**
    * minimum required count of standards found
    *
    * @return
    */
  override protected def getMinimumFoundStandards: Int = minimumFoundStandards
}
