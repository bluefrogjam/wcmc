package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.diagnostics.JSONSampleLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.Regression
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.CorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.exception.NotEnoughStandardsDefinedException
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms._
import edu.ucdavis.fiehnlab.ms.carrot.math.{CombinedRegression, SimilarityMethods}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * targeted retention index correction, should be refactored to be a super class to make things easier
  */
@Component
@Profile(Array("carrot.lcms"))
class LCMSTargetRetentionIndexCorrectionProcess @Autowired()(libraryAccess: LibraryAccess[CorrectionTarget], val config: LCMSCorrectionLibraryProperties) extends CorrectionProcess(libraryAccess) with LazyLogging {
  /**
    * Mass accuracy (in Dalton) used in target filtering and similarity calculation
    */
  @Value("${wcmc.pipeline.workflow.config.correction.peak.mass.accuracy:0.015}")
  val massAccuracySetting: Double = 0.0

  /**
    * Retention time accuracy (in seconds) used in target filtering and similarity calculation
    */
  @Value("${wcmc.pipeline.workflow.config.correction.peak.rt.accuracy:12}")
  val rtAccuracySetting: Double = 0.0

  /**
    * Intensity used for penalty calculation - the peak similarity score for targets below this
    * intensity will be scaled down by the ratio of the intensity to this threshold
    */
  @Value("${wcmc.pipeline.workflow.config.correction.peak.intensityPenaltyThreshold:1000}")
  val intensityPenaltyThreshold: Float = 0

  /**
    * absolute value of the height of a peak, to be considered a retention index marker. This is a hard cut off
    * and will depend on inject volume for these reasons
    */
  @Value("${wcmc.pipeline.workflow.config.correction.peak.intensity:1000}")
  val minPeakIntensity: Float = 0

  /**
    * minimum amount of standards, which have to be defined for this method to work
    */
  @Value("${wcmc.pipeline.workflow.config.correction.minimumDefinedStandard:5}")
  var minimumDefinedStandard: Int = 5

  /**
    * this defines how many standards we need to find on minimum
    * for a retention index correction method to be successful
    */
  @Value("${wcmc.pipeline.workflow.config.correction.minimumFoundStandards:14}")
  var minimumFoundStandards: Int = 0
  /**
    * how many data points are required for the linear regression at the beginning and the end of the curve
    */
  @Value("${wcmc.pipeline.workflow.config.correction.regression.linear:2}")
  val linearSamples: Int = 0

  /**
    * what order is the polynomial regression
    */
  @Value("${wcmc.pipeline.workflow.config.correction.regression.polynom:3}")
  val polynomialOrder: Int = 0
  /**
    * we are utilizing the setting to group close by retention targets. This is mostly required, since we can't guarantee the order
    * if markers, if they come at the same time, but have different ionization and so we rather drop them
    * and only use one ionization product.
    *
    * This step happens after the required attribute was checked and so should not cause any issues with the required standards
    *
    * This setting needs to be provided in seconds
    */
  @Value("${wcmc.pipeline.workflow.config.correction.groupStandard:25}")
  var groupCloseByRetentionIndexStandardDifference: Int = 0

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
    //sorting is required since groupBy doesn't respsect order
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
    val massIntensity = new MassAccuracyPPMorMD(5, massAccuracySetting, "correction", minIntensity = minPeakIntensity) with JSONSampleLogging {
      /**
        * which sample we require to log
        */
      override protected val sampleToLog: String = input.fileName
    }


    //our defined filters to find possible matches are registered in here
    val filters: SequentialAnnotate = new SequentialAnnotate(massIntensity :: List()) with JSONSampleLogging {
      /**
        * which sample we require to log
        */
      override protected val sampleToLog: String = input.fileName
    }

    /**
      * find possible matches for our specified standards
      */
    val matches: Seq[TargetAnnotation[Target, Feature]] = {

      val matches = istdTargets.toSeq.sortBy(_.retentionTimeInMinutes).collect {

        //find a possible match
        case target: Target =>

          logger.debug(s"looking for matches for ${target.name.get}")
          val result = findMatch(target, input.spectra, filters)

          //nothing found, return null
          if (result.isEmpty) {
            logger.debug("\t=>\tno hits found for this standard")
            None
          }
          //1 found, perfect
          else if (result.size == 1) {
            logger.debug(s"\t=>\t(${result.head.accurateMass.get}:${result.head.retentionTimeInSeconds}) found for this target")
            TargetAnnotation[Target, Feature](target, result.head)
          }
          //otherwise let's find the best hit
          else {
            logger.debug(s"\t=>\t${result.size} hits found for this standard")
            findBestHit(target, result)
          }
      }.collect {
        //just a quick filter so we only return objects of type hit
        case hit: TargetAnnotation[Target, Feature] =>
          logger.info(s"annotated: ${hit.target.name.getOrElse("Unknown")} => ${hit.target.retentionIndex}s ${hit.target.precursorMass.getOrElse(0)}Da with ${hit.annotation.retentionTimeInSeconds}s ${hit.annotation.massOfDetectedFeature.get.mass}Da")
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
