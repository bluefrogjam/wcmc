package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.SpectraHelper
import edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation.{AccurateMassAnnotation, MassIsHighEnoughAnnotation, SequentialAnnotate}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.{MassAccuracy, Regression}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.AnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms._
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.WorkflowProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.exception._
import edu.ucdavis.fiehnlab.ms.carrot.math.CombinedRegression
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component

/**
  * targeted retention index correction, should be refactored to be a super class to make things easier
  */
@Component
class LCMSTargetRetentionIndexCorrection @Autowired()(val libraryAccess: LibraryAccess[RetentionIndexTarget], properties: WorkflowProperties) extends AnnotationProcess[RetentionIndexTarget, Sample, CorrectedSample](libraryAccess, properties.trackChanges) with LazyLogging {

  @Value("${wcms.pipeline.workflow.config.correction.peak.mass.accuracy:0.015}")
  val massAccuracySetting: Double = 0.015

  /**
    * absolute value of the height of a peak, to be considered a retention index marker. This is a hard cut off
    * and will depend on inject volume for thes e reasons
    */
  @Value("${wcms.pipeline.workflow.config.correction.peak.intensity:10000}")
  val minPeakIntensity: Float =10000

  /**
    * minimum amount of standards, which have to be defined for this method to work
    */
  @Value("${wcms.pipeline.workflow.config.correction.regression.polynom:5}")
  var minimumDefinedStandard: Int = 5

  /**
    * this defines how many standards we need to find on minimum
    * for a retention index correction method to be successful
    */
  @Value("${wcms.pipeline.workflow.config.correction.regression.polynom:5}")
  var minimumFoundStandards: Int = 16

  /**
    * we are utilizing the setting to group close by retention targets. This is mostly required, since we can't guarantee the order
    * if markers, if they come at the same time, but have different ionization and so we rather drop them
    * and only use one ionization product.
    *
    * This step happens after the required attribute was checked and so should not cause any issues with the required standards
    *
    * This setting needs to be provided in seconds
    */
  @Value("${wcms.pipeline.workflow.config.correction.groupStandard:10}")
  var groupCloseByRetentionIndexStandardDifference: Int = 10
  /**
    * how many data points are required for the linear regression at the beginning and the end of the curve
    */
  @Value("${wcms.pipeline.workflow.config.correction.regression.linear:2}")
  val linearSamples: Int = 2

  /**
    * what order is the polynomial regression
    */
  @Value("${wcms.pipeline.workflow.config.correction.regression.polynom:5}")
  val polynomialOrder: Int = 5

  /**
    * needs to be lazily loaded, since the correction settings need to be set first by spring
    */
  lazy val massAccuracy = new AccurateMassAnnotation(massAccuracySetting, 0)

  /**
    * allows us to filter the data by the height of the ion
    */
  lazy val massIntensity = new MassIsHighEnoughAnnotation(massAccuracySetting, minPeakIntensity)

  /**
    * this defines our regression curve, which is supposed to be utilized during the correction. Lazy loading is required to avoid null pointer exception of the configuration settings
    */
  lazy val regression: Regression = new CombinedRegression(linearSamples, polynomialOrder)

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
  def findBestHit(standard: RetentionIndexTarget, spectra: Seq[_ <: Feature]): TargetAnnotation[RetentionIndexTarget, Feature] = {

    //sort by accuracy first
    val sortedByAccuracy = spectra.sortBy { spectra =>
      val mass = standard.monoIsotopicMass.get
      val bestIon = MassAccuracy.findClosestIon(spectra, mass /*, correctionSettings.massAccuracy*/).get
      val distance = Math.abs(mass - bestIon.mass)

      //that's our distance
      distance
    }
    //return the closest mass distance wise
    TargetAnnotation[RetentionIndexTarget, Feature](standard, sortedByAccuracy.head)
  }

  /**
    * runs several optimization algorithms over the Seq of matches and returns the same Seq of a subset
    *
    * @param matches
    * @return
    */
  def optimize(matches: Seq[TargetAnnotation[RetentionIndexTarget, Feature]]): Seq[TargetAnnotation[RetentionIndexTarget, Feature]] = {
    logger.debug(s"\t=> matches before optimization: ${matches.size}")
    val result = matches.zipWithIndex.collect {
      case (target, position) =>
        logger.debug(s"\t\t=> ${position} of ${target}")
        if (position == 0) {
          target
        }
        else {
          val min = target.target.retentionTimeInSeconds - groupCloseByRetentionIndexStandardDifference
          val max = target.target.retentionTimeInSeconds + groupCloseByRetentionIndexStandardDifference
          val previousTarget = matches(position - 1)
          val previousTime = previousTarget.target.retentionTimeInSeconds
          val result = previousTime >= min && previousTime <= max

          if (result) {
            /**
              * we only need to optimize the targets, if the times are out of order
              * i
              */
            val previousAnnotationTime = previousTarget.annotation.retentionTimeInSeconds
            val currentAnnotationTime = target.annotation.retentionTimeInSeconds

            if (previousAnnotationTime < currentAnnotationTime) {
              /**
                * times are in order, returning the target
                */
              target
            }
            else {
              logger.debug(s"\t\t\t=> dropping ${target} since it's to close to ${previousTarget} and annotations was not in order")
            }
          }
          else {
            target
          }
        }
    }.collect {
      //filter nulls from the original collect
      case target: TargetAnnotation[RetentionIndexTarget, Feature] => target
    }

    logger.debug(s"\tmatches after optimization: ${result.size}")
    result
  }

