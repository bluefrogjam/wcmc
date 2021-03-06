package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms

import edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.MergeLibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.{MassAccuracy, Regression}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.CorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.exception.NotEnoughStandardsDefinedException
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms._
import edu.ucdavis.fiehnlab.ms.carrot.math.{CombinedRegression, SimilarityMethods}
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import javax.annotation.PostConstruct
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import scala.beans.BeanProperty

/**
  * targeted retention index correction, should be refactored to be a super class to make things easier
  */
@Component
@Profile(Array("carrot.lcms"))
@ConfigurationProperties(prefix = "wcmc.workflow.lcms.process.correction", ignoreUnknownFields = false, ignoreInvalidFields = false)
class LCMSCorrectionProcessProperties {

  @BeanProperty
  var massAccuracySetting: Double = 0.01

  /**
    * MassAccuracy in PPM for correction target search
    */
  @BeanProperty
  var massAccuracyPPMSetting: Double = 10

  /**
    * Retention time accuracy (in seconds) used in target filtering and similarity calculation
    */
  @BeanProperty
  var rtAccuracySetting: Double = 12

  /**
    * Intensity used for penalty calculation - the peak similarity score for targets below this
    * intensity will be scaled down by the ratio of the intensity to this threshold
    */
  @BeanProperty
  var intensityPenaltyThreshold: Float = 10000

  /**
    * absolute value of the height of a peak, to be considered a retention index marker. This is a hard cut off
    * and will depend on inject volume for these reasons
    */
  @BeanProperty
  var minPeakIntensity: Float = 1000

  /**
    * minimum amount of standards, which have to be defined for this method to work
    */
  @BeanProperty
  var minimumDefinedStandard: Int = 5

  /**
    * this defines how many standards we need to find on minimum
    * for a retention index correction method to be successful
    */
  @BeanProperty
  var minimumFoundStandards: Int = 5
  /**
    * how many data points are required for the linear regression at the beginning and the end of the curve
    */
  @BeanProperty
  var linearSamples: Int = 2

  /**
    * what order is the polynomial regression
    */
  @BeanProperty
  var polynomialOrder: Int = 3

  /**
    * we are utilizing the setting to group close by retention targets. This is mostly required, since we can't guarantee the order
    * if markers, if they come at the same time, but have different ionization and so we rather drop them
    * and only use one ionization product.
    *
    * This step happens after the required attribute was checked and so should not cause any issues with the required standards
    *
    * This setting needs to be provided in seconds
    */
  @BeanProperty
  var groupCloseByRetentionIndexStandardDifference: Int = 25
}

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
  def printConfig(): Unit = {
  }

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
    //if we prefer mass accurracy
    //    TargetAnnotation(standard, spectra.minBy(spectra => MassAccuracy.calculateMassError(spectra, standard)))
    //if we we prefer mass intensity
    //    TargetAnnotation(standard, spectra.minBy(x => Math.abs(x.retentionTimeInSeconds - standard.retentionIndex)))
    //if we we prefer mass difference
    //     if we prefer a combination of the two

    val best = TargetAnnotation(standard, spectra.maxBy(x =>
      SimilarityMethods.featureTargetSimilarity(x, standard, massAccuracySetting, rtAccuracySetting, intensityPenaltyThreshold))
    )

    best
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
    val istdTargets = targets.filter(_.isRetentionIndexStandard)

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
      * find possible matches for our specified standards
      */
    val matches: Seq[TargetAnnotation[Target, Feature]] = {

      val matches = istdTargets.toSeq.sortBy(_.retentionTimeInMinutes).collect {

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

      matches
    }

    logger.info(s"${matches.size} possible matches for RI markers were found")
    //do some optimization for us
    val optimizedMatches = optimize(matches)
    optimizedMatches
  }

  /**
    * minimum required count of standards found
    *
    * @return
    */
  override protected def getMinimumFoundStandards: Int = minimumFoundStandards
}
