package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.diagnostics.JSONSampleLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.Regression
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.CorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.exception.{NotEnoughStandardsDefinedException, RequiredStandardNotFoundException}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms._
import edu.ucdavis.fiehnlab.ms.carrot.math.CombinedRegression
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * targeted retention index correction, should be refactored to be a super class to make things easier
  */
@Component
@Profile(Array("carrot.lcms"))
class LCMSTargetRetentionIndexCorrectionProcess @Autowired()(libraryAccess: LibraryAccess[Target]) extends CorrectionProcess(libraryAccess) with LazyLogging {

  @Value("${wcmc.pipeline.workflow.config.correction.peak.mass.accuracy:0.015}")
  val massAccuracySetting: Double = 5

  /**
    * absolute value of the height of a peak, to be considered a retention index marker. This is a hard cut off
    * and will depend on inject volume for thes e reasons
    */
  @Value("${wcmc.pipeline.workflow.config.correction.peak.intensity:1000}")
  val minPeakIntensity: Float = 10000

  /**
    * minimum amount of standards, which have to be defined for this method to work
    */
  @Value("${wcmc.pipeline.workflow.config.correction.minimumDefinedStandard:5}")
  var minimumDefinedStandard: Int = 5

  /**
    * this defines how many standards we need to find on minimum
    * for a retention index correction method to be successful
    */
  @Value("${wcmc.pipeline.workflow.config.correction.regression.polynom:5}")
  var minimumFoundStandards: Int = 16
  /**
    * how many data points are required for the linear regression at the beginning and the end of the curve
    */
  @Value("${wcmc.pipeline.workflow.config.correction.regression.linear:2}")
  val linearSamples: Int = 2

  /**
    * what order is the polynomial regression
    */
  @Value("${wcmc.pipeline.workflow.config.correction.regression.polynom:5}")
  val polynomialOrder: Int = 5
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
  var groupCloseByRetentionIndexStandardDifference: Int = 10

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
    //TargetAnnotation(standard, spectra.minBy(spectra => MassAccuracy.calculateMassError(spectra, standard)))
    //if we we prefer mass intensity
    TargetAnnotation(standard, spectra.minBy(x => Math.abs(x.retentionTimeInSeconds - standard.retentionIndex)))
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
      case x if x._2.nonEmpty => x._2.minBy(y => Math.abs(y.target.accurateMass.get - y.annotation.accurateMass.get))
    }.toSeq.sortBy(_.target.retentionIndex)

    logger.info(s"after optimization, we kepts ${result.size} ri standards out of ${matches.size}")

    result
  }

  protected def findCorrectionTargets(input: Sample, target: Iterable[Target],method: AcquisitionMethod): Seq[TargetAnnotation[Target, Feature]] = {
    val targets = target.filter(_.isRetentionIndexStandard)
    logger.debug(s"correction sample: ${input} with ${targets.size} defined standards")

    if (targets.size < minimumDefinedStandard) {
      throw new NotEnoughStandardsDefinedException(s"we require a defined minimum of ${minimumDefinedStandard} retention index standard for this correction to work. But only ${targets.size} standards were provided")
    }
    else {
      logger.info(s"${targets.size} standards were defined")
    }


    /**
      * needs to be lazily loaded, since the correction settings need to be set first by spring
      */
    val massAccuracy = new MassAccuracyPPMorMD(5, massAccuracySetting, "correction") with JSONSampleLogging{
      /**
        * which sample we require to log
        */
      override protected val sampleToLog: String = input.fileName
    }

    /**
      * allows us to filter the data by the height of the ion
      */
    val massIntensity = new MassAccuracyPPMorMD(5, massAccuracySetting, "correction", minIntensity = minPeakIntensity) with JSONSampleLogging{
      /**
        * which sample we require to log
        */
      override protected val sampleToLog: String = input.fileName
    }


    //our defined filters to find possible matches are registered in here
    val filters: SequentialAnnotate = new SequentialAnnotate(massAccuracy :: massIntensity :: List()) with JSONSampleLogging{
      /**
        * which sample we require to log
        */
      override protected val sampleToLog: String = input.fileName
    }

    /**
      * find possible matches for our specified standards
      */
    val matches: Seq[TargetAnnotation[Target, Feature]] = targets.toSeq.sortBy(_.retentionTimeInMinutes).collect {

      //find a possible match
      case target: Target =>
        logger.debug(s"looking for matches for ${target}")
        val result = findMatch(target, input.spectra, filters)

        //nothing found, return null
        if (result.isEmpty) {
          if (target.requiredForCorrection) {
            throw new RequiredStandardNotFoundException(s"this target ${target} was not found during the detection phase, but it's required. Sample was ${input.fileName}")
          }
          else {
            logger.debug("\t=>\tno hits found for this standard")
            None
          }
        }
        //1 found, perfect
        else if (result.size == 1) {
          logger.debug(s"\t=>\t${result.head} found for this target")
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
        logger.info(s"annotated: ${hit.target.name.getOrElse("Unknown")}/${hit.target.retentionIndex}/${hit.target.precursorMass.getOrElse(0)} with ${hit.annotation.retentionTimeInSeconds}s ${hit.annotation.massOfDetectedFeature.get.mass}Da")
        hit
    }.seq

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