  /**
    * verifies that one standard is not annotate twice
    *
    * @param optimizedMatches
    * @return
    */
  def verifyAnnotations(optimizedMatches: Seq[TargetAnnotation[RetentionIndexTarget, Feature]], input: Sample) = {
    if (optimizedMatches.map(_.target).toSet.size != optimizedMatches.size) {
      new StandardAnnotatedTwice(s"one of the standards, was annotated twice in sample ${input.fileName}!")
    }
  }

  /**
    * subclasses need to overwrite this method with the exact wished behavior
    *
    * @param input
    * @return
    */
  override def process(input: Sample, targets: Iterable[RetentionIndexTarget]): CorrectedSample = {

    logger.debug(s"correction sample: ${input}")

    if (targets.size < minimumDefinedStandard) {
      throw new NotEnoughStandardsDefinedException(s"we require a defined minimum of ${minimumDefinedStandard} retention index standard for this correction to work. But only ${targets.size} standards were provided")
    }
    else {
      logger.debug(s"${targets.size} standards were defined")
    }


    //our defined filters to find possible matches are registered in here
    val filters: SequentialAnnotate = new SequentialAnnotate(massAccuracy :: massIntensity :: List())

    /**
      * find possible matches for our specified targets
      */
    val matches: Seq[TargetAnnotation[RetentionIndexTarget, Feature]] = targets.toSeq.sortBy(_.retentionTimeInMinutes).par.collect {

      //find a possible match
      case target: RetentionIndexTarget =>
        logger.debug(s"looking for matches for ${target}")
        val result = findMatch(target, input.spectra, filters)

        //nothing found, return null
        if (result.isEmpty) {
          if (target.required) {
            throw new RequiredStandardNotFoundException(s"this target ${target} was not found during the detection phase, but it's required. Sample was ${input.fileName}")
          }
          else {
            logger.debug("\t=>\tno hits found for this target")
            None
          }
        }
        //1 found, perfect
        else if (result.size == 1) {
          logger.debug(s"\t=>\t${result.head} found for this target")
          TargetAnnotation[RetentionIndexTarget, Feature](target, result.head)
        }
        //otherwise let's find the best hit
        else {
          logger.debug(s"\t=>\t${result.size} hits found for this target")
          findBestHit(target, result)
        }
    }

      .collect {
        //just a quick filter so we only return objects of type hit
        case hit: TargetAnnotation[RetentionIndexTarget, Feature] =>
          logger.debug(s"annotated: ${hit.target} with ${hit.annotation}")
          hit
      }.seq

    //do some optimization for us
    val optimizedMatches = optimize(matches)

    //make sure they are in numerical order
    verifyOrder(optimizedMatches, input)

    //verify we got enough standards
    verifyCount(optimizedMatches, input)

    //verify that there
    verifyAnnotations(optimizedMatches, input)

    //do the actual correction and return the sample for further processing
    doCorrection(optimizedMatches, input, regression, input)
  }

  /**
    * check that the found annotations are enough to satisfy the need for correction
    *
    * @param possibleHits
    */
  def verifyCount(possibleHits: Seq[TargetAnnotation[RetentionIndexTarget, Feature]], input: Sample) = {
    //ensure we found enough standards
    if (possibleHits.size < minimumFoundStandards) {
      throw new NotEnoughStandardsFoundException(s"sorry we did not find enough standards in this sample: ${input.fileName} for a successful correction. We only found ${possibleHits.size}, but require ${minimumFoundStandards}")
    }
  }

  /**
    * executes the actual correction of the samples
    *
    * @param possibleHits
    * @param sampleToCorrect
    * @param regression
    * @return
    */
  def doCorrection(possibleHits: Seq[TargetAnnotation[RetentionIndexTarget, Feature]], sampleToCorrect: Sample, regression: Regression, sampleUsedForCorrection: Sample): CorrectedSample = {

    val x: Array[Double] = possibleHits.map(_.target.retentionTimeInSeconds.toDouble).toArray
    val y: Array[Double] = possibleHits.map(_.annotation.retentionTimeInSeconds.toDouble).toArray

    regression.calibration(x, y)

    logger.debug(s"${regression.toString}\n")

    val correctedSpectra: Seq[_ <: Feature with CorrectedSpectra] = sampleToCorrect.spectra.map(x => SpectraHelper.addCorrection(x,regression.computeY(x.retentionTimeInSeconds)))

    /**
      * generates a new corrected sample object
      * and computes all it's properties
      */

    new CorrectedSample {
      //needs to be possible to replace this with a different sample later
      override val correctedWith: Sample = sampleUsedForCorrection

      //generate our new spectra collection
      override val spectra: Seq[_ <: Feature with CorrectedSpectra] = correctedSpectra

      //the original data, this sample is based on
      override val featuresUsedForCorrection: Seq[TargetAnnotation[RetentionIndexTarget, Feature]] = possibleHits
      override val regressionCurve: Regression = regression
      override val fileName: String = sampleToCorrect.fileName
    }

  }

  /**
    * verifies the order of the annotated retention index standards or throws an exception
    *
    * @param possibleHits
    */
  def verifyOrder(possibleHits: Seq[TargetAnnotation[RetentionIndexTarget, Feature]], input: Sample) = {
    //brian would suggest to delete standards, which are out of order in case they are the same compound with different ionisations and come very close together
    if (!possibleHits.sliding(2).forall(x => x.head.annotation.retentionTimeInSeconds < x.last.annotation.retentionTimeInSeconds)) {
      throw new StandardsNotInOrderException(s"one or more standards where not annotated in ascending order of there retention times! Sample was ${input.fileName}")
    }
  }
}
